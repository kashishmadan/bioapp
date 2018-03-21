package nl.tue.ppeters.flower;
 
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * This class filters the GSR data received by the Shimmer device and communicates the results back to GraphView.
 * The filters that are applied here are a low pass filter and a high pass filter to filter out the Tonic and Phasic domains
 * of the GSR data.
 * @author Misha
 *
 */


public class Filters extends Activity {
	public int y;
	public int z;
	public int inputLength = 1;
	public float ALPHA = 0.1f;	//was 0.1f
	public float ALPHA50Hz = 50.0f; //was 0.01f -> 0,001f
	public float ALPHATonic = 1.0f; //was 0.01f -> 0,001f
	public float ALPHAPhasic = 0.5f; //was 0.05f -> 0,01f
	public float input;
	public float output;
	public float input50Hz;
	public float output50Hz;
	public float inputTonic;
	public float outputTonic;
	public float inputPhasic;
	public float outputPhasic;
	public float inputOudPhasic;
	public float filteredValue;
	public String peakOud;
	
	
	
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	 }
	 
	 public void filterData(float data, float outputOud50Hz, float inputOudPhasic, float outputOud, float outputOudTonic, float outputOudPhasic){
	    	try {        
	    		input = data;	
	    		
	        	
/////PEAK DETECTION///////
	   
/////50Hz LPF/////////////
	    		
	    		/*input50Hz = data;
	        	
	        	output50Hz = outputOud50Hz + ALPHA50Hz * (input50Hz - outputOud50Hz);
	        	   	
	        	outputOud50Hz= output50Hz;*/
	    		
/////50Hz LPF/////////////
	        	
/////TONIC FILTERING LPF/////////////
	        	
	        	inputTonic = data;
	        	
	        	outputTonic = outputOudTonic + ALPHATonic * (inputTonic - outputOudTonic);
	        	   	
	        	outputOudTonic = outputTonic;
	        	
/////TONIC FILTERING/////////////
	        	
	        	
/////PHASIC FILTERING HPF/////////////
	        	
	        	inputPhasic = outputTonic;
	        	//inputPhasic = input;
	        	
	        	outputPhasic = ALPHAPhasic * (outputOudPhasic + inputPhasic - inputOudPhasic);
	        	
	        	outputOudPhasic = outputPhasic;
	        	inputOudPhasic = input;
	        			
	        	
/////PHASIC FILTERING HPF/////////////
			} 
		
	        catch (Exception e) {
	        	
	            Log.d("Downloader", e.getMessage());
	        }
	    	
	    	
	    }
//the following instances are called by GraphView to obtain the various filtered data elements	    
		public float getTonic(){
	    	return outputTonic;
	    	
	    }
	    
	    public float getPhasic(){
	    	return outputPhasic;
	    	
	    }
	    
	    public float get50Hz(){
	    	return output50Hz;
	    	
	    }
	    
	    public float getOutput(){
	    	return output;
	    	
	    }
	    public float getInput(){
	    	return input;
	    }
	    
	  
	 }
