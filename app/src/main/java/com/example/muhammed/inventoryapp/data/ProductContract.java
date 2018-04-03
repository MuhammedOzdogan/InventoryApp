package com.example.muhammed.inventoryapp.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Muhammed on 3/31/2018.
 */

//This Contract class is represent a schema in database.
public final class ProductContract {

    /**
     * The Content Authority is a name for the entire content provider, similar to the relationship
     * between a domain name and its website. A convenient string to use for content authority is
     * the package name for the app, since it is guaranteed to be unique on the device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.muhammed.inventoryapp";

    /**
     * The Content Authority is used to create the base of all URIs which apps will use to
     * contact this content provider.
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * A list of posibble paths that will be appended to the base URI for each of different
     * tables.
     */
    public static final String PATH_PRODUCT = "product";

    //To prevent somone from accidentally instantiating the contract class,
    //make the constructor private.
    private ProductContract(){}


    /**
     * Inner class that defines the table contents
     */
    public static class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCT).build();

        //These are special type prefixes that specify if a URI returns a list or a specific item
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_URI + "/" + PATH_PRODUCT;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_URI + "/" + PATH_PRODUCT;

        public static final String TABLE_NAME = "products";

        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_PICTURE = "picture";
        public static final String COLUMN_SUPPLIER = "supplier";

        //Define a function to build a URI to find a specific movie by it's identifier
        public static Uri buildProductUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


    }
}
