package com.biu.biu.contact.model;

import com.biu.biu.contact.entity.AddContactBean;
import com.biu.biu.contact.entity.AddContactRequest;
import com.biu.biu.contact.entity.ContactInfo;

import java.util.List;

import rx.Subscriber;

/**
 * Created by fubo on 2016/6/8 0008.
 * email:bofu1993@163.com
 */
public interface IContactModel {


  ContactInfo queryContactInfo(String contactId);

  void saveContactInfo(ContactInfo contactInfo);

  void queryContactListFromNet(String userId);

  List<ContactInfo> queryAllContactFromDB();

  void saveContactInfos(List<ContactInfo> contactInfoList);

  List<AddContactBean> queryAllAddContactBean();

  void acceptFriendRequest(AddContactBean addContactBean);

  void updateFriendRequest(AddContactBean addContactBean);

  int getUnDealRequestCount();

  void queryAllAddContactBeanFromNet(Subscriber<List<AddContactBean>> subscriber);

  void deleteAddContactRequest(AddContactRequest addContactRequest);

  void deleteContact(ContactInfo contactInfo);

  void deleteAllContact();

  void deleteContactInfoInDB(ContactInfo contactInfo);
}
