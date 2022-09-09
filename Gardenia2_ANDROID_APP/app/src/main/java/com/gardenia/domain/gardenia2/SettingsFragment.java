package com.gardenia.domain.gardenia2;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


public class SettingsFragment extends PreferenceFragmentCompat {

    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Settings");

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState,
                                    String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        EditTextPreference countingPreference = findPreference("edit_text_preference_1");

        if (countingPreference != null) {

            countingPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
             public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                String val = newValue.toString();
               // Toast.makeText(getContext(), "Selected: "+val, Toast.LENGTH_SHORT).show();
                myRef.child("info_update_time_interval").setValue(val);
                return true;
            }
        });

    }

        //Gwiazdkowanie hasła
        EditTextPreference preference = findPreference("edit_text_pass_cam");
        if (preference!= null) {
            preference.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        }
                    });
        }


    }


}



