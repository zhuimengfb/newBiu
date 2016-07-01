package com.biu.biu.user.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biu.biu.main.ChooseImageActivity;
import com.biu.biu.user.entity.UserPicInfo;
import com.biu.biu.user.entity.UserPicInfoCommons;
import com.biu.biu.user.presenter.UserPresenter;
import com.biu.biu.user.utils.UserPreferenceUtil;
import com.biu.biu.user.views.adapter.UserUploadPicAdapter;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.utils.GlideCircleTransform;
import com.biu.biu.utils.GlobalString;
import com.biu.biu.utils.ShowImageUtil;
import com.biu.biu.views.base.BaseActivity;
import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

/**
 * Created by fubo on 2016/5/17 0017.
 * email:bofu1993@163.com
 */
public class UserInfoActivity extends BaseActivity implements IUserInfo {

  private static final int REQUEST_USER_ICON_PIC = 1;
  private static final int REQUEST_USER_PIC = 2;
  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.toolbar_title)
  TextView toolbarTitle;
  @BindView(R.id.rl_setting_user_icon)
  RelativeLayout setIconLayout;
  @BindView(R.id.rl_setting_user_name)
  RelativeLayout setUserNameLayout;
  @BindView(R.id.gv_user_picture)
  RecyclerView userPictureGrid;
  @BindView(R.id.tv_setting_user_name)
  TextView tvSettingUserName;
  @BindView(R.id.iv_set_user_icon)
  ImageView ivSetUserIcon;
  @BindView(R.id.pb_upload_pic)
  ProgressBar uploadUserPicProgress;
  @BindView(R.id.pb_upload_head_icon)
  ProgressBar uploadUserHeadIcon;

  private UserPresenter userPresenter;

  private UserUploadPicAdapter userUploadPicAdapter;
  private List<UserPicInfo> userPicInfos = new ArrayList<>();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_info);
    ButterKnife.bind(this);
    userPresenter = new UserPresenter(this);
    initView();
    initData();
    initEvent();
  }

  private void initView() {
    setBackableToolbar(toolbar);
    toolbarTitle.setText(getString(R.string.edit_user_info));
    if (!StringUtils.isEmpty(UserPreferenceUtil.getUserPreferenceNickName())) {
      tvSettingUserName.setText(UserPreferenceUtil.getUserPreferenceNickName());
    }
    if (!StringUtils.isEmpty(UserPreferenceUtil.getUserIconAddress())) {
      Glide.with(this).load(new File(UserPreferenceUtil.getUserIconAddress())).transform(new
          GlideCircleTransform(this)).into(ivSetUserIcon);
      return;
    }
    Glide.with(this).load(GlobalString.BASE_URL + "/" + UserPreferenceUtil
        .getUserIconLargeNet()).placeholder(R.drawable.default_user_icon2).error(R.drawable
        .default_user_icon2).transform(new GlideCircleTransform(this)).into(ivSetUserIcon);
  }

  private void initData() {
    userPictureGrid.setLayoutManager(new GridLayoutManager(this, 3));
    userPictureGrid.setItemAnimator(new DefaultItemAnimator());
    userUploadPicAdapter = new UserUploadPicAdapter(userPicInfos);
    userPictureGrid.setAdapter(userUploadPicAdapter);
    userPresenter.queryUserPics();
  }

  private void initEvent() {
    setIconLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        modifyIcon();
      }
    });
    setUserNameLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        modifyName();
      }
    });
    userUploadPicAdapter.setOnAddPicListener(new UserUploadPicAdapter.OnAddPicListener() {
      @Override
      public void onAddClick(View view) {
        Intent intent = new Intent();
        intent.setClass(UserInfoActivity.this, ChooseImageActivity.class);
        startActivityForResult(intent, REQUEST_USER_PIC);
      }
    });
    userUploadPicAdapter.setOnOperationListener(new UserUploadPicAdapter.OnOperationListener() {
      @Override
      public boolean onRemoveClick(View view, final int position) {
        showDialog(getString(R.string.sure_to_delete), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            userPresenter.deleteUserPic(position, userPicInfos.get(position));
          }
        });
        return false;
      }

      @Override
      public void onLookDetailClick(View view, UserPicInfo userPicInfo) {
        new ShowImageUtil(UserInfoActivity.this, (ImageView) view).showImage();
      }
    });
  }

  public static void toThisActivity(Context context) {
    Intent intent = new Intent();
    intent.setClass(context, UserInfoActivity.class);
    context.startActivity(intent);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_USER_ICON_PIC:
        if (data != null) {
          Uri uri = data.getData();
          userPresenter.modifyUserIcon(uri);
        }
        break;
      case REQUEST_USER_PIC:
        if (data != null) {
          userPresenter.uploadUserPic(data.getData());
        }
        break;
      default:
        break;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  private void modifyName() {
    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(UserInfoActivity.this).inflate
        (R.layout
            .edit_dialog_layout, null);
    final EditText editText = (EditText) linearLayout.findViewById(R.id.et_dialog);
    AlertDialog alertDialog = new AlertDialog.Builder(UserInfoActivity.this).setTitle
        (getString(R.string.change_name)).setView(linearLayout).setPositiveButton(getString(R
        .string.confirm), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        userPresenter.modifyNickName(UserConfigParams.device_id, editText.getText().toString());
      }
    }).setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    }).create();
    alertDialog.show();
  }

  private void modifyIcon() {
    Intent intent = new Intent();
    intent.setClass(this, ChooseImageActivity.class);
    startActivityForResult(intent, REQUEST_USER_ICON_PIC);
  }


  @Override
  public void showInputNullName() {
    showToast(getString(R.string.input_nothing));
  }

  @Override
  public void showNetFailure() {
    showToast(getString(R.string.network_fail));
  }

  @Override
  public void setNickName(String nickName) {
    tvSettingUserName.setText(nickName);
  }

  @Override
  public void setUserIcon(Uri uri) {
    Glide.with(this).load(uri).transform(new GlideCircleTransform(this)).into(ivSetUserIcon);
  }

  @Override
  public void addUserPic(UserPicInfo userPicInfo) {
    userPicInfos.add(userPicInfo);
    userUploadPicAdapter.notifyDataSetChanged();
  }

  @Override
  public void showUploadIcon() {
    uploadUserHeadIcon.setVisibility(View.VISIBLE);
  }

  @Override
  public void hideUploadIcon() {
    uploadUserHeadIcon.setVisibility(View.GONE);
  }

  @Override
  public void showUploadPic() {
    uploadUserPicProgress.setVisibility(View.VISIBLE);
  }

  @Override
  public void hideUploadPic() {
    uploadUserPicProgress.setVisibility(View.GONE);
  }

  @Override
  public void showSuccessToast() {
    showToast(getString(R.string.upload_success));
  }

  @Override
  public void showFailToast() {
    showToast(getString(R.string.upload_fail));
  }

  @Override
  public void showAllPic(List<UserPicInfo> userPicInfos) {
    this.userPicInfos.clear();
    for (UserPicInfo userPicInfo : userPicInfos) {
      this.userPicInfos.add(userPicInfo);
    }
    userUploadPicAdapter.notifyDataSetChanged();
  }

  @Override
  public void showDeleteSuccess(int position) {
    userPicInfos.remove(position);
    userUploadPicAdapter.notifyDataSetChanged();
    showToast(getString(R.string.delete_success));
  }

  @Override
  public void showDeleteFail() {
    showToast(getString(R.string.delete_fail));
  }
}
