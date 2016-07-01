package com.biu.biu.contact.presenter;

import com.biu.biu.contact.entity.ContactInfo;
import com.biu.biu.contact.model.ChatModel;
import com.biu.biu.contact.model.ContactModel;
import com.biu.biu.contact.model.IChatModel;
import com.biu.biu.contact.model.IContactModel;
import com.biu.biu.contact.views.IChatActivityView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Message;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by fubo on 2016/6/8 0008.
 * email:bofu1993@163.com
 */
public class ChatPresenter {

  private IChatActivityView chatActivityView;
  private IChatModel chatModel;
  private IContactModel contactModel;

  public ChatPresenter(IChatActivityView chatActivityView) {
    this.chatActivityView = chatActivityView;
    chatModel = new ChatModel();
    contactModel = new ContactModel();
  }

  public ContactInfo getContactInfo(String contactId) {
    return contactModel.queryContactInfo(contactId);
  }

  public void unbind() {
    chatActivityView = null;
    chatModel = null;
  }

  public void sendMessage(String userId, String message) {
    Message message1 = JMessageClient.createSingleTextMessage(userId, message);
    JMessageClient.sendMessage(message1);
    chatActivityView.updateChatMessage(message1);
  }

  public void sendImageMessage(String userId, String imagePath) {
    try {
      Message message = JMessageClient.createSingleImageMessage(userId,new
          File(imagePath));
      JMessageClient.sendMessage(message);
      chatActivityView.updateChatMessage(message);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void queryChatHistory(String userId, final int currentPage) {
    Observable.just(userId).map(new Func1<String, List<Message>>() {
      @Override
      public List<Message> call(String s) {
        return JMessageClient.getSingleConversation(s).getMessagesFromNewest(currentPage * 10,
            (currentPage + 1) * 10);
      }
    }).subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<List<Message>>() {
          @Override
          public void onCompleted() {

          }

          @Override
          public void onError(Throwable e) {

          }

          @Override
          public void onNext(List<Message> messages) {
            chatActivityView.updateChatMessages(messages);
          }
        });
  }
}
