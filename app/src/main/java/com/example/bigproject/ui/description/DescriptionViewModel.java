package com.example.bigproject.ui.description;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DescriptionViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public DescriptionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("这是一款可以帮助你养成健康的使用手机习惯的app，希望在不可避免经常使用手机的情况下，" +
                "好好保护你的双眼\n这款app有以下几个功能:\n1、在光线过暗的情况下提醒您前往光线充足的地方看手机;\n" +
                "2、在走动的情况下提醒您不要边走边看手机;\n3、督促您用正确的姿势查看手机，避免卧躺情况下长时间看手机;\n" +
                "4、为您生成每日的使用手机报告，帮助您养成良好的习惯。\n祝您使用愉快！");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
