package com.example.muhammed.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.muhammed.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Muhammed on 4/1/2018.
 */

public class ProductCursorAdapter extends CursorAdapter {

    ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }


    //The newView method is used to inflate a new view and return it,
    //you don't bid any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    //The bindView method is used to bind all data to a given view
    //such as setting the text on a TextView
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        //Find fields to populate in inflated template
        TextView titleTextView = view.findViewById(R.id.title_text_view);
        TextView priceTextView = view.findViewById(R.id.price_text_view);
        TextView quantityTextView = view.findViewById(R.id.quantity_text_view);
        ImageView productImageView = view.findViewById(R.id.product_image_view);
        final Button sellButton = view.findViewById(R.id.sell_button);


        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(ProductEntry._ID));
        final String title = cursor
                .getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_TITLE));
        final int price = cursor
                .getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE));
        final int quantity = cursor
                .getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY));
        final int updatedQuantity = quantity > 0 ? quantity - 1 : 0;
        final String supplier = cursor
                .getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_SUPPLIER));
        final byte[] image = cursor
                .getBlob(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PICTURE));

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rows = -1;
                if(quantity > 0) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ProductEntry.COLUMN_NAME_TITLE, title);
                    contentValues.put(ProductEntry.COLUMN_PICTURE, price);
                    contentValues.put(ProductEntry.COLUMN_QUANTITY, updatedQuantity);
                    contentValues.put(ProductEntry.COLUMN_SUPPLIER, supplier);
                    contentValues.put(ProductEntry.COLUMN_PICTURE, image);
                    rows = context.getContentResolver().update(ProductEntry.buildProductUri(id), contentValues,
                            null, null);
                }

                if(rows > 0) {
                    Toast.makeText(context, context.getString(R.string.have_a_nice_day),Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, context.getString(R.string.selling_failed),Toast.LENGTH_SHORT).show();
                }
            }
        });

        titleTextView.setText(title);
        priceTextView.setText(context.getString(R.string.price, price));
        quantityTextView.setText(context.getString(R.string.quantity, quantity));

        //Convert byte array to bitmap
        Bitmap thumbnail = BitmapFactory.decodeByteArray(image, 0, image.length);

        productImageView.setImageBitmap(thumbnail);
    }
}
