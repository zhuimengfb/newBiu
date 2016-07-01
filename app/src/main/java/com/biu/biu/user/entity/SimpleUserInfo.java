package com.biu.biu.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by fubo on 2016/6/4 0004.
 * email:bofu1993@163.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleUserInfo extends RealmObject {

  @Ignore
  public static final String KEY_DEVICE_ID = "device_id";
  @Ignore
  public static final String KEY_JM_ID = "jm_id";

  private String device_id;
  @PrimaryKey
  private String jm_id;
  private String nickname;
  private String icon_large;
  private String icon_small;
  private String showoff;

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

  public String getShowoff() {
    return showoff;
  }

  public void setShowoff(String showoff) {
    this.showoff = showoff;
  }


  public static SimpleUserInfo getSimpleUserInfoFromRealm(SimpleUserInfo realmUser) {
    SimpleUserInfo simpleUserInfo = new SimpleUserInfo();
    simpleUserInfo.setDevice_id(realmUser.getDevice_id());
    simpleUserInfo.setIcon_small(realmUser.getIcon_small());
    simpleUserInfo.setIcon_large(realmUser.getIcon_large());
    simpleUserInfo.setNickname(realmUser.getNickname());
    simpleUserInfo.setJm_id(realmUser.getJm_id());
    simpleUserInfo.setShowoff(realmUser.getShowoff());
    return simpleUserInfo;
  }
}
