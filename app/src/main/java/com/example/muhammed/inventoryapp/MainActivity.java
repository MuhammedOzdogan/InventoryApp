package com.example.muhammed.inventoryapp;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.muhammed.inventoryapp.data.ProductContract.ProductEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Colums which we need to display to user.
    public static String[] mProjection = new String[]{
            ProductEntry._ID,
            ProductEntry.COLUMN_NAME_TITLE,
            ProductEntry.COLUMN_PRICE,
            ProductEntry.COLUMN_QUANTITY,
            ProductEntry.COLUMN_SUPPLIER,
            ProductEntry.COLUMN_PICTURE
    };

    View mEmptyView;

    ListView mListView;

    CursorAdapter mAdapter = new ProductCursorAdapter(this, null);


    private static final int DATA_BASE_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Find views
        mListView = findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);
        mEmptyView = findViewById(R.id.empty_layout);
        mListView.setEmptyView(mEmptyView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent editItem = new Intent(MainActivity.this, EditorActivity.class);
                editItem.setData(ProductEntry.buildProductUri(id));
                startActivity(editItem);
            }
        });

        //Setup floating action button.
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editorIntent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(editorIntent);
            }
        });
        //Start query on loader
        getSupportLoaderManager().initLoader(DATA_BASE_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//Inflate menu
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {//Handle menu click.
        switch (item.getItemId()) {
            case R.id.delete_all :
                    deleteAll();
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean deleteAll() {//Clear the "products" table in database.
         int rows = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);

         if(rows > 0) {
             Toast.makeText(this, getString(R.string.all_deleted), Toast.LENGTH_SHORT).show();
             return true;
         } else {   //If all products already deleted and user want to delete more this toast will shown.
             Toast.makeText(this, getString(R.string.deletion_failed), Toast.LENGTH_SHORT).show();
             return false;
         }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Start query on another thread.
        return new CursorLoader(MainActivity.this,
                ProductEntry.CONTENT_URI,
                mProjection,
                null,
                null,
                null );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Out data is ready to use.
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Time to clear up data.
        mAdapter.swapCursor(null);
    }
}
