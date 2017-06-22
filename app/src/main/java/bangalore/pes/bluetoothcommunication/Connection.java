package bangalore.pes.bluetoothcommunication;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Connection extends AppCompatActivity {
    String address = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter myBluetooth = null;
    public static String EXTRA_ADDRESS = "device_address";
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    Button StartConnection;
    Button Disconnect;
    Button Recieve;
    TextView incomingData;
    InputStream mmInStream=null;
    String incomingMessage;
    StringBuilder messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        Intent newint = getIntent();
        messages = new StringBuilder();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);
        StartConnection=(Button)findViewById(R.id.button5);
        Disconnect=(Button)findViewById(R.id.button6);
        incomingData=(TextView)findViewById(R.id.textView);
        Recieve=(Button)findViewById(R.id.button7);
        StartConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
               new ConnectBT().execute();


            }
        });
        Disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btSocket!=null) //If the btSocket is busy
                {
                    try
                    {
                        btSocket.close(); //close connection
                    }
                    catch (IOException e)
                    { }
                }
                finish();
            }
        });
        Recieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Handler handler=new Handler();
                Runnable runnable=new Runnable() {
                    @Override
                    public void run() {
                        ListenInput.start();
                        incomingData.setText(messages);
                        handler.postDelayed(this,200);
                    }
                };
                runnable.run();
                //ListenInput.start();


               // byte[] buffer = new byte[1024];  // buffer store for the stream

                //int bytes; // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs
                /*while (true) {
                    // Read from the InputStream
                    try {
                        if(mmInStream==null)
                        {
                            Log.d("","InputStream is null");
                        }
                        bytes = mmInStream.read(buffer);
                        String incomingMessage = new String(buffer, 0, bytes);
                        incomingData.setText(incomingMessage);


                    } catch (IOException e) {


                    }
            }*/
        }});

    }
    Thread ListenInput=new Thread(){
        @Override
        public void run() {
            try {
                mmInStream=btSocket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytes;
                while (true) {
                    // Read from the InputStream
                    try {
                        if(mmInStream==null)
                        {
                            Log.d("","InputStream is null");
                        }
                        bytes = mmInStream.read(buffer);
                        incomingMessage = new String(buffer, 0, bytes);
                        messages.append(incomingMessage);





                    } catch (IOException e) {


                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    };


    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            Toast.makeText(getApplicationContext(),"Connecting....",Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    ActivityCompat.requestPermissions(Connection.this,new String[]{Manifest.permission.BLUETOOTH},1);
                    ActivityCompat.requestPermissions(Connection.this,new String[]{Manifest.permission.BLUETOOTH_ADMIN},1);
                    ActivityCompat.requestPermissions(Connection.this,new String[]{Manifest.permission.BLUETOOTH_PRIVILEGED},1);

                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device

                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);

                    //connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                Toast.makeText(getApplicationContext(),"Connection Failed",Toast.LENGTH_SHORT).show();
                finish();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Connection Successful",Toast.LENGTH_SHORT).show();
                isBtConnected = true;
            }

        }
    }
}
