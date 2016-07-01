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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

public class MyReplyActivity extends BaseActivity implements OnRefreshListener,
                                                             OnLoadListener {
  private AutoListView mListView = null;
  // private SimpleAdapter msimpleAdapter = null; // “我回复的”的列表框内容适配器
  // private ArrayList<HashMap<String, Object>> mmyReplyInfo = new
  // ArrayList<HashMap<String, Object>>(); // 存储数据
  private String url = "http://api.bbbiu.com:1234";
  private MyReplyHandler myRepliesHandler = null;
  private JSONArray jsonArray = null;

  private final int GET_MYREPLY_OK = 0;
  private final int GET_MYREPLY_ERROR = 1;
  private boolean misLoadMore = false; // false调用refresh接口，true调用loadmore接口
  private boolean mfirstRefresh = true; // 第一次启动时刷新页面
  private int mOffset = 0; // 获取回复贴时跳过的条数（即已经获得到本地的条数）

  // 重新定义存储数据变量
  private List<ReplythreadInfo> mmyReplyInfo = new ArrayList<>();

  // 重新定义一个数组适配器
  private MyReplyAdapter myReplyAdapter = null;
  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.toolbar_title)
  TextView toolbarTitle;
  @BindView(R.id.rl_nothing_layout)
  RelativeLayout nothingLayout;

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
    // 启动线程，从服务器获得数据
    if (mfirstRefresh) {
      getMyReply(false, 0, 10); // 获取我的回复
      mfirstRefresh = false;
    } else {
      // 如果不是第一次启动，但是在发表回复之后，也进行一次获取首页刷新。
      if (UserConfigParams.isHomeRefresh) {
        UserConfigParams.isHomeRefresh = false;
        getMyReply(false, 0, 10); // 获取我的回复
      }
    }
    MobclickAgent.onResume(this);
  }

  private void getMyReply(boolean isLoadMore, int noffset, int nlimit) {
    // TODO Auto-generated method stub
    misLoadMore = isLoadMore;
    Thread thread = new Thread(new GetMyRepliesThread(myRepliesHandler,
        noffset, nlimit));
    thread.start();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my_reply);
    ButterKnife.bind(this);
    findViewId();
    initToolbar();
    mfirstRefresh = true;
    initView();
    myRepliesHandler = new MyReplyHandler();
    mListView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view,
          int position, long id) {
        // TODO Auto-generated method stub
        int nPosition = position - 1;
        if (nPosition >= mmyReplyInfo.size()) {
          return;
        }
        if (mmyReplyInfo.get(nPosition).getPush_num() > 0) {
          new Thread(new ClearPushNumThread(mmyReplyInfo.get(nPosition).getReply_to_id(),
              myRepliesHandler)).start();
          if (mmyReplyInfo.get(nPosition).getPush_num() != 0) {
            //如果回帖对应有新消息，则进行一次刷新
            mfirstRefresh = true;
          }
          mmyReplyInfo.get(nPosition).setPush_num(0);
          myReplyAdapter.notifyDataSetChanged();
        }
        Intent intent = new Intent();
        intent.setClass(MyReplyActivity.this, PeepDetailActivity.class);
        intent.putExtra("thread_id", mmyReplyInfo.get(nPosition).getReply_to_id());
        String strType = mmyReplyInfo.get(nPosition).getType();
        if (strType.equals("0")) {
          intent.putExtra("DetailMode", PeepDetailActivity.TIPDETAIL_FOR_HOMETIP);
        } else if (strType.equals("1")) {
          intent.putExtra("DetailMode", PeepDetailActivity.TIPDETAIL_FOR_MOONBOOX);
        } else if (strType.equals("3")) {
          intent.putExtra("DetailMode", PeepDetailActivity.TIPDETAIL_FOR_PEEPTOPIC);
        } else {
          intent.putExtra("DetailMode", PeepDetailActivity.TIPDETAIL_FOR_HOMETIP);
        }
        MyReplyActivity.this.startActivity(intent);
      }

    });

  }

  private void initToolbar() {
    setSupportActionBar(toolbar);
    setBackableToolbar(toolbar);
    toolbarTitle.setText(getString(R.string.me_myreply));
  }

  private void showNoPublish() {
    nothingLayout.setVisibility(View.VISIBLE);
  }
  private void hideNoPublish() {
    nothingLayout.setVisibility(View.GONE);
  }

  private void initView() {
    // TODO Auto-generated method stub
    // 初始化标题栏

    // addTestData(); // 添加默认数据
    // 重新定义
    // msimpleAdapter = new SimpleAdapter(this, mmyReplyInfo,
    // R.layout.myreplylistitem, new String[] { "replytime",
    // "replycontent", "replyInfo" }, new int[] {
    // R.id.myreplytimetv, R.id.myreplycontent,
    // R.id.myreplyiteminfotv });

    myReplyAdapter = new MyReplyAdapter(this, R.layout.myreplylistitem, mmyReplyInfo);
    mListView.setAdapter(myReplyAdapter);
    mListView.setOnRefreshListener(this);
    mListView.setOnLoadListener(this);
    mListView.setPageSize(10); // 设置每次加载10条
  }


  private void findViewId() {
    // TODO Auto-generated method stub
    mListView = (AutoListView) findViewById(R.id.myreplylistview);
  }

  /*
   * 我回复的消息处理Handler
   */
  class MyReplyHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
      // TODO Auto-generated method stub
      int nMsgNo = msg.what;
      switch (nMsgNo) {
        case GET_MYREPLY_OK:
          // 获得了“我的回复”列表信息,处理并添加到列表框
          if (misLoadMore) {
            mListView.onLoadComplete();
          } else {
            mListView.onRefreshComplete();
          }

          dealMyRepliesToListview();
          break;
        case GET_MYREPLY_ERROR:
          Toast.makeText(MyReplyActivity.this, "获取评论历史信息失败，请检查网络连接！",
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

  /**
   * 从服务器取得我回复的帖子列表
   */
  class GetMyRepliesThread implements Runnable {
    private Handler mhandler;
    private int moffset = 0;
    private int mlimit = 0;

    public GetMyRepliesThread(Handler arg0, int offset, int limit) {
      this.mhandler = arg0;
      this.moffset = offset;
      this.mlimit = limit;
    }

    @Override
    public void run() {
      // TODO Auto-generated method stub
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
      } catch (Exception e) {
        Message msg = Message.obtain();
        msg.what = GET_MYREPLY_ERROR;
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
                new InputStreamReader(httpEntity.getContent(), "UTF-8"), 8 * 1024);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
              entityStringBuilder.append(line + "/n");
            }
            // 利用从HttpEntity中得到的String生成JsonObject
            // 这次确实jsonObject
            jsonArray = new JSONArray(entityStringBuilder.toString());
            // 得到了首页数据，传递消息，进行解析并显示
            Message msg = Message.obtain();
            msg.what = GET_MYREPLY_OK;
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

  public void dealMyRepliesToListview() {
    // TODO Auto-generated method stub
    try {
      // 我回复的帖子
      MyDateTimeDeal timedeal = new MyDateTimeDeal();
      if (! misLoadMore) {
        mmyReplyInfo.clear();
        mOffset = 0;
      }
      mOffset += jsonArray.length();
      // mmyReplyInfo
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject everyJsonObject = jsonArray.getJSONObject(i);
        ReplythreadInfo replythreadInfo = new ReplythreadInfo();
        replythreadInfo.setReply_id(everyJsonObject.getString("id"));
        replythreadInfo.setReplycontent(everyJsonObject
            .getString("content"));
        String creatimetemp = timedeal.getTimeGapDesc(everyJsonObject
            .getString("created_at"));
        replythreadInfo.setReplytime("我" + creatimetemp + "评论了");
        // 可能有问题
        replythreadInfo.setPush_num(everyJsonObject.getInt("push_num"));
        // 存储帖子回复的楼主的相关内容
        JSONObject jsonreplyto = everyJsonObject
            .getJSONObject("reply_to_thread");
        replythreadInfo.setReply_to_id(jsonreplyto.getString("id"));
        replythreadInfo.setReplyToContent(jsonreplyto
            .getString("content"));
        replythreadInfo.setType(jsonreplyto.getString("type"));
        mmyReplyInfo.add(replythreadInfo);
        // HashMap<String, Object> map = new HashMap<String, Object>();
        // map.put("replycontent",
        // everyJsonObject.getString("content"));
        // String creatimetemp;
        // creatimetemp = timedeal.getTimeGapDesc(everyJsonObject
        // .getString("created_at"));
        // map.put("replytime", "我" + creatimetemp + "评论了");
        // JSONObject jsonreplyto = everyJsonObject
        // .getJSONObject("reply_to_thread");
        // map.put("replyInfo", jsonreplyto.getString("content"));
        // map.put("thread_id", jsonreplyto.getString("id"));
        // map.put("type", jsonreplyto.getString("type"));
        // mmyReplyInfo.add(map);
      }
      mListView.setResultSize(jsonArray.length());
      myReplyAdapter.notifyDataSetChanged();
      if (mmyReplyInfo.size() > 0) {
        hideNoPublish();
      } else {
        showNoPublish();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String makeUrl(int offset, int limit) {
    // TODO Auto-generated method stub
    String result = null;
    result = String.format(Locale.ENGLISH,
        "%s/devices/%s/threads?want_reply=%d&offset=%d&limit=%d", url,
        UserConfigParams.device_id, 1, offset, limit);

    return result;
  }

  @Override
  public void onLoad() {
    // TODO Auto-generated method stub
    getMyReply(true, mOffset, 10);
  }

  @Override
  public void onRefresh() {
    // TODO Auto-generated method stub
    getMyReply(false, 0, 10);
  }

  class ReplythreadInfo {
    // map.put("replycontent", everyJsonObject.getString("content"));
    // String creatimetemp;
    // creatimetemp = timedeal.getTimeGapDesc(everyJsonObject
    // .getString("created_at"));
    // map.put("replytime", "我" + creatimetemp + "评论了");
    // JSONObject jsonreplyto = everyJsonObject
    // .getJSONObject("reply_to_thread");
    // map.put("replyInfo", jsonreplyto.getString("content"));
    // map.put("thread_id", jsonreplyto.getString("id"));
    // map.put("type", jsonreplyto.getString("type"));
    // 楼主贴的帖子ID
    private String reply_to_id;
    // 楼主帖子内容
    private String replyToContent;
    private String type;
    // 我的回复内容
    private String replycontent;
    private String replytime;
    // 回复帖子的帖子ID
    private String reply_id;

    // 回帖的新消息情况
    private int push_num;

    public int getPush_num() {
      return push_num;
    }

    public void setPush_num(int push_num) {
      this.push_num = push_num;
    }

    public String getReply_to_id() {
      return reply_to_id;
    }

    public void setReply_to_id(String reply_to_id) {
      this.reply_to_id = reply_to_id;
    }

    public String getReplyToContent() {
      return replyToContent;
    }

    public void setReplyToContent(String replyToContent) {
      this.replyToContent = replyToContent;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getReplycontent() {
      return replycontent;
    }

    public void setReplycontent(String replycontent) {
      this.replycontent = replycontent;
    }

    public String getReplytime() {
      return replytime;
    }

    public void setReplytime(String replytime) {
      this.replytime = replytime;
    }

    public String getReply_id() {
      return reply_id;
    }

    public void setReply_id(String reply_id) {
      this.reply_id = reply_id;
    }

  }

  // 定义我的回复适配器
  class MyReplyAdapter extends ArrayAdapter<ReplythreadInfo> {
    public MyReplyAdapter(Context context, int resource,
        List<ReplythreadInfo> objects) {
      super(context, resource, objects);
      // TODO Auto-generated constructor stub
      this.resourceId = resource;
    }

    private int resourceId;

    public MyReplyAdapter(Context context, int resource,
        int textViewResourceId, List<ReplythreadInfo> objects) {
      super(context, resource, textViewResourceId, objects);
      // TODO Auto-generated constructor stub
      this.resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      // TODO Auto-generated method stub
      ReplythreadInfo replythreadInfo = getItem(position);
      View view;
      ViewHolder viewHolder;
      if (convertView == null) {
        viewHolder = new ViewHolder();
        view = LayoutInflater.from(getContext()).inflate(resourceId,
            null);
        viewHolder.replyTimeView = (TextView) view
            .findViewById(R.id.myreplytimetv);

        viewHolder.replyPushNumView = (TextView) view
            .findViewById(R.id.titlenum_reply);
        viewHolder.replyContentView = (TextView) view
            .findViewById(R.id.myreplycontent);
        viewHolder.replyToContentView = (TextView) view
            .findViewById(R.id.myreplyiteminfotv);
        view.setTag(viewHolder);
      } else {
        view = convertView;
        viewHolder = (ViewHolder) view.getTag();
      }

      viewHolder.replyTimeView.setText(replythreadInfo.getReplytime());

      if (replythreadInfo.getPush_num() > 0) {
        viewHolder.replyPushNumView.setVisibility(TextView.VISIBLE);
        if (replythreadInfo.getPush_num() < 99) {
          // 可能有问题
          // String number = Integer.toString(publishThread
          // .getPush_num());
          viewHolder.replyPushNumView.setText(Integer
              .toString(replythreadInfo.getPush_num()));
        } else {
          viewHolder.replyPushNumView.setText("99+");
        }
      } else {
        viewHolder.replyPushNumView.setVisibility(TextView.INVISIBLE);
      }

      viewHolder.replyContentView.setText(replythreadInfo
          .getReplycontent());
      viewHolder.replyToContentView.setText(replythreadInfo
          .getReplyToContent());
      // return super.getView(position, convertView, parent);
      return view;
    }

    class ViewHolder {
      // 时间
      TextView replyTimeView;
      // 新消息数目
      TextView replyPushNumView;
      // 我的回复
      TextView replyContentView;
      // 楼主的帖子内容
      TextView replyToContentView;

    }
  }

}
