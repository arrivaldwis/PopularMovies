package id.odt.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.os.PersistableBundle;
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
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.odt.popularmovies.adapter.MoviesAdapter;
import id.odt.popularmovies.config.APIService;
import id.odt.popularmovies.config.Constant;
import id.odt.popularmovies.data.DataContract;
import id.odt.popularmovies.model.MoviesModel;
import id.odt.popularmovies.model.MoviesResult;
import id.odt.popularmovies.sync.SyncAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private String TAG;

    @BindView(R.id.rv_movie)
    RecyclerView rv_movie;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    private ArrayList<MoviesResult> moviesList;
    private GridLayoutManager mGridLayoutManager;
    private MoviesAdapter mAdapter;
    private int sortState = 0;
    private String sortTitle = "";
    private Parcelable layoutManagerSavedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        TAG = getPackageName();
        sortTitle = getString(R.string.title_popular);
        moviesList = new ArrayList<MoviesResult>();

        int mNoOfColumns = Constant.calculateNoOfColumns(getApplicationContext());
        mGridLayoutManager = new GridLayoutManager(this, mNoOfColumns);
        rv_movie.setLayoutManager(mGridLayoutManager);
        mAdapter = new MoviesAdapter(moviesList, this);
        rv_movie.setAdapter(mAdapter);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMovies(sortState);
            }
        });
        SyncAdapter.initializeSyncAdapter(this);

        if (savedInstanceState != null) {
            String sortChange = savedInstanceState.getString("sortState");
            String sortTitle = savedInstanceState.getString("sortTitle");
            int positionIndex = Integer.parseInt(savedInstanceState.getString("sortState"));
            int topView = Integer.parseInt(savedInstanceState.getString("sortState"));

            sortState = Integer.parseInt(sortChange);
            setTitle(sortTitle);
            loadMovies(sortState);
        } else {
            loadMovies(sortState);
        }
    }

    private void loadMovies(final int sort) {
        moviesList.clear();
        swipeRefresh.setRefreshing(true);

        APIService service = APIService.retrofit.create(APIService.class);
        Call<MoviesModel> call = null;
        if (sort == 0) call = service.loadMovie("popular");
        else call = service.loadMovie("top_rated");
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
                        if(layoutManagerSavedState!=null) restoreLayoutManagerPosition();
                    }
                }
            }

            @Override
            public void onFailure(Call<MoviesModel> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });

        if(sort == 2) {
            call = service.loadMovie("popular");
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

        Uri URL = DataContract.MovieEntry.CONTENT_URI;

        Uri students = URL;
        Cursor c = getContentResolver().query(students, null, null, null, DataContract.MovieEntry.COLUMN_MOVIE_ID);

        if (c.moveToFirst()) {
            do {
                if (c.getString(c.getColumnIndex(DataContract.MovieEntry.COLUMN_MOVIE_ID)).equals(Integer.toString(movieId))) {
                    result = true;
                    break;
                }
            } while (c.moveToNext());
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
                sortTitle = getString(R.string.title_popular);
                loadMovies(sortState);
                setTitle(sortTitle);
                return true;
            case R.id.sortTopRated:
                sortState = 1;
                sortTitle = getString(R.string.title_top_rated);
                loadMovies(sortState);
                setTitle(sortTitle);
                return true;
            case R.id.sortFavourite:
                sortState = 2;
                sortTitle = getString(R.string.title_favourite);
                loadMovies(sortState);
                setTitle(sortTitle);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("myState", rv_movie.getLayoutManager().onSaveInstanceState());
        outState.putString("sortState", Integer.toString(sortState));
        outState.putString("sortTitle", sortTitle);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        layoutManagerSavedState = savedInstanceState.getParcelable("myState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void restoreLayoutManagerPosition() {
        if (layoutManagerSavedState != null) {
            rv_movie.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
        }
    }
}
