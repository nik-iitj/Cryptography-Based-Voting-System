package com.example.ivs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

public class VoteRoomActivity extends AppCompatActivity {

    Button vote;
    TextView total;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    RadioGroup radioGroup;
    RadioButton radioButton;
    HashMap<String,String>voter = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_room);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        vote = findViewById(R.id.vote);

        String[] questions =  getResources().getStringArray(R.array.questions);

        HashMap<String,String> qDecode = new HashMap<String, String>();
        qDecode.put(questions[7],Integer.toBinaryString(29));
        qDecode.put(questions[1],Integer.toBinaryString(37));
        qDecode.put(questions[2],Integer.toBinaryString(113));
        qDecode.put(questions[3],Integer.toBinaryString(59));
        qDecode.put(questions[4],Integer.toBinaryString(71));
        qDecode.put(questions[5],Integer.toBinaryString(143));
        qDecode.put(questions[6],Integer.toBinaryString(83));

        HashMap<String,String>retrieve = new HashMap<>();
        retrieve.put(Integer.toBinaryString(29),questions[7]);
        retrieve.put(Integer.toBinaryString(37),questions[1]);
        retrieve.put(Integer.toBinaryString(113),questions[2]);
        retrieve.put(Integer.toBinaryString(59),questions[3]);
        retrieve.put(Integer.toBinaryString(71),questions[4]);
        retrieve.put(Integer.toBinaryString(143),questions[5]);
        retrieve.put(Integer.toBinaryString(83),questions[6]);


        firebaseFirestore.collection("B5_Hostel_Secretary").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        vote.setEnabled(false);
                        vote.setText("Thank you :)");
                        vote.setCompoundDrawables(null,null,null,null);

                    }

                }
            }
        });




        total = findViewById(R.id.total);

        firebaseFirestore.collection("B5_Hostel_Secretary").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot value,FirebaseFirestoreException error) {
                int count = value.size();
                total.setText("Total votes until now : " + Integer.toString(count));
            }
        });



        vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] arr = new String[4];
                String[] ansArr = new String[4];
                View view = LayoutInflater.from(VoteRoomActivity.this).inflate(R.layout.alert_layout,null);

                firebaseFirestore.collection("Personal_Questions").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        Map<String, Object> x = task.getResult().getData();
                        int i=0;

                        for(Map.Entry<String,Object> entry : x.entrySet()){

                            arr[i]=entry.getKey();
                            ansArr[i]=decryption(entry.getValue().toString().toCharArray());
//                            Toast.makeText(VoteRoomActivity.this, ansArr[i], Toast.LENGTH_SHORT).show();
                            i++;
                        }
                       TextView q1 = view.findViewById(R.id.ques1);
                        q1.setText(retrieve.get(arr[0]));

                        TextView q2 = view.findViewById(R.id.ques2);
                        q2.setText(retrieve.get(arr[1]));

                        TextView q3 = view.findViewById(R.id.ques3);
                        q3.setText(retrieve.get(arr[2]));

                        TextView q4 = view.findViewById(R.id.ques4);
                        q4.setText(retrieve.get(arr[3]));



                    }
                });

                startDialogue(view,ansArr);



            }
        });




    }



    private void startDialogue(View view, String[] arr) {

        AlertDialog.Builder builder = new AlertDialog.Builder(VoteRoomActivity.this);
        EditText a1 = view.findViewById(R.id.ans1);
        EditText a2 = view.findViewById(R.id.ans2);
        EditText a3 = view.findViewById(R.id.ans3);
        EditText a4 = view.findViewById(R.id.ans4);

        radioGroup = findViewById(R.id.candidates);

        builder.setMessage("Confirm your identity").setView(view).setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String ans1 = a1.getText().toString();
                String ans2 = a2.getText().toString();
                String ans3 = a3.getText().toString();
                String ans4 = a4.getText().toString();

                if(!TextUtils.isEmpty(a1.getText().toString()) && !TextUtils.isEmpty(a2.getText().toString()) && !TextUtils.isEmpty(a3.getText().toString()) && !TextUtils.isEmpty(a4.getText().toString()) ){

//                    Toast.makeText(VoteRoomActivity.this, a1.getText().toString() + " -> " +arr[0], Toast.LENGTH_SHORT).show();
//                    Toast.makeText(VoteRoomActivity.this, a2.getText().toString() + " -> " +arr[1], Toast.LENGTH_SHORT).show();
//                    Toast.makeText(VoteRoomActivity.this, a3.getText().toString() + " -> " +arr[2], Toast.LENGTH_SHORT).show();
//                    Toast.makeText(VoteRoomActivity.this, a4.getText().toString() + " -> " +arr[3], Toast.LENGTH_SHORT).show();

//                    if(!ans1.equals(arr[0].trim()))
//                        Toast.makeText(VoteRoomActivity.this, ans1.getClass().getName(), Toast.LENGTH_SHORT).show();
//
//
////                    if(ans2.equals(arr[1]))
////                        Toast.makeText(VoteRoomActivity.this, "correct1", Toast.LENGTH_SHORT).show();
////                    if(ans3.equals(arr[2]))
////                        Toast.makeText(VoteRoomActivity.this, "correct2", Toast.LENGTH_SHORT).show();
////                    if(ans4.equals(arr[3]))
////                        Toast.makeText(VoteRoomActivity.this, "correct3", Toast.LENGTH_SHORT).show();

                    if(ans1.equals(arr[0].trim()) && ans2.equals(arr[1].trim()) && ans3.equals(arr[2].trim()) && ans4.equals(arr[3].trim())){

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentDateTime = dateFormat.format(new Date());

                        int radioId=radioGroup.getCheckedRadioButtonId();

                        radioButton = findViewById(radioId);

                        String candidate = radioButton.getText().toString();
                        candidate = removeSpaces(candidate);

                        candidate = LowerToUpper(candidate);


                        String initialKey = "";
                        initialKey = initialKey + ans1.charAt(0) + ans2.charAt(0) + ans3.charAt(0) + ans4.charAt(0);


                        String key = getKey(candidate,initialKey);
                        key = LowerToUpper(key);


                        String encodedText = encodeText(candidate,key);



//                        String decodedText = decodeText(encodedText,key);




                        voter.put("Candidate",encodedText);
                        voter.put("Timestamp",currentDateTime);

                        firebaseFirestore.collection("B5_Hostel_Secretary").document(firebaseAuth.getCurrentUser().getUid()).set(voter).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(VoteRoomActivity.this, "Vote registered! Thank you for your time !", Toast.LENGTH_SHORT).show();
                                    vote.setEnabled(false);
                                    vote.setText("Thank you :)");
                                    vote.setCompoundDrawables(null,null,null,null);


                                } else {

                                    Toast.makeText(VoteRoomActivity.this, "Operation Failed! try again later", Toast.LENGTH_SHORT).show();


                                }
                            }
                        });

                    } else {

                        Toast.makeText(VoteRoomActivity.this, "Incorrect", Toast.LENGTH_SHORT).show();
                    }

                } else {

                    Toast.makeText(VoteRoomActivity.this, "Can't leave any answer blank", Toast.LENGTH_SHORT).show();
                }



            }
        }).setNegativeButton("Cancel",null).setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();

    }

    public String removeSpaces(String str){

        str = str.replaceAll("\\s","");

        return str;

    }

    static String LowerToUpper(String s){

        StringBuffer str =new StringBuffer(s);
        for(int i = 0; i < s.length(); i++)
        {
            if(Character.isLowerCase(s.charAt(i)))
            {
                str.setCharAt(i, Character.toUpperCase(s.charAt(i)));
            }
        }
        s = str.toString();
        return s;
    }

    static String decodeText(String cipher_text, String key)
    {
        String orig_text="";

        for (int i = 0 ; i < cipher_text.length() &&
                i < key.length(); i++)
        {

            int x = (cipher_text.charAt(i) -
                    key.charAt(i) + 26) %26;


            x += 'A';
            orig_text+=(char)(x);
        }
        return orig_text;
    }

    static String encodeText(String str, String key)
    {
        String cipher_text="";

        for (int i = 0; i < str.length(); i++)
        {
            // converting in range 0-25
            int x = (str.charAt(i) + key.charAt(i)) %26;

            // convert into alphabets(ASCII)
            x += 'A';

            cipher_text+=(char)(x);
        }
        return cipher_text;
    }

    static String getKey(String str, String key)
    {
        int x = str.length();

        for (int i = 0; ; i++)
        {
            if (x == i)
                i = 0;
            if (key.length() == str.length())
                break;
            key+=(key.charAt(i));
        }
        return key;
    }

    static String decryption(char []s)
    {
        int l = s.length;
        int b = (int) Math.ceil(Math.sqrt(l));
        int a = (int) Math.floor(Math.sqrt(l));
        String decrypted="";

        char [][]arr = new char[b][a];
        int k = 0;

        for (int j = 0; j < b; j++)
        {
            for (int i = 0; i < a; i++)
            {
                if (k < l)
                {
                    arr[j][i] = s[k];
                }
                k++;
            }
        }

        for (int j = 0; j < a; j++)
        {
            for (int i = 0; i < b; i++)
            {
                decrypted = decrypted +
                        arr[i][j];
            }
        }
        return decrypted;
    }


}