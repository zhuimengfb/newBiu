package com.biu.biu.contact.entity;

import com.biu.biu.utils.PinyinUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by fubo on 2016/6/9 0009.
 * email:bofu1993@163.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class biu_friends {
  private String jm_id;
  private String nickname;
  private String icon_small;

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

  public ContactInfo toContactInfo() {
    ContactInfo contactInfo = new ContactInfo();
    contactInfo.setUserId(jm_id);
    contactInfo.setName(nickname);
    contactInfo.setIconNetAddress(icon_small);
    contactInfo.setEnglishName(PinyinUtil.getPinYin(nickname));
    contactInfo.setFlag(ContactInfo.FLAG_NORMAL);
    return contactInfo;
  }

}
