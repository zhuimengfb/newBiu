package com.biu.biu.main;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.biu.biu.app.BiuApp;
import com.biu.biu.thread.PostNewTopicTempThread;
import com.biu.biu.tools.EditTextLengthIndicate;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.views.base.BaseActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

public class PublishTopicActivity extends BaseActivity implements
                                                       AMapLocationListener {
  private String mTopicId = null;
  private EditText mPublishContent;
  private CheckBox mShowPlaceCheckbox;
  private TextView mContentCount;
  private EditTextLengthIndicate mContentLengthIndicate;
  private TextView mshowPlace;
  /*private Button mTabTopSubmit;
  private ImageButton mTabBackbt;*/
  private double mLat = 3.0;
  private double mLng = 125.0;
  private boolean mIsShowPlace = false; // 是否显示我的位置，关联选择框
  private String mPlace = null; // 存储我的地址文本
  private String mCapturePhotoPath; // 拍照得到的相片存储地址
  private PublishTopicTipHandler mHandler; // 处理消息
  private ImageView mAddPhotoImage;
  private final int REQUEST_CODE_PICK_IMAGE = 100;
  private final int REQUEST_CODE_CAPTURE_CAMEIA = 101;
  private final int REQUEST_CODE_CHOOSE_IMAGE_SOURCE = 102;
  private Bitmap mPhoto;
  private ImageView mImageToPublish;
  private boolean mHasImage = false; // 发帖是否含有图片，初始为否
  private boolean mThreadIsRunning = false; // 表征线程是否正在执行
  private int mPublishMode = PUBLISH_FOR_HOMETIP; // 默认为首页发表模式

  private static final int MAX_COUNT = 200;
  private static final int POST_TOPIC_TIP_OK = 0;
  private static final int POST_TOPIC_TIP_ERROR = - 1;
  protected boolean mhasgetlatlng;
  private boolean mCanbePublish = true;

  // 发表activity的模式，决定发表帖子时的操作
  public final static int PUBLISH_FOR_HOMETIP = 0; // 发表首页帖子
  public final static int PUBLISH_FOR_MOONBOOX = 1; // 发表月光宝盒帖子
  public final static int PUBLISH_FOR_PEEPTOPIC = 2; // 发表偷看话题帖子

  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.toolbar_title)
  TextView toolbarTitle;
  @BindView(R.id.fab_topic_done)
  FloatingActionButton fabTopicDone;
  @BindView(R.id.iv_anonymity_publish)
  ImageView anonymityPublish;

  private boolean isAnonymityPublish = false;

  private byte[] mBitmapbyteArray; // 存储压缩图片之后的二维数组

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_publish_topic);
    ButterKnife.bind(this);
    findId();
    initConfigParam();
    initToolbar();
    initFab();
    initView();
  }

  private void initToolbar() {
    setSupportActionBar(toolbar);
    setBackableToolbar(toolbar);
    toolbarTitle.setText(getString(R.string.publish_topic));
  }

  private void initFab() {
    fabTopicDone.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        synchronized (this) {
          if (! mCanbePublish) {
            return;
          }
          String text = mPublishContent.getText().toString();
          if (text.isEmpty()) {
            Toast.makeText(PublishTopicActivity.this, "请输入新话题内容！",
                Toast.LENGTH_SHORT).show();
            return;
          }
          mCanbePublish = false;
          Toast.makeText(PublishTopicActivity.this, "正在发表新主题，请耐心等待！",
              Toast.LENGTH_SHORT).show();
          PostNewTip();
        }
      }
    });
  }

  /**
   * 发表帖子
   */
  protected void PostNewTip() {
    // TODO Auto-generated method stub
    if (mThreadIsRunning) {
      return;
    }
    String url = "";
    PostNewTopicTempThread thread = null;
    switch (mPublishMode) {
      case PUBLISH_FOR_HOMETIP:
        // 生成post目标url
        url = "http://api.bbbiu.com:1234/devices/"
            + UserConfigParams.device_id + "/threads";
        // 生成要传递的参数
        Map<String, String> parametersmap = new HashMap<String, String>();
        String content = mPublishContent.getText().toString();
        parametersmap.put("title", content); // 帖子标题，暂时与内容相同
        parametersmap.put("content", content);
        // 经度纬度
        // 如果自己定位了，就使用发表页定的位，更精确。否则使用全局定位信息
        if (mhasgetlatlng) {
          parametersmap.put("lat", String.valueOf(mLat));
          parametersmap.put("lng", String.valueOf(mLng));
        } else {
          if (! UserConfigParams.hasGettedLocation()) {
            Toast.makeText(PublishTopicActivity.this, "无定位信息，无法发表新话题！",
                Toast.LENGTH_SHORT).show();
            return;
          }
          // 使用全局定位信息
          parametersmap.put("lat", UserConfigParams.latitude);
          parametersmap.put("lng", UserConfigParams.longitude);

        }
        // 发表地址
        if (mIsShowPlace) {
          parametersmap.put("address", mPlace);
        }
        if (isAnonymityPublish) {
          parametersmap.put("anony", String.valueOf(1));
        } else {
          parametersmap.put("anony", String.valueOf(0));
        }
        parametersmap.put("type", "0");
        // thread = new PostNewTopicThread(mCapturePhotoPath, mHandler, url,
        // parametersmap);
        thread = new PostNewTopicTempThread(mCapturePhotoPath, mHandler,
            url, parametersmap);
        // thread = new PostNewTopicThread(mBitmapbyteArray, mHandler, url,
        // parametersmap); // 这种方式似乎不行啊，放弃直接发表字节流的试图
        break;
      case PUBLISH_FOR_MOONBOOX:
        // 月光宝盒
        // 生成post目标url
        url = "http://api.bbbiu.com:1234/devices/"
            + UserConfigParams.device_id + "/threads";
        // 生成要传递的参数
        Map<String, String> moonParametersMap = new HashMap<String, String>();
        String mooncontent = mPublishContent.getText().toString();
        moonParametersMap.put("title", mooncontent); // 帖子标题，暂时与内容相同
        moonParametersMap.put("content", mooncontent);
        // 经度纬度
        // 如果自己定位了，就使用发表页定的位，更精确。否则使用全局定位信息
        if (mhasgetlatlng) {
          moonParametersMap.put("lat", String.valueOf(mLat));
          moonParametersMap.put("lng", String.valueOf(mLng));
        } else {
          if (! UserConfigParams.hasGettedLocation()) {
            Toast.makeText(PublishTopicActivity.this, "无定位信息，无法发表新话题！",
                Toast.LENGTH_SHORT).show();
            return;
          }
          // 使用全局定位信息
          moonParametersMap.put("lat", UserConfigParams.latitude);
          moonParametersMap.put("lng", UserConfigParams.longitude);
        }
        // 发表地址
        if (mIsShowPlace) {
          moonParametersMap.put("address", mPlace);
        }
        // 月光宝盒多出来一个type参数
        moonParametersMap.put("type", "1");
        // thread = new PostNewTopicThread(mCapturePhotoPath, mHandler, url,
        // moonParametersMap);
        thread = new PostNewTopicTempThread(mCapturePhotoPath, mHandler,
            url, moonParametersMap);
        // thread = new PostNewTopicThread(mBitmapbyteArray, mHandler, url,
        // moonParametersMap);
        break;
      case PUBLISH_FOR_PEEPTOPIC:
        url = "http://api.bbbiu.com:1234/topic/"
            + UserConfigParams.device_id + "/release";
        String contentTemp = mPublishContent.getText().toString();
        // thread = new PostNewTopicThread(mCapturePhotoPath, mHandler, url,
        // mTopicId, contentTemp);
        // 话题发帖需要添加临时位置
        if (mhasgetlatlng) {
          thread = new PostNewTopicTempThread(mCapturePhotoPath,
              mHandler, url, mTopicId, contentTemp, mLat, mLng);
        } else {
          if (! UserConfigParams.hasGettedLocation()) {
            Toast.makeText(PublishTopicActivity.this, "无定位信息，无法发表新话题！",
                Toast.LENGTH_SHORT).show();
            return;
          }

          // 使用全局定位信息
          thread = new PostNewTopicTempThread(mCapturePhotoPath,
              mHandler, url, mTopicId, contentTemp,
              Double.valueOf(UserConfigParams.latitude),
              Double.valueOf(UserConfigParams.longitude));

        }
        Log.i("topic------", mLat + "话题发帖");
        Log.i("topic------", UserConfigParams.latitude + "话题发帖");
        // thread = new PostNewTopicThread(mBitmapbyteArray, mHandler, url,
        // mTopicId, contentTemp);

        break;
      default:
        Log.e("PublishTopicActivity", "错误的发表模式");

    }
    // // 向服务器发送发表帖子的post信息
    if (thread != null) {
      if (! mThreadIsRunning) {
        mThreadIsRunning = true;
        thread.start();
      }
    }

  }

  private void initView() {
    // TODO Auto-generated method stub
    mHandler = new PublishTopicTipHandler(this);
    // 字数的动态处理，限长200
    // mPublishContent.addTextChangedListener(mTextWatcher);
    mContentLengthIndicate.bindEditText(mPublishContent, 200);
    mPublishContent.setSelection(mPublishContent.length()); // 随着输入内容移动光标
    // setLeftCount();
    mPublishContent.setGravity(Gravity.TOP);
    mPublishContent.setSingleLine(false);
    mPublishContent.setHorizontallyScrolling(false);

    // 显示我的地址
    mShowPlaceCheckbox
        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            // TODO Auto-generated method stub
            if (isChecked) {
              mIsShowPlace = true;
              mshowPlace.setText(mPlace);
            } else {
              mIsShowPlace = false;
              mshowPlace.setText(R.string.showmyplace);
            }
          }
        });

    // 右上角的提交内容
        /*mTabTopSubmit.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 发送Post消息给服务器
				// if(!mCanbePublish)
				// return;
				synchronized (this) {
					if (!mCanbePublish)
						return;
					String text = mPublishContent.getText().toString();
					if (text.isEmpty()) {
						Toast.makeText(PublishTopicActivity.this, "请输入新话题内容！",
								Toast.LENGTH_SHORT).show();
						return;
					}
					mCanbePublish = false;
					Toast.makeText(PublishTopicActivity.this, "正在发表新主题，请耐心等待！",
							Toast.LENGTH_SHORT).show();
					PostNewTip();
				}

			}
		});*/

    // 左上角回退按钮
        /*mTabBackbt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});*/

    // 添加图片
    mAddPhotoImage.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        // openOptionsMenu();
        selectPicture();
      }
    });
    anonymityPublish.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        isAnonymityPublish = ! isAnonymityPublish;
        if (isAnonymityPublish) {
          Toast.makeText(BiuApp.getContext(), "匿名发表", Toast.LENGTH_SHORT).show();
          setAnonymityPublish();
        } else {
          Toast.makeText(BiuApp.getContext(), "非匿名发表", Toast.LENGTH_SHORT).show();
          setAutonymPublish();
        }
      }
    });
  }

  private void selectPicture() {
    Intent intent = new Intent();
    intent.setClass(PublishTopicActivity.this,
        ChooseImgResActivity.class);
    startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE_SOURCE);
  }

  private void initConfigParam() {
    // TODO Auto-generated method stub
    Intent intent = this.getIntent();
    mTopicId = intent.getStringExtra("topic_id"); // 话题ID
    mPublishMode = intent.getIntExtra("PublishMode", PUBLISH_FOR_HOMETIP);
  }

  private void findId() {
    // TODO Auto-generated method stub
    mPublishContent = (EditText) findViewById(R.id.publishcontent); // 发表内容编辑框
    mAddPhotoImage = (ImageView) findViewById(R.id.addphoto); // 给发表帖子添加图片
    mShowPlaceCheckbox = (CheckBox) findViewById(R.id.showplacecheck); // 控制显示本地地址信息Checkbox
    // mContentCount = (TextView)findViewById(R.id.publishcontcount); // 字数
    mContentLengthIndicate = (EditTextLengthIndicate) findViewById(R.id.publishcontcount);
    mImageToPublish = (ImageView) findViewById(R.id.imagetopublish); // 要发表的图片
    mshowPlace = (TextView) findViewById(R.id.publish_showplace);
		/*mTabTopSubmit = (Button) findViewById(R.id.submit_new); // 右上角的提交按钮
		mTabBackbt = (ImageButton) findViewById(R.id.publish_back); // 左上角回退按钮*/
  }

  /**
   * 创建菜单
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // TODO Auto-generated method stub
    /*menu.add(Menu.NONE, Menu.FIRST + 1, 1, "拍照");
    menu.add(Menu.NONE, Menu.FIRST + 2, 2, "图片");*/
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // TODO Auto-generated method stub
    switch (item.getItemId()) {
      case Menu.FIRST + 1: // 拍照
        getImageFromCamera();
        break;
      case Menu.FIRST + 2: // 图片
        Intent intent = new Intent();
        intent.setClass(PublishTopicActivity.this,
            ChooseImageActivity.class);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        // showLocateSettingDlg();
        break;
      default:

    }
    return super.onOptionsItemSelected(item);
  }

  // 计算图片合适的压缩比例
  public static int calculateInSampleSize(BitmapFactory.Options options,
      int reqWidth, int reqHeight) {
    // 源图片的高度和宽度
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;
    if (height > reqHeight || width > reqWidth) {
      // 计算出实际宽高和目标宽高的比率
      final int heightRatio = Math.round((float) height
          / (float) reqHeight);
      final int widthRatio = Math.round((float) width / (float) reqWidth);
      // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
      // 一定都会大于等于目标的宽和高。
      inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
    }
    return inSampleSize;
  }

  /**
   * 按照给定imageView的大小，返回生成的bitmap图片对象。
   *
   * @param imgPathName
   * @param resId
   * @param reqWidth
   * @param reqHeight
   * @return
   */
  public static Bitmap decodeSampledBitmapFromResource(String imgPathName,
      int resId, int reqWidth, int reqHeight) {
    // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(imgPathName, options);
    // 调用上面定义的方法计算inSampleSize值
    options.inSampleSize = calculateInSampleSize(options, reqWidth,
        reqHeight);
    // 使用获取到的inSampleSize值再次解析图片
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeFile(imgPathName, options);
    // return BitmapFactory.decodeResource(res, resId, options);
  }

  /**
   * 质量压缩方法，循环判断压缩后是否大于100k,大于则继续压缩
   *
   * @param image
   * @return
   */
  private Bitmap compressImage(Bitmap image) {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Log.i("pp", "100%");
    image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
    int options = 70; // 获得他人建议，直接以90开始压缩
    while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
      baos.reset();// 重置baos即清空baos
      image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
      options -= 10;// 每次都减少10
      Log.i("pp", "option：" + options);
    }
    // mBitmapbyteArray = baos.toByteArray(); // 存储压缩之后的字节流,使用字节流好像不行
    ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//
    // 把压缩后的数据baos存放到ByteArrayInputStream中
    Log.i("pp", "开始解流");
    Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
    return bitmap;
  }

  /**
   * 图片按比例大小压缩
   *
   * @param srcPath
   * @return
   */
  private Bitmap getImage(String srcPath) {
    BitmapFactory.Options newOpts = new BitmapFactory.Options();
    // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
    newOpts.inJustDecodeBounds = true;
    Bitmap bitmap = null;
    bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

    newOpts.inJustDecodeBounds = false;
    int w = newOpts.outWidth;
    int h = newOpts.outHeight;
    // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
    float hh = 800f;// 这里设置高度为800f
    float ww = 480f;// 这里设置宽度为480f
    // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
    int be = 1;// be=1表示不缩放
    if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
      be = (int) (newOpts.outWidth / ww);
    } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
      be = (int) (newOpts.outHeight / hh);
    }
    Log.i("pp", "缩放比例为" + be);
    if (be <= 0) {
      be = 1;
    }

    newOpts.inSampleSize = be;// 设置缩放比例
    // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
    bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
    return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
  }

  private void setUploadPicture() {
    mAddPhotoImage.setImageURI(Uri.parse("android.resource://" + getPackageName() + "/" + R
        .drawable.upload_picture_after));
  }

  private void cancelUploadPicture() {
    mAddPhotoImage.setImageURI(Uri.parse("android.resource://" + getPackageName() + "/" + R
        .drawable.upload_picture_before));
  }

  private void setAutonymPublish() {
    anonymityPublish.setImageURI(Uri.parse("android.resource://" + getPackageName() + "/" + R
        .drawable.anonymity_before));
  }

  private void setAnonymityPublish() {
    anonymityPublish.setImageURI(Uri.parse("android.resource://" + getPackageName() + "/" + R
        .drawable.anonymity_after));
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    switch (requestCode) {
      case REQUEST_CODE_PICK_IMAGE:// 从本地选择完毕图片
        if (data == null) {
          return;
        }
        Uri uri = data.getData();
        // to do find the path of pic
        if (uri != null) {
          if (mPhoto != null) {
            mPhoto.recycle();
            mPhoto = null;
          }
          // 若图片太大，不直接解析，而是通过缩小
          mPhoto = getImage(uri.getPath()); // 获取图片用于显示ImageView
          // mPhoto = BitmapFactory.decodeFile(uri.getPath());

          // 做两件事情：1.生成缩略图用于imageView显示。2.缩小图片用于发送
          // mPhoto = decodeSampledBitmapFromResource(uri.getPath(),
          // mImageToPublish.getId(), 100, 100);
          mCapturePhotoPath = writCompressedFile(mPhoto); // 将压缩过的图片写入缓存目录
          // mPhoto.compress(Bitmap.CompressFormat.JPEG, 100,
          // mUploadImgOutstream); // 为发表时使用的输出流赋值
          // mCapturePhotoPath = uri.getPath();
          mImageToPublish.setImageBitmap(mPhoto);
          mHasImage = true;
          mImageToPublish.setVisibility(View.VISIBLE);
          setUploadPicture();
        }
        break;
      case REQUEST_CODE_CAPTURE_CAMEIA: // 拍了一张照片
        if (resultCode == RESULT_OK) {

          if (mPhoto != null) {
            mPhoto.recycle();
            mPhoto = null;
          }
          // mPhoto = data.getParcelableExtra("data");
          mPhoto = getImage(mCapturePhotoPath); // 问题出在这
          mCapturePhotoPath = writCompressedFile(mPhoto);

          if (mPhoto != null) {
            // writePhotoToFile(mPhoto); // 将Bitmap对象写入文件
            mImageToPublish.setImageBitmap(mPhoto);
            mHasImage = true;
            mImageToPublish.setVisibility(View.VISIBLE);
            setUploadPicture();
          }
        }
        break;

      case REQUEST_CODE_CHOOSE_IMAGE_SOURCE: // 执行的动作是选择图片来源。
        if (0 == resultCode) {
          // 拍照
          getImageFromCamera();
        } else if (1 == resultCode) {
          // 来自图片
          Intent intent = new Intent();
          intent.setClass(PublishTopicActivity.this,
              ChooseImageActivity.class);
          startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        } else {
          // 取消，do nothing.
        }
        break;
    }
  }

  /**
   * 将压缩过的图片写入缓存目录，并返回图片所在路径
   *
   * @param bitmaptowrite
   * @return
   */
  private String writCompressedFile(Bitmap bitmaptowrite) {
    // TODO Auto-generated method stub
    File sdcache = getExternalCacheDir(); // 获得外置图片缓存路径
    File f = new File(sdcache, System.currentTimeMillis() + ".jpg");
    try {
      f.createNewFile();
    } catch (Exception e) {
      e.printStackTrace();
    }
    FileOutputStream fOut = null;
    try {
      fOut = new FileOutputStream(f);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // 按照100kb的标准，得到压缩倍率
    int nsize = 100;
    ByteArrayOutputStream sp = new ByteArrayOutputStream();
    do {
      sp.reset();
      bitmaptowrite.compress(Bitmap.CompressFormat.JPEG, nsize, sp);
      nsize -= 10;
    } while (sp.toByteArray().length / 1024 > 100);
    // 按照试出来的压缩倍率，压缩图片到文件输出流，并写入文件。
    bitmaptowrite.compress(Bitmap.CompressFormat.JPEG, nsize, fOut);
    // Log.e();
    try {
      fOut.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      fOut.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return f.getAbsolutePath();
  }

  /**
   * 将拍照得到的图片写入到SD卡中
   *
   * @param mBitmap
   */
  public void writePhotoToFile(Bitmap mBitmap) {
    // String strdir = Environment.getExternalStorageDirectory().getPath() +
    // UserConfigParams.TOPIC_PHOTO_SAVED_DIR_PATH;
    File sdcache = getExternalCacheDir(); // 获得外置图片缓存路径
    // String strdir = getExternalCacheDir();
    // File dir = new File(strdir);
    // if (!dir.exists()) {
    // dir.mkdirs();
    // }
    // mCapturePhotoPath = strdir + System.currentTimeMillis() + ".jpg";
    File f = new File(sdcache, System.currentTimeMillis() + ".jpg");
    try {
      f.createNewFile();
    } catch (Exception e) {
      e.printStackTrace();
    }
    FileOutputStream fOut = null;
    try {
      fOut = new FileOutputStream(f);
    } catch (Exception e) {
      e.printStackTrace();
    }
    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
    try {
      fOut.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      fOut.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    mCapturePhotoPath = f.getAbsolutePath();
  }

  /**
   * 销毁图片文件
   */
  private void destoryBimap() {
    if (mPhoto != null && ! mPhoto.isRecycled()) {
      mPhoto.recycle();
      mPhoto = null;
    }
  }

  /**
   * Handler
   *
   * @author grf
   */
  private static class PublishTopicTipHandler extends Handler {
    WeakReference<PublishTopicActivity> mPubTopicActwekref;

    public PublishTopicTipHandler(PublishTopicActivity activity) {
      mPubTopicActwekref = new WeakReference<PublishTopicActivity>(
          activity);
    }

    @Override
    public void handleMessage(Message msg) {
      int nMsgNo = msg.what;
      PublishTopicActivity activity = mPubTopicActwekref.get();
      if (activity.mThreadIsRunning) {
        activity.mThreadIsRunning = false; // 接到消息后，表示某个线程执行完毕。
      }
      switch (nMsgNo) {
        case PublishTopicActivity.POST_TOPIC_TIP_OK:
          // 发表成功后，关闭发表页面，回到首页，并设置令首页刷新一次
          UserConfigParams.isHomeRefresh = true;
          activity.destoryBimap(); // 释放Image对象
          UserConfigParams.isHomeRefresh = true;
          activity.finish(); // 关闭activity
          break;
        case PublishTopicActivity.POST_TOPIC_TIP_ERROR:
          activity.showError("发表新主题失败！");
          activity.setCanbePublish(true);
          break;
        default:
      }
      super.handleMessage(msg);
    }

  }

  // 从相机获取图片
  protected void getImageFromCamera() {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    // intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
    File sdcache = getExternalCacheDir(); // 获得外置图片缓存路径
    File f = new File(sdcache, System.currentTimeMillis() + ".jpg");
    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
    startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);
    mCapturePhotoPath = f.getAbsolutePath();
  }

  public void showError(String errmsg) {
    // TODO Auto-generated method stub
    Toast.makeText(PublishTopicActivity.this, errmsg, Toast.LENGTH_SHORT)
        .show();
  }

  private TextWatcher mTextWatcher = new TextWatcher() {
    private int editStart;
    private int editEnd;

    public void afterTextChanged(Editable s) {
      // editStart = mPublishContent.getSelectionStart();
      // editEnd = mPublishContent.getSelectionEnd();
      //
      // // 先去掉监听器，否则会出现栈溢出
      // mPublishContent.removeTextChangedListener(mTextWatcher);
      //
      // // 注意这里只能每次都对整个EditText的内容求长度，不能对删除的单个字符求长度
      // // 因为是中英文混合，单个字符而言，calculateLength函数都会返回1
      // while (calculateLength(s.toString()) > MAX_COUNT) { //
      // 当输入字符个数超过限制的大小时，进行截断操作
      // s.delete(editStart - 1, editEnd);
      // editStart--;
      // editEnd--;
      // }
      // mPublishContent.setText(s);
      // mCanbePublish = true;
      // mPublishContent.setSelection(editStart);
      // // 恢复监听器
      // mPublishContent.addTextChangedListener(mTextWatcher);
      // setLeftCount();
    }

    public void beforeTextChanged(CharSequence s, int start, int count,
        int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before,
        int count) {

    }
  };
  private LocationManagerProxy mLocationManagerProxy;

  /**
   * 计算发表内容的字数；一个汉字=两个英文字母，一个中文标点=两个英文标点
   *
   * @param c
   * @return
   */
  private long calculateLength(CharSequence c) {
    // TODO Auto-generated method stub
    double len = 0;
    for (int i = 0; i < c.length(); i++) {
      int tmp = (int) c.charAt(i);
      if (tmp > 0 && tmp < 127) {
        len += 0.5;
      } else {
        len++;
      }
    }
    return Math.round(len);
  }

  /**
   * 刷新剩余字数，最大值为200字
   */
  private void setLeftCount() {
    // TODO Auto-generated method stub
    mContentCount.setText(String.valueOf((MAX_COUNT - getInputCount())));
  }

  /**
   * 获取输入框内容字数
   *
   * @return
   */
  private long getInputCount() {
    // TODO Auto-generated method stub
    // 可能为空
    if (mPublishContent.getText() != null) {
      return calculateLength(mPublishContent.getText().toString());
    } else {
      return 0;
    }

  }

  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    InitLocation(); // 初始化定位操作
    // setLatLng();
    SharedPreferences preferences = getSharedPreferences("user_Params",
        MODE_PRIVATE);
    UserConfigParams.device_id = preferences.getString("device_ID", "");
    super.onResume();
  }

  private void InitLocation() {
    // TODO Auto-generated method stub
    mLocationManagerProxy = LocationManagerProxy.getInstance(this);
    mLocationManagerProxy.requestLocationData(
        LocationProviderProxy.AMapNetwork, 30 * 1000, 500, this);
  }

  @Override
  public void onLocationChanged(Location location) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onProviderDisabled(String provider) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onProviderEnabled(String provider) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onLocationChanged(AMapLocation amapLocation) {
    // TODO Auto-generated method stub
    if (amapLocation != null
        && amapLocation.getAMapException().getErrorCode() == 0) {
      // 获取位置信息
      mLat = amapLocation.getLatitude();
      mLng = amapLocation.getLongitude();
      mhasgetlatlng = true; // 自身定位得到经纬度
      mPlace = amapLocation.getCity() + amapLocation.getDistrict()
          + amapLocation.getStreet();
    } else {
      Toast.makeText(this,
          amapLocation.getAMapException().getErrorMessage(),
          Toast.LENGTH_SHORT).show();
      mLocationManagerProxy.destroy();
    }
  }

  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    if (mLocationManagerProxy != null) {
      mLocationManagerProxy.destroy();
    }
    super.onPause();
  }

  public boolean isCanbePublish() {
    return mCanbePublish;
  }

  public void setCanbePublish(boolean mCanbePublish) {
    this.mCanbePublish = mCanbePublish;
  }

  // 定义测试固定位置的帖子
  public void setLatLng() {
    UserConfigParams.latitude = "20";
    UserConfigParams.longitude = "122";
  }
}
