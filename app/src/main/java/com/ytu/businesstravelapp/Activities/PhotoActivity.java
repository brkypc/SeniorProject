package com.ytu.businesstravelapp.Activities;

import static com.ytu.businesstravelapp.Activities.MainActivity.firebaseURL;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
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
import com.ytu.businesstravelapp.R;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Objects;

public class PhotoActivity extends AppCompatActivity {
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private FirebaseFunctions mFunctions;
    private ImageView billPhoto;
    private String distance, date, tripTime, taxiType, calculatedPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        Intent intent = getIntent();
        distance = intent.getStringExtra("distance");
        date = intent.getStringExtra("date");
        tripTime = intent.getStringExtra("tripTime");
        taxiType = intent.getStringExtra("taxiType");
        calculatedPrice = intent.getStringExtra("amount");

        TextView amountField = findViewById(R.id.amount);
        TextView distanceField = findViewById(R.id.distance);
        AppCompatButton button = findViewById(R.id.receiptButton);
        billPhoto = findViewById(R.id.billPhoto);

        distanceField.setText(MessageFormat.format("{0} km", distance));
        amountField.setText(MessageFormat.format("{0} ₺", calculatedPrice));

        button.setOnClickListener(view -> {
            if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_CAMERA_PERMISSION_CODE);
            } else {
                chooseImage(PhotoActivity.this);
            }
        });
    }


    public void startOCR() {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(((BitmapDrawable) billPhoto.getDrawable()).getBitmap(), 0);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(visionText -> {
                        })
                        .addOnFailureListener(
                                e -> {
                                });

        while (!result.isComplete()) ;

        TextView resultView = findViewById(R.id.resultText);
        TextView receiptAmount = findViewById(R.id.receiptAmount);

        String resultText = result.getResult().getText();
        resultView.setText(resultText);

        Log.d("test", resultText);
        Log.d("test", "starts here\n*********");
        boolean control = true;

        for (Text.TextBlock block : result.getResult().getTextBlocks()) {
            Log.d("test", block.getText());
            if (block.getText().toLowerCase().contains("toplam")) {
                control = false;
                int index = result.getResult().getTextBlocks().indexOf(block);
                String amount = result.getResult().getTextBlocks().get(index + 1).getText();
                receiptAmount.setText("Fiş Tutarı: " + amount);
                Log.d("test", "Toplam Tutar: " + amount + " Lira");
                amount = amount.replace(",", ".");
                if(amount.matches("\\d+(?:\\.\\d+)?")) {
                    EditText edittext = new EditText(this);
                    edittext.setText(amount);
                    edittext.setGravity(Gravity.CENTER);
                    edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setMessage("Faturadan okunan tutarı onaylayınız");
                    alert.setView(edittext);
                    alert.setPositiveButton("Onayla", (dialog, whichButton) -> {
                        String userInput = edittext.getText().toString();
                        saveToFirebase(userInput);
                        /*if(userInput.matches("\\d+(?:\\.\\d+)?")) {
                            Toast.makeText(PhotoActivity.this, userInput, Toast.LENGTH_SHORT).show();
                            saveToFirebase(userInput);
                        }else {
                            Toast.makeText(PhotoActivity.this,"Lütfen sadece sayı giriniz", Toast.LENGTH_SHORT).show();
                        }*/
                    });
                    alert.setNegativeButton("İptal", null);

                    alert.show();

                }
                else {
                    Toast.makeText(PhotoActivity.this, R.string.noAmountFound, Toast.LENGTH_SHORT).show();
                }
                Log.d("test1", amount.matches("\\d+(?:\\.\\d+)?")+ " matches");

                break;
            }
        }
        if (control) Toast.makeText(PhotoActivity.this, R.string.noAmountFound, Toast.LENGTH_SHORT).show();
    }

    private void saveToFirebase(String amount) {
        String status = "no";

        if (Float.parseFloat(amount) < Float.parseFloat(amount)) {
            Log.d("test1", "fatura küçüktür hesaplanan");
            status = "yes";
        } else if (Float.parseFloat(amount) < (Float.parseFloat(amount) * 1.2)) {
            Log.d("test1", "fatura küçüktür 1.2 hesaplanan");
            status = "yes";
        }
        Log.d("test1", "" + Float.parseFloat(amount));
        Log.d("test1", "" + Float.parseFloat(calculatedPrice) * 1.2);

        FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseURL);
        DatabaseReference tripRef = database.getReference("trips");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("date", date);
        hashMap.put("tripTime", tripTime);
        hashMap.put("taxiType", taxiType);
        hashMap.put("distance", distance);
        hashMap.put("amount", calculatedPrice);

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        String[] uName;
        if (fUser != null) {
            String uEmail = fUser.getEmail();
            uName = Objects.requireNonNull(uEmail).split("@");
            uName = uName[0].split("\\.");
            if (uName.length > 1) {
                String fName1 = uName[0].substring(0, 1).toUpperCase() + uName[0].substring(1);
                String fName2 = uName[1].substring(0, 1).toUpperCase() + uName[1].substring(1);
                hashMap.put("nameSurname", fName1 + " " + fName2);
            } else {
                hashMap.put("nameSurname", uName[0].substring(0, 1).toUpperCase() + uName[0].substring(1));
            }
        }
        hashMap.put("billPrice", amount);
        hashMap.put("status", status);

        //tripRef.push().setValue(hashMap);
        Toast.makeText(PhotoActivity.this, "Seyahatiniz kaydedildi", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PhotoActivity.this, TripsActivity.class);
        startActivity(intent);
        finish();
    }

    private void chooseImage(Context context) {
        final CharSequence[] optionsMenu = {"Fotoğraf Çek", "Galeriden Seç"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(optionsMenu, (dialogInterface, i) -> {
            if (optionsMenu[i].equals("Fotoğraf Çek")) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
            } else if (optionsMenu[i].equals("Galeriden Seç")) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
            }
        });
        builder.show();
    }

    public void startCloudOCR() {
        Log.d("test1", "ocr2");
        Bitmap bitmap = ((BitmapDrawable) billPhoto.getDrawable()).getBitmap();
        bitmap = scaleBitmapDown(bitmap);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String base64encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        mFunctions = FirebaseFunctions.getInstance();

        JsonObject request = new JsonObject();

        JsonObject image = new JsonObject();
        image.add("content", new JsonPrimitive(base64encoded));
        request.add("image", image);

        JsonObject feature = new JsonObject();
        feature.add("type", new JsonPrimitive("TEXT_DETECTION"));

        JsonArray features = new JsonArray();
        features.add(feature);
        request.add("features", features);
        Log.d("test1", request.toString());
        annotateImage(request.toString())
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("test1", "not successful");
                        Log.d("test1", Objects.requireNonNull(task.getException()).toString());
                        //Log.d("test1",task.getResult().getAsString());
                    } else {
                        Log.d("test1", "successful");
                        JsonObject annotation = task.getResult().getAsJsonArray().get(0).getAsJsonObject().get("fullTextAnnotation").getAsJsonObject();
                        System.out.format("%nComplete annotation:%n");
                        System.out.format("%s%n", annotation.get("text").getAsString());
                        Log.d("test1", annotation.get("text").getAsString());
                    }
                });
    }

    private Task<JsonElement> annotateImage(String requestJson) {
        return mFunctions
                .getHttpsCallable("annotateImage")
                .call(requestJson)
                .continueWith(task -> {
                    Log.d("test1", "here");
                    Log.d("test1", String.valueOf(JsonParser.parseString(new Gson().toJson(task.getResult().getData()))));
                    return JsonParser.parseString(new Gson().toJson(task.getResult().getData()));
                });
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = 640;
        int resizedHeight = 640;

        if (originalHeight > originalWidth) {
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else {
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
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
                        billPhoto.setImageBitmap(selectedImage);
                        startOCR();
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
                                billPhoto.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                            }
                        }
                        startOCR();
                    }
                    break;
            }
        }
    }

}