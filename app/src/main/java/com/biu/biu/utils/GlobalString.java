package com.biu.biu.utils;

import com.biu.biu.app.BiuApp;

/**
 * Created by fubo on 2016/6/2 0002.
 * email:bofu1993@163.com
 */
public class GlobalString {

  public static final String PACKAGE_NAME = BiuApp.getContext().getPackageName();
  public static final String BASE_URL = "http://api.bbbiu.com:1234";
  public static final String URI_RES_PREFIX = "android.resource://" + PACKAGE_NAME + "/";

  public static final String ACTION_FRIEND_DELETED = "action_friend_deleted";
  public static final String KEY_DELETED_FRIEND_ID = "key_delete_friend_id";
  public static final String ACTION_NEW_FRIEND_REQUEST = "action_new_friend_request";
  public static final String ACTION_FRIEND_CONFIRM = "action_friend_confirm";
  public static final String ACTION_FRIEND_REQUEST_CHANGE = "action_friend_request_change";
  public static final String ACTION_CLEAR_BADGE = "clear_badge";
  public static final String ACTION_NEW_MESSAGE = "new_message";
}
