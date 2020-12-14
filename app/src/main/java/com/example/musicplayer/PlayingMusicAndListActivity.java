package com.example.musicplayer;

import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.media.ToneGenerator.MAX_VOLUME;
import static com.example.musicplayer.MainActivity.musicList;

public class PlayingMusicAndListActivity extends AppCompatActivity {

    TextView songName, endTime, startTime;
    ImageView previous_btn, next_btn, play_btn, sound_btn, songImage, repeat_btn;
    SeekBar seekBar;
    int position;
    ReferPosition referPosition = new ReferPosition();
    static MediaPlayer mediaPlayer;
    static Uri uri;
    private RecyclerView recyclerView;
    ArrayList<Song> songArrayList;
    Handler handler = new Handler();
    Handler positionHandler = new Handler();
    Thread prevThread, nextThread, startstopThread, positionThread;
    AudioManager audioManager;
    boolean soundCheck;
    int volumeProgress;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_playing_music_and_list);
        initViews();
        soundCheck = false;
        volumeProgress = 50;
        startItemMethod();
        mediaPlayer.setVolume((float) (1 - (Math.log(MAX_VOLUME - volumeProgress) / Math.log(MAX_VOLUME))),
                (float) (1 - (Math.log(MAX_VOLUME - volumeProgress) / Math.log(MAX_VOLUME))));

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null){
                    if(soundCheck == false) {
                        int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);
                    }
                }
                handler.postDelayed(this, 250);
            }
        });
        t.start();

        positionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (referPosition.getPreviousPosition() != referPosition.getPosition()) {
                    referPosition.setPreviousPosition(referPosition.getPosition());
                    position = referPosition.getPosition();
                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                    metadataRetriever.setDataSource(songArrayList.get(position).getPath());
                    byte[] image = metadataRetriever.getEmbeddedPicture();
                    metadataRetriever.release();
                    if (image != null) {
                        Glide.with(getApplicationContext()).asBitmap().load(image).into(songImage);
                    } else {
                        songImage.setImageResource(R.drawable.music_icon);
                    }

                    mediaPlayer.stop();
                    boolean isLooping = mediaPlayer.isLooping();
                    mediaPlayer.release();
                    Uri uri = Uri.parse(songArrayList.get(position).getPath());
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                    mediaPlayer.setVolume((float) (1 - (Math.log(MAX_VOLUME - volumeProgress) / Math.log(MAX_VOLUME))),
                            (float) (1 - (Math.log(MAX_VOLUME - volumeProgress) / Math.log(MAX_VOLUME))));
                    mediaPlayer.setLooping(isLooping);
                    mediaPlayer.start();
                    if(soundCheck == false) {
                        seekBar.setMax(mediaPlayer.getDuration() / 1000);
                        seekBar.setProgress(0);
                        songName.setText(songArrayList.get(position).getTitle());
                    }
                    else{
                        seekBar.setProgress(volumeProgress);
                        songName.setText("Volume");
                        songName.setText("Volume");
                    }
                    play_btn.setImageResource(R.drawable.ic_pause);
                }
                positionHandler.postDelayed(this, 500);
            }
        });
        positionThread.start();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(soundCheck == false) {
                    int nowPosition = seekBar.getProgress() * 1000,
                            endPosition = mediaPlayer.getDuration() - seekBar.getProgress() * 1000;

                    String timeNow = String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(nowPosition),
                            TimeUnit.MILLISECONDS.toSeconds(nowPosition - (nowPosition / 60000) * 60000));

                    String timeEnd = String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(endPosition),
                            TimeUnit.MILLISECONDS.toSeconds(endPosition - (endPosition / 60000) * 60000));

                    if (seekBar.getProgress() >= seekBar.getMax() && mediaPlayer.isLooping() == false) {
                        next_btn.performClick();
                    }

                    startTime.setText(timeNow);
                    endTime.setText(timeEnd);
                }
                else{
                    float volume = (float) (1 - (Math.log(MAX_VOLUME - i) / Math.log(MAX_VOLUME)));
                    mediaPlayer.setVolume(volume, volume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(soundCheck == false) {
                    mediaPlayer.seekTo(seekBar.getProgress() * 1000);
                }
                else{
                    volumeProgress = seekBar.getProgress();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        View.OnClickListener playOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer != null) {
                    if (mediaPlayer.isPlaying() == true) {
                        play_btn.setImageResource(R.drawable.ic_play_arrow);
                        mediaPlayer.pause();
                    }
                    else {
                        play_btn.setImageResource(R.drawable.ic_pause);
                        mediaPlayer.start();
                    }
                }
            }
        };
        View.OnClickListener prevOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(position - 1 >= 0){
                    position = position - 1;
                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                    metadataRetriever.setDataSource(songArrayList.get(position).getPath());
                    byte[] image = metadataRetriever.getEmbeddedPicture();
                    metadataRetriever.release();
                    if(image != null){
                        Glide.with(getApplicationContext()).asBitmap().load(image).into(songImage);
                    }
                    else{
                        songImage.setImageResource(R.drawable.music_icon);
                    }
                }
                mediaPlayer.stop();
                boolean isLooping = mediaPlayer.isLooping();
                mediaPlayer.release();
                Uri uri = Uri.parse(songArrayList.get(position).getPath());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.setVolume((float) (1 - (Math.log(MAX_VOLUME - volumeProgress) / Math.log(MAX_VOLUME))),
                        (float) (1 - (Math.log(MAX_VOLUME - volumeProgress) / Math.log(MAX_VOLUME))));
                mediaPlayer.setLooping(isLooping);
                mediaPlayer.start();
                if(soundCheck == false) {
                    seekBar.setMax(mediaPlayer.getDuration() / 1000);
                    seekBar.setProgress(0);
                    songName.setText(songArrayList.get(position).getTitle());
                }
                else{
                    seekBar.setMax(MAX_VOLUME);
                    seekBar.setProgress(volumeProgress);
                    songName.setText("Volume");
                }
                play_btn.setImageResource(R.drawable.ic_pause);
            }
        };
        View.OnClickListener nextOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(position + 1 < songArrayList.size()){
                    position = position + 1;
                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                    metadataRetriever.setDataSource(songArrayList.get(position).getPath());
                    byte[] image = metadataRetriever.getEmbeddedPicture();
                    metadataRetriever.release();
                    if(image != null){
                        Glide.with(getApplicationContext()).asBitmap().load(image).into(songImage);
                    }
                    else{
                        songImage.setImageResource(R.drawable.music_icon);
                    }
                }
                mediaPlayer.stop();
                boolean isLooping = mediaPlayer.isLooping();
                mediaPlayer.release();
                Uri uri = Uri.parse(songArrayList.get(position).getPath());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.setVolume((float) (1 - (Math.log(MAX_VOLUME - volumeProgress) / Math.log(MAX_VOLUME))),
                        (float) (1 - (Math.log(MAX_VOLUME - volumeProgress) / Math.log(MAX_VOLUME))));
                mediaPlayer.setLooping(isLooping);
                mediaPlayer.start();
                if(soundCheck == false) {
                    seekBar.setMax(mediaPlayer.getDuration() / 1000);
                    seekBar.setProgress(0);
                    songName.setText(songArrayList.get(position).getTitle());
                }
                else{
                    seekBar.setMax(MAX_VOLUME);
                    seekBar.setProgress(volumeProgress);
                    songName.setText("Volume");
                }
                play_btn.setImageResource(R.drawable.ic_pause);
            }
        };
        View.OnClickListener repeatOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isLooping() == true){
                    repeat_btn.setImageResource(R.drawable.ic_repeat_one_gray);
                    mediaPlayer.setLooping(false);
                }
                else{
                    repeat_btn.setImageResource(R.drawable.ic_repeat_one_blue);
                    mediaPlayer.setLooping(true);
                }
            }
        };

        View.OnClickListener soundClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(soundCheck == false){
                    seekBar.setMax(MAX_VOLUME);
                    seekBar.setProgress(volumeProgress);
                    sound_btn.setImageResource(R.drawable.ic_volume_blue);
                    songName.setTextColor(Color.parseColor("#FFEF0E0E"));
                    songName.setText("Volume");
                    soundCheck = true;
                }
                else{
                    soundCheck = false;
                    seekBar.setMax(mediaPlayer.getDuration() / 1000);
                    seekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
                    sound_btn.setImageResource(R.drawable.ic_volume_gray);
                    songName.setTextColor(Color.parseColor("#FF03DAC6"));
                    songName.setText(songArrayList.get(position).getTitle());
                }
            }
        };
        sound_btn.setOnClickListener(soundClickListener);
        play_btn.setOnClickListener(playOnClickListener);
        previous_btn.setOnClickListener(prevOnClickListener);
        next_btn.setOnClickListener(nextOnClickListener);
        repeat_btn.setOnClickListener(repeatOnClickListener);


    }

    private void startItemMethod() {
        //RecycleView
        recyclerView.setHasFixedSize(true);
        RecycleViewAdapter recycleViewAdapter = new RecycleViewAdapter(musicList, getApplicationContext(), 1, referPosition);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        recyclerView.setAdapter(recycleViewAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        position = getIntent().getIntExtra("position", -1);
        songArrayList = musicList;
        if(songArrayList != null){
            play_btn.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(songArrayList.get(position).getPath());
        }
        if(mediaPlayer != null){
            mediaPlayer.stop();
            boolean isLooping = mediaPlayer.isLooping();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.setLooping(isLooping);
            mediaPlayer.start();
        }
        else{
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
        }
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        songName.setText(songArrayList.get(position).getTitle());

        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(songArrayList.get(position).getPath());
        byte[] image = metadataRetriever.getEmbeddedPicture();
        metadataRetriever.release();
        if(image != null){
            Glide.with(getApplicationContext()).asBitmap().load(image).into(songImage);
        }
    }

    private void initViews() {
        previous_btn = findViewById(R.id.previous_btn);
        next_btn = findViewById(R.id.next_btn);
        play_btn = findViewById(R.id.start_btn);
        sound_btn = findViewById(R.id.sound_image);
        soundCheck = false;
        repeat_btn = findViewById(R.id.repeat_image_view);
        songName = findViewById(R.id.SongName);
        songImage = findViewById(R.id.SongImage);
        startTime = findViewById(R.id.starttime);
        endTime = findViewById(R.id.endttime);
        seekBar = findViewById(R.id.SeekBar);
        recyclerView = findViewById(R.id.recycleview2);
    }
}
