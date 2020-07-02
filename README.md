# Cow_Breed_Idenification_App

## Project Overview
This is an Android App written in Java designed to detect the Cow Breed from an image of a cow using the Google Cloud AutoML Vision API (now Tensorflow Lite Edge Model, see Branch). The photo can be obtained from the phone's camera, photo gallery, or a web URL. If the photo does not score high enough for any of the cow breeds in the machine learning algorithm (possibly because it is not a cow), then the app tries to identify what else the photo could be.

Note: Google's AutoML Beta API support ended on February 20, 2020. The pricing for the new AutoML release is based on node hours instead of number of requests so the app has moved to an Tensorflow Lite Edge model (see Branch for code).

<img src="https://github.com/hmsmith5/Cow_Breed_Idenification_App/blob/master/featurelogocowapp4.PNG?raw=true">

## Creating the Model
The first step is to create a Google Cloud account and enable the AutoML Cloud API. It also requires billing to be enabled. Once that is set up, The GUI can be used to create the model. Each breed of cow had 50-100 images. The AutoML API recommends at least 100 images per label for accurate results. Most of the images were downloaded from Google using the Fatkun Batch Download Chrome extension. The next step is to train the model, evaluate it, and then use it to predict. After training, it gives you statistics on how well the model performs and information for using the REST API. A Google service account is required to use the REST API. The accuracy for the Tensorflow Lite model is 0.89 vs 0.90 for the Cloud model.

## Authentication
Authentication in this app was done using OAuth2 and JSON key file. The key file is stored using in an Android C++ NDK class (Not shown in project). API Request was completed using Android Volley POST request. The format of the JSON request can be found on the Google Cloud documentation. If the uploaded image does not seem to be a cow, then the app will call the Google Vision API which will send a generic identification of the image which will be displayed to the user. 

No authentication was needed for TFLite model.

## Setting up the Code
The main code for this project can be found in Main Activity and Activity Results. The XML code for the layout of the app can be found in the [Layout file](https://github.com/hmsmith5/Cow_Breed_Idenification_App/blob/master/app/src/main/res/layout/activity_main.xml). Most of the layout can be done using the GUI. The background for the app was created in Microsoft Word. The logo was created using cooltext.com. 

Update: The app now uses a TFLite Edge model (see branch) instead of the AutoML API. For using the online Cloud AutoML API, see the code in Main.java and ActivityResult.java. For information on using the TFLite model, see the Branch. For this, the TFLite model was downloaded from Google Cloud AutoML tool using gcloud CLI. The TFLite model and list of labels can be directly added to Android Studio Assets folder. The model was accessed using the Tensorflow support library for Tensorflow Lite.

The old API request was performed using a POST request according the the Google AutoML guidelines. The response is parsed for the cow breed and the result is returned to the user using a map to correct formatting.

## Installing the App
The beta version of the app will be released on the App Store shortly

## Screenshots
<img src="https://github.com/hmsmith5/Cow_Breed_Idenification_App/blob/master/image.png?raw=true">   <img src="https://github.com/hmsmith5/Cow_Breed_Idenification_App/blob/master/screenshotcowapp2.PNG?raw=true">

## About the Model
The Machine Learning model was created using images of cows. It currently supports 8 breeds of cows, with plans to add more. The cows included are:
1. Holstein
2. Angus
3. Jersey
4. Hereford
5. Red Angus
6. Charolais
7. Simmental
8. Texas Longhorn
9. Highland
10. Limousin
11. Ayrshire
12. Belted Galloway
13. Brahman
14. Gelbvieh
15. Guernsey
16. Shorthorn
                
## Future Updates
1. Restructure project code to better organize app components

2. Updates to include the app in different languages and settings

3. Option to post results on Facebook and other social media platforms.
