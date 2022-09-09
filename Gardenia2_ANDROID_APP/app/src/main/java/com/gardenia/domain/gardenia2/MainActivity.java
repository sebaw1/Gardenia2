package com.gardenia.domain.gardenia2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity{

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String diskFree, diskUsed, ramFree, ramUsed, cpuTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("PI");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                diskFree = dataSnapshot.child("DISK").child("free").getValue(String.class);
                diskUsed = dataSnapshot.child("DISK").child("used").getValue(String.class);
                ramFree = dataSnapshot.child("RAM").child("free").getValue(String.class);
                ramUsed = dataSnapshot.child("RAM").child("used").getValue(String.class);
                cpuTemp = dataSnapshot.child("CPU").child("temperature").getValue(String.class);


            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("RPI_INFO", "Failed to read value", error.toException());

            }
        });

    }
        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.about_settings) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("O Autorach");
            dialog.setMessage("Design, programowanie: Sebastian Wielgus \n\nTestowanie: Piotr Zdunek");
            dialog.setPositiveButton("OK", null);
            AlertDialog alert = dialog.create();
            alert.show();
            return true;
        }

        if (id == R.id.raspberry_settings) {

            Bundle args = new Bundle();
            args.putString("KEYdiskFree", diskFree);
            args.putString("KEYdiskUsed", diskUsed);
            args.putString("KEYramFree", ramFree);
            args.putString("KEYramUsed", ramUsed);
            args.putString("KEYcpuTemp", cpuTemp);
            Dialog_RaspberryInfo dialog_rpi = new Dialog_RaspberryInfo();
            dialog_rpi.setArguments(args);
            dialog_rpi.show(getSupportFragmentManager(), "RPI");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
           // return PlaceholderFragment.newInstance(position + 1);

            Fragment fragment = null;

            switch(position)
            {
                case 0:
                    fragment = new Fragment2();
                    break;
                case 1:
                    fragment = new Fragment1();
                    break;
                case 2:
                    fragment = new Fragment3();
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}
