package com.biu.biu.user.entity;

import java.io.Serializable;

/**
 * Created by fubo on 2016/6/9 0009.
 * email:bofu1993@163.com
 */
public class ShowUserInfoBean implements Serializable {

  private String jm_id;
  private String nickname;
  private String icon_small;
  private String icon_large;
  private String showoff;
  private String status;

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

  public String getIcon_small() {
    return icon_small;
  }

  public void setIcon_small(String icon_small) {
    this.icon_small = icon_small;
  }

  public String getIcon_large() {
    return icon_large;
  }

  public void setIcon_large(String icon_large) {
    this.icon_large = icon_large;
  }

  public String getShowoff() {
    return showoff;
  }

  public void setShowoff(String showoff) {
    this.showoff = showoff;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
