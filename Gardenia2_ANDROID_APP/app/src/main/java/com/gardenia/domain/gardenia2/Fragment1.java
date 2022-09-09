package com.gardenia.domain.gardenia2;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class Fragment1 extends Fragment implements Dialog_AddPlant.Dialog_AddPlantListener{

    Spinner privSpinner;
    Button privButtonAddPlant, privButtonRemovePlant, privButtonApply;
    TextView privSetTemp;
    String selectedPlant, tempSelectedPlant;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayAdapter<String> spinnerAdapter;
    List<String> updatedPlantList = new ArrayList<String>();
    int startHourWater = 0, startMinuteWater = 0, endHourWater = 0, endMinuteWater = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.layout_fragment1, container, false);

        //SPINNER
        privSpinner = (Spinner) view.findViewById(R.id.spinner1);
        spinnerAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, updatedPlantList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        privSpinner.setAdapter(spinnerAdapter);

        privSetTemp = (TextView) view.findViewById(R.id.textView3);

        // Spinner click listener
        // Wybieranie uprawy z listy dropdown
        privSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id)
            {

                selectedPlant = parent.getItemAtPosition(position).toString();

                //Pobieranie wartości temperatury aktualnie wybranej uprawy
                myRef = database.getReference("Plants/names/"+selectedPlant+"/temp");
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        tempSelectedPlant = dataSnapshot.getValue(String.class);
                        privSetTemp.setText(tempSelectedPlant+ "℃");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // END OF SPINNER

        //TIME PICKER DIALOG START WATERING
        // Get open TimePickerDialog button.

        Button timePickerDialogButtonStart = (Button) view.findViewById(R.id.button_setWateringStart);
        final TextView timePickerValueTextViewStart = (TextView) view.findViewById(R.id.textView_setWateringStart);
        final Button timePickerDialogButtonEnd = (Button) view.findViewById(R.id.button_setWateringEnd);
        timePickerDialogButtonEnd.setEnabled(false);

        timePickerDialogButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new OnTimeSetListener instance. This listener will be invoked when user click ok button in TimePickerDialog.
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        StringBuffer strBuf;
                        strBuf = new StringBuffer();
                        strBuf.append(hour);
                        strBuf.append(":");
                        strBuf.append(minute);
                        timePickerValueTextViewStart.setText(strBuf.toString());

                        startHourWater = hour;
                        startMinuteWater = minute;

                        timePickerDialogButtonEnd.setEnabled(true);
                    }
                };

                Calendar now = Calendar.getInstance();
                int hour = now.get(java.util.Calendar.HOUR_OF_DAY);
                int minute = now.get(java.util.Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(), android.R.style.Theme_Holo_Light_Dialog,  onTimeSetListener, hour, minute, true);
                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                timePickerDialog.setTitle("Początek nawadniania:");
                //TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(), onTimeSetListener, hour, minute, true);

                timePickerDialog.show();
            }
        });
        // end of TIME PICKER DIALOG START WATERING

        //TIME PICKER DIALOG END WATERING
        // Get open TimePickerDialog button.
        final TextView timePickerValueTextViewEnd = (TextView) view.findViewById(R.id.textView_setWateringEnd);

        timePickerDialogButtonEnd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Create a new OnTimeSetListener instance. This listener will be invoked when user click ok button in TimePickerDialog.
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        StringBuffer strBuf;
                        strBuf = new StringBuffer();
                        strBuf.append(hour);
                        strBuf.append(":");
                        strBuf.append(minute);

                        timePickerValueTextViewEnd.setText(strBuf.toString());

                        endHourWater = hour;
                        endMinuteWater = minute;

                        if (endHourWater > startHourWater && endMinuteWater > startMinuteWater)
                            displayAlert(getView(), "Nawadnianie będzie trwało więcej niż godzinę!", "Uwaga!");

                    }

                };

                Calendar now = Calendar.getInstance();
                int hour = now.get(java.util.Calendar.HOUR_OF_DAY);
                int minute = now.get(java.util.Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(), android.R.style.Theme_Holo_Light_Dialog,  onTimeSetListener, hour, minute, true);
                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                timePickerDialog.setTitle("Koniec nawadniania:");

                timePickerDialog.show();



            }
        });
        // end of TIME PICKER DIALOG END WATERING

        //Usuwanie wybranej uprawy z bazy
        privButtonRemovePlant = (Button) view.findViewById(R.id.button_removeplant);
        privButtonRemovePlant.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                myRef = database.getReference().child("Plants/names").child(selectedPlant);
                myRef.removeValue();
            }
        });

        privButtonAddPlant = (Button) view.findViewById(R.id.button_addplant);
        privButtonAddPlant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        //Wywołanie przy każdej zmianie w Plants/names
        //Pobranie upraw z bazy i dodanie ich do listy - updatedPlantList
        myRef = database.getReference("Plants/names");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                updatedPlantList.clear();
                for(DataSnapshot datas: dataSnapshot.getChildren()){
                    String plant=datas.getKey();
                    Log.d("ON_DATA_CHANGE",  plant);
                    updatedPlantList.add(plant);
                }
                privSetTemp.setText(tempSelectedPlant+ " ℃");

                spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                 Log.w("ERROR", "Error przy odczycie danych.", error.toException());
            }
        });

        //Wysłanie zastosowanych danych do bazy
        privButtonApply= (Button) view.findViewById(R.id.button_setSettings);
        privButtonApply.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                Map<String,Object> dataMap = new HashMap<>();

                if(selectedPlant != null) {
                    dataMap.put("plant", selectedPlant);
                    dataMap.put("temp", tempSelectedPlant);
                    dataMap.put("watering", startHourWater+":"+startMinuteWater+"-"+endHourWater+":"+endMinuteWater);

                    writeFirebaseData("CurrentActivity", dataMap);
                }
                else
                    displayAlert(getView(), "Wybierz rodzaj uprawy!", "Uwaga!");

            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

    }
    private void openDialog()
    {
        Dialog_AddPlant dialog_addPlant = new Dialog_AddPlant();
        dialog_addPlant.show(getChildFragmentManager(), "DodajUprawe");
    }

    //Dodanie nowej uprawy do bazy
    @Override
    public void writeFirebaseDataDialog(String childPath, String value) {
        Map<String,Object> taskMap = new HashMap<>();
        taskMap.put("temp", value);
        writeFirebaseData(childPath, taskMap);
    }

    private void writeFirebaseData(String childPath, Map<String,Object> valueMap) {

        myRef = database.getReference();
        myRef.child(childPath).setValue(valueMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {

                    @Override
                    public void onSuccess(Void aVoid) {

                        if(!isNetworkAvailable())
                            displayAlert(getView(), "Błąd połączenia z internetem! Dane zostaną automatycznie wysłane przy wznowieniu połączenia", "INFO");
                        else
                            displayAlert(getView(), "Pomyślnie wysłano dane do bazy!", "INFO");

                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        displayAlert(getView(), "Wystapił błąd przy wysłaniu danych do bazy!", "Error");
                    }
                });

    }


    public void displayAlert(View rootView, String text, String title) {

        new AlertDialog.Builder(rootView.getContext())
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
