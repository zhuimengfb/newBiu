package com.biu.biu.user.presenter;

import com.biu.biu.contact.entity.ContactInfo;
import com.biu.biu.contact.model.ContactModel;
import com.biu.biu.contact.model.IContactModel;
import com.biu.biu.user.entity.ShowUserInfoBean;
import com.biu.biu.user.model.UserModel;
import com.biu.biu.user.views.IShowUserInfo;

import org.apache.commons.lang3.StringUtils;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by fubo on 2016/6/9 0009.
 * email:bofu1993@163.com
 */
public class UserShowPresenter {
  private IShowUserInfo showUserInfo;
  private UserModel userModel;
  private IContactModel contactModel;

  public UserShowPresenter(IShowUserInfo showUserInfo) {
    this.showUserInfo = showUserInfo;
    userModel = new UserModel();
    contactModel = new ContactModel();
  }

  public void queryAlreadyFriend(String jmId) {
    Observable.just(contactModel.queryContactInfo(jmId))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<ContactInfo>() {
          @Override
          public void onCompleted() {

          }

          @Override
          public void onError(Throwable e) {

          }

          @Override
          public void onNext(ContactInfo contactInfo) {
            if (contactInfo == null) {
              showUserInfo.isNotFriendYet();
            } else {
              showUserInfo.isAlreadyFriend();
            }
          }
        });
  }

  public void queryUserInfo(String jmId) {
    userModel.queryUserInfo(jmId)
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<ShowUserInfoBean>() {
          @Override
          public void onCompleted() {

          }

          @Override
          public void onError(Throwable e) {

          }

          @Override
          public void onNext(ShowUserInfoBean showUserInfoBean) {
            showUserInfo.showUserInfo(showUserInfoBean);
            showUserInfo.showToolbarName(showUserInfoBean.getNickname());
            if (StringUtils.equals(showUserInfoBean.getStatus(), "0")) {
              showUserInfo.isAlreadyFriend();
            }
            String[] picAddress = showUserInfoBean.getShowoff().split(";");
            if (picAddress.length > 0) {
              showUserInfo.updateUserPic(picAddress);
            }
          }
        });
  }
}
