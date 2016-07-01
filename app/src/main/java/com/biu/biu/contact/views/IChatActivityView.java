package com.biu.biu.contact.views;

import java.util.List;

import cn.jpush.im.android.api.model.Message;

/**
 * Created by fubo on 2016/5/29 0029.
 * email:bofu1993@163.com
 */
public interface IChatActivityView {

  void updateChatMessages(List<Message> chatMessageBeen);

  void updateChatMessage(Message message);
}
