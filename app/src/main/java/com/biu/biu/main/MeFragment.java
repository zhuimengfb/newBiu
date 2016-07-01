package com.biu.biu.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biu.biu.user.utils.UserPreferenceUtil;
import com.biu.biu.user.views.UserInfoActivity;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.utils.GlideCircleTransform;
import com.biu.biu.utils.GlobalString;
import com.biu.biu.views.ActivityUserAgreement;
import com.bumptech.glide.Glide;
import com.umeng.update.UmengUpdateAgent;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.badgeview.BGABadgeFrameLayout;
import grf.biu.R;


public class MeFragment extends Fragment {

  public static final String ME_FRAGMENT_MSG_UPDATE_ACTION = "me_fragment_msg_action";
  public static final String KEY_ME_FRAGMENT_MSG_NUMBER = "me_fragment_msg_number";
  // 显示新消息情况的红点
  private ImageView meStatusView;

  @BindView(R.id.bga_me_publish_frame)
  BGABadgeFrameLayout publishFrame;
  @BindView(R.id.bga_me_reply_frame)
  BGABadgeFrameLayout replyFrame;

  // 用户的新消息数情况
  private JSONObject msgNumDetail = null;
  private MsgNumHandler msgNumHandler = null;

  @BindView(R.id.iv_user_icon)
  ImageView userIcon;
  @BindView(R.id.tv_user_name)
  TextView userName;
  @BindView(R.id.iv_icon_outside)
  ImageView userIconOutside;
  @BindView(R.id.layout_my_publish)
  RelativeLayout myPublishLayout;
  @BindView(R.id.layout_my_reply)
  RelativeLayout myReplyLayout;
  @BindView(R.id.layout_my_suggestion)
  RelativeLayout mySuggestionLayout;
  @BindView(R.id.layout_check_version)
  RelativeLayout checkVersionLayout;

  public static MeFragment getInstance(ImageView meStatusView) {

    MeFragment meFragment = new MeFragment();
    meFragment.meStatusView = meStatusView;
    return meFragment;
  }

  public MeFragment() {
  }

  @Override
  public void onResume() {
    // TODO Auto-generated method stub
    // 初始化标识me界面上的新消息红点标识的状态
    UserConfigParams.meStatus = false;
    super.onResume();
    refreshMsgNum();
    showUserIcon();
    showNickName();
  }

  private void toUserAgreeActivity() {
    Intent toUserAgreement = new Intent(MeFragment.this
        .getActivity(), ActivityUserAgreement.class);
    startActivity(toUserAgreement);
  }

  private void toSuggestionActivity() {
    Intent intent = new Intent();
    intent.setClass(getActivity(), ResponseSuggestActivity.class);
    startActivity(intent);
  }

  private void toMyPublishActivity() {
    Intent intent = new Intent();
    intent.setClass(getActivity(), MyPublishActivity.class);
    startActivity(intent);
  }

  private void toMyReplyActivity() {
    Intent intent = new Intent();
    intent.setClass(getActivity(), MyReplyActivity.class);
    startActivity(intent);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreateView(inflater, container, savedInstanceState);
    msgNumHandler = new MsgNumHandler();
    View meView = inflater.inflate(R.layout.activity_tab_me, container,
        false);
    ButterKnife.bind(this, meView);
    showNickName();
    showUserIcon();
    initEvent();
    return meView;
  }

  private void showNickName() {
    if (!StringUtils.isEmpty(UserPreferenceUtil.getUserPreferenceNickName())) {
      userName.setText(UserPreferenceUtil.getUserPreferenceNickName());
    }
  }

  private void showUserIcon() {
    if (!StringUtils.isEmpty(UserPreferenceUtil.getUserIconAddress())) {
      Glide.with(getActivity()).load(Uri.fromFile(new File(UserPreferenceUtil.getUserIconAddress())
      )).error(R.drawable.user_icon_big).transform(new GlideCircleTransform(getActivity()))
          .into(userIcon);
      return;
    }
    Glide.with(getActivity()).load(GlobalString.BASE_URL + "/" + UserPreferenceUtil
        .getUserIconLargeNet()).placeholder(R.drawable.default_big_icon).error(R.drawable
        .default_big_icon).transform(new GlideCircleTransform(getActivity())).into(userIcon);
  }

  private void initEvent() {
    userIcon.setOnClickListener(new UserClickListener());
    userName.setOnClickListener(new UserClickListener());
    userIconOutside.setOnClickListener(new UserClickListener());
    myPublishLayout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        toMyPublishActivity();
      }
    });
    myReplyLayout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        toMyReplyActivity();
      }
    });
    mySuggestionLayout.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        toSuggestionActivity();
      }

    });
    checkVersionLayout.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        toUserAgreeActivity();
      }
    });
  }

  class UserClickListener implements OnClickListener {

    @Override
    public void onClick(View v) {
      UserInfoActivity.toThisActivity(getActivity());
    }
  }

  /**
   * 检测版本按钮单机监听器 由于提示消息是2秒，所以等待时间也设置为2100豪秒。
   */
  private class CheckUpdatebtnListener implements OnClickListener {
    private boolean flag = true;

    // 计时线程，1秒钟只能点一次
    private class TimeThread extends Thread {
      public void run() {
        try {
          Thread.sleep(2100);
          flag = true;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    private synchronized void setFlag() {
      flag = false;
    }

    @Override
    public void onClick(View v) {
      // TODO Auto-generated method stub
      if (!flag) {
        return;
      } else {
        setFlag();
        new TimeThread().start();
      }
      UmengUpdateAgent.forceUpdate(getActivity());
    }

  }

  // 获取我的发表和我的回复新消息数目
  // 参数为设备ID
  class MsgNumThread implements Runnable {
    private String device_id;
    private MsgNumHandler msgNumHandler;

    public MsgNumThread(String device_id, MsgNumHandler msgNumHandler) {
      this.device_id = device_id;
      this.msgNumHandler = msgNumHandler;
    }

    @Override
    public void run() {
      // TODO Auto-generated method stub
      String device_id = UserConfigParams.device_id;
      String url = "http://api.bbbiu.com:1234/message/" + device_id;
      HttpClient httpClient = new DefaultHttpClient();
      StringBuilder urlStringBuilder = new StringBuilder(url);
      StringBuilder entityStringBuilder = new StringBuilder();
      // 利用URL生成一个HttpGet请求
      HttpGet httpGet = new HttpGet(urlStringBuilder.toString());
      httpGet.setHeader("Content-Type",
          "application/x-www-form-urlencoded; charset=utf-8");
      BufferedReader bufferedReader = null;
      HttpResponse httpResponse = null;
      try {
        // HttpClient发出一个HttpGet请求
        httpResponse = httpClient.execute(httpGet);
      } catch (UnknownHostException e) {
        // 无法连接到主机
        Message msg = Message.obtain();
        // msg.what = NEXT_PAGE_GET_ERROR;
        // 通过Handler发布传送消息，handler
        // this.mhandler.sendMessage(msg);
        return;
      } catch (Exception e) {
        e.printStackTrace();
        return;
      }
      // 得到httpResponse的状态响应码
      int statusCode = httpResponse.getStatusLine().getStatusCode();

      if (statusCode == HttpStatus.SC_OK) {
        // 得到httpResponse的实体数据
        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity != null) {
          try {
            bufferedReader = new BufferedReader(
                new InputStreamReader(httpEntity.getContent(),
                    "UTF-8"), 8 * 1024);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
              entityStringBuilder.append(line + "/n");
            }

            msgNumDetail = new JSONObject(
                entityStringBuilder.toString());
            Message message = Message.obtain();
            message.what = MSG_NUM_OK;
            this.msgNumHandler.sendMessage(message);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } else {
        // // 获取数据错误
        Message msg = Message.obtain();
        msg.what = MSG_NUM_ERR;
        msgNumHandler.sendMessage(msg);
      }

    }
  }

  class MsgNumHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      // TODO Auto-generated method stub
      // super.handleMessage(msg);
      int msg_state = msg.what;
      switch (msg_state) {
        case MSG_NUM_OK:
          try {
            Log.d(SHOWMSG,
                "刷新新消息的数目" + "获得的发表新消息数目：：：："
                    + msgNumDetail.getInt("publish"));
            Log.d(SHOWMSG,
                "刷新新消息的数目" + "获得的回复新消息数目：：：："
                    + msgNumDetail.getInt("reply"));
            if (msgNumDetail.getInt("publish") > 0) {
              //myPublishNum.setVisibility(TextView.VISIBLE);
              if (msgNumDetail.getInt("publish") < 99) {
                            /*myPublishNum.setText(msgNumDetail
                  .getString("publish"));*/
                publishFrame.showTextBadge(msgNumDetail
                    .getString("publish"));
              } else {
							/*myPublishNum.setText("99+");*/
                publishFrame.showTextBadge("99+");
              }
              UserConfigParams.meStatus = true;

            } else {
              publishFrame.hiddenBadge();
						/*myPublishNum.setVisibility(TextView.INVISIBLE);*/
            }

            if (msgNumDetail.getInt("reply") > 0) {
						/*myReplyNum.setVisibility(TextView.VISIBLE);*/
              if (msgNumDetail.getInt("reply") < 99) {
							/*myReplyNum.setText(msgNumDetail.getString("reply"));*/
                replyFrame.showTextBadge(msgNumDetail.getString("reply"));
              } else {
							/*myReplyNum.setText("99+");*/
                replyFrame.showTextBadge("99+");
              }
              UserConfigParams.meStatus = true;
            } else {
						/*myReplyNum.setVisibility(TextView.INVISIBLE);*/
              replyFrame.hiddenBadge();
            }
            sendMsgNumBroadcast(msgNumDetail.getInt("reply") + msgNumDetail.getInt("publish"));
          } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

				/*if (UserConfigParams.meStatus) {
					meStatusView.setVisibility(View.VISIBLE);
				} else {
					if (meStatusView!=null) {
						meStatusView.setVisibility(View.INVISIBLE);
					}
				}*/
          UserConfigParams.meStatus = false;
          break;
        case MSG_NUM_ERR:
          break;
        default:
          break;
      }
    }
  }

  private void sendMsgNumBroadcast(int number) {
    Intent intent = new Intent();
    intent.setAction(ME_FRAGMENT_MSG_UPDATE_ACTION);
    intent.putExtra(KEY_ME_FRAGMENT_MSG_NUMBER, number);
    getActivity().sendBroadcast(intent);
  }

  private final static int MSG_NUM_OK = 0;
  private final static int MSG_NUM_ERR = -1;

  // 请求新消息数目的方法
  private void refreshMsgNum() {
    Log.d(SHOWMSG, "刷新新消息的数目");
    // 哪一个
    Log.d(SHOWMSG, "用户的设备ID---》" + UserConfigParams.device_id);
    String device_id = UserConfigParams.device_id;
    new Thread(new MsgNumThread(device_id, msgNumHandler)).start();
  }

  private static final String SHOWMSG = "show----->num";
}
