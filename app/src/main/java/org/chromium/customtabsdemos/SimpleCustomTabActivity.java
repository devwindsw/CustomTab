package org.chromium.customtabsdemos;

import static org.chromium.customtabsdemos.R.id.start_custom_tab;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

public class SimpleCustomTabActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mUrlEditText;
    private CustomTabActivityHelper mCustomTabActivityHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_custom_tab);

        findViewById(start_custom_tab).setOnClickListener(this);
        mCustomTabActivityHelper = new CustomTabActivityHelper();
        mUrlEditText = findViewById(R.id.url);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_custom_tab) {
            String url = mUrlEditText.getText().toString();
            mCustomTabActivityHelper.openSimpleCustomTab(this, Uri.parse(url));
        }
    }
}
