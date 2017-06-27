package id.odt.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {
    private Authenticator mAuth;

    @Override
    public void onCreate() {
        mAuth = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuth.getIBinder();
    }
}
