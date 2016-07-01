package com.biu.biu.imageloader;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.biu.biu.utils.CommonAdapter;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import grf.biu.R;

public class MyAdapter extends CommonAdapter<String>{
	private Context mContext;
	

	/**
	 * 用户选择的图片，存储为图片的完整路径
	 */
	public static List<String> mSelectedImage = new LinkedList<String>();

	/**
	 * 文件夹路径
	 */
	private String mDirPath;

	public MyAdapter(Context context, List<String> mDatas, int itemLayoutId,
			String dirPath)
	{
		super(context, mDatas, itemLayoutId);
		this.mDirPath = dirPath;
		this.mContext = context;
	}

	@Override
	public void convert(final com.biu.biu.utils.ViewHolder helper, final String item)
	{
		//设置no_pic
		helper.setImageResource(R.id.id_item_image, R.drawable.pictures_no);
		//设置no_selected
				helper.setImageResource(R.id.id_item_select,
						R.drawable.picture_unselected);
		//设置图片
		helper.setImageByUrl(R.id.id_item_image, mDirPath + "/" + item);
		
		final ImageView mImageView = helper.getView(R.id.id_item_image);
		final ImageView mSelect = helper.getView(R.id.id_item_select);
		
		mImageView.setColorFilter(null);
		//设置ImageView的点击事件
		mImageView.setOnClickListener(new OnClickListener()
		{
			//选择，则将图片变暗，反之则反之
			@Override
			public void onClick(View v)
			{
				// 选择了某个图片就直接返回
				Intent intent = new Intent();
				File file = new File(mDirPath + "/" + item);
				Uri uri = Uri.fromFile(file);
				intent.setData(uri);
				((Activity)mContext).setResult(101, intent);
				((Activity)mContext).finish();
//				setResult(100, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
//				finish();//此处一定要调用finish()方法
			

//				// 已经选择过该图片
//				if (mSelectedImage.contains(mDirPath + "/" + item))
//				{
//					mSelectedImage.remove(mDirPath + "/" + item);
//					mSelect.setImageResource(R.drawable.picture_unselected);
//					mImageView.setColorFilter(null);
//				} else
//				// 未选择该图片
//				{
//					mSelectedImage.clear();		// 始终只能选择一个图片
//					mSelectedImage.add(mDirPath + "/" + item);
//					mSelect.setImageResource(R.drawable.pictures_selected);
//					mImageView.setColorFilter(Color.parseColor("#77000000"));
//				}

			}
		});
		
		/**
		 * 已经选择过的图片，显示出选择过的效果
		 */
		if (mSelectedImage.contains(mDirPath + "/" + item))
		{
			mSelect.setImageResource(R.drawable.pictures_selected);
			mImageView.setColorFilter(Color.parseColor("#77000000"));
		}

	}
}
