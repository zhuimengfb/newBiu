package com.biu.biu.main;

import com.biu.biu.user.entity.SimpleUserInfo;

/**
 * 一个帖子含有的项
 *
 * @author grf
 */
public class TipItemInfo {
  public String content;                // 内容
  public String created_at;            // 发表时间
  public String device_id;            // 设备ID
  public String id;                    // 帖子ID
  public String topic_id;                // 话题ID，用于指定发帖的话题
  public String lat;                    // 纬度
  public String like_num;                // 点赞的数量
  public String lng;                    // 纬度
  public String reply_num;            // 回复数
  public String reply_to;                // null
  public String title;                // 标题
  public String tread_num;            // 回帖数
  public String updated_at;            // 更新时间
  public String pubplace;                // 发帖地址
  public String imgurl = null;        // 图片地址
  public boolean hasliked;            // 顶的状态
  public boolean hastreaded;            // 是否已踩
  public boolean ishot = true;        // 是否为热帖
  public boolean isTitle = false;        // 详情页中的标题页
  public boolean isEmpty = false;        // 是否为空帖子
  public boolean isDisplayMore = false;    // 是否使用更多热帖专用布局
  public int anony = 1;
  public SimpleUserInfo simpleUserInfo;
}
