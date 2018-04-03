package com.example.muhammed.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.muhammed.inventoryapp.data.ProductContract.ProductEntry;
/**
 * Created by Muhammed on 3/31/2018.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "Products.db";




   public ProductDbHelper(Context context) {
       //We can pass null to factory which is default value.
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    //SQLite statements which creating and deleting this table.

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
                    ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ProductEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL, " +
                    ProductEntry.COLUMN_PRICE + " INTEGER NOT NULL, " +
                    ProductEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                    ProductEntry.COLUMN_SUPPLIER + " TEXT NOT NULL, " +
                    ProductEntry.COLUMN_PICTURE + " BLOB)";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS" + ProductEntry.TABLE_NAME;
}
