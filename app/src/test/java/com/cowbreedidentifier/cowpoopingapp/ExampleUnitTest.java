package com.cowbreedidentifier.cowpoopingapp;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}



//UNUSED CODE SNIPPETS:
        /*
        super.onActivityResult(2, RESULT_OK, intent);
        Uri selectedImage = intent.getData();
        String[] filePath = { MediaStore.Images.Media.DATA };
        Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(filePath[0]);
        String picturePath = c.getString(columnIndex);
        c.close();
        Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
        Log.w("path of image", picturePath+"");
        ImageView viewImage;
        viewImage=(ImageView)findViewById(R.id.viewImage);
        viewImage.setImageBitmap(thumbnail);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
byte[] byteArray = byteArrayOutputStream .toByteArray();
    } */

        /* COW BREED TRAINING in AUTOML
        Cow Breeds to Test for:
       May add more cow breeds in the future.
        1. Holstein
        2. Jersey
        3. Angus
        4. Hereford
        5. Charolais
        6. Simmental
        7. Texas Longhorn
        8. Red Aungus

        */


//Vision API TEST:
//API Key : AIzaSyBnnTQAZ35okyLxeiHf8b76OfDE_30rEtk
//HeEeEeEyYy SqUiDwArD

//AutoML Vision
//POST URL: https://automl.googleapis.com/v1beta1/projects/animal-identification-app/locations/us-central1/models/ICN6662254112112983311:predict
/* JSON format
{
  "payload": {
    "image": {
      "imageBytes": "YOUR_IMAGE_BYTE"
    }
  }
}
 */


    /*if (button_type == 0 && bitmap != null) {
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
                }*/

/*

   public String readFile(String fileName) throws IOException
    {
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(getAssets().open(fileName), "UTF-8"));

        String content = "";
        String line;
        while ((line = reader.readLine()) != null)
        {
            content = content + line;
        }

        return content;

    }
 */

