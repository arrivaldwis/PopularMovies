package id.odt.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import id.odt.popularmovies.data.DataContract.MovieEntry;

public class DataProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private DataDbHelper dbHelper;

    static final int MOVIE = 100;

    static UriMatcher buildUriMatcher() {
        final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;
        sURIMatcher.addURI(authority, DataContract.PATH_MOVIE, MOVIE);
        return sURIMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DataDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                return MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (uriMatcher.match(uri)) {
            case MOVIE: {
                retCursor = dbHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                normalizeDate(values);
                long id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if ( id > 0 )
                    returnUri = MovieEntry.buildMovieUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                normalizeDate(values);
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long id = db.insert(MovieEntry.TABLE_NAME, null, value);
                        if (id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);

        int rowsDeleted;
        if ( null == selection ) selection = "1";

        switch (match) {
            case MOVIE: {
                rowsDeleted = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        if (values.containsKey(MovieEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(MovieEntry.COLUMN_DATE);
            values.put(MovieEntry.COLUMN_DATE, DataContract.normalizeDate(dateValue));
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        dbHelper.close();
        super.shutdown();
    }

}
