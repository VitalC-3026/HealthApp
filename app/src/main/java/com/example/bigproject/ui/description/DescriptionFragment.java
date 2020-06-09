package com.example.bigproject.ui.description;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bigproject.R;
import com.example.bigproject.ui.team.TeamViewModel;

public class DescriptionFragment extends Fragment {
    private DescriptionViewModel descriptionViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        descriptionViewModel =
                ViewModelProviders.of(this).get(DescriptionViewModel.class);
        View root = inflater.inflate(R.layout.fragment_description, container, false);
        final TextView textView = root.findViewById(R.id.text_description);
        descriptionViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}
