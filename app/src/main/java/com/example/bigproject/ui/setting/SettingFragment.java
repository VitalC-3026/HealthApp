package com.example.bigproject.ui.setting;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bigproject.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingFragment extends Fragment {

    private SettingViewModel settingViewModel;
    private Button storage;
    private Switch[] isGranted;
    String[] permissions;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingViewModel =
                ViewModelProviders.of(this).get(SettingViewModel.class);
        View root = inflater.inflate(R.layout.fragment_setting, container, false);
        // final TextView textView = root.findViewById(R.id.text_slideshow);
        /*settingViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        isGranted = new Switch[5];

        permissions = new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
        };
        // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
        List<String> mPermissionList = new ArrayList<>();

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean isGrantedPermissions(String permissions) {
        return ActivityCompat.checkSelfPermission(Objects.requireNonNull(getContext()), permissions)
                == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onStart() {
        isGranted[0] = (Switch) getActivity().findViewById(R.id.phone_state);
        isGranted[1] = (Switch) getActivity().findViewById(R.id.location);
        isGranted[2] = (Switch) getActivity().findViewById(R.id.read_write_storage);
        isGranted[3] = (Switch) getActivity().findViewById(R.id.censors);
        isGranted[4] = (Switch) getActivity().findViewById(R.id.notification);
        if (isGrantedPermissions(permissions[0])) {
            isGranted[0].setChecked(true);
        } else {
            isGranted[0].setChecked(false);
        }
        if (isGrantedPermissions(permissions[1]) && isGrantedPermissions(permissions[2])) {
            isGranted[1].setChecked(true);
        } else {
            isGranted[1].setChecked(false);
        }
        if (isGrantedPermissions(permissions[3]) && isGrantedPermissions(permissions[4])) {
            isGranted[2].setChecked(true);
        } else {
            isGranted[2].setChecked(false);
        }
        if (isGrantedPermissions(permissions[5])) {
            isGranted[3].setChecked(true);
        } else {
            isGranted[3].setChecked(false);
        }
        if (isGrantedPermissions(permissions[6])) {
            isGranted[4].setChecked(true);
        } else {
            isGranted[4].setChecked(false);
        }
        isGranted[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingFragment.this.requestPermissions(new String[]{permissions[0]}, 100);
                }
            }
        });
        isGranted[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!isGrantedPermissions(permissions[1])) {
                        SettingFragment.this.requestPermissions(new String[]{permissions[1]}, 100);
                    }
                    if (!isGrantedPermissions(permissions[2])) {
                        SettingFragment.this.requestPermissions(new String[]{permissions[2]}, 100);
                    }
                }
            }
        });
        isGranted[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!isGrantedPermissions(permissions[3])) {
                        SettingFragment.this.requestPermissions(new String[]{permissions[3]}, 100);
                    }
                    if (!isGrantedPermissions(permissions[4])) {
                        SettingFragment.this.requestPermissions(new String[]{permissions[4]}, 100);
                    }
                }
            }
        });
        isGranted[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!isGrantedPermissions(permissions[5])) {
                        SettingFragment.this.requestPermissions(new String[]{permissions[5]}, 100);
                    }

                }
            }
        });
        isGranted[4].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!isGrantedPermissions(permissions[6])) {
                        SettingFragment.this.requestPermissions(new String[]{permissions[6]}, 100);
                    }

                }
            }
        });


        super.onStart();
    }

}
