package com.sandklef.coachapp.report;

import android.widget.Toast;
import android.content.Context;

public class ReportUser {

    public static void warning(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }
    
}
