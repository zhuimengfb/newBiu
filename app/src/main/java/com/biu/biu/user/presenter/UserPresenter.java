package com.biu.biu.user.presenter;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.biu.biu.app.BiuApp;
import com.biu.biu.user.entity.UserPicInfo;
import com.biu.biu.user.entity.UserPicInfoCommons;
import com.biu.biu.user.model.UserModel;
import com.biu.biu.user.utils.CommonString;
import com.biu.biu.user.utils.UserPreferenceUtil;
import com.biu.biu.user.views.ISendRequestView;
import com.biu.biu.user.views.IUserInfo;
import com.biu.biu.utils.FileUtils;
import com.biu.biu.utils.ImageUtil;
import com.biu.biu.utils.NetUtils;
import com.biu.biu.utils.UUIDGenerator;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by fubo on 2016/5/17 0017.
 * email:bofu1993@163.com
 */
public class UserPresenter {

  private IUserInfo userInfo;
  private UserModel userModel;
  private ISendRequestView sendRequestView;
  private static long lastUploadTime =0;


  public UserPresenter(ISendRequestView sendRequestView) {
    this.sendRequestView = sendRequestView;
    userModel = new UserModel();
  }

  public void sendFriendRequest(String jmId, String message) {
    userModel.requestFriend(jmId, message, new UserModel.OnPostFriendRequest() {
      @Override
      public void success() {
        sendRequestView.sendSuccess();
      }

      @Override
      public void failure() {
        Log.d("failure", "failure");
      }
    });
  }

  public UserPresenter(IUserInfo userInfo) {
    this.userInfo = userInfo;
    userModel = new UserModel();
  }

  public void modifyNickName(String deviceId, String nickName) {
    if (StringUtils.isEmpty(nickName)) {
      userInfo.showInputNullName();
    } else {
      if (!NetUtils.isNetConnected()) {
        userInfo.showNetFailure();
      } else {
        //保存到本地
        UserPreferenceUtil.getPreferences().edit().putString(UserPreferenceUtil
            .USER_PREFERENCE_NAME_KEY, nickName).apply();
        //更新JMessage
        updateJMessageNickName(nickName);
        //服务器更新
        userModel.renameNickName(deviceId, nickName);
        userInfo.setNickName(nickName);
      }
    }
  }

  public void modifyUserIcon(Uri uri) {
    Observable.just(uri.getPath())
        .map(new Func1<String, String>() {
          @Override
          public String call(String s) {
            //图片压缩处理
            String oldPath = UserPreferenceUtil.getUserIconAddress();
            String localAddress = CommonString.USER_ICON_PATH + UserPreferenceUtil
                .getUserPreferenceId() + "-" + UUIDGenerator.getUUID() + ".png";
            FileUtils.copy(s, localAddress);
            UserPreferenceUtil.setUserIconLocalAddress(localAddress);
            ImageUtil.saveBitmap(ImageUtil.getCompressedImage(localAddress),localAddress);
            FileUtils.deleteFile(oldPath);
            return localAddress;
          }
        }).subscribeOn(Schedulers.io())
        .doOnNext(new Action1<String>() {
          @Override
          public void call(String s) {
            //上传到服务器
            userModel.modifyHeadIcon(UserPreferenceUtil.getUserPreferenceId(), s);
          }
        })
        .doOnNext(new Action1<String>() {
          @Override
          public void call(String s) {
            //更新JMessage
            JMessageClient.updateUserAvatar(new File(s), new BasicCallback() {
              @Override
              public void gotResult(int i, String s) {
                Log.d("update icon", s);
              }
            });
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.newThread()).subscribe(new Subscriber<String>() {
      @Override
      public void onCompleted() {
        userInfo.setUserIcon(Uri.fromFile(new File(UserPreferenceUtil.getUserIconAddress())));
      }

      @Override
      public void onError(Throwable e) {
        e.printStackTrace();
      }

      @Override
      public void onNext(String s) {

      }
    });

  }

  private void updateJMessageNickName(String nickName) {
    UserInfo myInfo = JMessageClient.getMyInfo();
    if (myInfo != null) {
      myInfo.setNickname(nickName);
      JMessageClient.updateMyInfo(UserInfo.Field.nickname, myInfo, new BasicCallback() {
        @Override
        public void gotResult(int i, String s) {
          if (i == 0) {
            UserPreferenceUtil.setUpdateNickNameSuccess();
          } else {
            UserPreferenceUtil.setUpdateNickNameFail();
          }
        }
      });
    }
  }

  public void uploadUserPic(final Uri data) {
    if (System.currentTimeMillis() - lastUploadTime < 1000 * 5) {
      Toast.makeText(BiuApp.getContext(),"服务器君有点忙，请稍后上传吧",Toast.LENGTH_SHORT).show();
      return ;
    }
    lastUploadTime =System.currentTimeMillis();
    userInfo.showUploadPic();
    Observable.just(data.getPath()).map(new Func1<String, UserPicInfo>() {
      @Override
      public UserPicInfo call(String s) {
        String picId = UUIDGenerator.getUUID();
        String localAddress = CommonString.USER_PIC_SHOW_PATH + UserPreferenceUtil
            .getUserPreferenceId() + "-" + picId + ".png";
        //保存本地
        FileUtils.copy(s, localAddress);
        ImageUtil.saveBitmap(ImageUtil.getCompressedImage(localAddress),localAddress);
        UserPicInfo userPicInfo = new UserPicInfo();
        userPicInfo.setUserId(UserPreferenceUtil.getUserPreferenceId());
        userPicInfo.setLocalPath(localAddress);
        userPicInfo.setPicId(picId);
        userPicInfo.setFlag(UserPicInfoCommons.FLAG_NORMAL);
        userModel.saveUserPicToDB(userPicInfo);
        //上传服务器
        userModel.uploadPhoto(UserPreferenceUtil.getUserPreferenceId(), localAddress);
        return userPicInfo;
      }
    }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<UserPicInfo>() {
          @Override
          public void onCompleted() {
            /*userInfo.addUserPic(data);
            userInfo.hideUploadPic();
            userInfo.showSuccessToast();*/
          }

          @Override
          public void onError(Throwable e) {

          }

          @Override
          public void onNext(UserPicInfo userPicInfo) {
            userInfo.addUserPic(userPicInfo);
            userInfo.hideUploadPic();
            userInfo.showSuccessToast();
          }
        });

  }

  public void deleteUserPic(final int position, UserPicInfo userPicInfo) {
    Observable.just(userPicInfo).doOnNext(new Action1<UserPicInfo>() {
      @Override
      public void call(UserPicInfo userPicInfo) {
        //TODO 等待完善删除接口
        int number = position + 1;
        userModel.deleteUserPicNet(userPicInfo, number);
      }
    }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<UserPicInfo>() {
          @Override
          public void onCompleted() {
            userInfo.showDeleteSuccess(position);
          }

          @Override
          public void onError(Throwable e) {
            userInfo.showDeleteFail();
          }

          @Override
          public void onNext(UserPicInfo userPicInfo) {

          }
        });

  }

  public void queryUserPics() {
    Observable.create(new Observable.OnSubscribe<List<UserPicInfo>>() {
      @Override
      public void call(Subscriber<? super List<UserPicInfo>> subscriber) {
        subscriber.onNext(userModel.queryUserPicFromDB(UserPreferenceUtil.getUserPreferenceId()));
        subscriber.onCompleted();
      }
    }).subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<List<UserPicInfo>>() {
          @Override
          public void onCompleted() {

          }

          @Override
          public void onError(Throwable e) {

          }

          @Override
          public void onNext(List<UserPicInfo> userPicInfos) {
            userInfo.showAllPic(userPicInfos);
          }
        });
  }

}
