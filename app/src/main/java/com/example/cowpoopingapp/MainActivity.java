package com.example.cowpoopingapp;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
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

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BreedApp:Main";
    private static final int IMAGE_GALLERY_REQUEST = 20;
    private static final int CAMERA_REQUEST = 15;
    private static RequestQueue requestQueue;
    private int button_type;  //button type depending on if image was uploaded or from URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        //GUI Elements
        final TextView defaultTextView = findViewById(R.id.defaultInstructions);
        final TextView breedTextView = findViewById(R.id.BreedText);
        final EditText imageURI = findViewById(R.id.enterURL);
        final Button btnURLEnter = findViewById(R.id.buttonURLEnter);
        final ImageView viewImage=(ImageView)findViewById(R.id.viewImage);
        final Button btnOpenGallery=(Button)findViewById(R.id.btnSelectPhoto);
        final Button btnTakePhoto = (Button)findViewById(R.id.btnSelectPhoto2);

        /**
         * User selects Upload or URL and the relevant text fields will become visible accordingly
         * The button_type will be used in later functions to determine if user entered URL or uploaded image
         * 0: Image from Gallery
         * 1: Image from Web URL
         **/

        //Toggle buttons:
        final Button button_upload = findViewById(R.id.toggleButtonUpload);
        final Button button_url = findViewById(R.id.toggleButtonURL);

        button_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_upload.setVisibility(View.GONE);
                button_url.setVisibility(View.GONE);
                imageURI.setVisibility(View.VISIBLE);
                btnURLEnter.setVisibility(View.VISIBLE);
                button_type = 1;
            }
        });

        button_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_url.setVisibility(View.GONE);
                button_upload.setVisibility(View.GONE);
                btnOpenGallery.setVisibility(View.VISIBLE);
                btnTakePhoto.setVisibility(View.VISIBLE);
                button_type = 0;
            }
        });

        //Select image from Gallery
        btnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        //Take photo using phone
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        //Select image from URL
        btnURLEnter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String cowURL = imageURI.getText().toString();
                if (cowURL == null) {
                    //Handle invalid input
                } else {
                    autoMLTest(cowURL);
                }
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
                    ImageView viewImage = (ImageView)findViewById(R.id.viewImage);
                    final Button btnOpenGallery=(Button)findViewById(R.id.btnSelectPhoto);
                    final Button btnTakePhoto = (Button)findViewById(R.id.btnSelectPhoto2);
                    btnOpenGallery.setVisibility(View.GONE);
                    btnTakePhoto.setVisibility(View.GONE);
                    viewImage.setImageBitmap(bitmap);
                    autoMLTest("url");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == CAMERA_REQUEST) {
                try {
                    bitmap = (Bitmap) data.getExtras().get("data");
                    ImageView viewImage = (ImageView) findViewById(R.id.viewImage);
                    final Button btnOpenGallery = (Button) findViewById(R.id.btnSelectPhoto);
                    final Button btnTakePhoto = (Button) findViewById(R.id.btnSelectPhoto2);
                    btnOpenGallery.setVisibility(View.GONE);
                    btnTakePhoto.setVisibility(View.GONE);
                    viewImage.setImageBitmap(bitmap);
                    autoMLTest("camera");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private void autoMLTest(String url) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            String base64webimage;

            //Sets the base64 string depending on whether photo comes from web or phone
            if (button_type == 1) {
                URL myurl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) myurl.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmapImage = BitmapFactory.decodeStream(input);
                ImageView viewImage = (ImageView) findViewById(R.id.viewImage);

                //Display image on screen
                final EditText imageURI = findViewById(R.id.enterURL);
                final Button btnURLEnter = findViewById(R.id.buttonURLEnter);
                final Button btnOpenGallery=(Button)findViewById(R.id.btnSelectPhoto);
                imageURI.setVisibility(View.GONE);
                btnURLEnter.setVisibility(View.GONE);
                btnOpenGallery.setVisibility(View.GONE);
                viewImage.setImageBitmap(bitmapImage);

                //Convert image to Base64 encoding for API call
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                base64webimage = Base64.encodeToString(byteArray, Base64.NO_WRAP);

            } else if (button_type == 0) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                base64webimage = Base64.encodeToString(byteArray, Base64.NO_WRAP);

            } else {
                base64webimage = null;
            }

            //Get Service Account Token
            InputStream is = getAssets().open("animal-identification-app-61411926d77b.json");
            GoogleCredentials credentials = GoogleCredentials.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            credentials.refreshIfExpired();
            AccessToken accesstoken = credentials.getAccessToken();
            final String token = accesstoken.getTokenValue();
            is.close();

                //Set up JSON Object Payload to send to API
                JSONObject JSONObjPayload = new JSONObject();
                JSONObject JSONObjInner = new JSONObject();
                JSONObject JSONObjImage = new JSONObject();

                JSONObjImage.put("imageBytes", base64webimage);
                JSONObjInner.put("image", JSONObjImage);
                JSONObjPayload.put("payload", JSONObjInner);

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
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + token);
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
            JSONObject newResponse = response;
            Log.d(TAG, newResponse.toString(2)); //Test response from API
            JSONArray arrRet = response.getJSONArray("payload");
            JSONObject objRet = (JSONObject) arrRet.get(0);
            String displayName = (String) objRet.get("displayName");
            Log.d(TAG, displayName);
            final TextView myTextView = (TextView) findViewById(R.id.BreedText);
            myTextView.setText("Cow Breed: " + displayName);
            myTextView.setVisibility(View.VISIBLE);

        } catch (JSONException ignored) {
            Log.d(TAG, "An Unexpected Error occurred");
        }

    }

    /**
     * //API call to the Google Vision API - will serve as backup if Cow Breed Identifier does not identify a
     * Will return the first result of the Google Vision API call
     * @param imageurl
     */
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








//Useful Links
//JWT for authorizing app:
//https://developers.google.com/identity/protocols/OAuth2ServiceAccount#jwt-auth