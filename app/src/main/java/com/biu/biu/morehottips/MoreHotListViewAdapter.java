package com.biu.biu.morehottips;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biu.biu.main.PeepDetailActivity;
import com.biu.biu.main.TipItemInfo;
import com.biu.biu.netimage.ImageDownloader;
import com.biu.biu.netimage.OnImageDownload;
import com.biu.biu.netoperate.TipLikeTreadThread;
import com.biu.biu.tools.AutoListView;
import com.biu.biu.user.views.ShowUserInfoActivity;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.utils.GlideCircleTransform;
import com.biu.biu.utils.GlobalString;
import com.biu.biu.utils.ShareUtils;
import com.bumptech.glide.Glide;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import grf.biu.R;

public class MoreHotListViewAdapter extends BaseAdapter {

  private LayoutInflater listContainer;
  private Context context;
  private Handler morehothandler = null;
  private AutoListView mListView = null;
  View convertView = null;
  private ArrayList<TipItemInfo> mlistItemsinfo;
  // 识别是当地用户还是游客
  private boolean localornot;
  ImageDownloader mDownloader;
  private WeakReference<Activity> activityWeakReference;

  public void setActivity(Activity activity) {
    activityWeakReference = new WeakReference<>(activity);
  }

  private TopicNumberListener topicNumberListener;

  public void setTopicNumberListener(TopicNumberListener topicNumberListener) {
    this.topicNumberListener = topicNumberListener;
  }

  public interface TopicNumberListener {
    void showNoTopic();

    void hideNoTopic();
  }

  /**
   * @param context   :运行上下文，这里是MoreHotActivity
   * @param listItems ：帖子信息存储
   */
  public MoreHotListViewAdapter(Context context,
      ArrayList<TipItemInfo> listItems, Handler handler,
      boolean localornot) {
    this.context = context;
    this.mlistItemsinfo = listItems;
    listContainer = LayoutInflater.from(context); // 创建视图容器工厂并设置上下文
    // convertView = listContainer.inflate(R.layout.homelistitemlayout,
    // null);
    this.morehothandler = handler;
    this.localornot = localornot;
  }

  public class TreadDownThread implements Runnable {
    private Handler mhandler = null;
    private String thread_id = null; // 帖子ID
    private String url = null;

    public TreadDownThread(Handler targethandler, String threadid,
        String treadurl) {
      // TODO Auto-generated constructor stub
      this.mhandler = targethandler;
      this.thread_id = threadid;
      this.url = treadurl;
    }

    @Override
    public void run() {
      // TODO Auto-generated method stub
      JSONObject resultJsonObject = null;
      HttpPut httpput = new HttpPut(url);
      HttpClient httpClient = new DefaultHttpClient();
      StringBuilder urlStringBuilder = new StringBuilder(url);
      StringBuilder entityStringBuilder = new StringBuilder();
      BufferedReader bufferedReader = null;
      HttpResponse httpResponse = null;

      try {
        // HttpClient发出一个HttpGet请求
        httpResponse = httpClient.execute(httpput);
      } catch (Exception e) {
        e.printStackTrace();
      }
      // 得到httpResponse的状态响应码
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        // 成功，更新帖子状态
        Message msg = Message.obtain();
        msg.what = 7;
        // errordesc = e.getMessage() + "执行请求错误";
        // 通过Handler发布传送消息，handler
        this.mhandler.sendMessage(msg);
      } else {
        // 不存在关联的发表帖子，关闭此activity
        // Toast.makeText(context, "服务器错误，操作失败！",
        // Toast.LENGTH_SHORT).show();
        // Toast.makeText(this, , duration)（还是通过发消息到activity里面。）
      }
    }

  }

  public class PutupThread implements Runnable {
    private Handler mhandler = null;
    private String thread_id = null; // 帖子ID
    private String url = null;

    public PutupThread(Handler targethandler, String threadid, String puturl) {
      this.mhandler = targethandler;
      this.thread_id = threadid;
      this.url = puturl;
      // this.mposition = nPosition;
    }

    @Override
    public void run() {
      // TODO Auto-generated method stub
      HttpPut httpput = new HttpPut(url);
      HttpClient httpClient = new DefaultHttpClient();
      HttpResponse httpResponse = null;

      try {
        // HttpClient发出一个HttpGet请求
        httpResponse = httpClient.execute(httpput);
      } catch (Exception e) {
        e.printStackTrace();
      }
      // 得到httpResponse的状态响应码
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        // 刷新首页
        Message msg = Message.obtain();
        msg.what = 7;
        // 通过Handler发布传送消息，handler
        this.mhandler.sendMessage(msg);
      } else {
        // 不存在关联的发表帖子，关闭此activity
      }
    }
  }

  public class DownbtnListener implements OnClickListener {
    private Integer mdowncount = 0; // 当前踩贴数
    int mPosition = 0;
    String mtipid = null;
    // TextView mtreadCounttv = null;
    private int mlikeState = 0; // 0：没踩没顶 1：顶了 -1：踩了
    // boolean mhastreaded = false; // 踩的状态
    private boolean flag = true;
    private String murl = "";

    public DownbtnListener(int position, String tipid, int nlikestate) {
      // TODO Auto-generated constructor stub
      this.mPosition = position;
      this.mtipid = tipid;
      this.mlikeState = nlikestate;
    }

    // 计时线程，1秒钟只能点一次
    private class TimeThread extends Thread {
      public void run() {
        try {
          Thread.sleep(1000);
          flag = true;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    private synchronized void setFlag() {
      flag = false;
    }

    @Override
    public void onClick(View v) {
      // TODO Auto-generated method stub
      if (! flag) {
        return;
      } else {
        setFlag();
        new TimeThread().start();
      }
      murl = "http://api.bbbiu.com:1234/threads/" + mtipid;
      Integer nlikenum = Integer
          .parseInt(mlistItemsinfo.get(mPosition).like_num);
      // 当前处于顶的状态
      switch (mlikeState) {
        case - 1:
          // 当前为踩，点击踩，取消踩。
          murl += "/action:tread" + "/?device_id="
              + UserConfigParams.device_id + "&is_repeal=1";
          ++ nlikenum;
          mlistItemsinfo.get(mPosition).hastreaded = false;
          break;
        case 0:
          // 当前无，点击踩，设为踩
          murl += "/action:tread" + "/?device_id="
              + UserConfigParams.device_id;
          -- nlikenum;
          mlistItemsinfo.get(mPosition).hastreaded = true;
          break;
        case 1:
          // 当前顶，点击踩，取消顶
          murl += "/action:like" + "/?device_id="
              + UserConfigParams.device_id + "&is_repeal=1";
          -- nlikenum;
          mlistItemsinfo.get(mPosition).hasliked = false;
          break;
      }
      mlistItemsinfo.get(mPosition).like_num = nlikenum.toString();
      // 更新显示ListView的状态
      MoreHotListViewAdapter.this.notifyDataSetChanged();
      TipLikeTreadThread mthread = new TipLikeTreadThread(morehothandler,
          mtipid, mPosition, murl);
      // mthread.setSendMessage(false); // 只执行操作，不发送消息
      Thread thread = new Thread(mthread);
      thread.start();
    }

  }

  private class TopbtnListener implements OnClickListener {
    private Integer mtopcount = 0; // 当前的顶贴数
    int mPosition = 0;
    String mtipid = null;
    boolean mhasliked = false;
    private boolean flag = true;
    private int mlikeState = 0; // 0：没踩没顶 1：顶了 -1：踩了
    private String murl = "";

    public TopbtnListener(int position, String tipid, int nlikestate) {
      this.mPosition = position;
      this.mlikeState = nlikestate;
      this.mtipid = tipid;
    }

    // 计时线程，1秒钟只能点一次
    private class TimeThread extends Thread {
      public void run() {
        try {
          Thread.sleep(1000);
          flag = true;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    private synchronized void setFlag() {
      flag = false;
    }

    @Override
    public void onClick(View v) {
      // TODO Auto-generated method stub
      if (! flag) {
        return;
      } else {
        setFlag();
        new TimeThread().start();
      }
      murl = "http://api.bbbiu.com:1234/threads/" + mtipid;
      Integer nlikenum = Integer
          .parseInt(mlistItemsinfo.get(mPosition).like_num);
      switch (mlikeState) {
        case - 1:
          // 当前为踩，点击顶，取消踩。
          murl += "/action:tread" + "/?device_id="
              + UserConfigParams.device_id + "&is_repeal=1";
          ++ nlikenum;
          mlistItemsinfo.get(mPosition).hastreaded = false;
          break;
        case 0:
          // 当前无，点击顶，设为顶
          murl += "/action:like" + "/?device_id="
              + UserConfigParams.device_id;
          ++ nlikenum;
          mlistItemsinfo.get(mPosition).hasliked = true;
          break;
        case 1:
          // 当前顶，点击顶，取消顶
          murl += "/action:like" + "/?device_id="
              + UserConfigParams.device_id + "&is_repeal=1";
          -- nlikenum;
          mlistItemsinfo.get(mPosition).hasliked = false;
          break;
      }
      mlistItemsinfo.get(mPosition).like_num = nlikenum.toString();
      // 更新显示ListView的状态
      MoreHotListViewAdapter.this.notifyDataSetChanged();
      TipLikeTreadThread mthread = new TipLikeTreadThread(morehothandler,
          mtipid, mPosition, murl);
      // mthread.setSendMessage(false); // 只执行操作，不发送消息
      Thread thread = new Thread(mthread);
      thread.start();
    }

  }

  /**
   * 将activity的匿名内部类引用传入adaper，直接发送消息。
   *
   * @param
   */
  // public void setHandler(Handler handler){
  // this.myhandler = handler;
  // }
  @Override
  public int getCount() {
    // TODO Auto-generated method stub
    return mlistItemsinfo.size();
  }

  @Override
  public Object getItem(int position) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long getItemId(int position) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    // TODO Auto-generated method stub
    // 自定义视图
    ListItemView listItemView = null; // 与ViewHolder类似
    if (convertView == null) {
      listItemView = new ListItemView();
      // 获取list_item布局文件的视图
      // convertView = listContainer.inflate(R.layout.homelistitemlayout,
      // null); // 2015年9月28日：换新的布局
      convertView = listContainer.inflate(R.layout.peeptopicitemlayout,
          null);
      // 获取控件对象
      listItemView.itemLayout = (RelativeLayout) convertView.findViewById(R.id.news_card);
      listItemView.homeContenttv = (TextView) convertView.findViewById(R.id.topiccontent);
      listItemView.img = (ImageView) convertView.findViewById(R.id.topicimg);
      listItemView.publishPlacetv = (TextView) convertView.findViewById(R.id.publishplace);
      listItemView.publishTimetv = (TextView) convertView.findViewById(R.id.create_at_tv);
      listItemView.replayCounttv = (TextView) convertView.findViewById(R.id.reply_num_tv);
      listItemView.hometopbtn = (ImageView) convertView.findViewById(R.id.likebtn);
      listItemView.homedownbtn = (ImageButton) convertView.findViewById(R.id.treadbtn);
      listItemView.TopCounttv = (TextView) convertView.findViewById(R.id.likecounttv);
      listItemView.replyIcon = (ImageView) convertView.findViewById(R.id.iv_reply_icon);
      listItemView.shareIcon = (ImageView) convertView.findViewById(R.id.iv_share_icon);
      listItemView.userName = (TextView) convertView.findViewById(R.id.tv_user_name);
      listItemView.userHeadIcon = (ImageView) convertView.findViewById(R.id.iv_head_icon);
      listItemView.shareLayout = (RelativeLayout) convertView.findViewById(R.id.share_layout);
      listItemView.likeLayout = (RelativeLayout) convertView.findViewById(R.id.like_layout);
      listItemView.userInfoLayout = (RelativeLayout) convertView.findViewById(R.id
          .user_info_layout);
      // 设置控件集到convertView
      convertView.setTag(listItemView);
    } else {
      listItemView = (ListItemView) convertView.getTag();
    }

    // 设置默认显示资源图片
    if (mlistItemsinfo.get(position).isEmpty) {
      // 无数据
      if (topicNumberListener != null) {
        topicNumberListener.showNoTopic();
      }
      listItemView.itemLayout.setVisibility(View.GONE);
      listItemView.homeContenttv.setText("很遗憾，您附近没有热门贴。");
      listItemView.publishPlacetv.setVisibility(TextView.GONE); // 位置信息
      listItemView.replayCounttv.setVisibility(TextView.GONE); // 回复数
      //			listItemView.DownCounttv.setVisibility(TextView.GONE); // 踩数
      listItemView.TopCounttv.setVisibility(TextView.GONE); // 顶数
      listItemView.homedownbtn.setVisibility(ImageButton.GONE);
      listItemView.hometopbtn.setVisibility(ImageButton.GONE);
      listItemView.img.setVisibility(View.GONE);
      listItemView.replyIcon.setVisibility(View.GONE);
      listItemView.shareIcon.setVisibility(View.GONE);
      // 发表时间图标
      // listItemView.pubtimeImg.setVisibility(ImageView.GONE);
      // 回复数图标
      // listItemView.replyImg.setVisibility(ImageView.GONE);
    } else {
      // 显示所有控件
      if (topicNumberListener != null) {
        topicNumberListener.hideNoTopic();
      }
      listItemView.publishPlacetv.setVisibility(TextView.VISIBLE); // 位置信息
      listItemView.replayCounttv.setVisibility(TextView.VISIBLE); // 回复数
      // listItemView.DownCounttv.setVisibility(TextView.VISIBLE); // 踩数
      if (localornot) {
        listItemView.TopCounttv.setVisibility(TextView.VISIBLE); // 顶数
        listItemView.homedownbtn.setVisibility(ImageButton.VISIBLE);
        listItemView.hometopbtn.setVisibility(ImageButton.VISIBLE);
      } else {
        listItemView.TopCounttv.setVisibility(TextView.GONE); // 顶数
        listItemView.homedownbtn.setVisibility(ImageButton.GONE);
        listItemView.hometopbtn.setVisibility(ImageButton.GONE);
      }
      listItemView.shareLayout.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          if (activityWeakReference != null && activityWeakReference.get() != null) {
            ShareUtils shareUtils = new ShareUtils(activityWeakReference.get());
            shareUtils.openShare();
          }
        }
      });
      listItemView.shareIcon.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (activityWeakReference != null && activityWeakReference.get() != null) {
            ShareUtils shareUtils = new ShareUtils(activityWeakReference.get());
            shareUtils.openShare();
          }
        }
      });

      //匿名状态
      if (mlistItemsinfo.get(position).anony == 0) {
        listItemView.userName.setText(mlistItemsinfo.get(position).simpleUserInfo.getNickname());
        Glide.with(context).load(GlobalString.BASE_URL + "/" + mlistItemsinfo.get(position)
            .simpleUserInfo.getIcon_small()).transform(new GlideCircleTransform(context)).into
            (listItemView.userHeadIcon);
        listItemView.userInfoLayout.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            ShowUserInfoActivity.toShowUserPicActivity(context, mlistItemsinfo.get(position)
                .device_id);
          }
        });
      } else {
        listItemView.userName.setText("匿名");
        Glide.with(context).load(GlobalString.URI_RES_PREFIX + R.drawable.default_user_icon2)
            .into(listItemView.userHeadIcon);
        listItemView.userInfoLayout.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {

          }
        });
      }
      // 发表时间图标
      // listItemView.pubtimeImg.setVisibility(ImageView.VISIBLE);
      // 回复数图标
      // listItemView.replyImg.setVisibility(ImageView.VISIBLE);
      listItemView.replyIcon.setVisibility(View.VISIBLE);
      listItemView.shareIcon.setVisibility(View.VISIBLE);
      // 设置动态改变资源
      boolean blikestate = mlistItemsinfo.get(position).hasliked;
      boolean btreadstate = mlistItemsinfo.get(position).hastreaded;
      if (blikestate) {
        listItemView.hometopbtn.setImageResource(R.drawable.like_after_icon);
      } else {
        listItemView.hometopbtn.setImageResource(R.drawable.like_before_icon);
      }
      // 设置踩的状态
      if (btreadstate) {
        listItemView.homedownbtn
            .setImageResource(R.drawable.arrow2click);
      } else {
        listItemView.homedownbtn.setImageResource(R.drawable.arrow2);
      }
      // 设置Topic内容
      // if(position < 3){
      // String contenttemp =
      // mlistItemsinfo.get(position).get("itemContent").toString();
      // contenttemp = "<img src=\"" + R.drawable.hot_icon + "\" />" +
      // contenttemp;
      // listItemView.homeContenttv.setText(Html.fromHtml(contenttemp,
      // imageGetter, null));
      // }else{
      // listItemView.homeContenttv.setText(mlistItemsinfo.get(position).get("itemContent")
      // .toString());
      // }
      listItemView.homeContenttv
          .setText(mlistItemsinfo.get(position).content);
      // 设置发表时间、回复数、顶、踩数量
      Integer topcount = Integer
          .parseInt(mlistItemsinfo.get(position).like_num);
      // Integer downcount =
      // Integer.parseInt(mlistItemsinfo.get(position).get("downCount").toString());
      listItemView.publishTimetv
          .setText(mlistItemsinfo.get(position).created_at);
      listItemView.replayCounttv.setText(mlistItemsinfo.get(position).reply_num+"");
      listItemView.TopCounttv.setText(topcount.toString());
      // listItemView.DownCounttv.setText(downcount.toString());
      String itemPlace = mlistItemsinfo.get(position).pubplace;
      if (itemPlace.isEmpty() || itemPlace.equals("null")) {
        listItemView.publishPlacetv.setVisibility(TextView.INVISIBLE);
      } else {
        listItemView.publishPlacetv.setVisibility(TextView.VISIBLE);
        listItemView.publishPlacetv.setText(itemPlace);
      }
      // 给Button添加单击事件，添加Button之后ListView将失去焦点，需要将Button的焦点去掉
      int nlikestate = 0;
      if (blikestate) {
        nlikestate = 1;
      } else {
        nlikestate = btreadstate ? - 1 : 0;
      }
      String tipid = mlistItemsinfo.get(position).id;
      listItemView.likeLayout.setOnClickListener(new TopbtnListener(position, tipid,
          nlikestate));
      listItemView.hometopbtn.setOnClickListener(new TopbtnListener(
          position, tipid, nlikestate));
      listItemView.homedownbtn.setOnClickListener(new DownbtnListener(
          position, tipid, nlikestate));
      // 给ContentTextView添加点击事件
      // listItemView.homeContenttv
      // .setOnClickListener(new HomeContentClickListener(tipid));
      convertView.setOnClickListener(new HomeContentClickListener(tipid));
      listItemView.img.setOnClickListener(new HomeContentClickListener(
          tipid));
      // 处理图片
      String urltemp = mlistItemsinfo.get(position).imgurl;
      if (urltemp != null && ! urltemp.equals("null")) {
        final String url = "http://api.bbbiu.com:1234/" + urltemp;
        listItemView.img.setTag(String.valueOf(position)); // 用索引作为标记
        // listItemView.img.setImageResource(android.R.drawable.stat_sys_download_done);
        if (mDownloader == null) {
          mDownloader = new ImageDownloader();
        }
        // 异步下载图片
        mDownloader.imageDownload(String.valueOf(position), url,
            listItemView.img, context.getExternalCacheDir()
                .getPath(), (Activity) this.context,
            new OnImageDownload() {
              @Override
              public void onDownloadSucc(String tag,
                  Bitmap bitmap, String c_url,
                  ImageView mimageView) {
                ImageView imageView = (ImageView) mListView
                    .findViewWithTag(tag);
                if (imageView != null) {
                  imageView.setVisibility(View.VISIBLE);
                  imageView.setImageBitmap(bitmap);
                  imageView.setTag("");
                }
              }
            });
      } else {
        listItemView.img.setVisibility(View.GONE);
      }
    }
    return convertView;
  }

  private class HomeContentClickListener implements OnClickListener {
    private String mTipId = "";

    public HomeContentClickListener(String tipid) {
      // TODO Auto-generated constructor stub
      mTipId = tipid;
    }

    @Override
    public void onClick(View v) {
      // TODO Auto-generated method stub
      Intent intent = new Intent();
      intent.setClass(context, PeepDetailActivity.class);
      intent.putExtra("thread_id", mTipId);
      intent.putExtra("DetailMode",
          PeepDetailActivity.TIPDETAIL_FOR_HOMETIP);
      intent.putExtra("localornot", localornot);
      context.startActivity(intent);
    }
  }

  // /**
  // * 表现热帖的html图标
  // */
  // final Html.ImageGetter imageGetter = new Html.ImageGetter() {
  // private Drawable mdrawable = null;
  // @Override
  // public Drawable getDrawable(String source) {
  // if(mdrawable != null)
  // return mdrawable;
  // int rId = Integer.parseInt(source);
  // mdrawable = context.getResources().getDrawable(rId);
  // mdrawable.setBounds(0, 0, mdrawable.getIntrinsicWidth(),
  // mdrawable.getIntrinsicHeight());
  //
  // return mdrawable;
  // }
  // };

  /**
   * 存储帖子内容信息的缓存
   *
   * @author grf
   */
  public final class ListItemView {

    // public boolean noItemInfo; // 没有内容时的提示
    public TextView homeContenttv; // 内容控件
    public ImageView img; // 图片控件
    public TextView publishTimetv; // 发表时间
    public TextView replayCounttv; // 回复时间
    public TextView TopCounttv; // 顶的数量
    public TextView DownCounttv; // 踩的计数
    public TextView publishPlacetv; // 发表时间
    public ImageView pubtimeImg; // 发表时间图标
    public ImageView replyImg; // 回复数图标
    public ImageView hometopbtn; // 顶按钮
    public ImageButton homedownbtn; // 踩按钮
    public ImageView replyIcon;
    public ImageView shareIcon;
    // public TextView morehottips; // 更多热帖
    public ImageView userHeadIcon;
    public TextView userName;
    public RelativeLayout itemLayout;
    public RelativeLayout shareLayout;
    public RelativeLayout likeLayout;
    public RelativeLayout userInfoLayout;
  }

  public void setListView(AutoListView listView) {
    // TODO Auto-generated method stub
    this.mListView = listView;
  }
}
