/*
 * Author: Matthew Syrén
 *
 * Date:   10 October 2017
 *
 * Description: Class displays information to the user about their results and ranking compared to other users
 */

package a15008377.opsc7312_assign2_15008377;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class UserStatisticsActivity extends UserBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_statistics);

            //Sets the NavigationDrawer for the Activity and sets the selected item in the NavigationDrawer to Statistics
            super.onCreateDrawer();
            super.setSelectedNavItem(R.id.nav_statistics);

            //Fetches the User's Statistics
            new Statistic().requestStatistics(null, this, new DataReceiver(new Handler()));

            //Displays the ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
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

    //Method calculates the user's average Quiz score
    public void calculateAverageScore(ArrayList<Statistic> lstStatistics){
        try{
            //Initialises variables
            double userAverage = 0;
            double currentUserTotal = 0;
            double currentUserCount = 0;
            double currentUserAverage = 0;
            ArrayList lstUserAverages = new ArrayList<>();
            String currentUsername = new User(this).getUserKey();

            //Calculates the average score for each user
            if(lstStatistics.size() > 0){
                double userTotal = 0;
                double userCount = 0;
                String user = lstStatistics.get(0).getUserKey();

                for(Statistic statistic: lstStatistics){
                    //Calculates the average score for the user that is signed in
                    if(statistic.getUserKey().equals(currentUsername)){
                        currentUserTotal += statistic.getResult();
                        currentUserCount++;
                    }

                    //Calculates the average score for all users
                    if(statistic.getUserKey().equals(user)){
                        userTotal += statistic.getResult();
                        userCount++;
                    }
                    else{
                        userAverage = userTotal / userCount;
                        lstUserAverages.add(userAverage);

                        //Fetches the next user
                        user = statistic.getUserKey();
                        userTotal = statistic.getResult();
                        userCount = 1;
                    }
                }

                //Processes the final user
                userAverage = userTotal / userCount;
                lstUserAverages.add(userAverage);
            }

            //Calculates current user's average
            if(currentUserCount > 0){
                currentUserAverage = currentUserTotal / currentUserCount;
            }

            //Sorts the list of all user averages in descending order
            Collections.sort(lstUserAverages);
            Collections.reverse(lstUserAverages);

            //Displays the current user's average
            TextView txtAverage = (TextView) findViewById(R.id.text_statistics);
            txtAverage.setText(getResources().getString(R.string.text_user_average_result, currentUserAverage));
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_results);
            progressBar.setMax(100);

            //Changes colour of ProgressBar based on average score (learnt from https://www.android-examples.com/change-horizontal-progress-bar-color-in-android-programmatically/)
            if(currentUserAverage >= 80){
                progressBar.getProgressDrawable().setColorFilter(Color.parseColor("#00a025"), PorterDuff.Mode.SRC_IN);
            }
            else if(currentUserAverage >= 50){
                progressBar.getProgressDrawable().setColorFilter(Color.parseColor("#e5d600"), PorterDuff.Mode.SRC_IN);
            }
            else{
                progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            }
            progressBar.setProgress((int) Math.round(currentUserAverage));
            progressBar.setScaleY(3f);

            //Displays the current user's rank compared to all other users
            TextView txtCurrentRank = (TextView) findViewById(R.id.text_current_rank);
            for(int i = 0; i < lstUserAverages.size(); i++){
                if(currentUserAverage == (double) lstUserAverages.get(i)){
                    txtCurrentRank.setText(getResources().getString(R.string.text_current_rank, (i + 1)));
                    break;
                }
            }
        }
        catch(Exception exc){
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
            //Processes the result when the Statistic has been fetched from the Firebase Database
            if (resultCode == FirebaseService.ACTION_FETCH_STATISTIC_RESULT_CODE) {
                ArrayList<Statistic> lstStatistics = (ArrayList<Statistic>) resultData.getSerializable(FirebaseService.ACTION_FETCH_STATISTIC);

                if(lstStatistics.size() > 0){
                    calculateAverageScore(lstStatistics);
                }
                else{
                    Toast.makeText(getApplicationContext(), "You haven't completed any quizzes, yet", Toast.LENGTH_LONG).show();
                }

                //Hides the ProgressBar
                toggleProgressBarVisibility(View.INVISIBLE);
            }
        }
    }
}