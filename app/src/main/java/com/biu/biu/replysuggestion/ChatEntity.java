/**
 * biu应用程序下反馈建议的页面实现
 */
package com.biu.biu.replysuggestion;

public class ChatEntity {
	private int userImage;
	private String content;
	private String chatTime;
	private boolean isComeMsg;

	public int getUserImage() {
		return userImage;
	}

	public void setUserImage(int userImage) {
		this.userImage = userImage;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getChatTime() {
		return chatTime;
	}

	public void setChatTime(String chatTime) {
		this.chatTime = chatTime;
	}

	public boolean isComeMsg() {
		return isComeMsg;
	}

	public void setComeMsg(boolean isComeMsg) {
		this.isComeMsg = isComeMsg;
	}
}
