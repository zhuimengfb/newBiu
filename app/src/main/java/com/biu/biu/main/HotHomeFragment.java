package com.biu.biu.main;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.biu.biu.morehottips.GetMoreHotThread;
import com.biu.biu.morehottips.MoreHotListViewAdapter;
import com.biu.biu.tools.AutoListView;
import com.biu.biu.tools.AutoListView.OnLoadListener;
import com.biu.biu.tools.AutoListView.OnRefreshListener;
import com.biu.biu.user.entity.SimpleUserInfo;
import com.biu.biu.userconfig.UserConfigParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import grf.biu.R;

public class HotHomeFragment extends Fragment implements OnRefreshListener,
                                                         OnLoadListener {

  final int AIRPLAY_MESSAGE_HIDE_TOAST = 2;
  // final int MORE_HOT_REFRESH = 3;
  private AutoListView mlistView;
  private MoreHotListViewAdapter mlistViewAdapter = null;
  // private TextView mtabToptv = null; // Tab标题
  private ArrayList<TipItemInfo> mlistItemsinfo; // 帖子内容信息
  private Handler myHandler = null;
  private Toast mToast; // 自定义提示文本显示时间
  private GetMoreHotThread mGetThread;
  private JSONArray mjsonArray;
  private int mOffset = 0;
  private boolean mfirstRefreshMore; // 只在首次启动时自动刷新一次
  private boolean localornot = true;
  RelativeLayout noPublishLayout;

  // @Override
  // protected void onCreate(Bundle savedInstanceState) {
  // super.onCreate(savedInstanceState);
  // setContentView(R.layout.activity_more_hot);
  // localornot = getIntent().getBooleanExtra("localornot", true);
  // initHandler();
  // findViewId();
  // initView();
  // mfirstRefreshMore = true;
  // Log.i("localornotlocalornot", String.valueOf(localornot));
  // this.initLatLng();
  //
  // }

  public static HotHomeFragment getInstance(Boolean localOrNot) {
    HotHomeFragment hotHomeFragment = new HotHomeFragment();
    Bundle args = new Bundle();
    hotHomeFragment.localornot = localOrNot;
    args.putBoolean("LOCAL_OR_NOT", localOrNot);
    hotHomeFragment.setArguments(args);
    return hotHomeFragment;
  }

	/*public HotHomeFragment(Boolean localornot) {
        this.localornot = localornot;
	}*/

  public HotHomeFragment() {
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onActivityCreated(savedInstanceState);
    initHandler();
    mfirstRefreshMore = true;
  }

  private void initHandler() {
    // TODO Auto-generated method stub
    myHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        // TODO Auto-generated method stub
        switch (msg.what) {
          case 0:
            mlistView.onRefreshComplete();
            // 操作执行成功
            try {
              if (mGetThread != null && mGetThread.getHttpResult() != null) {
                mjsonArray = new JSONArray(mGetThread.getHttpResult());
                parseHttpResult();
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }

            mlistView.onLoadComplete();
            break;
          case -1:
            // 操作执行失败
            String errorMsg = msg.getData().getString("e_msg");
            showToast(errorMsg, 1500); // 显示1.5秒
            break;
          case AIRPLAY_MESSAGE_HIDE_TOAST:
            cancelToast();
            break;
          // case MORE_HOT_REFRESH:
          // 下拉刷新

        }
      }

      // 转化帖子信息情况并更新view
      private void parseHttpResult() {
        // TODO Auto-generated method stub
        try {
          // ArrayList<Integer> topDownFlags = new
          // ArrayList<Integer>();
          MyDateTimeDeal timedeal = new MyDateTimeDeal();
          if (mOffset == 0) {
            mlistItemsinfo.clear();
          }
          for (int i = 0; i < mjsonArray.length(); i++) {
            JSONObject everyJsonObject = mjsonArray.getJSONObject(i);
            TipItemInfo item = new TipItemInfo();
            item.content = everyJsonObject.getString("content");
            item.created_at = timedeal.getTimeGapDesc(everyJsonObject.getString("created_at"));
            item.device_id = everyJsonObject.getString("device_id");
            item.id = everyJsonObject.getString("id");
            item.lat = everyJsonObject.getString("lat");
            Integer likeresult = Integer.parseInt(everyJsonObject.getString("like_num"))
                - Integer.parseInt(everyJsonObject.getString("tread_num"));
            item.like_num = likeresult.toString();
            item.lng = everyJsonObject.getString("lng");
            item.reply_num = everyJsonObject.getString("reply_num");
            item.reply_to = everyJsonObject.getString("reply_to");
            item.title = everyJsonObject.getString("title");
            item.imgurl = everyJsonObject.getString("img_url");
            item.tread_num = everyJsonObject.getString("tread_num");
            item.updated_at = everyJsonObject.getString("updated_at");
            item.pubplace = everyJsonObject.getString("address");
            item.hasliked = everyJsonObject.getBoolean("has_liked");
            item.hastreaded = everyJsonObject.getBoolean("has_treaded");
            if (everyJsonObject.isNull("anony")) {
              item.anony = 1;
            } else {
              item.anony = Integer.parseInt(everyJsonObject.getString("anony"));
            }
            SimpleUserInfo simpleUserInfo = new SimpleUserInfo();
            if (!everyJsonObject.isNull("publisher") && everyJsonObject.get("publisher") != null) {
              JSONObject userJson = everyJsonObject.getJSONObject("publisher");
              simpleUserInfo.setJm_id(userJson.getString("jm_id"));
              simpleUserInfo.setDevice_id(userJson.getString("jm_id"));
              simpleUserInfo.setNickname(userJson.getString("nickname"));
              simpleUserInfo.setIcon_small(userJson.getString("icon_small"));
              simpleUserInfo.setIcon_large(userJson.getString("icon_small"));
            }
            item.simpleUserInfo = simpleUserInfo;
            // if(everyJsonObject.getBoolean("has_liked")){
            // item.likestate = 1; // 顶
            // }else if(everyJsonObject.getBoolean("has_treaded")){
            // item.likestate = 2; // 踩
            // }else{
            // item.likestate = 0; // 无
            // }

            mlistItemsinfo.add(item);
          }
          mOffset += mjsonArray.length(); // 更新已获得的帖子数
          if (mlistItemsinfo.isEmpty()) {
            // 添加一个空选项
            TipItemInfo itemempty = new TipItemInfo();
            itemempty.isEmpty = true;
            mlistItemsinfo.add(itemempty);
          }
          mlistView.setResultSize(mjsonArray.length());
          mlistViewAdapter.notifyDataSetChanged();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

    };
  }

  private void findViewId() {
    // TODO Auto-generated method stub
    mlistView = (AutoListView) getActivity().findViewById(
        R.id.morehotlistview);
  }

  @Override
  public void onResume() {
    // TODO Auto-generated method stub
    if (mfirstRefreshMore) { // 第一次启动时自动获取首页帖子信息
      mlistView.onRefreshComplete();
      getMoreHotFromServer(0);
      mfirstRefreshMore = false;

    }
    super.onResume();
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreateView(inflater, container, savedInstanceState);
    View hotView = inflater.inflate(R.layout.fragment_hot_home, container,
        false);
    noPublishLayout = (RelativeLayout) hotView.findViewById(R.id.rl_nothing_layout);
    return hotView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onViewCreated(view, savedInstanceState);

    findViewId();
    initView();

    this.initLatLng();
    mlistViewAdapter.setTopicNumberListener(new MoreHotListViewAdapter.TopicNumberListener() {
      @Override
      public void showNoTopic() {
        showNoPublish();
      }

      @Override
      public void hideNoTopic() {
        hideNoPublish();
      }
    });
  }

  /**
   * 从服务器获取更多热帖
   */
  private String HotLat;
  private String HotLng;

  private void initLatLng() {
    if (localornot) {
      HotLat = UserConfigParams.latitude;
      HotLng = UserConfigParams.longitude;
    } else {
      HotLat = String.valueOf(UserConfigParams.poiLat);
      HotLng = String.valueOf(UserConfigParams.poiLng);
    }

  }

  private void getMoreHotFromServer(int nOffset) {
    // TODO Auto-generated method stub
    if (nOffset == 0) {
      mOffset = 0;
    }
    // 此处一次性请求帖子的数量改为30条
    String url = "http://api.bbbiu.com:1234/hot-threads?lat=" + HotLat
        + "&lng=" + HotLng + "&offset=" + String.valueOf(nOffset)
        + "&limit=" + String.valueOf(30) + "&device_id="
        + UserConfigParams.device_id;
    //		String url = "http://api.bbbiu.com:1234/hot-threads?lat=" + 66
    //				+ "&lng=" + 66 + "&offset=" + String.valueOf(nOffset)
    //				+ "&limit=" + String.valueOf(30) + "&device_id="
    //				+ UserConfigParams.device_id;
    mGetThread = new GetMoreHotThread(myHandler, url);
    Thread thread = new Thread(mGetThread);
    thread.start();
  }

  private void initView() {
    // TODO Auto-generated method stub

    // 初始化TabTop相关描述文字
    // mtabToptv.setText(R.string.title_morehot);

    mlistItemsinfo = new ArrayList<TipItemInfo>();
    mlistViewAdapter = new MoreHotListViewAdapter(getActivity(),
        mlistItemsinfo, myHandler, localornot);
    mlistViewAdapter.setListView(mlistView);
    mlistView.setAdapter(mlistViewAdapter);
    mlistView.setOnRefreshListener(this);
    mlistView.setOnLoadListener(this);
  }

  @Override
  public void onLoad() {
    // TODO Auto-generated method stub
    // 更多热帖暂无加载更多接口，直接结束操作
    getMoreHotFromServer(mOffset);
  }

  @Override
  public void onRefresh() {
    // // TODO Auto-generated method stub
    // Message msg = myHandler.obtainMessage();
    // msg.what = MORE_HOT_REFRESH;
    // myHandler.sendMessage(msg);
    getMoreHotFromServer(0);
  }

  /*
   * 显示Toast
   */
  public void showToast(String text, long showlength) {
    if (mToast == null) {
      mToast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
    } else {
      mToast.setText(text);
      mToast.setDuration(Toast.LENGTH_SHORT);
    }
    mToast.show();

    Message delayMsg = myHandler.obtainMessage(AIRPLAY_MESSAGE_HIDE_TOAST);
    myHandler.sendMessageDelayed(delayMsg, 500);
  }

  // 终止显示Toast文本提示
  public void cancelToast() {
    if (mToast != null) {
      mToast.cancel();
    }
  }

  private void showNoPublish() {
    if (noPublishLayout.getVisibility() != View.VISIBLE) {
      noPublishLayout.setVisibility(View.VISIBLE);
    }
  }

  public void hideNoPublish() {
    if (noPublishLayout.getVisibility() != View.GONE) {
      noPublishLayout.setVisibility(View.GONE);
    }
  }
}