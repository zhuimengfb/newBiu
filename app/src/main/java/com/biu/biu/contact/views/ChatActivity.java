package com.biu.biu.contact.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.biu.biu.app.BiuApp;
import com.biu.biu.contact.entity.ContactInfo;
import com.biu.biu.contact.presenter.ChatPresenter;
import com.biu.biu.contact.utils.ContactAction;
import com.biu.biu.contact.views.adapter.ChatContentListAdapter;
import com.biu.biu.main.ChooseImageActivity;
import com.biu.biu.main.ChooseImgResActivity;
import com.biu.biu.utils.GlobalString;
import com.biu.biu.views.base.BaseActivity;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import grf.biu.R;

/**
 * Created by fubo on 2016/5/29 0029.
 * email:bofu1993@163.com
 */
public class ChatActivity extends BaseActivity
    implements IChatActivityView, EmojiconGridFragment.OnEmojiconClickedListener,
               EmojiconsFragment.OnEmojiconBackspaceClickedListener {

  @BindView(R.id.rv_chat_list)
  RecyclerView chatRecyclerView;
  @BindView(R.id.et_chat_content)
  EmojiconEditText chatContent;
  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.toolbar_title)
  TextView toolbarTitle;
  @BindView(R.id.emojicons)
  FrameLayout emojLayout;
  @BindView(R.id.iv_emoj_icon)
  ImageView emojImageView;
  @BindView(R.id.iv_send_pic)
  ImageView sendPicView;
  @BindView(R.id.layout_chat_room)
  RelativeLayout rvChatroom;
  @BindView(R.id.tv_send)
  TextView sendText;

  private int currentPage = 0;
  private ChatContentListAdapter chatContentListAdapter;
  private List<Message> chatMessages = new ArrayList<>();
  private ContactInfo contactInfo;
  private ChatPresenter chatPresenter;
  private String contactId;
  private boolean hasMoreHistoryMessage = true;

  private final int REQUEST_CODE_PICK_IMAGE = 100;
  private final int REQUEST_CODE_CAPTURE_CAMEIA = 101;
  private final int REQUEST_CODE_CHOOSE_IMAGE_SOURCE = 102;
  private String mCapturePhotoPath; // 拍照得到的相片存储地址

  private MyReceiver myReceiver = new MyReceiver();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat_room);
    ButterKnife.bind(this);
    chatPresenter = new ChatPresenter(this);
    initContact();
    initView();
    initData();
    initEvent();
  }

  private void initReceiver() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(GlobalString.ACTION_FRIEND_DELETED);
    registerReceiver(myReceiver, intentFilter);
  }

  @Override
  protected void onResume() {
    initContact();
    initReceiver();
    JMessageClient.enterSingleConversation(contactInfo.getUserId());
    JMessageClient.registerEventReceiver(this);
    Conversation conversation = JMessageClient.getSingleConversation(contactId);
    if (conversation != null) {
      conversation.resetUnreadCount();
    }
    super.onResume();
  }

  @Override
  protected void onPause() {
    hideKeyBoard();
    hideEmoj();
    Conversation conversation = JMessageClient.getSingleConversation(contactId);
    if (conversation != null) {
      conversation.resetUnreadCount();
    }
    JMessageClient.exitConversation();
    unregisterReceiver(myReceiver);
    super.onPause();
  }

  private void initContact() {
    if (StringUtils.isEmpty(getIntent().getStringExtra(ContactAction.KEY_CONTACT_ID))) {
      finish();
    }
    contactId = getIntent().getStringExtra(ContactAction.KEY_CONTACT_ID);
    ContactInfo contactInfo = chatPresenter.getContactInfo(getIntent().getStringExtra
        (ContactAction.KEY_CONTACT_ID));
    if (contactInfo == null || StringUtils.isEmpty(contactInfo.getUserId())) {
      setResult(120);
      finish();
    } else {
      this.contactInfo = contactInfo;
    }
  }

  private void initView() {
    setSupportActionBar(toolbar);
    setBackableToolbar(toolbar);
    toolbarTitle.setText(contactInfo.getName());
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setReverseLayout(true);
    chatRecyclerView.setLayoutManager(layoutManager);
    chatRecyclerView.setItemAnimator(new DefaultItemAnimator());
    setEmojiconFragment(false);
  }

  private void initData() {
    chatContentListAdapter = new ChatContentListAdapter(chatMessages);
    chatContentListAdapter.setActivity(this);
    chatRecyclerView.setAdapter(chatContentListAdapter);
    chatContentListAdapter.setContactInfo(contactInfo);
    chatPresenter.queryChatHistory(contactInfo.getUserId(), currentPage);
  }

  private void initEvent() {
    /*chatContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
          sendMessage(chatContent.getText().toString());
        }
        return false;
      }
    });*/
    chatContent.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        if (StringUtils.isEmpty(editable.toString())) {
          hideSendText();
          showSendPic();
        } else {
          hideSendPic();
          showSendText();
        }
      }
    });
    sendText.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        sendMessage(chatContent.getEditableText().toString());
      }
    });
    emojImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideKeyBoard();
        if (emojLayout.getVisibility() == View.VISIBLE) {
          hideEmoj();
        } else {
          showEmoj();
        }
      }
    });
    sendPicView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideEmoj();
        hideKeyBoard();
        Intent intent = new Intent();
        intent.setClass(ChatActivity.this, ChooseImgResActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE_SOURCE);
      }
    });
    chatContent.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
          boolean bool = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
              .showSoftInput(v, InputMethodManager.SHOW_FORCED);
          if (bool) {
            hideEmoj();
          }
        }
        return false;
      }
    });
    chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        int position = 0;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
          position = ((LinearLayoutManager) recyclerView.getLayoutManager())
              .findLastVisibleItemPosition();
        }
        if (position == chatMessages.size() - 1 && hasMoreHistoryMessage) {
          currentPage++;
          chatPresenter.queryChatHistory(contactInfo.getUserId(), currentPage);
        }
        super.onScrolled(recyclerView, dx, dy);
      }

      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
      }
    });
    chatRecyclerView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        hideKeyBoard();
        hideEmoj();
      }
    });
  }

  private void hideSendText() {
    sendText.setVisibility(View.GONE);
  }

  private void showSendText() {
    sendText.setVisibility(View.VISIBLE);
  }

  private void hideSendPic() {
    sendPicView.setVisibility(View.GONE);
  }

  private void showSendPic() {
    sendPicView.setVisibility(View.VISIBLE);
  }
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      View v = getCurrentFocus();
      if (isShouldHideInput(v, ev)) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context
            .INPUT_METHOD_SERVICE);
        float a = getWindow().getWindowManager().getDefaultDisplay().getHeight() - ev.getY();
        int b = emojLayout.getMeasuredHeight();
        if (imm != null) {
          imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        if (Math.round(a) > b) {
          hideEmoj();
        }
      }
      return super.dispatchTouchEvent(ev);
    }
    // 必不可少，否则所有的组件都不会有TouchEvent了
    if (getWindow().superDispatchTouchEvent(ev)) {
      return true;
    }
    return onTouchEvent(ev);
  }

  public boolean isShouldHideInput(View v, MotionEvent event) {
    if (v != null && (v instanceof EditText)) {
      int[] leftTop = {0, 0};
      //获取输入框当前的location位置
      v.getLocationInWindow(leftTop);
      int left = leftTop[0];
      int top = leftTop[1];
      int bottom = top + v.getHeight();
      int right = left + v.getWidth();
      if (event.getX() > left && event.getX() < right
          && event.getY() > top && event.getY() < bottom) {
        // 点击的是输入框区域，保留点击EditText的事件
        return false;
      } else {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    switch (requestCode) {
      case REQUEST_CODE_PICK_IMAGE:// 从本地选择完毕图片
        if (data == null) {
          return;
        }
        Uri uri = data.getData();
        if (uri != null) {
          chatPresenter.sendImageMessage(contactInfo.getUserId(), uri.getPath());
        }
        break;
      case REQUEST_CODE_CAPTURE_CAMEIA: // 拍了一张照片
        if (resultCode == RESULT_OK) {
          chatPresenter.sendImageMessage(contactInfo.getUserId(), mCapturePhotoPath);
        }
        break;

      case REQUEST_CODE_CHOOSE_IMAGE_SOURCE: // 执行的动作是选择图片来源。
        if (0 == resultCode) {
          // 拍照
          getImageFromCamera();
        } else if (1 == resultCode) {
          // 来自图片
          Intent intent = new Intent();
          intent.setClass(ChatActivity.this, ChooseImageActivity.class);
          startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        } else {
          // 取消，do nothing.
        }
        break;
    }
  }

  // 从相机获取图片
  protected void getImageFromCamera() {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    File sdcache = getExternalCacheDir(); // 获得外置图片缓存路径
    File f = new File(sdcache, System.currentTimeMillis() + ".jpg");
    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
    startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);
    mCapturePhotoPath = f.getAbsolutePath();
  }

  private void hideKeyBoard() {
    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow
        (chatContent.getWindowToken(), 0);
  }

  private void sendMessage(String message) {
    if (StringUtils.isEmpty(message)) {
      Toast.makeText(BiuApp.getContext(), getString(R.string.input_nothing), Toast.LENGTH_SHORT)
          .show();
      return;
    }
    chatPresenter.sendMessage(contactInfo.getUserId(), message);
    chatContent.setText("");
  }

  public static void toChatActivity(Context context, ContactInfo contactInfo) {
    Intent intent = new Intent();
    intent.setClass(context, ChatActivity.class);
    intent.putExtra(ContactAction.KEY_CONTACT_ID, contactInfo.getUserId());
    context.startActivity(intent);
  }

  private void setEmojiconFragment(boolean useSystemDefault) {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.emojicons, EmojiconsFragment.newInstance(useSystemDefault))
        .commit();
    hideEmoj();
  }


  /**
   * 接收消息类事件
   *
   * @param event 消息事件
   */
  public void onEvent(MessageEvent event) {
    final Message msg = event.getMessage();
    //刷新消息
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        //收到消息的类型为单聊
        if (msg.getTargetType() == ConversationType.single) {
          UserInfo userInfo = (UserInfo) msg.getTargetInfo();
          String targetId = userInfo.getUserName();
          String appKey = userInfo.getAppKey();
          //判断消息是否在当前会话中
          if (contactInfo != null && StringUtils.equals(targetId, contactId)) {
            Message lastMsg = null;
            if (chatMessages.size() > 0) {
              lastMsg = chatMessages.get(0);
            }
            //收到的消息和Adapter中最后一条消息比较，如果最后一条为空或者不相同，则加入到MsgList
            if (lastMsg == null || msg.getId() != lastMsg.getId()) {
              chatMessages.add(0, msg);
            }
            chatContentListAdapter.notifyDataSetChanged();
          }
        }
      }
    });
  }

  public void showEmoj() {
    emojLayout.setVisibility(View.VISIBLE);
  }

  public void hideEmoj() {
    emojLayout.setVisibility(View.GONE);
  }

  private void showNoMoreFriend() {
    AlertDialog alertDialog = new AlertDialog.Builder(this)
        .setCancelable(false).setTitle(R
            .string.hint)
        .setMessage("好友不存在，返回通讯录")
        .setPositiveButton(R.string.confirm, new
            DialogInterface.OnClickListener() {

              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                finish();
              }
            }).create();
    alertDialog.show();
  }

  @Override
  public void onBackPressed() {
    finish();
    super.onBackPressed();
  }

  @Override
  public void onEmojiconBackspaceClicked(View v) {
    EmojiconsFragment.backspace(chatContent);
  }

  @Override
  public void onEmojiconClicked(Emojicon emojicon) {
    EmojiconsFragment.input(chatContent, emojicon);
  }

  @Override
  public void updateChatMessages(List<Message> chatMessageBeen) {
    if (chatMessageBeen == null || chatMessageBeen.size() == 0) {
      hasMoreHistoryMessage = false;
      return;
    }
    this.chatMessages.addAll(chatMessageBeen);
    chatContentListAdapter.notifyDataSetChanged();
  }

  @Override
  public void updateChatMessage(Message message) {
    chatMessages.add(0, message);
    chatContentListAdapter.notifyDataSetChanged();
  }

  class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent != null && intent.getAction() != null) {
        switch (intent.getAction()) {
          case GlobalString.ACTION_FRIEND_DELETED:
            String friendId = intent.getStringExtra(GlobalString.KEY_DELETED_FRIEND_ID);
            if (!StringUtils.isEmpty(friendId) && StringUtils.equals(contactId, friendId)) {
              showNoMoreFriend();
            }
            break;
          default:
            break;
        }
      }
    }
  }

}
