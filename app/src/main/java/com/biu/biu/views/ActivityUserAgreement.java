package com.biu.biu.views;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.biu.biu.views.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

public class ActivityUserAgreement extends BaseActivity {


	@BindView(R.id.toolbar)
	Toolbar toolbar;
	@BindView(R.id.toolbar_title)
	TextView toolbarTitle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_agreement_sencond_version);
		ButterKnife.bind(this);
		setSupportActionBar(toolbar);
		setBackableToolbar(toolbar);
		toolbarTitle.setText(getString(R.string.title_activity_activity_user_agreement));
	}



	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
