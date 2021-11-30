package com.example.ivs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ResidenceActivity extends AppCompatActivity {

    Button next;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    EditText hRoom,wName,cNum;
    Spinner hName;
    Map<String,String> resMap = new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_residence);

        next = findViewById(R.id.resNext);

        hName=findViewById(R.id.hostelName);
        hRoom = findViewById(R.id.roomNo);
        wName=findViewById(R.id.warden);
        cNum = findViewById(R.id.contact);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(ResidenceActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.hostels));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hName.setAdapter(adapter);

        String h = hName.getSelectedItem().toString();
        Toast.makeText(this, h, Toast.LENGTH_SHORT).show();


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(hRoom.getText().toString())
                && !TextUtils.isEmpty(wName.getText().toString()) && !TextUtils.isEmpty(cNum.getText().toString()) && !TextUtils.equals(hName.getSelectedItem().toString(),"Choose Your Hostel")){


                    firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).update("Hostel",hName.getSelectedItem().toString());
                    firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).update("Room",hRoom.getText().toString());
                    firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).update("Warden",wName.getText().toString());
                    firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).update("Contact",cNum.getText().toString());

                    Intent intent = new Intent(ResidenceActivity.this,QuestionsActivity.class);
                    startActivity(intent);


                } else{

                    Toast.makeText(ResidenceActivity.this, "Can't leave any field blank", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }
}