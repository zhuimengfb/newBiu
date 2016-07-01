package com.biu.biu.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

import grf.biu.R;


public class ChooseImgResActivity extends Activity implements OnClickListener {

	private Button fromcamerabtn;
	private Button fromlocalimgbtn;
	private Button canclbtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_img_res);
		LayoutParams p = this.getWindow().getAttributes();
		p.width = getWindowManager().getDefaultDisplay().getWidth();
		getWindow().setAttributes(p);
		findId();
		// 添加按钮监听
		fromcamerabtn.setOnClickListener(this);
		fromlocalimgbtn.setOnClickListener(this);
		canclbtn.setOnClickListener(this);
		this.setResult(2); // 返回码设置为取消
	}

	private void findId() {
		// TODO Auto-generated method stub
		fromcamerabtn = (Button) findViewById(R.id.fromcamerabtn);
		fromlocalimgbtn = (Button) findViewById(R.id.localimagesbtn);
		canclbtn = (Button) findViewById(R.id.cancleadding);
	}

	// 实现onTouchEvent触屏函数当点击屏幕时销毁本Activity
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.setResult(2); // 返回码设置为取消
		finish();
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int nCode = 2;
		switch (v.getId()) {
		case R.id.fromcamerabtn:
			nCode = 0;
			break;
		case R.id.localimagesbtn:
			nCode = 1;
			break;
		case R.id.cancleadding:
			nCode = 2;
			break;
		}
		this.setResult(nCode);
		this.finish();
	}
}
