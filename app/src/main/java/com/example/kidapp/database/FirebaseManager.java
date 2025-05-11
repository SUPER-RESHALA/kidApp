package com.example.kidapp.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.kidapp.log.FileLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class FirebaseManager {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    public static FirebaseManager firebaseManager;

    public FirebaseManager() {
        this.firebaseDatabase =FirebaseDatabase.getInstance();
        this.databaseReference =firebaseDatabase.getReference();
        this.auth = FirebaseAuth.getInstance();
    }

    public static FirebaseManager getInstance(){
        if (firebaseManager==null){
           return new FirebaseManager();
        }
        return  firebaseManager;
    }
    public void signIn(){
        this.auth.signInAnonymously().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                FileLogger.log("signIn", "Auth failed: " + task.getException().getMessage());
            }
        });
    }
    public void linkWithParent(Button connectBtn, EditText codeField, ViewFlipper viewFlipper, Context context, SharedPreferences prefs) {
        String linkCode = codeField.getText().toString().trim();
        if (linkCode.isEmpty()) {
            Toast.makeText(context, "Enter code", Toast.LENGTH_SHORT).show();
            FileLogger.logError("linkWithParent", "linkCode is empty");
            return;
        }

        String childUid = auth.getCurrentUser().getUid();
        DatabaseReference codeRef = databaseReference.child("link_codes").child(linkCode);

        codeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String parentUid = snapshot.child("parent_uid").getValue(String.class);
                    if (parentUid != null) {
                        prefs.edit().putString("parent_uid", parentUid).apply();
                        codeRef.child("child_uid").setValue(childUid);
                        codeRef.removeValue();
                        codeField.setEnabled(false);
                        connectBtn.setEnabled(false);
                        listenForRequests(prefs);
                        FileLogger.log("linkWithParent", "Linked!");
                        viewFlipper.showNext();
                    } else {
                        FileLogger.log("linkWithParent", "Invalid code");
                    }
                } else {
                    FileLogger.logError("linkWithParent", "Code not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                FileLogger.logError("onCanceled", "Error: " + error.getMessage());
            }
        });
    }

    public void listenForRequests(SharedPreferences prefs) {
        String parentUid = prefs.getString("parent_uid", "");
        String childUid = auth.getCurrentUser().getUid();

        DatabaseReference requestsRef = databaseReference
                .child("users")
                .child(parentUid)
                .child("children")
                .child(childUid)
                .child("requests");

        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    String type = requestSnapshot.child("type").getValue(String.class);
                    String status = requestSnapshot.child("status").getValue(String.class);
                    if ("take".equals(type) && "pending".equals(status)) {
                        sendHelloMessage(prefs);
                        requestSnapshot.getRef().child("status").setValue("completed");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                FileLogger.logError("onCanceled", "Error: " + error.getMessage());
            }
        });
    }

    private void sendHelloMessage(SharedPreferences prefs) {
        String parentUid = prefs.getString("parent_uid", "");
        String childUid = auth.getCurrentUser().getUid();

        DatabaseReference messageRef = databaseReference
                .child("users")
                .child(parentUid)
                .child("children")
                .child(childUid)
                .child("messages")
                .push();

        messageRef.child("text").setValue("hello");
        messageRef.child("timestamp").setValue(ServerValue.TIMESTAMP);
        Log.i("sendHelloMessage", "send hello");
    }
}
