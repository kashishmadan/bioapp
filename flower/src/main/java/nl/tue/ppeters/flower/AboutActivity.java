package nl.tue.ppeters.flower;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * @author PPeters
 *
 * Show information when "About" menu item is pressed
 */
public class AboutActivity extends Activity {
	/**
	 * TAG for LogCat purposes
	 */
	static final private String TAG = AboutActivity.class.getName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // don't show title at top of window
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    // set content to display
		setContentView(R.layout.layout_about);
	}

}
