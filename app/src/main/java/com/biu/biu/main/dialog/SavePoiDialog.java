package com.biu.biu.main.dialog;


import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.biu.biu.biumap.PoiDatabaseHelper;

import grf.biu.R;

public class SavePoiDialog extends Dialog implements
		View.OnClickListener {
	private Context context;
	private double lat;
	private double lng;
	private String placeName;
	// 几个主要的控件
	private TextView currentPlace;
	private EditText placeDescription;
	private Button poiCancel;
	private Button poiSure;
	// 数据库
	private PoiDatabaseHelper poiDbHelper;
	// 保存按钮的显示隐藏
	private Button saveButton;

	public SavePoiDialog(Context context, int themeResId) {
		super(context, themeResId);
		// TODO Auto-generated constructor stub
	}

	public SavePoiDialog(Context context, double lat, double lng,
			String placeName, int id, Button saveButton) {
		super(context, id);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.lat = lat;
		this.lng = lng;
		this.placeName = placeName;
		this.saveButton = saveButton;
		WindowManager m = getWindow().getWindowManager();
		Display d = m.getDefaultDisplay();
		WindowManager.LayoutParams p = getWindow().getAttributes();
		p.height = (int) (d.getHeight() * 0.6);
		p.width = (int) (d.getWidth() * 0.95);
		getWindow().setAttributes(p);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dialog_save_poi);
		this.setTitle("帮这个兴趣点起个容易识别的名称吧！");
		// 定义对话框的属性
		currentPlace = (TextView) findViewById(R.id.current_place);
		placeDescription = (EditText) findViewById(R.id.place_description);
		poiCancel = (Button) findViewById(R.id.poi_cancel);
		poiCancel.setOnClickListener(this);
		poiSure = (Button) findViewById(R.id.poi_sure);
		poiSure.setOnClickListener(this);
		// 创建数据库
		poiDbHelper = new PoiDatabaseHelper(context, "poi.db", null, 1);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == poiCancel) {
			this.dismiss();
		} else if (v == poiSure) {
			// 包船当前的地理坐标级位置信息（向数据库中添加数据）
			SQLiteDatabase db = poiDbHelper.getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			contentValues.put("lat", lat);
			contentValues.put("lng", lng);
			contentValues.put("placename", placeName);
			contentValues.put("description", placeDescription.getText()
					.toString());
			db.insert("poi", null, contentValues);
			this.dismiss();
			// 那个保存按钮消失
			saveButton.setVisibility(View.GONE);
			Log.i("保存位置坐标", lat + "  " + lng);
		}

	}
}
