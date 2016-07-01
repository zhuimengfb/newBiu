package com.biu.biu.contact.presenter;

import android.util.Log;

import com.biu.biu.contact.entity.AddContactBean;
import com.biu.biu.contact.model.ContactModel;
import com.biu.biu.contact.model.IContactModel;
import com.biu.biu.contact.views.IAddContactView;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by fubo on 2016/6/12 0012.
 * email:bofu1993@163.com
 */
public class AddContactPresenter {

  private IAddContactView addContactView;
  private IContactModel contactModel;

  public void bind(IAddContactView addContactView) {
    this.addContactView = addContactView;
    contactModel = new ContactModel();
  }

  public void getAddContactList() {
    contactModel.queryAllAddContactBeanFromNet(new Subscriber<List<AddContactBean>>() {
      @Override
      public void onCompleted() {
        Log.d("addrequestlist", "complete");
      }

      @Override
      public void onError(Throwable e) {
        e.printStackTrace();
        addContactView.updateList(contactModel.queryAllAddContactBean());
      }

      @Override
      public void onNext(List<AddContactBean> addContactBeans) {
        Log.d("addcontactsize", addContactBeans.size() + "");
        addContactView.updateList(addContactBeans);
      }
    });
  }

  public void acceptAddContactRequest(AddContactBean addContactBean) {
    Observable.just(addContactBean)
        .doOnNext(new Action1<AddContactBean>() {
          @Override
          public void call(AddContactBean addContactBean) {
            //回复服务器
            contactModel.acceptFriendRequest(addContactBean);
            addContactBean.getAddContactRequest().setStatus(AddContactBean.STATUS_ADD_ALREADY);
            contactModel.updateFriendRequest(addContactBean);
          }
        })
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<AddContactBean>() {
          AddContactBean addContactBean;

          @Override
          public void onCompleted() {
            Log.d("complete", "complete");
            addContactView.acceptRequestSuccess(addContactBean);
          }

          @Override
          public void onError(Throwable e) {
            e.printStackTrace();
          }

          @Override
          public void onNext(AddContactBean addContactBean) {
            this.addContactBean = addContactBean;
          }
        });
  }

  public void removeAddRequest(AddContactBean addContactBean, final int position) {
    Observable.just(addContactBean)
        .doOnNext(new Action1<AddContactBean>() {
          @Override
          public void call(AddContactBean addContactBean) {
            contactModel.deleteAddContactRequest(addContactBean.getAddContactRequest());
          }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<AddContactBean>() {
          @Override
          public void onCompleted() {
            addContactView.removeAddRequest(position);
          }

          @Override
          public void onError(Throwable e) {

          }

          @Override
          public void onNext(AddContactBean addContactBean) {

          }
        });
  }
}
