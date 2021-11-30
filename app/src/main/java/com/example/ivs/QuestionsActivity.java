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

public class QuestionsActivity extends AppCompatActivity {
    Button save;
    Spinner s1,s2,s3,s4;
    EditText q1,q2,q3,q4;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    Map<String,String> quesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        save = findViewById(R.id.pNext);

        s1=findViewById(R.id.spinner1);
        s2 = findViewById(R.id.spinner2);
        s3=findViewById(R.id.spinner3);
        s4 = findViewById(R.id.spinner4);

        q1 = findViewById(R.id.q1);
        q2 = findViewById(R.id.q2);
        q3 = findViewById(R.id.q3);
        q4 = findViewById(R.id.q4);

        HashMap<String,String> qDecode = new HashMap<String, String>();


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        String[] questions =  getResources().getStringArray(R.array.questions);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(QuestionsActivity.this,
                android.R.layout.simple_list_item_1, questions);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        s1.setAdapter(adapter);
        s2.setAdapter(adapter);
        s3.setAdapter(adapter);
        s4.setAdapter(adapter);




        qDecode.put(questions[7],Integer.toBinaryString(29));
        qDecode.put(questions[1],Integer.toBinaryString(37));
        qDecode.put(questions[2],Integer.toBinaryString(113));
        qDecode.put(questions[3],Integer.toBinaryString(59));
        qDecode.put(questions[4],Integer.toBinaryString(71));
        qDecode.put(questions[5],Integer.toBinaryString(143));
        qDecode.put(questions[6],Integer.toBinaryString(83));


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String a1 = s1.getSelectedItem().toString();
                String a2 = s2.getSelectedItem().toString();
                String a3 = s3.getSelectedItem().toString();
                String a4 = s4.getSelectedItem().toString();

                if(!TextUtils.equals(a1,"Choose Your Question") && !TextUtils.equals(a2,"Choose Your Question") && !TextUtils.equals(a3,"Choose Your Question") &&
                        !TextUtils.equals(a4,"Choose Your Question") ){


                    HashMap<String,Integer> hash=new HashMap<String, Integer>();

                    hash.put(a1,1);
                    hash.put(a2,2);
                    hash.put(a3,3);
                    hash.put(a4,4);

                    if(hash.size()==4 && !TextUtils.isEmpty(q1.getText().toString()) && !TextUtils.isEmpty(q2.getText().toString())
                            && !TextUtils.isEmpty(q3.getText().toString())
                            && !TextUtils.isEmpty(q4.getText().toString())){

                        String a = qDecode.get(a1);
                        String b = qDecode.get(a2);
                        String c = qDecode.get(a3);
                        String d = qDecode.get(a4);


                        Toast.makeText(QuestionsActivity.this, encryption(q1.getText().toString().toCharArray()), Toast.LENGTH_SHORT).show();

                        quesMap.put(a,encryption(q1.getText().toString().toCharArray()));
                        quesMap.put(b,encryption(q2.getText().toString().toCharArray()));
                        quesMap.put(c,encryption(q3.getText().toString().toCharArray()));
                        quesMap.put(d,encryption(q4.getText().toString().toCharArray()));



                        firebaseFirestore.collection("Personal_Questions").document(firebaseAuth.getCurrentUser().getUid()).set(quesMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull @NotNull Void unused) {

                                Toast.makeText(QuestionsActivity.this, "Response saved !", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(QuestionsActivity.this,VoteActivity.class);
                                startActivity(intent);

                            }
                        });





                    } else {
//                        Toast.makeText(QuestionsActivity.this, Integer.toString(hash.size()), Toast.LENGTH_SHORT).show();
                        Toast.makeText(QuestionsActivity.this, "Please Select different questions and don't leave answers blank!", Toast.LENGTH_SHORT).show();

                    }


                } else {

                    Toast.makeText(QuestionsActivity.this, "Mandatory to select 4 questions !", Toast.LENGTH_SHORT).show();
                }



            }
        });


    }
    static String encryption(char[] s)
    {
        int l = s.length;
        int b = (int) Math.ceil(Math.sqrt(l));
        int a = (int) Math.floor(Math.sqrt(l));
        String encrypted = "";
        if (b * a < l)
        {
            if (Math.min(b, a) == b)
            {
                b = b + 1;
            }
            else
            {
                a = a + 1;
            }
        }

        char [][]arr = new char[a][b];
        int k = 0;

        for (int j = 0; j < a; j++)
        {
            for (int i = 0; i < b; i++)
            {
                if (k < l)
                {
                    arr[j][i] = s[k];
                }
                k++;
            }
        }

        for (int j = 0; j < b; j++)
        {
            for (int i = 0; i < a; i++)
            {
                encrypted = encrypted +
                        arr[i][j];
            }
        }
        return encrypted;
    }



}