package com.embrwave.embrwave;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.embrwave.embrwave.databinding.ActivityEmbrWaveBinding;
import com.embrwave.embrwave.service.EmbrWaveService;

public class EmbrWaveActivity extends AppCompatActivity
{
    private ActivityEmbrWaveBinding activityEmbrWaveBinding;
    private static final String TAG = "EmbrWaveActivity";
    private boolean isConnected = false;
    private boolean isChangingTemperature = false;
    private int sliderValue = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_embr_wave);
        activityEmbrWaveBinding = DataBindingUtil.setContentView(this, R.layout.activity_embr_wave);

        activityEmbrWaveBinding.embrWaveConnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(isConnected) {
                    // disconnect
                    activityEmbrWaveBinding.embrWaveConnect.setText(R.string.button_connect);
                } else {
                    // connect
                    try
                    {
                        Intent bindIntent = new Intent(EmbrWaveActivity.this, EmbrWaveService.class);
                        //        bindService(bindIntent, this.temperatureServiceConnection, Context.BIND_AUTO_CREATE);

                        bindIntent.putExtra("bracelet_name", PreferenceManager.getDefaultSharedPreferences(EmbrWaveActivity.this).getString("bracelet_name", ""));
                        startService(bindIntent);
//                        bindIntent.putExtra("partner_sensor_address", this.getPartnerSensorName());

//                        this.startService(bindIntent);
//                                    this.temperatureService.onConnect(deviceAddress, this.getPartnerSensorName());

                        //                    final Handler handler = new Handler();
                        //                    handler.postDelayed(new Runnable() {
                        //                        @Override
                        //                        public void run() {
                        //                            startLogging();
                        //                        }
                        //                    }, 1000);
                    } catch(IllegalArgumentException e)
                    {
                        e.printStackTrace();
                        Toast.makeText(EmbrWaveActivity.this, "Error connecting", Toast.LENGTH_SHORT).show();
                    }
                    activityEmbrWaveBinding.embrWaveConnect.setText(R.string.button_disconnect);
                }
                isConnected = !isConnected;
            }
        });
//        activityEmbrWaveBinding.embrWaveSlider.setValue(sliderValue);
//        activityEmbrWaveBinding.embrWaveSlider.setIndicatorTextDecimalFormat("0");
//        activityEmbrWaveBinding.embrWaveSlider.setOnRangeChangedListener(new OnRangeChangedListener()
//        {
//            @Override
//            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser)
//            {
//                Log.d(TAG, leftValue + " " + rightValue);
//                if(leftValue < 0) {
//                    activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_cool);
//                    activityEmbrWaveBinding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_button_cool));
//                } else if (leftValue > 0) {
//                    activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_warm);
//                    activityEmbrWaveBinding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_button_warm));
//                }
//                sliderValue = (int) leftValue;
//            }
//
//            @Override
//            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft)
//            {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft)
//            {
//
//            }
//        });
        activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_start);
//        activityEmbrWaveBinding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_button_warm));
        activityEmbrWaveBinding.embrWaveActivate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(isChangingTemperature) {
//                    if(sliderValue < 0) {
//                        activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_cool);
//                        activityEmbrWaveBinding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_button_cool));
//                    } else {
//                        activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_warm);
//                        activityEmbrWaveBinding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_button_warm));
//                    }
                    activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_start);
                } else {
                    activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_stop);
//                    activityEmbrWaveBinding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_gray));
                }
                isChangingTemperature = !isChangingTemperature;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.embr_wave, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.preferences)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
