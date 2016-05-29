package com.sandklef.coachapp.report;

import android.widget.Toast;
import android.content.Context;

import com.sandklef.coachapp.misc.Log;
import com.sandklef.coachapp.storage.Storage;

public class ReportUser {

    public static void warning(Context context, String text, String detail) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
        log(text, detail);
    }

    public static void inform(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void log(String msg, String detail) {
        Storage.getInstance().log(msg, detail);
    }

    // TODO: remove me
    public static void Log(String msg, String detail) {
        Storage.getInstance().log(msg, detail);
    }

}
