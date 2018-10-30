package com.frost.mqtttutorial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import helpers.ChartHelper;
import helpers.MqttHelper;

import android.os.Handler;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class Ppgview extends AppCompatActivity {
    TextView textView;
    String message;
    MqttHelper mqttHelper;
    ChartHelper mChart;
    LineChart chart;

    public String ppg;

    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    private double graphLastXValue = 50d;
    double i = 0;


    private LineGraphSeries<DataPoint> mSeries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppgview);

        textView = (TextView) findViewById(R.id.textViewp);
        chart = (LineChart) findViewById(R.id.chart);
        mChart = new ChartHelper(chart);
        GraphView graph = (GraphView) findViewById(R.id.graph);

        startMqtt();
        initGraph(graph);
    }

    public void initGraph(GraphView graph) {
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(500);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(425);
        graph.getViewport().setMaxY(625);

        // first mSeries is a line
        mSeries = new LineGraphSeries<>();
        graph.addSeries(mSeries);
    }

    public void onResume() {
        super.onResume();
        mTimer = new Runnable() {
            @Override
            public void run() {

                if (graphLastXValue == 500) {
                    graphLastXValue = 0;
                    mSeries.resetData(new DataPoint[]{
                            new DataPoint(graphLastXValue, i)
                    });
                }

                graphLastXValue += 1d;
                mSeries.appendData(new DataPoint(graphLastXValue, i), false, 1000);
                mHandler.postDelayed(this, 5);
            }

        };
        mHandler.postDelayed(mTimer, 500);
    }

    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTimer);
    }

    double mLastRandom = 2;

    private void getRandom(double i) {
     this.i=i;
    }


    private String splitmsg(String message) {

        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(message);
        int find = 2;
        int count = 1;
        while (m.find()) {
            String s = m.group();
            if (find == count) {
                return s;
            }
            count++;
        }

        return null;

    }

    public double startMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Debug", "Connected");
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
//                Log.w("Debug1111", mqttMessage.toString());
                textView.setText(mqttMessage.toString());
                textView.setText(splitmsg(mqttMessage.toString()));

//                Log.w("Split", splitmsg(mqttMessage.toString()));
                i= Double.parseDouble(splitmsg(mqttMessage.toString()));
                Log.w("Split", String.valueOf(i));
                getRandom(i);
                mChart.addEntry(Float.valueOf(mqttMessage.toString()));
            }


            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }

        });
        return i;

    }


}
