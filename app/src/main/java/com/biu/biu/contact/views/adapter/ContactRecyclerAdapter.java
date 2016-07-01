package com.biu.biu.contact.views.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.biu.biu.app.BiuApp;
import com.biu.biu.contact.entity.ContactInfo;
import com.biu.biu.contact.views.AddContactActivity;
import com.biu.biu.user.views.ShowUserInfoActivity;
import com.biu.biu.utils.GlideCircleTransform;
import com.biu.biu.utils.GlobalString;
import com.bumptech.glide.Glide;
import com.camnter.easyrecyclerviewsidebar.sections.EasySection;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.badgeview.BGABadgeImageView;
import cn.bingoogolapple.badgeview.BGABadgeLinearLayout;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Conversation;
import grf.biu.R;

/**
 * Created by fubo on 2016/5/25 0025.
 * email:bofu1993@163.com
 */
public class ContactRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private String headers;
  private List<ContactInfo> contactInfos;
  private Context context;
  //header和名字的索引
  private List<Integer> nameIndexs = new ArrayList<>();
  private List<EasySection> sectionNames = new ArrayList<>();
  private OnItemClickListener onItemClickListener;
  private static final int TYPE_ADD_HEADER = 0;
  private static final int TYPE_NAME_HEADER = 1;
  private static final int TYPE_NORMAL = 2;
  private static final int TYPE_FOOTER = 3;
  private BGABadgeImageView addImageView;

  public ContactRecyclerAdapter(Context context, List<ContactInfo> contactInfos) {
    this.context = context;
    this.contactInfos = contactInfos;
    init();
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  private void init() {
    nameIndexs.clear();
    sectionNames.clear();
    sectionNames.add(new EasySection("*"));
    StringBuilder stringBuilder = new StringBuilder("");
    if (contactInfos != null && contactInfos.size() > 0) {
      int headerIndex = -1;
      int normalIndex = 1;
      Collections.sort(contactInfos, new ContactInfo.ContactComparator());
      stringBuilder.append(contactInfos.get(0).getEnglishName().substring(0, 1).toUpperCase());
      sectionNames.add(new EasySection(contactInfos.get(0).getEnglishName().substring(0, 1)
          .toUpperCase()));
      nameIndexs.add(headerIndex--);
      nameIndexs.add(normalIndex++);
      for (int i = 1; i < contactInfos.size(); i++) {
        if (!StringUtils.equalsIgnoreCase(contactInfos.get(i - 1).getEnglishName().substring(0, 1)
            , contactInfos.get(i).getEnglishName().substring(0, 1))) {
          nameIndexs.add(headerIndex--);
          if (contactInfos.get(i).getEnglishName().substring(0, 1).toUpperCase()
              .compareToIgnoreCase("Z") < 1) {
            stringBuilder.append(contactInfos.get(i).getEnglishName().substring(0, 1)
                .toUpperCase());
            sectionNames.add(new EasySection(contactInfos.get(i).getEnglishName().substring(0, 1)
                .toUpperCase()));
          } else {
            stringBuilder.append("#");
            sectionNames.add(new EasySection("#"));
            break;
          }
        }
        nameIndexs.add(normalIndex++);
      }
      headers = stringBuilder.toString();
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (position == 0) {
      return TYPE_ADD_HEADER;
    } else if (position == getItemCount() - 1) {
      return TYPE_FOOTER;
    } else if (nameIndexs.get(position - 1) < 0) {
      return TYPE_NAME_HEADER;
    } else {
      return TYPE_NORMAL;
    }
  }

  private int getContactPosition(int position) {
    return nameIndexs.get(position - 1) - 1;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == TYPE_NAME_HEADER) {
      View view = LayoutInflater.from(BiuApp.getContext()).inflate(R.layout.item_contact_header,
          parent, false);
      return new HeaderViewHolder(view);
    } else if (viewType == TYPE_NORMAL) {
      View view = LayoutInflater.from(BiuApp.getContext()).inflate(R.layout.item_contact_normal,
          parent, false);
      return new NormalViewHolder(view);
    } else if (viewType == TYPE_ADD_HEADER) {
      return new AddHeaderViewHolder(LayoutInflater.from(BiuApp.getContext()).inflate(R.layout
          .item_contact_add_header, parent, false));
    } else {
      return new FooterViewHolder(LayoutInflater.from(BiuApp.getContext()).inflate(R.layout
          .item_contact_total_footer, parent, false));
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (getItemViewType(position) == TYPE_NAME_HEADER) {
      int index = Math.abs(nameIndexs.get(position - 1)) - 1;
      ((HeaderViewHolder) holder).setContactHeader(headers.substring(index, index + 1));
    } else if (getItemViewType(position) == TYPE_NORMAL) {
      int index = getContactPosition(position);
      ((NormalViewHolder) holder).setUserName(contactInfos.get(index).getName());
      ((NormalViewHolder) holder).setUserHeadIcon(GlobalString.BASE_URL + "/" + contactInfos.get
          (index).getIconNetAddress());
      ((NormalViewHolder) holder).setContactLayoutClick(contactInfos.get(index));
      ((NormalViewHolder) holder).removeContact(contactInfos.get(index), index);
      ((NormalViewHolder) holder).setIconClick(contactInfos.get(index));
      Conversation conversation = JMessageClient.getSingleConversation(contactInfos.get(index)
          .getUserId());
      if (conversation != null) {
        ((NormalViewHolder) holder).showUnReadNum(conversation.getUnReadMsgCnt());
      }

    } else if (getItemViewType(position) == TYPE_FOOTER) {
      ((FooterViewHolder) holder).setContactNumber(contactInfos.size());
    }
  }

  public List<EasySection> getSections() {
    return sectionNames;
  }

  public int getHeaderPosition(int index) {
    if (index == 0) {
      return 0;
    }
    int count = 0;
    for (int i = 0; i < nameIndexs.size(); i++) {
      if (nameIndexs.get(i) < 0) {
        count++;
      }
      if (count == index) {
        return i + 1;
      }
    }
    return 0;
  }


  public void updateData(List<ContactInfo> contactInfoList) {
    this.contactInfos = contactInfoList;
    init();
    notifyDataSetChanged();
  }

  @Override
  public int getItemCount() {
    return nameIndexs.size() + 2;
  }

  public interface OnItemClickListener {
    void onItemClick(ContactInfo contactInfo);
  }

  public interface RemoveContactListener {
    void removeContact(ContactInfo contactInfo, int position);
  }

  private RemoveContactListener removeContactListener;

  public void setRemoveContactListener(RemoveContactListener removeContactListener) {
    this.removeContactListener = removeContactListener;
  }

  class NormalViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.contact_layout)
    BGABadgeLinearLayout contactLayout;
    @BindView(R.id.iv_user_icon)
    ImageView userHeadIcon;
    @BindView(R.id.tv_contact_name)
    TextView userNameTextView;

    public NormalViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    public void setUserHeadIcon(String path) {
      Glide.with(context).load(path).transform(new GlideCircleTransform(context)).into
          (userHeadIcon);
    }

    public void setUserName(String name) {
      userNameTextView.setText(name);
      userNameTextView.setTypeface(BiuApp.globalTypeface);
    }

    public void setContactLayoutClick(final ContactInfo contactInfo) {
      if (onItemClickListener != null) {
        contactLayout.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            onItemClickListener.onItemClick(contactInfo);
            //点击进入聊天界面，清除未读消息
            showUnReadNum(0);
          }
        });
      }
    }

    public void removeContact(final ContactInfo contactInfo, final int position) {
      if (removeContactListener != null) {
        contactLayout.setOnLongClickListener(new View.OnLongClickListener() {
          @Override
          public boolean onLongClick(View view) {
            removeContactListener.removeContact(contactInfo, position);
            return false;
          }
        });
      }
    }

    public void setIconClick(final ContactInfo contactInfo) {
      userHeadIcon.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          ShowUserInfoActivity.toShowUserPicActivity(context, contactInfo.getUserId());
        }
      });
    }

    public void showUnReadNum(int num) {
      String text = "";
      if (num == 0) {
        contactLayout.hiddenBadge();
        return;
      }
      if (num > 0 && num <= 99) {
        text = num + "";
      } else if (num > 99) {
        text = "99+";
      }
      contactLayout.showTextBadge(text);
    }

  }

  public void setHasNewRequest(int number) {
    if (addImageView != null) {
      if (number > 0) {
        addImageView.showCirclePointBadge();
      } else {
        addImageView.hiddenBadge();
      }
    }
  }


  class HeaderViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_contact_header)
    TextView contactHeaderTextView;

    public HeaderViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    public void setContactHeader(String header) {
      contactHeaderTextView.setText(header);
      contactHeaderTextView.setTypeface(BiuApp.globalTypeface);
    }
  }


  class AddHeaderViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.iv_add_contact)
    BGABadgeImageView addContactImageView;
    @BindView(R.id.tv_add_contact)
    TextView addContactTextView;

    public AddHeaderViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
      Glide.with(context).load(Uri.parse(GlobalString.URI_RES_PREFIX + R.drawable
          .add_contact_icon)).transform(new GlideCircleTransform(context)).into
          (addContactImageView);
      addContactImageView.setOnClickListener(new AddNewFriendListener());
      addContactTextView.setOnClickListener(new AddNewFriendListener());
      addContactTextView.setTypeface(BiuApp.globalTypeface);
      addImageView = addContactImageView;
    }

    class AddNewFriendListener implements View.OnClickListener {

      @Override
      public void onClick(View v) {
        setHasNewRequest(0);
        Intent clearIntent = new Intent();
        clearIntent.setAction(GlobalString.ACTION_CLEAR_BADGE);
        context.sendBroadcast(clearIntent);
        Intent intent = new Intent();
        intent.setClass(context, AddContactActivity.class);
        context.startActivity(intent);
      }
    }

  }

  class FooterViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.tv_contact_num)
    TextView contactNumberTextView;

    public FooterViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
      contactNumberTextView.setTypeface(BiuApp.globalTypeface);
    }

    public void setContactNumber(int number) {
      contactNumberTextView.setText(String.valueOf(number) + BiuApp.getContext().getString(R.string
          .contact_number));
    }
  }
}
