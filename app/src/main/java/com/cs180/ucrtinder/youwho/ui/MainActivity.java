package com.cs180.ucrtinder.youwho.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cs180.ucrtinder.youwho.FragmentSupport.NavigationListener;
import com.cs180.ucrtinder.youwho.FragmentSupport.OnCardsLoadedListener;
import com.cs180.ucrtinder.youwho.Messenger.AtlasMessagesScreen;
import com.cs180.ucrtinder.youwho.Parse.ParseConstants;
import com.cs180.ucrtinder.youwho.Services.GeoLocationService;
import com.cs180.ucrtinder.youwho.tindercard.Data;
import com.cs180.ucrtinder.youwho.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.youwho.R;
import com.cs180.ucrtinder.youwho.tindercard.FlingCardListener;
import com.cs180.ucrtinder.youwho.tindercard.SwipeFlingAdapterView;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements FlingCardListener.ActionDownInterface {

    public static MyAppAdapter myAppAdapter;
    public static ViewHolder viewHolder;
    private ParseUser user;
    private List<ParseUser> candidates;
    private ArrayList<Data> al;
    private SwipeFlingAdapterView flingContainer;
    private AndroidDrawer mAndroidDrawer;
    private Toolbar mToolbar;
    int currentCandidate = 0;
    MainActivity mActivity;
    private AtomicInteger mCounter = new AtomicInteger(0);
    private AtomicInteger mLimit;
    String newUserId = null;

    private OnCardsLoadedListener mCardsCompleteListener;

    private Button likebtn;
    private Button dislikebtn;

    public static final String CARD_BUNDLE = "cardBundle";
    public static final String CARD_USER = "cardUser";
    public static final String CARD_NAME = "cardName";
    public static final String CARD_ID = "cardID";

    public static final String CANDIDATE_ID = "candidateId";
    public static final String MATCHED_BUNDLE = "matchedBundle";

    public static final String GEO_PREF = "geolocationStatus";
    public static final String GEO_BOOL = "geolocationBool";
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    Intent geolocationIntent;

    private boolean mIsMatch = false;

    public static void removeBackground() {
        viewHolder.background.setVisibility(View.GONE);
        myAppAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            ParseUser curruser = ParseUser.getCurrentUser();
            if (curruser == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (NullPointerException n) {

        }

        mActivity = this;

        // Start Location when this activity is open
        geolocationIntent = new Intent(this, GeoLocationService.class);
        if (!checkIfLocationEnabled()) {
            //Log.d(getClass().getSimpleName(), "Started the geolocation service");
            requestionLocationPermisson();
        } else {
            startService(geolocationIntent);
        }



        mAndroidDrawer = new AndroidDrawer(this, R.id.drawer_layout_main, R.id.left_drawer_main, R.id.main_profile_drawer_pic);

        //Set up toolbar
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.mipmap.ic_drawer);
        mToolbar.setNavigationOnClickListener(new NavigationListener(mAndroidDrawer));
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException np) {
            //np.printStackTrace();
        }

        // Builds Fling card container
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        pullCandidates();
    }


    @Override
    public void onResume(){
        super.onResume();
        //getCandidates();
        //Log.d(getClass().getSimpleName(), "Stopped the geolocation service");
        // Start geolocation update service
        startService(new Intent(this, GeoLocationService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.d(getClass().getSimpleName(), "Stopped the geolocation service");
        // Stop geolocation update service
        stopService(new Intent(this, GeoLocationService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.d(getClass().getSimpleName(), "Stopped the geolocation service");
        // Stop geolocation update service
        stopService(new Intent(this, GeoLocationService.class));
    }

    public void pullCandidates(){
        mCardsCompleteListener = new OnCardsLoadedListener() {
            @Override
            public void onCardsLoaded() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mCounter.addAndGet(1) == mLimit.get()) {
                            myAppAdapter = new MyAppAdapter(al, MainActivity.this);
                            flingContainer.setAdapter(myAppAdapter);
                            myAppAdapter.notifyDataSetChanged();
                            setContainerListeners();
                        }
                    }
                });
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    user = ParseUser.getCurrentUser();
                } catch (NullPointerException n) {
                    //n.printStackTrace();
                }

                al = new ArrayList<>();
                candidates = getCandidates();
                /*
                if (candidates == null) {
                    return;
                }
                */
            }
        });
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    void setContainerListeners(){

        flingContainer.setFlingListener(new CardSwipeListener());

        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.background).setAlpha(0);

                myAppAdapter.notifyDataSetChanged();
                //  Toast.makeText(getApplicationContext(), "Clicked card", Toast.LENGTH_SHORT).show();

                Intent cardProfileIntent = new Intent(MainActivity.this, CardProfileActivity.class);
                Bundle b = new Bundle();

                // Get card user parse String
                b.putString(CARD_USER, al.get(itemPosition).getUserString());
                b.putString(CARD_ID, al.get(itemPosition).getID());
                cardProfileIntent.putExtra(CARD_BUNDLE, b);

                startActivity(cardProfileIntent);
            }
        });

        // Set buttons on main activity
        likebtn = (Button) findViewById(R.id.likebtn);
        dislikebtn = (Button) findViewById(R.id.dislikebtn);


        likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!al.isEmpty()) {
                    View view = flingContainer.getSelectedView();
                    view.findViewById(R.id.background).setAlpha(0);
                    view.findViewById(R.id.item_swipe_left_indicator).setAlpha(1);
                    flingContainer.getTopCardListener().selectRight();
                    myAppAdapter.notifyDataSetChanged();
                }
            }
        });

        dislikebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!al.isEmpty()) {
                    View view = flingContainer.getSelectedView();
                    view.findViewById(R.id.background).setAlpha(0);
                    view.findViewById(R.id.item_swipe_right_indicator).setAlpha(1);
                    flingContainer.getTopCardListener().selectLeft();
                    myAppAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onActionDownPerform() {
        //Log.e("action", "bingo");
    }

    public static class ViewHolder {
        public static FrameLayout background;
        public TextView DataText;
        public ImageView cardImage;
        public TextView mutualFriendText;

    }

    public class MyAppAdapter extends BaseAdapter {


        public List<Data> parkingList;
        public Context context;

        private MyAppAdapter(List<Data> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
        }

        @Override
        public int getCount() {
            return parkingList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.item, parent, false);
                // configure view holder
                viewHolder = new ViewHolder();
                viewHolder.DataText = (TextView) rowView.findViewById(R.id.bookText);
                viewHolder.background = (FrameLayout) rowView.findViewById(R.id.background);
                viewHolder.cardImage = (ImageView) rowView.findViewById(R.id.cardImage);
                viewHolder.mutualFriendText = (TextView) rowView.findViewById(R.id.mutual_friend_count);

                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Update texts
            String dataText = parkingList.get(position).getDescription() + "";
            String mutualText = viewHolder.mutualFriendText.getText().toString() + " " + parkingList.get(position).getMutualFriendCount();
            viewHolder.DataText.setText(dataText);
            viewHolder.mutualFriendText.setText(mutualText);

            Glide.with(MainActivity.this).load(parkingList.get(position).getImagePath()).into(viewHolder.cardImage);

            return rowView;
        }
    }

    public List<ParseUser> getCandidates()  {

//        List<ParseUser> likes = new ArrayList<ParseUser>();
//        user.put("likes", likes);
//        user.put("matches",likes);
//        user.put("dislikes",likes);
//        user.saveInBackground();

        // local variables
        if (user != null) {
            final String gender = user.getString(ParseConstants.KEY_GENDER);
            boolean men = user.getBoolean(ParseConstants.KEY_MEN);
            boolean women = user.getBoolean(ParseConstants.KEY_WOMEN);
            final int age = (int) user.getNumber(ParseConstants.KEY_AGE);
            int minAge = (int) user.getNumber(ParseConstants.KEY_SMALLESTAGE);
            int maxAge = (int) user.getNumber(ParseConstants.KEY_LARGESTAGE);
            int maxDist = (int) user.getNumber(ParseConstants.KEY_DISTANCE);
            String id = user.getString(ParseConstants.KEY_ID);

            final ParseGeoPoint location = user.getParseGeoPoint(ParseConstants.KEY_LOCATION);

            // set up query
            ParseQuery<ParseUser> mainQuery = ParseUser.getQuery();

            if (men && women) {
            } else if (men) {
                mainQuery.whereEqualTo(ParseConstants.KEY_GENDER, "male");
            } else if (women) {
                mainQuery.whereEqualTo(ParseConstants.KEY_GENDER, "female");
            }
            mainQuery.whereNotEqualTo(ParseConstants.KEY_OBJECTID, user.getObjectId());
            mainQuery.whereGreaterThanOrEqualTo(ParseConstants.KEY_AGE, minAge);
            mainQuery.whereLessThanOrEqualTo(ParseConstants.KEY_AGE, maxAge);
            mainQuery.whereWithinMiles(ParseConstants.KEY_LOCATION, location, maxDist);


            // further filter candidates
            try {
                mainQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> list, ParseException e) {
                        if (e != null) {
                            return;
                        }
                        candidates = list;

                        // remove likes, dislikes, matches
                        List<ParseUser> likes = user.getList(ParseConstants.KEY_LIKES);
                        List<ParseUser> dislikes = user.getList(ParseConstants.KEY_DISLIKES);
                        List<ParseUser> matches = user.getList(ParseConstants.KEY_MATCHES);

                        for(int j=0; j<likes.size(); j++){
                            ParseUser u = likes.get(j);
                            for(int i=0; i<candidates.size(); i++){
                                if(u.hasSameId(candidates.get(i))){
                                    candidates.remove(i);
                                }
                            }
                        }
                        for(int j=0; j<dislikes.size(); j++){
                            ParseUser u = dislikes.get(j);
                            for(int i=0; i<candidates.size(); i++){
                                if(u.hasSameId(candidates.get(i))){
                                    candidates.remove(i);
                                }
                            }
                        }
                        for(int j=0; j<matches.size(); j++){
                            ParseUser u = matches.get(j);
                            for(int i=0; i<candidates.size(); i++){
                                if(u.hasSameId(candidates.get(i))){
                                    candidates.remove(i);
                                }
                            }
                        }


                        mLimit = new AtomicInteger(candidates.size());
                        for (int i = 0; i < candidates.size(); ++i) {

                            if (candidates.get(i).getString(ParseConstants.KEY_FACEBOOKID) == null) {
                                mLimit.decrementAndGet();
                            }

                            al.add(new Data(
                                    candidates.get(i).getString(ParseConstants.KEY_PHOTO0),
                                    candidates.get(i).getString(ParseConstants.KEY_NAME) + ", " + candidates.get(i).getNumber(ParseConstants.KEY_AGE),
                                    candidates.get(i).getObjectId(),
                                    candidates.get(i).getString(ParseConstants.KEY_FACEBOOKID),
                                    mCardsCompleteListener,
                                    i,
                                    candidates.get(i).getString(ParseConstants.KEY_LAYERID)));
                        }



                        Iterator<ParseUser> it = candidates.iterator();
                        while (it.hasNext()) {
                            ParseUser candidate = it.next();
                            if (age < (int) candidate.getNumber(ParseConstants.KEY_SMALLESTAGE) || age > (int) candidate.getNumber(ParseConstants.KEY_LARGESTAGE) ||
                                    location.distanceInMilesTo(candidate.getParseGeoPoint(ParseConstants.KEY_LOCATION)) > (int) candidate.getNumber(ParseConstants.KEY_DISTANCE) ||
                                    (gender.equals("male") && !candidate.getBoolean(ParseConstants.KEY_MEN)) ||
                                    (gender.equals("female") && !candidate.getBoolean(ParseConstants.KEY_WOMEN))) {
                                it.remove();
                            }
                        }

                        if (candidates == null) {
                            candidates = new ArrayList<>();
                        }
                    }
                });

            } catch (Exception e) {
                return null;
            }
        }
        return candidates;
    }

    private class CardSwipeListener implements SwipeFlingAdapterView.onFlingListener{
        @Override
        public void removeFirstObjectInAdapter() {

        }

        @Override
        public void onLeftCardExit(Object dataObject) {

            if (!al.isEmpty()) {
                al.remove(0);
                myAppAdapter.notifyDataSetChanged();

                List<ParseUser> dislikes = user.getList(ParseConstants.KEY_DISLIKES);
                int i = currentCandidate;
                i += 1;
                if (i < candidates.size()) {
                    dislikes.add(candidates.get(currentCandidate++));
                }
                user.put("dislikes", dislikes);
                try {
                    user.saveInBackground();
                }
                catch(Exception e){
                    //e.printStackTrace();
                }
            }
        }

        @Override
        public void onRightCardExit(final Object dataObject) {

            if (!al.isEmpty()) {
                final Data data = al.get(0);
                al.remove(0);
                myAppAdapter.notifyDataSetChanged();


                // Start of executor service
                List<ParseUser> likes = user.getList(ParseConstants.KEY_LIKES);
                int i = currentCandidate;
                i += 1;
                if (i < candidates.size()) {
                    likes.add(candidates.get(currentCandidate));
                }
                user.put("likes", likes);
                user.saveInBackground();

                if (currentCandidate < candidates.size()) {

                    List<ParseUser> targetlikes = candidates.get(currentCandidate).getList(ParseConstants.KEY_LIKES);

//                    // This is here for testing. Its for the adding of new conversations
//                    ParseUser tempCandidate;
//                    Iterator<ParseUser> candidateIter1 = candidates.iterator();
//                    int index1 = 0;
//                    while (candidateIter1.hasNext() && candidates.size() > currentCandidate) {
//                        if (index1 == currentCandidate) {
//                            break;
//                        }
//                        candidateIter1.next();
//                        index1++;
//                    }
//                    tempCandidate = candidateIter1.next();
//                    // -- end --

                    boolean exists = false;

                    for(int j=0; j<targetlikes.size(); j++){//ParseUser u : targetlikes){
                        ParseUser u = targetlikes.get(j);
                        if(u.hasSameId(user)){
                           exists = true;
                        }
                    }

                    //if (targetlikes.contains(user)) {
                    if(exists){
                        // Get the current Candidate
                        Iterator<ParseUser> candidateIter = candidates.iterator();
                        int index = 0;
                        while (candidateIter.hasNext() && candidates.size() > currentCandidate) {
                            if (index == currentCandidate) {
                                break;
                            }
                            candidateIter.next();
                            index++;
                        }
                        ParseUser currCandidate = candidateIter.next();

                        // Get matches list for current user and candidate
                        List<ParseUser> matches = user.getList(ParseConstants.KEY_MATCHES);
                        List<ParseUser> targetMatches = currCandidate.getList(ParseConstants.KEY_MATCHES);
                        //List<ParseUser> targetMatches = candidates.get(currentCandidate).getList("matches");

                        // Update each list for matched likes
                        matches.add(currCandidate);
                        //matches.add(candidates.get(currentCandidate));
                        targetMatches.add(user);

                        user.put(ParseConstants.KEY_MATCHES, matches);
                        currCandidate.put(ParseConstants.KEY_MATCHES, targetMatches);
                        //candidates.get(currentCandidate).put("matches", targetMatches);

                        //addNewConversationWithThisCard(currCandidate);
                        // Save in the background
                        try {
                            mIsMatch = true;
                            user.saveInBackground();
                            currCandidate.saveInBackground();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            Log.e("ERROR", "there is an errror");
                        }

                    }

                    // Th/is is here for testing but now works just needs to have the algorithm
                    // Add the current card if it matches
                   //addNewConversationWithThisCard(tempCandidate);
                }
                ++currentCandidate;

                if(mIsMatch) {
                    // When two people match on tinder
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Make a notification
                            Intent intent = new Intent(MainActivity.this, MatchedNotifcationActivity.class);
                            Bundle b = new Bundle();
                            // Get card user parse String
                            b.putString(CARD_USER, data.getImagePath());
                            //b.putString(CARD_USER, currCandidate.getString(ParseConstants.KEY_PHOTO0));
                            b.putString(CARD_ID, data.getID());
                            //b.putString(CARD_ID, currCandidate.getObjectId());
                            intent.putExtra(CARD_BUNDLE, b);
                            user.saveInBackground();
                            startActivity(intent);
                        }
                    });
                    mIsMatch = false;
                }
                // End of executor service
            }
        }

        @Override
        public void onAdapterAboutToEmpty(int itemsInAdapter) {

        }

        @Override
        public void onScroll(float scrollProgressPercent) {

            View view = flingContainer.getSelectedView();
            try {
                if (view.findViewById(R.id.background) != null) {
                    view.findViewById(R.id.background).setAlpha(0);
                }
                if (view.findViewById(R.id.item_swipe_right_indicator) != null) {
                    view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                }
                if (view.findViewById(R.id.item_swipe_left_indicator) != null) {
                    view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
                }
            }
            catch (NullPointerException n) {
                //n.printStackTrace();
            }
        }
    }

    public void addNewConversationWithThisCard(ParseUser currCandidate) {
        Log.d(getClass().getSimpleName(), "Adding a new person to conversation list");
        try {
            try {
                if (currCandidate != null) {
                    Log.d(getClass().getSimpleName(), "Username: " + currCandidate.get(ParseConstants.KEY_NAME));
                }
                newUserId = currCandidate.getString(ParseConstants.KEY_LAYERID);
            } catch (NullPointerException n) {
                //n.printStackTrace();
            }

            if (newUserId != null && !newUserId.equals("")) {
                Log.d(getClass().getSimpleName(), "Jumping to new conversation message update " + newUserId);
                Intent intent = new Intent(getApplicationContext(), AtlasMessagesScreen.class);
                intent.putExtra(AtlasMessagesScreen.EXTRA_CONVERSATION_IS_NEW, true);
                intent.putExtra(AtlasMessagesScreen.EXTRA_NEW_USER, newUserId);
                startActivity(intent);
            }
        } catch (NullPointerException n) {
            //n.printStackTrace();
        }
    }

    public void requestionLocationPermisson() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    startService(geolocationIntent);
                    SharedPreferences pref = getSharedPreferences(GEO_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();

                    editor.putBoolean(GEO_BOOL,true);

                    editor.apply();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    boolean checkIfLocationEnabled() {
        SharedPreferences pref = getSharedPreferences(GEO_PREF, Context.MODE_PRIVATE);

        boolean status = pref.getBoolean(GEO_BOOL, false);

        if (!status) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(GEO_BOOL, false);
            editor.apply();
        }
        return status;
    }
}
