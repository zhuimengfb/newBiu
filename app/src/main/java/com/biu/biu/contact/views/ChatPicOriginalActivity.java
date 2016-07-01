package com.biu.biu.contact.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.ImageView;

import com.biu.biu.views.base.BaseActivity;
import com.bumptech.glide.Glide;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import grf.biu.R;

/**
 * Created by fubo on 2016/6/11 0011.
 * email:bofu1993@163.com
 */
public class ChatPicOriginalActivity extends BaseActivity {


  @BindView(R.id.iv_pic_original)
  ImageView ivPicOriginal;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show_pic_original);
    ButterKnife.bind(this);
    String userId = getIntent().getStringExtra("userId");
    int position = getIntent().getIntExtra("position", -1);
    if (position == -1) {
      finish();
    }
    Conversation conversation = JMessageClient.getSingleConversation(userId);
    final Message message = conversation.getMessagesFromNewest(0, position + 1).get(position);
    ((ImageContent) message.getContent()).downloadOriginImage(message, new
        DownloadCompletionCallback() {
          @Override
          public void onComplete(int i, String s, File file) {
            if (i == 0) {
              Glide.with(ChatPicOriginalActivity.this).load(file).into(ivPicOriginal);
            } else {
              Glide.with(ChatPicOriginalActivity.this).load(((ImageContent) message.getContent())
                  .getLocalThumbnailPath()).into(ivPicOriginal);
            }
          }
        });
    Glide.with(this).load(((ImageContent) message.getContent()).getLocalThumbnailPath()).into
        (ivPicOriginal);
  }

  public static void toPicOriginalActivity(Context context, String userId, int position) {
    Intent intent = new Intent();
    intent.setClass(context, ChatPicOriginalActivity.class);
    intent.putExtra("userId", userId);
    intent.putExtra("position", position);
    context.startActivity(intent);
  }
}
