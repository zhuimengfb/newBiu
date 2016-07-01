package com.biu.biu.user.entity;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by fubo on 2016/6/2 0002.
 * email:bofu1993@163.com
 */
public class UserPicInfo extends RealmObject {

  @PrimaryKey
  private String picId;
  private String userId;
  private String netAddress;
  private String localPath;
  private Date generateDate;
  private int flag = 1;

  public String getPicId() {
    return picId;
  }

  public void setPicId(String picId) {
    this.picId = picId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getNetAddress() {
    return netAddress;
  }

  public void setNetAddress(String netAddress) {
    this.netAddress = netAddress;
  }

  public String getLocalPath() {
    return localPath;
  }

  public void setLocalPath(String localPath) {
    this.localPath = localPath;
  }

  public int getFlag() {
    return flag;
  }

  public void setFlag(int flag) {
    this.flag = flag;
  }

  public Date getGenerateDate() {
    return generateDate;
  }

  public void setGenerateDate(Date generateDate) {
    this.generateDate = generateDate;
  }
}
