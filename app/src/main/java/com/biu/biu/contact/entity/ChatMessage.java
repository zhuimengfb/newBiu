package com.biu.biu.contact.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by fubo on 2016/5/30 0030.
 * email:bofu1993@163.com
 */
public class ChatMessage implements Serializable {

  private String messageId;
  private String senderId;
  private String receiverId;
  private String content;
  private int type;
  private Date generateTime;
  private int readFlag;
  private int flag;

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getSenderId() {
    return senderId;
  }

  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }

  public String getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(String receiverId) {
    this.receiverId = receiverId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public Date getGenerateTime() {
    return generateTime;
  }

  public void setGenerateTime(Date generateTime) {
    this.generateTime = generateTime;
  }

  public int getReadFlag() {
    return readFlag;
  }

  public void setReadFlag(int readFlag) {
    this.readFlag = readFlag;
  }

  public int getFlag() {
    return flag;
  }

  public void setFlag(int flag) {
    this.flag = flag;
  }

}
