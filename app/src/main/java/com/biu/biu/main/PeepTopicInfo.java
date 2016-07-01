package com.biu.biu.main;

public class PeepTopicInfo {
	public String id; // 标题ID
	public String content; // 文本内容
	public int allowimg; // 所在标题是否允许发表图片
	public int isDeleted; // 标记被删除
	public String creat_at; // 创建日期
	public String update_at; // 更新日期
	public boolean isTitle = false; // 是否为主题（新鲜事）
	// 新字段，表示设备与话题的状态关系
	public int status = 0;
}
