package com.example.bigproject.ui.description;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DescriptionViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public DescriptionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("功能介绍");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
