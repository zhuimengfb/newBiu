package com.biu.biu.contact.entity;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by fubo on 2016/5/27 0027.
 * email:bofu1993@163.com
 */
public class AddContactRequest extends RealmObject {

  @Ignore
  public static final String KEY_STATUS = "status";
  @Ignore
  public static final String KEY_JM_ID = "senderJmId";
  @Ignore
  public static final String GENERATE_DATE = "generateDate";
  @Ignore
  public static final String ID = "id";
  @Ignore
  public static String Key_REQUESTER_ID="senderId";

  @PrimaryKey
  private String id;
  private String senderId;
  private String senderJmId;
  private String receiverId;
  private String receiverJmId;
  private String message;
  private int status;
  private Date generateDate;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSenderId() {
    return senderId;
  }

  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }

  public String getSenderJmId() {
    return senderJmId;
  }

  public void setSenderJmId(String senderJmId) {
    this.senderJmId = senderJmId;
  }

  public String getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(String receiverId) {
    this.receiverId = receiverId;
  }

  public String getReceiverJmId() {
    return receiverJmId;
  }

  public void setReceiverJmId(String receiverJmId) {
    this.receiverJmId = receiverJmId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public Date getGenerateDate() {
    return generateDate;
  }

  public void setGenerateDate(Date generateDate) {
    this.generateDate = generateDate;
  }

  public static AddContactRequest getFromRealm(AddContactRequest realmRequest) {
    AddContactRequest addContactRequest = new AddContactRequest();
    addContactRequest.setStatus(realmRequest.getStatus());
    addContactRequest.setSenderId(realmRequest.getSenderId());
    addContactRequest.setSenderJmId(realmRequest.getSenderJmId());
    addContactRequest.setReceiverId(realmRequest.getReceiverId());
    addContactRequest.setReceiverJmId(realmRequest.getReceiverJmId());
    addContactRequest.setId(realmRequest.getId());
    addContactRequest.setMessage(realmRequest.getMessage());
    return addContactRequest;
  }


}
