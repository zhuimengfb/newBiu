package com.biu.biu.user.model;

import com.biu.biu.user.entity.ShowUserInfoBean;
import com.biu.biu.user.entity.SimpleUserInfo;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by fubo on 2016/6/4 0004.
 * email:bofu1993@163.com
 */
public interface IUserService {

  @GET("/myfigure/{device_id}")
  Observable<SimpleUserInfo> getMyInfo(@Path("device_id") String deviceId);

  @GET("/biuer_large_icon/{jm_id}")
  Observable<String> getIconAddress(@Path("jm_id") String jMessageId);

  @FormUrlEncoded
  @POST("/rename_nickname ")
  Call<ResponseBody> renameNickName(
      @Field("device_id") String deviceId, @Field("nickname") String nickName);

  @FormUrlEncoded
  @POST("/post_jm_id")
  Call<ResponseBody> postJMessageId(
      @Field("device_id") String deviceId, @Field("jm_id") String jMessageId);

  @Multipart
  @POST("/change_figure")
  Call<ResponseBody> modifyHeadIcon(@Part MultipartBody.Part deviceId,
      @Part MultipartBody.Part file);

  @Multipart
  @POST("/photoalbum")
  Call<ResponseBody> uploadPhoto(@Part MultipartBody.Part deviceId, @Part MultipartBody.Part file);

  //TODO 附加字段待确认
  @FormUrlEncoded
  @POST("/friend_request")
  Call<ResponseBody> requestFriend(
      @Field("requester") String requesterId,
      @Field("receiver") String receiverId, @Field("description") String message);


  @GET("/biuer_detail")
  Observable<ShowUserInfoBean> queryUserInfo(
      @Query("jm_id") String jmId, @Query("other_jm_id") String otherJmId);


  @FormUrlEncoded
  @POST("/del_single_img")
  Call<ResponseBody> deletePhoto(@Field("jm_id") String jmId, @Field("number") int sequence);
}
