package com.team10.recipeapp.viewmodel;

import android.util.Patterns;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.team10.recipeapp.data.model.User;
import com.team10.recipeapp.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    public static final String ERR_EMPTY_FIELDS = "EMPTY_FIELDS";
    public static final String ERR_INVALID_USERNAME = "INVALID_USERNAME";
    public static final String ERR_INVALID_EMAIL = "INVALID_EMAIL";
    public static final String ERR_WEAK_PASSWORD = "WEAK_PASSWORD";
    public static final String ERR_PASSWORDS_DONT_MATCH = "PASSWORDS_DONT_MATCH";

    public static class AuthEvent {
        public enum Type {
            REGISTER_SUCCESS, LOGIN_SUCCESS, PASSWORD_CHANGED, RESET_EMAIL_SENT, ERROR
        }

        public final Type type;
        @Nullable
        public final String errorCode;
        private boolean handled = false;

        AuthEvent(Type type, @Nullable String errorCode) {
            this.type = type;
            this.errorCode = errorCode;
        }

        @Nullable
        public AuthEvent getIfNotHandled() {
            if (handled) {
                return null;
            }
            handled = true;
            return this;
        }
    }

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<AuthEvent> event = new MutableLiveData<>();
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<AuthEvent> getEvent() {
        return event;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return repository.isLoggedIn();
    }

    public void logout() {
        repository.logout();
        currentUser.setValue(null);
    }

    public void register(String username, String email, String password, String confirmPassword) {
        username = username.trim();
        email = email.trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            postError(ERR_EMPTY_FIELDS);
            return;
        }
        if (username.length() < 3 || username.contains("@") || username.contains(" ")) {
            postError(ERR_INVALID_USERNAME);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            postError(ERR_INVALID_EMAIL);
            return;
        }
        if (!isPasswordValid(password)) {
            postError(ERR_WEAK_PASSWORD);
            return;
        }
        if (!password.equals(confirmPassword)) {
            postError(ERR_PASSWORDS_DONT_MATCH);
            return;
        }

        loading.setValue(true);
        repository.register(username, email, password, new AuthRepository.Callback<Void>() {
            @Override
            public void onSuccess(@Nullable Void result) {
                loading.setValue(false);
                event.setValue(new AuthEvent(AuthEvent.Type.REGISTER_SUCCESS, null));
            }

            @Override
            public void onError(String errorCode) {
                loading.setValue(false);
                postError(errorCode);
            }
        });
    }

    public void login(String identifier, String password) {
        identifier = identifier.trim();
        if (identifier.isEmpty() || password.isEmpty()) {
            postError(ERR_EMPTY_FIELDS);
            return;
        }

        loading.setValue(true);
        repository.login(identifier, password, new AuthRepository.Callback<User>() {
            @Override
            public void onSuccess(@Nullable User result) {
                loading.setValue(false);
                currentUser.setValue(result);
                event.setValue(new AuthEvent(AuthEvent.Type.LOGIN_SUCCESS, null));
            }

            @Override
            public void onError(String errorCode) {
                loading.setValue(false);
                postError(errorCode);
            }
        });
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            postError(ERR_EMPTY_FIELDS);
            return;
        }
        if (!isPasswordValid(newPassword)) {
            postError(ERR_WEAK_PASSWORD);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            postError(ERR_PASSWORDS_DONT_MATCH);
            return;
        }

        loading.setValue(true);
        repository.changePassword(currentPassword, newPassword, new AuthRepository.Callback<Void>() {
            @Override
            public void onSuccess(@Nullable Void result) {
                loading.setValue(false);
                event.setValue(new AuthEvent(AuthEvent.Type.PASSWORD_CHANGED, null));
            }

            @Override
            public void onError(String errorCode) {
                loading.setValue(false);
                postError(errorCode);
            }
        });
    }

    public void resetPassword(String email) {
        email = email.trim();
        if (email.isEmpty()) {
            postError(ERR_EMPTY_FIELDS);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            postError(ERR_INVALID_EMAIL);
            return;
        }

        loading.setValue(true);
        repository.resetPassword(email, new AuthRepository.Callback<Void>() {
            @Override
            public void onSuccess(@Nullable Void result) {
                loading.setValue(false);
                event.setValue(new AuthEvent(AuthEvent.Type.RESET_EMAIL_SENT, null));
            }

            @Override
            public void onError(String errorCode) {
                loading.setValue(false);
                postError(errorCode);
            }
        });
    }

    private boolean isPasswordValid(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) upper = true;
            else if (Character.isLowerCase(c)) lower = true;
            else if (Character.isDigit(c)) digit = true;
        }
        return upper && lower && digit;
    }

    private void postError(String errorCode) {
        event.setValue(new AuthEvent(AuthEvent.Type.ERROR, errorCode));
    }
}