package id.odt.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.odt.popularmovies.adapter.ReviewsAdapter;
import id.odt.popularmovies.adapter.TrailerAdapter;
import id.odt.popularmovies.config.APIService;
import id.odt.popularmovies.config.Constant;
import id.odt.popularmovies.config.MyLinearLayoutManager;
import id.odt.popularmovies.data.PopularMoviesContract;
import id.odt.popularmovies.model.MoviesModel;
import id.odt.popularmovies.model.MoviesResult;
import id.odt.popularmovies.model.ReviewsModel;
import id.odt.popularmovies.model.ReviewsResult;
import id.odt.popularmovies.model.TrailerModel;
import id.odt.popularmovies.model.TrailerResult;
import id.odt.popularmovies.sync.MovieDataLoader;
import id.odt.popularmovies.sync.PopularMoviesSyncAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

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
    @BindView(R.id.img_favourite)
    ImageView img_favourite;

    private MyLinearLayoutManager mLinearLayoutManager;
    private LinearLayoutManager mLinearLayoutManagerHorizontal;
    private ReviewsAdapter mReviewsAdapter;
    private TrailerAdapter mTrailerAdapter;
    private ArrayList<ReviewsResult> reviewsList;
    private ArrayList<TrailerResult> trailerList;
    private int movieId;
    private SharedPreferences mPrefs = null;
    private boolean mIsPreferenceChanged = false;
    private MenuItem mShareMenuItem;
    public static final String FAVORITE_MOVIE_IDS_SET_KEY = "movie_id_set_key";
    private Toast mFavoriteToast;
    private String mFirstTrailerUrl = null;
    private ShareActionProvider mShareActionProvider;
    public static final int COL_KEY = 4;
    private static final int MOVIE_TRAILER_LOADER = 0;
    private static final int MOVIE_REVIEW_LOADER = 1;
    public static final String[] MOVIE_TRAILER_COLUMNS = {
            PopularMoviesContract.MovieTrailerEntry.TABLE_NAME + "." + PopularMoviesContract.MovieTrailerEntry._ID,
            PopularMoviesContract.MovieTrailerEntry.TABLE_NAME + "." + PopularMoviesContract.MovieTrailerEntry.COLUMN_MOVIE_ID,
            PopularMoviesContract.MovieTrailerEntry.COLUMN_TRAILER_ID,
            PopularMoviesContract.MovieTrailerEntry.COLUMN_ISO_369_1,
            PopularMoviesContract.MovieTrailerEntry.COLUMN_KEY,
            PopularMoviesContract.MovieTrailerEntry.COLUMN_NAME,
            PopularMoviesContract.MovieTrailerEntry.COLUMN_SITE,
            PopularMoviesContract.MovieTrailerEntry.COLUMN_SIZE,
            PopularMoviesContract.MovieTrailerEntry.COLUMN_TYPE,
            PopularMoviesContract.MovieTrailerEntry.TABLE_NAME + "." + PopularMoviesContract.MovieTrailerEntry.COLUMN_DATE
    };

    public static final String[] MOVIE_REVIEWS_COLUMNS = {
            PopularMoviesContract.MovieReviewEntry.TABLE_NAME + "." + PopularMoviesContract.MovieReviewEntry._ID,
            PopularMoviesContract.MovieReviewEntry.TABLE_NAME + "." + PopularMoviesContract.MovieReviewEntry.COLUMN_MOVIE_ID,
            PopularMoviesContract.MovieReviewEntry.COLUMN_REVIEW_ID,
            PopularMoviesContract.MovieReviewEntry.COLUMN_AUTHOR,
            PopularMoviesContract.MovieReviewEntry.COLUMN_CONTENT,
            PopularMoviesContract.MovieReviewEntry.COLUMN_URL,
            PopularMoviesContract.MovieReviewEntry.TABLE_NAME + "." + PopularMoviesContract.MovieReviewEntry.COLUMN_DATE
    };

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
        if (mPrefs == null) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        }
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        if (getIntent().getExtras() != null) {
            Intent intent = getIntent();
            movieId = intent.getIntExtra("id", 0);
            setTitle(intent.getStringExtra("title"));
            Picasso.with(this).load("http://image.tmdb.org/t/p/w500" + intent.getStringExtra("backdrop")).into(backdrop);
            Picasso.with(this).load("http://image.tmdb.org/t/p/w185" + intent.getStringExtra("poster")).into(mMoviePoster);
            tvTitle.setText(intent.getStringExtra("title"));
            try {
                Date dateInput = input.parse(intent.getStringExtra("release"));
                tvMonth.setText("Release date: " + output.format(dateInput));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            tvRating.setText("Average rating: " + intent.getDoubleExtra("rating", 0));
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

            if (isMovieFavorite()) {
                img_favourite.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.favorite_selected));
            }

            img_favourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isAlreadyFavorite = isMovieFavorite();

                    int labelId = -1;
                    int backgroundColorId = -1;
                    int textColorId = -1;

                    Set<String> favoriteMovieIdsSet = null;

                    if (mPrefs.contains(FAVORITE_MOVIE_IDS_SET_KEY)) {
                        favoriteMovieIdsSet = mPrefs.getStringSet(FAVORITE_MOVIE_IDS_SET_KEY, null);
                    }

                    if (favoriteMovieIdsSet == null) {
                        favoriteMovieIdsSet = new LinkedHashSet<>();
                    }

                    if (isAlreadyFavorite) {
                        favoriteMovieIdsSet.remove(Integer.toString(movieId));
                        labelId = R.string.label_movie_marked_not_favorite;
                        backgroundColorId = R.color.favorite_not_selected;
                        PopularMoviesSyncAdapter.syncImmediately(getApplicationContext());
                    } else {
                        favoriteMovieIdsSet.add(Integer.toString(movieId));
                        final SharedPreferences.Editor prefsEdit = mPrefs.edit();
                        prefsEdit.putStringSet(FAVORITE_MOVIE_IDS_SET_KEY, favoriteMovieIdsSet);
                        prefsEdit.commit();

                        labelId = R.string.label_movie_marked_favorite;
                        backgroundColorId = R.color.favorite_selected;
                    }

                    if (mFavoriteToast != null) {
                        mFavoriteToast.cancel();
                    }

                    mFavoriteToast = Toast.makeText(DetailActivity.this, getString(labelId), Toast.LENGTH_SHORT);

                    mFavoriteToast.show();
                    img_favourite.setColorFilter(ContextCompat.getColor(getApplicationContext(),backgroundColorId));
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //mShareMenuItem = menu.findItem(R.id.action_share);
        //mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenuItem);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_details, menu);
        return true;
    }

    private boolean isMovieFavorite() {
        boolean result = false;
        Set<String> favoriteMovieIdsSet = null;

        if (mPrefs == null) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        }

        if (mPrefs.contains(FAVORITE_MOVIE_IDS_SET_KEY)) {
            favoriteMovieIdsSet = mPrefs.getStringSet(FAVORITE_MOVIE_IDS_SET_KEY, null);
        }

        if (favoriteMovieIdsSet != null) {
            Iterator<String> favIterator = favoriteMovieIdsSet.iterator();

            while (favIterator.hasNext()) {
                String favMovieId = favIterator.next();
                if (favMovieId.equals(Integer.toString(movieId))) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private String firstTrailerUrl = "";
    private void loadTrailer() {
        trailerList.clear();

        APIService service = APIService.retrofit.create(APIService.class);
        Call<TrailerModel> call = service.movieTrailer(movieId);
        call.enqueue(new Callback<TrailerModel>() {
            @Override
            public void onResponse(Call<TrailerModel> call, Response<TrailerModel> response) {
                if (response.isSuccessful()) {
                    TrailerModel movies = response.body();
                    if (movies.getResults().size() <= 0) {
                        rv_trailer.setVisibility(View.GONE);
                    } else {
                        for (int i = 0; i < movies.getResults().size(); i++) {
                            TrailerResult moviesData = movies.getResults().get(i);
                            trailerList.add(moviesData);
                            mTrailerAdapter.notifyDataSetChanged();
                            if(firstTrailerUrl.isEmpty()) firstTrailerUrl = "https://www.youtube.com/watch?v="+moviesData.getKey();
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
                    if (movies.getResults().size() <= 0) {
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
                return true;
            case R.id.action_share:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(firstTrailerUrl));
                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    Toast.makeText(this, "No apps support to open the url!", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_sort_order_key))) {
            mIsPreferenceChanged = true;
            if (mShareMenuItem != null) mShareMenuItem.setVisible(false);
        }
    }

    private Intent createShareTrailerUrlIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mFirstTrailerUrl);

        return shareIntent;
    }

    private void populateFirstTrailerUrl(Cursor cursor) {
        @MovieDataLoader.MovieStatus int status = Constant.getMovieStatus(getApplicationContext());
        switch (status) {
            case MovieDataLoader.MOVIE_STATUS_OK:
                if (cursor != null && cursor.moveToFirst()) {
                    mFirstTrailerUrl = "https://www.youtube.com/watch?v=" + cursor.getString(COL_KEY);
                    if (mShareMenuItem != null) {
                        mShareMenuItem.setVisible(true);
                    }

                    if (mShareActionProvider != null) {
                        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
                        mShareActionProvider.setShareIntent(createShareTrailerUrlIntent());
                    }

                }
                break;
            default:
                if (mShareMenuItem != null) {
                    mShareMenuItem.setVisible(false);
                }
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MOVIE_TRAILER_LOADER:
                Uri trackUri = PopularMoviesContract.MovieTrailerEntry.buildMovieTrailerUri(movieId);

                return new CursorLoader(this,
                        trackUri,
                        MOVIE_TRAILER_COLUMNS,
                        null,
                        null,
                        null);

            case MOVIE_REVIEW_LOADER:
                Uri reviewUri = PopularMoviesContract.MovieReviewEntry.buildMovieReviewsUri(movieId);

                return new CursorLoader(this,
                        reviewUri,
                        MOVIE_REVIEWS_COLUMNS,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case MOVIE_TRAILER_LOADER:
                populateFirstTrailerUrl(data);
                break;
            case MOVIE_REVIEW_LOADER:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case MOVIE_TRAILER_LOADER:
                break;
            case MOVIE_REVIEW_LOADER:
                break;
        }
    }
}
