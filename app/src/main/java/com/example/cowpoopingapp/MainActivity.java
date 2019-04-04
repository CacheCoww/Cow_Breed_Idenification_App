package com.example.cowpoopingapp;

import android.util.Base64;
import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import static android.widget.Toast.LENGTH_LONG;

//API Key : AIzaSyBnnTQAZ35okyLxeiHf8b76OfDE_30rEtk
//HeEeEeEyYy SqUiDwArD

public class MainActivity extends AppCompatActivity {

    /** Default logging tag for messages from the main activity. */
    private static final String TAG = "BreedApp:Main";
    private static final int IMAGE_GALLERY_REQUEST = 20;

    /** Request queue for our API requests. */
    private static RequestQueue requestQueue;

    private int button_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);



        //GUI Elements
        final TextView myTextView = findViewById(R.id.BreedText);
        final EditText imageURI;
        imageURI = findViewById(R.id.enterURL);
        final Button button = findViewById(R.id.button);
        ImageView viewImage;
        Button b;
        b=(Button)findViewById(R.id.btnSelectPhoto);
        viewImage=(ImageView)findViewById(R.id.viewImage);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


        //Toggle buttons:
        final Button button_upload = findViewById(R.id.toggleButton);
        final Button button_url = findViewById(R.id.toggleButton1);

        //Show either URL text field or upload field and Enter button on button click
        button_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageURI.setVisibility(View.VISIBLE);
                button.setVisibility(View.VISIBLE);
                button_type = 1;
            }
        });

        button_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setVisibility(View.VISIBLE);
                button_type = 0;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (button_type == 0 && bitmap != null) {
                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        startAPICall(encoded);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // Code here executes on main thread after user presses button
                    startAPICall("https://cdn1-www.dogtime.com/assets/uploads/2011/01/file_23012_beagle.jpg");
                    //Testing if button works:
                    //Editable textTest = imageURI.getText();
                    String testcow = imageURI.getText().toString();
                    myTextView.setText(testcow);
                }
                }

        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void selectImage() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        //Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent intent = new Intent(Intent.ACTION_PICK);
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);
        intent.setDataAndType(data, "image/*");
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST);

    }

    Bitmap bitmap;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                Uri imageUri = data.getData();
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    ImageView viewImage;
                    viewImage=(ImageView)findViewById(R.id.viewImage);
                    viewImage.setImageBitmap(bitmap);
                    Button button;
                    button =(Button)findViewById(R.id.button);
                    button.setVisibility(View.VISIBLE);
                    button_type = 0;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    void startAPICall(final String imageurl) {
        try {
            //JSON data needed for Google Vision API
            JSONObject JSONObjOverall = new JSONObject();
            JSONArray JSONArrayOverall = new JSONArray();
            JSONObject JSONObjInner = new JSONObject();
            JSONObject JSONObjImage = new JSONObject();
            JSONObject JSONObjImagesrc = new JSONObject();
            JSONArray JSONArrayFeature = new JSONArray();
            JSONObject JSONObjFeature = new JSONObject();
            JSONObject JSONBase64img = new JSONObject();

            //Arranging JSON data into proper format
            JSONObjImagesrc.put("imageUri", imageurl);
            //Image content depending on whether from camera or URL
            if (button_type == 0) {
                JSONObjImage.put("content", imageurl);
            } else {
                JSONObjImage.put("source", JSONObjImagesrc);
            }

            JSONObjFeature.put("type", "LABEL_DETECTION");
            JSONObjFeature.put("maxResults", 9);
            JSONArrayFeature.put(0, JSONObjFeature);
            JSONObjInner.put("image" , JSONObjImage);
            JSONObjInner.put("features" , JSONArrayFeature);
            JSONArrayOverall.put(0, JSONObjInner);

            // final JSON Object to send to API
            JSONObjOverall.put("requests", JSONArrayOverall);


            //API POST Request
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    "https://vision.googleapis.com/v1/images:annotate?key=AIzaSyBnnTQAZ35okyLxeiHf8b76OfDE_30rEtk",
                    JSONObjOverall,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                           apiCallDone(response);

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(final VolleyError error) {
                    Log.e(TAG, error.toString());
                }
            });

            jsonObjectRequest.setShouldCache(false);
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    void apiCallDone(JSONObject response) {
        try {
            //Parse JSON Response
            JSONArray arrRet = response.getJSONArray("responses");
            JSONObject objRet = (JSONObject) arrRet.get(0);
            JSONArray arrRet2 = objRet.getJSONArray("labelAnnotations");
            Log.d(TAG, arrRet2.toString(2));

            //Create Array of "description" keywords returned by API
            ArrayList<String> listOfDescription = new ArrayList<>();
            for (int i = 0; i < arrRet2.length(); i++) {
                JSONObject listObject = (JSONObject) arrRet2.get(i);
                listOfDescription.add(listObject.get("description").toString());
            }

            Log.d(TAG, response.toString(2));
            Log.d(TAG, listOfDescription.toString());

            //Display result on screen: Current implementation displays top result
            final TextView myTextView = (TextView) findViewById(R.id.BreedText);
            myTextView.setText("Animal:" + listOfDescription.get(0));

        } catch (JSONException ignored) {
            Log.d(TAG, "Problem parsing JSON Response");
        }
    }

}


//UNUSED CODE SNIPPETS:


        /*
        super.onActivityResult(2, RESULT_OK, intent);
        Uri selectedImage = intent.getData();
        String[] filePath = { MediaStore.Images.Media.DATA };
        Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(filePath[0]);
        String picturePath = c.getString(columnIndex);
        c.close();
        Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
        Log.w("path of image", picturePath+"");
        ImageView viewImage;
        viewImage=(ImageView)findViewById(R.id.viewImage);
        viewImage.setImageBitmap(thumbnail);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
byte[] byteArray = byteArrayOutputStream .toByteArray();
    } */


        /* COW BREED TRAINING in AUTOML
        Cow Breeds to Test for:
        1. Holstein
        2. Jersey
        3. Angus
        4. Hereford
        5. Charolais
        6. Simmental
        7. Ayrshire
        8. Brown Swiss
        9. Texas Longhorn
        10. Brahman





        */


