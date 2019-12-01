package com.cowbreedidentifier.cowpoopingapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "BreedApp:Main";
    private static final int IMAGE_GALLERY_REQUEST = 20;
    private static final int CAMERA_REQUEST = 15;
    public static final String EXTRA_URL = "com.cowbreedidentifier.cowpoopingapp.URL";
    public static final String EXTRA_INT = "com.cowbreedidentifier.cowpoopingapp.INT";
    public static final String REQUEST_CODE = "com.cowbreedidentifier.cowpoopingapp.CODE";
    private int button_type;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static {
        System.loadLibrary("native-lib");
    }

    /**
     *       button_type: variable to distinguish where photo came from
     *       0: Image from Camera
     *       1: Image from Gallery
     *       2: Image from Web URL
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);


        //GUI Elements
        final TextView defaultTextView = findViewById(R.id.defaultInstructions);
        final TextView breedTextView = findViewById(R.id.BreedText);
        final EditText imageURI = findViewById(R.id.enterURL);
        final Button btnURLEnter = findViewById(R.id.buttonURLEnter);
        final ImageView viewImage=(ImageView)findViewById(R.id.viewImage);
        final Button btnTakePhoto = (Button)findViewById(R.id.btnSelectPhoto2);
        final Button startOver = (Button)findViewById(R.id.startOver);

        //3 Main Button Selections
        final Button button_camera = (Button)findViewById(R.id.toggleButtonUpload);
        final Button btnOpenGallery=(Button)findViewById(R.id.btnSelectPhoto);
        final Button button_url = (Button)findViewById(R.id.toggleButtonURL);

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
                button_type = 2;
                final Button startOver = (Button)findViewById(R.id.startOver);
                startOver.setVisibility(View.VISIBLE);
            }
        });
            btnURLEnter.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String cowURL = imageURI.getText().toString();
                    //cowURL="http://mediad.publicbroadcasting.net/p/shared/npr/styles/x_large/nprshared/201802/582782417.jpg";
                    //cowURL = "https://us.123rf.com/450wm/sublimage/sublimage1702/sublimage170200116/72524962-vertical-oxblood-red-barn-door-boards-and-planks-background-one-red-hinge-.jpg?ver=6";
                    boolean validUrl = URLUtil.isValidUrl(cowURL);
                    //validUrl = true;
                    if (cowURL == null || validUrl == false) {
                        defaultTextView.setText("Web URL entered is invalid");
                    } else {
                        btnURLEnter.setVisibility(View.GONE);
                        imageURI.setVisibility(View.GONE);
                        defaultTextView.setVisibility(View.INVISIBLE);
                        //cowURL = "https://upload.wikimedia.org/wikipedia/commons/0/0c/Cow_female_black_white.jpg";
                        final Button startOver = (Button)findViewById(R.id.startOver);
                        startOver.setVisibility(View.INVISIBLE);
                        startNewActivity(cowURL);
                    }
                }
            });

        //Camera is selected
        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_url.setVisibility(View.GONE);
                button_camera.setVisibility(View.GONE);
                btnOpenGallery.setVisibility(View.GONE);
                btnTakePhoto.setVisibility(View.GONE);
                button_type = 0;
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
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
                button_type = 1;
                final Button startOver = (Button) findViewById(R.id.startOver);
                startOver.setVisibility(View.VISIBLE);
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

    //Start activity for Web URL
    private void startNewActivity(String cowURL) {
        Intent intent = new Intent(this, DisplayResult.class);
        intent.putExtra(EXTRA_URL, cowURL);
        intent.putExtra(EXTRA_INT, button_type);
        intent.putExtra(REQUEST_CODE, "url");
        startActivity(intent);
    }

 //Select image from Gallery
    private void selectImage() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String pictureDirectoryPath = pictureDirectory.getPath();
            Uri data = Uri.parse(pictureDirectoryPath);
            intent.setDataAndType(data, "image/*");
            startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
        } catch (Exception e) {
            final TextView defaultTextView = findViewById(R.id.defaultInstructions);
            defaultTextView.setVisibility(View.VISIBLE);
            defaultTextView.setText("An unexpected error occurred. Please try again");
            final Button startOver = (Button) findViewById(R.id.startOver);
            startOver.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    //Deal with image after it is selected from Gallery or taken with Camera
    Bitmap bitmap;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                Uri imageUri = data.getData();
                String imageUriString = imageUri.toString();
                Intent intent = new Intent(this, DisplayResult.class);
                intent.putExtra(EXTRA_URL, imageUriString);
                intent.putExtra(EXTRA_INT, button_type);
                intent.putExtra(REQUEST_CODE, "gallery");
                startActivity(intent);
            }
            if (requestCode == CAMERA_REQUEST) {
                try {
                    bitmap = (Bitmap) data.getExtras().get("data");
                    File tempDir= Environment.getExternalStorageDirectory();
                    tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
                    tempDir.mkdir();
                    String title = "cowImage";
                    File tempFile = File.createTempFile(title, ".jpg", tempDir);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    byte[] bitmapData = bytes.toByteArray();

                    //write the bytes in file
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(bitmapData);
                    fos.flush();
                    fos.close();
                    Uri moo = Uri.fromFile(tempFile);
                    String moocow = moo.toString();

                    Intent intent = new Intent(this, DisplayResult.class);
                    intent.putExtra(EXTRA_URL, moocow);
                    intent.putExtra(EXTRA_INT, button_type);
                    intent.putExtra(REQUEST_CODE, "gallery");
                    startActivity(intent);
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

        }
    }


}

