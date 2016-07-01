package com.biu.biu.main;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.biu.biu.thread.ClearPushNumThread;
import com.biu.biu.tools.AutoListView;
import com.biu.biu.tools.AutoListView.OnLoadListener;
import com.biu.biu.tools.AutoListView.OnRefreshListener;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.views.base.BaseActivity;
import com.umeng.analytics.MobclickAgent;

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
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.badgeview.BGABadgeLinearLayout;
import grf.biu.R;

public class MyPublishActivity extends BaseActivity implements OnRefreshListener,
                                                               OnLoadListener {

  private AutoListView mListView = null;
  // private ArrayList<HashMap<String, Object>> mmyPublishInfo = new
  // ArrayList<HashMap<String, Object>>(); // 存储数据
  private MyPublishHandler myPublishHandler = null;
  private String url = "http://api.bbbiu.com:1234";
  private JSONArray jsonArray = null;
  private final int GET_MYPUBLISH_OK = 0; // 正确返回
  private final int GET_MYPUBLISH_ERROR = - 1; // 返回失败
  private final int NO_MYPUBLISH = 1; // 没有关联的发表列表
  // private SimpleAdapter msimpleAdapter = null; // 我发表的的列表框内容适配器
  // private ArrayList<HashMap<String, Object>> mypublishlistitems = new
  // ArrayList<HashMap<String, Object>>();
  private int mOffset = 0; // 获取已发表贴时跳过的条数
  private boolean misLoadMore = false; // 是否为加载更多，默认为false
  private boolean mfisrtRefresh = true; // 第一次加载页面，加载一次。

  // 定义一个存放我的发表的帖子信息(重定义)
  private List<PublishThread> mypublishlistitems = new ArrayList<PublishThread>();
  // 重新定义适配器
  private MyPublishAdapter publishAdapter = null;

  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.toolbar_title)
  TextView toolbarTitle;
  @BindView(R.id.rl_nothing_layout)
  RelativeLayout nothingLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my_publish);
    ButterKnife.bind(this);
    findViewId();
    initToolbar();
    mfisrtRefresh = true; // 第一次启动
    initView();
    myPublishHandler = new MyPublishHandler();
    mListView.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view,
          int position, long id) {
        // TODO Auto-generated method stub
        int nPosition = position - 1; // 减去header
        if (nPosition >= mypublishlistitems.size()) // 剔除当点击footer时的响应
        {
          return;
        }
        if (mypublishlistitems.get(nPosition).getPush_num() > 0) {
          mfisrtRefresh = true;
          new Thread(new ClearPushNumThread(mypublishlistitems.get(
              nPosition).getThread_id(), myPublishHandler))
              .start();
          mypublishlistitems.get(nPosition).setPush_num(0);
          publishAdapter.notifyDataSetChanged();
        }
        Intent intent = new Intent();
        intent.setClass(MyPublishActivity.this, PeepDetailActivity.class);
        intent.putExtra("thread_id", mypublishlistitems.get(nPosition).getThread_id());
        intent.putExtra("CanDeleteTip", true); // 打开删帖接口
        String strType = mypublishlistitems.get(nPosition).getThread_type();
        if (strType.equals("0")) {
          intent.putExtra("DetailMode", PeepDetailActivity.TIPDETAIL_FOR_HOMETIP);
        } else if (strType.equals("1")) {
          intent.putExtra("DetailMode", PeepDetailActivity.TIPDETAIL_FOR_MOONBOOX);
        } else if (strType.equals("3")) {
          intent.putExtra("DetailMode", PeepDetailActivity.TIPDETAIL_FOR_PEEPTOPIC);
        } else {
          intent.putExtra("DetailMode", PeepDetailActivity.TIPDETAIL_FOR_HOMETIP);
        }
        MyPublishActivity.this.startActivity(intent);
      }

    });
  }

  private void showNoPublish() {
    nothingLayout.setVisibility(View.VISIBLE);
  }
  private void hideNoPublish() {
    nothingLayout.setVisibility(View.GONE);
  }

  private void initToolbar() {
    setSupportActionBar(toolbar);
    setBackableToolbar(toolbar);
    toolbarTitle.setText(R.string.my_publish);
  }

  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
    MobclickAgent.onPause(this);
  }

  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    SharedPreferences preferences = getSharedPreferences("user_Params",
        MODE_PRIVATE);
    UserConfigParams.device_id = preferences.getString("device_ID", "");
    // 启动线程，从服务器获得首页数据
    if (mfisrtRefresh) {
      getMyPublish(false, 0, 10); // 从服务器获取我的发表
      mfisrtRefresh = false;
    } else {
      // 如果不是第一次启动，但是在删除帖子之后，也进行一次获取首页刷新。
      if (UserConfigParams.isHomeRefresh) {
        UserConfigParams.isHomeRefresh = false;
        getMyPublish(false, 0, 10); // 获取我的发表
      }
    }
    MobclickAgent.onResume(this);
  }

  private void getMyPublish(boolean isLoadMore, int noffset, int nlimit) {
    // TODO Auto-generated method stub
    misLoadMore = isLoadMore;
    Thread thread = new Thread(new GetMyPublishTips(myPublishHandler,
        noffset, nlimit)); // 跳过0个帖子，初始获取10
    thread.start();
  }

  /**
   * 根据指定的参数，生成用于GET方法的url字符串
   *
   * @param offset ：跳过帖子数，用于加载更多。
   * @param limit  ：获取信息最大数量
   */
  private String makeUrl(int offset, int limit) {
    // TODO Auto-generated method stub
    String result = null;
    result = String.format(Locale.ENGLISH,
        "%s/devices/%s/threads?want_reply=%d&offset=%d&limit=%d", url,
        UserConfigParams.device_id, 0, offset, limit);

    return result;
  }

  private void initView() {
    // TODO Auto-generated method stub
    // addTestData(); // 添加默认数据
    // 初始化TabTop相关描述文字
    // 重新配置
    // msimpleAdapter = new SimpleAdapter(this, mypublishlistitems,
    // R.layout.mypublishlistitem, new String[] { "pubtime",
    // "pubcontent" }, new int[] { R.id.mypubitemtimetv,
    // R.id.mypubitemcontenttv });
    publishAdapter = new MyPublishAdapter(this, R.layout.mypublishlistitem,
        mypublishlistitems);
    mListView.setAdapter(publishAdapter);
    mListView.setOnRefreshListener(this);
    mListView.setOnLoadListener(this);
    mListView.setPageSize(10); // 设置每次加载10条
  }

  @SuppressWarnings("unused")
  // private void addTestData() {
  // // TODO Auto-generated method stub
  // HashMap<String, Object> map = new HashMap<String, Object>();
  // map.put("pubtime", "我 3分钟前 发表了：");
  // map.put("pubcontent", "我想变成一棵树，孤独地站在道路的一旁，就没人在注意到自己的存在。");
  // mmyPublishInfo.add(map);
  //
  // map = new HashMap<String, Object>();
  // map.put("pubtime", "我 1小时前 发表了：");
  // map.put("pubcontent", "你站在桥上看风景，看风景的人在楼上看你。明月装饰了你的窗子，你装饰了别人的梦。");
  // mmyPublishInfo.add(map);
  // map = new HashMap<String, Object>();
  // map.put("pubtime", "我 2015年03月21日 发表了：");
  // map.put("pubcontent", "一段感情带来多大的伤害，就曾带来多大的欢乐。");
  // mmyPublishInfo.add(map);
  // map = new HashMap<String, Object>();
  // map.put("pubtime", "我 2015年03月31日 发表了：");
  // map.put("pubcontent",
  // "为了过自想要的生活，要勇于放弃一些东西。这个世界没有公正之处。你永远也得不到两全之计。若要...");
  // mmyPublishInfo.add(map);
  //
  // }
  private void findViewId() {
    // TODO Auto-generated method stub
    mListView = (AutoListView) findViewById(R.id.mypublishlistview);
  }

  /*
   * 自定义的Handler
   */
  class MyPublishHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
      // TODO Auto-generated method stub
      int nMsgNo = msg.what;
      switch (nMsgNo) {
        case GET_MYPUBLISH_OK:
          // 获得了列表
          if (misLoadMore) {
            mListView.onLoadComplete();
          } else {
            mListView.onRefreshComplete();
          }

          dealMyPublishListview();
          break;
        case GET_MYPUBLISH_ERROR:
          Toast.makeText(MyPublishActivity.this, "获取发表历史信息失败，请检查网络连接！",
              Toast.LENGTH_SHORT).show();
          break;
        case ClearPushNumThread.MSG_PUT_OK:
          break;
        case ClearPushNumThread.MSG_PUT_ERROR:
          break;
      }
      super.handleMessage(msg);
    }
  }

  /*
   * 自定义线程，从服务器端获取到本设备ID关联的发表帖子列表信息
   */
  class GetMyPublishTips implements Runnable {
    private Handler mhandler = null;
    private int moffset = 0;
    private int mlimit = 0;

    public GetMyPublishTips(Handler arg0, int offset, int limit) {
      this.mhandler = arg0;
      this.moffset = offset;
      this.mlimit = limit;
    }

    @Override
    public void run() {
      // TODO Auto-generated method stub
      JSONObject resultJsonObject = null;

      HttpClient httpClient = new DefaultHttpClient();

      StringBuilder urlStringBuilder = new StringBuilder(makeUrl(
          this.moffset, this.mlimit));
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
        Message msg = Message.obtain();
        msg.what = GET_MYPUBLISH_ERROR;
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
            // 这次确实jsonObject
            jsonArray = new JSONArray(
                entityStringBuilder.toString());
            // 得到了首页数据，传递消息，进行解析并显示
            Message msg = Message.obtain();
            msg.what = GET_MYPUBLISH_OK;
            // 通过Handler发布传送消息，handler
            this.mhandler.sendMessage(msg);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } else {
        // 不存在关联的发表帖子，关闭此activity
        // Toast.makeText(this, , duration)（还是通过发消息到activity里面。）
      }
    }

  }

  /**
   * 处理得到的JSON数据，并且将其填入adapter，并更新列表
   */
  public void dealMyPublishListview() {
    // TODO Auto-generated method stub
    try {
      // 我发表的帖子
      MyDateTimeDeal timedeal = new MyDateTimeDeal();
      if (! misLoadMore) { // 不是加载更多，则清除现有项
        mypublishlistitems.clear();
        mOffset = 0;
      }
      mOffset += jsonArray.length();
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject everyJsonObject = jsonArray.getJSONObject(i);
        PublishThread publishThread = new PublishThread();
        publishThread.setThread_id(everyJsonObject.getString("id"));
        String creatimetemp = timedeal.getTimeGapDesc(everyJsonObject
            .getString("created_at"));
        publishThread.setCreated_at("我" + creatimetemp + "发表了");
        publishThread.setThread_content(everyJsonObject
            .getString("content"));
        publishThread.setPush_num(everyJsonObject.getInt("push_num"));
        publishThread.setThread_type(everyJsonObject.getString("type"));
        mypublishlistitems.add(publishThread);
        // HashMap<String, Object> map = new HashMap<String, Object>();
        //
        // map.put("pubcontent", everyJsonObject.getString("content"));
        // map.put("thread_id", everyJsonObject.get("id"));
        // map.put("type", everyJsonObject.getString("type")); // 帖子类型
        // String creatimetemp;
        // creatimetemp = timedeal.getTimeGapDesc(everyJsonObject
        // .getString("created_at"));
        // map.put("pubtime", "我" + creatimetemp + "发表了");
        // // map.put("like_num", everyJsonObject.get("like_num"));
        // // map.put("tread_num", everyJsonObject.get("tread_num"));
        // mypublishlistitems.add(map);
      }
      mListView.setResultSize(jsonArray.length());
      publishAdapter.notifyDataSetChanged();
      if (mypublishlistitems.size() > 0) {
        hideNoPublish();
      } else {
        showNoPublish();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onLoad() {
    // TODO Auto-generated method stub
    getMyPublish(true, mOffset, 10);
  }

  @Override
  public void onRefresh() {
    // TODO Auto-generated method stub
    getMyPublish(false, 0, 10);
  }

  // 定义我的回复内容的适配器（数组适配器）
  class MyPublishAdapter extends ArrayAdapter<PublishThread> {
    public MyPublishAdapter(Context context, int resource,
        List<PublishThread> objects) {
      super(context, resource, objects);
      // TODO Auto-generated constructor stub
      this.resourceId = resource;
    }

    private int resourceId;

    public MyPublishAdapter(Context context, int resource,
        int textViewResourceId, List<PublishThread> objects) {
      super(context, resource, textViewResourceId, objects);
      // TODO Auto-generated constructor stub
      this.resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      // TODO Auto-generated method stub
      PublishThread publishThread = getItem(position);
      View view;
      ViewHolder viewHolder;
      if (convertView == null) {
        viewHolder = new ViewHolder();
        view = LayoutInflater.from(getContext()).inflate(resourceId,
            null);
        viewHolder.publishTimeView = (TextView) view
            .findViewById(R.id.mypubitemtimetv);

        viewHolder.publishPushNumView = (TextView) view
            .findViewById(R.id.titlenum_publish);
        viewHolder.publishContentView = (TextView) view
            .findViewById(R.id.mypubitemcontenttv);
        viewHolder.bgaBadgeLinearLayout = (BGABadgeLinearLayout) view.findViewById(R.id
            .mypublishtar);
        view.setTag(viewHolder);
      } else {
        view = convertView;
        viewHolder = (ViewHolder) view.getTag();
      }

      viewHolder.publishTimeView.setText(publishThread.getCreated_at());

      if (publishThread.getPush_num() > 0) {
        //				viewHolder.publishPushNumView.setVisibility(TextView.VISIBLE);
        if (publishThread.getPush_num() < 99) {
          // 可能有问题
          // String number = Integer.toString(publishThread
          // .getPush_num());
					/*viewHolder.publishPushNumView.setText(Integer
							.toString(publishThread.getPush_num()));*/
          viewHolder.bgaBadgeLinearLayout.showTextBadge(Integer
              .toString(publishThread.getPush_num()));
        } else {
					/*viewHolder.publishPushNumView.setText("99+");*/
          viewHolder.bgaBadgeLinearLayout.showTextBadge("99+");
        }
      } else {
				/*viewHolder.publishPushNumView.setVisibility(TextView.INVISIBLE);*/
        viewHolder.bgaBadgeLinearLayout.hiddenBadge();
      }

      viewHolder.publishContentView.setText(publishThread
          .getThread_content());
      // return super.getView(position, convertView, parent);
      return view;
    }

    class ViewHolder {
      TextView publishTimeView;
      TextView publishPushNumView;
      TextView publishContentView;
      BGABadgeLinearLayout bgaBadgeLinearLayout;
    }
  }

  class PublishThread {
    private String thread_id;
    private String thread_content;
    private String thread_type;
    private String created_at;
    private int push_num;

    public int getPush_num() {
      return push_num;
    }

    public void setPush_num(int push_num) {
      this.push_num = push_num;
    }

    public String getThread_id() {
      return thread_id;
    }

    public void setThread_id(String thread_id) {
      this.thread_id = thread_id;
    }

    public String getThread_content() {
      return thread_content;
    }

    public void setThread_content(String thread_content) {
      this.thread_content = thread_content;
    }

    public String getThread_type() {
      return thread_type;
    }

    public void setThread_type(String thread_type) {
      this.thread_type = thread_type;
    }

    public String getCreated_at() {
      return created_at;
    }

    public void setCreated_at(String created_at) {
      this.created_at = created_at;
    }
  }

}
