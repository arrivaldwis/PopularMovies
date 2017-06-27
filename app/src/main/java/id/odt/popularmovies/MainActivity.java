package id.odt.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
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
import id.odt.popularmovies.model.MoviesModel;
import id.odt.popularmovies.model.MoviesResult;
import id.odt.popularmovies.sync.SyncAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private SharedPreferences mPref = null;

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
        SyncAdapter.initializeSyncAdapter(this);
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
        Set<String> movieIdsFav = null;

        if (mPref == null) {
            mPref = PreferenceManager.getDefaultSharedPreferences(this);
        }

        if (mPref.contains(Constant.MOVIE_IDS_FAVOURITE)) {
            movieIdsFav = mPref.getStringSet(Constant.MOVIE_IDS_FAVOURITE, null);
        }

        if (movieIdsFav != null) {
            Iterator<String> favIterate = movieIdsFav.iterator();

            while (favIterate.hasNext()) {
                String getMovieId = favIterate.next();
                if (getMovieId.equals(Integer.toString(movieId))) {
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
                setTitle(getString(R.string.title_popular));
                return true;
            case R.id.sortTopRated:
                sortState = 1;
                loadMovies(sortState);
                setTitle(getString(R.string.title_top_rated));
                return true;
            case R.id.sortFavourite:
                sortState = 2;
                loadMovies(sortState);
                setTitle(getString(R.string.title_favourite));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
