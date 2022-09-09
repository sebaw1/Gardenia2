package com.gardenia.domain.gardenia2;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;


public class Dialog_RaspberryInfo extends AppCompatDialogFragment {

    PieChart pieChartDISK, pieChartRAM;
    TextView rpiCPUTemp;
    Map<String, Double> diskStat, ramStat = new HashMap<>();
    Double diskFree, diskUsed, ramFree, ramUsed;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.raspberry_info_dialog, null);
        pieChartDISK= view.findViewById(R.id.pieChart_DISK);
        pieChartRAM = view.findViewById(R.id.pieChart_RAM);
        rpiCPUTemp = view.findViewById(R.id.TextCPUTemp);

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        ArrayList<PieEntry> pieEntries2 = new ArrayList<>();
        String label = "";


        Bundle mArgs = getArguments();
        String STRdiskFree = mArgs.getString("KEYdiskFree");
        String STRdiskUsed = mArgs.getString("KEYdiskUsed");
        String STRramFree = mArgs.getString("KEYramFree");
        String STRramUsed = mArgs.getString("KEYramUsed");
        String STRcpuTemp = mArgs.getString("KEYcpuTemp");
        Log.w("RPI_INFO2", STRdiskFree);
        Log.w("RPI_INFO2", STRdiskUsed);
        Log.w("RPI_INFO2", STRramFree);
        Log.w("RPI_INFO2", STRramUsed);
        Log.w("RPI_INFO2", STRcpuTemp);

        diskFree = Double.parseDouble(STRdiskFree);
        diskUsed = Double.parseDouble(STRdiskUsed);
        ramFree = Double.parseDouble(STRramFree);
        ramUsed = Double.parseDouble(STRramUsed);

        diskStat.put("Disk Free [GB]",diskFree);
        diskStat.put("Disk Used [GB]",diskUsed);
        ramStat.put("Ram Free [MB]",ramFree);
        ramStat.put("Ram Used [MB]",ramUsed);

        rpiCPUTemp.setText("Raspberry Pi CPU Temperature: " + STRcpuTemp + "℃");

        //initializing colors for the entries
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        ArrayList<Integer> colors2 = new ArrayList<>();
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors2.add(c);

        //-----------------------------------------------DISK
        //input data and fit data into pie chart entry
        for(String type: diskStat.keySet()){
            pieEntries.add(new PieEntry(diskStat.get(type).floatValue(), type));
        }
        //collecting the entries with label name
        PieDataSet pieDataSet = new PieDataSet(pieEntries,label);
        //setting text size of the value
        pieDataSet.setSliceSpace(2f);

        pieDataSet.setValueTextSize(10f);
        //providing color list for coloring different entries
        pieDataSet.setColors(colors);
        pieDataSet.setValueFormatter(new LargeValueFormatter());

        //grouping the data set from entry to chart
        PieData pieData = new PieData(pieDataSet);
        //showing the value of the entries, default true if not set
        pieData.setDrawValues(true);

        pieChartDISK.setHoleColor(Color.WHITE);
        pieChartDISK.setTransparentCircleColor(Color.WHITE);
        pieChartDISK.setTransparentCircleAlpha(110);
        pieChartDISK.setHoleRadius(48f);
        pieChartDISK.setTransparentCircleRadius(55f);
        pieChartDISK.setEntryLabelColor(Color.BLACK);
        pieChartDISK.setEntryLabelTextSize(12f);
        pieChartDISK.getDescription().setEnabled(false);
        Legend l = pieChartDISK.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);

        pieChartDISK.setData(pieData);
        pieChartDISK.invalidate();

        //-----------------------------------------------RAM
        //input data and fit data into pie chart entry
        for(String type: ramStat.keySet()){
            pieEntries2.add(new PieEntry(ramStat.get(type).floatValue(), type));
        }

        //collecting the entries with label name
        PieDataSet pieDataSet2 = new PieDataSet(pieEntries2,label);
        pieDataSet2.setSliceSpace(2f);
        //setting text size of the value
        pieDataSet2.setValueTextSize(10f);
        //providing color list for coloring different entries
        pieDataSet2.setColors(colors2);

        pieDataSet2.setValueFormatter(new LargeValueFormatter());
        //grouping the data set from entry to chart
        PieData pieData2 = new PieData(pieDataSet2);
        //showing the value of the entries, default true if not set
        pieData2.setDrawValues(true);

        pieChartRAM.setHoleColor(Color.WHITE);
        pieChartRAM.setTransparentCircleColor(Color.WHITE);
        pieChartRAM.setTransparentCircleAlpha(110);
        pieChartRAM.setHoleRadius(48f);
        pieChartRAM.setTransparentCircleRadius(55f);
        pieChartRAM.setEntryLabelColor(Color.BLACK);
        pieChartRAM.setEntryLabelTextSize(12f);
        pieChartRAM.getDescription().setEnabled(false);
        Legend l2 = pieChartRAM.getLegend();
        l2.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l2.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l2.setOrientation(Legend.LegendOrientation.VERTICAL);

        pieChartRAM.setData(pieData2);
        pieChartRAM.invalidate();

        builder.setView(view)
                .setTitle("RASPBERRY INFO")
                .setPositiveButton("OK", null);

        return builder.create();


    }

}