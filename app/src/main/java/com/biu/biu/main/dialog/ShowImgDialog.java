package com.biu.biu.main.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.biu.biu.imageview.MatrixImageView;

import grf.biu.R;

public class ShowImgDialog extends Dialog {
	private Context context;

	// 对话框中显示图片的imageView
	private MatrixImageView surfImage;
	private ImageView imageView;
	private Bitmap content;

	public ShowImgDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	// theme指定dialog的样式
	public ShowImgDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
	}

	public ShowImgDialog(Context context, int theme, Bitmap content) {
		super(context, theme);
		this.context = context;
		this.content = content;
	}

	@Override
	protected void onCreate(Bundle savesdInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savesdInstanceState);
		this.setCancelable(true);
		this.setCanceledOnTouchOutside(true);
		this.setContentView(R.layout.show_image_dialog);
		surfImage = (MatrixImageView) this.findViewById(R.id.show_img);
		surfImage.setImageBitmap(content);
		surfImage.dialog = this;
	}

}
