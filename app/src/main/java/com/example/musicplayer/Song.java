package com.example.musicplayer;

public class Song {
    private String title;
    private String path;
    private String artist;
    private String album;
    private String duration;

    public Song(String title, String path, String artist, String album, String duration) {
        this.title = title;
        this.path = path;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getDuration() {
        return duration;
    }
}
