package com.gardenia.domain.gardenia2;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String ZEWN_IP = "edit_text_zewn_ip";
    public static final String WEWN_IP = "edit_text_wewn_ip";
    public static final String LOGIN_CAM = "edit_text_login_cam";
    public static final String PASS_CAM = "edit_text_pass_cam";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }
}