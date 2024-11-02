package com.aican.tlcanalyzer.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.TextView;

import com.aican.tlcanalyzer.R;

public class LoadingDialog {

    private static Dialog loadingDialog;

    public static void showLoading(Activity context, boolean cancelable, boolean cancelOnTouchOutside, String message) {
        if (!context.isFinishing()) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                cancelLoading();
            }
            loadingDialog = new Dialog(context);
            loadingDialog.setContentView(R.layout.loading_dialog);

            TextView loadingMessageTextView = loadingDialog.findViewById(R.id.textView);
            loadingMessageTextView.setText(message);

            try {
                loadingDialog.getWindow().setDimAmount(0);
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            } catch (Exception e) {
                Log.d("TAG", "showLoading: " + e.getMessage());
            }
            loadingDialog.setCanceledOnTouchOutside(cancelOnTouchOutside);
            loadingDialog.setCancelable(cancelable);
            loadingDialog.show();
        }
    }

    public static void cancelLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            try {
                loadingDialog.cancel();
                loadingDialog = null;
            } catch (Exception e) {
                Log.d("TAG", "cancelLoading: " + e.getMessage());
            }
        }
    }


}
