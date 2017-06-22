package id.odt.popularmovies;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import id.odt.popularmovies.adapter.MoviesAdapter;
import id.odt.popularmovies.config.APIService;
import id.odt.popularmovies.model.MoviesModel;
import id.odt.popularmovies.model.MoviesResult;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        TAG = getPackageName();
        moviesList = new ArrayList<MoviesResult>();

        mGridLayoutManager = new GridLayoutManager(this, 2);
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
    }

    private void loadMovies(int sort) {
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
                        moviesList.add(moviesData);
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
        }
        return super.onOptionsItemSelected(item);
    }
}