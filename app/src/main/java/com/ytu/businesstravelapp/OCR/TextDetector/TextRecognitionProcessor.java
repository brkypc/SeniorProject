package com.ytu.businesstravelapp.OCR.TextDetector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.Text.Element;
import com.google.mlkit.vision.text.Text.Line;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface;
import com.ytu.businesstravelapp.OCR.CameraSettings.GraphicOverlay;
import com.ytu.businesstravelapp.OCR.CameraSettings.VisionProcessorBase;
import com.ytu.businesstravelapp.OCR.Preference.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TextRecognitionProcessor extends VisionProcessorBase<Text> {
    public static final String BROADCAST_FILTER = "ManageConection_broadcast_receiver_intent_filter";
    private static final String TAG = "TextRecProcessor";

    private final TextRecognizer textRecognizer;
    private final Boolean shouldGroupRecognizedTextInBlocks;
    private final Boolean showLanguageTag;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public TextRecognitionProcessor(
            Context context, TextRecognizerOptionsInterface textRecognizerOptions) {
        super(context);
        mContext = context;

        shouldGroupRecognizedTextInBlocks = PreferenceUtils.shouldGroupRecognizedTextInBlocks(context);
        showLanguageTag = PreferenceUtils.showLanguageTag(context);
        textRecognizer = TextRecognition.getClient(textRecognizerOptions);
    }

    @Override
    public void stop() {
        super.stop();
        textRecognizer.close();
    }

    @Override
    protected Task<Text> detectInImage(InputImage image) {
        return textRecognizer.process(image);
    }

    @Override
    protected void onSuccess(@NonNull Text text, @NonNull GraphicOverlay graphicOverlay) {
        Log.d(TAG, "On-device Text detection successful");
        logExtrasForTesting(text);
        graphicOverlay.add(
                new TextGraphic(graphicOverlay, text, shouldGroupRecognizedTextInBlocks, showLanguageTag));
    }

    private static void logExtrasForTesting(Text text) {
        Intent intent = new Intent(BROADCAST_FILTER);
        ArrayList<String> textBlocks = new ArrayList<>();
        if (text != null) {
            for (Text.TextBlock textBlock : text.getTextBlocks()) {
                textBlocks.add(textBlock.getText());
            }

            intent.putExtra("connection_established", "true");
            intent.putStringArrayListExtra("text", textBlocks);
            mContext.sendBroadcast(intent);
            Log.v(MANUAL_TESTING_LOG, "Detected text has : " + text.getTextBlocks().size() + " blocks");
            for (int i = 0; i < text.getTextBlocks().size(); ++i) {
                List<Line> lines = text.getTextBlocks().get(i).getLines();
                Log.v(
                        MANUAL_TESTING_LOG,
                        String.format("Detected text block %d has %d lines", i, lines.size()));
                for (int j = 0; j < lines.size(); ++j) {
                    List<Element> elements = lines.get(j).getElements();
                    Log.v(
                            MANUAL_TESTING_LOG,
                            String.format("Detected text line %d has %d elements", j, elements.size()));
                    for (int k = 0; k < elements.size(); ++k) {
                        Element element = elements.get(k);
                        Log.v(
                                MANUAL_TESTING_LOG,
                                String.format("Detected text element %d says: %s", k, element.getText()));
                        Log.v(
                                MANUAL_TESTING_LOG,
                                String.format(
                                        "Detected text element %d has a bounding box: %s",
                                        k, Objects.requireNonNull(element.getBoundingBox()).flattenToString()));
                        Log.v(
                                MANUAL_TESTING_LOG,
                                String.format(
                                        "Expected corner point size is 4, get %d", Objects.requireNonNull(element.getCornerPoints()).length));
                        for (Point point : element.getCornerPoints()) {
                            Log.v(
                                    MANUAL_TESTING_LOG,
                                    String.format(
                                            "Corner point for element %d is located at: x - %d, y = %d",
                                            k, point.x, point.y));
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Text detection failed." + e);
    }
}
