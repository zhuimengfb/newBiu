package com.biu.biu.contact.entity;

import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.utils.UUIDGenerator;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by fubo on 2016/6/8 0008.
 * email:bofu1993@163.com
 */
public class ChatMessageBean implements Serializable {

  public static final int TYPE_TEXT = 1;
  private ChatMessage chatMessage;
  private ContactInfo contactInfo;

  public ChatMessage getChatMessage() {
    return chatMessage;
  }

  public void setChatMessage(ChatMessage chatMessage) {
    this.chatMessage = chatMessage;
  }

  public static int getTypeText() {
    return TYPE_TEXT;
  }

  public ContactInfo getContactInfo() {
    return contactInfo;
  }

  public void setContactInfo(ContactInfo contactInfo) {
    this.contactInfo = contactInfo;
  }

  public static ChatMessage generateTextMessage(String receiverId, String content) {
    ChatMessage chatMessage = new ChatMessage();
    chatMessage.setReceiverId(receiverId);
    chatMessage.setContent(content);
    chatMessage.setType(TYPE_TEXT);
    chatMessage.setMessageId(UUIDGenerator.getUUID());
    chatMessage.setSenderId(UserConfigParams.device_id);
    chatMessage.setGenerateTime(new Date());
    return chatMessage;
  }
}
