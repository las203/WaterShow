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
                //Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(intent, 10);
                playAudio();
            }
        });
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_OK && requestCode == 10) {
            Uri uriSound = data.getData();
            //playAudio(uriSound);
        }
    }*/

    void stopAudio() {
        if (player != null)
            player.release();
    }

    void playAudio() {
        player = MediaPlayer.create(this, R.raw.ghost);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.start();
    }

    //Not quite sure how to do this easily yet
    void pickMusic() {

    }
}
