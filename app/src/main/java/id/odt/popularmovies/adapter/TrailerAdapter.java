package id.odt.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import id.odt.popularmovies.DetailActivity;
import id.odt.popularmovies.R;
import id.odt.popularmovies.model.MoviesResult;
import id.odt.popularmovies.model.TrailerResult;

/**
 * Created by arrival on 6/22/17.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {
    private ArrayList<TrailerResult> moviesList;
    private Context context;

    public TrailerAdapter(ArrayList<TrailerResult> moviesList, Context context) {
        this.moviesList = moviesList;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView img_trailer;
        private ImageView img_play;

        public ViewHolder(View itemView) {
            super(itemView);
            img_trailer = (ImageView) itemView.findViewById(R.id.img_trailer);
            img_play = (ImageView) itemView.findViewById(R.id.img_play);
        }

        @Override
        public void onClick(View view) {

        }
    }

    @Override
    public TrailerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trailer_item, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(TrailerAdapter.ViewHolder holder, final int position) {
        final TrailerResult movies = moviesList.get(position);
        Picasso.with(context).load("https://img.youtube.com/vi/"+movies.getKey()+"/0.jpg").into(holder.img_trailer);
        holder.img_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v="+movies.getKey()));
                try {
                    context.startActivity(intent);
                } catch (Exception ex) {
                    Toast.makeText(context, "No apps support to open the url!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        animate(holder);
    }

    public  void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAlpha = AnimationUtils.loadAnimation(context, R.anim.up);
        viewHolder.itemView.setAnimation(animAlpha);
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}
