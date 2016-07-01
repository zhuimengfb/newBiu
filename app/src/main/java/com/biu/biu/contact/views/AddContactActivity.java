package com.biu.biu.contact.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biu.biu.contact.entity.AddContactBean;
import com.biu.biu.contact.entity.AddContactRequest;
import com.biu.biu.contact.presenter.AddContactPresenter;
import com.biu.biu.contact.views.adapter.AddContactListAdapter;
import com.biu.biu.user.entity.SimpleUserInfo;
import com.biu.biu.utils.GlobalString;
import com.biu.biu.views.base.BaseActivity;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

/**
 * Created by fubo on 2016/5/25 0025.
 * email:bofu1993@163.com
 */
public class AddContactActivity extends BaseActivity implements IAddContactView {


  @BindView(R.id.rv_add_contact_list)
  RecyclerView addContactRecyclerView;
  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.toolbar_title)
  TextView toolbarTitle;
  @BindView(R.id.rl_nothing_layout)
  RelativeLayout nothingLayout;

  private AddContactListAdapter addContactListAdapter;
  private List<AddContactBean> addContactBeanList;

  private AddContactPresenter addContactPresenter = new AddContactPresenter();
  private MyReceiver myReceiver = new MyReceiver();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_contact);
    ButterKnife.bind(this);
    addContactPresenter.bind(this);
    initView();
    initData();
    initEvent();
  }

  private void initData() {
    addContactBeanList = new ArrayList<>();
    addContactListAdapter = new AddContactListAdapter(addContactBeanList);
    addContactListAdapter.setContext(this);
    addContactRecyclerView.setAdapter(addContactListAdapter);
    addContactPresenter.getAddContactList();
  }

  private void initView() {
    initToolbar();
    addContactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    addContactRecyclerView.setItemAnimator(new DefaultItemAnimator());
  }

  private void initEvent() {
    addContactListAdapter.setAcceptRequestListener(new AddContactListAdapter
        .AcceptRequestListener() {
      @Override
      public void onAcceptRequest(AddContactBean addContactBean) {
        addContactPresenter.acceptAddContactRequest(addContactBean);
      }
    });
    addContactListAdapter.setRemoveRequestListener(new AddContactListAdapter
        .RemoveRequestListener() {
      @Override
      public void removeRequest(final AddContactBean addContactBean, final int position) {
        AlertDialog alertDialog = new AlertDialog.Builder(AddContactActivity.this).setTitle
            (getString(R.string.delete_request)).setPositiveButton(getString(R.string.confirm),
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                addContactPresenter.removeAddRequest(addContactBean, position);
              }
            }).setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        }).create();
        alertDialog.show();
      }
    });
  }

  @Override
  protected void onResume() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(GlobalString.ACTION_FRIEND_REQUEST_CHANGE);
    intentFilter.addAction(GlobalString.ACTION_FRIEND_DELETED);
    registerReceiver(myReceiver, intentFilter);
    super.onResume();
  }

  @Override
  protected void onPause() {
    unregisterReceiver(myReceiver);
    super.onPause();
  }

  private void initToolbar() {
    setSupportActionBar(toolbar);
    setBackableToolbar(toolbar);
    toolbarTitle.setText(getString(R.string.add_new_friend));
  }

  private void showNoRequest() {
    nothingLayout.setVisibility(View.VISIBLE);
  }

  private void hideNoRequest() {
    nothingLayout.setVisibility(View.GONE);
  }

  private void updateNoRequestView() {
    if (addContactBeanList.size() > 0) {
      hideNoRequest();
    } else {
      showNoRequest();
    }
  }

  @Override
  public void updateList(List<AddContactBean> addContactBeanList) {
    this.addContactBeanList.clear();
    for (AddContactBean addContactBean : addContactBeanList) {
      addContactBean.setSenderInfo(SimpleUserInfo.getSimpleUserInfoFromRealm(addContactBean
          .getSenderInfo()));
      addContactBean.setAddContactRequest(AddContactRequest.getFromRealm(addContactBean
          .getAddContactRequest()));
      this.addContactBeanList.add(addContactBean);
    }
    addContactListAdapter.notifyDataSetChanged();
    updateNoRequestView();
  }

  @Override
  public void updateList(AddContactBean addContactBean) {
    addContactBean.setSenderInfo(SimpleUserInfo.getSimpleUserInfoFromRealm(addContactBean
        .getSenderInfo()));
    addContactBean.setAddContactRequest(AddContactRequest.getFromRealm(addContactBean
        .getAddContactRequest()));
    this.addContactBeanList.add(0, addContactBean);
    addContactListAdapter.notifyDataSetChanged();
    updateNoRequestView();
  }

  @Override
  public void acceptRequestSuccess(AddContactBean addContactBean) {
    for (int i = 0; i < addContactBeanList.size(); i++) {
      if (StringUtils.equals(addContactBeanList.get(i).getAddContactRequest().getId(),
          addContactBean.getAddContactRequest().getId())) {
        addContactBeanList.get(i).getAddContactRequest().setStatus(AddContactBean
            .STATUS_ADD_ALREADY);
      }
    }
    addContactListAdapter.notifyDataSetChanged();
  }

  @Override
  public void removeAddRequest(int position) {
    addContactBeanList.remove(position);
    addContactListAdapter.notifyDataSetChanged();
    updateNoRequestView();
  }

  class MyReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent != null) {
        switch (intent.getAction()) {
          case GlobalString.ACTION_FRIEND_REQUEST_CHANGE:
            if (addContactPresenter != null) {
              addContactPresenter.getAddContactList();
            }
            break;
          case GlobalString.ACTION_FRIEND_DELETED:
            if (addContactPresenter != null) {
              addContactPresenter.getAddContactList();
            }
            break;
          default:
            break;
        }
      }
    }
  }
}
