/*
 * Author: Matthew Syrén
 *
 * Date:   10 October 2017
 *
 * Description: Class allows the user to manage Quizzes by deleting a Quiz, adding a Quiz, and downloading or uploading a video
 */

package a15008377.opsc7312_assign2_15008377;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class QuizManagerActivity extends AdminBaseActivity {
    //Declarations
    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;
    private int notificationID = 1;
    private Notification notification;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_quiz_manager);

            //Sets the NavigationDrawer for the Activity and sets the selected item in the NavigationDrawer to Home
            super.onCreateDrawer();
            super.setSelectedNavItem(R.id.nav_home);

            //Requests Quizzes
            new Quiz().requestQuizzes(null, this, new DataReceiver(new Handler()));

            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Sets the TextChangedListener for the text_search_quizzes, which will perform a search when the user types
            final EditText txtSearchQuizzes = (EditText) findViewById(R.id.text_search_quizzes);
            txtSearchQuizzes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuizzes(txtSearchQuizzes);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            //Prevents the keyboard from appearing automatically. Learnt from https://stackoverflow.com/questions/2496901/android-on-screen-keyboard-auto-popping-up
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method prevents the user from going back to the previous Activity by clicking the back button
    @Override
    public void onBackPressed() {
    }

    //Method fetches all Quizzes that match the search
    public void searchQuizzes(EditText txtSearchQuizzes){
        try{
            //Fetches the search term and requests Quizzes that match the search term
            String searchTerm = txtSearchQuizzes.getText().toString();

            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Fetches the Quizzes from the Firebase Database that match the search term
            new Quiz().requestQuizzes(searchTerm, this, new DataReceiver(new Handler()));
        }
        catch(Exception exc){
            Toast.makeText(getBaseContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method toggles the ProgressBar's visibility
    public void toggleProgressBarVisibility(int visible){
        try{
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(visible);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method takes the user to the QuizSetterActivity
    public void addQuizOnClick(View view){
        try{
            Intent intent = new Intent(QuizManagerActivity.this, QuizSetterActivity.class);
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method displays the available Quizzes to the user
    public void displayQuizzes(final ArrayList<Quiz> lstQuizzes){
        try{
            QuizManagementListViewAdapter quizListViewAdapter = new QuizManagementListViewAdapter(this, lstQuizzes);
            ListView listView = (ListView) findViewById(R.id.list_view_quizzes);
            listView.setAdapter(quizListViewAdapter);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Opens the video camera on the phone to record the tutorial video
    public void recordVideo(String quizKey){
        try{
            //Sets up notification
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationBuilder = new Notification.Builder(this);

            //Opens video camera
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                key = quizKey;
                startActivityForResult(takeVideoIntent, 1);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method displays uploads the video taken by the user
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try{
            if (requestCode == 1 && resultCode == RESULT_OK) {
                Uri videoUri = intent.getData();
                uploadVideo(videoUri);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("VisibleForTests")
    //Method uploads the video that the user has taken to Firebase Storage
    private void uploadVideo(Uri file){
        try{
            Toast.makeText(getApplicationContext(), "Starting upload, please don't close the app until the video has finished uploading...", Toast.LENGTH_LONG).show();

            //Displays notification for upload progress
            notificationBuilder.setOngoing(true)
                    .setContentTitle("Trainer")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText("Uploading...")
                    .setProgress(100, 0, false);
            notification = notificationBuilder.build();
            notificationManager.notify(notificationID, notification);

            //Gets reference to Firebase Storage and Database
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            //Stores the video with a file name of the Quiz key
            StorageReference store = storageReference.child(key + ".mp4");
            store.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(), "Video uploaded successfully", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Upload failed, please try again. " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    //Updates upload progress
                    int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                    notificationBuilder.setProgress(100, progress, false);
                    notificationBuilder.setContentText("Uploading: " + progress + "%");

                    //Displays the upload progress
                    if(progress == 100){
                        notificationManager.cancel(notificationID);
                    }
                    else{
                        notification = notificationBuilder.build();
                        notificationManager.notify(notificationID, notification);
                    }
                }
            });
        }
        catch(Exception exc) {
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Processes the data received from FirebaseService
    private class DataReceiver extends ResultReceiver {
        private DataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult ( int resultCode, Bundle resultData){
            //Processes the result when the Quizzes have been fetched from the Firebase Database
            if (resultCode == FirebaseService.ACTION_FETCH_QUIZ_RESULT_CODE) {
                ArrayList<Quiz> lstQuizzes = (ArrayList<Quiz>) resultData.getSerializable(FirebaseService.ACTION_FETCH_QUIZ);
                if(lstQuizzes.size() == 0){
                    Toast.makeText(getApplicationContext(), "There are currently no quizzes in the database", Toast.LENGTH_LONG).show();
                }
                else{
                    //Displays the Quizzes
                    displayQuizzes(lstQuizzes);
                }

                //Hides ProgressBar
                toggleProgressBarVisibility(View.INVISIBLE);
            }
        }
    }
}
