package com.example.stress;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.stress.databinding.ActivityStressMainBinding;
import com.example.stress.retrofit.ApiUtils;
import com.example.stress.retrofit.CallbackWrapper;
import com.triggertrap.seekarc.SeekArc;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
//import android.databinding.DataBindingUtil;

public class StressMainActivity extends AppCompatActivity
{

    private static final String TAG = "StressMainActivity";
    private ActivityStressMainBinding binding;
    private Handler handler = new Handler();
    private final int RUNNABLE_DELAY = 1000; // 1sec
    private Runnable senDataRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(TAG, "tic");
            String studyNumber = PreferenceManager
                    .getDefaultSharedPreferences(StressMainActivity.this)
                    .getString(getString(R.string.study_number_key), "");
            String participantNumber = PreferenceManager
                    .getDefaultSharedPreferences(StressMainActivity.this)
                    .getString(getString(R.string.participant_number_key), "");
            if(studyNumber.isEmpty() || participantNumber.isEmpty())
            {
                Toast.makeText(StressMainActivity.this, "You must a valid id participant and id study", Toast.LENGTH_SHORT).show();
            } else
            {
                ApiUtils.create(StressMainActivity.this)
                        .addAffect(studyNumber, participantNumber, Integer.parseInt(binding.content.seekArcProgress.getText().toString()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new CallbackWrapper<retrofit2.Response<Void>>(StressMainActivity.this)
                        {

                            @Override
                            protected void onSuccess(Response<Void> voidResponse)
                            {
//                                Toast.makeText(StressMainActivity.this, "Value saved", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Value saved");
                            }
                        })
                        .onComplete();
            }
            handler.postDelayed(senDataRunnable, RUNNABLE_DELAY);
        }
    };
    private static boolean IsRunnableRUnning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_stress_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_stress_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //        ((SeekArc) findViewById(R.id.seekArc)).setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener()
        binding.content.seekArc.setProgress(100);
        binding.content.seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener()
        {

            @Override
            public void onStopTrackingTouch(SeekArc seekArc)
            {
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc)
            {
            }

            @Override
            public void onProgressChanged(SeekArc seekArc, int progress,
                                          boolean fromUser)
            {
                if(!StressMainActivity.IsRunnableRUnning)
                {
                    Log.d(TAG, "start tic");
                    handler.postDelayed(senDataRunnable, RUNNABLE_DELAY);
                    StressMainActivity.IsRunnableRUnning = true;
                }
                binding.content.seekArcProgress.setText(String.valueOf(progress - 100));
            }
        });


        //        FloatingActionButton fab = findViewById(R.id.fab);
        //        fab.setOnClickListener(new View.OnClickListener()
        //        {
        //            @Override
        //            public void onClick(View view)
        //            {
        //                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //                        .setAction("Action", null).show();
        //            }
        //        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "stop tic");
        super.onStop();
        handler.removeCallbacks(senDataRunnable);
        StressMainActivity.IsRunnableRUnning = false;
    }
}
