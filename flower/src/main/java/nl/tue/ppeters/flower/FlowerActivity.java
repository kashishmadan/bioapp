package nl.tue.ppeters.flower;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

//import com.shimmerresearch.driver.Shimmer;

public class FlowerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Debugging
    private static final String TAG = FlowerActivity.class.getName();
    private static boolean D = true;
    private static final String preferencesFileName = "Flower.prefs";

    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE2 = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    //Message types sent from ShimmerGraph Handler
    // Intent request codes
    private static Context context;
    static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PREFER_SHIMMER = 2;
    private static final int REQUEST_CONNECT_SHIMMER = 3;
    private static final int REQUEST_CONFIGURE_SHIMMER = 7;
    private static final int REQUEST_CONFIGURE_VIEW_SENSOR = 4;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 5;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 6;

    // Member object for communication services
    private static Shimmer mShimmerDevice = null;
    public BluetoothService mChatService = null;

    //private final BluetoothSocket socket;
    // Local Bluetooth adapter Shimmer
    private BluetoothAdapter mBluetoothAdapter = null; //SHIMMER

    // Name of the connected device
    public static String mConnectedDeviceName = null;

    private static int mGraphSubSamplingCount = 0;

    private static GraphView mGraph;
    private static boolean graphViewed = false;
    private static boolean outlierViewed = false;
    private static String mSensorView = "GSR";
    private static final String Calibrated = "CAL";
    private static final String Uncalibrated = "RAW";
    public static String buttonColor = "-";
    private SharedPreferences app_preferences;
    private static NavigationView navigationView;
    private static View mainView;
    private static DrawerLayout drawerLayout;
    private static boolean flowerViewed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.flower_preferences, false);
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String serverUrl = app_preferences.getString("server_url_main","");
        Log.i(TAG, "Server URL: " + serverUrl);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                prepareNavMenu();
                drawerView.invalidate();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                prepareNavMenu();
                drawerView.invalidate();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mainView = findViewById(R.id.main_view);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.no_bluetooth + "\n" + R.string.exiting, Toast.LENGTH_LONG).show();
            finish();
        }
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    @Override
    public void onStart() {
        super.onStart();
        D = readSharedBoolean("checkboxDebug",false);
        if (!mBluetoothAdapter.isEnabled()) {
            shortToaster("Activate Bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            setupMain();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mShimmerDevice != null) mShimmerDevice.stop();
        mShimmerDevice = null;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void prepareNavMenu() {
        //navigationView = (NavigationView) findViewById(R.id.nav_view)
        Menu navMenu = navigationView.getMenu().getItem(0).getSubMenu();
        if ((mShimmerDevice.getState() == Shimmer.STATE_CONNECTED)) {
            navMenu.getItem(0).setTitle(R.string.disconnect);
            navMenu.getItem(1).setEnabled(true);
            if (mShimmerDevice.getStreamingStatus())
                navMenu.getItem(1).setTitle(R.string.stopstream);
            else
                navMenu.getItem(1).setTitle(R.string.startstream);
        } else {
            navMenu.getItem(0).setTitle(R.string.connect);
            navMenu.getItem(1).setTitle(R.string.startstream);
            navMenu.getItem(1).setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.About) {
            Intent openAboutPanel = new Intent(FlowerActivity.this,
                    AboutActivity.class);
            startActivity(openAboutPanel);
        } else if (item.getItemId() == R.id.Preferences) {
            Intent openPreferencesPanel = new Intent(FlowerActivity.this,
                    PreferencesActivity.class);
            startActivity(openPreferencesPanel);
        } else if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //   @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here
        int id = item.getItemId();
        //navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (id == R.id.Connect) {
            if ((mShimmerDevice.getState() == Shimmer.STATE_CONNECTED)) {
                mShimmerDevice.stop();
                //mShimmerDevice = new Shimmer(this, mHandler,"Device 1",false);
                shortsnack("No connection");
                // there should be a cleaner way to reset the navigation drawer item titles...
            } else {
                if (mShimmerDevice.getState() == Shimmer.STATE_NONE) {
                    //connect shimmer
                    String btAddress = readSharedSetting("bluetoothaddress", "");
                    if (btAddress.equals("")) snack("Configure bluetooth address first");
                    else {
                        connect(btAddress);

                    }
                }
//                    Intent serverIntent = new Intent(this, FlowerActivity.class);
//                    startActivityForResult(serverIntent, REQUEST_CONNECT_SHIMMER);
            }
        } else if (id == R.id.ConfigureBT) {
            //find shimmer
            Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_PREFER_SHIMMER);
            shortsnack("Searching for Shimmer device");
        } else if (id == R.id.Start) {
            if (mShimmerDevice.getState() == Shimmer.STATE_CONNECTED) {
                if (mShimmerDevice.getStreamingStatus()) {
                    mShimmerDevice.stopStreaming();
                    shortsnack("Program was stopped");
                } else {
                    mShimmerDevice.startStreaming();
                    shortsnack("Program is started");
                }
            } else snack("Establish connection first");
        } else if (id == R.id.Refresh) {
        } else if (id == R.id.Exit) {
            shortsnack("Exiting");
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        prepareNavMenu();
        return true;
    }

    private void setupMain() {
        if (D) Log.d(TAG, "setupMain");
        flowerViewed = readSharedBoolean("checkboxFlower",false);
        graphViewed = readSharedBoolean("checkboxGraph",false);
        outlierViewed = readSharedBoolean("checkboxOutliers",false);
        String btAddress = readSharedSetting("bluetoothaddress", "");
        if (btAddress.equals("")) {
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_PREFER_SHIMMER);
        } else {
            mGraph = (GraphView)findViewById(R.id.graph);
            if (mShimmerDevice == null) {
                mShimmerDevice= new Shimmer(this, mHandler,"Device 1",false);
            }
        }
    }

    private void connect(String btAddress) {
        //BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(btAddress);
        //Log.d(TAG, device.toString());
        //mShimmerDevice.stop();
        //mShimmerDevice.connect(device,"default");
        mShimmerDevice.stop();
        mShimmerDevice.connect(btAddress,"default");
        //mShimmerDevice.setgetdatainstruction("a");
    }

    private static void shortToaster(String msg) {
        Toast.makeText(FlowerActivity.context, msg, Toast.LENGTH_SHORT).show();
    }

    private static void toaster(String msg) {
        Toast.makeText(FlowerActivity.context, msg, Toast.LENGTH_LONG).show();
    }

    public static boolean viewGraph() {
        return graphViewed;
    }

    public static boolean viewFlower() {
        return flowerViewed;
    }

    public static boolean viewOutlier() {
        return outlierViewed;
    }

    public static String getButtonColor() {
        return buttonColor;
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            //if (D) Log.d(TAG, "mHandler " + msg.toString());
            //drawerLayout.invalidate();

            switch (msg.what) {

                case Shimmer.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "State change: " + msg.toString());
                    switch (msg.arg1) {
                        case Shimmer.STATE_CONNECTED:
                            mShimmerDevice.writeEnabledSensors(Shimmer.SENSOR_GSR);
                            shortsnack("Successfully connected");
                            break;
                        case Shimmer.STATE_CONNECTING:
                            shortsnack("Connecting...");
                            break;
                        case Shimmer.STATE_NONE:
                            shortsnack("No connection");
                            break;
                    }
                    break;

                case Shimmer.MESSAGE_READ:
                    Log.d(TAG, "Message read: " + msg.toString());
                    if ((msg.obj instanceof ObjectCluster)) {
                        ObjectCluster objectCluster = (ObjectCluster) msg.obj;

                        int[] dataArray = new int[0];
                        double[] calibratedDataArray = new double[0];
                        String[] sensorName = new String[0];
                        String units = "";
                        String calibratedUnits = "";
                        //mSensorView determines which sensor to graph
                        if (mSensorView.equals("Accelerometer")) {
                            sensorName = new String[3]; // for x y and z axis
                            dataArray = new int[3];
                            calibratedDataArray = new double[3];
                            sensorName[0] = "AccelerometerX";
                            sensorName[1] = "AccelerometerY";
                            sensorName[2] = "AccelerometerZ";
                        }

                        if (mSensorView.equals("GSR")) {
                            Log.d(TAG, "GSR sensor: ");
                            sensorName = new String[1];
                            dataArray = new int[1];
                            calibratedDataArray = new double[1];
                            sensorName[0] = "GSR";
                        }

                        if (mSensorView.equals("TimeStamp")) {
                            sensorName = new String[1];
                            dataArray = new int[1];
                            calibratedDataArray = new double[1];
                            sensorName[0] = "TimeStamp";
                        }

                        String deviceName = objectCluster.mMyName;
                        if (deviceName == "Device 1" && sensorName.length != 0) {  // ShimmerDevice is the assigned user id, see constructor of the Shimmer
                            if (sensorName.length > 0) {
                                Log.d(TAG, "Sensor: "+sensorName[0]);
                                Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[0]);  // first retrieve all the possible formats for the current sensor device
                                logFormatClusterCollection(ofFormats);
                                FormatCluster formatCluster = ((FormatCluster) objectCluster.returnFormatCluster(ofFormats, "CAL"));
                                if (formatCluster != null) {
                                    Log.d(TAG,"Formatcluster: "+formatCluster.toString());
                                    //Obtain data for text view
                                    calibratedDataArray[0] = formatCluster.mData;
                                    calibratedUnits = formatCluster.mUnits;
                                    //Obtain data for graph
                                    if (sensorName[0] == "GSR") { // Heart Rate has no uncalibrated data
                                        dataArray[0] = (int) ((FormatCluster) objectCluster.returnFormatCluster(ofFormats, "CAL")).mData;
                                        Log.d(TAG, "Sensor: "+sensorName[0]+"; Data:: "+dataArray[0]);
                                        units = ((FormatCluster) objectCluster.returnFormatCluster(ofFormats, Calibrated)).mUnits;
                                    } else {
                                        dataArray[0] = (int) ((FormatCluster) objectCluster.returnFormatCluster(ofFormats, Uncalibrated)).mData;
                                    }
                                    units = ((FormatCluster) objectCluster.returnFormatCluster(ofFormats, Uncalibrated)).mUnits; //TODO: Update data structure to include Max and Min values. This is to allow easy graph adjustments for the length and width
                                }
                            }
                            if (sensorName.length > 1) {
                                Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[1]);  // first retrieve all the possible formats for the current sensor device
                                FormatCluster formatCluster = ((FormatCluster) objectCluster.returnFormatCluster(ofFormats, Calibrated));
                                if (formatCluster != null) {
                                    calibratedDataArray[1] = formatCluster.mData;
                                    //Obtain data for text view

                                    //Obtain data for graph
                                    dataArray[1] = (int) ((FormatCluster) objectCluster.returnFormatCluster(ofFormats, Uncalibrated)).mData;
                                    units = ((FormatCluster) objectCluster.returnFormatCluster(ofFormats, Uncalibrated)).mUnits; //TODO: Update data structure to include Max and Min values. This is to allow easy graph adjustments for the length and width

                                }
                            }
                            if (sensorName.length > 2) {

                                Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[2]);  // first retrieve all the possible formats for the current sensor device
                                FormatCluster formatCluster = ((FormatCluster) objectCluster.returnFormatCluster(ofFormats, Calibrated));
                                if (formatCluster != null) {
                                    calibratedDataArray[2] = formatCluster.mData;


                                    //Obtain data for graph
                                    dataArray[2] = (int) ((FormatCluster) objectCluster.returnFormatCluster(ofFormats, Uncalibrated)).mData;
                                    units = ((FormatCluster) objectCluster.returnFormatCluster(ofFormats, Uncalibrated)).mUnits; //TODO: Update data structure to include Max and Min values. This is to allow easy graph adjustments for the length and width
                                }

                            }
                            //in order to prevent LAG the number of data points plotted is REDUCED
                            int maxNumberofSamplesPerSecond = 50; //Change this to increase/decrease the number of samples which are graphed
                            int subSamplingCount = 0;
                            if (mShimmerDevice.getSamplingRate() > maxNumberofSamplesPerSecond) {
                                subSamplingCount = (int) (mShimmerDevice.getSamplingRate() / maxNumberofSamplesPerSecond);
                                mGraphSubSamplingCount++;
                            }
                            if (mGraphSubSamplingCount == subSamplingCount) {
                                mGraph.setDataWithAdjustment(dataArray, "Shimmer : " + deviceName, units);
                                mGraphSubSamplingCount = 0;
                            }
                        }
                    }

                    break;

                case Shimmer.MESSAGE_ACK_RECEIVED:
                    break;

                case Shimmer.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = mShimmerDevice.getBluetoothAddress();
                    if (D) shortToaster("Connected to sock " + mConnectedDeviceName);
                    break;

                case Shimmer.MESSAGE_TOAST:
                    //if (D) shortToaster(msg.getData().getString(Shimmer.TOAST));
                    break;

                default:
                    Log.d(TAG, "Unhandled message in mHandler: " + msg.toString());
                    break;

            }
        }
    };

    public void logFormatClusterCollection(Collection<FormatCluster> collectionFormatCluster){
        Iterator<FormatCluster> iFormatCluster=collectionFormatCluster.iterator();
        FormatCluster formatCluster;

        while(iFormatCluster.hasNext()){
            formatCluster=(FormatCluster)iFormatCluster.next();
            Log.d(TAG,"Formatclustercollection: "+formatCluster.mFormat + " : "+formatCluster.mUnits+" : "+formatCluster.mData);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + requestCode + "," + resultCode);

        BluetoothSocket tmp = null;

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    if (D) shortsnack("Bluetooth is now enabled, setting up Shimmer device");
                    setupMain();
                } else {
                    // User did not enable Bluetooth or an error occured
                    if (D) shortsnack("Bluetooth enabling failed, exiting...");
                    finish();
                }
                break;
            case REQUEST_PREFER_SHIMMER:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String btAddress = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    saveSharedSetting(FlowerActivity.context, "bluetoothaddress", btAddress);
                    setupMain();
                    //if (D) shortToaster("Opgeslagen bluetooth device adres: " + btAddress);
                }
                break;
            case REQUEST_CONNECT_SHIMMER:
                if (resultCode == Activity.RESULT_OK) {
                    if (mShimmerDevice.getState() == Shimmer.STATE_NONE) {
                        //connect shimmer
                        String btAddress = readSharedSetting("bluetoothaddress", "");
                        if (btAddress.equals("")) snack("Configure bluetooth address first");
                        else {
                            connect(btAddress);
                        }
                    }
                }
                break;
            case REQUEST_CONFIGURE_SHIMMER:
                if (resultCode == Activity.RESULT_OK) {
                    mShimmerDevice.writeEnabledSensors(Shimmer.SENSOR_GSR);
                }
                break;
            case REQUEST_CONFIGURE_VIEW_SENSOR:
                if (resultCode == Activity.RESULT_OK) {
                    //mSensorView=data.getExtras().getString(ConfigureActivity.mDone);
                    mSensorView = "GSR";
                    if (mSensorView.equals("GSR")) {
                        //
                    }
                }

                break;
            default:
                Log.d(TAG, "Unhandled requestcode in onActivityResult: " + requestCode);
                break;
        }
    }

    public void colorButtonSelected(View view) {
        ImageButton b = (ImageButton) view;
        boolean selected = !b.isSelected();
        findViewById(R.id.colorButton1).setSelected(false); //deselect all other buttons
        findViewById(R.id.colorButton2).setSelected(false);
        findViewById(R.id.colorButton3).setSelected(false);
        findViewById(R.id.colorButton4).setSelected(false);
        findViewById(R.id.colorButton5).setSelected(false);
        findViewById(R.id.colorButton6).setSelected(false);
        b.setSelected(selected);
        if (selected) buttonColor = b.getContentDescription().toString();
        else buttonColor = "-";
        if (D) shortToaster("Button color: " + buttonColor);
    }

    private static Snackbar snackbar;

    private static void snack(String message) {
        if (snackbar != null) snackbar.dismiss();
        snackbar = Snackbar.make(mainView, message, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(Color.WHITE)
                .setAction("X", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
        snackbar.show();
    }

    private static void shortsnack(String message) {
        if (snackbar != null) snackbar.dismiss();
        Snackbar.make(mainView, message, Snackbar.LENGTH_SHORT)
                .setActionTextColor(Color.WHITE).show();
    }

    private void shortsnack(int id) {
        if (snackbar != null) snackbar.dismiss();
        Snackbar.make(findViewById(R.id.main_view), getResources().getString(id), Snackbar.LENGTH_SHORT)
                .setActionTextColor(Color.WHITE).show();
    }

    public void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public String readSharedSetting(String settingName, String defaultValue) {
        return app_preferences.getString(settingName, defaultValue);
    }

    public boolean readSharedBoolean(String settingName, boolean defaultValue) {
        return app_preferences.getBoolean(settingName, false);
    }
}
