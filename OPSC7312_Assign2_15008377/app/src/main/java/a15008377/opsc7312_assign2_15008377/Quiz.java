package a15008377.opsc7312_assign2_15008377;

import android.content.Context;
import android.content.Intent;

import android.os.ResultReceiver;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Matthew Syrén on 2017/09/16.
 */

public class Quiz implements Serializable{
    //Declarations
    private String name;
    private ArrayList<Question> lstQuestions;

    public Quiz(){

    }

    public Quiz(String name, ArrayList<Question> lstQuestions) {
        this.name = name;
        this.lstQuestions = lstQuestions;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Question> getLstQuestions() {
        return lstQuestions;
    }

    //Requests Quiz Items from the Firebase Database
    public void requestQuizzes(String searchTerm, Context context, ResultReceiver resultReceiver){
        try{
            //Requests location information from the LocationService class
            String firebaseKey = new User(context).getUserKey();
            Intent intent = new Intent(context, FirebaseService.class);
            intent.putExtra(FirebaseService.QUIZ_ID, searchTerm);
            intent.setAction(FirebaseService.ACTION_FETCH_QUIZ);
            intent.putExtra(FirebaseService.RECEIVER, resultReceiver);
            context.startService(intent);
        }
        catch(Exception exc){
            Toast.makeText(context, exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method calls the FirebaseService class and passes in a Quiz object that must be written to the Firebase Database
    public void requestWriteOfQuiz(Context context, String action, ResultReceiver resultReceiver){
        try{
            //Requests location information from the LocationService class
            Intent intent = new Intent(context, FirebaseService.class);
            intent.setAction(FirebaseService.ACTION_WRITE_QUIZ);
            intent.putExtra(FirebaseService.ACTION_WRITE_QUIZ, this);
            intent.putExtra(FirebaseService.ACTION_WRITE_QUIZ_INFORMATION, action);
            intent.putExtra(FirebaseService.RECEIVER, resultReceiver);
            context.startService(intent);
        }
        catch(Exception exc){
            Toast.makeText(context, exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
