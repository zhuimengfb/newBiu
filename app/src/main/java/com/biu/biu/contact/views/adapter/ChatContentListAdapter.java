package com.biu.biu.contact.views.adapter;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.biu.biu.app.BiuApp;
import com.biu.biu.contact.entity.ContactInfo;
import com.biu.biu.contact.utils.DateParseUtil;
import com.biu.biu.contact.views.ChatActivity;
import com.biu.biu.contact.views.ChatPicOriginalActivity;
import com.biu.biu.user.utils.UserPreferenceUtil;
import com.biu.biu.user.views.ShowUserInfoActivity;
import com.biu.biu.utils.GlideCircleTransform;
import com.biu.biu.utils.GlobalString;
import com.biu.biu.utils.ShowImageUtil;
import com.bumptech.glide.Glide;
import com.rockerhieu.emojicon.EmojiconTextView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.model.Message;
import grf.biu.R;

/**
 * Created by fubo on 2016/5/30 0030.
 * email:bofu1993@163.com
 */
public class ChatContentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private List<Message> chatMessages;
  private static final int TYPE_MESSAGE_DEFAULT = 0;
  private static final int TYPE_MESSAGE_TEXT_LEFT = 1;
  private static final int TYPE_MESSAGE_TEXT_RIGHT = 2;
  private long lastTime = 0;
  private ContactInfo contactInfo;
  private WeakReference<Activity> activityWeakReference;
  private float mDensity;
  private File userIconFile = new File(UserPreferenceUtil.getUserIconAddress());

  public void setActivity(Activity activity) {
    activityWeakReference = new WeakReference<Activity>(activity);
    DisplayMetrics displayMetrics = new DisplayMetrics();
    activityWeakReference.get().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    mDensity = displayMetrics.density;
  }

  public ChatContentListAdapter(List<Message> chatMessages) {
    this.chatMessages = chatMessages;
  }

  public void setContactInfo(ContactInfo contactInfo) {
    this.contactInfo = contactInfo;
  }

  @Override
  public int getItemViewType(int position) {
    if (chatMessages.get(position).getFromUser().getUserName().equals(JMessageClient.getMyInfo
        ().getUserName())) {
      return TYPE_MESSAGE_TEXT_RIGHT;
    } else {
      return TYPE_MESSAGE_TEXT_LEFT;
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case TYPE_MESSAGE_TEXT_LEFT:
        return new ContentViewHolder(LayoutInflater.from(BiuApp.getContext()).inflate(R.layout
            .item_chat_content_left, parent, false));
      case TYPE_MESSAGE_TEXT_RIGHT:
        return new ContentViewHolder(LayoutInflater.from(BiuApp.getContext()).inflate(R.layout
            .item_chat_content_right, parent, false));
      default:
        return null;
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    showTime(holder, position);
    switch (getItemViewType(position)) {
      case TYPE_MESSAGE_TEXT_LEFT:
        if (chatMessages.get(position).getContentType() == ContentType.text) {
          showText(holder, position);
        } else if (chatMessages.get(position).getContentType() == ContentType.image) {
          showImage(holder, position);
        }
        Glide.with(BiuApp.getContext()).load(GlobalString.BASE_URL + "/" + contactInfo
            .getIconNetAddress()).transform(new GlideCircleTransform(BiuApp.getContext())).error
            (R.drawable.default_user_icon2).into(((ContentViewHolder) holder).userIcon);
        ((ContentViewHolder) holder).userIcon.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            ShowUserInfoActivity.toShowUserPicActivity(activityWeakReference.get(), contactInfo
                .getUserId());
          }
        });
        break;
      case TYPE_MESSAGE_TEXT_RIGHT:
        if (chatMessages.get(position).getContentType() == ContentType.text) {
          showText(holder, position);
        } else if (chatMessages.get(position).getContentType() == ContentType.image) {
          showImage(holder, position);
        }
        if (userIconFile.exists()) {
          Glide.with(BiuApp.getContext()).load(Uri.fromFile(userIconFile))
              .transform(new GlideCircleTransform(BiuApp.getContext()))
              .placeholder(R.drawable.default_user_icon2).error(R.drawable.default_user_icon2)
              .into(((ContentViewHolder) holder).userIcon);
        } else {
          Glide.with(BiuApp.getContext()).load(GlobalString.BASE_URL + "/" + UserPreferenceUtil
              .getUserIconSmallNet()).transform(new GlideCircleTransform(BiuApp.getContext()))
              .placeholder(R.drawable.default_user_icon2).error(R.drawable.default_user_icon2)
              .into(((ContentViewHolder) holder).userIcon);
        }
        break;
      default:
        break;
    }
  }

  private void showTime(RecyclerView.ViewHolder holder, int position) {
    if (position == chatMessages.size() - 1) {
      lastTime = 0;
    } else {
      lastTime = chatMessages.get(position + 1).getCreateTime();
    }
    if (StringUtils.isEmpty(DateParseUtil.parseDate(lastTime, chatMessages.get(position)
        .getCreateTime()))) {
      ((ContentViewHolder) holder).messageTime.setVisibility(View.GONE);
    } else {
      ((ContentViewHolder) holder).messageTime.setVisibility(View.VISIBLE);
      ((ContentViewHolder) holder).messageTime.setText(DateParseUtil.parseDate(lastTime,
          chatMessages.get(position).getCreateTime()));
    }
  }

  private void showText(RecyclerView.ViewHolder holder, int position) {
    ((ContentViewHolder) holder).chatContent.setVisibility(View.VISIBLE);
    ((ContentViewHolder) holder).chatPicImageView.setVisibility(View.GONE);
    try {
      ((ContentViewHolder) holder).chatContent.setText(new JSONObject(chatMessages.get(position)
          .getContent().toJson()).getString("text"));
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private void setPictureScale(String path, ImageView imageView) {
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(path, opts);
    //计算图片缩放比例
    double imageWidth = opts.outWidth;
    double imageHeight = opts.outHeight;
    if (imageWidth < 100 * mDensity) {
      imageHeight = imageHeight * (100 * mDensity / imageWidth);
      imageWidth = 100 * mDensity;
    }
    ViewGroup.LayoutParams params = imageView.getLayoutParams();
    params.width = (int) imageWidth;
    params.height = (int) imageHeight;
    imageView.setLayoutParams(params);
  }

  private void showImage(final RecyclerView.ViewHolder holder, final int position) {
    ((ContentViewHolder) holder).chatContent.setVisibility(View.GONE);
    ((ContentViewHolder) holder).chatPicImageView.setVisibility(View.VISIBLE);
    ImageContent imageContent = (ImageContent) chatMessages.get(position).getContent();
    final String path = imageContent.getLocalPath();
    if (path == null) {
      imageContent.downloadOriginImage(chatMessages.get(position), new
          DownloadCompletionCallback() {
            @Override
            public void onComplete(int i, String s, File file) {
              //              setPictureScale(path, ((ContentViewHolder) holder).chatPicImageView);
              Glide.with(BiuApp.getContext()).load(file).into(((ContentViewHolder) holder)
                  .chatPicImageView);
            }
          });
    } else {
      //      setPictureScale(path, ((ContentViewHolder) holder).chatPicImageView);
      Glide.with(BiuApp.getContext()).load(new File(path)).into(((ContentViewHolder) holder)
          .chatPicImageView);
    }
    ((ContentViewHolder) holder).chatPicImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new ShowImageUtil(activityWeakReference.get(), ((ContentViewHolder) holder)
            .chatPicImageView).showImage();
        /*ChatPicOriginalActivity.toPicOriginalActivity(activityWeakReference.get(), contactInfo
            .getUserId(), position);*/
      }
    });
  }

  @Override
  public int getItemCount() {
    return chatMessages.size();
  }

  class ContentViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.iv_chat_user_icon)
    ImageView userIcon;
    @BindView(R.id.tv_chat_content)
    EmojiconTextView chatContent;
    @BindView(R.id.tv_message_time)
    TextView messageTime;
    @BindView(R.id.iv_chat_pic)
    ImageView chatPicImageView;

    public ContentViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
