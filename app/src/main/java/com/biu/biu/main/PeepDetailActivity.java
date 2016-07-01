package com.biu.biu.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.biu.biu.thread.GetHttpThread;
import com.biu.biu.thread.PostTopicReplyThread;
import com.biu.biu.user.entity.SimpleUserInfo;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.utils.GlobalString;
import com.biu.biu.views.base.BaseActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;


public class PeepDetailActivity extends BaseActivity {
  private final static String SHARETAG = "Share";

  private ListView mReplyListView; // 评论ListView
  private EditText mReplyContentEdit; // 回复内容
  private ImageButton mSendReplyBtn;
  private ImageButton mDeleteTipBtn = null;
  protected boolean mIsScrolltoEnd = false; // 是否将评论ListView滚动到最下面
  private TipItemInfo mTargetTipInfo = new TipItemInfo(); // 存储详情目标帖子的所有信息
  private Handler mHandler = null;
  private JSONObject mJsonObject = new JSONObject(); // 存储httpget线程返回的内容
  GetHttpThread mGettDetailThread; // 获取帖子详情线程对象
  private ArrayList<TipItemInfo> simpledatalist = new ArrayList<TipItemInfo>(); // 存储所有评论帖子
  // 适配器
  private PeepDetailListViewAdapter msimpleAdapter = null; // 回复的列表适配器
  private int mDetailMode = TIPDETAIL_FOR_HOMETIP; // 默认为首页详情模式
  private boolean mCanDeleteTip = false; // 是否显示删除接口（从我的发表进入，显示删除接口。默认为false）

  private final int GET_DETAILMSG_OK = 0;
  private final int GET_DETAILMSG_ERROR = 1;
  private final int POST_NEWREPLY_OK = 2;
  private final int POST_NEWREPLY_ERROR = -1;
  private final int DELETE_TIP_OK = 3;
  private final int DELETE_TIP_ERROR = 4;
  private String mlat = null;
  private String mlng = null;
  // 识别查看帖子详情时是本地用户还是游客
  private boolean localornot;

  // 帖子详情页面的使用模式，决定获取贴子详情的url
  public final static int TIPDETAIL_FOR_HOMETIP = 0; // 首页帖子的详情
  public final static int TIPDETAIL_FOR_MOONBOOX = 1; // 月光宝盒帖子的详情
  public final static int TIPDETAIL_FOR_PEEPTOPIC = 2; // 话题帖子的详情
  /*@BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.toolbar_title)
  TextView toolbarTitle;*/
  @BindView(R.id.toolbar_peep)
  Toolbar peepToolbar;

  @BindView(R.id.layout_anonymity)
  RelativeLayout anonymityLayout;
  @BindView(R.id.iv_annoymity_reply)
  ImageView anonymityImageView;
  @BindView(R.id.tv_send)
  TextView sendText;
  @BindView(R.id.layout_anony)
  RelativeLayout anonyLayout;

  private boolean annoyReplyFlag = false;

  // 分享
  private final UMSocialService mController = UMServiceFactory
      .getUMSocialService("com.umeng.share");

  private void configPlatforms() {
    Log.d(SHARETAG, "------>>configuration share1");
    // 添加新浪SSO授权
    // mController.getConfig().setSsoHandler(new SinaSsoHandler());
    // 添加QQ、QZone平台
    addQQQZonePlatform();
    // 添加微信、微信朋友圈平台
    addWXPlatform();
  }

  /**
   * @return
   * @功能描述 : 添加QQ平台支持 QQ分享的内容， 包含四种类型， 即单纯的文字、图片、音乐、视频. 参数说明 : title, summary,
   * image url中必须至少设置一个, targetUrl必须设置,网页地址必须以"http://"开头 . title :
   * 要分享标题 summary : 要分享的文字概述 image url : 图片地址 [以上三个参数至少填写一个] targetUrl
   * : 用户点击该分享时跳转到的目标地址 [必填] ( 若不填写则默认设置为友盟主页 )
   */
  private void addQQQZonePlatform() {
    Log.d(SHARETAG, "------>>configuration QQshare1");
    String appId = "100424468";
    String appKey = "c7394704798a158208a74ab60104f0ba";
    // 添加QQ支持, 并且设置QQ分享内容的target url
    UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this, appId, appKey);
    qqSsoHandler.setTargetUrl("http://www.bbbiu.com");
    qqSsoHandler.addToSocialSDK();

    // 添加QZone平台
    QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(this, appId,
        appKey);
    qZoneSsoHandler.addToSocialSDK();
  }

  /**
   * @return
   * @功能描述 : 添加微信平台分享
   */
  private void addWXPlatform() {
    // 注意：在微信授权的时候，必须传递appSecret
    // wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
    String appId = "wxcef5bcd54791e409";
    // String appSecret = "5bb696d9ccd75a38c8a0bfe0675559b3";
    String appSecret = "d4624c36b6795d1d99dcf0547af5443d";
    // 添加微信平台
    UMWXHandler wxHandler = new UMWXHandler(this, appId, appSecret);
    wxHandler.addToSocialSDK();

    // 支持微信朋友圈
    UMWXHandler wxCircleHandler = new UMWXHandler(this, appId, appSecret);
    wxCircleHandler.setToCircle(true);
    wxCircleHandler.addToSocialSDK();
  }

  private void setShareContent(String content, String imgUrl) {
    Log.d(SHARETAG, "------>>configuration share1 content");
    UMImage urlImage = null;
    if (imgUrl != null) {
      urlImage = new UMImage(this, imgUrl);
    } else {
      urlImage = new UMImage(this, R.drawable.icon);
      // urlImage = new UMImage(this, BitmapFactory.decodeResource(
      // getResources(), R.drawable.icon320));
      // Bitmap logo = BitmapFactory.decodeResource(getResources(),
      // R.drawable.icon);
      // UMImage urlImage2 = new UMImage(this, arg1);
    }

    // 微信分享
    WeiXinShareContent weiXinShareContent = new WeiXinShareContent();
    weiXinShareContent.setShareContent(content);
    weiXinShareContent.setTitle("这是来自Biu的内容，欢迎使用Biu！！！");
    weiXinShareContent.setTargetUrl("http://www.bbbiu.com");
    weiXinShareContent.setShareMedia(urlImage);
    mController.setShareMedia(weiXinShareContent);
    // 朋友圈分享
    CircleShareContent circleShareContent = new CircleShareContent();
    // circleShareContent.setShareContent(content);
    // circleShareContent.setTitle("这是来自Biu的内容，欢迎使用Biu！！！");
    circleShareContent.setTargetUrl("http://www.bbbiu.com");
    // 朋友圈不能显示content的内容
    circleShareContent.setShareContent(content);
    circleShareContent.setTitle(content);
    circleShareContent.setShareMedia(urlImage);
    mController.setShareMedia(circleShareContent);
    // 新浪微博分享
    // SinaShareContent sinaShareContent = new SinaShareContent();
    // sinaShareContent.setShareContent(content);
    // sinaShareContent.setTitle("这是来自Biu的内容，欢迎使用Biu！！！");
    // sinaShareContent.setShareImage(urlImage);
    // sinaShareContent.setShareMedia(sinaShareContent);

    // qq
    // 设置QQ空间分享内容
    QZoneShareContent qzone = new QZoneShareContent();
    qzone.setShareContent(content);
    qzone.setTargetUrl("http://www.bbbiu.com");
    qzone.setTitle("这是来自Biu的内容，欢迎使用Biu！！！");
    qzone.setShareMedia(urlImage);
    // qzone.setShareMedia(uMusic);
    mController.setShareMedia(qzone);

    QQShareContent qqShareContent = new QQShareContent();
    qqShareContent.setShareContent(content);
    qqShareContent.setTitle("这是来自Biu的内容，欢迎使用Biu！！！");
    qqShareContent.setShareMedia(urlImage);
    qqShareContent.setTargetUrl("http://www.bbbiu.com");
    mController.setShareMedia(qqShareContent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_peep_detail);
    ButterKnife.bind(this);
    localornot = getIntent().getBooleanExtra("localornot", true);
    findId();
    initParam();
    initView();
    initToolbar();
    msimpleAdapter = new PeepDetailListViewAdapter(this, simpledatalist, localornot);
    msimpleAdapter.setActivity(this);
    if (mDetailMode == TIPDETAIL_FOR_MOONBOOX) {
      msimpleAdapter.setDetailMode(TIPDETAIL_FOR_MOONBOOX);
    }
    mReplyListView.setAdapter(msimpleAdapter);
    msimpleAdapter.setListView(mReplyListView);
    // 配置分享平台的参数
    this.configPlatforms();
    initEvent();
  }

  private void initEvent() {
    anonymityLayout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (annoyReplyFlag) {
          anonymityImageView.setImageURI(Uri.parse(GlobalString.URI_RES_PREFIX + R.drawable
              .anonymity_reply_before));
          annoyReplyFlag = false;
        } else {
          anonymityImageView.setImageURI(Uri.parse(GlobalString.URI_RES_PREFIX + R.drawable
              .anonymity_reply_after));
          annoyReplyFlag = true;
        }
      }
    });
  }

  private void initView() {
    // TODO Auto-generated method stub
    // 处理消息的Handler
    mHandler = new Handler() {

      @Override
      public void handleMessage(Message msg) {
        // TODO Auto-generated method stub
        int nMsgNo = msg.what;
        switch (nMsgNo) {
          case GET_DETAILMSG_OK:
            // 处理帖子详情
            dealTipDetail();
            break;
          case GET_DETAILMSG_ERROR:
            break;
          case POST_NEWREPLY_ERROR:
            break;
          case POST_NEWREPLY_OK:
            // 重新获取一次帖子的详情
            //					UserConfigParams.isHomeRefresh = true; // 发表了评论后，再次回到首页要自动刷新一下
            getTipDetail();
            break;
          case DELETE_TIP_OK:
            //					UserConfigParams.isHomeRefresh = true; // 发表了评论后，再次回到首页要自动刷新一下
            finish(); // 删除帖子成功
            break;
          case DELETE_TIP_ERROR:
            break;
          default:

        }
        super.handleMessage(msg);
      }

    };

    // 发表新回复
    mSendReplyBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        String replycontent = mReplyContentEdit.getText().toString();
        if (replycontent.isEmpty()) {
          Toast.makeText(PeepDetailActivity.this, "请输入评论内容！",
              Toast.LENGTH_SHORT).show();
          return;
        }
        mIsScrolltoEnd = true;
        mReplyContentEdit.setText("");
        hintkbTwo(); // 关闭软键盘
        postNewReply(replycontent);
      }

      private void hintkbTwo() {
        // TODO Auto-generated method stub
        InputMethodManager imm = (InputMethodManager) getSystemService(Context
            .INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
          if (getCurrentFocus().getWindowToken() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus()
                    .getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
          }
        }
      }

    });
    mReplyContentEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
                    /*隐藏软键盘*/
          InputMethodManager imm = (InputMethodManager) v
              .getContext().getSystemService(
                  Context.INPUT_METHOD_SERVICE);
          if (imm.isActive()) {
            imm.hideSoftInputFromWindow(
                v.getApplicationWindowToken(), 0);
          }
          String replycontent = mReplyContentEdit.getText().toString();
          if (replycontent.isEmpty()) {
            Toast.makeText(PeepDetailActivity.this, "请输入回复内容！",
                Toast.LENGTH_SHORT).show();
            return false;
          }
          mIsScrolltoEnd = true;
          mReplyContentEdit.setText("");
          postNewReply(replycontent);
          return true;
        }
        return false;
      }
    });
    mReplyContentEdit.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        if (StringUtils.isEmpty(editable.toString())) {
          hideSendText();
          showAnony();
        } else {
          hideAnony();
          showSendText();
        }
      }
    });
    sendText.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        String replycontent = mReplyContentEdit.getText().toString();
        if (replycontent.isEmpty()) {
          Toast.makeText(PeepDetailActivity.this, "请输入回复内容！",
              Toast.LENGTH_SHORT).show();
        }
        mIsScrolltoEnd = true;
        mReplyContentEdit.setText("");
        postNewReply(replycontent);
      }
    });
    // 如果是从“我的发表”页面过来的，则显示删除按钮
    if (mCanDeleteTip) {
      // 修改后此控件已经是可见了(zb)
      mDeleteTipBtn.setVisibility(View.VISIBLE);

      mDeleteTipBtn.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          // TODO Auto-generated method stub
          new AlertDialog.Builder(PeepDetailActivity.this)
              .setMessage("是否删除？")
              .setPositiveButton("确认",
                  new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                        DialogInterface dialog,
                        int which) {
                      // TODO Auto-generated method stub
                      deleteTip();
                    }

                    // 删除这个帖子
                    private void deleteTip() {
                      // TODO Auto-generated method stub
                      String url = "http://api.bbbiu.com:1234"
                          + "/threads/"
                          + mTargetTipInfo.id;
                      DelTipThread tipThread = new DelTipThread(
                          mTargetTipInfo.id, url);
                      Thread thread = new Thread(
                          tipThread);
                      thread.start();
                      Toast.makeText(
                          PeepDetailActivity.this,
                          "帖子已删除", Toast.LENGTH_SHORT)
                          .show();
                    }
                  }).setNegativeButton("取消", null).show();

        }
      });
    } else {

      mDeleteTipBtn.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          // TODO Auto-generated method stub
          // 设置分享的内容
          // TODO Auto-generated method stub
          mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN,
              SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ,
              SHARE_MEDIA.QZONE);
          mController.openShare(PeepDetailActivity.this, false);
        }
      });

    }
    // 当发表回复编辑框获得输入焦点后，显示到
  }
  private void hideSendText() {
    sendText.setVisibility(View.GONE);
  }

  private void showSendText() {
    sendText.setVisibility(View.VISIBLE);
  }
  private void hideAnony() {
    anonyLayout.setVisibility(View.GONE);
  }

  private void showAnony() {
    anonyLayout.setVisibility(View.VISIBLE);
  }
  /**
   * 删除帖子线程
   *
   * @author grf
   */
  class DelTipThread implements Runnable {
    private String thread_id = null;
    private String url = null;

    public DelTipThread(String id, String url) {
      this.thread_id = id;
      this.url = url;
    }

    @Override
    public void run() {
      // TODO Auto-generated method stub
      HttpDelete httpdel = new HttpDelete(url);
      HttpClient httpClient = new DefaultHttpClient();
      HttpResponse httpResponse = null;
      try {
        httpResponse = httpClient.execute(httpdel);
      } catch (Exception e) {
        Message msg = Message.obtain();
        msg.what = DELETE_TIP_ERROR;
        String str = e.toString();
        Bundle bundle = new Bundle();
        bundle.putString("msg", str);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        return;
      }

      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        Message msg = Message.obtain();
        msg.what = DELETE_TIP_OK;
        mHandler.sendMessage(msg);
      } else {
        Message msg = Message.obtain();
        msg.what = DELETE_TIP_ERROR;
        String str = httpResponse.toString();
        Bundle bundle = new Bundle();
        bundle.putString("msg", str);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
      }
    }
  }

  /**
   * 处理HttpGet线程执行完毕后返回的帖子详情
   */
  protected void dealTipDetail() {
    // TODO Auto-generated method stub
    // 得到了帖子的详情页
    MyDateTimeDeal dealtime = new MyDateTimeDeal();
    mJsonObject = mGettDetailThread.getJsonObject();
    try {
      // 获得帖子的内容信息
      mTargetTipInfo.content = mJsonObject.getString("content");
      mTargetTipInfo.device_id = mJsonObject.getString("device_id");
      mTargetTipInfo.reply_num = mJsonObject.getString("reply_num");
      Integer likeresult = Integer.parseInt(mJsonObject.getString("like_num")) - Integer.parseInt
          (mJsonObject.getString("tread_num"));
      mTargetTipInfo.like_num = likeresult.toString();
      mTargetTipInfo.tread_num = mJsonObject.getString("tread_num");
      mTargetTipInfo.ishot = mJsonObject.getString("is_hot").equals("1");
      mTargetTipInfo.imgurl = mJsonObject.getString("img_url");
      // 获取图片位置
      String tempUrl = mJsonObject.getString("img_url");
      mTargetTipInfo.id = mJsonObject.getString("id");
      mTargetTipInfo.created_at = dealtime.getTimeGapDesc(mJsonObject
          .getString("created_at"));
      mTargetTipInfo.hasliked = mJsonObject.getBoolean("has_liked");
      mTargetTipInfo.hastreaded = mJsonObject.getBoolean("has_treaded");
      mTargetTipInfo.isTitle = true; // 主题,非评论

      mTargetTipInfo.anony = Integer.parseInt(mJsonObject.getString("anony"));
      SimpleUserInfo simpleUserInfo = new SimpleUserInfo();
      if (!mJsonObject.isNull("publisher") && mJsonObject.getJSONObject("publisher") != null) {
        JSONObject userJson = mJsonObject.getJSONObject("publisher");
        simpleUserInfo.setJm_id(userJson.getString("jm_id"));
        simpleUserInfo.setDevice_id(userJson.getString("jm_id"));
        simpleUserInfo.setNickname(userJson.getString("nickname"));
        simpleUserInfo.setIcon_small(userJson.getString("icon_small"));
        simpleUserInfo.setIcon_large(userJson.getString("icon_small"));
      }
      mTargetTipInfo.simpleUserInfo = simpleUserInfo;
      // 将主题添加到标记中
      simpledatalist.clear(); // 每次获得帖子评论都重新输入评论内容
      simpledatalist.add(mTargetTipInfo);
      // 该帖子的评论内容
      JSONArray jsonArray = mJsonObject.getJSONArray("replies");
      MyDateTimeDeal timedeal = new MyDateTimeDeal();
      for (int i = 0; i < jsonArray.length(); i++) {
        TipItemInfo replyIteminfo = new TipItemInfo();
        JSONObject everyJsonObject = jsonArray.getJSONObject(i);
        replyIteminfo.id = everyJsonObject.getString("id");
        replyIteminfo.device_id = everyJsonObject.getString("device_id");
        replyIteminfo.content = everyJsonObject.getString("content");
        replyIteminfo.created_at = timedeal
            .getTimeGapDesc(everyJsonObject.getString("created_at"));
        replyIteminfo.like_num = everyJsonObject.getString("like_num");
        replyIteminfo.tread_num = everyJsonObject.getString("tread_num");
        replyIteminfo.hasliked = everyJsonObject.getBoolean("has_liked");
        replyIteminfo.hastreaded = everyJsonObject.getBoolean("has_treaded");

        if (everyJsonObject.isNull("anony")) {
          replyIteminfo.anony = 1;
        } else {
          replyIteminfo.anony = Integer.parseInt(everyJsonObject.getString("anony"));
        }
        SimpleUserInfo simpleReplyUserInfo = new SimpleUserInfo();
        if (!everyJsonObject.isNull("publisher") && everyJsonObject.getJSONObject("publisher")
            != null) {
          JSONObject userReplyJson = everyJsonObject.getJSONObject("publisher");
          simpleReplyUserInfo.setJm_id(userReplyJson.getString("jm_id"));
          simpleReplyUserInfo.setDevice_id(userReplyJson.getString("jm_id"));
          simpleReplyUserInfo.setNickname(userReplyJson.getString("nickname"));
          simpleReplyUserInfo.setIcon_small(userReplyJson.getString("icon_small"));
          simpleReplyUserInfo.setIcon_large(userReplyJson.getString("icon_small"));
          replyIteminfo.simpleUserInfo = simpleReplyUserInfo;
        }
        simpledatalist.add(replyIteminfo);
      }
      msimpleAdapter.notifyDataSetChanged();
      if (mIsScrolltoEnd) {
        mReplyListView.setSelection(msimpleAdapter.getCount() - 1);
        mIsScrolltoEnd = false;
      }

      // 定义分享的内容
      if (tempUrl != null && !tempUrl.equals("null")) {
        String imgUrl = "http://api.bbbiu.com:1234/" + tempUrl;
        setShareContent(mTargetTipInfo.content, imgUrl);
      } else {
        setShareContent(mTargetTipInfo.content, null);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * 获取帖子的详情
   */
  protected synchronized void getTipDetail() {
    // TODO Auto-generated method stub
    String url = "";
    switch (mDetailMode) {
      case TIPDETAIL_FOR_HOMETIP:
      case TIPDETAIL_FOR_MOONBOOX:
        // 月光宝盒和首页详情使用同一个接口
        url = "http://api.bbbiu.com:1234/threads/" + mTargetTipInfo.id
            + "?device_id=" + UserConfigParams.device_id;
        break;
      case TIPDETAIL_FOR_PEEPTOPIC:
        url = "http://api.bbbiu.com:1234" + "/topic/" + mTargetTipInfo.id
            + "?device_id=" + UserConfigParams.device_id;
        break;
      default:

    }
    mGettDetailThread = new GetHttpThread(mHandler, url);
    mGettDetailThread.setReturnMsgCode(GET_DETAILMSG_OK,
        GET_DETAILMSG_ERROR);
    mGettDetailThread.setJsonObject(mJsonObject);
    mGettDetailThread.start();
  }

  /**
   * 向服务器发表新的评论
   *
   * @param replycontent
   */
  protected void postNewReply(String replycontent) {
    // TODO Auto-generated method stub
    String url = null;
    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    switch (mDetailMode) {
      case TIPDETAIL_FOR_HOMETIP:
        // 首页回复，有经纬度，type=0
        nameValuePairs.add(new BasicNameValuePair("title", ""));
        nameValuePairs.add(new BasicNameValuePair("lat", mlat));
        nameValuePairs.add(new BasicNameValuePair("lng", mlng));
        nameValuePairs.add(new BasicNameValuePair("type", "0"));
        // 指向首页的帖子回复URL
        // url = "http://api.bbbiu.com:1234/devices/"
        // + UserConfigParams.device_id + "/release/";
        break;
      case TIPDETAIL_FOR_MOONBOOX:
        // 月光回复，有经纬度，type=1
        nameValuePairs.add(new BasicNameValuePair("title", ""));
        nameValuePairs.add(new BasicNameValuePair("lat", mlat));
        nameValuePairs.add(new BasicNameValuePair("lng", mlng));
        nameValuePairs.add(new BasicNameValuePair("type", "1"));
        // 指向首页的帖子回复URL
        // url = "http://api.bbbiu.com:1234/devices/"
        // + UserConfigParams.device_id + "/release/";
        break;
      case TIPDETAIL_FOR_PEEPTOPIC:
        // 偷看回复，无经纬度，type=0(服务器默认)
        nameValuePairs.add(new BasicNameValuePair("type", "3"));
        // 添加经纬度坐标
        nameValuePairs.add(new BasicNameValuePair("lat", mlat));
        nameValuePairs.add(new BasicNameValuePair("lng", mlng));
        // 指向话题帖子回复的URL

        break;
    }
//    url = "http://api.bbbiu.com:1234/topic/" + UserConfigParams.device_id
//        + "/release/";
    url = "http://api.bbbiu.com:1234/devices/" + UserConfigParams.device_id
        + "/threads";
    String content = replycontent;
    NameValuePair vp1 = new BasicNameValuePair("content", content);
    nameValuePairs.add(new BasicNameValuePair("title", content));
    NameValuePair vp2 = new BasicNameValuePair("reply_to", mTargetTipInfo.id);
    if (annoyReplyFlag) {
      nameValuePairs.add(new BasicNameValuePair("anony", String.valueOf(1)));
    } else {
      nameValuePairs.add(new BasicNameValuePair("anony", String.valueOf(0)));
    }
    nameValuePairs.add(vp1);
    nameValuePairs.add(vp2);
    try {
      HttpEntity requestHttpEntity = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
      PostTopicReplyThread thread = new PostTopicReplyThread(mHandler, url, requestHttpEntity);
      thread.setReturnMsgCode(POST_NEWREPLY_OK, POST_NEWREPLY_ERROR); // 设置操作成功返回代码
      // 向服务器发送发表帖子的post信息
      thread.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void initToolbar() {
    setSupportActionBar(peepToolbar);
    setBackableToolbar(peepToolbar);
  }

  private void initParam() {
    // TODO Auto-generated method stub
    Intent intent = this.getIntent();
    mTargetTipInfo.id = intent.getStringExtra("thread_id"); // 帖子ID
    mDetailMode = intent.getIntExtra("DetailMode", TIPDETAIL_FOR_HOMETIP);
    mCanDeleteTip = intent.getBooleanExtra("CanDeleteTip", false);
    if (!mCanDeleteTip) {
      // mDeleteTipBtn.setBackgroundResource(R.drawable.more);
//      mDeleteTipBtn.setImageResource(R.drawable.ic_share_white_36dp);
      mDeleteTipBtn.setVisibility(View.GONE);
    } else {
//      mDeleteTipBtn.setImageResource(R.drawable.trash);
      mDeleteTipBtn.setVisibility(View.VISIBLE);
      // mDeleteTipBtn.setBackgroundResource(R.drawable.trash);
    }
    mlat = UserConfigParams.latitude;
    mlng = UserConfigParams.longitude;
  }

  private void findId() {
    // TODO Auto-generated method stub
    mReplyListView = (ListView) findViewById(R.id.replylistview);
    mReplyContentEdit = (EditText) findViewById(R.id.myreplyedit);
    mSendReplyBtn = (ImageButton) findViewById(R.id.sendreplybtn); // 发送按钮
    mDeleteTipBtn = (ImageButton) findViewById(R.id.shareButton); // 从我的发表进入后显示该
    if (!localornot) {
      mReplyContentEdit.setVisibility(View.GONE);
      mSendReplyBtn.setVisibility(View.GONE);
    }
  }

  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    SharedPreferences preferences = getSharedPreferences("user_Params",
        MODE_PRIVATE);
    UserConfigParams.device_id = preferences.getString("device_ID", "");
    getTipDetail(); // 获取帖子详情
    MobclickAgent.onResume(this);
  }

  private void hideKeyBoard() {
    if (mReplyContentEdit!=null) {
      ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow
          (mReplyContentEdit.getWindowToken(), 0);
    }
  }
  @Override
  protected void onPause() {
    hideKeyBoard();
    super.onPause();
  }
}
