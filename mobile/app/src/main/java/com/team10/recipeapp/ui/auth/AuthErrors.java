package com.team10.recipeapp.ui.auth;

import android.content.Context;

import com.team10.recipeapp.R;
import com.team10.recipeapp.data.repository.AuthRepository;
import com.team10.recipeapp.viewmodel.AuthViewModel;

public class AuthErrors {

    private AuthErrors() {
    }

    public static String getMessage(Context context, String code) {
        int resId;
        switch (code) {
            case AuthViewModel.ERR_EMPTY_FIELDS:
                resId = R.string.err_empty_fields;
                break;
            case AuthViewModel.ERR_INVALID_USERNAME:
                resId = R.string.err_invalid_username;
                break;
            case AuthViewModel.ERR_INVALID_EMAIL:
                resId = R.string.err_invalid_email;
                break;
            case AuthViewModel.ERR_WEAK_PASSWORD:
                resId = R.string.err_weak_password;
                break;
            case AuthViewModel.ERR_PASSWORDS_DONT_MATCH:
                resId = R.string.err_passwords_dont_match;
                break;
            case AuthRepository.ERR_USERNAME_TAKEN:
                resId = R.string.err_username_taken;
                break;
            case AuthRepository.ERR_EMAIL_TAKEN:
                resId = R.string.err_email_taken;
                break;
            case AuthRepository.ERR_INVALID_CREDENTIALS:
                resId = R.string.err_invalid_credentials;
                break;
            case AuthRepository.ERR_NOT_VERIFIED:
                resId = R.string.err_not_verified;
                break;
            case AuthRepository.ERR_USER_NOT_FOUND:
                resId = R.string.err_user_not_found;
                break;
            default:
                resId = R.string.err_unknown;
        }
        return context.getString(resId);
    }
}