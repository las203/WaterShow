package eecs397.watershow;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.IOException;

public class AudioActivity extends AppCompatActivity {

    MediaPlayer player;
    final static int RQS_OPEN_AUDIO_MP3 = 1;

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
    }
}
