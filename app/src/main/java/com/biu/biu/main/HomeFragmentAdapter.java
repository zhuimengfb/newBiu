package com.biu.biu.main;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

public class HomeFragmentAdapter extends FragmentStatePagerAdapter {
	List<Fragment> fragmentList = new ArrayList<Fragment>();
	private FragmentManager fm;

	public HomeFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
		super(fm);
		this.fm = fm;
		this.fragmentList = fragmentList;
		notifyDataSetChanged();
	}

	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		return fragmentList.get(position);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		// return fragmentList.size();
		return 3;
	}

	@Override
	public int getItemPosition(Object object) {
		// TODO Auto-generated method stub
		return PagerAdapter.POSITION_NONE;
	}

	// 改变List中第二个合第四个元素的位置
	public void changeIndex() {
		

	}

}
