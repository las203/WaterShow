package eecs397.watershow;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.*;
import android.content.Intent;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    Button btnOn, btnOff;
    BluetoothAdapter adapter;
    BluetoothSocket socket;
    BluetoothDevice device;
    private static final String TAG = "bluetooth";
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "00:06:66:7B:AA:5D";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOn = (Button) findViewById(R.id.btnOn);
        btnOff = (Button) findViewById(R.id.btnOff);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        if (enableBluetooth()) {
            btnOn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.d(TAG, "Click 1");
                    device = adapter.getRemoteDevice(address);
                    //if (device.getBondState()==device.BOND_BONDED) {

                    Log.d(TAG, device.getName());
                        //BluetoothSocket mSocket=null;
                        try {


                            socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            Log.d(TAG, "socket not created");
                            e1.printStackTrace();
                        }
                        try {

                            socket.connect();
                        } catch (IOException e) {
                            try {

                                socket.close();
                                Log.d(TAG, "Cannot connect");
                            } catch (IOException e1) {
                                Log.d(TAG, "Socket not closed");
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                        sendData("1");
                    //}
                    Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
                }
            });

            btnOff.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    sendData("0");
                    Log.d(TAG, "Click2");
                    Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    boolean enableBluetooth() {
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            FragmentManager manager = getFragmentManager();;
            DialogFragment newFragment = new NoBluetoothFragment();
            newFragment.show(manager, "nobluetooth");
            return false;
        }
        else {
            if (!adapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            return true;
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        //Log.d(TAG, "...Send data: " + message + "...");

        try {
            socket.getOutputStream().write(msgBuffer);
            Log.e("Sending", "Stuff");
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }

}
