package com.telhai.spl.coregulation;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class CoRegulationPreferencesActivity extends PreferenceActivity {
	static final private String TAG = "COREG::PREFS";

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    getFragmentManager().beginTransaction().replace(android.R.id.content,
                new CoRegulationPreferenceFragment()).commit();
	}
}
