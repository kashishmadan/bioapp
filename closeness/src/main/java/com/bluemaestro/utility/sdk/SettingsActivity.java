package com.bluemaestro.utility.sdk;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.Map;

public class SettingsActivity extends PreferenceActivity
{
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.closeness_preferences);
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            Map<String,?> map = app_preferences.getAll();
            for (Map.Entry<String, ?> entry : map.entrySet())
            {
                Preference preference = findPreference(entry.getKey());
                if (preference == null) // if preference belongs to another screen, we won't find it
                    continue;
                if (entry.getValue() instanceof String) {
                    preference.setSummary((String)entry.getValue());
                    preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newVal) {
                            preference.setSummary((String)newVal);
                            return true;
                        }
                    });
                }
                if (entry.getValue()instanceof Integer) {
                    preference.setSummary((Integer)entry.getValue());
                    preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newVal) {
                            preference.setSummary((Integer)newVal);
                            return true;
                        }
                    });
                }
            }
        }
    }
}