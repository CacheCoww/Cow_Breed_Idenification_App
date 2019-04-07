package com.example.cowpoopingapp;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//"ya29.GlvjBiBAyFZJfrRbeZz4qYln57CqQex95tJxSW-rhV__9qemp3hFuWM_CeMdYpYLmSpblnqOCHW\n" +
//        "sjwlHsp9OE3cp4hmmpjrExfghMhYkHeCtwoBmJDC1VJ9fxvqw";


public class MainActivity extends AppCompatActivity {

private static final String accesstoken = "ya29.c.El_kBuMlQ-rCPJLIISVmGszfu3iBRrWLd2oJAkoYGb8NUsGEkl1IrmVeOkadtzH9zIlmy3V-kfarnCAdiHEeWOgMikOo8keb4lIePNQLfxEarRDzFqRODvXkMkMdl49DnA";

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
       // imageURI = findViewById(R.id.enterURL);
        final Button button = findViewById(R.id.button);
        ImageView viewImage;
    /*    Button b;
        b=(Button)findViewById(R.id.btnSelectPhoto);
        viewImage=(ImageView)findViewById(R.id.viewImage);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });*/


        //Toggle buttons:
        final Button button_upload = findViewById(R.id.toggleButton);
        final Button button_url = findViewById(R.id.toggleButton1);

        //Toggle buttons to Show either URL text field or upload field and Enter button on button click
 /*       button_url.setOnClickListener(new View.OnClickListener() {
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
        });*/
//Button to make API call after image upload method is selected
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                autoMLTest("https://cdn1-www.dogtime.com/assets/uploads/2011/01/file_23012_beagle.jpg");
                final TextView myTextView = (TextView) findViewById(R.id.BreedText);
                myTextView.setText("Moo");
                /*
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
                } */
                }

        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
//Method to select image from Phones photo gallery
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
    //Deal with image after it is selected from Gallery
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

    //Testing the newly created AUTOML Model on Google Cloud
    //Creates JSON object from Holstein cow image on the web
    //Attempts to POST to REST API
    private void autoMLTest(String url) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);
            //url = "https://www.iof2020.eu/trials/use-cases/image-thumb__246__auto_b86924d5bcb568ec4e39ada86f2ae768/2.2.png";
            URL myurl = new URL("https://cdn1-www.dogtime.com/assets/uploads/2011/01/file_23012_beagle.jpg");
            HttpURLConnection connection = (HttpURLConnection) myurl.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmapImage = BitmapFactory.decodeStream(input);
            ImageView viewImage;
            viewImage = (ImageView) findViewById(R.id.viewImage);
            viewImage.setImageBitmap(bitmapImage);
            //Uri myUri = Uri.parse(url);
            //InputStream inputStream = getContentResolver().openInputStream(myUri);
            //Bitmap bitmapImage = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64webimage = Base64.encodeToString(byteArray, Base64.NO_WRAP);

            JSONObject JSONObjPayload = new JSONObject();
            JSONObject JSONObjInner = new JSONObject();
            JSONObject JSONObjImage = new JSONObject();
            //JSONObject JSONObjOverall = new JSONObject();

            JSONObjImage.put("imageBytes", base64webimage);
            JSONObjInner.put("image", JSONObjImage);
            JSONObjPayload.put("payload", JSONObjInner);
            //JSONObjOverall.put("request", JSONObjPayload);

            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    "https://automl.googleapis.com/v1beta1/projects/animal-identification-app/locations/us-central1/models/ICN6662254112112983311:predict", JSONObjPayload,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            apiTestCallDone(response);

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(final VolleyError error) {
                    Log.e(TAG, error.toString());
                }
            })

            {


                @Override
                public Map<String, String> getHeaders() throws AuthFailureError{
                    Map<String, String> headers = new HashMap<>();
                    //headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + accesstoken);
                    return headers;
                }

            };

            jsonObjectRequest.setShouldCache(false);
            requestQueue.add(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void apiTestCallDone(JSONObject response){
        try {
            Log.d(TAG, response.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //API call to the Google Vision API using either an image URL from web or uploaded image from camera
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
            //JSONObject JSONBase64img = new JSONObject();

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


    //Response from Google Vision API
    //Returns first match
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

