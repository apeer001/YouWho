package com.cs180.ucrtinder.youwho.Instagram;

/**
 * Created by Aaron on 12/2/2015.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.cs180.ucrtinder.youwho.Parse.ParseConstants;
import com.parse.ParseUser;

import java.net.URL;
import java.util.ArrayList;

/**
 * Manage access token and user name. Uses shared preferences to store access
 * token and user name.
 *
 * @author Thiago Locatelli <thiago.locatelli@gmail.com>
 * @author Lorensius W. L T <lorenz@londatiga.net>
 *
 */
public class InstagramSession {

    private SharedPreferences sharedPref;
    private Editor editor;

    private static final String SHARED = "Instagram_Preferences";
    private static final String API_USERNAME = "username";
    private static final String API_ID = "id";
    private static final String API_NAME = "name";
    private static final String API_ACCESS_TOKEN = "access_token";
    private static final String API_USER_IMAGE = "user_image";

    private ArrayList<String> photoUrls;

    public InstagramSession(Context context) {
        sharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        photoUrls = null;
    }

    /**
     *
     * @param accessToken
     * @param expireToken
     * @param expiresIn
     * @param username
     */
    public void storeAccessToken(String accessToken, String id,
                                 String username, String name, String image) {
        editor.putString(API_ID, id);
        editor.putString(API_NAME, name);
        editor.putString(API_ACCESS_TOKEN, accessToken);
        editor.putString(API_USERNAME, username);
        editor.putString(API_USER_IMAGE, image);
        editor.commit();
    }

    public void storeAccessToken(String accessToken) {
        editor.putString(API_ACCESS_TOKEN, accessToken);
        editor.commit();
    }

    public void storeRecentUserPhotos(ArrayList<String> urls) {
       photoUrls = urls;
    }

    /**
     * Reset access token and user name
     */
    public void resetAccessToken() {
        editor.putString(API_ID, null);
        editor.putString(API_NAME, null);
        editor.putString(API_ACCESS_TOKEN, null);
        editor.putString(API_USERNAME, null);
        editor.putString(API_USER_IMAGE, null);
        editor.commit();
    }

    /**
     * Get user name
     *
     * @return User name
     */
    public String getUsername() {
        return sharedPref.getString(API_USERNAME, null);
    }

    /**
     *
     * @return
     */
    public String getId() {
        return sharedPref.getString(API_ID, null);
    }

    /**
     *
     * @return
     */
    public String getName() {
        return sharedPref.getString(API_NAME, null);
    }

    /**
     * Get access token
     *
     * @return Access token
     */
    public String getAccessToken() {
        return sharedPref.getString(API_ACCESS_TOKEN, null);
    }

    /**
     * Get userImage
     *
     * @return userImage
     */
    public String getUserImage() {
        return sharedPref.getString(API_USER_IMAGE, null);
    }

    public ArrayList<String> getRecentUserPhotos() {
        return photoUrls;
    }

    public void saveToParse() {
        if (photoUrls != null && !photoUrls.isEmpty()) {
            Log.d(getClass().getSimpleName(), "Saving the photos to parse");
            ParseUser currentUser = ParseUser.getCurrentUser();
            currentUser.put(ParseConstants.KEY_IG_PHOTOLIST, photoUrls);
            if(getId() != null) {
                currentUser.put(ParseConstants.KEY_IG_API_ID, getId());
            }
            if (getAccessToken() != null) {
                currentUser.put(ParseConstants.KEY_IG_USER_ACCESSTOKEN, getAccessToken());
            }
            currentUser.put(ParseConstants.KEY_IG_BOOL, true);
            // Save
            currentUser.saveInBackground();
        }
    }
}