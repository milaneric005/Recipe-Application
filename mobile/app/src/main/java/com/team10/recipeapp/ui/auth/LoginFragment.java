package com.team10.recipeapp.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.team10.recipeapp.R;
import com.team10.recipeapp.viewmodel.AuthViewModel;

public class LoginFragment extends Fragment {

    private AuthViewModel viewModel;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Shared with the other auth fragments through the activity
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        EditText etIdentifier = view.findViewById(R.id.et_identifier);
        EditText etPassword = view.findViewById(R.id.et_password);
        MaterialButton btnLogin = view.findViewById(R.id.btn_login);
        LinearProgressIndicator progress = view.findViewById(R.id.progress);
        TextView tvForgot = view.findViewById(R.id.tv_forgot);
        TextView tvRegister = view.findViewById(R.id.tv_register);

        btnLogin.setOnClickListener(v -> viewModel.login(
                etIdentifier.getText().toString(),
                etPassword.getText().toString()));

        tvForgot.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_login_to_forgot));

        tvRegister.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_login_to_register));

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progress.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
            btnLogin.setEnabled(!isLoading);
        });

        viewModel.getEvent().observe(getViewLifecycleOwner(), e -> {
            AuthViewModel.AuthEvent event = e.getIfNotHandled();
            if (event == null) {
                return;
            }
            if (event.type == AuthViewModel.AuthEvent.Type.LOGIN_SUCCESS) {
                ((AuthActivity) requireActivity()).openMain();
            } else if (event.type == AuthViewModel.AuthEvent.Type.ERROR) {
                Toast.makeText(requireContext(),
                        AuthErrors.getMessage(requireContext(), event.errorCode),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}