package com.biu.biu.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.biu.biu.app.BiuApp;
import com.biu.biu.contact.entity.AddContactBean;
import com.biu.biu.contact.entity.AddContactRequest;
import com.biu.biu.contact.entity.ContactInfo;
import com.biu.biu.contact.model.ContactModel;
import com.biu.biu.contact.utils.JMessageUtils;
import com.biu.biu.contact.views.AddContactActivity;
import com.biu.biu.main.MainActivity;
import com.biu.biu.thread.PostRegIDThread;
import com.biu.biu.user.entity.ShowUserInfoBean;
import com.biu.biu.user.entity.SimpleUserInfo;
import com.biu.biu.user.model.UserModel;
import com.biu.biu.user.utils.UserPreferenceUtil;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.utils.GlobalString;
import com.biu.biu.utils.UUIDGenerator;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 自定义接收器
 * <p/>
 * 如果不定义这个 Receiver，则： 1) 默认用户会打开主界面 2) 接收不到自定义消息
 */

public class MyReceiver extends BroadcastReceiver {
  private static final String TAG = "BiuPush";
  private static Map<Integer, NotificationType> typeMap = new Hashtable<>();
  // private static final int SEND_RIGID = 1;
  // public static int newNum = 0;
  // 开启一个线程将regId传递给应用服务器后台

  private enum NotificationType {
    TYPE_REPLY, TYPE_ADD_REQUEST, TYPE_ADD_SUCCESS
  }

  private Handler regIdHandler = new Handler() {
    public void handleMessage(android.os.Message msg) {
      switch (msg.what) {
        case PostRegIDThread.RIGID_OK:
          break;
        case PostRegIDThread.RIGID_ERR:
          break;
        default:
          break;
      }
    }

    ;
  };

  @Override
  public void onReceive(Context context, final Intent intent) {
    Bundle bundle = intent.getExtras();
    switch (intent.getAction()) {
      case JPushInterface.ACTION_REGISTRATION_ID:
        String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
        String regIdUrl = "http://api.bbbiu.com:1234/register/push";
        String device_id = UserConfigParams.device_id;
        if (device_id != null) {
          new Thread(new PostRegIDThread(regIdHandler, regIdUrl, device_id, regId)).start();
        }
        break;
      case JPushInterface.ACTION_MESSAGE_RECEIVED:
        Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: "
            + bundle.getString(JPushInterface.EXTRA_MESSAGE));
        dealMessage(bundle, bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID));
        break;
      case JPushInterface.ACTION_NOTIFICATION_RECEIVED:
        Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
        int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
        // 每次收到通知的时候将数值+1
        //UserConfigParams.badgeNum += 1;
        Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
        dealMessage(bundle, notifactionId);
        break;
      case JPushInterface.ACTION_NOTIFICATION_OPENED:
        Log.i(TAG, "[MyReceiver] 用户点击打开了通知");
        // 获取点击的通知ID并让其消失
        Bundle tempDundle = intent.getExtras();
        int notificationId = 0;
        notifactionId = tempDundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
        JPushInterface.clearNotificationById(context, notificationId);
        // 用户打开通知 先不做处理了
        // 打开自定义的Activity
        if (typeMap.get(notifactionId) == NotificationType.TYPE_ADD_REQUEST) {
          Intent intent1 = new Intent();
          intent1.setClass(context, AddContactActivity.class);
          intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
          context.startActivity(intent1);
        } else if (typeMap.get(notifactionId) == NotificationType.TYPE_ADD_SUCCESS) {
          bundle.putBoolean("newFriend", true);
          toMainActivity(context, bundle);
        } else if (typeMap.get(notifactionId) == NotificationType.TYPE_REPLY) {
          toMainActivity(context, bundle);
        }
        /*String extra = tempDundle.getString(JPushInterface.EXTRA_ALERT);
        if (StringUtils.equals(extra, "来了一个新好友请求")) {
          Intent intent1 = new Intent();
          intent1.setClass(context, AddContactActivity.class);
          intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
          context.startActivity(intent1);
        } else {
          toMainActivity(context, bundle);
        }*/
        break;
      case JPushInterface.ACTION_RICHPUSH_CALLBACK:
        Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface
            .EXTRA_EXTRA));
        break;
      case JPushInterface.ACTION_CONNECTION_CHANGE:
        boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
        Log.w(TAG, "[MyReceiver]" + intent.getAction() + " connected state change to " + connected);
        break;
      case "cn.jpush.im.android.action.IM_RESPONSE":
        Log.d("1", "1");
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            Intent intent1 = new Intent();
            intent1.setAction(GlobalString.ACTION_NEW_MESSAGE);
            BiuApp.getContext().sendBroadcast(intent1);
          }
        }, 500);

        break;
      default:
        break;
    }
  }

  private void dealMessage(Bundle bundle, int notificationId) {
    String titleString = bundle.getString(JPushInterface.EXTRA_TITLE);
    String extraString = bundle.getString(JPushInterface.EXTRA_EXTRA);
    try {
      JSONObject jsonObject = new JSONObject(extraString);
      if (!jsonObject.isNull("mode") && !jsonObject.isNull("requester")) {
        switch (jsonObject.getString("mode")) {
          case "add":
            String requesterId = jsonObject.getString("requester");
            typeMap.put(notificationId, NotificationType.TYPE_ADD_REQUEST);
            //newAddRequest(requesterId);
            sendNewRequestBroadcast();
            break;
          case "delete":
            String requesterId1 = jsonObject.getString("requester");
            newDeleteRequest(requesterId1);
            break;
          case "confirm":
            typeMap.put(notificationId, NotificationType.TYPE_ADD_SUCCESS);
            Intent intent = new Intent();
            intent.setAction(GlobalString.ACTION_FRIEND_CONFIRM);
            BiuApp.getContext().sendBroadcast(intent);
            newFriendConfirm(jsonObject.getString("requester"));
            break;
          default:
            break;
        }
      } else {
        //TODO 暂时把非请求类的推送都认为是回复，后期需要修改后台完善推送种类
        typeMap.put(notificationId, NotificationType.TYPE_REPLY);
      }
    } catch (JSONException e) {
      e.printStackTrace();
      typeMap.put(notificationId, NotificationType.TYPE_REPLY);
    }
  }

  private void toMainActivity(Context context, Bundle bundle) {
    Intent i = new Intent(context, MainActivity.class);
    i.putExtras(bundle);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(i);
  }

  private void newFriendConfirm(final String id) {
    if (StringUtils.isEmpty(id)) {
      return;
    }
    final ContactModel contactModel = new ContactModel();
    Observable.just(id).subscribeOn(Schedulers.io())
        .doOnNext(new Action1<String>() {
          @Override
          public void call(String s) {
            AddContactRequest addContactRequest = contactModel.queryAddContactRequestByRequestId(id);
            if (addContactRequest != null) {
              addContactRequest.setStatus(AddContactBean.STATUS_ADD_ALREADY);
              contactModel.saveAddContactRequest(addContactRequest);
            }
          }
        }).subscribe(new Subscriber<String>() {
      @Override
      public void onCompleted() {
        Log.d("friend confirm", "complete");
        Intent intent = new Intent();
        intent.setAction(GlobalString.ACTION_FRIEND_REQUEST_CHANGE);
        BiuApp.getContext().sendBroadcast(intent);
      }

      @Override
      public void onError(Throwable e) {
        Log.d("friend confirm", "error");
        e.printStackTrace();
      }

      @Override
      public void onNext(String s) {
        Log.d("friend confirm", "next");
      }
    });
  }

  private void sendNewRequestBroadcast() {
    Intent intent = new Intent();
    intent.setAction(GlobalString.ACTION_NEW_FRIEND_REQUEST);
    BiuApp.getContext().sendBroadcast(intent);
  }

  private void newAddRequest(final String requesterId) {
    final ContactModel contactModel = new ContactModel();
    final UserModel userModel = new UserModel();
    userModel.queryUserInfo(requesterId).subscribeOn(Schedulers.newThread())
        .map(new Func1<ShowUserInfoBean, AddContactRequest>() {
          @Override
          public AddContactRequest call(ShowUserInfoBean showUserInfoBean) {
            //数据库存储个人信息
            SimpleUserInfo simpleUserInfo = new SimpleUserInfo();
            simpleUserInfo.setShowoff(showUserInfoBean.getShowoff());
            simpleUserInfo.setDevice_id(showUserInfoBean.getJm_id());
            simpleUserInfo.setIcon_large(showUserInfoBean.getIcon_large());
            simpleUserInfo.setIcon_small(showUserInfoBean.getIcon_small());
            simpleUserInfo.setNickname(showUserInfoBean.getNickname());
            simpleUserInfo.setJm_id(showUserInfoBean.getJm_id());
            contactModel.saveSimpleUserInfo(simpleUserInfo);
            AddContactRequest addContactRequest = new AddContactRequest();
            addContactRequest.setReceiverJmId(UserPreferenceUtil.getUserPreferenceId());
            addContactRequest.setGenerateDate(new Date());
            addContactRequest.setId(UUIDGenerator.getUUID());
            //TODO 附加的话
            addContactRequest.setMessage("");
            addContactRequest.setReceiverId(UserPreferenceUtil.getUserPreferenceId());
            addContactRequest.setSenderId(requesterId);
            addContactRequest.setSenderJmId(requesterId);
            addContactRequest.setStatus(AddContactBean.STATUS_ADD_NORMAL);
            return addContactRequest;
          }
        }).doOnNext(new Action1<AddContactRequest>() {
      @Override
      public void call(AddContactRequest addContactRequest) {
        contactModel.saveAddContactRequest(addContactRequest);
      }
    }).subscribe(new Subscriber<AddContactRequest>() {
      @Override
      public void onCompleted() {
        Log.d("推送好友请求", "complete");
      }

      @Override
      public void onError(Throwable e) {
        Log.d("推送好友请求", "error");
        e.printStackTrace();
      }

      @Override
      public void onNext(AddContactRequest addContactRequest) {

        Log.d("推送好友请求", "next");
      }
    });
  }

  private void newDeleteRequest(final String requesterId) {
    final ContactModel contactModel = new ContactModel();
    Observable.just(requesterId)
        .subscribeOn(Schedulers.io())
        .doOnNext(new Action1<String>() {
          @Override
          public void call(String s) {
            ContactInfo contactInfo = contactModel.queryContactInfo(requesterId);
            if (contactInfo != null) {
              //
              contactModel.deleteContact(contactInfo);
              contactModel.deleteAddContactRequest(contactModel.queryAddContactRequest
                  (contactInfo.getUserId()));
            }
          }
        }).observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<String>() {
          @Override
          public void onCompleted() {

          }

          @Override
          public void onError(Throwable e) {

          }

          @Override
          public void onNext(String s) {
            //发送全局广播，告知好友被删除
            Intent intent = new Intent();
            intent.setAction(GlobalString.ACTION_FRIEND_DELETED);
            intent.putExtra(GlobalString.KEY_DELETED_FRIEND_ID, s);
            BiuApp.getContext().sendBroadcast(intent);
          }
        });

  }
}
