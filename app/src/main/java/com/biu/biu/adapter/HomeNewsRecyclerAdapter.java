package com.biu.biu.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.biu.biu.main.TipItemInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

/**
 * Created by fubo on 2016/5/7 0007.
 * email:bofu1993@163.com
 */
public class HomeNewsRecyclerAdapter
    extends RecyclerView.Adapter<HomeNewsRecyclerAdapter.HomeNewsViewHolder> {

  private List<TipItemInfo> tipItemInfos;
  private Context context;

  public HomeNewsRecyclerAdapter(Context context, List<TipItemInfo> tipItemInfos) {
    this.context=context;
    this.tipItemInfos = tipItemInfos;
  }

  @Override
  public HomeNewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.peeptopicitemlayout, parent);
    return new HomeNewsViewHolder(view);
  }

  @Override
  public void onBindViewHolder(HomeNewsViewHolder holder, final int position) {
    holder.content.setText(tipItemInfos.get(position).content);
    holder.date.setText(tipItemInfos.get(position).created_at);
    holder.replyNumber.setText(tipItemInfos.get(position).reply_num);
    holder.likeNumber.setText(tipItemInfos.get(position).like_num);
    holder.cardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onItemClickListener.onClick(position);
      }
    });
    holder.setLikeButtonState(tipItemInfos.get(position).hasliked);
    holder.setUnlikeButtonStatus(tipItemInfos.get(position).hastreaded);
  }


  @Override
  public int getItemCount() {
    return tipItemInfos.size();
  }

  interface OnItemClickListener {
    void onClick(int position);
  }

  private OnItemClickListener onItemClickListener;

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  class HomeNewsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.topiccontent) TextView content;
    @BindView(R.id.create_at_tv) TextView date;
    @BindView(R.id.reply_num_tv) TextView replyNumber;
    @BindView(R.id.likebtn) ImageButton ibLike;
    @BindView(R.id.likecounttv)TextView likeNumber;
    @BindView(R.id.treadbtn) ImageButton ibUnLike;
    @BindView(R.id.news_card)
    CardView cardView;
    public HomeNewsViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this,itemView);
    }
    public void setLikeButtonState(boolean status){
      if (status){
        ibLike.setImageResource(R.drawable.arrow1click);
      } else {
        ibLike.setImageResource(R.drawable.arrow1);
      }
    }
    public void setUnlikeButtonStatus(boolean status){
      if (status){
        ibUnLike.setImageResource(R.drawable.arrow2click);
      } else {
        ibUnLike.setImageResource(R.drawable.arrow2);
      }
    }
  }
}
