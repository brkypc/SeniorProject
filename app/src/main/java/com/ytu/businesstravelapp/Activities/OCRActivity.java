package com.ytu.businesstravelapp.Activities;

import static java.lang.Math.max;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.ytu.businesstravelapp.OCR.CameraSettings.BitmapUtils;
import com.ytu.businesstravelapp.OCR.CameraSettings.GraphicOverlay;
import com.ytu.businesstravelapp.OCR.CameraSettings.VisionImageProcessor;
import com.ytu.businesstravelapp.OCR.Preference.SettingsActivity;
import com.ytu.businesstravelapp.OCR.TextDetector.TextRecognitionProcessor;
import com.ytu.businesstravelapp.R;

import java.io.IOException;
import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;

@KeepName
public final class OCRActivity extends AppCompatActivity {
    private static final String TAG = "ytu";

    private static final String SIZE_SCREEN = "w:screen"; // Match screen width
    private static final String SIZE_ORIGINAL = "w:original"; // Original image size

    private static final String KEY_IMAGE_URI = "com.google.mlkit.vision.demo.KEY_IMAGE_URI";
    private static final String KEY_SELECTED_SIZE = "com.google.mlkit.vision.demo.KEY_SELECTED_SIZE";

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_CHOOSE_IMAGE = 1002;

    private ImageView preview;
    private GifImageView gifImageView;
    private GraphicOverlay graphicOverlay;
    private String selectedSize = SIZE_ORIGINAL;

    private Uri imageUri;
    private int imageMaxWidth;
    private int imageMaxHeight;
    private VisionImageProcessor imageProcessor;

    private String distance, date, tripTime, taxiType, calculatedPrice, amount, oLat, oLong, dLat, dLong;
    private AppCompatButton confirmAmount, selectImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ocr);

        if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }

        Intent intent = getIntent();
        distance = intent.getStringExtra("distance");
        date = intent.getStringExtra("date");
        tripTime = intent.getStringExtra("tripTime");
        taxiType = intent.getStringExtra("taxiType");
        calculatedPrice = intent.getStringExtra("amount");
        oLat = intent.getStringExtra("oLat");
        oLong = intent.getStringExtra("oLong");
        dLat = intent.getStringExtra("dLat");
        dLong = intent.getStringExtra("dLong");

        Log.d(TAG, oLat);
        Log.d(TAG, oLong);
        Log.d(TAG, dLat);
        Log.d(TAG, dLong);

        confirmAmount = findViewById(R.id.confirmAmount);
        selectImage = findViewById(R.id.select_image_button);
        gifImageView = findViewById(R.id.gif);

        confirmAmount.setOnClickListener(view -> confirmDialog());

        selectImage.setOnClickListener(this::chooseImage);

        preview = findViewById(R.id.preview);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI);
            selectedSize = savedInstanceState.getString(KEY_SELECTED_SIZE);
        }

        View rootView = findViewById(R.id.root);
        rootView
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        new OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                imageMaxWidth = rootView.getWidth();
                                imageMaxHeight = rootView.getHeight() - findViewById(R.id.constraintLayout).getHeight();
                                if (SIZE_SCREEN.equals(selectedSize)) {
                                    tryReloadAndDetectInImage();
                                }
                            }
                        });

        ImageView settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(
                v -> {
                    Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                    settings.putExtra(
                            SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.STILL_IMAGE);
                    startActivity(settings);
                });
    }

    private void chooseImage(View view) {
        PopupMenu popup = new PopupMenu(OCRActivity.this, view);
        popup.setOnMenuItemClickListener(
                menuItem -> {
                    int itemId = menuItem.getItemId();
                    if (itemId == R.id.select_images_from_local) {
                        startChooseImageIntentForResult();
                        return true;
                    } else if (itemId == R.id.take_photo_using_camera) {
                        startCameraIntentForResult();
                        return true;
                    }
                    return false;
                });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.camera_button_menu, popup.getMenu());
        popup.show();
    }

    private void confirmDialog() {
        if (amount.equalsIgnoreCase("0.0"))
            Toast.makeText(OCRActivity.this, R.string.noAmountFound, Toast.LENGTH_SHORT).show();

        EditText edittext = new EditText(OCRActivity.this);
        edittext.setText(amount);
        edittext.setGravity(Gravity.CENTER);
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder alert = new AlertDialog.Builder(OCRActivity.this);
        alert.setMessage("Faturadan okunan tutarı onaylayınız");
        alert.setView(edittext);
        alert.setPositiveButton("Onayla", (dialog, whichButton) -> {
            String userInput = edittext.getText().toString();
            Intent intent = new Intent(OCRActivity.this, ResultActivity.class);
            intent.putExtra("date", date);
            intent.putExtra("tripTime", tripTime);
            intent.putExtra("taxiType", taxiType);
            intent.putExtra("amount", calculatedPrice);
            intent.putExtra("distance", distance);
            intent.putExtra("ocr", userInput);
            intent.putExtra("oLat", oLat);
            intent.putExtra("oLong", oLong);
            intent.putExtra("dLat", dLat);
            intent.putExtra("dLong", dLong);
            Log.e("ytuLog", "hata");
            startActivity(intent);
            finish();
        });
        alert.setNegativeButton("İptal", null);

        alert.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        createImageProcessor();
        tryReloadAndDetectInImage();
        registerReceiver(mReceiver, new IntentFilter(TextRecognitionProcessor.BROADCAST_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_IMAGE_URI, imageUri);
        outState.putString(KEY_SELECTED_SIZE, selectedSize);
    }

    private void startCameraIntentForResult() {
        imageUri = null;
        preview.setImageBitmap(null);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void startChooseImageIntentForResult() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            tryReloadAndDetectInImage();
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            tryReloadAndDetectInImage();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void tryReloadAndDetectInImage() {
        Log.d(TAG, "Try reload and detect image");
        try {
            if (imageUri == null) {
                return;
            }

            if (SIZE_SCREEN.equals(selectedSize) && imageMaxWidth == 0) {
                // UI layout has not finished yet, will reload once it's ready.
                return;
            }

            Bitmap imageBitmap = BitmapUtils.getBitmapFromContentUri(getContentResolver(), imageUri);
            if (imageBitmap == null) {
                return;
            }

            graphicOverlay.clear();

            Bitmap resizedBitmap;
            if (selectedSize.equals(SIZE_ORIGINAL)) {
                resizedBitmap = imageBitmap;
            } else {
                Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

                float scaleFactor =
                        max(
                                (float) imageBitmap.getWidth() / (float) targetedSize.first,
                                (float) imageBitmap.getHeight() / (float) targetedSize.second);

                resizedBitmap =
                        Bitmap.createScaledBitmap(
                                imageBitmap,
                                (int) (imageBitmap.getWidth() / scaleFactor),
                                (int) (imageBitmap.getHeight() / scaleFactor),
                                true);
            }

            preview.setImageBitmap(resizedBitmap);
            gifImageView.setVisibility(View.GONE);
            preview.setVisibility(View.VISIBLE);

            if (imageProcessor != null) {
                graphicOverlay.setImageSourceInfo(
                        resizedBitmap.getWidth(), resizedBitmap.getHeight(), false);
                imageProcessor.processBitmap(resizedBitmap, graphicOverlay);
            } else {
                Log.e(TAG, "Null imageProcessor, please check adb logs for imageProcessor creation error");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error retrieving saved image");
            imageUri = null;
        }
    }

    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth = imageMaxWidth;
        int targetHeight = imageMaxHeight;

        return new Pair<>(targetWidth, targetHeight);
    }

    private void createImageProcessor() {
        if (imageProcessor != null) {
            imageProcessor.stop();
        }

        imageProcessor = new TextRecognitionProcessor(this, new TextRecognizerOptions.Builder().build());
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, intent.getStringExtra("connection_established"));
            Log.d(TAG, intent.getStringArrayListExtra("text").toString());
            ArrayList<String> text = intent.getStringArrayListExtra("text");

            amount = "0.0";
            for (String s : text) {
                if (s.toLowerCase().contains("top") || s.toLowerCase().contains("tup") || s.toLowerCase().contains("tdp")) {
                    Log.d(TAG, "toplam stringi: " + s);
                    int index = text.indexOf(s);
                    for (int i = 1; i <= 5; i++) {
                        if (index + i < text.size()) {
                            Log.d(TAG, "tutar: " + text.get(index + i));
                            String read = text.get(index + i);
                            read = read.replace(",", ".");
                            if (read.matches("\\d+(?:\\.\\d+)?")) {
                                amount = read;
                                break;
                            }
                        }
                    }
                    Log.d(TAG, "Toplam Tutar: " + amount + " Lira");
                }
            }

            confirmAmount.setVisibility(View.VISIBLE);
            selectImage.setText(R.string.take_again);
        }
    };
}