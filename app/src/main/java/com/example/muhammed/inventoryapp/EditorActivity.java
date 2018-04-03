package com.example.muhammed.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.muhammed.inventoryapp.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This activity for adding a new product or editing an existing product.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = EditorActivity.class.getSimpleName();

    private EditText mTitle;

    private EditText mPrice;

    private EditText mSupplier;

    private static int mQuantity;

    /**
     * A button for decrementing quantit of product.
     */
    private Button mDecrement;

    /**
     * TextSwitcher for representing quantity of product.
     * I used TextSwitcher in order to "fade in", "fade out" animation.
     */
    private TextSwitcher mSwitcher;

    /**
     * A button for decrementing quantit of product.
     */
    private Button mIncrement;

    /**
     * ImageView for representing picture of product.
     */
    private ImageView mImageView;

    /**
     * Bitmap object to store image of product.
     * We can resize and save this in to database.
     */
    private static Bitmap mBitmap;

    /**
     * A constatnt for take image intent.
     */
    private int REQUEST_TAKE_PHOTO = 1;

    /**
     * Take photo floating action button.
     */
    private FloatingActionButton mFab;

    /**
     * Location of fullsize picture of current product.
     */
    private String mCurrentPhotoPath;

    /**
     * This is about dialog box.
     */
    private boolean isTouched = false;

    /**
     * Uri of updating item.
     */
    private Uri mItemUri;

    /**
     * Constant for loader.
     */
    private int ITEM_LOADER = 1;

    /**
     * Order button that calls supplier.
     */
    private Button mOrderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        if (savedInstanceState != null) {//Retrive these on device rotation change.
            mQuantity = savedInstanceState.getInt("quantity");
            mBitmap = savedInstanceState.getParcelable("bitmap");
            mCurrentPhotoPath = savedInstanceState.getString("path");
        }

        //Setup views.
        mTitle = findViewById(R.id.title);
        mPrice = findViewById(R.id.price);
        mSupplier = findViewById(R.id.supplier);
        mIncrement = findViewById(R.id.increment_quantity);
        mImageView = findViewById(R.id.product_thumbnail);
        mOrderButton = findViewById(R.id.order_button);

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mSupplier.getText().toString()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        mDecrement = findViewById(R.id.decrement_quantity);
        mSwitcher = findViewById(R.id.quantity_switcher);
        setupTextSwitcher();
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        //If user taked a picture put it in to ImageView.
        if (mBitmap != null) {
            mImageView.setImageBitmap(mBitmap);
        }

        //Set touch listener on input views in order to represent alert dialog if user left activitiy
        //without saving changes.
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isTouched = true;
                return false;
            }
        };
        mTitle.setOnTouchListener(touchListener);
        mPrice.setOnTouchListener(touchListener);
        mIncrement.setOnTouchListener(touchListener);
        mDecrement.setOnTouchListener(touchListener);
        mSupplier.setOnTouchListener(touchListener);

        // Get the Intent that started this activity and extract the relevant item data.
        Intent intent = getIntent();
        mItemUri = intent.getData();
        if (mItemUri != null) {
            getSupportLoaderManager().initLoader(ITEM_LOADER, null, this);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //On configuration changes save important data
        outState.putInt("quantity", mQuantity);
        outState.putParcelable("bitmap", mBitmap);
        outState.putString("path", mCurrentPhotoPath);
        super.onSaveInstanceState(outState);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    //Setup textSwitcher to show some effect
    private final void setupTextSwitcher() {

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        mSwitcher.setInAnimation(in);
        mSwitcher.setOutAnimation(out);
        mIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuantity++;
                mSwitcher.setText(String.valueOf(mQuantity));
            }
        });
        mDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mQuantity > 0) {
                    mQuantity--;
                    mSwitcher.setText(String.valueOf(mQuantity));
                }
            }
        });

        ViewSwitcher.ViewFactory factory = new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(EditorActivity.this);
                t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                t.setTextAppearance(EditorActivity.this, android.R.style.TextAppearance_Large);
                return t;
            }
        };

        mSwitcher.setFactory(factory);
        mSwitcher.setCurrentText(String.valueOf(mQuantity));
    }

    //Inflate the menu and hide delete option in create mode.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.editor_activity_menu, menu);
        if (mItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_item);
            menuItem.setVisible(false);
        }
        return true;
    }

    //Handle menu input
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.editor_done:
                persistProduct();

                return true;

            case R.id.delete_item:
                deleteItem();
                return true;
            case android.R.id.home://If user want to back main activity alert user if there is
                //changes on views.
                if (isTouched) {
                    discardDialog();
                } else {
                    onBackPressed();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void persistProduct() {

        //Take data from inputs.
        String titleString = mTitle.getText().toString();
        String priceString = mPrice.getText().toString();
        String supplierString = mSupplier.getText().toString();

        //Sanity check.
        if (TextUtils.isEmpty(titleString)) {
            Toast.makeText(this, getString(R.string.title_sanity), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.price_sanity), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(supplierString)) {
            Toast.makeText(this, getString(R.string.supplier_sanity), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mBitmap == null) {
            Toast.makeText(this, getString(R.string.photo_sanity), Toast.LENGTH_SHORT).show();
            return;
        }

        //Handle photo to lesser sizes.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        //Reduce the image size
        int h = 96; // height in pixels
        int w = 96; // width in pixels
        Bitmap scaled = Bitmap.createScaledBitmap(mBitmap, h, w, true);

        scaled.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] image = bos.toByteArray();


        //Prepare data to insert database.
        ContentValues contentValues = new ContentValues();

        contentValues.put(ProductEntry.COLUMN_NAME_TITLE, titleString);
        contentValues.put(ProductEntry.COLUMN_PRICE, Integer.parseInt(priceString));
        contentValues.put(ProductEntry.COLUMN_QUANTITY, mQuantity);
        contentValues.put(ProductEntry.COLUMN_SUPPLIER, supplierString);
        contentValues.put(ProductEntry.COLUMN_PICTURE, image);
        Uri newUri = null;
        long rows = -1;
        if (mItemUri == null) { //Insert mode.
            newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);
            rows = ContentUris.parseId(newUri);
        } else {//Update mode.
            rows = getContentResolver().update(mItemUri, contentValues, null, null);
        }

        if (rows < 1) { //An error occured.
            Toast.makeText(this, getString(R.string.save_failed),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.save_successful),
                    Toast.LENGTH_SHORT).show();

        }
        //Finis this activity and turn back to main activity.
        this.finish();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",    /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        mBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(mBitmap);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mCurrentPhotoPath != null) {//If main thread drawed up the ImageView and
                                                    //Photo taken insert it in to ImageView.
            setPic();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //This calls when photo taken. But here is not aporive location to insert image
            //in to ImageView.
        }
    }

    @Override
    public void onBackPressed() {
        //Alert user before return main activity if there is any change on inputs.
        if (isTouched) {
            discardDialog();
            return;
        }
        super.onBackPressed();
    }

    //Draw alert dialog if user want to go back main activity without save changes.
    public void discardDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
        alertDialog.setTitle(getString(R.string.leave_editor));
        alertDialog.setMessage(getString(R.string.changes_not_saved));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.discard),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditorActivity.this.finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    //Heavy work is to query database about current item. Because of that we use loader.
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(EditorActivity.this,
                mItemUri,
                MainActivity.mProjection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Data is ready let's put it in to UI.
        if (data.moveToFirst()) {
            mTitle.setText(data.getString(
                    data.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_TITLE)));
            mPrice.setText(data.getString(
                    data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE)));
            mQuantity = (data.getInt(
                    data.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY)));
            mSwitcher.setText(String.valueOf(mQuantity));
            mSupplier.setText(data.getString(
                    data.getColumnIndexOrThrow(ProductEntry.COLUMN_SUPPLIER)));
            byte[] image = data
                    .getBlob(data.getColumnIndexOrThrow(ProductEntry.COLUMN_PICTURE));

            //Convert byte array to bitmap
            mBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

            mImageView.setImageBitmap(mBitmap);
//            mCurrentPhotoPath = null;
        }
    }

    //I think it is very clear this function deletes current item from database.
    private void deleteItem() {
        int rows = getContentResolver().delete(mItemUri, null, null);

        if (rows < 1) {
            Toast.makeText(this, getString(R.string.deletion_failed),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.deletion_successful),
                    Toast.LENGTH_SHORT).show();

        }
        this.finish();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {//When we gone some where else we shold clear
                                                      //data.
        mTitle.setText("");
        mPrice.setText(String.valueOf(0));
        mQuantity = 0;
        mSwitcher.setText(String.valueOf(mQuantity));
        mSupplier.setText("");
        mImageView.setImageResource(android.R.color.transparent);
        mBitmap = null;
        mCurrentPhotoPath = "";
    }

}
