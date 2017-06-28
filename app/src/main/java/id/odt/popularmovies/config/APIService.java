package id.odt.popularmovies.config;

import id.odt.popularmovies.model.MoviesModel;
import id.odt.popularmovies.model.ReviewsModel;
import id.odt.popularmovies.model.TrailerModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by arrival on 6/22/17.
 */

public interface APIService {
    @GET("3/movie/{sort}?api_key="+Constant.API_KEY)
    Call<MoviesModel> loadMovie(@Path("sort") String sort);

    @GET("3/movie/{movie_id}/reviews?api_key="+Constant.API_KEY)
    Call<ReviewsModel> movieReviews(@Path("movie_id") Integer movie_id);

    @GET("3/movie/{movie_id}/videos?api_key="+Constant.API_KEY)
    Call<TrailerModel> movieTrailer(@Path("movie_id") Integer movie_id);

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constant.API_HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
