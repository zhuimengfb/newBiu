package com.biu.biu.tools;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;


public class EditTextLengthIndicate extends TextView {
	

	public EditTextLengthIndicate(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	private EditText mEditText;
	private int mMaxLength;
	
	public void bindEditText(EditText et, int maxLength) {
		mEditText = et;
		mMaxLength = maxLength;
//		int nLength = mEditText.getText().length();
		setText(String.valueOf(mMaxLength - mEditText.getText().length()));  // 设置剩余可输入的字数
		
		if (et != null && maxLength > 0) {
			et.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
					String str = s.toString();
					int length = str.length();
					setText(String.valueOf(mMaxLength - length));
				}

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub
					
				}
				
			});
		}
	}

}
