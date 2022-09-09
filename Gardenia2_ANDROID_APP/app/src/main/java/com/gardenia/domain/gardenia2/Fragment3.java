package com.gardenia.domain.gardenia2;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Fragment3 extends Fragment{

    LibVLC libvlc;
    MediaPlayer mMediaPlayer;
    VLCVideoLayout mVideoLayout;
    private String[] sIp = {"", "", ""};
    private String[] sAuth = {"", ""};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.layout_fragment3, container, false);
        int networkStatus = getNetworkStatus();

        if(networkStatus != 0) {
            String url = "rtsp://"+sAuth[0]+":"+sAuth[1]+"@" + sIp[networkStatus];

            final ArrayList<String> args = new ArrayList<>();
            args.add("--no-drop-late-frames");
            args.add("--no-skip-frames");
            args.add("--rtsp-tcp");
            args.add("-vvv");
            libvlc = new LibVLC(rootView.getContext(), args);
            mMediaPlayer = new MediaPlayer(libvlc);

            mVideoLayout = rootView.findViewById(R.id.video_layout);
            mMediaPlayer.attachViews(mVideoLayout, null, false, true);
            final Media media = new Media(libvlc, Uri.parse(url));

            // try {

            mMediaPlayer.setMedia(media);
            media.release();
            mMediaPlayer.play();

        }
        else Toast.makeText(getContext(), "Brak połączenia z internetem", Toast.LENGTH_LONG).show();


        return rootView;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // ZAKŁADKA - USTAWIENIA
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        sIp[1] = sharedPref.getString(SettingsActivity.WEWN_IP, "true"); //wifi
        sIp[2] = sharedPref.getString(SettingsActivity.ZEWN_IP, "true"); //dane mobilne

        sAuth[0] = sharedPref.getString(SettingsActivity.LOGIN_CAM, "true"); //login
        sAuth[1] = sharedPref.getString(SettingsActivity.PASS_CAM, "true"); //haslo

    }

    public int getNetworkStatus(){
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                //Toast.makeText(getContext(), "Wifi", Toast.LENGTH_SHORT).show();
                return 1;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to mobile data
                //Toast.makeText(getContext(), "Mobile data", Toast.LENGTH_SHORT).show();
                return 2;
            }
        }
        return 0;
    }

}
