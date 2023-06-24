package com.app.imagedownloader.framework.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class sendEmailIntent {
    public void sendIntent(String email, String subject, String body, Context contenxt) {
        String[] recipients = email.split(",");
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Insta Story Tale Feedback : " + subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.setData(Uri.parse("mailto:"));
        contenxt.startActivity(Intent.createChooser(intent, "Choose an email client"));
    }
}
