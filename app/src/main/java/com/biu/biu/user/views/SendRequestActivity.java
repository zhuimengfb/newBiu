package com.biu.biu.user.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.biu.biu.user.presenter.UserPresenter;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

/**
 * Created by fubo on 2016/6/4 0004.
 * email:bofu1993@163.com
 */
public class SendRequestActivity extends Activity implements ISendRequestView{

  @BindView(R.id.et_request)
  EditText etRequest;
  @BindView(R.id.tv_cancel)
  RelativeLayout tvCancel;
  @BindView(R.id.tv_confirm)
  RelativeLayout tvConfirm;
  private String jmId = "";
  private UserPresenter userPresenter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_send_request);
    ButterKnife.bind(this);
    Bundle bundle = getIntent().getBundleExtra("jmId");
    jmId = bundle.getString("jmId");
    initEvent();
    userPresenter = new UserPresenter(this);
  }

  private void initEvent() {
    tvCancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

    tvConfirm.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (StringUtils.isEmpty(etRequest.getText().toString())) {
          Toast.makeText(getApplicationContext(), getString(R.string.input_nothing), Toast
              .LENGTH_SHORT).show();
        } else {
          userPresenter.sendFriendRequest(jmId,etRequest.getText().toString());
        }
      }
    });
  }

  public static void toSendRequestActivity(Context context, String jmId) {
    Intent intent = new Intent();
    intent.setClass(context, SendRequestActivity.class);
    Bundle bundle = new Bundle();
    bundle.putString("jmId", jmId);
    intent.putExtra("jmId", bundle);
    context.startActivity(intent);
  }

  @Override
  public void sendSuccess() {
    finish();
  }
}
