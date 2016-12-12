package com.zqkaz_000.app2;

/**
 * This MainActivity is part of the Software Engineering 2016 Loyola University Maryland Project.
 * This specific class is for the email and Contact access functionality. The following uses the
 * activity_main.xml as its menu. It completes the following functions
 * <ul>
 * <li>Opening Up Sample email
 * <li>Accessing User Contacts to read in an email into the emailAddress.
 * <li>Connecting with Phone email app to send an email.
 *     (see <a href="#setXORMode">setXORMode</a>)
 * </ul>
 * <p>
 * This classes uses various built-in functionalities of Android Studio and the device.
 * The email sending is only possible through the email app on the device thus it must be set up.
 * This also requests access to Internet on the device - the permission are all included in the manifest.
 * @author      Zahara Kazmi
 * @version     1.0 - Final Version
 * @since       1.0
 */

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;


/**
 * In our defining of the MainActivity we inherit from AppCompatActivity.
 * We define Global variables to be used in all the methods. This allows us
 * to create more methods and have smaller pieces of code because we do not have big chunk of code
 * and we are not passing vaiables back and forth.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    EditText editTextEmail, editTextSubject, editTextMessage;
    Button btnSend, btnattch,emailContact;
    String email,subject, message,photopath;
    ImageView pimageView;
    private static final int PICK_CONTACT=2;
    private static final int CAMERA_REQUEST=3;
    int PERMISSION_ALL=1;
    String[] PERMISSIONS={android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA};
    File picture;
    Uri photoUri;
    /**
     * The OnCreate Method reads in the values user entered on the menu and starts the method the
     * buttons that are pressed. We pass in the SavedState of the menu - showing whatever the user
     * entered so far.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextEmail = (EditText) findViewById(R.id.editTexTo);
        editTextSubject = (EditText) findViewById(R.id.editTextSubject);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        btnSend = (Button) findViewById(R.id.buttonSend);
        emailContact=(Button)findViewById(R.id.contact);
        btnattch=(Button)findViewById(R.id.attach);
        pimageView=(ImageView)findViewById(R.id.imageView);

        btnSend.setOnClickListener(this);
        emailContact.setOnClickListener(this);
        btnattch.setOnClickListener(this);

    }

    /**
     * This method runns the activity for whatever selection is made by the user from the buttons.
     * @param requestCode - code for what the user is requesting - Email or Contacts
     * @param resultCode - Checking with system to ask if it is okay to proceed
     * @param data - Intent that was completed by user - send, contact, capture
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            pimageView.setImageBitmap(imageBitmap);

        }
            if (requestCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {
                if (!hasPermissions(this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                }
                editTextEmail.setText(getEmailContact());
                email = editTextEmail.getText().toString();
                subject = editTextSubject.getText().toString();
                message = editTextMessage.getText().toString();
            }
    }

    /**
     * This method directs the program according whatever button is clicked and decides whatever happens
     * as a result to what the buttons do.
     * @param v cheecks which button was clicked
     */
    @Override
    public void onClick(View v) {

        if (v == btnSend) {
            email = editTextEmail.getText().toString();
            subject = editTextSubject.getText().toString();
            message = editTextMessage.getText().toString();
            sendEmail();
        }
        if(v==emailContact){
            openContacts();
        }
        if(v==btnattch){
            TakePicture();

        }
    }

    /**
     *This method starts the intent to open the contacts on the device, starting
     * the activity to initiate the opening of contacts.
     */
    public void openContacts(){
        Intent intent=new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent,PICK_CONTACT);
    }

    /**
     * This method  execuutes the intent for sending an email with information collected from the menu.
     * This method reads in the global variables of string email, subject, and message. We can define
     * these as we are about to send the email. This allows us to use email from what user entered or
     * if used contact list to get email.
     */
    public void sendEmail(){
        try {

            final Intent emailIntent = new Intent(
                    Intent.ACTION_SEND);
            emailIntent.setType("image/png");
            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{email});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                    subject);
            //THIS IS TOO ADD IMAGE SENDING AS PART OF EMAIL
            emailIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
            emailIntent
                    .putExtra(Intent.EXTRA_TEXT, message);
            this.startActivity(Intent.createChooser(emailIntent,
                    "Sending email..."));

        } catch (Throwable t) {
            Toast.makeText(this,
                    "Request failed try again: " + t.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This method is a helper method which we will be using to check for permissions. This new verison
     * of android states that permission should be asked within the app when we are about to use instead of
     * defining in the manifest thus this method is simply to grant permission.
     * @param context - what are we asking permission for
     * @param permissions - set of defined permission that we will be using in this class
     * @return returns permission has been granted or rejected
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method reads in the contact once we have access to the list. Using Cursor and ContentResolver,
     * it will read in contacts information such as their name, phone and email.
     * @return email for the contact we select to send an email
     */
    public String getEmailContact(){
        String id, name,phoneNumber;
                String emailContact="sample@sample.edu";
        Cursor cursor=getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
            while(cursor.moveToNext()){
                id=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                name=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Cursor phoneCursor=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" =?",new String[]{id},null);

                while(phoneCursor.moveToNext()){
                    phoneNumber=phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }

                Cursor emailCursor=getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",new String[]{id},null);
                while(emailCursor.moveToNext()) {
                    emailContact = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                }
                emailCursor.close();
                phoneCursor.close();
            }
        cursor.close();
        return emailContact;
    }

    /**
     * This following functionality returns a picture file that has been created after taking a picture
     * actions is complete. We can use this method to get URI from file for sending email.
     * @return picture file for taken picture.
     */

    public void TakePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        File photoFile = null;
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("BROKEN", "CAN't"+ ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoUri = Uri.fromFile(photoFile);
                //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    /**
     * This method creates an image file and store it in the picture directory on the device.
     * @return returns photofile which is created
     * @throws IOException for if a null file is made.
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        storageDir.mkdirs();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        photopath = image.getAbsolutePath();
        return image;

    }



}

