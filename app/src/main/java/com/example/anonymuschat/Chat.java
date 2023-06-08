package com.example.anonymuschat;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class Chat extends AppCompatActivity {

   // private ArrayAdapter<String> adapter;
    FloatingActionButton fab;
    EditText editText;

    RecyclerAdapter adapter;


    private  Uri imageUrl;
    RecyclerView massages;
    DatabaseReference database;
    DatabaseReference database2;
    DatabaseReference database3;

    StorageReference storage;

    FloatingActionButton photo;
    ArrayList<String> massagesList;

    Button choosePhotoButton;
    int i;
    String name;
    String key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        fab =findViewById(R.id.fab);
        storage = FirebaseStorage.getInstance().getReference();

        editText = findViewById(R.id.input);
        database = FirebaseDatabase.getInstance().getReference("Rooms");
        massages = findViewById(R.id.recycle_view);

        massagesList = new ArrayList<String>();
        adapter = new RecyclerAdapter(this, massagesList );
        massages.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        massages.setHasFixedSize(true);
        massages.setAdapter(adapter);







        i = getIntent().getIntExtra("room",0);
         if(i==1) {
             database2 = FirebaseDatabase.getInstance().getReference("Rooms").child("room1").child("messages");
             database3 = FirebaseDatabase.getInstance().getReference("Rooms").child("room1").child("Users");
         }
         else if(i==2){
             database2 = FirebaseDatabase.getInstance().getReference("Rooms").child("room2").child("messages");
             database3 = FirebaseDatabase.getInstance().getReference("Rooms").child("room2").child("Users");
         }
         name = getIntent().getStringExtra("user");
         key = getIntent().getStringExtra("key");
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(i==1){
                database.child("room1").child("messages")
                        .push()
                        .setValue( name+": "+ editText.getText().toString());

                    getMessages();
                }
                else if(i==2){
                    database.child("room2").child("messages")
                            .push()
                            .setValue(name+": "+editText.getText().toString());

                    getMessages();
                }
                editText.setText("");
            }
        });

    }
    private void getMessages(){
        ValueEventListener chelListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                massagesList.removeAll(massagesList);
                if(i == 1){
                for(DataSnapshot ds: dataSnapshot.getChildren()) {

                        massagesList.add(ds.getValue(String.class));
                }
                }else if(i==2) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        massagesList.add(ds.getValue(String.class));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error",databaseError.getMessage());
            }
        };
        if(i==1) {
            database2.addValueEventListener(chelListener);
        }
        else if(i==2){
            database2.addValueEventListener(chelListener);
        }
    }
    public void deleteUserOrRoom(){

        database3.child(key).removeValue();
        if(i==1) {
           database.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   if (!snapshot.child("room1").hasChild("Users")) database.child("room1").removeValue();
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });

        }
        else if(i==2){
            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.child("room2").hasChild("Users")) database.child("room2").removeValue();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    public void openFileManager(View view){
        Intent in = new Intent();
        in.setType("image/*");
        in.setAction(Intent.ACTION_GET_CONTENT);
        mainActivityResultLauncher.launch(in);
    }



    ActivityResultLauncher<Intent> mainActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK && result.getData() != null){
                        imageUrl = result.getData().getData();
                        System.out.println("Выбрана картинка: " + imageUrl.toString());
                        UploadFile();
                    }
                }
            }
    );

    private void UploadFile(){
        String fileName = System.currentTimeMillis() + "." + getFileExtencion(imageUrl);
        StorageReference fileRefence = storage.child(fileName);

        fileRefence.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if(i==1){
                    database.child("room1").child("messages")
                            .push()
                            .setValue("images/"+fileName);

                    getMessages();
                }else if(i==2){
                    database.child("room2").child("messages")
                            .push()
                            .setValue("images/"+fileName);

                    getMessages();
                }
            }
        });


    }

    private String getFileExtencion(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType((resolver.getType(uri)));
    }

    @Override
    public void finish() {
        deleteUserOrRoom();

        super.finish();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);

    }
}

