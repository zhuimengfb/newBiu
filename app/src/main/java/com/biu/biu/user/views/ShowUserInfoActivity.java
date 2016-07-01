package com.biu.biu.user.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.biu.biu.app.BiuApp;
import com.biu.biu.user.entity.ShowUserInfoBean;
import com.biu.biu.user.presenter.UserShowPresenter;
import com.biu.biu.user.utils.CommonAction;
import com.biu.biu.user.utils.UserPreferenceUtil;
import com.biu.biu.user.views.adapter.UserPicShowAdapter;
import com.biu.biu.utils.GlideCircleTransform;
import com.biu.biu.utils.GlobalString;
import com.biu.biu.views.base.BaseActivity;
import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

/**
 * Created by fubo on 2016/5/27 0027.
 * email:bofu1993@163.com
 */
public class ShowUserInfoActivity extends BaseActivity implements IShowUserInfo {

  @BindView(R.id.view_pager_show_user_pic)
  ViewPager viewPager;
  @BindView(R.id.indicator_layout)
  LinearLayout indicatorLayout;
  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.toolbar_title)
  TextView toolbarTitle;
  @BindView(R.id.iv_not_interest)
  ImageView ivNotInterest;
  @BindView(R.id.iv_interest)
  ImageView ivInterest;
  private Button preSelectedBt;
  private UserPicShowAdapter userPicShowAdapter;
  private LinearLayout.LayoutParams normalDotParams;
  private LinearLayout.LayoutParams activeDotParams;
  private List<View> showViews = new ArrayList<>();
  private String userJmId;
  private UserShowPresenter userShowPresenter;
  private boolean canRequestFriend = true;
  private String[] picAddress;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show_user_info);
    ButterKnife.bind(this);
    userJmId = getIntent().getStringExtra(CommonAction.KEY_USER_JM_ID);
    initView();
    initData();
    initEvent();
  }

  private void initView() {
    setSupportActionBar(toolbar);
    setBackableToolbar(toolbar);
    toolbarTitle.setText("");
    normalDotParams = new LinearLayout.LayoutParams(20, 20);
    normalDotParams.leftMargin = 5;
    normalDotParams.rightMargin = 5;
    activeDotParams = new LinearLayout.LayoutParams(20, 20);
    activeDotParams.leftMargin = 5;
    activeDotParams.rightMargin = 5;
  }

  private void initData() {
    userShowPresenter = new UserShowPresenter(this);
    userShowPresenter.queryAlreadyFriend(userJmId);
    userShowPresenter.queryUserInfo(userJmId);
    View view = LayoutInflater.from(this).inflate(R.layout.item_show_user_main_info, null);
    showViews.add(view);
    userPicShowAdapter = new UserPicShowAdapter(showViews);
    viewPager.setAdapter(userPicShowAdapter);
    for (int i = 0; i < showViews.size(); i++) {
      Button bt = new Button(this);
      if (i == 0) {
        bt.setBackgroundResource(R.drawable.dot_indicator_active);
        bt.setLayoutParams(activeDotParams);
        preSelectedBt = bt;
      } else {
        bt.setBackgroundResource(R.drawable.dot_indicator_normal);
        bt.setLayoutParams(normalDotParams);
      }
      indicatorLayout.addView(bt);
    }
  }

  private void initEvent() {
    viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {
        if (position != 0 && picAddress != null) {
          Glide.with(BiuApp.getContext()).load(GlobalString.BASE_URL + "/" + picAddress[position
              - 1]).error(R.drawable.default_big_icon).into((ImageView) showViews.get(position)
              .findViewById(R.id.iv_show_user_pic));
        }
        if (preSelectedBt != null) {
          preSelectedBt.setBackgroundResource(R.drawable.dot_indicator_normal);
          preSelectedBt.setLayoutParams(normalDotParams);
        }
        Button currentBt = (Button) indicatorLayout.getChildAt(position);
        currentBt.setBackgroundResource(R.drawable.dot_indicator_active);
        currentBt.setLayoutParams(activeDotParams);
        preSelectedBt = currentBt;
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });
    ivInterest.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (StringUtils.equals(userJmId, UserPreferenceUtil.getUserPreferenceId())) {
          UserInfoActivity.toThisActivity(ShowUserInfoActivity.this);
          return;
        }
        if (canRequestFriend) {
          SendRequestActivity.toSendRequestActivity(ShowUserInfoActivity.this, userJmId);
        }
      }
    });
    ivNotInterest.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });
  }

  public static void toShowUserPicActivity(Context context, String jmId) {
    Intent intent = new Intent();
    intent.setClass(context, ShowUserInfoActivity.class);
    intent.putExtra(CommonAction.KEY_USER_JM_ID, jmId);
    context.startActivity(intent);
  }

  @Override
  public void showToolbarName(String name) {
    toolbarTitle.setText(name);
  }

  @Override
  public void showUserInfo(ShowUserInfoBean userInfo) {
    ((TextView) showViews.get(0).findViewById(R.id.tv_show_user_name)).setText(userInfo
        .getNickname());
    Glide.with(this).load(GlobalString.BASE_URL + "/" + userInfo.getIcon_large()).placeholder(R
        .drawable.default_big_icon).error(R.drawable.default_big_icon).transform(new GlideCircleTransform(this)).into(
        (ImageView)
            showViews.get(0).findViewById(R.id.iv_show_user_head));
    userPicShowAdapter.notifyDataSetChanged();
  }

  @Override
  public void isAlreadyFriend() {
    this.canRequestFriend = false;
    ivInterest.setImageURI(Uri.parse(GlobalString.URI_RES_PREFIX + R.drawable.like_user_after));
  }

  @Override
  public void isNotFriendYet() {
    this.canRequestFriend = true;
    ivInterest.setImageURI(Uri.parse(GlobalString.URI_RES_PREFIX + R.drawable.like_user_before));
  }

  @Override
  public void updateUserPic(String[] picAddresses) {
    this.picAddress = picAddresses;
    for (int i = 0; i < picAddresses.length; i++) {
      View view = LayoutInflater.from(this).inflate(R.layout.item_show_user_info, null);
      /*Glide.with(this).load(GlobalString.BASE_URL + "/" + picAddresses[i]).error(R.drawable
          .default_big_icon).into((ImageView) view.findViewById(R.id.iv_show_user_pic));*/
      showViews.add(view);
      Button bt = new Button(this);
      bt.setBackgroundResource(R.drawable.dot_indicator_normal);
      bt.setLayoutParams(normalDotParams);
      indicatorLayout.addView(bt);
    }
    userPicShowAdapter.notifyDataSetChanged();
  }
}
