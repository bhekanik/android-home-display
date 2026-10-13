package com.bhekanik.homedisplay;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Calendar;

public final class WallDisplayActivity extends Activity {
    private static final String TAG = "WallDisplay";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences preferences;
    private FrameLayout root;
    private LinearLayout errorPanel;
    private TextView errorMessage;
    private WebView webView;
    private ConnectivityManager connectivity;
    private boolean resumed;
    private boolean failed;
    private boolean dimmed;
    private boolean volumeHeld;
    private int attempts;
    private long awakeUntil;
    private final Runnable retry = this::loadDashboard;
    private final Runnable clock = new Runnable() {
        @Override public void run() {
            applyDisplayMode();
            if (resumed) handler.postDelayed(this, 15_000);
        }
    };
    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override public void onAvailable(Network network) {
            handler.post(() -> { if (resumed && failed) loadDashboard(); });
        }
        @Override public void onLost(Network network) {
            handler.post(() -> {
                if (resumed) showFailure("Connection lost. Retrying when the dashboard is reachable.", true);
            });
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        preferences = getSharedPreferences("display", MODE_PRIVATE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);
        createWebView();
        createErrorPanel();
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        connectivity = getSystemService(ConnectivityManager.class);
        connectivity.registerDefaultNetworkCallback(networkCallback);
        loadDashboard();
        if (preferences.getString("dashboard_url", "").isEmpty()) openSettings();
    }

    private void createWebView() {
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setUserAgentString(settings.getUserAgentString() + " WallDisplay/1");
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String scheme = request.getUrl().getScheme();
                return !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                failed = false;
                if (errorPanel != null) errorPanel.setVisibility(View.GONE);
            }
            @Override public void onPageFinished(WebView view, String url) {
                if (!failed) {
                    attempts = 0;
                    handler.removeCallbacks(retry);
                    errorPanel.setVisibility(View.GONE);
                    CookieManager.getInstance().flush();
                    Log.i(TAG, "Page load completed");
                }
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    Log.w(TAG, "Main page network error: " + error.getErrorCode());
                    showFailure("Cannot reach the dashboard. Check Wi-Fi and the server address.", true);
                }
            }
            @Override public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse response) {
                if (request.isForMainFrame()) {
                    int code = response.getStatusCode();
                    showFailure("The dashboard returned HTTP " + code + ".", code >= 500 || code == 408 || code == 429);
                }
            }
            @Override public void onReceivedSslError(WebView view, SslErrorHandler sslHandler, SslError error) {
                sslHandler.cancel();
                showFailure("The dashboard's HTTPS certificate could not be verified.", false);
            }
            @Override public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                Log.w(TAG, "WebView renderer exited; crashed=" + detail.didCrash());
                root.removeView(view);
                view.destroy();
                webView = null;
                showFailure("Restarting the dashboard display.", true);
                return true;
            }
        });
        root.addView(webView, 0, new FrameLayout.LayoutParams(-1, -1));
    }

    private void createErrorPanel() {
        errorPanel = new LinearLayout(this);
        errorPanel.setOrientation(LinearLayout.VERTICAL);
        errorPanel.setGravity(Gravity.CENTER);
        errorPanel.setPadding(32, 32, 32, 32);
        errorPanel.setBackgroundColor(Color.rgb(20, 25, 29));
        TextView heading = new TextView(this);
        heading.setText(R.string.app_name);
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(28);
        errorPanel.addView(heading);
        errorMessage = new TextView(this);
        errorMessage.setTextColor(Color.LTGRAY);
        errorMessage.setTextSize(18);
        errorMessage.setPadding(0, 16, 0, 24);
        errorPanel.addView(errorMessage);
        Button retryButton = new Button(this);
        retryButton.setText("Try again");
        retryButton.setOnClickListener(view -> loadDashboard());
        errorPanel.addView(retryButton);
        Button settingsButton = new Button(this);
        settingsButton.setText("Display settings");
        settingsButton.setOnClickListener(view -> openSettings());
        errorPanel.addView(settingsButton);
        errorPanel.setVisibility(View.GONE);
        root.addView(errorPanel, new FrameLayout.LayoutParams(-1, -1));
    }

    private void loadDashboard() {
        handler.removeCallbacks(retry);
        if (isFinishing() || isDestroyed()) return;
        if (webView == null) createWebView();
        failed = false;
        String address = preferences.getString("dashboard_url", getString(R.string.default_dashboard_url));
        if (address.trim().isEmpty()) {
            showFailure("Choose Display settings to enter your dashboard address.", false);
            return;
        }
        webView.loadUrl(address);
    }

    private void showFailure(String message, boolean retryAutomatically) {
        failed = true;
        errorMessage.setText(message);
        errorPanel.setVisibility(View.VISIBLE);
        handler.removeCallbacks(retry);
        if (retryAutomatically && resumed) {
            long delay = Math.min(60_000, 2_000L << Math.min(attempts++, 5));
            handler.postDelayed(retry, delay);
        }
    }

    private void applyDisplayMode() {
        boolean next = preferences.getBoolean("night_dim", true)
                && NightHours.isNight(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                && SystemClock.elapsedRealtime() >= awakeUntil;
        WindowManager.LayoutParams parameters = getWindow().getAttributes();
        parameters.screenBrightness = next ? 0.08f : 0.45f;
        getWindow().setAttributes(parameters);
        if (next == dimmed) return;
        dimmed = next;
        // Dimming must leave the dashboard visible and its data refreshing.
        Log.i(TAG, "Night dim=" + next);
    }

    @Override public void onUserInteraction() {
        super.onUserInteraction();
        awakeUntil = SystemClock.elapsedRealtime() + 120_000;
        applyDisplayMode();
    }

    private void hideSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void openSettings() {
        awakeUntil = SystemClock.elapsedRealtime() + 120_000;
        applyDisplayMode();
        DisplaySettingsDialog.show(this, preferences, () -> {
            applyDisplayMode();
            loadDashboard();
        });
    }

    @Override public void onResume() {
        super.onResume();
        resumed = true;
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
        hideSystemBars();
        handler.removeCallbacks(clock);
        clock.run();
        if (failed) loadDashboard();
    }

    @Override public void onPause() {
        resumed = false;
        handler.removeCallbacks(clock);
        handler.removeCallbacks(retry);
        if (webView != null) webView.onPause();
        CookieManager.getInstance().flush();
        super.onPause();
    }

    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused);
        if (focused) hideSystemBars();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.getRepeatCount() == 0) { event.startTracking(); volumeHeld = false; }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            volumeHeld = true;
            openSettings();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!volumeHeld) Log.d(TAG, "Hold Volume Up to open display settings");
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
    }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (connectivity != null) connectivity.unregisterNetworkCallback(networkCallback);
        if (webView != null) {
            webView.resumeTimers();
            root.removeView(webView);
            webView.destroy();
        }
        super.onDestroy();
    }
}
