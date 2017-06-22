package id.odt.popularmovies;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.odt.popularmovies.adapter.ReviewsAdapter;
import id.odt.popularmovies.adapter.TrailerAdapter;
import id.odt.popularmovies.config.APIService;
import id.odt.popularmovies.config.MyLinearLayoutManager;
import id.odt.popularmovies.model.MoviesModel;
import id.odt.popularmovies.model.MoviesResult;
import id.odt.popularmovies.model.ReviewsModel;
import id.odt.popularmovies.model.ReviewsResult;
import id.odt.popularmovies.model.TrailerModel;
import id.odt.popularmovies.model.TrailerResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private String TAG;

    @BindView(R.id.backdrop)
    KenBurnsView backdrop;
    @BindView(R.id.img_movie)
    ImageView mMoviePoster;
    @BindView(R.id.title)
    TextView tvTitle;
    @BindView(R.id.month)
    TextView tvMonth;
    @BindView(R.id.rating)
    TextView tvRating;
    @BindView(R.id.deskripsi)
    TextView tvDescriptions;
    @BindView(R.id.tvNoReviews)
    TextView tvNoReviews;
    @BindView(R.id.rv_review)
    RecyclerView rv_review;
    @BindView(R.id.rv_trailer)
    RecyclerView rv_trailer;

    private MyLinearLayoutManager mLinearLayoutManager;
    private LinearLayoutManager mLinearLayoutManagerHorizontal;
    private ReviewsAdapter mReviewsAdapter;
    private TrailerAdapter mTrailerAdapter;
    private ArrayList<ReviewsResult> reviewsList;
    private ArrayList<TrailerResult> trailerList;
    private int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        TAG = getPackageName();
        ButterKnife.bind(this);

        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        reviewsList = new ArrayList<>();
        trailerList = new ArrayList<>();

        if(getIntent().getExtras()!=null) {
            Intent intent = getIntent();
            movieId = intent.getIntExtra("id",0);
            setTitle(intent.getStringExtra("title"));
            Picasso.with(this).load("http://image.tmdb.org/t/p/w500"+intent.getStringExtra("backdrop")).into(backdrop);
            Picasso.with(this).load("http://image.tmdb.org/t/p/w185"+intent.getStringExtra("poster")).into(mMoviePoster);
            tvTitle.setText(intent.getStringExtra("title"));
            try {
                Date dateInput = input.parse(intent.getStringExtra("release"));
                tvMonth.setText("Release date: "+output.format(dateInput));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            tvRating.setText("Average rating: "+intent.getDoubleExtra("rating",0));
            tvDescriptions.setText(intent.getStringExtra("descriptions"));

            mLinearLayoutManager = new MyLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mLinearLayoutManagerHorizontal = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            rv_trailer.setLayoutManager(mLinearLayoutManagerHorizontal);
            rv_review.setLayoutManager(mLinearLayoutManager);
            mReviewsAdapter = new ReviewsAdapter(reviewsList, this);
            mTrailerAdapter = new TrailerAdapter(trailerList, this);
            rv_review.setAdapter(mReviewsAdapter);
            rv_review.setNestedScrollingEnabled(false);
            rv_trailer.setAdapter(mTrailerAdapter);
            rv_trailer.setNestedScrollingEnabled(false);
            loadReviews();
            loadTrailer();
        }
    }

    private void loadTrailer() {
        trailerList.clear();

        APIService service = APIService.retrofit.create(APIService.class);
        Call<TrailerModel> call = service.movieTrailer(movieId);
        call.enqueue(new Callback<TrailerModel>() {
            @Override
            public void onResponse(Call<TrailerModel> call, Response<TrailerModel> response) {
                if (response.isSuccessful()) {
                    TrailerModel movies = response.body();
                    if(movies.getResults().size() <= 0) {
                        rv_trailer.setVisibility(View.GONE);
                    } else {
                        for (int i = 0; i < movies.getResults().size(); i++) {
                            TrailerResult moviesData = movies.getResults().get(i);
                            trailerList.add(moviesData);
                            mTrailerAdapter.notifyDataSetChanged();
                        }
                        rv_trailer.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<TrailerModel> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void loadReviews() {
        reviewsList.clear();

        APIService service = APIService.retrofit.create(APIService.class);
        Call<ReviewsModel> call = service.movieReviews(movieId);
        call.enqueue(new Callback<ReviewsModel>() {
            @Override
            public void onResponse(Call<ReviewsModel> call, Response<ReviewsModel> response) {
                if (response.isSuccessful()) {
                    ReviewsModel movies = response.body();
                    if(movies.getResults().size() <= 0) {
                        tvNoReviews.setVisibility(View.VISIBLE);
                        rv_review.setVisibility(View.GONE);
                    } else {
                        for (int i = 0; i < movies.getResults().size(); i++) {
                            ReviewsResult moviesData = movies.getResults().get(i);
                            reviewsList.add(moviesData);
                            mReviewsAdapter.notifyDataSetChanged();
                        }
                        rv_review.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ReviewsModel> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
