package com.example.bigproject.ui.gallery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GalleryViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public GalleryViewModel() {
//        mText = new MutableLiveData<>();
//        mText.setValue("This is gallery fragment");a


    }

    public LiveData<String> getText() {
        return mText;
    }
}

