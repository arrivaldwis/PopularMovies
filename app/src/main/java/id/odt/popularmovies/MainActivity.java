package id.odt.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.odt.popularmovies.adapter.MoviesAdapter;
import id.odt.popularmovies.config.APIService;
import id.odt.popularmovies.config.Constant;
import id.odt.popularmovies.data.PopularMoviesContract;
import id.odt.popularmovies.model.MoviesModel;
import id.odt.popularmovies.model.MoviesResult;
import id.odt.popularmovies.sync.PopularMoviesSyncAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import id.odt.popularmovies.data.PopularMoviesContract.MovieEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String TAG;

    @BindView(R.id.rv_movie)
    RecyclerView rv_movie;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    private ArrayList<MoviesResult> moviesList;
    private GridLayoutManager mGridLayoutManager;
    private MoviesAdapter mAdapter;
    private int sortState = 0;
    private SharedPreferences mPrefs = null;
    public static final String FAVORITE_MOVIE_IDS_SET_KEY = "movie_id_set_key";

    public static final String[] MOVIE_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_IS_ADULT,
            MovieEntry.COLUMN_BACK_DROP_PATH,
            MovieEntry.COLUMN_ORIGINAL_LANGUAGE,
            MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_POPULARITY,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_IS_VIDEO,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_VOTE_COUNT,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_STATUS,
            MovieEntry.COLUMN_DATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        TAG = getPackageName();
        moviesList = new ArrayList<MoviesResult>();

        int mNoOfColumns = Constant.calculateNoOfColumns(getApplicationContext());
        mGridLayoutManager = new GridLayoutManager(this, mNoOfColumns);
        rv_movie.setLayoutManager(mGridLayoutManager);
        mAdapter = new MoviesAdapter(moviesList, this);
        rv_movie.setAdapter(mAdapter);

        loadMovies(sortState);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMovies(sortState);
            }
        });
        PopularMoviesSyncAdapter.initializeSyncAdapter(this);
    }

    private void loadMovies(final int sort) {
        moviesList.clear();
        swipeRefresh.setRefreshing(true);

        APIService service = APIService.retrofit.create(APIService.class);
        Call<MoviesModel> call = null;
        if (sort == 0) call = service.popularMovie();
        else call = service.topRatedMovie();
        call.enqueue(new Callback<MoviesModel>() {
            @Override
            public void onResponse(Call<MoviesModel> call, Response<MoviesModel> response) {
                if (response.isSuccessful()) {
                    MoviesModel movies = response.body();
                    for (int i = 0; i < movies.getMoviesResults().size(); i++) {
                        MoviesResult moviesData = movies.getMoviesResults().get(i);
                        if(sort != 2) moviesList.add(moviesData);
                        else {
                            if(isMovieFavorite(moviesData.getId())) {
                                moviesList.add(moviesData);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<MoviesModel> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });

        if(sort == 2) {
            call = service.popularMovie();
            call.enqueue(new Callback<MoviesModel>() {
                @Override
                public void onResponse(Call<MoviesModel> call, Response<MoviesModel> response) {
                    if (response.isSuccessful()) {
                        MoviesModel movies = response.body();
                        for (int i = 0; i < movies.getMoviesResults().size(); i++) {
                            MoviesResult moviesData = movies.getMoviesResults().get(i);
                            if(isMovieFavorite(moviesData.getId())) {
                                moviesList.add(moviesData);
                            }
                            mAdapter.notifyDataSetChanged();
                            swipeRefresh.setRefreshing(false);
                        }
                    }
                }

                @Override
                public void onFailure(Call<MoviesModel> call, Throwable t) {
                    Log.d(TAG, t.getMessage());
                }
            });
        }
    }

    private boolean isMovieFavorite(final int movieId) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sortPopular:
                sortState = 0;
                loadMovies(sortState);
                setTitle("Popular Movies");
                return true;
            case R.id.sortTopRated:
                sortState = 1;
                loadMovies(sortState);
                setTitle("Top Rated Movies");
                return true;
            case R.id.sortFavourite:
                sortState = 2;
                loadMovies(sortState);
                setTitle("Favourite Movies");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sortOrderSelected = prefs.getString(this.getString(R.string.pref_sort_order_key), null);

        String sortOrder = PopularMoviesContract.MovieEntry.COLUMN_POPULARITY + " DESC";

        if(sortOrderSelected != null && sortOrderSelected.equals(this.getString(R.string.pref_sort_order_vote_average))) {
            sortOrder = MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        }

        Uri weatherForLocationUri = PopularMoviesContract.MovieEntry.buildMovieUri();

        return new CursorLoader(this,
                weatherForLocationUri,
                MOVIE_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
