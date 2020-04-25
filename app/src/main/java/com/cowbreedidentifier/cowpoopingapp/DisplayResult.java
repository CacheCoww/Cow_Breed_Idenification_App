package com.cowbreedidentifier.cowpoopingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.label.TensorLabel;

public class DisplayResult extends AppCompatActivity {
    private static final String TAG = "BreedApp:Main";
    private static RequestQueue requestQueue;
    private int button_type;
    String base64WebImage;
    String breedresult;

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

        //Get image from main activity and Display image on screen
        //3 functions corresponding to where image came from to run Cow ML Model
        if (button_type == 0) {
            displayCameraImage(cowURL);
        }
        if (button_type == 1) {
            displayGalleryImage(cowURL);
        }
        if (button_type == 2) {
            displayWebImage(cowURL);
        }

        //Start Over button to go back to main screen/activity
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

    //Image
    Bitmap bitmap;

    /**
     *Function that displays Camera image on screen and makes subsequent call to ML API
     * @params cowURL: location of image
     */
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
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("An unexpected error occurred. Please try again");
            final Button startOver = (Button) findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);
        }
    }

    /**
     *Function that displays Camera gallery image on screen and makes subsequent call to ML API
     * @params cowURL: location of image
     */
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

    /**
     *Function that displays image from web URL and makes subsequent call to ML API
     * @params cowURL: location of image
     */
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
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("An unexpected error occurred. Please try again");
            final Button startOver = (Button) findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);
        }

    }

    /** Memory-map the model file in Assets. */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    String getModelPath(){
        return "model.tflite";
    }


    /**
     * Function for performing the cow breed identification using an image in base64 format and the TFLite predication model. If the function fails to identify a cow
     * then the image is routed to the Vision Function call which performs a generic identification of the image.
     * @param base64WebImage
     */
    void MLTest(String base64WebImage){

        //Prepare image for TFLite model
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                        .build();
        TensorImage tImage = new TensorImage(DataType.UINT8);

        TensorBuffer probabilityBuffer =
                TensorBuffer.createFixedSize(new int[]{1, 16}, DataType.UINT8);

        try{
            byte[] decodedString = Base64.decode(base64WebImage, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            tImage.load(decodedByte);
            tImage = imageProcessor.process(tImage);


            MappedByteBuffer tfliteModel
                    = FileUtil.loadMappedFile(this,
                    "model.tflite");
            @Deprecated
            Interpreter tflite = new Interpreter(loadModelFile(this));

            if(null != tflite) {
                tflite.run(tImage.getBuffer(), probabilityBuffer.getBuffer());
            }

        } catch (IOException e){
           // Log.e("tfliteSupport", "Error reading model", e);
        }

        final String ASSOCIATED_AXIS_LABELS = "dict.txt";
        List<String> associatedAxisLabels = null;
        try {
            associatedAxisLabels = FileUtil.loadLabels(this, ASSOCIATED_AXIS_LABELS);
        } catch (IOException e) {
            //Log.e("tfliteSupport", "Error reading label file", e);
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("An unexpected error occurred. Please try again");
            final Button startOver = (Button) findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);
        } catch (Exception e){
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("An unexpected error occurred. Please try again");
            final Button startOver = (Button) findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);
        }

        TensorProcessor probabilityProcessor =
                new TensorProcessor.Builder().add(new NormalizeOp(0, 255)).build();

        if (null != associatedAxisLabels) {
            // Map of labels and their corresponding probability
            TensorLabel labels = new TensorLabel(associatedAxisLabels,
                    probabilityProcessor.process(probabilityBuffer));

            // Create a map to access the result based on label
            Map<String, Float> floatMap = labels.getMapWithFloatValue();

            float maximum = 0;
            String breed = "None";
            for (Map.Entry<String,Float> entry : floatMap.entrySet()){
                float val = entry.getValue();
                if (val > maximum){
                    maximum = val;
                    breed = entry.getKey();
                }
            }
            MLTestResponse(breed, maximum);
           // Log.d("map", floatMap.toString());
        }


        /*catch (Exception e) {
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("An unexpected error occurred. Please try again");
            final Button startOver = (Button)findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);
        e.printStackTrace();
    } */
}

//https://github.com/tensorflow/tensorflow/blob/master/tensorflow/lite/experimental/support/java/README.md
    /**
     * Function that parses JSON response from ML model and returns cow breed as String displayed on screen
     * @param breed score
     */
    void MLTestResponse(String breed, float score){
        try {
            if (score > 0.40 || checkInternetConnectivity() == false) {
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
                if (breedMap.containsKey(breed)){
                    breed = breedMap.get(breed);
                }
                breedresult = breed;
                myTextView.setText(breed);
                myTextView.setVisibility(View.VISIBLE);
                final Button startOver = (Button) findViewById(R.id.startOver);
                startOver.setVisibility(View.VISIBLE);
            } else {
                startVisionCall(base64WebImage);
            }

        } catch (Exception e) {
            e.printStackTrace();
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("An unexpected error occurred. Please try again");
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


    /**
     * Function that returns the response from the generic vision call. The first result is returned to the user and
     * is designated as a 'non-cow breed'
     * @param response
     */
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

    boolean checkInternetConnectivity(){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;
        return connected;
    }

    public native String stringFromImage();
    }


