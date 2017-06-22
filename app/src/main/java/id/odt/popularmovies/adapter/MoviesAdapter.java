package id.odt.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

import id.odt.popularmovies.DetailActivity;
import id.odt.popularmovies.R;
import id.odt.popularmovies.model.MoviesModel;
import id.odt.popularmovies.model.MoviesResult;

/**
 * Created by arrival on 6/22/17.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder> {
    private ArrayList<MoviesResult> moviesList;
    private Context context;

    public MoviesAdapter(ArrayList<MoviesResult> moviesList, Context context) {
        this.moviesList = moviesList;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView img_movies;
        private TextView tv_title;
        private TextView tv_genre;
        private CardView cv_movie;

        public ViewHolder(View itemView) {
            super(itemView);
            img_movies = (ImageView) itemView.findViewById(R.id.img_movie);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_genre = (TextView) itemView.findViewById(R.id.tv_genre);
            cv_movie = (CardView) itemView.findViewById(R.id.cv_movie);
        }

        @Override
        public void onClick(View view) {

        }
    }

    @Override
    public MoviesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_item, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(MoviesAdapter.ViewHolder holder, final int position) {
        final MoviesResult movies = moviesList.get(position);
        if(movies.getTitle().length()>=20) {
            holder.tv_title.setText(movies.getTitle().substring(0,12)+"...");
        } else {
            holder.tv_title.setText(movies.getTitle());
        }

        holder.tv_genre.setText("Rating: "+movies.getVoteAverage().toString());
        Picasso.with(context).load("http://image.tmdb.org/t/p/w185"+movies.getPosterPath()).into(holder.img_movies);
        holder.cv_movie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra("backdrop", movies.getBackdropPath());
                i.putExtra("poster", movies.getPosterPath());
                i.putExtra("title", movies.getTitle());
                i.putExtra("release", movies.getReleaseDate());
                i.putExtra("rating", movies.getVoteAverage());
                i.putExtra("descriptions", movies.getOverview());
                i.putExtra("id", movies.getId());
                context.startActivity(i);
            }
        });

        animate(holder);
    }

    public  void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAlpha = AnimationUtils.loadAnimation(context, R.anim.activity_open_translate_from_bottom);
        viewHolder.itemView.setAnimation(animAlpha);
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}
