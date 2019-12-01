package com.cowbreedidentifier.cowpoopingapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Tab1.OnFragmentInteractionListener1} interface
 * to handle interaction events.
 * Use the {@link Tab1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Tab1 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener1 mListener;

    public Tab1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Tab1.
     */
    // TODO: Rename and change types and number of parameters
    public static Tab1 newInstance(String param1, String param2) {
        Tab1 fragment = new Tab1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab1, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener1) {
            mListener = (OnFragmentInteractionListener1) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener1 {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }
}



  /**
     * Method that begins API call to Cloud AutoML Vision Model
     * @param url web URL of image, if applicable.

private void autoMLTest(String url) {
    try {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String base64webimage;
        final String cowURL = url;


        Intent intent = new Intent(this, DisplayResult.class);
        intent.putExtra(EXTRA_URL, cowURL);
        intent.putExtra(EXTRA_INT, button_type);
        startActivity(intent);



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
            imageData = base64webimage;

        } else if (button_type == 0) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            base64webimage = Base64.encodeToString(byteArray, Base64.NO_WRAP);
            imageData = base64webimage;
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
                "https://automl.googleapis.com/v1beta1/projects/animal-identification-app/locations/us-central1/models/ICN7163460716149905373:predict", JSONObjPayload,
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

/*    public void onResult(View view) {
        Intent intent = new Intent(this, DisplayResult.class);
        startActivity(intent);
    }*/
    /**
     * Parse JSON response from model
     * @param response

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
            if (button_type == 1) {
                JSONObjImage.put("source", JSONObjImagesrc);
            } else {
                JSONObjImage.put("content", imageData);
            }

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

    */