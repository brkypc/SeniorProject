package com.ytu.businesstravelapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.Objects;

public class PhotoActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    ImageView imageView;
    private FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        Intent intent = getIntent();
        String amount = intent.getStringExtra("amount");
        String km = intent.getStringExtra("km");

        TextView amountField = findViewById(R.id.amount);
        TextView distanceField = findViewById(R.id.distance);
        AppCompatButton button = findViewById(R.id.receiptButton);
        imageView = findViewById(R.id.imageView);

        distanceField.setText(MessageFormat.format("{0} km", km));
        amountField.setText(MessageFormat.format("{0} ₺", amount));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    /*Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);*/
                    chooseImage(PhotoActivity.this);

                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                chooseImage(PhotoActivity.this);
            } else {
                Toast.makeText(this, "İzin reddedildi.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imageView.setImageBitmap(selectedImage);
                        startOCR2();
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                            }
                        }
                        startOCR2();
                    }
                    break;
            }

        }
    }

    public void startOCR2() {

        Log.d("test1","ocr2");
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        bitmap = scaleBitmapDown(bitmap, 640);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String base64encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);


        mFunctions = FirebaseFunctions.getInstance();
        if(mFunctions == null) Log.d("test1","null");
        else   Log.d("test1","not null");


        // Create json request to cloud vision
        JsonObject request = new JsonObject();
// Add image to request
        JsonObject image = new JsonObject();
        image.add("content", new JsonPrimitive(base64encoded));
        request.add("image", image);
//Add features to the request
        JsonObject feature = new JsonObject();
        feature.add("type", new JsonPrimitive("TEXT_DETECTION"));
// Alternatively, for DOCUMENT_TEXT_DETECTION:
//feature.add("type", new JsonPrimitive("DOCUMENT_TEXT_DETECTION"));
        JsonArray features = new JsonArray();
        features.add(feature);
        request.add("features", features);
        Log.d("test1",request.toString());
        annotateImage(request.toString())
                .addOnCompleteListener(new OnCompleteListener<JsonElement>() {
                    @Override
                    public void onComplete(@NonNull Task<JsonElement> task) {
                        if (!task.isSuccessful()) {
                            Log.d("test1","not successful");
                            Log.d("test1", Objects.requireNonNull(task.getException()).toString());
                            //Log.d("test1",task.getResult().getAsString());
                        } else {
                            Log.d("test1","successful");
                            JsonObject annotation = task.getResult().getAsJsonArray().get(0).getAsJsonObject().get("fullTextAnnotation").getAsJsonObject();
                            System.out.format("%nComplete annotation:%n");
                            System.out.format("%s%n", annotation.get("text").getAsString());
                            Log.d("test1", annotation.get("text").getAsString());
                        }
                    }
                });



    }

    private Task<JsonElement> annotateImage(String requestJson) {
        return mFunctions
                .getHttpsCallable("annotateImage")
                .call(requestJson)
                .continueWith(new Continuation<HttpsCallableResult, JsonElement>() {
                    @Override
                    public JsonElement then(@NonNull Task<HttpsCallableResult> task) {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        Log.d("test1", "here");
                        Log.d("test1", String.valueOf(JsonParser.parseString(new Gson().toJson(task.getResult().getData()))));
                        return JsonParser.parseString(new Gson().toJson(task.getResult().getData()));
                    }
                });
    }

    public void startOCR() {

        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(((BitmapDrawable) imageView.getDrawable()).getBitmap(), 0);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // ...
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

        while (!result.isComplete()) ;

        String resultText = result.getResult().getText();
        Log.d("test", resultText);
        Log.d("test", "starts here\n*********");
        for (Text.TextBlock block : result.getResult().getTextBlocks()) {
            Log.d("test", block.getText());
            if (block.getText().toLowerCase().contains("toplam")) {
                int index = result.getResult().getTextBlocks().indexOf(block);
                String amount = result.getResult().getTextBlocks().get(index + 1).getText();
                //Log.d("test","Toplam Tutar: " + amount + " Lira");
            }
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }


    private void chooseImage(Context context) {
        final CharSequence[] optionsMenu = {"Fotoğraf Çek", "Galeriden Seç"}; // create a menuOption Array
        // create a dialog for showing the optionsMenu
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // set the items in builder
        builder.setItems(optionsMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (optionsMenu[i].equals("Fotoğraf Çek")) {
                    // Open the camera and get the photo
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                } else if (optionsMenu[i].equals("Galeriden Seç")) {
                    // choose from  external storage
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);
                }
            }
        });
        builder.show();
    }

}