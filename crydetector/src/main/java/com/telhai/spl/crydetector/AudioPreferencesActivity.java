package com.telhai.spl.crydetector;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AudioPreferencesActivity extends PreferenceActivity
{
    static final private String TAG = "AUDRECPROC::PREFS";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new AudioPreferenceFragment()).commit();
    }
}
