package com.biu.biu.views.base;

import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * Created by fubo on 2016/5/7 0007.
 * email:bofu1993@163.com
 */
public class BaseFragment extends Fragment {

  public void showShortToast(String message) {
    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
  }

}
