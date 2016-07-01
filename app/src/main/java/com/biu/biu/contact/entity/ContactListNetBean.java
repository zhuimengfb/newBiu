package com.biu.biu.contact.entity;

import java.util.List;

/**
 * Created by fubo on 2016/6/9 0009.
 * email:bofu1993@163.com
 */
public class ContactListNetBean {
  private List<biu_friends> biu_friends;
  private int requests;

  public List<com.biu.biu.contact.entity.biu_friends> getBiu_friends() {
    return biu_friends;
  }

  public void setBiu_friends(List<com.biu.biu.contact.entity.biu_friends> biu_friends) {
    this.biu_friends = biu_friends;
  }

  public int getRequests() {
    return requests;
  }

  public void setRequests(int requests) {
    this.requests = requests;
  }
}
