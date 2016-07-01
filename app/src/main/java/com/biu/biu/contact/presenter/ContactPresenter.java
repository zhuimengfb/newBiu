package com.biu.biu.contact.presenter;

import android.util.Log;

import com.biu.biu.contact.entity.ContactInfo;
import com.biu.biu.contact.model.ContactModel;
import com.biu.biu.contact.model.IContactModel;
import com.biu.biu.contact.views.IContactListView;
import com.biu.biu.utils.NetUtils;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by fubo on 2016/5/18 0018.
 * email:bofu1993@163.com
 */
public class ContactPresenter implements ContactModel.GetContactListener {

  private IContactListView contactListView;
  private IContactModel contactModel;

  public ContactPresenter(IContactListView contactListView) {
    this.contactListView = contactListView;
    contactModel = new ContactModel(this);
  }

  public void saveContact(ContactInfo contactInfo) {
    contactModel.saveContactInfo(contactInfo);
  }


  public void queryContact(String userId) {
    if (NetUtils.isNetConnected()) {
      contactModel.queryContactListFromNet(userId);
    } else {
      contactListView.updateContactList(contactModel.queryAllContactFromDB());
    }
  }

  public void unbind() {
    contactListView = null;
    contactModel = null;
  }

  public void removeContact(ContactInfo contactInfo, final int position) {
    Observable.just(contactInfo)
        .doOnNext(new Action1<ContactInfo>() {
          @Override
          public void call(ContactInfo contactInfo) {
            contactModel.deleteContact(contactInfo);
          }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<ContactInfo>() {
          @Override
          public void onCompleted() {
            contactListView.deleteContactSuccess(position);
          }

          @Override
          public void onError(Throwable e) {

          }

          @Override
          public void onNext(ContactInfo contactInfo) {

          }
        });
  }

  @Override
  public void onGetContactList(final List<ContactInfo> contactInfoList) {
    contactListView.updateContactList(contactInfoList);
    //后台更新数据库数据
    Observable.just(contactInfoList)
        .subscribeOn(Schedulers.io())
        .doOnNext(new Action1<List<ContactInfo>>() {
          @Override
          public void call(List<ContactInfo> contactInfos) {
            contactModel.deleteAllContact();
            contactModel.saveContactInfos(contactInfos);
          }
        }).subscribe(new Subscriber<List<ContactInfo>>() {
      @Override
      public void onCompleted() {
        Log.d("complete", "complete");
      }

      @Override
      public void onError(Throwable e) {
        e.printStackTrace();
      }

      @Override
      public void onNext(List<ContactInfo> contactInfos) {

      }
    });
  }

  @Override
  public void onGetRequestNumber(int number) {
    //TODO 暂时不需要从服务端获取数据的红点提示
    //contactListView.hasNewRequest(number);
  }

  public void queryUnDealRequestCount() {
    Observable.just(contactModel.getUnDealRequestCount())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Integer>() {
          @Override
          public void onCompleted() {

          }

          @Override
          public void onError(Throwable e) {

          }

          @Override
          public void onNext(Integer integer) {
            contactListView.hasNewRequest(integer);
          }
        });
  }

}
