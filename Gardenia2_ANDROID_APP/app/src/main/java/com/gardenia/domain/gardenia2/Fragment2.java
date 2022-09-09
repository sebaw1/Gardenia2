package com.gardenia.domain.gardenia2;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class Fragment2 extends Fragment {

    private static final String TAG = "odczyt";
    DatabaseReference myRef, actualRef, upRef;
    FirebaseDatabase database;
    private TextView privTextViewHumidity, privTextViewTemp, privTextViewSoil, privTextViewLight,
            privTextViewTempMAX,privTextViewHumidityMAX,privTextViewTempMIN, privTextViewHumidityMIN;
    private TextView privTextViewPlant, privTextViewSetTemp, privTextViewWatering;
    private TextView privTextViewLastUpdate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.layout_fragment2, container, false);

        final SwipeRefreshLayout swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swiperefresh);

        privTextViewLastUpdate = (TextView) view.findViewById(R.id.sensor_last_update);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Controls/Sensors");
        actualRef = database.getReference("CurrentActivity");
        upRef = database.getReference("Settings");

        privTextViewPlant = (TextView) view.findViewById(R.id.current_plant);
        privTextViewSetTemp = (TextView) view.findViewById(R.id.set_plant_temp);
        privTextViewWatering = (TextView) view.findViewById(R.id.set_watering_time);

        privTextViewTemp = (TextView) view.findViewById(R.id.sensor1_current_value);
        privTextViewHumidity = (TextView) view.findViewById(R.id.sensor2_current_value);
        privTextViewTempMAX = (TextView) view.findViewById(R.id.sensor1_max_value);
        privTextViewHumidityMAX = (TextView) view.findViewById(R.id.sensor2_max_value);
        privTextViewTempMIN = (TextView) view.findViewById(R.id.sensor1_min_value);
        privTextViewHumidityMIN = (TextView) view.findViewById(R.id.sensor2_min_value);

        privTextViewSoil = (TextView) view.findViewById(R.id.sensor3_current_value);
        privTextViewLight = (TextView) view.findViewById(R.id.sensor4_current_value);

        // Pierwszy widżet z danymi
        actualRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String Plant = (String) dataSnapshot.child("plant").getValue();
                String SetTemp = (String) dataSnapshot.child("temp").getValue();
                String Watering = (String) dataSnapshot.child("watering").getValue();

                privTextViewPlant.setText(Plant);
                privTextViewSetTemp.setText(SetTemp+"℃");
                privTextViewWatering.setText(Watering);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.actualRef", error.toException());

            }
        });

        // Pozostałe widżety z danymi
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.


                       String humidity = (String) dataSnapshot.child("Humidity/current_inside").getValue();
                       String light = (String) dataSnapshot.child("Light/photoresistor").getValue();
                       String soil = (String) dataSnapshot.child("Soil/humidity").getValue();
                       String temp = (String) dataSnapshot.child("Temperature/current_inside").getValue();

                       String humidityMAX = (String) dataSnapshot.child("Humidity/max_inside").getValue();
                       String humidityMIN = (String) dataSnapshot.child("Humidity/min_inside").getValue();
                       String tempMAX = (String) dataSnapshot.child("Temperature/max_inside").getValue();
                       String tempMIN = (String) dataSnapshot.child("Temperature/min_inside").getValue();


                        privTextViewTemp.setText(temp+"℃");
                        privTextViewHumidity.setText(humidity+"%");
                        privTextViewSoil.setText(soil+"%");
                        privTextViewLight.setText(light+"%");

                        privTextViewTempMAX.setText(tempMAX+"℃");
                        privTextViewHumidityMAX .setText(humidityMAX+"%");
                        privTextViewTempMIN.setText(tempMIN+"℃");
                        privTextViewHumidityMIN.setText(humidityMIN+"%");

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.myRef", error.toException());

            }
        });


        // Widżet z datą i godziną ostatniej aktualizacji raspberry z bazą
        upRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String lastUpdate = (String) dataSnapshot.child("last_update_datetime").getValue();
                privTextViewLastUpdate.setText("Ostatnia aktualizacja: "+ lastUpdate);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.myRef", error.toException());

            }
        });

        //Obsługa eventu swipe, czyli przeciągnięcia palcem w dół, po czym następuje aktualizacja danych w bazie
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        upRef.child("ref_request").setValue(1);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(getContext(), "refresh", Toast.LENGTH_SHORT).show();
                                upRef.child("ref_request").setValue(0);
                                swipeRefreshLayout.setRefreshing(false); // Disables the refresh icon
                            }
                        }, 1000);

                    }
                }
        );

        return view;
    }
}
