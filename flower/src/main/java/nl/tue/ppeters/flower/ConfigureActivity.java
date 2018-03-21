//**NORMAAL MOET DE GEBRUIKER HANDMATIG AANGEVEN WELKE SENSOREN ER OP DE MODULE ZITTEN
//**ECHTER HEBBEN WE DIT HIER OMZEILD DOOR DIRECT IN TE VOEREN DAT ER ALLEEN DE GSR SENSOR WORDT GEBRUIKT

package nl.tue.ppeters.flower;


import android.app.Activity;
import android.content.Intent;

/**
 * This class determines the sensors that are attached to the Shimmer and communicates this back to ShimmerGraph
 * Currently it has been adapted from the orignal app of Shimmer to only using the GSR sensor data.
 */

public class ConfigureActivity extends Activity{
	// Return Intent extra
    public static String mDone = "Done";
	private int mReturnEnabledSensors = 0;
	
	private final int SENSOR_ACCEL=0x80;
	private final int SENSOR_GSR=0x04;
		
	public void GSRSensor(){
		mReturnEnabledSensors=mReturnEnabledSensors | SENSOR_GSR | SENSOR_ACCEL;
		Intent intent = new Intent();
        intent.putExtra(mDone, mReturnEnabledSensors);
        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent);
        mReturnEnabledSensors=0;
		finish();
	   
	   }
}
	
   
