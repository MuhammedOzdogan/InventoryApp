package com.example.muhammed.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Muhammed on 4/1/2018.
 */

public class ProductProvider extends ContentProvider {

    private ProductDbHelper mOpenHelper;

    //Use an int for each URI we will run, this represents the different queries
    private static final int PRODUCTS = 100;

    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher;

    static {
        String content  =ProductContract.CONTENT_AUTHORITY;

        //All paths to the UriMatcher have a corresponding code to return
        //when a match is found (the ints above).
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(content, ProductContract.PATH_PRODUCT, PRODUCTS);
        sUriMatcher.addURI(content, ProductContract.PATH_PRODUCT + "/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case PRODUCTS :
                retCursor = db.query(
                        ProductContract.ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case PRODUCT_ID :
                long _id = ContentUris.parseId(uri);
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(_id)};
                retCursor = db.query(
                        ProductContract.ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
                default: throw  new UnsupportedOperationException("Unknownn uri : " + uri);

        }

        //Set the notification URI for the cursor to the one passed into the function.
        //This causes the cursor to register a content observer to watch for changes that happen to
        //this URI and any of it's descendats. By descendats, we mean any URI that begins with this
        //path.
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    //Determine MIME type of the results.
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PRODUCTS :
                return ProductContract.ProductEntry.CONTENT_TYPE;

            case PRODUCT_ID :
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default : throw new UnsupportedOperationException("Unknown uri : " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long _id;
        Uri returnUri;

        switch(sUriMatcher.match(uri)) {
            case PRODUCTS :
                _id = db.insert(ProductContract.ProductEntry.TABLE_NAME, null,
                        contentValues);
                if(_id > 0) {
                    returnUri = ProductContract.ProductEntry.buildProductUri(_id);
                } else {
                    throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
                }
                break;
            case PRODUCT_ID :
                _id = db.insert(ProductContract.ProductEntry.TABLE_NAME, null,
                        contentValues);
                if(_id > 0) {
                    returnUri = ProductContract.ProductEntry.buildProductUri(_id);
                } else {
                    throw  new UnsupportedOperationException("Unable to insert rows into: " + uri);
                }
                break;

                default: throw new UnsupportedOperationException("Unknown uri : " + uri);
        }
        //Use this on the URI passed into the function to notify any observers that the uri has
        //changed.
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rows;//Number of rows effected

        switch (sUriMatcher.match(uri)) {
            case PRODUCTS :
                rows = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case PRODUCT_ID :
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri))
                };
                rows = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
                default: throw new UnsupportedOperationException("Unknown uri : " + uri);
        }
        //Because null could delete all rows
        if(selection == null || rows != 0) {
            getContext().getContentResolver().notifyChange(ProductContract.ProductEntry.CONTENT_URI, null);
        }
        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rows;

        switch (sUriMatcher.match(uri)) {
           case PRODUCTS : rows = db.update(ProductContract.ProductEntry.TABLE_NAME, contentValues, selection,
                    selectionArgs);
            case PRODUCT_ID :
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{
                      String.valueOf(ContentUris.parseId(uri))
                };
                rows = db.update(ProductContract.ProductEntry.TABLE_NAME, contentValues,
                        selection, selectionArgs);
                    break;
            default : throw new UnsupportedOperationException("Unknown uri : " + uri);
        }

        if(rows != 0) {
            getContext().getContentResolver().notifyChange(ProductContract.ProductEntry.CONTENT_URI, null);
        }

        return rows;
    }
}
