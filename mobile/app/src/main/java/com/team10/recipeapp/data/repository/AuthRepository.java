package com.team10.recipeapp.data.repository;

import androidx.annotation.Nullable;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team10.recipeapp.data.model.User;

public class AuthRepository {

    public static final String ERR_USERNAME_TAKEN = "USERNAME_TAKEN";
    public static final String ERR_EMAIL_TAKEN = "EMAIL_TAKEN";
    public static final String ERR_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ERR_NOT_VERIFIED = "NOT_VERIFIED";
    public static final String ERR_USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String ERR_UNKNOWN = "UNKNOWN";

    private static final String USERS = "users";

    public interface Callback<T> {
        void onSuccess(@Nullable T result);

        void onError(String errorCode);
    }

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    @Nullable
    public FirebaseUser getCurrentFirebaseUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }

    // Registration: unique username check -> create auth account -> save profile -> send verification email
    public void register(String username, String email, String password, Callback<Void> cb) {
        db.collection(USERS).whereEqualTo("username", username).limit(1).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        cb.onError(ERR_USERNAME_TAKEN);
                        return;
                    }
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(result -> {
                                FirebaseUser fu = result.getUser();
                                if (fu == null) {
                                    cb.onError(ERR_UNKNOWN);
                                    return;
                                }
                                User user = new User(fu.getUid(), username, email, null);
                                db.collection(USERS).document(fu.getUid()).set(user)
                                        .addOnSuccessListener(v -> fu.sendEmailVerification()
                                                .addOnSuccessListener(v2 -> {
                                                    auth.signOut();
                                                    cb.onSuccess(null);
                                                })
                                                .addOnFailureListener(e -> cb.onError(ERR_UNKNOWN)))
                                        .addOnFailureListener(e -> cb.onError(ERR_UNKNOWN));
                            })
                            .addOnFailureListener(e -> cb.onError(mapError(e)));
                })
                .addOnFailureListener(e -> cb.onError(ERR_UNKNOWN));
    }

    // Login with username or email
    public void login(String identifier, String password, Callback<User> cb) {
        if (identifier.contains("@")) {
            signIn(identifier, password, cb);
            return;
        }
        db.collection(USERS).whereEqualTo("username", identifier).limit(1).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        cb.onError(ERR_INVALID_CREDENTIALS);
                        return;
                    }
                    User found = snap.getDocuments().get(0).toObject(User.class);
                    if (found == null || found.getEmail() == null) {
                        cb.onError(ERR_INVALID_CREDENTIALS);
                        return;
                    }
                    signIn(found.getEmail(), password, cb);
                })
                .addOnFailureListener(e -> cb.onError(ERR_UNKNOWN));
    }

    private void signIn(String email, String password, Callback<User> cb) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser fu = result.getUser();
                    if (fu == null) {
                        cb.onError(ERR_UNKNOWN);
                        return;
                    }
                    fu.reload()
                            .addOnSuccessListener(v -> {
                                if (!fu.isEmailVerified()) {
                                    auth.signOut();
                                    cb.onError(ERR_NOT_VERIFIED);
                                    return;
                                }
                                activateAndLoadUser(fu.getUid(), cb);
                            })
                            .addOnFailureListener(e -> cb.onError(ERR_UNKNOWN));
                })
                .addOnFailureListener(e -> cb.onError(mapError(e)));
    }

    private void activateAndLoadUser(String uid, Callback<User> cb) {
        DocumentReference ref = db.collection(USERS).document(uid);
        ref.update("activated", true)
                .addOnSuccessListener(v -> ref.get()
                        .addOnSuccessListener(doc -> {
                            User user = doc.toObject(User.class);
                            if (user == null) {
                                auth.signOut();
                                cb.onError(ERR_USER_NOT_FOUND);
                                return;
                            }
                            cb.onSuccess(user);
                        })
                        .addOnFailureListener(e -> cb.onError(ERR_UNKNOWN)))
                .addOnFailureListener(e -> {
                    auth.signOut();
                    cb.onError(ERR_USER_NOT_FOUND);
                });
    }

    public void changePassword(String currentPassword, String newPassword, Callback<Void> cb) {
        FirebaseUser fu = auth.getCurrentUser();
        if (fu == null || fu.getEmail() == null) {
            cb.onError(ERR_USER_NOT_FOUND);
            return;
        }
        AuthCredential credential = EmailAuthProvider.getCredential(fu.getEmail(), currentPassword);
        fu.reauthenticate(credential)
                .addOnSuccessListener(v -> fu.updatePassword(newPassword)
                        .addOnSuccessListener(v2 -> cb.onSuccess(null))
                        .addOnFailureListener(e -> cb.onError(mapError(e))))
                .addOnFailureListener(e -> cb.onError(mapError(e)));
    }

    public void resetPassword(String email, Callback<Void> cb) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(e -> cb.onError(mapError(e)));
    }

    private String mapError(Exception e) {
        if (e instanceof FirebaseAuthUserCollisionException) {
            return ERR_EMAIL_TAKEN;
        }
        if (e instanceof FirebaseAuthInvalidUserException
                || e instanceof FirebaseAuthInvalidCredentialsException) {
            return ERR_INVALID_CREDENTIALS;
        }
        return ERR_UNKNOWN;
    }
}