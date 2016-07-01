package com.biu.biu.contact.views;

import com.biu.biu.contact.entity.ContactInfo;

import java.util.List;

/**
 * Created by fubo on 2016/5/18 0018.
 * email:bofu1993@163.com
 */
public interface IContactListView {

  void updateContactList(List<ContactInfo> contactInfoList);

  void hasNewRequest(int number);

  void deleteContactSuccess(int position);

  void showNoFriend();
}
