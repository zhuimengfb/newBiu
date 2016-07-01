package com.biu.biu.user.views.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by fubo on 2016/6/2 0002.
 * email:bofu1993@163.com
 */
public class UserPicShowAdapter extends PagerAdapter {
  private List<View> userPicViews;

  public UserPicShowAdapter(List<View> userPicViews) {
    this.userPicViews = userPicViews;
  }

  @Override
  public int getCount() {
    return userPicViews.size();
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    ((ViewPager) container).addView(userPicViews.get(position));
    return userPicViews.get(position);
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    ((ViewPager) container).removeView(userPicViews.get(position));
  }
}
