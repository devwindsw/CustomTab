// Copyright 2023 Google Inc. All Rights Reserved.
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

import static androidx.browser.customtabs.CustomTabsIntent.ACTIVITY_HEIGHT_ADJUSTABLE;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.browser.customtabs.CustomTabsCallback;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.browser.customtabs.EngagementSignalsCallback;

public class EngagementSignalsActivity extends AppCompatActivity
        implements View.OnClickListener, CustomTabActivityHelper.ConnectionCallback  {
    private static final String TAG = "EngagementSignals";

    private static final int INITIAL_HEIGHT_DEFAULT_PX = 600;
    private static final int CORNER_RADIUS_MAX_DP = 16;
    private static final int CORNER_RADIUS_DEFAULT_DP = CORNER_RADIUS_MAX_DP;

    private EditText mUrlEditText;
    private TextView mTextVerticalScroll;
    private TextView mTextGreatestPercentage;
    private TextView mTextSessionEnd;
    private TextView mTextNavigation;

    @Nullable
    private ServiceConnection mConnection;
    @Nullable
    private CustomTabsClient mClient;
    @Nullable
    private CustomTabsSession mCustomTabsSession;

    private CustomTabActivityHelper mCustomTabActivityHelper;

    private EngagementSignalsCallback mEngagementSignalsCallback = new EngagementSignalsCallback() {
        @Override
        public void onVerticalScrollEvent(boolean isDirectionUp, @NonNull Bundle extras) {
            log("onVerticalScrollEvent (isDirectionUp=" + isDirectionUp + ')');
            mTextVerticalScroll.setText("vertical scroll " + (isDirectionUp ? "UP️" : "DOWN"));
        }

        @Override
        public void onGreatestScrollPercentageIncreased(int scrollPercentage, @NonNull Bundle extras) {
            log("scroll percentage: " + scrollPercentage + "%");
            mTextGreatestPercentage.setText("scroll percentage: " + scrollPercentage + "%");
        }

        @Override
        public void onSessionEnded(boolean didUserInteract, @NonNull Bundle extras) {
            log("onSessionEnded (didUserInteract=" + didUserInteract + ')');
            mTextSessionEnd.setText(didUserInteract ? "session ended with user interaction" : "session ended without user interaction");
        }
    };

    private CustomTabsCallback mCustomTabsCallback = new CustomTabsCallback() {
        @Override
        public void onNavigationEvent(int navigationEvent, @Nullable Bundle extras) {
            String event;
            switch (navigationEvent) {
                case CustomTabsCallback.NAVIGATION_ABORTED:
                    event = "NAVIGATION_ABORTED";
                    break;
                case CustomTabsCallback.NAVIGATION_FAILED:
                    event = "NAVIGATION_FAILED";
                    break;
                case CustomTabsCallback.NAVIGATION_FINISHED:
                    event = "NAVIGATION_FINISHED";
                    break;
                case CustomTabsCallback.NAVIGATION_STARTED:
                    event = "NAVIGATION_STARTED";
                    // Scroll percentage and direction should be reset
                    mTextVerticalScroll.setText("vertical scroll: n/a");
                    mTextGreatestPercentage.setText("scroll percentage: n/a");
                    break;
                case CustomTabsCallback.TAB_SHOWN:
                    event = "TAB_SHOWN";
                    break;
                case CustomTabsCallback.TAB_HIDDEN:
                    event = "TAB_HIDDEN";
                    break;
                default:
                    event = String.valueOf(navigationEvent);
            }
            log("onNavigationEvent (navigationEvent=" + event + ')');
            mTextNavigation.setText("onNavigationEvent " + event);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engagement_signals);
        findViewById(R.id.start_custom_tab).setOnClickListener(this);

        mUrlEditText = findViewById(R.id.url);
        mTextGreatestPercentage = findViewById(R.id.label_event_greatest_percentage);
        mTextNavigation = findViewById(R.id.label_event_navigation);
        mTextSessionEnd = findViewById(R.id.label_event_session_ended);
        mTextVerticalScroll = findViewById(R.id.label_event_vertical_scroll);

        mCustomTabActivityHelper = new CustomTabActivityHelper();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCustomTabActivityHelper.setConnectionCallback(this);
        if (!mCustomTabActivityHelper.bindCustomTabsService(this)) {
            // Failed to request binding service.
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCustomTabActivityHelper.setConnectionCallback(null);
        mCustomTabActivityHelper.unbindCustomTabsService(this);
    }

    @Override
    public void onCustomTabsConnected() {
        log("onCustomTabsConnected");
        CustomTabsSession customTabsSession = mCustomTabActivityHelper.getSession(mCustomTabsCallback);
        try {
            boolean engagementSignalsApiAvailable = customTabsSession.isEngagementSignalsApiAvailable(Bundle.EMPTY);
            if (!engagementSignalsApiAvailable) {
                loge("CustomTab Engagement signals not available, make sure to use the " +
                        "latest Chrome version");
                return;
            }
            boolean signalsCallback = customTabsSession.setEngagementSignalsCallback(mEngagementSignalsCallback, Bundle.EMPTY);
            if (!signalsCallback) {
                loge("Could not set EngagementSignalsCallback");
            }
        } catch (RemoteException e) {
            loge("The Service died while responding to the request." + e);
        } catch (UnsupportedOperationException e) {
            loge("Engagement Signals API isn't supported by the browser." + e);
        }
    }

    @Override
    public void onCustomTabsDisconnected() {
        log("onCustomTabsDisconnected");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_custom_tab) {
            openCustomTab();
        }
    }

    private void openCustomTab() {
        log("openCustomTab");
        String url = mUrlEditText.getText().toString();

        int toolbarCornerRadiusDp = 16;
        int toolbarCornerRadiusPx =
                Math.round(toolbarCornerRadiusDp * getResources().getDisplayMetrics().density);
        mCustomTabActivityHelper.openPartialCustomTab(this,
                CustomTabsIntent.ACTIVITY_HEIGHT_DEFAULT, INITIAL_HEIGHT_DEFAULT_PX,
                toolbarCornerRadiusDp,toolbarCornerRadiusPx,
                true, Uri.parse(url));
    }

    private static void log(String msg) {
        Log.i(TAG, msg);
    }

    private static void loge(String msg) {
        Log.e(TAG, msg);
    }
}
