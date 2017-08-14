package com.sunxuedian.ipc_test.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by sunxuedian on 2017/8/14.
 */

public class ToastUtils {
    public static void showToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
