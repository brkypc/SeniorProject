package com.ytu.businesstravelapp.OCR.Preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.gms.common.images.Size;
import com.google.common.base.Preconditions;
import com.ytu.businesstravelapp.OCR.CameraSettings.CameraSource;
import com.ytu.businesstravelapp.OCR.CameraSettings.CameraSource.SizePair;
import com.ytu.businesstravelapp.R;

public class PreferenceUtils {
    static void saveString(Context context, @StringRes int prefKeyId, @Nullable String value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(context.getString(prefKeyId), value)
                .apply();
    }

    @Nullable
    public static SizePair getCameraPreviewSizePair(Context context, int cameraId) {
        Preconditions.checkArgument(
                cameraId == CameraSource.CAMERA_FACING_BACK
                        || cameraId == CameraSource.CAMERA_FACING_FRONT);
        String previewSizePrefKey;
        String pictureSizePrefKey;
        if (cameraId == CameraSource.CAMERA_FACING_BACK) {
            previewSizePrefKey = context.getString(R.string.pref_key_rear_camera_preview_size);
            pictureSizePrefKey = context.getString(R.string.pref_key_rear_camera_picture_size);
        } else {
            previewSizePrefKey = context.getString(R.string.pref_key_front_camera_preview_size);
            pictureSizePrefKey = context.getString(R.string.pref_key_front_camera_picture_size);
        }

        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return new SizePair(
                    Size.parseSize(sharedPreferences.getString(previewSizePrefKey, null)),
                    Size.parseSize(sharedPreferences.getString(pictureSizePrefKey, null)));
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean shouldHideDetectionInfo(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(R.string.pref_key_info_hide);
        return sharedPreferences.getBoolean(prefKey, false);
    }


    public static boolean shouldGroupRecognizedTextInBlocks(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(R.string.pref_key_group_recognized_text_in_blocks);
        return sharedPreferences.getBoolean(prefKey, false);
    }

    public static boolean showLanguageTag(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(R.string.pref_key_show_language_tag);
        return sharedPreferences.getBoolean(prefKey, false);
    }


    public static boolean isCameraLiveViewportEnabled(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(R.string.pref_key_camera_live_viewport);
        return sharedPreferences.getBoolean(prefKey, false);
    }

    private PreferenceUtils() {
    }
}
