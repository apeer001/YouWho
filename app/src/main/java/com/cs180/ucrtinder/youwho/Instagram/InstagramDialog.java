package com.cs180.ucrtinder.youwho.Instagram;

/**
 * Created by Aaron on 12/2/2015.
 */
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cs180.ucrtinder.youwho.R;

/**
 * Display 37Signals authentication dialog.
 *
 * @author Thiago Locatelli <thiago.locatelli@gmail.com>
 * @author Lorensius W. L T <lorenz@londatiga.net>
 *
 */
public class InstagramDialog extends Dialog {

    static final float[] DIMENSIONS_LANDSCAPE = { 720, 180 };
    static final float[] DIMENSIONS_PORTRAIT = { 420, 420 };
    static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    static final int MARGIN = 4;
    static final int PADDING = 2;

    private String mUrl;
    private OAuthDialogListener mListener;
    private ProgressDialog mSpinner;
    private WebView mWebView;
    private LinearLayout mContent;
    private TextView mTitle;
    private Activity mActivity;

    private static final String TAG = "Instagram-WebView";

    public InstagramDialog(Context context, String url,
                           OAuthDialogListener listener, Activity activity) {
        super(context);

        mUrl = url;
        mListener = listener;
        mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mSpinner = new ProgressDialog(getContext());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");
        //mContent = new LinearLayout(getContext());
        //mContent.setOrientation(LinearLayout.VERTICAL);

        setContentView(R.layout.activity_igweb);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setBackgroundDrawable(null);
        //setUpTitle();
        setUpWebView();


        //Display display = getWindow().getWindowManager().getDefaultDisplay();

        /*
        Point size = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        int height = size.y;
        final float scale = getContext().getResources().getDisplayMetrics().density;
        float[] dimensions = (width < height) ? DIMENSIONS_PORTRAIT : DIMENSIONS_LANDSCAPE;


        addContentView(mContent, new FrameLayout.LayoutParams(
                (int) (dimensions[0] * scale + 0.5f), (int) (dimensions[1]
                * scale + 0.5f)));
         */


        //CookieSyncManager.createInstance(getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.flush();
        cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {
                if (value) {
                    Log.d(getClass().getSimpleName(), "Removed all cookies");
                }
            }
        });
    }

    private void setUpTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mTitle = new TextView(getContext());
        mTitle.setText("Instagram");
        mTitle.setTextColor(Color.WHITE);
        mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mTitle.setBackgroundColor(Color.BLACK);
        mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
        mContent.addView(mTitle);
    }

    private void setUpWebView() {
        //mWebView = new WebView(getContext());
        mWebView = (WebView) this.findViewById(R.id.ig_webview);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new OAuthWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(FILL);
        //mContent.addView(mWebView);
    }

    private class OAuthWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Redirecting URL " + url);

            if (url.startsWith(InstagramApp.mCallbackUrl)) {
                String urls[] = url.split("=");
                mListener.onComplete(urls[1]);
                InstagramDialog.this.dismiss();
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.d(TAG, "Page error: " + description);

            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(description);
            InstagramDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "Loading URL: " + url);

            super.onPageStarted(view, url, favicon);
            mSpinner.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            /*
            String title = mWebView.getTitle();
            if (title != null && title.length() > 0) {
                mTitle.setText(title);
            }
            */
            Log.d(TAG, "onPageFinished URL: " + url);
            mSpinner.dismiss();
        }

    }

    void forceDismiss() {
        InstagramDialog.this.setCancelable(true);
        InstagramDialog.this.dismiss();
    }

    public interface OAuthDialogListener {
        public abstract void onComplete(String accessToken);

        public abstract void onError(String error);
    }
}