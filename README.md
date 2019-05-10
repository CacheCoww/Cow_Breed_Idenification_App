# Cow_Breed_Idenification_App

## Project Overview
This is an Android App written in Java designed to detect the Cow Breed from an image of a cow using the Google Cloud AutoML Vision API. The photo can be obtained from the phone's camera, photo gallery, or a web URL.

## Creating the Model
The first step is to create a Google Cloud account and enable the AutoML Cloud API. It also requires billing to be enabled. Once that is set up, The GUI was can be used to create the model. Each breed of cow had 50-100 images. The AutoML API recommends at least 100 images per label for accurate results. Most of the images were downloaded from Google using the Fatkun Batch Download Chrome extension. The next step is to train the model, evaluate it, and then use it to predict. After training, it gives you information for using the REST API. A Google service account is required to use the REST API.

Authentication in this app was done using OAuth2 and JSON key file. The key file is stored using in an Android C++ NDK class. API Request was completed using Android Volley POST request. The format of the JSON request can be found on the Google Cloud documentation. If the uploaded image does not seem to be a cow, then the app will display what it most likely is according to the generic Google Vision API, instead of the cow breed.

## Setting up the Code
The main code for this project can be found in [Main Activity](https://github.com/hmsmith5/Cow_Breed_Idenification_App/blob/master/app/src/main/java/com/example/cowpoopingapp/MainActivity.java). The XML code for the layout of the app can be found in the [Layout file](https://github.com/hmsmith5/Cow_Breed_Idenification_App/blob/master/app/src/main/res/layout/activity_main.xml). Most of the layout can be done using the GUI. Other files that may need to be included are the Grade build file and the C++ file with the JSON key.

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
