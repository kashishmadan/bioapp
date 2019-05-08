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
    private ActivityEmbrWaveBinding binding;
    private static final String TAG = "EmbrWaveActivity";
    private boolean isConnected = false;
    private boolean isChangingTemperature = false;
    private int sliderValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_embr_wave);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_embr_wave);

        if(EmbrWaveService.isRunning)
        {
            binding.embrWaveConnect.setText(R.string.button_disconnect);
            isConnected = true;
        }

        binding.embrWaveConnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(isConnected)
                {
                    // disconnect
                    binding.embrWaveConnect.setText(R.string.button_connect);
                    Intent intent = new Intent(EmbrWaveActivity.this, EmbrWaveService.class);
                    stopService(intent);
                } else
                {
                    // connect
                    try
                    {
                        Intent bindIntent = new Intent(EmbrWaveActivity.this, EmbrWaveService.class);
                        //        bindService(bindIntent, this.temperatureServiceConnection, Context.BIND_AUTO_CREATE);

                        bindIntent.putExtra(getString(R.string.embr_wave_address_key), PreferenceManager
                                .getDefaultSharedPreferences(EmbrWaveActivity.this)
                                .getString(getString(R.string.embr_wave_address_key), "ED:A9:79:FD:7D:1B"));
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
                    binding.embrWaveConnect.setText(R.string.button_disconnect);
                }
                isConnected = !isConnected;
            }
        });

        //        binding.embrWaveSlider.setValue(sliderValue);
        //        binding.embrWaveSlider.setIndicatorTextDecimalFormat("0");
        //        binding.embrWaveSlider.setOnRangeChangedListener(new OnRangeChangedListener()
        //        {
        //            @Override
        //            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser)
        //            {
        //                Log.d(TAG, leftValue + " " + rightValue);
        //                if(leftValue < 0) {
        //                    binding.embrWaveActivate.setText(R.string.button_cool);
        //                    binding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color
        //                    .color_button_cool));
        //                } else if (leftValue > 0) {
        //                    binding.embrWaveActivate.setText(R.string.button_warm);
        //                    binding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color
        //                    .color_button_warm));
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
        binding.embrWaveActivate.setText(R.string.button_start);
        //        binding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_button_warm));
        binding.embrWaveActivate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!isConnected)
                {
                    Toast.makeText(EmbrWaveActivity.this, "You must connect to the device first", Toast.LENGTH_LONG).show();
                } else
                {
                    String temperature = PreferenceManager.getDefaultSharedPreferences(EmbrWaveActivity.this)
                            .getString(getString(R.string.embr_wave_temperature_key), "");
                    try
                    {
                        int value = Integer.parseInt(temperature);
                        if(value < -9 || value > 9)
                        {
                            Toast.makeText(EmbrWaveActivity.this, "You must enter a value between 9 and -9", Toast.LENGTH_LONG).show();
                            return;
                        }
                    } catch(NumberFormatException e)
                    {
                        Toast.makeText(EmbrWaveActivity.this, "You must enter a valid value", Toast.LENGTH_LONG).show();
                        return;
                        //                    } catch(NullPointerException e) {
                        //                        return;
                    }
                    Intent bindIntent = new Intent(EmbrWaveActivity.this, EmbrWaveService.class);
                    if(isChangingTemperature)
                    {
                        //                    if(sliderValue < 0) {
                        //                        binding.embrWaveActivate.setText(R.string.button_cool);
                        //                        binding.embrWaveActivate.setBackgroundColor(getResources().getColor(R
                        //                        .color
                        //                        .color_button_cool));
                        //                    } else {
                        //                        binding.embrWaveActivate.setText(R.string.button_warm);
                        //                        binding.embrWaveActivate.setBackgroundColor(getResources().getColor(R
                        //                        .color
                        //                        .color_button_warm));
                        //                    }
                        binding.embrWaveActivate.setText(R.string.button_start);
                        bindIntent.putExtra(EmbrWaveService.SERVICE_ACTION_KEY, EmbrWaveService.SERVICE_ACTION_STOP_COOL_WARM);
                    } else
                    {
                        binding.embrWaveActivate.setText(R.string.button_stop);
                        bindIntent.putExtra(EmbrWaveService.SERVICE_ACTION_KEY, EmbrWaveService.SERVICE_ACTION_START_COOL_WARM);
                        bindIntent.putExtra(EmbrWaveService.SERVICE_VALUE_KEY, Integer
                                .valueOf(PreferenceManager.getDefaultSharedPreferences(EmbrWaveActivity.this)
                                        .getString(getString(R.string.embr_wave_temperature_key), "0")));
                        //                    binding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color
                        //                    .color_gray));
                    }
                    startService(bindIntent);
                    isChangingTemperature = !isChangingTemperature;
                }

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

    @Override
    protected void onStart()
    {
        super.onStart();
        binding.embrWaveValue.setText(PreferenceManager.getDefaultSharedPreferences(EmbrWaveActivity.this)
                .getString(getString(R.string.embr_wave_temperature_key), "Select value"));
    }
}
