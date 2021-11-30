package com.example.ivs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

    Button next;
    ImageView id_card;
    int height,width;
    Bitmap bitmap;
    InputImage image;
    Uri finalUri;

    EditText sName,sBranch,sDOB,sRoll;

    StorageReference storageReference;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    Map<String,String> dataMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        next = findViewById(R.id.next);

        id_card = findViewById(R.id.id_card);

        sName=findViewById(R.id.txtName);
        sBranch=findViewById(R.id.branch);
        sRoll=findViewById(R.id.id);
        sDOB=findViewById(R.id.dob);

        height = -1;
        width=-1;

        byte[] byteArray = getIntent().getByteArrayExtra("bytes");
        Bundle data = getIntent().getExtras();

        if(data!=null){
            height = data.getInt("height");
            width = data.getInt("width");
        }

        if(height!=-1){

            Bitmap k = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            bitmap = Bitmap.createScaledBitmap(k,width,height,false);

            id_card.setImageDrawable(null);

            id_card.setImageBitmap(bitmap);



            image=InputImage.fromBitmap(bitmap,0);

            executeClassification();


        }







        id_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent,"Select your ID card Image"),100);

                Intent intent = new Intent(ProfileSetupActivity.this,FromCamDetect.class);
                startActivity(intent);

            }
        });


        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();


        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    if(task.getResult().exists()){

                        Intent intent = new Intent(ProfileSetupActivity.this,VoteActivity.class);
                        startActivity(intent);

                    }



                } else{

                    Toast.makeText(ProfileSetupActivity.this, "Retrieval Failed", Toast.LENGTH_SHORT).show();
                }

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(sName.getText().toString()) && !TextUtils.isEmpty(sBranch.getText().toString())
                && !TextUtils.isEmpty(sDOB.getText().toString()) && !TextUtils.isEmpty(sRoll.getText().toString())){

                    String user_id = firebaseAuth.getCurrentUser().getUid();

                    StorageReference path = storageReference.child("ID_Images").child(user_id+".jpg");

                    if(bitmap!=null){
                        Uri FUri =getImageUri(ProfileSetupActivity.this,bitmap);
                        path.putFile(FUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(@NonNull @NotNull UploadTask.TaskSnapshot taskSnapshot) {
                                path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(@NonNull @NotNull Uri uri) {

                                        finalUri = uri;
                                        dataMap.put("ID_image",finalUri.toString());
                                        dataMap.put("Name",sName.getText().toString());
                                        dataMap.put("Roll",sRoll.getText().toString());
                                        dataMap.put("DOB",sDOB.getText().toString());
                                        dataMap.put("Branch",sBranch.getText().toString());

                                        setData(dataMap);

//                                     Toast.makeText(ProfileSetupActivity.this, finalUri.toString(), Toast.LENGTH_SHORT).show();


                                    }
                                });
                            }
                        });
                    } else{

                        dataMap.put("Name",sName.getText().toString());
                        dataMap.put("Roll",sRoll.getText().toString());
                        dataMap.put("DOB",sDOB.getText().toString());
                        dataMap.put("Branch",sBranch.getText().toString());
                        dataMap.put("ID_image","No image !");

                        setData(dataMap);

                    }


                } else{

                    Toast.makeText(ProfileSetupActivity.this, "Can't leave any field blank !", Toast.LENGTH_SHORT).show();

                }





            }
        });





    }

    public void setData(Map<String,String>h){

        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(h).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull @NotNull Void unused) {
                Toast.makeText(ProfileSetupActivity.this, "Profile setup Completed", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileSetupActivity.this,ResidenceActivity.class);
                startActivity(intent);
            }
        });



    }



    public void executeClassification(){

        final int[] i = {0};
        final String[] checker = new String[1];
        final String[] name = new String[1];
        final String[] id = new String[1];
        final String[] branch = new String[1];
        final String[] dob = new String[1];

        final String[] test = {""};

        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Task<Text> result=recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(@NonNull @NotNull Text text) {
                String resultText = text.getText();

                if(!resultText.isEmpty()){

                    for(Text.TextBlock block : text.getTextBlocks()){
                        test[0] = test[0] + block.getText();

                        String blockText = block.getText();

                        if(i[0]==0){
                            checker[0] = block.getText();
                        }

                        if(i[0] ==1){
                            name[0] = block.getText();
                        }
                        if(i[0] ==3){
                            id[0] =block.getText();
                        }

                        if(i[0] ==4){
                            dob[0] =block.getText();
                        }

                        if(i[0] ==5){
                            branch[0] =block.getText();
                        }

                        i[0] = i[0] +1;








                    }



                    if(checker[0].equals("Indian Institute of Technology Jodhpur")|| dob[0].isEmpty() || id[0].isEmpty() || name[0].isEmpty() || branch[0].isEmpty()){

                        if(branch[0].equals("Card Holder") ){

                            Toast.makeText(ProfileSetupActivity.this, "Try rescanning with little bit of more zoom", Toast.LENGTH_SHORT).show();

                        } else{
                            String fDob="",fRoll="";

                            if(dob[0].length()<15){
                                fDob = dob[0];
                            } else {
                                fDob=dob[0].substring(15);
                            }


                            if(id[0].length()<13){
                                fRoll = id[0];
                            } else {
                                fRoll=id[0].substring(13);
                            }




                            sName.setText(name[0]);
                            sDOB.setText(fDob);
                            sBranch.setText(branch[0]);
                            sRoll.setText(fRoll);
                        }



                    } else{

                        Toast.makeText(ProfileSetupActivity.this, "IITJ ID card not detected! try again", Toast.LENGTH_SHORT).show();
                    }





//                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSetupActivity.this);
////
////                    String message = "Classified text are : \nName : " + name[0] +"\nDOB : " + fDob
////                            + "\nbranch : " + branch[0] + "\nRoll : " + fRoll +"\n Would you like to add these automatically or edit?";
////
////                    String finalFDob = fDob;
////                    String finalFRoll = fRoll;
//                    builder.setMessage(test[0]).setPositiveButton("Add", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
////                            sName.setText(name[0]);
////                            sDOB.setText(finalFDob);
////                            sBranch.setText(branch[0]);
////                            sRoll.setText(finalFRoll);
//                            Toast.makeText(ProfileSetupActivity.this, "Okk", Toast.LENGTH_SHORT).show();
//
//                        }
//                    }).setNegativeButton("Edit",null);
//
//                    AlertDialog alert = builder.create();
//                    alert.show();




                } else{

                    Toast.makeText(ProfileSetupActivity.this, "No Text Recognized \n Try Again", Toast.LENGTH_SHORT).show();


                }

            }
        });







    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}