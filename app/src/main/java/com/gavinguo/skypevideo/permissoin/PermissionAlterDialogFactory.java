package com.gavinguo.skypevideo.permissoin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by gavinguo on 6/28/2016.
 */
public class PermissionAlterDialogFactory {

    public static AlertDialog getAlertDialog(Context context,String title,String allowPrompt,String message,DialogInterface.OnClickListener onClickListener) {
        AlertDialog builder = new AlertDialog.Builder(context)
                .setTitle(title).create();
        builder.setButton(DialogInterface.BUTTON_POSITIVE, allowPrompt,onClickListener);
        builder.setMessage(message);
        return builder;
    }
}
