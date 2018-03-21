package nl.tue.ppeters.flower;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.Map;

public class PrefsFragmentScenario extends PreferenceFragment {
    protected PreferenceFragment settingsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.flower_preferences);
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(FlowerActivity.getContext());
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
