package com.example.bigproject.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("帮助您养成健康使用手机的好习惯，一起保护明亮的眼睛");
    }

    public LiveData<String> getText() {
        return mText;
    }
}