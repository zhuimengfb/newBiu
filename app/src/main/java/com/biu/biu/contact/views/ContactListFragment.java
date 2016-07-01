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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biu.biu.contact.entity.ContactInfo;
import com.biu.biu.contact.presenter.ContactPresenter;
import com.biu.biu.contact.utils.ContactAction;
import com.biu.biu.contact.views.adapter.ContactRecyclerAdapter;
import com.biu.biu.user.utils.UserPreferenceUtil;
import com.biu.biu.utils.GlobalString;
import com.biu.biu.views.base.BaseFragment;
import com.camnter.easyrecyclerviewsidebar.EasyRecyclerViewSidebar;
import com.camnter.easyrecyclerviewsidebar.sections.EasyImageSection;
import com.camnter.easyrecyclerviewsidebar.sections.EasySection;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

/**
 * Created by fubo on 2016/5/18 0018.
 * email:bofu1993@163.com
 */
public class ContactListFragment extends BaseFragment implements IContactListView {


  @BindView(R.id.rv_contact_list)
  RecyclerView contactRecyclerView;
  @BindView(R.id.contact_sidebar)
  EasyRecyclerViewSidebar easyRecyclerViewSidebar;
  @BindView(R.id.section_layout)
  RelativeLayout sectionLayout;
  @BindView(R.id.tv_section_letter)
  TextView sectionLetter;
  @BindView(R.id.rl_nothing_layout)
  RelativeLayout nothingLayout;

  private ContactPresenter contactPresenter;

  private ContactRecyclerAdapter contactRecyclerAdapter;
  private List<ContactInfo> contactInfoList = new ArrayList<>();
  private MyReceiver myReceiver = new MyReceiver();


  public static ContactListFragment getInstance() {
    ContactListFragment contactListFragment = new ContactListFragment();
    return contactListFragment;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
  Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_contact, container, false);
    ButterKnife.bind(this, view);
    contactPresenter = new ContactPresenter(this);
    initView();
    initData();
    return view;
  }

  private void initView() {
    contactRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    contactRecyclerView.setItemAnimator(new DefaultItemAnimator());
    contactRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity
        ()).margin(40, 60).size(1).build());
  }

  private void initData() {
    contactPresenter.queryContact(UserPreferenceUtil.getUserPreferenceId());
    contactRecyclerAdapter = new ContactRecyclerAdapter(getActivity(), contactInfoList);
    contactRecyclerView.setAdapter(contactRecyclerAdapter);
    easyRecyclerViewSidebar.setSections(contactRecyclerAdapter.getSections());
    easyRecyclerViewSidebar.setFloatView(sectionLayout);
    easyRecyclerViewSidebar.setOnTouchSectionListener(new EasyRecyclerViewSidebar
        .OnTouchSectionListener() {

      @Override
      public void onTouchImageSection(int sectionIndex, EasyImageSection imageSection) {

      }

      @Override
      public void onTouchLetterSection(int sectionIndex, EasySection letterSection) {
        sectionLetter.setText(letterSection.letter);
        ((LinearLayoutManager) contactRecyclerView.getLayoutManager()).scrollToPositionWithOffset
            (contactRecyclerAdapter.getHeaderPosition(sectionIndex), 0);
      }
    });
    contactRecyclerAdapter.setOnItemClickListener(new ContactRecyclerAdapter.OnItemClickListener() {
      @Override
      public void onItemClick(ContactInfo contactInfo) {
        //ChatActivity.toChatActivity(getActivity(), contactInfo);
        Intent intent = new Intent();
        intent.setClass(getActivity(), ChatActivity.class);
        intent.putExtra(ContactAction.KEY_CONTACT_ID, contactInfo.getUserId());
        startActivityForResult(intent, 100);
      }
    });
    contactRecyclerAdapter.setRemoveContactListener(new ContactRecyclerAdapter
        .RemoveContactListener() {
      @Override
      public void removeContact(final ContactInfo contactInfo, final int position) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string
            .delete_contact).setPositiveButton(R.string.confirm, new DialogInterface
            .OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            contactPresenter.removeContact(contactInfo, position);
          }
        }).setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
          }
        }).create();
        alertDialog.show();
      }
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == 120) {
      showNoMoreFriend();
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private void showNoMoreFriend() {
    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setTitle(R
        .string.hint).setMessage("好友不存在，返回通讯录").setPositiveButton(R.string.confirm, new
        DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
          }
        }).create();
    alertDialog.show();
  }

  @Override
  public void updateContactList(List<ContactInfo> contactInfoList) {
    this.contactInfoList.clear();
    contactRecyclerAdapter.notifyDataSetChanged();
    this.contactInfoList.addAll(contactInfoList);
    contactRecyclerAdapter.updateData(this.contactInfoList);
    easyRecyclerViewSidebar.setSections(contactRecyclerAdapter.getSections());
    updateShowNothing();
  }

  private void updateShowNothing() {
    if (contactInfoList.size() > 0) {
      nothingLayout.setVisibility(View.GONE);
    } else {
      nothingLayout.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void hasNewRequest(int number) {
    contactRecyclerAdapter.setHasNewRequest(number);
  }

  @Override
  public void deleteContactSuccess(int position) {
    contactInfoList.remove(position);
    contactRecyclerAdapter.updateData(this.contactInfoList);
    easyRecyclerViewSidebar.setSections(contactRecyclerAdapter.getSections());
    updateShowNothing();
  }

  @Override
  public void showNoFriend() {
    nothingLayout.setVisibility(View.VISIBLE);
  }

  class AddContactListener implements View.OnClickListener {

    @Override
    public void onClick(View v) {
      Intent intent = new Intent();
      intent.setClass(getActivity(), AddContactActivity.class);
      getActivity().startActivity(intent);
    }
  }

  @Override
  public void onResume() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(GlobalString.ACTION_FRIEND_DELETED);
    intentFilter.addAction(GlobalString.ACTION_NEW_FRIEND_REQUEST);
    intentFilter.addAction(GlobalString.ACTION_FRIEND_CONFIRM);
    intentFilter.addAction(GlobalString.ACTION_NEW_MESSAGE);
    getActivity().registerReceiver(myReceiver, intentFilter);
    if (contactPresenter != null) {
      //contactPresenter.queryUnDealRequestCount();
      contactPresenter.queryContact(UserPreferenceUtil.getUserPreferenceId());
    }
    super.onResume();
  }

  @Override
  public void onDestroy() {
    getActivity().unregisterReceiver(myReceiver);
    super.onDestroy();
  }

  class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent != null) {
        switch (intent.getAction()) {
          case GlobalString.ACTION_FRIEND_DELETED:
            String contactId = intent.getStringExtra(GlobalString.KEY_DELETED_FRIEND_ID);
            for (int i = 0; i < contactInfoList.size(); i++) {
              if (StringUtils.equals(contactId, contactInfoList.get(i).getUserId())) {
                contactInfoList.remove(i);
                contactRecyclerAdapter.updateData(contactInfoList);
                if (i > 0 && contactInfoList.size() > 10) {
                  contactRecyclerView.smoothScrollToPosition(i - 2);
                }
              }
            }
            break;
          case GlobalString.ACTION_NEW_FRIEND_REQUEST:
            hasNewRequest(1);
            break;
          case GlobalString.ACTION_FRIEND_CONFIRM:
            if (contactPresenter != null) {
              contactPresenter.queryContact(UserPreferenceUtil.getUserPreferenceId());
            }
            break;
          case GlobalString.ACTION_NEW_MESSAGE:
            if (contactPresenter != null) {
              contactPresenter.queryContact(UserPreferenceUtil.getUserPreferenceId());
            }
            break;
          default:
            break;
        }

      }
    }
  }
}
