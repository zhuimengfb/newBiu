package com.biu.biu.user.views;

import android.net.Uri;

import com.biu.biu.user.entity.UserPicInfo;

import java.util.List;

/**
 * Created by fubo on 2016/5/17 0017.
 * email:bofu1993@163.com
 */
public interface IUserInfo {

  void showInputNullName();

  void showNetFailure();

  void setNickName(String nickName);

  void setUserIcon(Uri uri);

  void addUserPic(UserPicInfo userPicInfo);

  void showUploadIcon();

  void hideUploadIcon();

  void showUploadPic();

  void hideUploadPic();

  void showSuccessToast();

  void showFailToast();

  void showAllPic(List<UserPicInfo> userPicInfos);

  void showDeleteSuccess(int position);

  void showDeleteFail();
}
