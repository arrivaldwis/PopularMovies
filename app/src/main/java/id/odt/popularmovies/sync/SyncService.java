package id.odt.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncService extends Service {
    private static final Object sSyncAdapter = new Object();
    private static SyncAdapter sPopularMoviesAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapter) {
            if (sPopularMoviesAdapter == null) {
                sPopularMoviesAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sPopularMoviesAdapter.getSyncAdapterBinder();
    }
}