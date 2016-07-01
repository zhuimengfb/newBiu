package com.biu.biu.user.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.ImageView;

import com.biu.biu.views.base.BaseActivity;
import com.bumptech.glide.Glide;

import org.apache.commons.codec.binary.StringUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

/**
 * Author: FBi on 6/20/16.
 * Email: bofu1993@163.com
 */
public class PicOriginalActivity extends BaseActivity {

  @BindView(R.id.iv_pic_original)
  ImageView ivPicOriginal;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show_pic_original);
    ButterKnife.bind(this);
    String path = getIntent().getStringExtra("picPath");
    Glide.with(this).load(new File(path)).into(ivPicOriginal);
  }

  public static void toPicOriginalActivity(Context context, String path) {
    Intent intent = new Intent();
    intent.setClass(context, PicOriginalActivity.class);
    intent.putExtra("picPath", path);
    context.startActivity(intent);
  }
}
