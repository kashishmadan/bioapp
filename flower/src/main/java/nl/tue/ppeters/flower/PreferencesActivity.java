package nl.tue.ppeters.flower;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity {
	static final private String TAG = PreferencesActivity.class.getName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragmentScenario()).commit();
	}
}
