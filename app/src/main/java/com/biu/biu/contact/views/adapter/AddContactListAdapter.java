package com.biu.biu.contact.views.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biu.biu.app.BiuApp;
import com.biu.biu.contact.entity.AddContactBean;
import com.biu.biu.user.views.ShowUserInfoActivity;
import com.biu.biu.utils.GlideCircleTransform;
import com.biu.biu.utils.GlobalString;
import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

/**
 * Created by fubo on 2016/5/27 0027.
 * email:bofu1993@163.com
 */
public class AddContactListAdapter
    extends RecyclerView.Adapter<AddContactListAdapter.AddContactViewHolder> {

  private List<AddContactBean> requestList;
  private AcceptRequestListener acceptRequestListener;
  private RemoveRequestListener removeRequestListener;
  private Context context;

  public AddContactListAdapter(List<AddContactBean> requestList) {
    this.requestList = requestList;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public void setRemoveRequestListener(RemoveRequestListener removeRequestListener) {
    this.removeRequestListener = removeRequestListener;
  }

  @Override
  public AddContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new AddContactViewHolder(LayoutInflater.from(BiuApp.getContext()).inflate(R.layout
        .item_add_contact, parent, false));
  }

  @Override
  public void onBindViewHolder(AddContactViewHolder holder, final int position) {
    holder.addContactName.setText(requestList.get(position).getSenderInfo().getNickname());
    Glide.with(BiuApp.getContext()).load(GlobalString.BASE_URL + "/" + requestList.get(position)
        .getSenderInfo().getIcon_small()).transform(new GlideCircleTransform(BiuApp.getContext())
    ).placeholder(R.drawable.default_user_icon2).error(R.drawable.default_user_icon2).into(holder
        .addContactIcon);
    holder.addMessage.setText(requestList.get(position).getAddContactRequest().getMessage());
    holder.addContactIcon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (context != null) {
          ShowUserInfoActivity.toShowUserPicActivity(context, requestList.get(position)
              .getAddContactRequest().getSenderJmId());
        }
      }
    });
    switch (requestList.get(position).getAddContactRequest().getStatus()) {
      case AddContactBean.STATUS_ADD_ALREADY:
        holder.showAddAlready();
        break;
      case AddContactBean.STATUS_ADD_NORMAL:
        holder.showAddButton();
        holder.addContactButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (acceptRequestListener != null) {
              acceptRequestListener.onAcceptRequest(requestList.get(position));
            }
          }
        });
        break;
      default:
        break;
    }
    holder.addContactItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        if (removeRequestListener != null) {
          removeRequestListener.removeRequest(requestList.get(position), position);
        }
        return false;
      }
    });
  }

  public void setAcceptRequestListener(AcceptRequestListener acceptRequestListener) {
    this.acceptRequestListener = acceptRequestListener;
  }

  public interface AcceptRequestListener {
    void onAcceptRequest(AddContactBean addContactBean);
  }

  public interface RemoveRequestListener {
    void removeRequest(AddContactBean addContactBean, int position);
  }

  @Override
  public int getItemCount() {
    return requestList.size();
  }

  class AddContactViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.iv_add_head_icon)
    ImageView addContactIcon;
    @BindView(R.id.tv_add_contact_name)
    TextView addContactName;
    @BindView(R.id.bt_add_contact)
    Button addContactButton;
    @BindView(R.id.tv_already_add)
    TextView addAlready;
    @BindView(R.id.tv_add_message)
    TextView addMessage;
    @BindView(R.id.rl_add_contact_request)
    RelativeLayout addContactItemLayout;

    public AddContactViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    public void showAddAlready() {
      addContactButton.setVisibility(View.GONE);
      addAlready.setVisibility(View.VISIBLE);
    }

    public void showAddButton() {
      addContactButton.setVisibility(View.VISIBLE);
      addAlready.setVisibility(View.GONE);
    }
  }
}
