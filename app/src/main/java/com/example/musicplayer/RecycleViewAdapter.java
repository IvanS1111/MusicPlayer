package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {
    ArrayList<Song> songsList;
    Context context;
    int type_action;
    ReferPosition referPosition;

    public RecycleViewAdapter(ArrayList<Song> songsList, Context context, int type_action, ReferPosition referPosition){
        this.songsList = songsList;
        this.context = context;
        this.type_action = type_action;
        this.referPosition = referPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.songs_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.text.setText(songsList.get(position).getTitle());

        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(songsList.get(position).getPath());
        byte[] image = metadataRetriever.getEmbeddedPicture();
        metadataRetriever.release();
        if(image != null){
            Glide.with(context).asBitmap().load(image).into(holder.image);
        }

        if(type_action == 0) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PlayingMusicAndListActivity.class);
                    intent.putExtra("position", position);
                    context.startActivity(intent);
                }
            });
        }
        if(type_action == 1){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    referPosition.setPosition(position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView text;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageview1);
            text = itemView.findViewById(R.id.textview1);
        }
    }
}
