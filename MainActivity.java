package com.example.anonimchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText nameEditText;
    private Button loginButton;
    private DatabaseReference roomsDatabaseRef;
    private DatabaseReference room1UsersDatabaseRef;
    private DatabaseReference room2UsersDatabaseRef;

    private static final int ROOM_COUNT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameEditText = findViewById(R.id.editTextName);
        loginButton = findViewById(R.id.logIn);
        roomsDatabaseRef = FirebaseDatabase.getInstance().getReference("Rooms");
        room1UsersDatabaseRef = roomsDatabaseRef.child("room1").child("Users");
        room2UsersDatabaseRef = roomsDatabaseRef.child("room2").child("Users");

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int roomNumber = new Random().nextInt(ROOM_COUNT) + 1;
                String userName = nameEditText.getText().toString();
                String userKey = null;
                DatabaseReference roomUsersDatabaseRef = null;
                switch (roomNumber) {
                    case 1:
                        userKey = addUserToRoom(room1UsersDatabaseRef, userName);
                        roomUsersDatabaseRef = room1UsersDatabaseRef;
                        break;
                    case 2:
                        userKey = addUserToRoom(room2UsersDatabaseRef, userName);
                        roomUsersDatabaseRef = room2UsersDatabaseRef;
                        break;
                }
                startChat(roomNumber, userName, userKey);
            }
        });
    }

    private String addUserToRoom(DatabaseReference roomUsersDatabaseRef, String userName) {
        String userKey = roomUsersDatabaseRef.push().getKey();
        roomUsersDatabaseRef.child(userKey).setValue(userName);
        return userKey;
    }

    private void startChat(int roomNumber, String userName, String userKey) {
        Intent chatIntent = new Intent(MainActivity.this, Chat.class);
        chatIntent.putExtra("room", roomNumber);
        chatIntent.putExtra("user", userName);
        chatIntent.putExtra("key", userKey);
        startActivity(chatIntent);
    }
}