package com.ytu.businesstravelapp.OCR.Preference;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ytu.businesstravelapp.R;

public class StillImagePreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_still_image);
    }
}
