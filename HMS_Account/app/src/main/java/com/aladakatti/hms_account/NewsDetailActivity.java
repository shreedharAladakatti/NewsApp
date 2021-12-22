package com.aladakatti.hms_account;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NewsDetailActivity extends AppCompatActivity {
    private static final String TAG = "NewsDetailActivity";
    WebView webView;
    String url;
    String discription;
    String title, date, source, author, img;
    Toolbar toolbar;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("News Details");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed();
            }
        });
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            url = bundle.getString("url");
            discription = bundle.getString("discription");
            title = bundle.getString("title");
            date = bundle.getString("date");
            source = bundle.getString("source");
            author = bundle.getString("author");
            img = bundle.getString("img");
        }


        webView = (WebView)findViewById(R.id.view_web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}