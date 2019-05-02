# Cow_Breed_Idenification_App

## Project Overview
This is an Android App designed to detect the Cow Breed from an image of a cow using the Google Cloud AutoML Vision API.The purpose of this app is to allow people to find out what breed a particular cow is. The machine learning model was created using Google Cloud AutoML Vision tools for creating a custom model. The app supports 8 different breeds of cows currently.

## Creating the Model
I created the model by signing up for a Google Cloud account and enabling the AutoML Cloud API. It also required billing to be enabled. Once that was set up, The GUI was used to create the model. Each breed of cow had 50-100 images. The AutoML API recommends at least 100 images per label for accurate results. I got most of the images from google and downloaded them using the Fatkun Batch Download Chrome extension. The next step is to train the model. I trained this particular model for 1 hour since Google gives you one free hour of training time per model. From this, the model is evaluated in terms of accuracy. This model scored 0.93 for accuracy. After training, it also gives you information for using the REST API. A Google service account is required to use the REST API.

Authentication was done using OAuth2 and JSON key file. File is stored using Android C++ NDK class. API Request was completed using Android Volley POST request. App has options to obtain image from camera, gallery, and web URL. 

## Installing the App
Instructions and link for download will be updated here in the future. 

## Screenshots
<img src="https://github.com/hmsmith5/Cow_Breed_Idenification_App/blob/master/image.png?raw=true">   <img src="https://github.com/hmsmith5/Cow_Breed_Idenification_App/blob/master/screenshotcowapp2.PNG?raw=true">

