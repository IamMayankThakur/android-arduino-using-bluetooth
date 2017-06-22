package bangalore.pes.bluetoothcommunication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
Button EnableDisable;
Button Recieve;
Button Send;
Button EnableDiscoverable;
Button Discover;
    BluetoothAdapter mBluetoothAdapter;
    String deviceAddress;
    private static final String TAG="Main Activity";
    public static String EXTRA_ADDRESS = "device_address";
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    public DeviceListAdapter mDeviceListAdapter;

    ListView lvNewDevices;
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.activity_discover, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EnableDisable=(Button)findViewById(R.id.button);
        Recieve=(Button)findViewById(R.id.button4);
//        Send=(Button)findViewById(R.id.button11);
        EnableDiscoverable=(Button)findViewById(R.id.button2);
        Discover=(Button)findViewById(R.id.button3);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();
        lvNewDevices.setOnItemClickListener(MainActivity.this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        EnableDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBluetoothAdapter == null){
                    Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
                }
                if(!mBluetoothAdapter.isEnabled()){

                    Log.d(TAG, "enableDisableBT: enabling BT.");
                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBTIntent);

                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiver1, BTIntent);
            }
                if(mBluetoothAdapter.isEnabled()){
                    Log.d(TAG, "enableDisableBT: disabling BT.");
                    mBluetoothAdapter.disable();
                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBTIntent);

                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiver1, BTIntent);
        }
            }});
        EnableDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            }
        });
        Discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDiscover: Canceling discovery.");
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);

                    //check BT permissions in manifest

                    mBluetoothAdapter.startDiscovery();

                }
                if(!mBluetoothAdapter.isDiscovering()){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);

                    //check BT permissions in manifest
                    mBluetoothAdapter.startDiscovery();

                }
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
            }
        });
        Recieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent connection=new Intent(MainActivity.this,Connection.class);
                connection.putExtra(EXTRA_ADDRESS, deviceAddress);
                startActivity(connection);
            }
        });
        /*Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent connection=new Intent(MainActivity.this,Sending.class);
                connection.putExtra(EXTRA_ADDRESS, deviceAddress);
                startActivity(connection);

            }
        });*/

        }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver3);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBluetoothAdapter.cancelDiscovery();

        String deviceName = mBTDevices.get(i).getName();
        deviceAddress = mBTDevices.get(i).getAddress();
        mBTDevices.get(i).createBond();

        Intent connection=new Intent(MainActivity.this,Connection.class);
        connection.putExtra(EXTRA_ADDRESS, deviceAddress);




    }
}
