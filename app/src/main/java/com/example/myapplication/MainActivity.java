package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

//The Main Class
//AppCompatActivity in the Video
public class MainActivity extends AppCompatActivity {

    //Initialize Variable for all the Button ID's you created in the xml file
    EditText mResultEt;
    ImageView mPreviewIv;


    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    String cameraPermission[];
    String storagePermission[];

    Uri image_uri;

    //There's always an Override method in the Main Class
    //@Override also explained in the video

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  //The design layout xml file to which it will refer. Here, it refers to the "activity_main" xml file

        //Assign Variables declared above to each and every Button by using findViewById...
        //... to find the ID of the buttons from the xml file so that it links to the Java file...
        //...and we can assign ant task that the particular tool will perform through the Java file
        mResultEt = findViewById(R.id.resultEt);
        mPreviewIv = findViewById(R.id.imageIv);

        //Manifest file is used for all the permission that an app can require, like Internet access, camera and storage access
        //camera permission
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //storage permission
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }


    //Actionbar Menu--->>>
    //Refers to the 3-dots button which is used to pop a menu with certain options
    //Located at the top right of the mobile screen
    //OnCreateOptionsMenu() to specify the options menu for an activity. You can inflate your menu resource by this method
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*In (Menu menu) -->> Menu is just the type of the parameter menu. For example you can have a String type for a variable named string, dog, etc....
    ...And in this case there's a Menu type for a parameter named menu, denoted by menu_main xml file
        */

        //inflate menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);  // //The design layout xml file to which it will refer. Here, it refers to the "menu_main" xml file
        return true;
    }

    //--------------------------------------------------------------------------


    //Handle Actionbar item clicks--->>>

    //The items that will be present in the ActionBar (or the top bar) is set here. onOptionsItemSelected() method is there to deal with this.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();  //To get the ID of the buttons that we declared in the "menu_main" xml file
        if(id == R.id.addImage){   //Whenever the image button is clicked...
            showImageImportDialog();   //...Jump to this function
        }
        if(id == R.id.settings){    //On clicking the settings option in the Menu Inflater...
            Toast.makeText(this,"Settings", Toast.LENGTH_SHORT).show();  //Toast is a small bar that appears at the bottom of the mobile screen
        }
        return super.onOptionsItemSelected(item);
    }

    //-------------------------------------------------------------------------------

    //Access Commands --->>

    private void showImageImportDialog(){
        //items to display in dialog
        //AlertDialog.Builder object is created to display the 2 options that will be displayed for the user, to choose between then
        String[] items = {"Camera", "Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //setting title of the Dialog Box
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){  //If you've selected Camera
                    //camera option clicked
                    if(!checkCameraPermission()){
                        //camera permission not allowed, request it
                        requestCameraPermission();
                    }
                    else{
                        //permission allowed, take picture
                        pickCamera();
                    }
                }
                if(which == 1){  //If you've selected Gallery
                    //gallery option clicked
                    if(!checkStoragePermission()){
                        //storage permission not allowed, request it
                        requestStoragePermission();
                    }
                    else{
                        //permission allowed, take picture
                        pickGallery();
                    }
                }
            }
        });
        dialog.create().show();   //show dialog
    }

    //------------------------------------------------------------------


    //Gallery ----->>>>>
    private void pickGallery() {
        //intent to pick the image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        //set intent type to image
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    //--------------------------------------------------------------------


    //Camera ---------->>>>>
    private void pickCamera(){
        //intent to take the image from the camera, it will also be saved to the storage to get the high quality image
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic"); //title of the picture
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image to Text"); //description
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //The contract between the media provider and applications. Contains definitions for the supported URI's
        //Standard Intent action that can be sent to have the camera application capture an image and return it.

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //The name of the Intent-extra used to indicate a content resolver Uri to be used to store the requested image
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    //------------------------------------------------------------------


    //Request Storage Permission ----->>>>
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);

    }

    //------------------------------------------------------------------


    //Check Storage Permission ---->>>
    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    //------------------------------------------------------------------


    //Request Camera Permission ----->>>>
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission,CAMERA_REQUEST_CODE);
    }

    //------------------------------------------------------------------


    //Check Camera Permission ---->>>
    private boolean checkCameraPermission(){
        /* Check camera permission and return the result
            *In order to get high quality image we have to save the image to the external storage first
            * before inserting into image view , so the storage permission will also be required
         */
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result & result1;
    }

    //------------------------------------------------------------------


    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length > 0){
                    //Created a variable for Camera Permission and Storage Permission
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        pickCamera();
                    }
                    else{  //Denying of permission of Camera and Storage
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if(grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        pickGallery();
                    }
                    else{
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //-----------------------------------------------------------------------------------


    //handle image result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){ //Picked up the image from gallery
                //got image from gallery, now crop it
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON) //enable image guidelines
                .start(this);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){  //Shot an image on Camera
                //got image from camera, now crop it
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON) //enable image guidelines
                .start(this);
            }
        }

        //get cropped image
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri(); //get image uri(url)
                //set image to image view
                mPreviewIv.setImageURI(resultUri);

                //get drawable bitmap for text recognition
                //BitmapDrawable is related to the part where the cropped image will be shown on the screen, and the character recognition will be done from that cropped image
                //Hence it is using mPrevieIv (the button variable that links to the ID of the ImageView in the "activity_main" xml file

                BitmapDrawable bitmapDrawable = (BitmapDrawable)mPreviewIv.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                //TextRecognizer is an API provided by Google specially for OCR apps
                //recognizer object is created to support the working of the TextRecognizer API
                /*getApplicationContext() - Return the context of the process of all activity running inside it. If you need a context that bind entire lifecycle of application you can use this. You should use it in across the activity.
                Example Use: If you have to create a singleton object for your application and that object needs a context, always pass the application context.
                If you pass the activity context here, it will lead to the memory leak as it will keep the reference to the activity and activity will not be garbage collected. */

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if(!recognizer.isOperational()){
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }
                else{
                    //TextRecognizer - Finds and recognizes text in a supplied Frame.
                    //Object created for the Frame which will bounce through each word, considering each word as a separate frame and each space between the words are for breaking one frame from another

                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame); //Finds and recognizes text in a supplied Frame.

                    /*The principal operations on a StringBuilder are the append and insert methods, which are overloaded so as to accept data of any type.
                     Each effectively converts a given datum to a string and then appends or inserts the characters of that string to the string builder.
                      The append method always adds these characters at the end of the builder; the insert method adds the characters at a specified point.*/

                    StringBuilder sb = new StringBuilder();
                    //get text from sb until there is no text
                    for(int i = 0; i<items.size(); i++){
                        TextBlock myItem = items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");
                    }
                    //set text to edit text
                    //ToString() Returns a string representation of the object.
                    mResultEt.setText(sb.toString());
                }
            }
            else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                //if there is any error show it
                Exception error = result.getError();
                Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show();
            }
        }
    }


}

