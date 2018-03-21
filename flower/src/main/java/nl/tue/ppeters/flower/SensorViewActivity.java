//**NORMAAL KAN DE GEBRUIKER AANGEVEN WELKE SENSOR ZE WEERGEGEVEN WILLEN ZIEN OP HET SCHERM
//**MET ENKELE AANPASSINGEN IS DIT NIET MEER NODIG 
//**MOCHTEN WE TOCH MEER SENSOREN WILLEN UITLEZEN, DAN MOETEN WE DAT HIER AANPASSEN

package nl.tue.ppeters.flower;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * this class determines which sensor should be depicted on the screen, in our case there is only one sensor, the GSR sensor
 * This is an adapted version of the orignal app by Shimmer
 * @author Misha
 *
 */
public class SensorViewActivity extends Activity{
	// Return Intent extra
    public static String mDone = "Done";
	private String mSensorName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Bundle extras = getIntent().getExtras();
        int enabledSensors = extras.getInt("Enabled_Sensors");
        
        mSensorName="GSR";
        //mSensorName="Accelerometer";
        Intent intent = new Intent();
        intent.putExtra(mDone, mSensorName);
        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent);
    	finish();
    }
}
