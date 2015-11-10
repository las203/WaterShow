package eecs397.watershow;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SpectralPeakProcessor;
import be.tarsos.dsp.filters.BandPass;
import be.tarsos.dsp.filters.HighPass;
import be.tarsos.dsp.filters.LowPassFS;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AndroidAudioInputStream;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class AudioActivity extends AppCompatActivity{

    MediaPlayer player;
    AudioRecord recorder;
    Thread thread;
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
        setContentView(R.layout.activity_audio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button stop = (Button) findViewById(R.id.stop);
        stop.setText("Stop");
        stop.setOnClickListener(new View.OnClickListener() {
            public void  onClick(View v) {
                stopAudio();
            }
        });

        Button play = (Button) findViewById(R.id.play);
        play.setText("Play");
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("audio/mp3");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(
                        intent, "Open Audio (mp3) file"), RQS_OPEN_AUDIO_MP3);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RQS_OPEN_AUDIO_MP3) {
                Uri audioFileUri = data.getData();
                playAudio(audioFileUri);
            }
        }
    }

    void stopAudio() {
        if (player != null)
            player.release();
        if (recorder != null) {
            recorder.stop();
            recorder.release();
        }
        started = false;
    }

    void playAudio(Uri uri) {
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            player.setDataSource(getApplicationContext(), uri);
            player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        player.start();
/*
        BandPass lowFilter = new BandPass(300, 200, 44100);
        BandPass medFilter = new BandPass(2000, 1500, 44100);
        BandPass highFilter = new BandPass(8000, 4000, 44100);
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 2048, 0);

        SpectralPeakProcessor pdh = new SpectralPeakProcessor() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        };

        dispatcher.addAudioProcessor(lowFilter);
        dispatcher.addAudioProcessor(medFilter);
        dispatcher.addAudioProcessor(highFilter);
        new Thread(dispatcher,"Audio Dispatcher").start();*/
        started = true;
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    int blockSize = 2 * AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO,
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
                            Log.e("FilterValue", Byte.toString(aFilteredBuffer));
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
}
