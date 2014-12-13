package com.google.zxing.integration.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class XZingOfferingDialog extends AlertDialog {
    private final Activity context;

    public XZingOfferingDialog(
            final Activity context,
            final String title,
            final String message,
            final String positiveButtonText,
            final DialogInterface.OnClickListener onPositiveButtonClickListener,
            final String negativeButtonText) {
        super(context);

        this.context = context;

        setTitle(title);
        setMessage(message);
        setButton(AlertDialog.BUTTON_POSITIVE, positiveButtonText, onPositiveButtonClickListener);
        setButton(AlertDialog.BUTTON_NEGATIVE, negativeButtonText, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                context.finish();
            }
        });
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        context.finish();
    }
//    downloadDialog.setTitle(title);
//    downloadDialog.setMessage(message);
//    downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
//        @Override
//        public void onClick(DialogInterface dialogInterface, int i) {
//            String packageName;
//            if (targetApplications.contains(BS_PACKAGE)) {
//                // Prefer to suggest download of BS if it's anywhere in the list
//                packageName = BS_PACKAGE;
//            } else {
//                // Otherwise, first option:
//                packageName = targetApplications.get(0);
//            }
//            Uri uri = Uri.parse("market://details?id=" + packageName);
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            try {
//                if (fragment == null) {
//                    activity.startActivity(intent);
//                } else {
//                    fragment.startActivity(intent);
//                }
//            } catch (ActivityNotFoundException anfe) {
//                // Hmm, market is not installed
//                Log.w(TAG, "Google Play is not installed; cannot install " + packageName);
//            }
//            activity.finish();
//        }
//    });
//    downloadDialog.setNegativeButton(buttonNo, onNegativeButtonClickListener);
//    downloadDialog.setCancelable(false);
}
