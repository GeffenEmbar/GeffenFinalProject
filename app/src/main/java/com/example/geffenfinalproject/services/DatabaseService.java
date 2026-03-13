package com.example.geffenfinalproject.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.geffenfinalproject.models.Group;
import com.example.geffenfinalproject.models.Question;
import com.example.geffenfinalproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/// a service to interact with the Firebase Realtime Database.
/// this class is a singleton, use getInstance() to get an instance of this class
/// @see #getInstance()
/// @see FirebaseDatabase
public class DatabaseService {

    /// tag for logging
    private static final String TAG = "DatabaseService";

    /// paths for different data types in the database
    private static final String USERS_PATH = "users",
            QUEST_PATH = "questions",
            GROUPS_PATH = "groups";

    /// callback interface for database operations
    public interface DatabaseCallback<T> {
        void onCompleted(T object);
        void onFailed(Exception e);
    }

    /// the instance of this class
    private static DatabaseService instance;

    /// the reference to the database
    private final DatabaseReference databaseReference;

    /// use getInstance() to get an instance of this class
    private DatabaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    /// get an instance of this class
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    // region private generic methods

    /// write data to the database at a specific path
    private void writeData(@NotNull final String path,
                           @NotNull final Object data,
                           @Nullable final DatabaseCallback<Void> callback) {
        readData(path).setValue(data, (error, ref) -> {
            if (callback == null) return;

            if (error != null) {
                callback.onFailed(error.toException());
            } else {
                callback.onCompleted(null);
            }
        });
    }

    /// remove data from the database at a specific path
    private void deleteData(@NotNull final String path,
                            @Nullable final DatabaseCallback<Void> callback) {
        readData(path).removeValue((error, ref) -> {
            if (callback == null) return;

            if (error != null) {
                callback.onFailed(error.toException());
            } else {
                callback.onCompleted(null);
            }
        });
    }

    /// read data from the database at a specific path
    private DatabaseReference readData(@NotNull final String path) {
        return databaseReference.child(path);
    }

    /// get data from the database at a specific path
    private <T> void getData(@NotNull final String path,
                             @NotNull final Class<T> clazz,
                             @NotNull final DatabaseCallback<T> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data from path: " + path, task.getException());
                callback.onFailed(task.getException() != null
                        ? task.getException()
                        : new Exception("Unknown database error"));
                return;
            }

            DataSnapshot snapshot = task.getResult();
            if (snapshot == null || !snapshot.exists()) {
                callback.onCompleted(null);
                return;
            }

            T data = snapshot.getValue(clazz);
            callback.onCompleted(data);
        });
    }

    /// get a list of data from the database at a specific path
    private <T> void getDataList(@NotNull final String path,
                                 @NotNull final Class<T> clazz,
                                 @NotNull final DatabaseCallback<List<T>> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data list from path: " + path, task.getException());
                callback.onFailed(task.getException() != null
                        ? task.getException()
                        : new Exception("Unknown database error"));
                return;
            }

            List<T> tList = new ArrayList<>();
            DataSnapshot result = task.getResult();

            if (result != null) {
                for (DataSnapshot dataSnapshot : result.getChildren()) {
                    T t = dataSnapshot.getValue(clazz);
                    if (t != null) {
                        tList.add(t);
                    }
                }
            }

            callback.onCompleted(tList);
        });
    }

    /// generate a new id for a new object in the database
    private String generateNewId(@NotNull final String path) {
        return databaseReference.child(path).push().getKey();
    }

    /// run a transaction on the data at a specific path
    private <T> void runTransaction(@NotNull final String path,
                                    @NotNull final Class<T> clazz,
                                    @NotNull UnaryOperator<T> function,
                                    @NotNull final DatabaseCallback<T> callback) {
        readData(path).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                T currentValue = currentData.getValue(clazz);
                currentValue = function.apply(currentValue);
                currentData.setValue(currentValue);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error,
                                   boolean committed,
                                   @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Transaction failed", error.toException());
                    callback.onFailed(error.toException());
                    return;
                }

                T result = currentData != null ? currentData.getValue(clazz) : null;
                callback.onCompleted(result);
            }
        });
    }

    // endregion

    // region User Section

    public String generateUserId() {
        return generateNewId(USERS_PATH);
    }

    public void createNewUser(@NotNull final User user,
                              @Nullable final DatabaseCallback<Void> callback) {
        writeData(USERS_PATH + "/" + user.getId(), user, callback);
    }

    public void getUser(@NotNull final String uid,
                        @NotNull final DatabaseCallback<User> callback) {
        getData(USERS_PATH + "/" + uid, User.class, callback);
    }

    public void getUserList(@NotNull final DatabaseCallback<List<User>> callback) {
        getDataList(USERS_PATH, User.class, callback);
    }

    public void deleteUser(@NotNull final String uid,
                           @Nullable final DatabaseCallback<Void> callback) {
        deleteData(USERS_PATH + "/" + uid, callback);
    }

    public void getUserByEmailAndPassword(@NotNull final String email,
                                          @NotNull final String password,
                                          @NotNull final DatabaseCallback<User> callback) {
        readData(USERS_PATH).orderByChild("email").equalTo(email).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting user by email", task.getException());
                        callback.onFailed(task.getException() != null
                                ? task.getException()
                                : new Exception("Unknown database error"));
                        return;
                    }

                    if (task.getResult() == null || task.getResult().getChildrenCount() == 0) {
                        callback.onFailed(new Exception("User not found"));
                        return;
                    }

                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        User user = dataSnapshot.getValue(User.class);

                        if (user == null || !Objects.equals(user.getPassword(), password)) {
                            callback.onFailed(new Exception("Invalid email or password"));
                            return;
                        }

                        callback.onCompleted(user);
                        return;
                    }

                    callback.onFailed(new Exception("User not found"));
                });
    }

    public void checkIfEmailExists(@NotNull final String email,
                                   @NotNull final DatabaseCallback<Boolean> callback) {
        readData(USERS_PATH).orderByChild("email").equalTo(email).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error checking email", task.getException());
                        callback.onFailed(task.getException() != null
                                ? task.getException()
                                : new Exception("Unknown database error"));
                        return;
                    }

                    boolean exists = task.getResult() != null && task.getResult().getChildrenCount() > 0;
                    callback.onCompleted(exists);
                });
    }

    public void updateUser(@NotNull final User user,
                           @Nullable final DatabaseCallback<Void> callback) {
        runTransaction(USERS_PATH + "/" + user.getId(), User.class, currentUser -> user,
                new DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User object) {
                        if (callback != null) {
                            callback.onCompleted(null);
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        if (callback != null) {
                            callback.onFailed(e);
                        }
                    }
                });
    }

    // endregion User Section

    // region Group Section

    public void createNewGroup(@NotNull final Group group,
                               @Nullable final DatabaseCallback<Void> callback) {
        writeData(GROUPS_PATH + "/" + group.getGroupId(), group, callback);
    }

    public void getGroup(@NotNull final String groupId,
                         @NotNull final DatabaseCallback<Group> callback) {
        getData(GROUPS_PATH + "/" + groupId, Group.class, callback);
    }

    public void getGroupList(@NotNull final DatabaseCallback<List<Group>> callback) {
        getDataList(GROUPS_PATH, Group.class, callback);
    }

    public String generateGroupId() {
        return generateNewId(GROUPS_PATH);
    }

    public void deleteGroup(@NotNull final String groupId,
                            @Nullable final DatabaseCallback<Void> callback) {
        deleteData(GROUPS_PATH + "/" + groupId, callback);
    }

    // endregion Group Section

    // region Question Section

    public void createNewQuestion(@NotNull final Question question,
                                  @Nullable final DatabaseCallback<Void> callback) {
        writeData(QUEST_PATH + "/" + question.getId(), question, callback);
    }

    public void getQuestion(@NotNull final String questionId,
                            @NotNull final DatabaseCallback<Question> callback) {
        getData(QUEST_PATH + "/" + questionId, Question.class, callback);
    }

    public void getQuestionList(@NotNull final DatabaseCallback<List<Question>> callback) {
        getDataList(QUEST_PATH, Question.class, callback);
    }

    public void getUserQuestionList(@NotNull String uid,
                                    @NotNull final DatabaseCallback<List<Question>> callback) {
        getQuestionList(new DatabaseCallback<List<Question>>() {
            @Override
            public void onCompleted(List<Question> questions) {
                questions.removeIf(question -> !Objects.equals(question.getId(), uid));
                callback.onCompleted(questions);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    public String generateQuestionId() {
        return generateNewId(QUEST_PATH);
    }

    public void deleteQuestion(@NotNull final String questionId,
                               @Nullable final DatabaseCallback<Void> callback) {
        deleteData(QUEST_PATH + "/" + questionId, callback);
    }

    // endregion Question Section

    public void LoginUser(@NotNull final String email,
                          @NotNull final String password,
                          @Nullable final DatabaseCallback<String> callback) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");

                        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                            if (callback != null) {
                                callback.onFailed(new Exception("Login succeeded but current user is null"));
                            }
                            return;
                        }

                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        if (callback != null) {
                            callback.onCompleted(uid);
                        }

                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());

                        if (callback != null) {
                            callback.onFailed(task.getException() != null
                                    ? task.getException()
                                    : new Exception("Unknown login error"));
                        }
                    }
                });
    }
}