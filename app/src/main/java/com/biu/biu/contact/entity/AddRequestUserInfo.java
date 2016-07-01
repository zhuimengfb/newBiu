package com.biu.biu.contact.entity;

import com.biu.biu.user.entity.SimpleUserInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by fubo on 2016/6/19 0019.
 * email:bofu1993@163.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddRequestUserInfo {

  public static final String KEY_DEVICE_ID = "device_id";
  public static final String KEY_JM_ID = "jm_id";
  private String device_id;
  private String jm_id;
  private String nickname;
  private String icon_large;
  private String icon_small;
  private String description;
  private int state;

  public String getDevice_id() {
    return device_id;
  }

  public void setDevice_id(String device_id) {
    this.device_id = device_id;
  }

  public String getJm_id() {
    return jm_id;
  }

  public void setJm_id(String jm_id) {
    this.jm_id = jm_id;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getIcon_large() {
    return icon_large;
  }

  public void setIcon_large(String icon_large) {
    this.icon_large = icon_large;
  }

  public String getIcon_small() {
    return icon_small;
  }

  public void setIcon_small(String icon_small) {
    this.icon_small = icon_small;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }


  public SimpleUserInfo getSimpleUserInfo(){
    SimpleUserInfo simpleUserInfo = new SimpleUserInfo();
    simpleUserInfo.setJm_id(this.jm_id);
    simpleUserInfo.setNickname(this.nickname);
    simpleUserInfo.setIcon_small(this.icon_small);
    simpleUserInfo.setIcon_large(this.icon_large);
    simpleUserInfo.setDevice_id(this.jm_id);
    return simpleUserInfo;
  }
}
