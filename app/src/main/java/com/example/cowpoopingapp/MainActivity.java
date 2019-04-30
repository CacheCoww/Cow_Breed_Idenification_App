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
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BreedApp:Main";
    private static final int IMAGE_GALLERY_REQUEST = 20;
    private static final int CAMERA_REQUEST = 15;
    private static RequestQueue requestQueue;
    private int button_type;

    static {
        System.loadLibrary("native-lib");
    }

    /**
     * App Startup:
     *  User selects Camera, Gallery, or URL and the relevant text fields will become visible accordingly
     *       button_type: variable to distinguish where photo came from
     *       0: Image from Camera or Gallery
     *       1: Image from Web URL
     */
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
        final Button btnTakePhoto = (Button)findViewById(R.id.btnSelectPhoto2);
        final Button startOver = (Button)findViewById(R.id.startOver);


        //3 Main Button Selections
        final Button button_camera = findViewById(R.id.toggleButtonUpload);
        final Button btnOpenGallery=(Button)findViewById(R.id.btnSelectPhoto);
        final Button button_url = findViewById(R.id.toggleButtonURL);

        //Web URL is selected
        button_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultTextView.setText("Enter Web URL of image below");
                button_camera.setVisibility(View.GONE);
                button_url.setVisibility(View.GONE);
                btnOpenGallery.setVisibility(View.GONE);
                imageURI.setVisibility(View.VISIBLE);
                btnURLEnter.setVisibility(View.VISIBLE);
                button_type = 1;
            }
        });
            btnURLEnter.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String cowURL = imageURI.getText().toString();
                    //cowURL="http://mediad.publicbroadcasting.net/p/shared/npr/styles/x_large/nprshared/201802/582782417.jpg";
                    //cowURL = "https://us.123rf.com/450wm/sublimage/sublimage1702/sublimage170200116/72524962-vertical-oxblood-red-barn-door-boards-and-planks-background-one-red-hinge-.jpg?ver=6";
                    boolean validUrl = URLUtil.isValidUrl(cowURL);
                    if (cowURL == null || validUrl == false) {
                        defaultTextView.setText("Web URL entered is invalid");
                        final Button startOver = (Button)findViewById(R.id.startOver);
                        startOver.setVisibility(View.VISIBLE);
                    } else {
                        btnURLEnter.setVisibility(View.GONE);
                        imageURI.setVisibility(View.GONE);
                        defaultTextView.setVisibility(View.INVISIBLE);
                        autoMLTest(cowURL);
                    }
                }
            });

        //Camera is selected
        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultTextView.setText("Click below to open camera");
                button_url.setVisibility(View.GONE);
                button_camera.setVisibility(View.GONE);
                btnOpenGallery.setVisibility(View.GONE);
                btnTakePhoto.setVisibility(View.VISIBLE);
                button_type = 0;
            }
        });

            btnTakePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    btnTakePhoto.setVisibility(View.GONE);
                }
            });

        //Gallery is selected
        btnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultTextView.setText("Select photo from gallery");
                button_url.setVisibility(View.GONE);
                button_camera.setVisibility(View.GONE);
                btnOpenGallery.setVisibility(View.GONE);
                button_type = 0;
                selectImage();
            }
        });

        //Start Over button to go back to 'main screen'
        startOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_url.setVisibility(View.VISIBLE);
                button_camera.setVisibility(View.VISIBLE);
                btnOpenGallery.setVisibility(View.VISIBLE);
                viewImage.setImageResource(R.drawable.transparentcow);
                breedTextView.setVisibility(View.GONE);
                startOver.setVisibility(View.GONE);
                imageURI.setVisibility(View.GONE);
                btnURLEnter.setVisibility(View.GONE);
                defaultTextView.setText("Select Photo from:");
            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Method to select image from Phones photo gallery
     */
    private void selectImage() {
        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1); Not Needed
        Intent intent = new Intent(Intent.ACTION_PICK);
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);
        intent.setDataAndType(data, "image/*");
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
    }

    //Deal with image after it is selected from Gallery or taken with Camera
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
                    final Button startOver = (Button)findViewById(R.id.startOver);
                    startOver.setVisibility(View.VISIBLE);
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
                    final Button startOver = (Button)findViewById(R.id.startOver);
                    startOver.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }


    /**
     * Method that begins API call to Cloud AutoML Vision Model
     * @param url web URL of image, if applicable.
     */
    private void autoMLTest(String url) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            String base64webimage;
            final String cowURL = url;

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

            String sampleCowImage = stringFromImage();
            byte[] bytez = sampleCowImage.getBytes();
            String goat = new String(Base64.decode(bytez, Base64.DEFAULT));
            InputStream is = new ByteArrayInputStream(goat.getBytes(Charset.forName("UTF-8")));

            GoogleCredentials cowImageRep = GoogleCredentials.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            cowImageRep.refreshIfExpired();
            AccessToken accesstoken = cowImageRep.getAccessToken();
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
                                apiTestCallDone(response, cowURL);

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
            jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }
                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }
                @Override
                public void retry(VolleyError error) throws VolleyError {
                }
            });

                jsonObjectRequest.setShouldCache(false);
                requestQueue.add(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse JSON response from model
     * @param response
     */
    void apiTestCallDone(JSONObject response, String cowURL){
        try {
            JSONObject newResponse = response;
            Log.d(TAG, newResponse.toString(2)); //Test response from API
            JSONArray arrRet = response.getJSONArray("payload");
            JSONObject objRet = (JSONObject) arrRet.get(0);
            String displayName = (String) objRet.get("displayName");
            JSONObject classification = (JSONObject) objRet.getJSONObject("classification");
            double score = (double) classification.get("score");
            Log.d(TAG, Double.toString(score));

            if (score > 0.50 || button_type == 0) {
                final TextView myTextView = (TextView) findViewById(R.id.BreedText);
                final TextView defaultTextView = findViewById(R.id.defaultInstructions);
                defaultTextView.setVisibility(View.VISIBLE);
                defaultTextView.setText("Cow Breed:");
                //Convert model labels to Strings
                Map<String, String> breedMap = new HashMap<>();
                breedMap.put("holstein", "Holstein");
                breedMap.put("texaslonghorn", "Texas Longhorn");
                breedMap.put("charolais", "Charolais");
                breedMap.put("angus", "Angus");
                breedMap.put("redangus", "Red Angus");
                breedMap.put("limousin", "Limousin");
                breedMap.put("simmental", "Simmental");
                breedMap.put("hereford", "Hereford");
                myTextView.setText(breedMap.get(displayName));
                myTextView.setVisibility(View.VISIBLE);
                final Button startOver = (Button) findViewById(R.id.startOver);
                startOver.setVisibility(View.VISIBLE);
            } else {
                //Score is less than 0.50- probably not a cow, only works for URLs currently
                startVisionCall(cowURL);
            }

        } catch (JSONException e) {
            Log.d(TAG, "An Unexpected Error occurred");
            if (button_type == 1) {
                startVisionCall(cowURL);
            } else {
                final TextView defaultTextView = findViewById(R.id.defaultInstructions);
                defaultTextView.setVisibility(View.VISIBLE);
                defaultTextView.setText("An unexpected error occurred. Please try again");
                final Button startOver = (Button) findViewById(R.id.startOver);
                startOver.setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * //API call to the Google Vision API - will serve as backup if Cow Breed Identifier does not identify a cow with high precision
     * Will return the first result of the Google Vision API call
     * @param imageurl
     */
    void startVisionCall(final String imageurl) {
        try {
            //JSON data needed for Google Vision API
            JSONObject JSONObjOverall = new JSONObject();
            JSONArray JSONArrayOverall = new JSONArray();
            JSONObject JSONObjInner = new JSONObject();
            JSONObject JSONObjImage = new JSONObject();
            JSONObject JSONObjImagesrc = new JSONObject();
            JSONArray JSONArrayFeature = new JSONArray();
            JSONObject JSONObjFeature = new JSONObject();

            JSONObjImagesrc.put("imageUri", imageurl);
            JSONObjImage.put("source", JSONObjImagesrc);

            JSONObjFeature.put("type", "LABEL_DETECTION");
            JSONObjFeature.put("maxResults", 9);
            JSONArrayFeature.put(0, JSONObjFeature);
            JSONObjInner.put("image" , JSONObjImage);
            JSONObjInner.put("features" , JSONArrayFeature);
            JSONArrayOverall.put(0, JSONObjInner);

            // final JSON Object to send to API
            JSONObjOverall.put("requests", JSONArrayOverall);

            String sampleCowImage = stringFromImage();
            byte[] bytez = sampleCowImage.getBytes();
            String goat = new String(Base64.decode(bytez, Base64.DEFAULT));
            InputStream is = new ByteArrayInputStream(goat.getBytes(Charset.forName("UTF-8")));

            GoogleCredentials cowImageRep = GoogleCredentials.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            cowImageRep.refreshIfExpired();
            AccessToken accesstoken = cowImageRep.getAccessToken();
            final String token = accesstoken.getTokenValue();
            is.close();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    "https://vision.googleapis.com/v1/images:annotate",
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
            })
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }
                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }
                @Override
                public void retry(VolleyError error) throws VolleyError {
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

            //Log.d(TAG, response.toString(2));
            //Log.d(TAG, listOfDescription.toString());

            //Display result on screen: Current implementation displays top result
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("Cow Breed:");
            final TextView myTextView = (TextView) findViewById(R.id.BreedText);
            myTextView.setVisibility(View.VISIBLE);
            myTextView.setText(listOfDescription.get(0));
            final Button startOver = (Button)findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);

        } catch (JSONException ignored) {
            Log.d(TAG, "An Unexpected Error occurred");
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("An unexpected error occurred. Please try again");
            final Button startOver = (Button)findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);
        }
    }

    public native String stringFromImage();


}

//TODO :Error handling
//Invalid URL-done
//API Does not return result-done
//Empty Photo gallery
//Cant access camera/gallery

//TODO: Vision Call for Camera/Gallery