package com.biu.biu.main;

/**
 * Created by fubo on 2016/5/7 0007.
 * email:bofu1993@163.com
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.biu.biu.tools.AutoListView;
import com.biu.biu.user.entity.SimpleUserInfo;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.views.base.BaseFragment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import grf.biu.R;

public class HomeFragment extends BaseFragment
    implements AutoListView.OnRefreshListener, AutoListView.OnLoadListener {
  // 在请求帖子数据的时候。第一次时ListVIew中的帖子尺寸为10，之后帖子的尺寸为30
  // private Boolean firstThreadOrNot = true;
  // 设置一个变量用于控制在添加新的缓冲数据后ListView是否可以刷新
  private int nextPageSize = 0;
  // 定义一个变量用于标识是显示兴趣点的首页还是显示位置首页
  private Boolean localOrnot = true;
  private AutoListView mHomeListView;
  private final int FIRST_PAGE_GET_OK = 1;
  private final int FIRST_PAGE_GET_ERROR = 2;
  private final int NEXT_PAGE_GET_OK = 3; // 得到下一页成功
  private final int NEXT_PAGE_GET_ERROR = 4; // 或许下一页信息失败
  private final int HOME_LISTVIEW_REFRESH = 5; // 刷新页面
  private final int HOME_LISTVIEW_LOADMORE = 6; // 加载更多
  public final int UPDATE_HOMELISTVIEW = 7; // 更新ListView
  private final int GETLAT_LNGFAILURE = 8; // 获得定位失败
  // private String latitude = "38.00"; // 维度 38
  // private String longitude = "125.00"; // 经度 125

  private String url = "http://api.bbbiu.com:1234/first-page";
  // private String mMsgErrorDesc;
  // private JSONObject jsonObject;
  // private HomeTopicInfo mHomeTopicInfo = new HomeTopicInfo();
  private ArrayList<TipItemInfo> mHomeListItems = new ArrayList<TipItemInfo>();
  private JSONArray jsonArray = null; // 存储从网络中获得的数据
  private HomeHandler homeHandler;
  private HomeListAdapter listViewAdapter = null;
  // private boolean mlatlnghasgetted = false;
  private int mtipsGettedCount = 0; // 已经得到的帖子数量，用于为跳过赋值
  private ArrayList<TipItemInfo> firstlistItemsBuffer = new ArrayList<TipItemInfo>();
  private ArrayList<TipItemInfo> nextlistItemsBuffer = new ArrayList<TipItemInfo>();
  private boolean usefirstBuffer = true;
  private boolean mthreadisrunning = false;
  private boolean mfirstRefresh = true;
  private TextView mMoreHottv = null;
  private FloatingActionButton floatingActionButton;
  RelativeLayout noPublishLayout;

  // 设置一个变量也用识别是否是在刷新 使得nextlistItemsBuffer清空
  private boolean ctrlRefresh = false;
  // 专门用于定时刷新的Handler
  private Handler refreshHandler = new Handler() {
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case 1:
          onRefresh();
          break;

        default:
          break;
      }
    }

    ;
  };
  // 定义一个计时器
  private Timer refreshTimer;

  public static HomeFragment getInstance(Boolean localOrNot) {
    HomeFragment homeFragment = new HomeFragment();
    Bundle args = new Bundle();
    homeFragment.localOrnot = localOrNot;
    args.putBoolean("LOCAL_OR_NOT", localOrNot);
    homeFragment.setArguments(args);
    return homeFragment;
  }

  // 构造函数

	/*public HomeFragment(Boolean localOrnot) {
        this.localOrnot = localOrnot;
	}*/

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    homeHandler = new HomeHandler();
    mfirstRefresh = true;
    listViewAdapter.setHandler(homeHandler);
    super.onActivityCreated(savedInstanceState);
  }

  public HomeFragment() {
    super();
  }

  @Override
  public void onResume() {
    // TODO Auto-generated method stub
    // 获取首页帖子信息
    if (mfirstRefresh) { // 第一次启动时自动获取首页帖子信息
      // firstThreadOrNot = true;
      mHomeListView.getbValues();
      getFirstPageFromServer();
      mfirstRefresh = false;
    } else {
      // 如果不是第一次启动，但是在发表新帖之后，也进行一次获取首页刷新。
      if (UserConfigParams.isHomeRefresh) {
        mHomeListView.setPageSize(10);
        mHomeListView.getbValues();
        this.resetAllValues();
        UserConfigParams.isHomeRefresh = false;
        getFirstPageFromServer();
        mHomeListView.smoothScrollToPosition(0);
      }
    }
    super.onResume();
  }

  // 重置所有的变量
  private void resetAllValues() {
    firstlistItemsBuffer.clear();
    nextlistItemsBuffer.clear();
    mtipsGettedCount = 0;
    nextPageSize = 0;
    mtipsGettedCount = 0;
  }

  /**
   * 从服务器中获得首页的帖子
   */
  private void getFirstPageFromServer() {
    // TODO Auto-generated method stub
    // Toast.makeText(getActivity(), "开始获取首页信息", Toast.LENGTH_SHORT).show();
    mHomeListView.getbValues();
    GetFirstPageThread firstPageThread = new GetFirstPageThread(homeHandler);
    Thread thread = new Thread(firstPageThread);
    thread.start();
  }

  /**
   * 创建视图回调函数 在这个函数中，为Fragment对象创建View对象
   */
  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreateView(inflater, container, savedInstanceState);
    View homeView = inflater.inflate(R.layout.activity_tab_home, container, false);
    noPublishLayout = (RelativeLayout) homeView.findViewById(R.id.rl_nothing_layout);
    return homeView;
  }

  /**
   * 在onCreateView返回之后立刻调用。在任何存储的状态被恢复到View之前调用
   */
  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onViewCreated(view, savedInstanceState);
    floatingActionButton = (FloatingActionButton) (getActivity().findViewById(R.id.fab_add));
    mHomeListView = (AutoListView) (getActivity().findViewById(R.id.home_showtopic));
    mHomeListView.setHeaderDividersEnabled(false);
    mHomeListView.setFooterDividersEnabled(false);
    listViewAdapter = new HomeListAdapter(this.getActivity(), mHomeListItems);// 第一次实例化
    listViewAdapter.setActivity(this.getActivity());
    listViewAdapter.setListView(mHomeListView);
    mHomeListView.setAdapter(listViewAdapter);
    mHomeListView.setOnRefreshListener(this);
    mHomeListView.setOnLoadListener(this);
    mMoreHottv = (TextView) getActivity().findViewById(R.id.morehot);
    // homeHandler = new HomeHandler(); // 在onActivityCreated中进行此操作
    mtipsGettedCount = 0; // 每次创建视图时，归零已经得到的帖子数
    floatingActionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), PublishTopicActivity.class);
        intent.putExtra("PublishMode", PublishTopicActivity.PUBLISH_FOR_HOMETIP);
        getActivity().startActivity(intent);
      }
    });
    mHomeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent();
        intent.setClass(getActivity(), PeepDetailActivity.class);
        intent.putExtra("thread_id", mHomeListItems.get(position - 1).id);
        intent.putExtra("DetailMode",
            PeepDetailActivity.TIPDETAIL_FOR_HOMETIP);
        getActivity().startActivity(intent);
      }
    });
    listViewAdapter.setTopicNumberListener(new HomeListAdapter.TopicNumberListener() {
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
   * 线程可以通过传入的上下文调用此函数最终获得Handler对象。
   *
   * @return
   */
  public Handler getHandler() {
    return this.homeHandler;
  }

	/*
     * 得到自定义的HttpClient
	 */
  // private HttpClient getHttpClient() {
  // // TODO Auto-generated method stub
  // HttpParams mHttpParams = new BasicHttpParams();
  // // 即:Set the timeout in milliseconds until a connection is established.
  // HttpConnectionParams.setConnectionTimeout(mHttpParams, 20 * 1000);
  // // 即:in milliseconds which is the timeout for waiting for data.
  // HttpConnectionParams.setSoTimeout(mHttpParams, 20 * 1000);
  // // 设置socket缓存大小
  // HttpConnectionParams.setSocketBufferSize(mHttpParams, 8 * 1024);
  // // 设置是否可以重定向
  // HttpClientParams.setRedirecting(mHttpParams, true);
  // HttpClient httpClient = new DefaultHttpClient(mHttpParams);
  // return httpClient;
  // }

  /**
   * home页的自定义Handler
   *
   * @author grf
   */
  class HomeHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
      // TODO Auto-generated method stub
      int nMsgNo = msg.what; // what存储消息编号
      switch (nMsgNo) {
        case FIRST_PAGE_GET_OK:
          // 首页消息，将得到的服务器数据 解析并添加到ListView
          usefirstBuffer = true;
          DealTipListInfo();
          // 将缓冲数据初始化到ListView
          fillBufferonFirstPage(); // 填充首页数据
          // 启动线程，从服务器获得下一页数据并存入缓存
          // getNextPageFromServer();
          mHomeListView.onLoad();
          break;
        case FIRST_PAGE_GET_ERROR:
          usefirstBuffer = true;
          mHomeListView.onRefreshComplete();
          showShortToast("获取首页信息失败，请检查网络连接！");

          break;
        case NEXT_PAGE_GET_OK:
          usefirstBuffer = false;
          DealTipListInfo();
          // 将缓冲数据初始化到ListView
          addNextBuffertoListview();
          // 启动线程，从服务器获得下一页数据并存入缓存
          // getNextPageFromServer(); // 这样会造成不断的启动线程执行操作。
          break;
        case NEXT_PAGE_GET_ERROR:
          mHomeListView.onLoadComplete();
          showShortToast("获取分页信息失败，请检查网络连接！");
          break;
        // 首页刷新
        case HOME_LISTVIEW_REFRESH:
          // 下拉刷新
          usefirstBuffer = true;
          DealTipListInfo();
          mHomeListView.onRefreshComplete();
          fillBufferonFirstPage(); // 填充首页信息
          mHomeListView.onLoad(); // 获取首页信息后，马上再加载一次。（由于获取首页信息与分页接口不同，故第一次分页必须手动触发）
          // 此处预备缓冲数据
          break;
        // 加载分页数据
        case HOME_LISTVIEW_LOADMORE:
          // 此处的作用就是添加新的缓冲下一页的数据
          usefirstBuffer = false;
          mHomeListView.onLoadComplete();
          // addNextBuffertoListview();
          // 添加缓冲
          DealTipListInfo();
          break;
        case UPDATE_HOMELISTVIEW:
          // 更新ListView
          getFirstPageFromServer();
          break;
        // case LOCATE_MOREHOT_READY:
        // // 配置更多热帖位置
        // locateMoreHot();
        // break;
        case GETLAT_LNGFAILURE:
          mHomeListView.onRefreshComplete();
          showShortToast("获取高德定位信息失败，请检查网络连接！");
          break;
      }

      super.handleMessage(msg);
    }

  }

  // 线程，从服务器那边获取首页信息
  class GetFirstPageThread implements Runnable {
    private Handler mhandler = null; // 上下文信息，用于获得activity
    private int ncount = 1;

    public GetFirstPageThread(Handler arg0) {
      this.mhandler = arg0;
    }

    @Override
    public void run() {
      // TODO Auto-generated method stub
      ncount = 1;
      while (!UserConfigParams.hasGettedLocation()) { // 没得到经纬度就一直等着
        // Toast.makeText(getActivity(), "等待高德经纬度",
        // Toast.LENGTH_SHORT).show();
        try {
          Thread.sleep(200);
          ncount++;
          if (ncount > 50) {
            // Toast.makeText(getActivity(), "获取位置信息超时",
            // Toast.LENGTH_SHORT).show();
            Message msg = Message.obtain();
            msg.what = GETLAT_LNGFAILURE;
            // errordesc = "获取位置信息超时！";
            // 通过Handler发布传送消息，handler
            this.mhandler.sendMessage(msg);
            return;
          }

        } catch (InterruptedException e) {
          e.printStackTrace();
          return;
        }
      }
      String getfirstpageurl;
      if (localOrnot) {
        getfirstpageurl = url
            + ("?" + "device_id=" + UserConfigParams.device_id
            + "&lat=" + UserConfigParams.latitude + "&lng=" + UserConfigParams.longitude);
        Log.i("测试兴趣点首页-------", "true");
      } else {
        getfirstpageurl = url
            + ("?" + "device_id=" + UserConfigParams.device_id
            + "&lat=" + UserConfigParams.poiLat + "&lng=" + UserConfigParams.poiLng);
        Log.i("测试兴趣点首页-------", "false");
      }
      // 检查url的有效性
      if ("".equals(getfirstpageurl) || getfirstpageurl == null) {
        return;
      }

      HttpClient httpClient = new DefaultHttpClient();

      StringBuilder urlStringBuilder = new StringBuilder(getfirstpageurl);
      StringBuilder entityStringBuilder = new StringBuilder();
      // 利用URL生成一个HttpGet请求
      HttpGet httpGet = new HttpGet(urlStringBuilder.toString());
      httpGet.setHeader("Content-Type",
          "application/x-www-form-urlencoded; charset=utf-8");
      BufferedReader bufferedReader = null;
      HttpResponse httpResponse = null;

      try {
        // HttpClient发出一个HttpGet请求
        // Toast.makeText(getActivity(), "开始执行请求操作",
        // Toast.LENGTH_SHORT).show();
        httpResponse = httpClient.execute(httpGet);
      } catch (Exception e) {
        Message msg = Message.obtain();
        msg.what = FIRST_PAGE_GET_ERROR;
        // errordesc = e.getMessage() + "执行请求错误";
        // 通过Handler发布传送消息，handler
        this.mhandler.sendMessage(msg);
        return;
      }
      // 得到httpResponse的状态响应码

      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        // 得到httpResponse的实体数据
        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity != null) {
          try {
            bufferedReader = new BufferedReader(
                new InputStreamReader(httpEntity.getContent(),
                    "UTF-8"), 8 * 1024);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
              entityStringBuilder.append(line + "/n");
            }
            // 利用从HttpEntity中得到的String生成JsonObject
            jsonArray = new JSONArray(
                entityStringBuilder.toString());
            // 得到了首页数据，传递消息，进行解析并显示
            Message msg = Message.obtain();
            msg.what = HOME_LISTVIEW_REFRESH;
            // 通过Handler发布传送消息，handler
            this.mhandler.sendMessage(msg);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } else if (statusCode == HttpStatus.SC_BAD_REQUEST) {
        Message msg = Message.obtain();
        msg.what = FIRST_PAGE_GET_ERROR;
        // errordesc = "服务器错误";
        this.mhandler.sendMessage(msg);
      }
    }

  }

  // 线程，从服务器那边获取下一页信息
  class GetNexPageThread implements Runnable {
    private Handler mhandler = null; // 上下文信息，用于获得activity

    public GetNexPageThread(Handler arg0) {
      this.mhandler = arg0;
    }

    @Override
    public void run() {
      // TODO Auto-generated method stub
      if (mthreadisrunning) {
        return;
      }
      mthreadisrunning = true;
      String nextpageurl = "http://api.bbbiu.com:1234/threads";

      // 改成30
      // 此处在请求下页数据的时候要减去三条热帖的数量跳过的条数
      int offsetNum = mtipsGettedCount;
      // nextpageurl = nextpageurl + "?lat=" + UserConfigParams.latitude
      // + "&lng=" + UserConfigParams.longitude + "&offset="
      // + (Integer.valueOf(offsetNum).toString()) + "&limit=30"
      // + "&device_id=" + UserConfigParams.device_id;
      if (localOrnot) {
        Log.i("帖子偏移的数值---------》》》》", String.valueOf(mtipsGettedCount));
        Log.i("视图的界限尺寸---------》》》》",
            String.valueOf(mHomeListView.getPageSize()));
        nextpageurl = nextpageurl + "?lat=" + UserConfigParams.latitude
            + "&lng=" + UserConfigParams.longitude + "&offset="
            + (Integer.valueOf(offsetNum).toString()) + "&limit=30"
            + "&device_id=" + UserConfigParams.device_id;
      } else {
        Log.i("帖子偏移的数值---------》》》》", String.valueOf(mtipsGettedCount));
        nextpageurl = nextpageurl + "?lat=" + UserConfigParams.poiLat
            + "&lng=" + UserConfigParams.poiLng + "&offset="
            + (Integer.valueOf(offsetNum).toString()) + "&limit=30"
            + "&device_id=" + UserConfigParams.device_id;
      }

      HttpClient httpClient = new DefaultHttpClient();

      StringBuilder urlStringBuilder = new StringBuilder(nextpageurl);
      StringBuilder entityStringBuilder = new StringBuilder();
      // 利用URL生成一个HttpGet请求
      HttpGet httpGet = new HttpGet(urlStringBuilder.toString());
      httpGet.setHeader("Content-Type",
          "application/x-www-form-urlencoded; charset=utf-8");
      BufferedReader bufferedReader = null;
      HttpResponse httpResponse = null;

      try {
        // HttpClient发出一个HttpGet请求
        httpResponse = httpClient.execute(httpGet);
      } catch (UnknownHostException e) {
        // 无法连接到主机
        Message msg = Message.obtain();
        msg.what = NEXT_PAGE_GET_ERROR;
        // 通过Handler发布传送消息，handler
        this.mhandler.sendMessage(msg);
        return;
      } catch (Exception e) {
        e.printStackTrace();
        return;
      }
      // 得到httpResponse的状态响应码
      int statusCode = httpResponse.getStatusLine().getStatusCode();

      if (statusCode == HttpStatus.SC_OK) {
        // 得到httpResponse的实体数据
        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity != null) {
          try {
            bufferedReader = new BufferedReader(
                new InputStreamReader(httpEntity.getContent(),
                    "UTF-8"), 8 * 1024);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
              entityStringBuilder.append(line + "/n");
            }
            // 利用从HttpEntity中得到的String生成JsonObject
            jsonArray = new JSONArray(
                entityStringBuilder.toString());
            // 得到了首页数据，传递消息，进行解析并显示
            Message msg = Message.obtain();
            msg.what = HOME_LISTVIEW_LOADMORE;
            // 通过Handler发布传送消息，handler
            this.mhandler.sendMessage(msg);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } else {
        // 获取数据错误
        Message msg = Message.obtain();
        msg.what = NEXT_PAGE_GET_ERROR;
        this.mhandler.sendMessage(msg);
      }
      mthreadisrunning = false;
    }
  }

  /**
   * 将得到的网络数据存入缓存
   */
  public void DealTipListInfo() {
    // TODO Auto-generated method stub
    ArrayList<TipItemInfo> tempBuffer;
    if (usefirstBuffer) {
      tempBuffer = firstlistItemsBuffer;
    } else {
      // 添加缓冲
      tempBuffer = nextlistItemsBuffer;
    }
    try {
      tempBuffer.clear(); // 清除缓存
      // ArrayList<Integer> topDownFlags = new ArrayList<Integer>();
      MyDateTimeDeal timedeal = new MyDateTimeDeal();
      for (int i = 0; i < jsonArray.length(); i++) {
        // i=3时添加更多热帖项目
        // HashMap<String, Object> mapMoreHot = new HashMap<String,
        // Object>();
        // if(i == 3){
        // HashMap<String, Object> morehot = new HashMap<String,
        // Object>();
        // tempBuffer.add(morehot);
        // }

        JSONObject everyJsonObject = jsonArray.getJSONObject(i);
        TipItemInfo item = new TipItemInfo();
        item.content = everyJsonObject.getString("content");
        item.created_at = timedeal.getTimeGapDesc(everyJsonObject
            .getString("created_at"));
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
        SimpleUserInfo simpleUserInfo = new SimpleUserInfo();
        if (everyJsonObject.isNull("anony")) {
          item.anony = 1;
        } else {
          item.anony = Integer.parseInt(everyJsonObject.getString("anony"));
        }
        if (!everyJsonObject.isNull("publisher") && everyJsonObject.get("publisher") != null) {
          JSONObject userJson = everyJsonObject.getJSONObject("publisher");
          simpleUserInfo.setJm_id(userJson.getString("jm_id"));
          simpleUserInfo.setDevice_id(userJson.getString("jm_id"));
          simpleUserInfo.setNickname(userJson.getString("nickname"));
          simpleUserInfo.setIcon_small(userJson.getString("icon_small"));
          simpleUserInfo.setIcon_large(userJson.getString("icon_small"));
        }
        item.simpleUserInfo = simpleUserInfo;
        // if (usefirstBuffer && i == 2)
        // item.isDisplayMore = true;
        tempBuffer.add(item);
      }
      nextPageSize = jsonArray.length();
      // 在具体的fill操作中进行
      // if(!usefirstBuffer)
      // mtipsGettedCount += tempBuffer.size(); // 加载更多
      // else
      // mtipsGettedCount = 0; // 下拉刷新则归零
      // listViewAdapter.setTopOrDownStateArray(topDownFlags);
      // listViewAdapter.notifyDataSetChanged();
    } catch (Exception e) {
      e.printStackTrace();
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

  /**
   * 将下一页数据连接到当前页面，同时再次获得下一页数据。
   */
  public void addNextBuffertoListview() {
    // TODO Auto-generated method stub
    ArrayList<Integer> topdowntemp = listViewAdapter
        .getLikeTreadSaveBuffer();
    ArrayList<TipItemInfo> listtempitems = listViewAdapter.getListItems();
    for (int i = 0; i < nextlistItemsBuffer.size(); i++) {
      topdowntemp.add(0);
    }

    // 下一条子语句用于控制是不是能不能下拉刷新(因为存在缓冲数据，因而这边的判别不能放在此处)
    // mHomeListView.setResultSize(nextlistItemsBuffer.size());
    if (nextlistItemsBuffer.size() > 0) {
      nextPageSize = nextlistItemsBuffer.size();
    } else {
      nextPageSize = 0;
    }

    listtempitems.addAll(nextlistItemsBuffer);
    mtipsGettedCount += nextlistItemsBuffer.size();
    Log.i("帖子偏移的数值---------》》》》", String.valueOf(mtipsGettedCount));
    nextlistItemsBuffer.clear();
    listViewAdapter.notifyDataSetChanged();
  }

  /**
   * 启动线程，得到下一页数据
   */
  public void getNextPageFromServer() {
    // TODO Auto-generated method stub
    // 如果线程正在运行，则放弃此次操作
    if (mthreadisrunning) {
      return;
    }
    // 若未获得经纬度
    if (!UserConfigParams.hasGettedLocation()) {
      mHomeListView.onLoadComplete();
      return;
    }

    GetNexPageThread getnextpagethread = new GetNexPageThread(homeHandler);
    Thread thread = new Thread(getnextpagethread);
    thread.start();
  }

  // 重置各个变量 恢复Wie最初值
  private void backToOrginal() {

  }

  /**
   * 将缓存中的数据填充入首页
   */
  public void fillBufferonFirstPage() {
    // 每次先将AutiListView中的pageSize大小改为10
    mHomeListView.setPageSize(10);
    mHomeListView.setResultSize(firstlistItemsBuffer.size());
    Log.i("视图的界限尺寸---------》》》》",
        String.valueOf(mHomeListView.getPageSize()));
    // TODO Auto-generated method stub
    ArrayList<TipItemInfo> listtempitems = listViewAdapter.getListItems();
    listtempitems.clear();
    ArrayList<Integer> topdowntemp = listViewAdapter
        .getLikeTreadSaveBuffer();
    topdowntemp.clear();
    // for(int i = 0; i < firstlistItemsBuffer.size(); i++)
    // topdowntemp.add(0);
    // if (firstlistItemsBuffer.size() > 3) {
    // listtempitems.addAll(firstlistItemsBuffer.subList(3,
    // firstlistItemsBuffer.size()));
    // }
    // 恢复删掉的三条帖子记录
    listtempitems.addAll(firstlistItemsBuffer);
    mtipsGettedCount = listtempitems.size();
    // mtipsGettedCount = 0; // 首页信息时将获取帖子数归零
    if (listtempitems.isEmpty()) {
      // 添加一个空项提示
      TipItemInfo tipempty = new TipItemInfo();
      tipempty.isEmpty = true;
      listtempitems.add(tipempty);
      mHomeListView.setResultSize(0);
    }
    firstlistItemsBuffer.clear();
    // mHomeListView.setStateLoadMore(); // 无论如何，加载首页时，令ListView可以继续添加
    // mHomeListView.setResultSize(listtempitems.size());

    listViewAdapter.notifyDataSetChanged();
  }

  @Override
  public void onRefresh() {
    // TODO Auto-generated method stub
    // Message msg = homeHandler.obtainMessage();
    // msg.what = HOME_LISTVIEW_REFRESH;
    // // 发送刷新消息
    if (!UserConfigParams.hasGettedLocation()) {
      mHomeListView.onRefreshComplete();
      Toast.makeText(getActivity(), "获取定位信息失败！", Toast.LENGTH_SHORT)
          .show();
      return;
    }
    mHomeListView.setPageSize(10);
    // Message msg = homeHandler.obtainMessage();
    // msg.what = HOME_LISTVIEW_REFRESH;
    // msg.obj = getData();
    // homeHandler.sendMessage(msg);
    // 触发刷新操作，开始执行线程，线程执行成功后，发送刷新操作
    Log.i("HCTEST", "刷新数据！！！");
    // 刷新以后要将各个变量参数重置，不然会出现问题
    //firstlistItemsBuffer.clear();
    nextlistItemsBuffer.clear();
    mtipsGettedCount = 0;
    nextPageSize = 0;
    // AutoListView的适配器中的数据也应该清空(好像又不需要了)
    listViewAdapter.getListItems().clear();
    ctrlRefresh = true;
    mfirstRefresh = true;
    getFirstPageFromServer();
    mfirstRefresh = false;
  }

  // 若nextlistItemsBuffer存在缓冲数据在更新autolistview,并且请求下一个缓冲数据
  @Override
  public void onLoad() {
    // TODO Auto-generated method stub
    if (ctrlRefresh) {
      nextlistItemsBuffer.clear();
      ctrlRefresh = false;
    }

    // 判定nextlistItemsBuffer是不是为空 不为空则使用缓冲数据进行listview 更新
    if (nextlistItemsBuffer != null && nextlistItemsBuffer.size() > 0) {
      // mHomeListView.onLoadComplete();
      addNextBuffertoListview();
      // 控制加载缓冲数据到视图后能不能再进行刷新操作
      mHomeListView.setPageSize(30);
      mHomeListView.setResultSize(nextPageSize);
      // 将缓冲的数据添加进来以后再进行判定页面能不能刷新
      // 添加完将nextlistItemsBuffer清空
      nextlistItemsBuffer.clear();
      // 控制View是不是能够上滑刷新
    }

    Log.i("HCTEST", "不知道是不是缓冲有用个地方");
    getNextPageFromServer();
  }

  // 重复固定时间间隔刷新首页
  public void refreshByTimer() {
    refreshTimer = new Timer();
    Log.i("TIMER", "自动刷新");
    refreshTimer.scheduleAtFixedRate(new TimerTask() {

      @Override
      public void run() {
        // TODO Auto-generated method stub
        Message message = new Message();
        message.what = 1;
        // 进行刷新
        refreshHandler.sendMessage(message);
      }
    }, 15 * 1000, 3 * 1000);
  }

  public void stopRefreshByTimer() {
    refreshTimer.cancel();
  }

  public void setLatLng() {
    UserConfigParams.latitude = "20";
    UserConfigParams.longitude = "122";
  }
}
