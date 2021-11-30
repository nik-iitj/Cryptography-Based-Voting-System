package com.example.ivs;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class VoteActivity extends AppCompatActivity {

    MaterialToolbar mainToolbar;
    CardView c1,c2;
    Executor executor;
    private BiometricPrompt biometricPrompt,biometricPrompt2;
    private BiometricPrompt.PromptInfo promptInfo;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    TextView timer1,timer2;
    ImageView pic;

    TextView winner,winVote,rest,congrats;

    private static final long START_TIME_IN_MILLIS=10000;

    private CountDownTimer countDownTimer;
    private boolean mTimerRunning;

    private long mTimeLftInMillis = START_TIME_IN_MILLIS;
    Map<String,String>user_key= new HashMap<>();


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        mainToolbar = (MaterialToolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        timer1 = findViewById(R.id.timer1);
        timer2 = findViewById(R.id.timer2);

        winner = findViewById(R.id.winner);
        winVote = findViewById(R.id.winner_votes);
        rest=findViewById(R.id.rest_participants);
        pic = findViewById(R.id.votePic);

        congrats = findViewById(R.id.congrats);


        startTimer();



        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {

                String username = task.getResult().getString("Name");


                getSupportActionBar().setSubtitle("Do Vote " + username);


            }
        });


        getSupportActionBar().setTitle("Current Positions");

        c1 = findViewById(R.id.card1);
        c2 = findViewById(R.id.card2);

        executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(VoteActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(VoteActivity.this, errString, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(VoteActivity.this, "Authentication succeed", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(VoteActivity.this,VoteRoomActivity.class);
                startActivity(intent);


            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(VoteActivity.this, "Invalid Fingerprint!", Toast.LENGTH_SHORT).show();

            }
        });


        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Use your fingerprint to enter into Voting room")
                .setNegativeButtonText("Use account password")
                .build();






        c1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                biometricPrompt.authenticate(promptInfo);


            }
        });

        c2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(VoteActivity.this,VoteRoomActivity2.class);
                startActivity(intent);

            }
        });






    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(mTimeLftInMillis,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLftInMillis = millisUntilFinished;
                updateCountDownText();

            }

            @Override
            public void onFinish() {

                declareResult();

                final KonfettiView konfettiView = findViewById(R.id.viewKonfetti);
                konfettiView.build()
                        .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                        .addSizes(new Size(12, 5f))
                        .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                        .streamFor(300, 5000L);

            }
        }.start();

    }

    private void updateCountDownText(){

        int minutes = (int)(mTimeLftInMillis/1000)/60;
        int seconds = (int)(mTimeLftInMillis/1000)%60;

        String tlf = String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
        timer1.setText(tlf);
    }



    private void declareResult(){

        pic.setVisibility(View.GONE);

        winner.setVisibility(View.VISIBLE);
        winVote.setVisibility(View.VISIBLE);
        rest.setVisibility(View.VISIBLE);
        congrats.setVisibility(View.VISIBLE);
        getAns();



    }

    private void getAns(){

        firebaseFirestore.collection("Personal_Questions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    for(QueryDocumentSnapshot doc : task.getResult()){

                        String key="";

                        String id = doc.getId();

                        Map<String, Object> x = doc.getData();

                        for(Map.Entry<String,Object> entry : x.entrySet()){
                            String f = entry.getValue().toString();

                            key = key + f.charAt(0);

                        }

                        user_key.put(id,key);

                    }

                }

                getResult(user_key);
            }
        });
//


    }

    private void getResult(Map<String,String>x){


        final int[] v = {0};
        final int[] s = {0};
        final int[] o = { 0 };

        firebaseFirestore.collection("B5_Hostel_Secretary").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()){

                    for(QueryDocumentSnapshot doc : task.getResult()){

                        if(x.get(doc.getId())!=null){

                            String candid = doc.getString("Candidate");

                            String initialKey = x.get(doc.getId());

                            initialKey = LowerToUpper(initialKey);

                            String finalKey = getKey(candid,initialKey);


                            if(decodeText(candid,finalKey).charAt(0)=='O') o[0]++;
                            else if(decodeText(candid,finalKey).charAt(0)=='S') s[0]++;
                            else if(decodeText(candid,finalKey).charAt(0)=='V') v[0]++;




                        }


                    }

                    List<Integer> arr=new ArrayList<Integer>();

                    arr.add(s[0]);
                    arr.add(v[0]);
                    arr.add(o[0]);

                    Collections.sort(arr);

                    if(arr.get(2)==o[0]){

                        winner.setText("1. "+ "Om Solanki");
                        winVote.setText("( "+o[0]+" )");
                        rest.setText("2. Satyam Kumar "+"("+s[0]+" Votes"+")"+"\n\n3. Vivek Pareek "+"("+v[0]+" Votes"+")");

                    } else if(arr.get(2)==s[0]){

                        winner.setText("1. "+ "Satyam Kumar");
                        winVote.setText("( "+s[0]+" )");
                        rest.setText("2. Om Solanki "+"("+o[0]+" Votes"+")"+"\n\n3. Vivek Pareek "+"("+v[0]+" Votes"+")");


                    } else if(arr.get(2)==v[0]){

                        winner.setText("1. "+ "Vivek Pareek");
                        winVote.setText("( "+v[0]+" )");
                        rest.setText("2. Om Solanki "+"("+o[0]+" Votes"+")"+"\n\n3. Satyam Kumar "+"("+s[0]+" Votes"+")");


                    }





                }


            }



        });







    }


    static String decryptAns(char []s)
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


}