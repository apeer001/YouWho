package com.cs180.ucrtinder.youwho.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.cs180.ucrtinder.youwho.Parse.ParseConstants;
import com.cs180.ucrtinder.youwho.R;
import com.cs180.ucrtinder.youwho.tindercard.SwipePhotoAdapter;
import com.parse.ParseUser;

public class MatchedNotifcationActivity extends AppCompatActivity {

    private Button continueBtn;
    private Button messageButton;
    private ImageView currentUserImage;
    private ImageView currentCandidateImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched_notifcation);

        continueBtn = (Button) findViewById(R.id.continueBtn);
        messageButton = (Button) findViewById(R.id.message_them_btn);
        currentUserImage = (ImageView) findViewById(R.id.userImage);
        currentCandidateImage = (ImageView) findViewById(R.id.candidateImage);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle b = intent.getBundleExtra(MainActivity.CARD_BUNDLE);
            if (b != null) {
                ParseUser currentUser = ParseUser.getCurrentUser();
                Bitmap bmp;
                String url = "";
                if(currentUser != null) {
                    url = currentUser.getString(ParseConstants.KEY_PHOTO0);
                    bmp = SwipePhotoAdapter.getBitmapFromURL(url);
                    currentUserImage.setImageBitmap(bmp);
                } else {
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                    currentUserImage.setImageBitmap(bmp);
                }

                url = b.getString(MainActivity.CARD_USER);
                bmp = SwipePhotoAdapter.getBitmapFromURL(url);
                currentCandidateImage.setImageBitmap(bmp);
            } else {
                finish();
            }
        } else {
            finish();
        }



        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
