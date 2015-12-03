package com.cs180.ucrtinder.youwho.ui;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.cs180.ucrtinder.youwho.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.youwho.Instagram.InstagramApp;
import com.cs180.ucrtinder.youwho.Parse.ParseConstants;
import com.cs180.ucrtinder.youwho.Parse.YouWhoApplication;
import com.cs180.ucrtinder.youwho.R;
import com.parse.Parse;
import com.parse.ParseUser;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class IGWebActivity extends AppCompatActivity {

    private InstagramApp instaObj;
    private YouWhoApplication mApp;
    private String mRequestToken;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        AndroidDrawer drawer = new AndroidDrawer
                (this,R.id.drawer_layout_loading,R.id.left_drawer_loading, R.id.loading_drawer_pic);

        ParseUser currentUser = ParseUser.getCurrentUser();
        boolean isIGLoggedIn = currentUser.getBoolean(ParseConstants.KEY_IG_BOOL);
        if (!isIGLoggedIn) {
            InstagramApp.OAuthAuthenticationListener listener = new InstagramApp.OAuthAuthenticationListener() {
                @Override
                public void onSuccess() {
                    Log.e("Userid", instaObj.getId());
                    Log.e("Name", instaObj.getName());
                    Log.e("UserName", instaObj.getUserName());
                    instaObj.resetAccessToken();
                    instaObj.dismissDialog();
                    finish();
                }

                @Override
                public void onFail(String error) {
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                    finish();
                }
            };

            // Instagram Implementation
            instaObj = new InstagramApp(this, YouWhoApplication.IG_CLIENT_ID,
                    YouWhoApplication.IG_CLIENT_SECRET, YouWhoApplication.IG_CALLBACK_URL, this);
            instaObj.setListener(listener);

            instaObj.authorize();
        } else {
            Toast.makeText(getApplicationContext(), "Already logged into Instagram", Toast.LENGTH_SHORT).show();
            finish();
        }

        Log.e("HERE", "CHECK THIS");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_igweb, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
