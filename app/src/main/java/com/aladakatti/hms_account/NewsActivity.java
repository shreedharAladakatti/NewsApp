package com.aladakatti.hms_account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.SearchManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.aladakatti.hms_account.adapter.News_categories;
import com.aladakatti.hms_account.api.ApiClient;
import com.aladakatti.hms_account.api.ApiInterface;
import com.aladakatti.hms_account.models.Article;
import com.aladakatti.hms_account.models.News;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.huawei.agconnect.crash.AGConnectCrash;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.InterstitialAd;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;
import com.huawei.hms.analytics.type.ReportPolicy;
import com.huawei.hms.common.ApiException;

public class NewsActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener,
        News_categories.ItemClickListener{

    private static final String TAG = "NewsActivity";

    public static final String API_KEY = "Add your API key";
    private RecyclerView recyclerView;
    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private TextView topHeadline;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout errorLayout;
    private ImageView errorImage;
    private TextView errorTitle, errorMessage;
    private Button btnRetry;
    String email, pic, name;
    ArrayList<String> categoryList;
    private BannerView bannerView;
    private BannerView defaultBannerView;
    private static final int REFRESH_TIME = 60;
    private InterstitialAd interstitialAd;
    HiAnalyticsInstance instance;
    private MyReceiver  receiver;
    private final static String PUSH_ACTION = "com.huawei.push.action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        AGConnectCrash.getInstance().enableCrashCollection(true);
        getCategories();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            email = bundle.getString("email");
            pic = bundle.getString("pic");
            name = bundle.getString("name");
        }
        // Initialize the HUAWEI Ads SDK.
        HwAds.init(this);

        HiAnalyticsTools.enableLog();
        instance = HiAnalytics.getInstance(this);
        instance.setUserProfile("email",email);
        Set_Event_Reporting_Policies();

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(NewsActivity.this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        topHeadline = findViewById(R.id.topheadelines);
        recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(NewsActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        errorLayout = findViewById(R.id.errorLayout);
        errorImage = findViewById(R.id.errorImage);
        errorTitle = findViewById(R.id.errorTitle);
        errorMessage = findViewById(R.id.errorMessage);
        btnRetry = findViewById(R.id.btnRetry);
        defaultBannerView = findViewById(R.id.hw_banner_view);

        onLoadingSwipeRefresh("");
        loadDefaultBannerAd();

        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PUSH_ACTION);
        registerReceiver(receiver, filter);

        getToken();

    }

    public void getCategories(){
        // data to populate the RecyclerView with
        categoryList = new ArrayList<>();
        categoryList.add("Business");
        categoryList.add("Entertainment");
        categoryList.add("General");
        categoryList.add("Health");
        categoryList.add("Science");
        categoryList.add("Sports");
        categoryList.add("Technology");

        // set up the RecyclerView
        RecyclerView recyclerView_categories = findViewById(R.id.recyclerView_categories);
        recyclerView_categories.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        News_categories adapter_category = new News_categories(this, categoryList);
        adapter_category.setClickListener(this);
        recyclerView_categories.setAdapter(adapter_category);
        adapter_category.notifyDataSetChanged();

    }

    public void Load_NewsData(final String keyword){

        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        String country = Utils.getCountry();
        String language = Utils.getLanguage();

        Call<News> call;
        if (keyword.length() > 0 ){
            call = apiInterface.getNewsSearch(keyword, language, "publishedAt", API_KEY);
        } else {
            call = apiInterface.getNews(country, "",  API_KEY);
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(@NonNull Call<News> call, @NonNull Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null){

                    if (!articles.isEmpty()){
                        articles.clear();
                    }

                    articles = response.body().getArticle();
                    adapter = new Adapter(articles, NewsActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();

                    topHeadline.setVisibility(View.VISIBLE);
                    topHeadline.setText(R.string.top_headlines);
                    swipeRefreshLayout.setRefreshing(false);


                } else {

                    topHeadline.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = getString(R.string.not_found);
                            break;
                        case 500:
                            errorCode = getString(R.string.server_down);
                            break;
                        default:
                            errorCode = getString(R.string.unknown_error);
                            break;
                    }

                    showErrorMessage(
                            R.drawable.no_content,
                            getString(R.string.no_result),
                            getString(R.string.try_again)+
                                    errorCode);

                }
            }

            @Override
            public void onFailure(@NonNull Call<News> call, @NonNull Throwable t) {
                topHeadline.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                showErrorMessage(
                        R.drawable.ic_awkward,
                        "Oops..",
                        "Network failure, Please Try Again\n"+
                                t.toString());
            }
        });

    }

    private void initListener(){
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                loadInterstitialAd();
                Intent intent = new Intent(NewsActivity.this, NewsDetailActivity.class);
                Article article = articles.get(position);
                intent.putExtra("url", article.getUrl());
                intent.putExtra("title", article.getTitle());
                intent.putExtra("img",  article.getUrlToImage());
                intent.putExtra("date",  article.getPublishedAt());
                intent.putExtra("source",  article.getSource().getName());
                intent.putExtra("author",  article.getAuthor());
                intent.putExtra("discription",  article.getDescription());

                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getString(R.string.search_news));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2){
                    onLoadingSwipeRefresh(query);
                }
                else {
                    Toast.makeText(NewsActivity.this,
                            getResources().getString(R.string.more_char),
                            Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchMenuItem.getIcon().setVisible(false, false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.profile) {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("name", name);
            i.putExtra("pic", pic);
            i.putExtra("email", email);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        Load_NewsData("");
    }

    private void onLoadingSwipeRefresh(final String keyword){
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        Load_NewsData(keyword);
                    }
                }
        );

    }

    private void showErrorMessage(int imageView, String title, String message){

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
        }

        errorImage.setImageResource(imageView);
        errorTitle.setText(title);
        errorMessage.setText(message);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoadingSwipeRefresh("");
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "Position " +categoryList.get(position), Toast.LENGTH_SHORT).show();
        topHeadline.setText(categoryList.get(position));
        getNewsFromCategory(categoryList.get(position));
    }

    public void getNewsFromCategory(String category){

        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        String country = Utils.getCountry();

        Call<News> call = apiInterface.getNews(country, category,  API_KEY);

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(@NonNull Call<News> call, @NonNull Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null){

                    if (!articles.isEmpty()){
                        articles.clear();
                    }

                    articles = response.body().getArticle();
                    adapter = new Adapter(articles, NewsActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();

                    topHeadline.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);


                } else {

                    topHeadline.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = getString(R.string.not_found);
                            break;
                        case 500:
                            errorCode = getString(R.string.server_down);
                            break;
                        default:
                            errorCode = getString(R.string.unknown_error);
                            break;
                    }
                    showErrorMessage(
                            R.drawable.no_content,
                            getString(R.string.no_result),
                            getString(R.string.try_again)+
                                    errorCode);

                }
            }

            @Override
            public void onFailure(@NonNull Call<News> call, @NonNull Throwable t) {
                topHeadline.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                showErrorMessage(
                        R.drawable.ic_awkward,
                        "Oops..",
                        "Network failure, Please Try Again\n"+
                                t.toString());
            }
        });

    }

    /**
     * Load the default banner ad.
     */
    private void loadDefaultBannerAd() {
        // Obtain BannerView based on the configuration in layout/activity_main.xml.
        defaultBannerView.setBannerRefresh(REFRESH_TIME);

        AdParam adParam = new AdParam.Builder().build();
        defaultBannerView.loadAd(adParam);
    }

    private void loadInterstitialAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdId(this.getResources().getString(R.string.image_ad_id));
        interstitialAd.setAdListener(adListener);
        AdParam adParam = new AdParam.Builder().build();
        interstitialAd.loadAd(adParam);
    }

    private final AdListener adListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            showToast("Ad loaded");

            // Display an interstitial ad.
            showInterstitial();
        }

        @Override
        public void onAdFailed(int errorCode) {
            showToast("Ad load failed with error code: " + errorCode);
            Log.d(TAG, "Ad load failed with error code: " + errorCode);
        }

        @Override
        public void onAdClosed() {
            super.onAdClosed();
            showToast("Ad closed");
            Log.d(TAG, "onAdClosed");
        }

        @Override
        public void onAdClicked() {
            Log.d(TAG, "onAdClicked");
            super.onAdClicked();
        }

        @Override
        public void onAdOpened() {
            Log.d(TAG, "onAdOpened");
            super.onAdOpened();
        }
    };

    private void showInterstitial() {
        // Display an interstitial ad.
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show(this);
        } else {
            showToast("Ad did not load");
        }
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NewsActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void Set_Event_Reporting_Policies(){
        //For more information - https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-accessing-0000001050161888
        // Create a policy that is used to report an event upon app switching to the background.
        ReportPolicy moveBackgroundPolicy = ReportPolicy.ON_MOVE_BACKGROUND_POLICY;
        // Create a policy that is used to report an event at the specified interval.
        ReportPolicy scheduledTimePolicy = ReportPolicy.ON_SCHEDULED_TIME_POLICY;
        // Set the event reporting interval to 600 seconds.
        scheduledTimePolicy.setThreshold(600);
        Set<ReportPolicy> reportPolicies = new HashSet<>();
        // Add the ON_SCHEDULED_TIME_POLICY and ON_MOVE_BACKGROUND_POLICY policies.
        reportPolicies.add(scheduledTimePolicy);
        reportPolicies.add(moveBackgroundPolicy);
        // Set the ON_MOVE_BACKGROUND_POLICY and ON_SCHEDULED_TIME_POLICY policies.
        instance.setReportPolicies(reportPolicies);
    }

    /**
     * getToken(String appId, String scope), This method is used to obtain a token required for accessing HUAWEI Push Kit.
     * If there is no local AAID, this method will automatically generate an AAID when it is called because the Huawei Push server needs to generate a token based on the AAID.
     * This method is a synchronous method, and you cannot call it in the main thread. Otherwise, the main thread may be blocked.
     */
    private void getToken() {
        Log.d(TAG, "getToken: begin");
        new Thread() {
            @Override
            public void run() {
                try {
                    // read from agconnect-services.json
                    String appId = "104841155";
                    String token = HmsInstanceId.getInstance(NewsActivity.this).getToken(appId, "HCM");
                    Log.i(TAG, "get token:" + token);
                    if(!TextUtils.isEmpty(token)) {
                        sendRegTokenToServer(token);
                    }
                    Log.d(TAG, "getToken: token "+token);
                } catch (ApiException e) {
                    Log.e(TAG, "get token failed, " + e);
                }
            }
        }.start();
    }

    private void sendRegTokenToServer(String token) {
        Log.i(TAG, "sending token to server. token:" + token);
    }

    /**
     * MyReceiver
     */
    public static class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.getString("msg") != null) {
                String content = bundle.getString("msg");
                Toast.makeText(context, "Content " +content, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
