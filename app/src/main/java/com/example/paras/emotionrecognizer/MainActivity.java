package com.example.paras.emotionrecognizer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.contract.Scores;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button btnTakePic, btnProcess;

    EmotionServiceClient restClient = new EmotionServiceRestClient("4b61b00b998e48b9a7b374edabd657c7");

    int TAKE_PICTURE_CODE = 100, REQUEST_PERMISSION_CODE = 101;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , android.Manifest.permission.INTERNET
            }, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_CODE) {
            Uri selectedImageUri = data.getData();
            InputStream in = null;
            try {
                in = getContentResolver().openInputStream(selectedImageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            mBitmap = BitmapFactory.decodeStream(in);
            imageView.setImageBitmap(mBitmap);
        }

    }

    public void takePicFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, TAKE_PICTURE_CODE);
    }

    /*@Override
    public void onClick(View view) {
        if(view == btnTakePic){
            takePicFromGallery();
        }
        else if(view == btnProcess){
            processImage();
        }
    }*/
    private void initViews() {
        imageView = (ImageView) findViewById(R.id.imageview);
        btnProcess = (Button) findViewById(R.id.btnProcess);
        btnTakePic = (Button) findViewById(R.id.btnTakePic);

        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicFromGallery();
            }
        });
        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processImage();
            }
        });
    }

    private void processImage() {
        //converting image into stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        //to process asyncTask data
        AsyncTask<InputStream, String, List<RecognizeResult>> processAsync = new AsyncTask<InputStream, String, List<RecognizeResult>>() {

            ProgressDialog mdialog = new ProgressDialog(MainActivity.this);

            @Override
            protected void onPreExecute() {
                mdialog.show();
            }

            @Override
            protected void onProgressUpdate(String... values) {
                mdialog.setMessage(values[0]);
            }

            @Override
            protected List<RecognizeResult> doInBackground(InputStream... inputStreams) {
                publishProgress("Please Wait......");
                List<RecognizeResult> result = null;
                try {
                    result = restClient.recognizeImage(inputStreams[0]);
                } catch (EmotionServiceException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<RecognizeResult> recognizeResults) {
                mdialog.dismiss();
                for (RecognizeResult res : recognizeResults) {
                    String status = getEmotion(res);
                    imageView.setImageBitmap(ImageHelper.drawRectOnBitmap1(mBitmap, res.faceRectangle, status));
                }
            }
        };
        processAsync.execute(inputStream);
    }

    private String getEmotion(RecognizeResult res) {
        List<Double> list = new ArrayList<>();
        Scores scores = res.scores;

        list.add(scores.anger);
        list.add(scores.contempt);
        list.add(scores.disgust);
        list.add(scores.fear);
        list.add(scores.happiness);
        list.add(scores.neutral);
        list.add(scores.sadness);
        list.add(scores.surprise);

        Collections.sort(list);

        //get max value from list
        double maxNum = list.get(list.size() - 1);

        if (maxNum == scores.anger) {
        Toast.makeText(MainActivity.this,"Anger is the enemy of non-violence.",Toast.LENGTH_LONG).show();
            return "Anger";
        }
        else if(maxNum == scores.contempt) {
            Toast.makeText(MainActivity.this, "You might feel worthless to one person, but you're priceless to another. Don't ever forget your value.", Toast.LENGTH_LONG).show();
            return "Worthless";
        }
        else if(maxNum == scores.disgust) {
            Toast.makeText(MainActivity.this, "Disgust and resolve are two of the great emotions that lead to change", Toast.LENGTH_LONG).show();
            return "Disgust";
        }
        else if(maxNum == scores.fear) {
            Toast.makeText(MainActivity.this,"Forget everything and run ..or.. Face everything and rise",Toast.LENGTH_LONG).show();
            return "Fear";
        }
        else if (maxNum == scores.happiness) {
            Toast.makeText(MainActivity.this,"Don't let the silly things steal your happiness. KEEP SMILING :)",Toast.LENGTH_LONG).show();
            return "Happiness";
        }
        else if(maxNum == scores.neutral){
        Toast.makeText(MainActivity.this,"Smile because you may not know that you're inspiration for someone.",Toast.LENGTH_LONG).show();
            return "Neutral";
        }
        else if(maxNum == scores.sadness){
            Toast.makeText(MainActivity.this,"When the day becomes darkest is when you'll shine.",Toast.LENGTH_LONG).show();
            return "Sadness";
        }
        else if(maxNum == scores.surprise){
        Toast.makeText(MainActivity.this,"The best things happen unexpectedly.",Toast.LENGTH_LONG).show();
            return "Surprise";
        }
        else{
        Toast.makeText(MainActivity.this,"Wear a smile on your face for a blissful day.",Toast.LENGTH_LONG).show();
            return "Can't detect";
        }
    }
    }