package com.example.web_view_clean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.http.SslError;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.KeyEvent;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.firebase.messaging.FirebaseMessaging;

import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private final String url = "웹앱 Url";
    private long backPressedTime = 0;
    private InterstitialAd mInterstitialAd;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /**
         * 인터넷 연결이 없을 때 알림
         * **/
        if(!isNetworkAvailable(this)){
            Toast toast = Toast.makeText(this, "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
            toast.show();

            ActivityCompat.finishAffinity(this);
        }

        // Firebase 토픽 생성
        FirebaseMessaging.getInstance().subscribeToTopic("토픽 이름");

        /**
         * Google AdMob Banner Start
         * **/
//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
//            }
//        });
//
//        AdView adView = new AdView(this);
//        adView.setAdSize(AdSize.BANNER);
//        adView.setAdUnitId("ca-app-pub-광고 Id/광고 Id");
//
//        // Google AdMob
//        AdView mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
        /**
         * Google AdMob Banner Finished
         * **/

        /**
         * Google AdMob Clickable Start
         * **/
//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {}
//        });
//        AdRequest adRequest = new AdRequest.Builder().build();
//
//        InterstitialAd.load(this,"ca-app-pub-광고 Id/광고 Id", adRequest,
//                new InterstitialAdLoadCallback() {
//                    @Override
//                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                        // The mInterstitialAd reference will be null until
//                        // an ad is loaded.
//                        mInterstitialAd = interstitialAd;
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        // Handle the error
//                        mInterstitialAd = null;
//                    }
//                });
        /**
         * Google AdMob Clickable Finished
         * **/

        // 캡쳐 방지
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        // 세로모드로 고정 + Manifest.xml portrait 추가 해야함
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        webView = findViewById(R.id.web_view_clean);

        // 웹뷰에서 자바 스크립트로 빌드 된 화면을 렌더링 할 수 있도록 설정
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        // 웹뷰가 동작할 때 호환되는 브라우저 설정 - Front 개발 환경에 맞게 Chrome으로 설정함
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClientClass());

        // 웹뷰 로드 시 캐시 및 히스토리를 삭제 해 웹 업데이트를 반영하도록 함
        webView.clearCache(true);
        webView.clearHistory();
        webView.loadUrl(url);

    }

    // 네트워크가 없을 때
    private Boolean isNetworkAvailable(MainActivity application) {
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
        return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
    }

    // 웹뷰에서 KeyDownEvent를 적용해 화면 라우트 조절 및 광고 create
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (mInterstitialAd != null) {
            mInterstitialAd.show(MainActivity.this);
        }

        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            // 로그인 혹은 메인 화면에서 뒤로가기 클릭 시 앱 종료 토스트 발생
            if(webView.getUrl().equals(url + "/") || webView.getUrl().equals(url + "토스트 발생 시킬 Url")) {
                long FINISH_INTERNAL_TIME = 2000;
                if (0 <= intervalTime && FINISH_INTERNAL_TIME >= intervalTime) finish();
                else {
                    backPressedTime = tempTime;
                    Toast.makeText(getApplicationContext(), "한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                webView.goBack();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private static class WebViewClientClass extends WebViewClient {

        /**
         * SSL 인증서 무시
         * https 설정 된 도메인 구입하지 않았을 때 사용.
         * 추후 도메인 구입시 삭제
         */
        @SuppressLint("WebViewClientOnReceivedSslError")
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(String.valueOf((request.getUrl())));
            return true;
        }
    }
}