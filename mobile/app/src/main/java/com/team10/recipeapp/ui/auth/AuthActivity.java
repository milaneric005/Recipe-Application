package com.team10.recipeapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.team10.recipeapp.MainActivity;
import com.team10.recipeapp.R;
import com.team10.recipeapp.viewmodel.AuthViewModel;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthViewModel viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        if (viewModel.isLoggedIn()) {
            openMain();
            return;
        }
        setContentView(R.layout.activity_auth);
    }

    // Called by LoginFragment after a successful login
    public void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}