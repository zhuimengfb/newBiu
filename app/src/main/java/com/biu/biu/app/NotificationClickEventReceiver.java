package com.biu.biu.app;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.biu.biu.contact.entity.ContactInfo;
import com.biu.biu.contact.entity.ContactInfoCommons;
import com.biu.biu.contact.utils.ContactAction;
import com.biu.biu.contact.views.ChatActivity;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.NotificationClickEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import io.realm.Realm;
import io.realm.RealmResults;

public class NotificationClickEventReceiver {
  private static final String TAG = NotificationClickEventReceiver.class.getSimpleName();

  private Context mContext;

  public NotificationClickEventReceiver(Context context) {
    mContext = context;
    //注册接收消息事件
    JMessageClient.registerEventReceiver(this);
  }

  /**
   * 收到消息处理
   *
   * @param notificationClickEvent 通知点击事件
   */
  public void onEvent(NotificationClickEvent notificationClickEvent) {
    Log.d(TAG, "[onEvent] NotificationClickEvent !!!!");
    if (null == notificationClickEvent) {
      Log.w(TAG, "[onNotificationClick] message is null");
      return;
    }
    Message msg = notificationClickEvent.getMessage();
    if (msg != null) {
      String targetId = msg.getTargetID();
      String appKey = msg.getFromAppKey();
      ConversationType type = msg.getTargetType();
      Conversation conv;
      Intent notificationIntent = new Intent(mContext, ChatActivity.class);
      if (type == ConversationType.single) {
        conv = JMessageClient.getSingleConversation(targetId, appKey);
        notificationIntent.putExtra(JChatCommons.IS_GROUP, false);
        notificationIntent.putExtra(JChatCommons.TARGET_APP_KEY, appKey);
        Log.d("Notification", "msg.fromAppKey() " + appKey);
      } else {
        conv = JMessageClient.getGroupConversation(Long.parseLong(targetId));
        notificationIntent.putExtra(JChatCommons.IS_GROUP, true);
      }
      Log.d("Notification", "Conversation unread msg reset");
      notificationIntent.putExtra(JChatCommons.TARGET_ID, targetId);
      notificationIntent.putExtra("fromGroup", false);
      Realm realm = Realm.getInstance(BiuApp.getRealmConfiguration());
      RealmResults<ContactInfo> contactInfos = realm.where(ContactInfo.class).equalTo
          (ContactInfoCommons.KEY_USER_ID, msg.getFromUser().getUserName()).findAll();
      if (contactInfos != null && contactInfos.size() != 0) {
        notificationIntent.putExtra(ContactAction.KEY_CONTACT_ID, contactInfos.get(0).getUserId());
      }
      notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
          | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      mContext.startActivity(notificationIntent);
    }
  }

}
