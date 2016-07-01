package com.biu.biu.user.model;

import android.util.Log;

import com.biu.biu.app.BiuApp;
import com.biu.biu.user.entity.ShowUserInfoBean;
import com.biu.biu.user.entity.SimpleUserInfo;
import com.biu.biu.user.entity.UserPicInfo;
import com.biu.biu.user.entity.UserPicInfoCommons;
import com.biu.biu.user.utils.UserPreferenceUtil;
import com.biu.biu.utils.GlobalString;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by fubo on 2016/5/17 0017.
 * email:bofu1993@163.com
 */
public class UserModel {

  private Retrofit retrofit = new Retrofit.Builder()
      .baseUrl(GlobalString.BASE_URL)
      .addConverterFactory(JacksonConverterFactory.create())
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .build();
  private IUserService userService = retrofit.create(IUserService.class);

  public UserModel() {

  }

  public void getMyInfo(String deviceId, Subscriber<SimpleUserInfo> subscriber) {
    userService.getMyInfo(deviceId).subscribeOn(Schedulers.newThread()).observeOn
        (AndroidSchedulers.mainThread()).subscribe(subscriber);
  }

  public void getIconAddress(String jMessageId, Subscriber<String> subscriber) {
    userService.getIconAddress(jMessageId).subscribeOn(Schedulers.newThread()).observeOn
        (AndroidSchedulers.mainThread()).subscribe(subscriber);
  }

  public void renameNickName(String deviceId, String nickName) {
    Call<ResponseBody> call = userService.renameNickName(deviceId, nickName);
    call.enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        Log.d("rename", response.toString());
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        t.printStackTrace();
      }
    });
  }

  public void postJMessageId(String deviceId, String jMessageId) {
    Call<ResponseBody> call = userService.postJMessageId(deviceId, jMessageId);
    call.enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        Log.d("postJm", response.toString());
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        t.printStackTrace();
      }
    });

  }

  public void modifyHeadIcon(String deviceId, String iconPath) {
    File file = new File(iconPath);
    RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), file);
    Call<ResponseBody> call = userService.modifyHeadIcon(MultipartBody.Part.createFormData
        ("device_id", deviceId), MultipartBody.Part.createFormData("figure", "image.png",
        requestBody));
    call.enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        Log.d("modifyIcon", response.toString());
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        t.printStackTrace();
      }
    });
  }

  public void uploadPhoto(String deviceId, String photoPath) {
    File file = new File(photoPath);
    RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), file);
    Call<ResponseBody> call = userService.uploadPhoto(MultipartBody.Part.createFormData
        ("jm_id", deviceId), MultipartBody.Part.createFormData("showoffs[]", "image.png",
        requestBody));
    call.enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        Log.d("uploadPhoto", response.toString());
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        t.printStackTrace();
      }
    });
  }

  public void requestFriend(String receiverId, String message, final OnPostFriendRequest request) {
    Call<ResponseBody> call = userService.requestFriend(UserPreferenceUtil.getUserPreferenceId(),
        receiverId, message);
    call.enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        Log.d("requestFriend", response.toString());
        request.success();
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        t.printStackTrace();
        request.failure();
      }
    });
  }

  public interface OnPostFriendRequest {
    void success();

    void failure();
  }

  public Observable<ShowUserInfoBean> queryUserInfo(String jmId) {
    return userService.queryUserInfo(UserPreferenceUtil.getUserPreferenceId(), jmId);
  }

  public void saveUserPicToDB(UserPicInfo userPicInfo) {
    Realm realm = Realm.getInstance(BiuApp.getRealmConfiguration());
    realm.beginTransaction();
    realm.copyToRealm(userPicInfo);
    realm.commitTransaction();
  }

  public void deleteUserPicFromDB(UserPicInfo userPicInfo) {
    Realm realm = Realm.getInstance(BiuApp.getRealmConfiguration());
    realm.beginTransaction();
    RealmResults<UserPicInfo> results = realm.where(UserPicInfo.class).equalTo(UserPicInfoCommons
        .KEY_USER_PIC_ID, userPicInfo.getPicId()).equalTo(UserPicInfoCommons.KEY_FLAG,
        UserPicInfoCommons.FLAG_NORMAL).findAll();
    if (results != null) {
      results.remove(0);
    }
    realm.commitTransaction();
    realm.close();
  }

  public void deleteUserPicNet(final UserPicInfo userPicInfo, int position) {
    Call<ResponseBody> call = userService.deletePhoto(UserPreferenceUtil.getUserPreferenceId(),
        position);
    call.enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        Log.d("deletePhoto", response.body().toString());
        if (response.isSuccessful()) {
          deleteUserPicFromDB(userPicInfo);
        }
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        t.printStackTrace();
      }
    });
  }

  public List<UserPicInfo> queryUserPicFromDB(String userId) {
    Realm realm = Realm.getInstance(BiuApp.getRealmConfiguration());
    realm.beginTransaction();
    RealmResults<UserPicInfo> results = realm.where(UserPicInfo.class).equalTo(UserPicInfoCommons
        .KEY_USER_ID, userId).equalTo(UserPicInfoCommons.KEY_FLAG,
        UserPicInfoCommons.FLAG_NORMAL).findAllSorted(UserPicInfoCommons.KEY_GENERATE_DATE);
    List<UserPicInfo> userPicInfos = new ArrayList<>();
    for (UserPicInfo userPicInfo : results) {
      userPicInfos.add(userPicInfo);
    }
    realm.commitTransaction();
    return userPicInfos;
  }

}
