package id.odt.popularmovies.config;

import android.content.Context;
import android.util.DisplayMetrics;

import id.odt.popularmovies.BuildConfig;

/**
 * Created by arrival on 6/22/17.
 */

public class Constant {
    public final static String API_HOST = "https://api.themoviedb.org/";
    public final static String API_KEY = BuildConfig.API_KEY;
    public static final String MOVIE_IDS_FAVOURITE = "movie_id_set_key";

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);
        return noOfColumns;
    }
}
