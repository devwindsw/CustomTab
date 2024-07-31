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

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This Activity connect to the Chrome Custom Tabs Service on startup, and allows you to decide
 * when to call mayLaunchUrl.
 */
public class ServiceConnectionActivity extends AppCompatActivity
        implements View.OnClickListener, CustomTabActivityHelper.ConnectionCallback {
    private static final String TAG = "CustomTabActivityHelper";

    private EditText mUrlEditText;
    private View mMayLaunchUrlButton;
    private CustomTabActivityHelper mCustomTabActivityHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serviceconnection);

        mUrlEditText = findViewById(R.id.url);
        mMayLaunchUrlButton = findViewById(R.id.may_launch_url);
        mMayLaunchUrlButton.setEnabled(false);
        mMayLaunchUrlButton.setOnClickListener(this);

        findViewById(R.id.start_custom_tab).setOnClickListener(this);

        mCustomTabActivityHelper = new CustomTabActivityHelper();
        mCustomTabActivityHelper.setConnectionCallback(this);
        if (!mCustomTabActivityHelper.bindCustomTabsService(this)) {
            // Failed to request binding service.
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCustomTabActivityHelper.setConnectionCallback(null);
        mCustomTabActivityHelper.unbindCustomTabsService(this);
    }

    @Override
    public void onCustomTabsConnected() {
        log("onCustomTabsConnected");
        mMayLaunchUrlButton.setEnabled(true);
    }

    @Override
    public void onCustomTabsDisconnected() {
        log("onCustomTabsDisconnected");
        mMayLaunchUrlButton.setEnabled(false);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        Uri uri  = Uri.parse(mUrlEditText.getText().toString());
        if (viewId == R.id.may_launch_url) {
            log("onClick mayLaunchUrl");
            mCustomTabActivityHelper.mayLaunchUrl(uri, null, null);
        } else if (viewId == R.id.start_custom_tab) {
            log("onClick openCustomTab");
            mCustomTabActivityHelper.openCustomTab(this, uri);
        }
    }

    private static void log(String msg) {
        Log.i(TAG, msg);
    }
}
