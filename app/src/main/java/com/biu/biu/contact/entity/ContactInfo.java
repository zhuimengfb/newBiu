package com.biu.biu.contact.entity;


import java.util.Comparator;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by fubo on 2016/5/25 0025.
 * email:bofu1993@163.com
 */
public class ContactInfo extends RealmObject {

  @Ignore
  public static final String KEY_FLAG = "flag";
  @Ignore
  public static final int FLAG_NORMAL = 0;
  @Ignore
  public static final int FLAG_DELETED = 1;
  @Ignore
  public static final String KEY_USER_ID = "userId";


  @PrimaryKey
  private String userId;
  private String name;
  private String englishName;
  private String iconNetAddress;
  private String iconFileAddress;
  private Date startDate;
  private int flag;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEnglishName() {
    return englishName;
  }

  public void setEnglishName(String englishName) {
    this.englishName = englishName;
  }

  public String getIconNetAddress() {
    return iconNetAddress;
  }

  public void setIconNetAddress(String iconNetAddress) {
    this.iconNetAddress = iconNetAddress;
  }

  public String getIconFileAddress() {
    return iconFileAddress;
  }

  public void setIconFileAddress(String iconFileAddress) {
    this.iconFileAddress = iconFileAddress;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public int getFlag() {
    return flag;
  }

  public void setFlag(int flag) {
    this.flag = flag;
  }


  public static class ContactComparator implements Comparator<ContactInfo> {

    @Override
    public int compare(ContactInfo lhs, ContactInfo rhs) {
      if (lhs.englishName != null && rhs.englishName != null) {
        return lhs.englishName.compareToIgnoreCase(rhs.englishName);
      } else if (lhs.englishName == null && rhs.englishName != null) {
        return - 1;
      } else if (lhs.englishName == null && rhs.englishName == null) {
        return 0;
      } else {
        return 1;
      }
    }

    @Override
    public boolean equals(Object object) {
      return false;
    }
  }

  public static ContactInfo toNormalContactInfo(ContactInfo contactInfoCopy){
    ContactInfo contactInfo = new ContactInfo();
    contactInfo.setEnglishName(contactInfoCopy.getEnglishName());
    contactInfo.setFlag(contactInfoCopy.getFlag());
    contactInfo.setIconFileAddress(contactInfoCopy.getIconFileAddress());
    contactInfo.setIconNetAddress(contactInfoCopy.getIconNetAddress());
    contactInfo.setName(contactInfoCopy.getName());
    contactInfo.setStartDate(contactInfoCopy.getStartDate());
    contactInfo.setUserId(contactInfoCopy.getUserId());
    return contactInfo;
  }
}
