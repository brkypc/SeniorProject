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
import android.content.res.Configuration;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.ytu.businesstravelapp.OCR.CameraSettings.BitmapUtils;
import com.ytu.businesstravelapp.OCR.CameraSettings.GraphicOverlay;
import com.ytu.businesstravelapp.OCR.CameraSettings.VisionImageProcessor;
import com.ytu.businesstravelapp.OCR.Preference.PreferenceUtils;
import com.ytu.businesstravelapp.OCR.Preference.SettingsActivity;
import com.ytu.businesstravelapp.OCR.TextDetector.TextRecognitionProcessor;
import com.ytu.businesstravelapp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * Activity demonstrating different image detector features with a still image from camera.
 */
@KeepName
public final class OCRActivity extends AppCompatActivity {

    private static final String TAG = "StillImageActivity";

    private static final String OBJECT_DETECTION = "Object Detection";
    private static final String OBJECT_DETECTION_CUSTOM = "Custom Object Detection";
    private static final String CUSTOM_AUTOML_OBJECT_DETECTION =
            "Custom AutoML Object Detection (Flower)";
    private static final String FACE_DETECTION = "Face Detection";
    private static final String BARCODE_SCANNING = "Barcode Scanning";
    private static final String IMAGE_LABELING = "Image Labeling";
    private static final String IMAGE_LABELING_CUSTOM = "Custom Image Labeling (Birds)";
    private static final String CUSTOM_AUTOML_LABELING = "Custom AutoML Image Labeling (Flower)";
    private static final String POSE_DETECTION = "Pose Detection";
    private static final String SELFIE_SEGMENTATION = "Selfie Segmentation";
    private static final String TEXT_RECOGNITION_LATIN = "Text Recognition Latin";
    private static final String TEXT_RECOGNITION_CHINESE = "Text Recognition Chinese (Beta)";
    private static final String TEXT_RECOGNITION_DEVANAGARI = "Text Recognition Devanagari (Beta)";
    private static final String TEXT_RECOGNITION_JAPANESE = "Text Recognition Japanese (Beta)";
    private static final String TEXT_RECOGNITION_KOREAN = "Text Recognition Korean (Beta)";

    private static final String SIZE_SCREEN = "w:screen"; // Match screen width
    private static final String SIZE_1024_768 = "w:1024"; // ~1024*768 in a normal ratio
    private static final String SIZE_640_480 = "w:640"; // ~640*480 in a normal ratio
    private static final String SIZE_ORIGINAL = "w:original"; // Original image size

    private static final String KEY_IMAGE_URI = "com.google.mlkit.vision.demo.KEY_IMAGE_URI";
    private static final String KEY_SELECTED_SIZE = "com.google.mlkit.vision.demo.KEY_SELECTED_SIZE";

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_CHOOSE_IMAGE = 1002;

    private ImageView preview;
    private GifImageView gifImageView;
    private GraphicOverlay graphicOverlay;
    private String selectedMode = TEXT_RECOGNITION_LATIN;
    private String selectedSize = SIZE_SCREEN;

    boolean isLandScape;

    private Uri imageUri;
    private int imageMaxWidth;
    private int imageMaxHeight;
    private VisionImageProcessor imageProcessor;

    private String distance, date, tripTime, taxiType, calculatedPrice, amount;
    private AppCompatButton confirmAmount, selectImage;
    private ArrayList<String> text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ocr);

        confirmAmount = findViewById(R.id.confirmAmount);
        selectImage = findViewById(R.id.select_image_button);

        confirmAmount.setOnClickListener(view -> {
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
                    startActivity(intent);
                    finish();
                    /*if(userInput.matches("\\d+(?:\\.\\d+)?")) {
                        Toast.makeText(PhotoActivity.this, userInput, Toast.LENGTH_SHORT).show();
                        saveToFirebase(userInput);
                    }else {
                        Toast.makeText(PhotoActivity.this,"Lütfen sadece sayı giriniz", Toast.LENGTH_SHORT).show();
                    }*/
                });
                alert.setNegativeButton("İptal", null);

                alert.show();
        });

        if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {

        }

        Intent intent = getIntent();
        distance = intent.getStringExtra("distance");
        date = intent.getStringExtra("date");
        tripTime = intent.getStringExtra("tripTime");
        taxiType = intent.getStringExtra("taxiType");
        calculatedPrice = intent.getStringExtra("amount");

        gifImageView = findViewById(R.id.gif);

        findViewById(R.id.select_image_button)
                .setOnClickListener(
                        view -> {
                            // Menu for selecting either: a) take new photo b) select from existing
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
                        });
        preview = findViewById(R.id.preview);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        populateFeatureSelector();
        populateSizeSelector();

        isLandScape =
                (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

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
                                imageMaxHeight = rootView.getHeight() - findViewById(R.id.control).getHeight();
                                if (SIZE_SCREEN.equals(selectedSize)) {
                                    tryReloadAndDetectInImage();
                                }
                            }
                        });

        ImageView settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(
                v -> {
                    Intent intent2 = new Intent(getApplicationContext(), SettingsActivity.class);
                    intent2.putExtra(
                            SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.STILL_IMAGE);
                    startActivity(intent2);
                });
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

    private void populateFeatureSelector() {
        Spinner featureSpinner = findViewById(R.id.feature_selector);
        List<String> options = new ArrayList<>();
        options.add(OBJECT_DETECTION);
        options.add(OBJECT_DETECTION_CUSTOM);
        options.add(CUSTOM_AUTOML_OBJECT_DETECTION);
        options.add(FACE_DETECTION);
        options.add(BARCODE_SCANNING);
        options.add(IMAGE_LABELING);
        options.add(IMAGE_LABELING_CUSTOM);
        options.add(CUSTOM_AUTOML_LABELING);
        options.add(POSE_DETECTION);
        options.add(SELFIE_SEGMENTATION);
        options.add(TEXT_RECOGNITION_LATIN);
        options.add(TEXT_RECOGNITION_CHINESE);
        options.add(TEXT_RECOGNITION_DEVANAGARI);
        options.add(TEXT_RECOGNITION_JAPANESE);
        options.add(TEXT_RECOGNITION_KOREAN);

        // Creating adapter for featureSpinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        featureSpinner.setAdapter(dataAdapter);
        featureSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                        selectedMode = parentView.getItemAtPosition(pos).toString();
                        createImageProcessor();
                        tryReloadAndDetectInImage();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
    }

    private void populateSizeSelector() {
        Spinner sizeSpinner = findViewById(R.id.size_selector);
        List<String> options = new ArrayList<>();
        options.add(SIZE_SCREEN);
        options.add(SIZE_1024_768);
        options.add(SIZE_640_480);
        options.add(SIZE_ORIGINAL);

        // Creating adapter for featureSpinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        sizeSpinner.setAdapter(dataAdapter);
        sizeSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                        selectedSize = parentView.getItemAtPosition(pos).toString();
                        tryReloadAndDetectInImage();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_IMAGE_URI, imageUri);
        outState.putString(KEY_SELECTED_SIZE, selectedSize);
    }

    private void startCameraIntentForResult() {
        // Clean up last time's image
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
            // In this case, imageUri is returned by the chooser, save it.
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

            // Clear the overlay first
            graphicOverlay.clear();

            Bitmap resizedBitmap;
            if (selectedSize.equals(SIZE_ORIGINAL)) {
                resizedBitmap = imageBitmap;
            } else {
                // Get the dimensions of the image view
                Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

                // Determine how much to scale down the image
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
                        resizedBitmap.getWidth(), resizedBitmap.getHeight(), /* isFlipped= */ false);
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
        int targetWidth;
        int targetHeight;

        switch (selectedSize) {
            case SIZE_SCREEN:
                targetWidth = imageMaxWidth;
                targetHeight = imageMaxHeight;
                break;
            case SIZE_640_480:
                targetWidth = isLandScape ? 640 : 480;
                targetHeight = isLandScape ? 480 : 640;
                break;
            case SIZE_1024_768:
                targetWidth = isLandScape ? 1024 : 768;
                targetHeight = isLandScape ? 768 : 1024;
                break;
            default:
                throw new IllegalStateException("Unknown size");
        }

        return new Pair<>(targetWidth, targetHeight);
    }

    private void createImageProcessor() {
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
        try {
            switch (selectedMode) {
                case OBJECT_DETECTION:
                    Log.i(TAG, "Using Object Detector Processor");
                    ObjectDetectorOptions objectDetectorOptions =
                            PreferenceUtils.getObjectDetectorOptionsForStillImage(this);
                    //imageProcessor = new ObjectDetectorProcessor(this, objectDetectorOptions);
                    break;
                case OBJECT_DETECTION_CUSTOM:
                    Log.i(TAG, "Using Custom Object Detector Processor");
                    LocalModel localModel =
                            new LocalModel.Builder()
                                    .setAssetFilePath("custom_models/object_labeler.tflite")
                                    .build();
                    CustomObjectDetectorOptions customObjectDetectorOptions =
                            PreferenceUtils.getCustomObjectDetectorOptionsForStillImage(this, localModel);
                    //imageProcessor = new ObjectDetectorProcessor(this, customObjectDetectorOptions);
                    break;
                case CUSTOM_AUTOML_OBJECT_DETECTION:
                    Log.i(TAG, "Using Custom AutoML Object Detector Processor");
                    LocalModel customAutoMLODTLocalModel =
                            new LocalModel.Builder().setAssetManifestFilePath("automl/manifest.json").build();
                    CustomObjectDetectorOptions customAutoMLODTOptions =
                            PreferenceUtils.getCustomObjectDetectorOptionsForStillImage(
                                    this, customAutoMLODTLocalModel);
                    //imageProcessor = new ObjectDetectorProcessor(this, customAutoMLODTOptions);
                    break;
                case FACE_DETECTION:
                    Log.i(TAG, "Using Face Detector Processor");
                    //imageProcessor = new FaceDetectorProcessor(this);
                    break;
                case BARCODE_SCANNING:
                    //imageProcessor = new BarcodeScannerProcessor(this);
                    break;
                case TEXT_RECOGNITION_LATIN:
                    if (imageProcessor != null) {
                        imageProcessor.stop();
                    }
                    imageProcessor =
                            new TextRecognitionProcessor(this, new TextRecognizerOptions.Builder().build());
                    break;
                case TEXT_RECOGNITION_CHINESE:
                    if (imageProcessor != null) {
                        imageProcessor.stop();
                    }
                    //imageProcessor = new TextRecognitionProcessor(this, new ChineseTextRecognizerOptions.Builder().build());
                    break;
                case TEXT_RECOGNITION_DEVANAGARI:
                    if (imageProcessor != null) {
                        imageProcessor.stop();
                    }
                    //imageProcessor = new TextRecognitionProcessor(this, new DevanagariTextRecognizerOptions.Builder().build());
                    break;
                case TEXT_RECOGNITION_JAPANESE:
                    if (imageProcessor != null) {
                        imageProcessor.stop();
                    }
                    // imageProcessor = new TextRecognitionProcessor(this, new JapaneseTextRecognizerOptions.Builder().build());
                    break;
                case TEXT_RECOGNITION_KOREAN:
                    if (imageProcessor != null) {
                        imageProcessor.stop();
                    }
                    //imageProcessor = new TextRecognitionProcessor(this, new KoreanTextRecognizerOptions.Builder().build());
                    break;
                case IMAGE_LABELING:
                    //imageProcessor = new LabelDetectorProcessor(this, ImageLabelerOptions.DEFAULT_OPTIONS);
                    break;
                case IMAGE_LABELING_CUSTOM:
                    Log.i(TAG, "Using Custom Image Label Detector Processor");
                    LocalModel localClassifier =
                            new LocalModel.Builder()
                                    .setAssetFilePath("custom_models/bird_classifier.tflite")
                                    .build();
                    CustomImageLabelerOptions customImageLabelerOptions =
                            new CustomImageLabelerOptions.Builder(localClassifier).build();
                    //imageProcessor = new LabelDetectorProcessor(this, customImageLabelerOptions);
                    break;
                case CUSTOM_AUTOML_LABELING:
                    Log.i(TAG, "Using Custom AutoML Image Label Detector Processor");
                    LocalModel customAutoMLLabelLocalModel =
                            new LocalModel.Builder().setAssetManifestFilePath("automl/manifest.json").build();
                    CustomImageLabelerOptions customAutoMLLabelOptions =
                            new CustomImageLabelerOptions.Builder(customAutoMLLabelLocalModel)
                                    .setConfidenceThreshold(0)
                                    .build();
                    //imageProcessor = new LabelDetectorProcessor(this, customAutoMLLabelOptions);
                    break;
                case POSE_DETECTION:
                    PoseDetectorOptionsBase poseDetectorOptions =
                            PreferenceUtils.getPoseDetectorOptionsForStillImage(this);
                    Log.i(TAG, "Using Pose Detector with options " + poseDetectorOptions);
                    boolean shouldShowInFrameLikelihood =
                            PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodStillImage(this);
                    boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
                    boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
                    boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);
                    /*imageProcessor =
                            new PoseDetectorProcessor(
                                    this,
                                    poseDetectorOptions,
                                    shouldShowInFrameLikelihood,
                                    visualizeZ,
                                    rescaleZ,
                                    runClassification,
                                    /* isStreamMode =  false);*/

                    break;
                case SELFIE_SEGMENTATION:
                    //imageProcessor = new SegmenterProcessor(this, /* isStreamMode= */ false);
                    break;
                default:
                    Log.e(TAG, "Unknown selectedMode: " + selectedMode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + selectedMode, e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("test1", intent.getStringExtra("connection_established"));
            Log.d("test1", intent.getStringArrayListExtra("text").toString());
            text = intent.getStringArrayListExtra("text");

            amount = "0.0";
            for (String s : text) {
                if (s.toLowerCase().contains("top") || s.toLowerCase().contains("tup") || s.toLowerCase().contains("tdp")) {
                    Log.d("test", "toplam stringi: " + s);
                    int index = text.indexOf(s);
                    for (int i=1; i<=5; i++) {
                        if(index + i < text.size()) {
                            Log.d("test", "tutar: " + text.get(index+i));
                            String read = text.get(index + i);
                            read = read.replace(",", ".");
                            if (read.matches("\\d+(?:\\.\\d+)?")) {
                                amount = read;
                                break;
                            }
                        }
                    }
                    Log.d("test", "Toplam Tutar: " + amount + " Lira");
                }
            }

            confirmAmount.setVisibility(View.VISIBLE);
            selectImage.setText(R.string.take_again);
        }
    };
}