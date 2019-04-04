package com.embrwave.embrwave;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.embrwave.embrwave.databinding.ActivityEmbrWaveBinding;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

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
                    activityEmbrWaveBinding.embrWaveConnect.setText(R.string.button_disconnect);
                } else {
                    activityEmbrWaveBinding.embrWaveConnect.setText(R.string.button_connect);
                }
                isConnected = !isConnected;
            }
        });
        activityEmbrWaveBinding.embrWaveSlider.setValue(sliderValue);
        activityEmbrWaveBinding.embrWaveSlider.setIndicatorTextDecimalFormat("0");
        activityEmbrWaveBinding.embrWaveSlider.setOnRangeChangedListener(new OnRangeChangedListener()
        {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser)
            {
                Log.d(TAG, leftValue + " " + rightValue);
                if(leftValue < 0) {
                    activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_cool);
                    activityEmbrWaveBinding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_button_cool));
                } else if (leftValue > 0) {
                    activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_warm);
                    activityEmbrWaveBinding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_button_warm));
                }
                sliderValue = (int) leftValue;
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft)
            {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft)
            {

            }
        });
        activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_warm);
        activityEmbrWaveBinding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_button_warm));
        activityEmbrWaveBinding.embrWaveActivate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(isChangingTemperature) {
                    if(sliderValue < 0) {
                        activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_cool);
                        activityEmbrWaveBinding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_button_cool));
                    } else {
                        activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_warm);
                        activityEmbrWaveBinding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_button_warm));
                    }
                } else {
                    activityEmbrWaveBinding.embrWaveActivate.setText(R.string.button_stop);
                    activityEmbrWaveBinding.embrWaveActivate.setBackgroundColor(getResources().getColor(R.color.color_gray));
                }
                isChangingTemperature = !isChangingTemperature;
            }
        });
    }
}
