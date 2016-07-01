package com.biu.biu.main;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biu.biu.app.BiuApp;
import com.biu.biu.main.dialog.ShowImgDialog;
import com.biu.biu.netimage.ImageDownloader;
import com.biu.biu.netimage.OnImageDownload;
import com.biu.biu.netoperate.TipLikeTreadThread;
import com.biu.biu.user.views.ShowUserInfoActivity;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.utils.GlideCircleTransform;
import com.biu.biu.utils.GlobalString;
import com.biu.biu.utils.ShareUtils;
import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import grf.biu.R;

public class PeepDetailListViewAdapter extends BaseAdapter {

  private ArrayList<TipItemInfo> mListItemsInfo; // 列表控件数据缓存
  private final int VIEW_TYPE_TITLE = 0; // 标题
  private final int VIEW_TYPE_REPLY = 1; // 评论
  private final int VIEW_TYPE_COUNT = 2;
  private LayoutInflater mListContainer; // 视图容器工厂
  private ListView mListView = null; // 持有一个使用Adapter的ListView引用
  View mConvertView = null; // item布局视图文件
  ImageDownloader mDownloader;
  private Context mContext; // 调用者
  // private Bitmap hostimage = null;
  private int mDetailMode = 0; // 详情页模式标记，对于月光宝盒，需要特殊处理，只显示发表时间和回复数。
  // 识别是本地用户还是遊客
  private boolean localornot = true;
  private WeakReference<Activity> activityWeakReference;

  public void setActivity(Activity activity) {
    activityWeakReference = new WeakReference<>(activity);
  }

  public final class PeepDetailListItemView {
    public TextView pubtimetv; // 发表时间
    public TextView contentTv; // 文本内容
    public TextView replytimetv; // 回复时间
    public ImageButton replyimage; // 回复图标
    public TextView replyNumtv; // 回复数
    public ImageView likebtn; // 顶按钮
    public TextView likecounttv;
    public ImageButton treadbtn;
    public TextView treadcounttv;
    public ImageView imageView;
    public TextView userName;
    public ImageView shareIcon;
    public ImageView userIcon;
    public ImageView replyIcon;
    public RelativeLayout userInfoLayout;
    public ImageView userHeadIcon;
  }

  public void setListView(ListView listview) {
    this.mListView = listview;
  }

  public PeepDetailListViewAdapter(PeepDetailActivity context,
                                   ArrayList<TipItemInfo> tipInfoArray) {
    // TODO Auto-generated constructor stub
    this.mContext = context;
    this.mListItemsInfo = tipInfoArray;
    mListContainer = LayoutInflater.from(context);
    localornot = true;
  }

  public PeepDetailListViewAdapter(PeepDetailActivity context,
                                   ArrayList<TipItemInfo> tipInfoArray, boolean localornot) {
    // TODO Auto-generated constructor stub
    this.mContext = context;
    this.mListItemsInfo = tipInfoArray;
    mListContainer = LayoutInflater.from(context);
    this.localornot = localornot;
  }

  @Override
  public int getCount() {
    // TODO Auto-generated method stub
    return mListItemsInfo.size();
  }

  @Override
  public Object getItem(int position) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long getItemId(int position) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    // TODO Auto-generated method stub
    boolean isTitle = false;
    PeepDetailListItemView listItemView = null;
    isTitle = mListItemsInfo.get(position).isTitle;
    if (convertView == null) {
      if (isTitle) {
        convertView = mListContainer.inflate(R.layout.peepdetailtitleitemlayout, null);
      } else {
        convertView = mListContainer.inflate(R.layout.peepdetailreplyitemlayout, null);
      }

      // 获取要用到的控件
      listItemView = new PeepDetailListItemView();
      listItemView.userName = (TextView) convertView.findViewById(R.id.tv_user_name);
      listItemView.userName.setTypeface(BiuApp.globalTypeface);
      listItemView.contentTv = (TextView) convertView.findViewById(R.id.contenttv);
      listItemView.contentTv.setTypeface(BiuApp.globalTypeface);
      listItemView.likebtn = (ImageView) convertView.findViewById(R.id.likebtn);
      listItemView.likecounttv = (TextView) convertView.findViewById(R.id.likecounttv);
      listItemView.likecounttv.setTypeface(BiuApp.globalTypeface);
      listItemView.treadbtn = (ImageButton) convertView.findViewById(R.id.treadbtn);
      listItemView.pubtimetv = (TextView) convertView.findViewById(R.id.create_at_tv); // 发表时间
      listItemView.pubtimetv.setTypeface(BiuApp.globalTypeface);
      listItemView.replyNumtv = (TextView) (convertView.findViewById(R.id.reply_num_tv)); // 回复数
      listItemView.replyNumtv.setTypeface(BiuApp.globalTypeface);
      listItemView.userInfoLayout = (RelativeLayout) convertView.findViewById(R.id
          .user_info_layout);
      listItemView.userHeadIcon = (ImageView) convertView.findViewById(R.id.iv_head_icon);
      if (isTitle) {
        listItemView.imageView = (ImageView) convertView.findViewById(R.id.titleimg);
        listItemView.shareIcon = (ImageView) convertView.findViewById(R.id.iv_share);
        listItemView.shareIcon.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (activityWeakReference != null && activityWeakReference.get() != null) {
              ShareUtils shareUtils = new ShareUtils(activityWeakReference.get());
              shareUtils.openShare();
            }
          }
        });
      }
      convertView.setTag(listItemView);
    } else {
      listItemView = (PeepDetailListItemView) convertView.getTag();
    }

    // 设置发表时间图标
    // if(mDisplayForMoon){
    // listItemView.pubtimeImg.setVisibility(View.GONE);
    // listItemView.replyimage.setVisibility(View.INVISIBLE);
    // listItemView.likebtn.setVisibility(View.INVISIBLE);
    // listItemView.likecounttv.setVisibility(View.INVISIBLE);
    // listItemView.treadbtn.setVisibility(View.INVISIBLE);
    // listItemView.treadcounttv.setVisibility(View.INVISIBLE);
    // }else{
    //
    // }
    // 非匿名
    if (mListItemsInfo.get(position).anony == 0) {
      listItemView.userName.setText(mListItemsInfo.get(position).simpleUserInfo.getNickname());
      Glide.with(mContext).load(GlobalString.BASE_URL + "/" + mListItemsInfo.get(position)
          .simpleUserInfo.getIcon_small()).transform(new GlideCircleTransform(mContext))
          .into(listItemView.userHeadIcon);
      listItemView.userInfoLayout.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          ShowUserInfoActivity.toShowUserPicActivity(mContext, mListItemsInfo.get(position)
              .device_id);
        }
      });
    } else {
      listItemView.userName.setText("匿名");
      Glide.with(mContext).load(GlobalString.URI_RES_PREFIX + R.drawable.default_user_icon2)
          .transform(new GlideCircleTransform(mContext)).into(listItemView.userHeadIcon);
      listItemView.userInfoLayout.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
        }
      });
    }
    listItemView.contentTv.setText(mListItemsInfo.get(position).content);
    // 设置发表时间和回复数
    listItemView.pubtimetv.setText(mListItemsInfo.get(position).created_at);
    listItemView.replyNumtv.setText(mListItemsInfo.get(position).reply_num);
    if (mDetailMode == PeepDetailActivity.TIPDETAIL_FOR_MOONBOOX) {
      listItemView.likebtn.setEnabled(false);
      listItemView.likebtn.setVisibility(View.INVISIBLE);
      listItemView.likecounttv.setVisibility(View.INVISIBLE);
      listItemView.treadbtn.setEnabled(false);
      listItemView.treadbtn.setVisibility(View.INVISIBLE);
    } else {
      // 设置被顶的数目
      listItemView.likecounttv.setText(mListItemsInfo.get(position).like_num);

      // 设置动态改变顶、赞的情况
      boolean blikestate = mListItemsInfo.get(position).hasliked;
      boolean btreadstate = mListItemsInfo.get(position).hastreaded;
      if (blikestate) {
        listItemView.likebtn.setImageResource(R.drawable.like_after_icon);
        listItemView.likecounttv.setTextColor(Color.rgb(0x25, 0xd4, 0xb3));
      } else {
        listItemView.likebtn.setImageResource(R.drawable.like_before_icon);
        listItemView.likecounttv.setTextColor(Color.GRAY);
      }
      // 设置踩的状态
      if (btreadstate) {
        listItemView.treadbtn.setImageResource(R.drawable.arrow2click);
      } else {
        listItemView.treadbtn.setImageResource(R.drawable.arrow2);
      }
      // 给顶、踩添加事件监听器
      String tipid = mListItemsInfo.get(position).id;
      int nlikestate = 0;
      if (blikestate) {
        nlikestate = 1;
      } else {
        nlikestate = btreadstate ? -1 : 0;
      }
      listItemView.likebtn.setOnClickListener(new TipLikeClickListener(
          position, tipid, nlikestate));
      listItemView.treadbtn.setOnClickListener(new TipTreadClickListener(
          position, tipid, nlikestate));
    }
    // 处理图片操作
    if (isTitle) {
      String urltemp = mListItemsInfo.get(position).imgurl;
      if (!urltemp.equals("null")) {
        final String imgurl = "http://api.bbbiu.com:1234/" + urltemp;
        listItemView.imageView.setTag(String.valueOf(position));
        // listItemView.imageView.setImageResource(android.R.drawable.stat_sys_download_done);
        if (mDownloader == null) {
          mDownloader = new ImageDownloader();
        }

        final ImageView itemImageView = listItemView.imageView;
        // 添加imageView的单击事件
        itemImageView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            // TODO Auto-generated method stub/
            // mContext.startActivity(intent);
            Log.i("SHOWIMAGE", "imageview已经响应点击事件");
            // 定义一个图片对话框
            // Dialog showImgDialog = new ShowImgDialog(
            // mContext);
            itemImageView.setDrawingCacheEnabled(true);
            Bitmap content = Bitmap.createBitmap(itemImageView
                .getDrawingCache());
            // Bitmap content = itemImageView.getDrawingCache();
            itemImageView.setDrawingCacheEnabled(false);
            Dialog showImgDialog = new ShowImgDialog(mContext,
                R.style.ShowImgDialog, content);
            // Dialog showImgDialog = new ShowImgDialog(mContext,
            // R.style.ShowImgDialog);
            // 定义新的显示图片
            showImgDialog.show();
            Activity activity = (Activity) mContext;
            WindowManager windowManager = activity.getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = showImgDialog.getWindow().getAttributes();
            lp.width = (int) (display.getWidth()); // 设置宽度
            lp.height = (int) (display.getHeight());
            showImgDialog.getWindow().setAttributes(lp);
            // Intent intent = new Intent();
            // intent.setClass(mContext,
            // ShowImageActivity.class);
            // // 传递图片数据给图片显示活动
            // Bundle bundle = new Bundle();
            // Bitmap tempmap = Bitmap
            // .createBitmap(ImageView.this
            // .getDrawingCache());
            // // 不能用hostimage变量
            // // bundle.putParcelable("showimage", "");
            // intent.putExtras(bundle);
            // mContext.startActivity(intent);
          }
        });

        // 异步下载图片
        mDownloader.imageDownload(String.valueOf(position), imgurl,
            listItemView.imageView, mContext.getExternalCacheDir()
                .getPath(), (Activity) this.mContext,
            new OnImageDownload() {
              @Override
              public void onDownloadSucc(String tag,
                                         Bitmap bitmap, String c_url,
                                         ImageView mimageView) {
                ImageView imageView = (ImageView) mListView
                    .findViewWithTag(tag);
                if (imageView != null) {
                  imageView.setImageBitmap(bitmap);
                  // hostimage = bitmap;
                  imageView.setTag("");
                }
              }
            });
      } else {
        listItemView.imageView.setVisibility(View.GONE);
      }
    }
    // 设置回复内容、发表时间、顶、踩数量
    // listItemView.replytimetv.setText(mListItemsInfo.get(position).created_at);

    // 给顶、赞两个按钮添加单击事件
    // String tipid = mListItemsInfo.get(position).id;

    // listItemView.likebtn.setOnClickListener(new
    // DetailTopButtonListener(position, likecount, tipid, blikestate));
    // listItemView.treadbtn.setOnClickListener(new
    // DetailTreadButtonListener(position, treadcount, tipid, btreadstate));

    // // 显示为月光宝盒详情页
    // listItemView.pubtimeImg.setVisibility(View.GONE);
    // listItemView.replyimage.setVisibility(View.INVISIBLE);
    if (!localornot) {
      listItemView.likebtn.setVisibility(View.GONE);
      listItemView.treadbtn.setVisibility(View.GONE);
      listItemView.likecounttv.setVisibility(View.GONE);
    }

    return convertView;
  }

  // 顶贴按钮
  private class TipLikeClickListener implements OnClickListener {
    private TipLikeTreadThread mthread = null;
    private int mposition = 0;
    private int mlikeState = 0; // 0：没踩没顶 1：顶了 -1：踩了
    private String mtipid;
    private String murl = "";

    public TipLikeClickListener(int position, String tipid, int nlikestate) {
      // TODO Auto-generated constructor stub
      this.mposition = position;
      this.mlikeState = nlikestate;
      this.mtipid = tipid;
    }

    @Override
    public void onClick(View v) {
      // TODO Auto-generated method stub
      murl = "http://api.bbbiu.com:1234/topic/" + mtipid;
      Integer nlikenum = Integer
          .parseInt(mListItemsInfo.get(mposition).like_num);
      switch (mlikeState) {
        case -1:
          // 当前为踩，点击顶，取消踩。
          murl += "/action:tread" + "/?device_id="
              + UserConfigParams.device_id + "&is_repeal=1";
          ++nlikenum;
          mListItemsInfo.get(mposition).hastreaded = false;
          break;
        case 0:
          // 当前无，点击顶，设为顶
          murl += "/action:like" + "/?device_id="
              + UserConfigParams.device_id;
          ++nlikenum;
          mListItemsInfo.get(mposition).hasliked = true;
          break;
        case 1:
          // 当前顶，点击顶，取消顶
          murl += "/action:like" + "/?device_id="
              + UserConfigParams.device_id + "&is_repeal=1";
          --nlikenum;
          mListItemsInfo.get(mposition).hasliked = false;
          break;
      }
      mListItemsInfo.get(mposition).like_num = nlikenum.toString();
      // 更新显示ListView的状态
      PeepDetailListViewAdapter.this.notifyDataSetChanged();
      mthread = new TipLikeTreadThread(null, mtipid, mposition, murl);
      // mthread.setSendMessage(false); // 只执行操作，不发送消息
      Thread thread = new Thread(mthread);
      thread.start();

    }

  }

  // 踩贴点击
  private class TipTreadClickListener implements OnClickListener {
    private TipLikeTreadThread mthread = null;
    private int mposition = 0;
    private int mlikeState = 0; // 0：没踩没顶 1：顶了 -1：踩了
    private String mtipid;
    private String murl = "";

    public TipTreadClickListener(int position, String tipid, int nlikestate) {
      this.mposition = position;
      this.mtipid = tipid;
      this.mlikeState = nlikestate;
    }

    @Override
    public void onClick(View v) {
      // TODO Auto-generated method stub
      murl = "http://api.bbbiu.com:1234/topic/" + mtipid;
      Integer nlikenum = Integer
          .parseInt(mListItemsInfo.get(mposition).like_num);
      // 当前处于顶的状态
      switch (mlikeState) {
        case -1:
          // 当前为踩，点击踩，取消踩。
          murl += "/action:tread" + "/?device_id="
              + UserConfigParams.device_id + "&is_repeal=1";
          ++nlikenum;
          mListItemsInfo.get(mposition).hastreaded = false;
          break;
        case 0:
          // 当前无，点击踩，设为踩
          murl += "/action:tread" + "/?device_id="
              + UserConfigParams.device_id;
          --nlikenum;
          mListItemsInfo.get(mposition).hastreaded = true;
          break;
        case 1:
          // 当前顶，点击踩，取消顶
          murl += "/action:like" + "/?device_id="
              + UserConfigParams.device_id + "&is_repeal=1";
          --nlikenum;
          mListItemsInfo.get(mposition).hasliked = false;
          break;
      }
      mListItemsInfo.get(mposition).like_num = nlikenum.toString();
      // 更新显示ListView的状态
      PeepDetailListViewAdapter.this.notifyDataSetChanged();
      mthread = new TipLikeTreadThread(null, mtipid, mposition, murl);
      // mthread.setSendMessage(false);
      Thread thread = new Thread(mthread);
      thread.start();
    }

  }

  @Override
  public int getItemViewType(int position) {
    // TODO Auto-generated method stub
    boolean isTitle = mListItemsInfo.get(position).isTitle;
    if (isTitle) {
      return VIEW_TYPE_TITLE;
    } else {
      return VIEW_TYPE_REPLY;
    }
  }

  @Override
  public int getViewTypeCount() {
    // TODO Auto-generated method stub
    return VIEW_TYPE_COUNT;
  }

  /**
   * 设置详情适配器的适配模式，不设置时默认为HOME和偷看，使用相同适配方式；月光宝盒需要做特殊处理
   *
   * @param detailmode
   */
  public void setDetailMode(int detailmode) {
    // TODO Auto-generated method stub
    this.mDetailMode = detailmode;
  }

}
