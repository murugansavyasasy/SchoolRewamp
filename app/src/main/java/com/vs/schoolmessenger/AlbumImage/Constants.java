package com.vs.schoolmessenger.AlbumImage;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class Constants {

    public static final int PERMISSION_REQUEST_CODE = 1000;
    public static final int PERMISSION_GRANTED = 1001;
    public static final int PERMISSION_DENIED = 1002;

    public static final int REQUEST_CODE = 2000;

    public static final int FETCH_STARTED = 2001;
    public static final int FETCH_COMPLETED = 2002;
    public static final int ERROR = 2005;
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 23;
    public static final String INTENT_EXTRA_ALBUM = "album";
    public static final String INTENT_EXTRA_IMAGES = "images";
    public static final String INTENT_EXTRA_LIMIT = "limit";
    public static final int DEFAULT_LIMIT = 1;
    public static int limit;

    public static String getCount(Context c, String album_name) {
        Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};


        Cursor cursorExternal = c.getContentResolver().query(uriExternal, projection, MediaStore.Images.Media.BUCKET_ID + "=?", new String[]{String.valueOf(album_name)}, null);
        Cursor cursorInternal = c.getContentResolver().query(uriInternal, projection, MediaStore.Images.Media.BUCKET_ID + "=?", new String[]{String.valueOf(album_name)}, null);
        Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});
        Log.d("albumname", album_name);
        return cursor.getCount() + " Photos";
    }
}