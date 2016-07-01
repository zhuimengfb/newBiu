package com.biu.biu.main;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.SimpleAdapter;

/*-
 * 存储Home页主题中的信息
 */
@Deprecated
public class HomeTopicInfo implements Parcelable{
	public static String m_subItemName[]={
		"itemContent", "publicTime", "replayCount", "upCount", "downCount", "likeState"};
	private ArrayList<HashMap<String, Object>> mHomeTopicItem = new ArrayList<HashMap<String, Object>>();
	/*-
	 * 构造函数
	 */
	public HomeTopicInfo(){
//		addDefaultData();
	}
	
	/*-
	 * 添加测试数据。
	 */
	public void addDefaultData(){
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("itemContent", "我想变成一棵树，孤独地站在道路的一旁，就没人注意到自己的存在，曾经总是相信生活充满着奇迹，让自己满怀信心，去微笑每一个烂漫的日子。");
		map.put("publicTime", "3分钟前");
		map.put("replayCount", "9");
		map.put("upCount", "123");
		map.put("downCount", "123");
		mHomeTopicItem.add(map);
		map = new HashMap<String, Object>();
		map.put("itemContent", "有些事情累了，倦了，想说却不说，不想说也不说，说了也白说，还不如不说。那就“沉默是金”吧。");
		map.put("publicTime", "1小时前");
		map.put("replayCount", "0");
		map.put("upCount", "69");
		map.put("downCount", "48");
		mHomeTopicItem.add(map);
		map = new HashMap<String, Object>();
		map.put("itemContent", "阳光沐浴在我们身上一样，每一个毛孔都张开。");
		map.put("publicTime", "3小时前");
		map.put("replayCount", "18");
		map.put("upCount", "123");
		map.put("downCount", "123");
		
		mHomeTopicItem.add(map);
		map = new HashMap<String, Object>();
		map.put("itemContent", "静静地漫步在深夜的街道上，初秋的深夜冰冷刺骨，突然想起一过往，却又怎么也想不起是什么，走在这熟悉的陌生城市里，看着眼前风景一段一段的过去。");
		map.put("publicTime", "2015-02-19");
		map.put("replayCount", "0");
		map.put("upCount", "0");
		map.put("downCount", "1");
		mHomeTopicItem.add(map);
	}
	
	/*-
	 * 获得存储的数据信息
	 */
	public ArrayList<HashMap<String, Object>> getListItem(){
		return mHomeTopicItem;
	}
	
	/*-
	 * 返回指定子项名称的字符串数组
	 */
	public String[] getSubItemNameArray(){
		return m_subItemName;
	}

	public static final Creator<HomeTopicInfo> CREATOR = new Creator<HomeTopicInfo>(){

		
		@Override
		public HomeTopicInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			 HomeTopicInfo mTopicInfo = new HomeTopicInfo();  
			 source.readStringArray(mTopicInfo.m_subItemName);  
//			 source.readList(mTopicInfo.mHomeTopicItem, loader);  
//			 mTopicInfo.publishTime = source.readInt();  
	            return mTopicInfo; 
//			return null;
		}

		@Override
		public HomeTopicInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeStringArray(m_subItemName);	// 写入子项的名称数组
		dest.writeList(mHomeTopicItem);			// 写入Item内容信息
		
	}
}
