/*
 * Copyright (c) 2015 Layer. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cs180.ucrtinder.youwho.Messenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.cs180.ucrtinder.youwho.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.youwho.Parse.YouWhoApplication;
import com.cs180.ucrtinder.youwho.R;
import com.cs180.ucrtinder.youwho.atlas.Atlas.Tools;
import com.cs180.ucrtinder.youwho.atlas.AtlasParticipantPicker;


/**
 * @author Oleg Orlov
 * @since 24 Apr 2015
 */
public class AtlasParticipantPickersScreen extends AppCompatActivity {

    /** String[] with selected participants to use as a result*/
    public static final String EXTRA_KEY_USERIDS_SELECTED = "userids.selected";
    /** String[] of userIDs that cannot be selected */
    public static final String EXTRA_KEY_USERIDS_SKIP = "userids.skip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atlas_screen_participants_picker);


        // Android drawer init
        // Creating an android drawer to slide in from the left side
        AndroidDrawer mDrawer = new AndroidDrawer
                (this, R.id.drawer_layout_participants_picker, R.id.left_drawer_participants_picker, R.id.participants_profile_drawer_pic);


        String[] skipUserIds = getIntent().getExtras().getStringArray(EXTRA_KEY_USERIDS_SKIP);
        
        View addBtn = findViewById(R.id.atlas_screen_participants_picker_add);
        View cancelBtn = findViewById(R.id.atlas_screen_participants_picker_cancel);
        
        final AtlasParticipantPicker participantsPicker = (AtlasParticipantPicker) findViewById(R.id.atlas_screen_participants_picker_picker);
        participantsPicker.init(skipUserIds, ((YouWhoApplication) getApplication()).getParticipantProvider());
        
        addBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String[] selectedUserIds = participantsPicker.getSelectedUserIds();
                if (selectedUserIds.length == 0) return;
                Intent result = new Intent();
                result.putExtra(EXTRA_KEY_USERIDS_SELECTED, selectedUserIds);
                setResult(RESULT_OK, result);
                finish();
            }
        });
        
        cancelBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        
        prepareActionBar();
    }

    private void prepareActionBar() {
        ((TextView)findViewById(R.id.atlas_actionbar_title_text)).setText("Add People");
        final ImageView btnMenuLeft = (ImageView)findViewById(R.id.atlas_actionbar_left_btn);
        btnMenuLeft.setImageResource(R.drawable.atlas_ctl_btn_back);
        btnMenuLeft.setVisibility(View.VISIBLE);
        btnMenuLeft.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        Tools.setStatusBarColor(getWindow(), getResources().getColor(R.color.atlas_background_blue_dark));
    }
    
}
