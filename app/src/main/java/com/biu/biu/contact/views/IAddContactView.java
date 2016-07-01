package com.biu.biu.contact.views;

import com.biu.biu.contact.entity.AddContactBean;

import java.util.List;

/**
 * Created by fubo on 2016/6/8 0008.
 * email:bofu1993@163.com
 */
public interface IAddContactView {

  void updateList(List<AddContactBean> addContactBeanList);

  void updateList(AddContactBean addContactBean);

  void acceptRequestSuccess(AddContactBean addContactBean);

  void removeAddRequest(int position);
}
