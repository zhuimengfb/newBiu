package com.biu.biu.map;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;

/*
 * 通过高德地图获得设备当前所属的经纬度
 */
public class GetGPS {
	// 经纬度
	private double latitude;
	private double longitude;
	private String mlocatedesInfo;		// 存储位置信息
	private LocationManagerProxy mAMapLocationManagerProxy;
	private AMap aMap;
	private Context mcontext;
	public GetGPS(Context context){
		mcontext = context;
		aMap = new AMap();
		mAMapLocationManagerProxy = LocationManagerProxy.getInstance(context);
		// 注册监听
		mAMapLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, 5000, 10, aMap);
	}
	
	public String getExtraInfo(){
		return mlocatedesInfo;
	}
	
	
	class AMap implements AMapLocationListener{

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		/*
		 * 位置变化时，获取经纬度
		 * @see com.amap.api.location.AMapLocationListener#onLocationChanged(com.amap.api.location.AMapLocation)
		 */
		public void onLocationChanged(AMapLocation location) {
			// TODO Auto-generated method stub
			if(location != null && location.getAMapException().getErrorCode() == 0){
				// 获取位置信息
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				// 将获得的定位信息存入缓存变量
				Bundle locBundle = location.getExtras();
				if(locBundle != null){
					mlocatedesInfo = locBundle.getString("desc");
					String city = location.getCity();
					Log.v("城市信息", city);
					Toast.makeText(mcontext,
							"位置信息是" +mlocatedesInfo,
							Toast.LENGTH_SHORT).show();;
					
				}
			}
			
		}
	}
	/*
	 * 取消监听位置变化信息，不再获取经纬度
	 */
	public void destroyAMapLocationListener(){
		if(mAMapLocationManagerProxy != null){
			mAMapLocationManagerProxy.removeUpdates(aMap);
			mAMapLocationManagerProxy.destroy();
		}
		mAMapLocationManagerProxy = null;
	}
}
