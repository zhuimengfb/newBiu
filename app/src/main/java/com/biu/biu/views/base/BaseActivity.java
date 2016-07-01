package com.biu.biu.views.base;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import grf.biu.R;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends AppCompatActivity {


  public void showNonCanclebleDialog(Context context, String message) {
    new AlertDialog.Builder(context).setMessage(message).setTitle("提示").show();
  }

  public void showDialog(String message, DialogInterface.OnClickListener positiveClickListener) {
    new AlertDialog.Builder(this).setTitle(getString(R.string.hint)).setMessage(message)
        .setPositiveButton(R.string.confirm, positiveClickListener).setNegativeButton(R.string
        .cancle, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    }).show();
  }

  public void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  public void setBackableToolbar(Toolbar toolbar) {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setHomeButtonEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    toolbar.setNavigationIcon(R.drawable.back_button);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackPressed();
      }
    });
  }

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
  }
}
