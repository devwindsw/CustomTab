// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.chromium.customtabsdemos;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.NotificationCompat;

/**
 * This is the Activity that the Notification takes the user to.
 *
 * The Notification sends the user to this activity with an EXTRA_URL. Use it to call Chrome Custom
 * Tabs. The user will return to it when coming back from the web content.
 */
public class NotificationParentActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "NotificationParentActivity";

    private static final int NOTIFICATION_ID  = 1;
    public static final String EXTRA_URL = "extra.url";
    private static final String CT_NOTIFICATION_CHANNEL_ID = "999";

    private View mMessageTextView;
    private View mCreateNotificationButton;
    private CustomTabActivityHelper mCustomTabActivityHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate");
        setContentView(R.layout.activity_notification_parent);
        mMessageTextView = findViewById(R.id.back_to_app_message);
        mCreateNotificationButton = findViewById(R.id.create_notification);
        mCreateNotificationButton.setOnClickListener(this);
        mCustomTabActivityHelper = new CustomTabActivityHelper();
        startChromeCustomTab(getIntent());
    }

    /*@Override
    protected void onNewIntent(Intent intent) {
        log("onNewIntent intent=" + intent);
        super.onNewIntent(intent);
        startChromeCustomTab(intent);
    }*/

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.create_notification) {
            createAndShowNotification();
            finish();
        }
    }

    private void createAndShowNotification() {
        log("createAndShowNotification");
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = getApplicationContext().getPackageName() + "-" + CT_NOTIFICATION_CHANNEL_ID;
        NotificationChannel mChannel = notificationManager.getNotificationChannel(channelId);
        if (mChannel == null) {
            log("createAndShowNotification create channel");
            mChannel = new NotificationChannel(channelId,
                    "Custom Tab Demo app", NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.enableVibration(true);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CT_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_custom_tab_notification)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_text))
                        //.setOngoing(true)
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(
                this.getApplicationContext(), NotificationParentActivity.class);

        resultIntent.putExtra(
                NotificationParentActivity.EXTRA_URL, getString(R.string.notification_sample_url));

        resultIntent.setAction(Intent.ACTION_VIEW);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this.getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);

        mBuilder.setContentIntent(pendingIntent);
        // mId allows you to update the notification later on.
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        log("createAndShowNotification notified");
    }

    private void startChromeCustomTab(Intent intent) {
        log("startChromeCustomTab intent=" + intent);
        String url = intent.getStringExtra(EXTRA_URL);
        log("startChromeCustomTab url=" + url);
        if (url != null) {
            Uri uri = Uri.parse(url);

            /*int tabcolor = getResources().getColor(R.color.primaryColor);
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setToolbarColor(tabcolor)
                    .build();*/
            mCustomTabActivityHelper.openCustomTab(this, uri);

            //mMessageTextView.setVisibility(View.VISIBLE);
            //mCreateNotificationButton.setVisibility(View.GONE);
            finish();
        } else {
            mMessageTextView.setVisibility(View.GONE);
            mCreateNotificationButton.setVisibility(View.VISIBLE);
        }
    }

    private static void log(String msg) {
        Log.i(TAG, msg);
    }
}
