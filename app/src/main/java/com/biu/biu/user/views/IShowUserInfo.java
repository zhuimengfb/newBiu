package com.biu.biu.user.views;

import com.biu.biu.user.entity.ShowUserInfoBean;

/**
 * Created by fubo on 2016/5/27 0027.
 * email:bofu1993@163.com
 */
public interface IShowUserInfo {

  void showToolbarName(String name);

  void showUserInfo(ShowUserInfoBean userInfo);

  void isAlreadyFriend();

  void isNotFriendYet();

  void updateUserPic(String[] picAddresses);
}
