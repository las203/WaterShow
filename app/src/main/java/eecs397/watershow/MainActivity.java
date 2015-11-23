package eecs397.watershow;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Handler;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.filters.BandPass;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    Button btnOn, btnOff;
    BluetoothAdapter adapter;
    BluetoothSocket socket;
    BluetoothDevice device;
    OutputStream stream;
    private static final String TAG = "bluetooth";
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "00:06:66:7B:AA:5D";
    boolean choosingMusic = false;

    MediaPlayer player;
    AudioRecord recorder;
    Thread thread;
    Uri file;
    BandPass lowFilter = new BandPass(300, 200, 44100);
    BandPass medFilter = new BandPass(2000, 1500, 44100);
    BandPass highFilter = new BandPass(8000, 4000, 44100);
    final static int RQS_OPEN_AUDIO_MP3 = 1;
    final static int ENCODING_PCM_16BIT = 2;
    boolean started = false;
    byte[] filteredBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOn = (Button) findViewById(R.id.btnOn);
        btnOn.setText("Play");
        btnOff = (Button) findViewById(R.id.btnOff);
        btnOff.setText("Stop");

        if (enableBluetooth()) {
            btnOn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
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
                    Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
                    choosingMusic = true;
                    /*Intent intent = new Intent();
                    intent.setType("audio/mp3");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(
                            intent, "Open Audio (mp3) file"), RQS_OPEN_AUDIO_MP3);*/
                    //playAudio(file);
                    choosingMusic = false;
                    playAudio();
                }
            });

            btnOff.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    /*sendData("0");
                    Log.d(TAG, "Click2");
                    Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
                    */
                    if (player != null)
                        player.release();
                    if (recorder != null) {
                        recorder.stop();
                        recorder.release();
                    }
                    started = false;
                }
            });
        }
    }

    void playAudio() {
        player = MediaPlayer.create(getApplicationContext(), R.raw.ghose);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.start();
        started = true;
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    int blockSize = 3 * AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
                    final TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(8000, 8, 1, true, false);
                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, blockSize);
                    if(recorder == null){
                        Log.e("AudioRecord", "Recorder is null");
                    }

                    byte[] byteBuffer = new byte[blockSize];
                    recorder.startRecording();


                    while (started) {
                        final int bufferReadResult = recorder.read(byteBuffer, 0, blockSize);
                        Log.e("AudioRecord", "Recording");
                        AudioEvent audioEvent = new AudioEvent(format, bufferReadResult);
                        ShortBuffer sbuf =
                                ByteBuffer.wrap(byteBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                        short[] audioShorts = new short[sbuf.capacity()];
                        sbuf.get(audioShorts);
                        float[] audioFloats = new float[audioShorts.length];
                        for (int j = 0; j < audioShorts.length; j++) {
                            audioFloats[j] = ((float)audioShorts[j])/0x8000;
                        }
                        audioEvent.setFloatBuffer(audioFloats);
                        highFilter.process(audioEvent);
                        filteredBuffer = audioEvent.getByteBuffer();
                        for (byte aFilteredBuffer : filteredBuffer) {
                            //Log.e("FilterValue", Byte.toString(aFilteredBuffer));
                            sendData(Byte.toString(aFilteredBuffer));
                        }
                    }

                    recorder.stop();
                    recorder.release();
                } catch (Throwable t) {
                    Log.e("AudioRecord", "Recording Failed");
                }
            }
        };
        thread = new Thread(runnable, "AudioDispatcher");
        thread.start();
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
                //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                Log.e("Bluetooth", "Enable bluetooth, please.");
                return false;
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

        try {
            socket.getOutputStream().write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }

}
