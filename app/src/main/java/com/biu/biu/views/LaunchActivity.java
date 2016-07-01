package com.biu.biu.views;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.biu.biu.main.SplashActivity;
import com.biu.biu.views.base.BaseActivity;

import cn.jpush.android.api.JPushInterface;
import grf.biu.R;

public class LaunchActivity extends BaseActivity {

	private static final int REQUEST_READ_PHONE_STATE_CODE = 1;
	private static final int REQUEST_LOCATION_CODE = 2;
	private static final int REQUEST_STORAGE_CODE = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);
		Log.d("packageName", getPackageName());
		if (!isTaskRoot()) {
			finish();
			return;
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		checkPermissions();
		JPushInterface.onResume(this);
	}

	private void checkPermissions(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			checkReadStatePermission();
		} else {
			toSplashActivity();
		}
	}

	private void checkStoragePermission(){
		int checkDeviceIdPermission = ContextCompat.checkSelfPermission(
				this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (checkDeviceIdPermission != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					REQUEST_STORAGE_CODE);
		} else {
			checkLocationPermission();
		}
	}

	private void checkLocationPermission(){
		int checkDeviceIdPermission = ContextCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_COARSE_LOCATION);
		if (checkDeviceIdPermission != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
					REQUEST_LOCATION_CODE);
		} else {
			toSplashActivity();
		}
	}

	private void checkReadStatePermission() {
		int checkDeviceIdPermission = ContextCompat.checkSelfPermission(
				this, Manifest.permission.READ_PHONE_STATE);
		if (checkDeviceIdPermission != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.READ_PHONE_STATE},
					REQUEST_READ_PHONE_STATE_CODE);
		} else {
			checkStoragePermission();
		}
	}
	private void toSplashActivity() {
		Intent mainIntent = new Intent(LaunchActivity.this, SplashActivity.class);
		startActivity(mainIntent);
		finish();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		JPushInterface.onPause(this);
		super.onPause();
	}
	@Override
	public void onRequestPermissionsResult(int requestCode,
			String[] permissions, int[] grantResults) {
		// TODO Auto-generated method stub
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_READ_PHONE_STATE_CODE) {
			if (grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				checkStoragePermission();
			} else {
				new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.hint))
					.setMessage(getResources().getString(
						R.string.you_need_to_grant_read_phone_state_permission))
						.setPositiveButton("确定", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								checkReadStatePermission();
							}
						}).show();

			}
		}  else if (requestCode == REQUEST_LOCATION_CODE){
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				toSplashActivity();
			} else {
				new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.hint))
						.setMessage(getResources().getString(
								R.string.you_need_to_grant_read_location_state_permission))
						.setPositiveButton("确定", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								checkLocationPermission();
							}
						}).show();

			}
		} else if (requestCode==REQUEST_STORAGE_CODE){
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				checkLocationPermission();
			} else {
				new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.hint))
						.setMessage(getResources().getString(
								R.string.you_need_to_grant_read_storage_state_permission))
						.setPositiveButton("确定", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								checkStoragePermission();
							}
						}).show();
			}
		}
	}
}
