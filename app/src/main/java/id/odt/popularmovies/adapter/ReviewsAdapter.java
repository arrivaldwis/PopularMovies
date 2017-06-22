package id.odt.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;

import id.odt.popularmovies.DetailActivity;
import id.odt.popularmovies.R;
import id.odt.popularmovies.model.MoviesResult;
import id.odt.popularmovies.model.ReviewsModel;
import id.odt.popularmovies.model.ReviewsResult;

/**
 * Created by arrival on 6/22/17.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
    private ArrayList<ReviewsResult> moviesList;
    private Context context;

    public ReviewsAdapter(ArrayList<ReviewsResult> moviesList, Context context) {
        this.moviesList = moviesList;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tv_author;
        private TextView tv_content;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_author = (TextView) itemView.findViewById(R.id.tv_author);
            tv_content = (TextView) itemView.findViewById(R.id.tv_content);
        }

        @Override
        public void onClick(View view) {

        }
    }

    @Override
    public ReviewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reviews_item, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(ReviewsAdapter.ViewHolder holder, final int position) {
        final ReviewsResult movies = moviesList.get(position);
        holder.tv_author.setText(movies.getAuthor());

        if(movies.getContent().length()>=300) {
            holder.tv_content.setText(movies.getContent().substring(0, 295)+"...");
        } else {
            holder.tv_content.setText(movies.getContent());
        }

        animate(holder);
    }

    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAlpha = AnimationUtils.loadAnimation(context, R.anim.activity_open_translate_from_bottom);
        viewHolder.itemView.setAnimation(animAlpha);
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}
