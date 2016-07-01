package com.biu.biu.contact.model;

import com.biu.biu.contact.entity.AddRequestUserInfo;
import com.biu.biu.contact.entity.ContactListNetBean;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by fubo on 2016/6/9 0009.
 * email:bofu1993@163.com
 */
public interface ContactApiService {

  @GET("/biu_friend_list")
  Observable<ContactListNetBean> queryContactList(@Query("jm_id") String userId);


  @FormUrlEncoded
  @POST("/accept_friend_request")
  Call<ResponseBody> acceptFriendRequest(
      @Field("requester") String requester, @Field("receiver") String receiver);

  @GET("/friend_request_list")
  Observable<List<AddRequestUserInfo>> getAddContactList(@Query("jm_id") String jmId);

  @FormUrlEncoded
  @POST("/refuse_friend_request")
  Call<ResponseBody> refuseRequest(
      @Field("requester") String requesterId, @Field("receiver") String receiverId);

  @FormUrlEncoded
  @POST("/delete_friend")
  Call<ResponseBody> deleteFriend(@Field("jm_id") String jmId, @Field("del_jm_id") String delJmId);
}
