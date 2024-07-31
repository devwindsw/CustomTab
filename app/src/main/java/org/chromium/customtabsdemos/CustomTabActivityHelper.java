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

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsCallback;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;

import java.util.List;

/**
 * This is a helper class to manage the connection to the Custom Tabs Service.
 */
public class CustomTabActivityHelper implements ServiceConnectionCallback {
	private static final String TAG = "CustomTabActivityHelper";
	private static final boolean DEBUG = false;

    private static final int BACKGROUND_INTERACT_OFF_VALUE = 2;

    private CustomTabsSession mCustomTabsSession;
    private CustomTabsClient mClient;
    private CustomTabsServiceConnection mConnection;
    private ConnectionCallback mConnectionCallback;

    /**
     * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
     *
     * @param activity the host activity.
     * @param uri the Uri to be opened.
     */
    public void openSimpleCustomTab(Activity activity, Uri uri) {
        logd("openSimpleCustomTab");
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
        customTabsIntent.launchUrl(activity, uri);
    }

    /**
     * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
     *
     * @param activity the host activity.
     * @param resizeBehavior desired height behavior.
     * @param initialHeightDefaultPx initial height in pixels with default resize behavior.
     * @param toolbarCornerRadiusDp the toolbar's top corner radii in dp.
     * @param toolbarCornerRadiusPx the toolbar's top corner radii in pixels.
     * @param enableBackgroundInteract indicates whether enabling background interaction.
     * @param uri the Uri to be opened.
     */
    public void openPartialCustomTab(Activity activity,
                                     int resizeBehavior, int initialHeightDefaultPx,
                                     int toolbarCornerRadiusDp, int toolbarCornerRadiusPx,
                                     boolean enableBackgroundInteract, Uri uri) {
        log("openPartialCustomTab");
        // Uses the established session to build a PCCT intent.
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder(getSession(null));
        intentBuilder.setInitialActivityHeightPx(initialHeightDefaultPx, resizeBehavior);
        intentBuilder.setToolbarCornerRadiusDp(toolbarCornerRadiusDp);

        CustomTabsIntent customTabsIntent = intentBuilder.build();
        customTabsIntent.intent.putExtra(
                "androidx.browser.customtabs.extra.INITIAL_ACTIVITY_HEIGHT_IN_PIXEL",
                initialHeightDefaultPx);
        customTabsIntent.intent.putExtra(
                "androidx.browser.customtabs.extra.TOOLBAR_CORNER_RADIUS_IN_PIXEL",
                toolbarCornerRadiusPx);
        if (resizeBehavior != CustomTabsIntent.ACTIVITY_HEIGHT_DEFAULT) {
            customTabsIntent.intent.putExtra(
                    CustomTabsIntent.EXTRA_ACTIVITY_HEIGHT_RESIZE_BEHAVIOR, resizeBehavior);
        }
        if (!enableBackgroundInteract) {
            customTabsIntent.intent.putExtra(
                    "androix.browser.customtabs.extra.ENABLE_BACKGROUND_INTERACTION",
                    BACKGROUND_INTERACT_OFF_VALUE);
        }
        customTabsIntent.launchUrl(activity, uri);
    }

    /**
     * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
     *
     * @param activity the host activity.
     * @param customTabsIntent
     * @param uri the Uri to be opened.
     */
    public boolean openCustomTab(Activity activity, CustomTabsIntent customTabsIntent, Uri uri) {
        String packageName = CustomTabsHelper.getPackageNameToUse(activity);

        //If we cant find a package name, it means theres no browser that supports
        //Chrome Custom Tabs installed. So, we fallback to the webview
        if (packageName == null) {
            return false;
        }
        if (customTabsIntent == null) {
            customTabsIntent = new CustomTabsIntent.Builder(getSession(null)).build();
        }
        customTabsIntent.intent.setPackage(packageName);
        customTabsIntent.launchUrl(activity, uri);
        return true;
    }

    /**
     * Unbinds the Activity from the Custom Tabs Service.
     * @param activity the activity that is connected to the service.
     */
    public void unbindCustomTabsService(Activity activity) {
        if (mConnection == null) return;
        activity.unbindService(mConnection);
        mClient = null;
        mCustomTabsSession = null;
        mConnection = null;
    }

    /**
     * Creates or retrieves an exiting CustomTabsSession.
     *
     * @return a CustomTabsSession.
     */
    public CustomTabsSession getSession(CustomTabsCallback customTabsCallback) {
        if (mClient == null) {
            loge("getSession null client");
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            log("getSession get new session");
            mCustomTabsSession = mClient.newSession(customTabsCallback);
        }
        return mCustomTabsSession;
    }

    /**
     * Register a Callback to be called when connected or disconnected from the Custom Tabs Service.
     */
    public void setConnectionCallback(ConnectionCallback connectionCallback) {
        this.mConnectionCallback = connectionCallback;
    }

    /**
     * Binds the Activity to the Custom Tabs Service.
     * @param activity the activity to be binded to the service.
     * @return true if binding is requested successfully.
     */
    public boolean bindCustomTabsService(Activity activity) {
        if (mClient != null) {
        	loge("bindCustomTabsService non-null client");
        	return false;
        }

        String packageName = CustomTabsHelper.getPackageNameToUse(activity);
        log("bindCustomTabsService packageName=" + packageName);

        if (packageName == null) {
            return false;
        }
        mConnection = new ServiceConnection(this);
        CustomTabsClient.bindCustomTabsService(activity, packageName, mConnection);
        return true;
    }

    /**
     * @see {@link CustomTabsSession#mayLaunchUrl(Uri, Bundle, List)}.
     * @return true if call to mayLaunchUrl was accepted.
     */
    public boolean mayLaunchUrl(Uri uri, Bundle extras, List<Bundle> otherLikelyBundles) {
        if (mClient == null) {
        	loge("mayLaunchUrl null client");
        	return false;
        }

        CustomTabsSession session = getSession(null);
        if (session == null) {
            loge("mayLaunchUrl null session");
            return false;
        }

        boolean ret = session.mayLaunchUrl(uri, extras, otherLikelyBundles);
        log("mayLaunchUrl ret=" + ret);
        return ret;
    }

    @Override
    public void onServiceConnected(CustomTabsClient client) {
        log("onServiceConnected");
        mClient = client;
        mClient.warmup(0L);
        if (mConnectionCallback == null) {
        	loge("onServiceConnected null callback");
        } else {
        	mConnectionCallback.onCustomTabsConnected();
        }
    }

    @Override
    public void onServiceDisconnected() {
        log("onServiceDisonnected");
        mClient = null;
        mCustomTabsSession = null;
        if (mConnectionCallback == null) {
        	loge("onServiceDisonnected null callback");
        } else {
            mConnectionCallback.onCustomTabsDisconnected();
        }
    }

    /**
     * A Callback for when the service is connected or disconnected. Use those callbacks to
     * handle UI changes when the service is connected or disconnected.
     */
    public interface ConnectionCallback {
        /**
         * Called when the service is connected.
         */
        void onCustomTabsConnected();

        /**
         * Called when the service is disconnected.
         */
        void onCustomTabsDisconnected();
    }

    private static void log(String msg) {
        Log.i(TAG, msg);
    }

    private static void logd(String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }

    private static void loge(String msg) {
        Log.e(TAG, msg);
    }
}
