package com.cowbreedidentifier.cowpoopingapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DisplayResult extends AppCompatActivity {
    private static final String TAG = "BreedApp:Main";
    private static RequestQueue requestQueue;
    private int button_type;
    String base64WebImage;
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);
        requestQueue = Volley.newRequestQueue(this);
        Intent intent = getIntent();
        final String cowURL = intent.getStringExtra(MainActivity.EXTRA_URL);
        button_type = intent.getIntExtra(MainActivity.EXTRA_INT, 0);

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    //Display image on screen
        if (button_type == 0) {
            displayCameraImage(cowURL);
        }
        if (button_type == 1) {
            displayGalleryImage(cowURL);
        }
        if (button_type == 2) {
            displayWebImage(cowURL);
        }


        final Button startOver = (Button) findViewById(R.id.startOver);
        startOver.setVisibility(View.VISIBLE);

        startOver.setOnClickListener(new View.OnClickListener(){
        @Override
            public void onClick(View v){
                Intent intent = new Intent(DisplayResult.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


    }

    Bitmap bitmap;
    void displayCameraImage(String cowURL) {
        try {
            Uri cowURI = Uri.parse(cowURL);
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), cowURI);
            ImageView viewImage = (ImageView) findViewById(R.id.viewImage);
            viewImage.setImageBitmap(bitmap);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            base64WebImage = Base64.encodeToString(byteArray, Base64.NO_WRAP);
            MLTest(base64WebImage);
            final Button startOver = (Button)findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    void displayGalleryImage(String cowURL) {
        try {
            InputStream inputStream;
            Uri cowURI = Uri.parse(cowURL);
            inputStream = getContentResolver().openInputStream(cowURI);
            bitmap = BitmapFactory.decodeStream(inputStream);
            ImageView viewImage = (ImageView)findViewById(R.id.viewImage);
            final Button startOver = (Button) findViewById(R.id.startOver);
            viewImage.setImageBitmap(bitmap);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            base64WebImage = Base64.encodeToString(byteArray, Base64.NO_WRAP);
            MLTest(base64WebImage);
            startOver.setVisibility(View.VISIBLE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("An unexpected error occurred. Please try again");
            final Button startOver = (Button) findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);
        }

    }


    void displayWebImage(String cowURL) {
        try {
            URL myURL = new URL(cowURL);
            HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmapImage = BitmapFactory.decodeStream(input);
            ImageView viewImage = (ImageView) findViewById(R.id.viewImage);
            viewImage.setImageBitmap(bitmapImage);

            //Convert image to Base64 encoding for API call
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            base64WebImage = Base64.encodeToString(byteArray, Base64.NO_WRAP);
            MLTest(base64WebImage);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void MLTest(String base64WebImage){
        try{
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

        JSONObjImage.put("imageBytes", base64WebImage);
        JSONObjInner.put("image", JSONObjImage);
        JSONObjPayload.put("payload", JSONObjInner);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                "https://automl.googleapis.com/v1beta1/projects/animal-identification-app/locations/us-central1/models/ICN7163460716149905373:predict", JSONObjPayload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        apiTestCallDone(response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                //Log.e(TAG, error.toString());
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
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("An unexpected error occurred. Please try again");
            final Button startOver = (Button)findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);
        e.printStackTrace();
    }
}

/*    public void onResult(View view) {
        Intent intent = new Intent(this, DisplayResult.class);
        startActivity(intent);
    }*/
    /**
     * Parse JSON response from model
     * @param response
     */
    void apiTestCallDone(JSONObject response){
        try {
            JSONObject newResponse = response;
            //Log.d(TAG, newResponse.toString(2)); //Test response from API
            JSONArray arrRet = response.getJSONArray("payload");
            JSONObject objRet = (JSONObject) arrRet.get(0);
            String displayName = (String) objRet.get("displayName");
            JSONObject classification = (JSONObject) objRet.getJSONObject("classification");
            double score = (double) classification.get("score");
            //Log.d(TAG, Double.toString(score));

            if (score > 0.50) {
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
                breedMap.put("jersey", "Jersey");
                breedMap.put("simmental", "Simmental");
                breedMap.put("hereford", "Hereford");
                breedMap.put("highland", "Highland");
                breedMap.put("limousin", "Limousin");
                breedMap.put("ayrshire", "Ayrshire");
                breedMap.put("beltedgalloway", "Belted Galloway");
                breedMap.put("brahman", "Brahman");
                breedMap.put("gelbvieh", "Gelbvieh");
                breedMap.put("guernsey", "Guernsey");
                breedMap.put("shorthorn", "Short horn");
                myTextView.setText(breedMap.get(displayName));
                myTextView.setVisibility(View.VISIBLE);
                final Button startOver = (Button) findViewById(R.id.startOver);
                startOver.setVisibility(View.VISIBLE);
            } else {
                //Score is less than 0.50- probably not a cow, only works for URLs currently
                startVisionCall(base64WebImage);

            }

        } catch (JSONException e) {
            e.printStackTrace();
            //Log.d(TAG, "An Unexpected Error occurred");
                startVisionCall(base64WebImage);
                final Button startOver = (Button) findViewById(R.id.startOver);
                startOver.setVisibility(View.VISIBLE);

        }

    }

    /**
     * Calls the generic Google Vision API if the model does not predict image is of a cow
     * @param base64WebImage
     */
    void startVisionCall(final String base64WebImage) {
        try {
            //JSON data needed for Google Vision API
            JSONObject JSONObjOverall = new JSONObject();
            JSONArray JSONArrayOverall = new JSONArray();
            JSONObject JSONObjInner = new JSONObject();
            JSONObject JSONObjImage = new JSONObject();
            JSONArray JSONArrayFeature = new JSONArray();
            JSONObject JSONObjFeature = new JSONObject();
            JSONObjImage.put("content", base64WebImage);


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
                    //Log.e(TAG, error.toString());
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
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("An unexpected error occurred. Please try again");
            final Button startOver = (Button)findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);
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
            //Log.d(TAG, arrRet2.toString(2));

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
            defaultTextView.setText("Non-Cow Detected:");
            final TextView myTextView = (TextView) findViewById(R.id.BreedText);
            myTextView.setVisibility(View.VISIBLE);
            myTextView.setText(listOfDescription.get(0));
            final Button startOver = (Button)findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);

        } catch (JSONException ignored) {
            //Log.d(TAG, "An Unexpected Error occurred");
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("An unexpected error occurred. Please try again");
            final Button startOver = (Button)findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);
        }
    }

    public native String stringFromImage();
    }


