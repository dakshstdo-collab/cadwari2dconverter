package com.advocate.geetanjali.gupta.app.cadwari2dconverter;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Privacy Policy");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Load privacy policy HTML from assets
        WebView webView = new WebView(this);
        webView.loadUrl("file:///android_asset/privacy_policy.html");
        webView.getSettings().setDefaultTextEncodingName("utf-8");

        // Replace the placeholder TextView with the WebView
        LinearLayout layout = findViewById(R.id.privacy_layout);
        layout.addView(webView);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}