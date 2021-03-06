package edu.orangecoastcollege.cs273.flagquiz;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *  MainActivity is the main Controller for the app Flag Quiz.
 *
 *  There is a constant int <code>FLAGS_IN_QUIZ</code> which represents how many flags
 *  the user will try to guess.
 *
 *  The instance variables include:
 *      - A constant String for use in logcat messages.
 *      - An array of Buttons for the flag options, there are four currently.
 *      - A list of all the possible countries loaded from JSON.
 *      - A much shorter list which is populated randomly from the all list,
 *          it will contain only the countries for the quiz.
 *      - A correct country variable.
 *      - Int to track the total guesses.
 *      - Int to track number of correct guesses.
 *      - A SecureRandom object to help randomize the quiz
 *      - A handler which provides a delay after showing a correct answer so the user gets a
 *          second to associate the name of the country with its flag.
 *      - TextView to track which question number user is on.
 *      - ImageView to display flag.
 *      - TextView that will display the correct answer in green text.
 *
 *   This class has four methods:
 *      - onCreate
 *          onCreate sets the content view, initializes the quiz countries list, the random
 *          number generator, and the handler for delaying transitions.
 *          Also, the View and Button widgets are connected.
 *          The question # out of FLAG_IN_QUIZ is rendered.
 *          A try-catch surrounds the JSON loader which populates the quiz countries list.
 *          Lastly, resetQuiz is called.
 *
 *      - resetQuiz
 *      - loadNextFlag
 *      - makeGuess
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Flag Quiz";

    private static final int FLAGS_IN_QUIZ = 10;

    private Button[] mButtons = new Button[4];
    private List<Country> mAllCountriesList;  // all the countries loaded from JSON
    private List<Country> mQuizCountriesList; // countries in current quiz (just 10 of them)
    private Country mCorrectCountry; // correct country for the current question
    private int mTotalGuesses; // number of total guesses made
    private int mCorrectGuesses; // number of correct guesses
    private SecureRandom rng; // used to randomize the quiz
    private Handler handler; // used to delay loading next country

    private TextView mQuestionNumberTextView; // shows current question #
    private ImageView mFlagImageView; // displays a flag
    private TextView mAnswerTextView; // displays correct answer

    /**
     * onCreate is called when the app starts.
     *
     *  The content view is set, the quiz countries list, random number generator
     *  and the handler for transitions are all initialized.
     *
     *  The TextViews and ImageView are connected.
     *
     *  The current question text is set.
     *
     *  A try catch surrounds the JSON loading functionality to populate the all countries list.
     *
     *  The quiz is reset.
     *
     * @param savedInstanceState restores any previous state if applicable.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mQuizCountriesList = new ArrayList<>(FLAGS_IN_QUIZ);
        rng = new SecureRandom();
        handler = new Handler();

        // COMPLETED: Get references to GUI components (textviews and imageview)
        mQuestionNumberTextView = (TextView) findViewById(R.id.questionNumberTextView);
        mFlagImageView = (ImageView) findViewById(R.id.flagImageView);
        mAnswerTextView = (TextView) findViewById(R.id.answerTextView);

        // COMPLETED: Put all 4 buttons in the array (mButtons)
        mButtons[0] = (Button) findViewById(R.id.button);
        mButtons[1] = (Button) findViewById(R.id.button2);
        mButtons[2] = (Button) findViewById(R.id.button3);
        mButtons[3] = (Button) findViewById(R.id.button4);


        // COMPLETED: Set mQuestionNumberTextView's text to the appropriate strings.xml resource
        mQuestionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));

        // COMPLETED: Load all the countries from the JSON file using the JSONLoader
        try {
            mAllCountriesList = JSONLoader.loadJSONFromAsset(this);
        } catch (IOException  e) {
            Log.e(TAG, "Error loading JSON file", e);
        }

        // COMPLETED: Call the method resetQuiz() to start the quiz.
        resetQuiz();
    }

    /**
     * Sets up and starts a new quiz.
     * Guesses are reset, new countries are loaded into the quiz list, and the quiz is started
     * via loadNextFlag().
     */
    public void resetQuiz() {

        // COMPLETED: Reset the number of correct guesses made
        mCorrectGuesses = 0;

        // COMPLETED: Reset the total number of guesses the user made
        mTotalGuesses = 0;

        // COMPLETED: Clear list of quiz countries (for prior games played)
        mQuizCountriesList.clear();

        // COMPLETED: Randomly add FLAGS_IN_QUIZ (10) countries from the mAllCountriesList into the mQuizCountriesList
        int allCountriesSize = mAllCountriesList.size();
        Country randomCountry;
        while( mQuizCountriesList.size() < FLAGS_IN_QUIZ )
        {
            randomCountry = mAllCountriesList.get(rng.nextInt(allCountriesSize));
            // COMPLETED: Ensure no duplicate countries (e.g. don't add a country if it's already in mQuizCountriesList)
            if ( ! mQuizCountriesList.contains(randomCountry) )
                mQuizCountriesList.add(randomCountry);
        }

        // COMPLETED: Start the quiz by calling loadNextFlag
        loadNextFlag();
    }

    /**
     * The method loadNextFlag initiates the process of loading the next flag for the quiz, showing
     * the flag's image and then 4 buttons, one of which contains the correct answer.
     */
    private void loadNextFlag() {

        // COMPLETED: Initialize the mCorrectCountry by removing the item at position 0 in the mQuizCountries
        mCorrectCountry = mQuizCountriesList.remove(0);

        // COMPLETED: Clear the mAnswerTextView so that it doesn't show text from the previous question
        mAnswerTextView.setText("");

        // COMPLETED: Display current question number in the mQuestionNumberTextView
        int questionNumber = FLAGS_IN_QUIZ - mQuizCountriesList.size();
        mQuestionNumberTextView.setText(getString(R.string.question, questionNumber, FLAGS_IN_QUIZ));

        // COMPLETED: Use AssetManager to load next image from assets folder
        AssetManager am = getAssets();

        // COMPLETED: and try to use the InputStream to create a Drawable
        try {

            // COMPLETED: Get an InputStream to the asset representing the next flag
            InputStream stream = am.open(mCorrectCountry.getFileName());
            // COMPLETED: The file name can be retrieved from the correct country's file name.
            Drawable image = Drawable.createFromStream(stream, mCorrectCountry.getName());
            // COMPLETED: Set the image drawable to the correct flag.
            mFlagImageView.setImageDrawable(image);

        } catch (IOException e) {
            Log.e(TAG, "Error loading image: " + mCorrectCountry.getFileName(), e);
        }



        // COMPLETED: Shuffle the order of all the countries (use Collections.shuffle)
        do {
            Collections.shuffle(mAllCountriesList);
        } while(mAllCountriesList.subList(0, mButtons.length).contains(mCorrectCountry));

        // COMPLETED: Loop through all 4 buttons, enable them all and set them to the first 4 countries
        // COMPLETED: in the all countries list
        for (int i = 0; i < mButtons.length; ++i)
        {
            mButtons[i].setEnabled(true);
            mButtons[i].setText(mAllCountriesList.get(i).getName());
        }

        // COMPLETED: After the loop, randomly replace one of the 4 buttons with the name of the correct country
        mButtons[rng.nextInt(mButtons.length)].setText(mCorrectCountry.getName());
    }

    /**
     * Handles the click event of one of the 4 buttons indicating the guess of a country's name
     * to match the flag image displayed.  If the guess is correct, the country's name (in GREEN) will be shown,
     * followed by a slight delay of 2 seconds, then the next flag will be loaded.  Otherwise, the
     * word "Incorrect Guess" will be shown in RED and the button will be disabled.
     * @param v
     */
    public void makeGuess(View v) {

        // COMPLETED: Downcast the View v into a Button (since it's one of the 4 buttons)
        Button clickedButton = (Button) v;

        // COMPLETED: Get the country's name from the text of the button
        String guess = clickedButton.getText().toString();

        mTotalGuesses++;
        // COMPLETED: If the guess matches the correct country's name, increment the number of correct guesses,
        if (guess.equals(mCorrectCountry.getName())) {

            // Disable all buttons (don't let user guess again)
            for (Button b: mButtons)
                b.setEnabled(false);

            mCorrectGuesses++;
            mAnswerTextView.setText(mCorrectCountry.getName());
            // COMPLETED: then display correct answer in green text.  Also, disable all 4 buttons (can't keep guessing once it's correct)
            mAnswerTextView.setTextColor(ContextCompat.getColor(this, R.color.correct_answer));

            if (mCorrectGuesses < FLAGS_IN_QUIZ)
            {
                // Wait two seconds, then load next flag
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextFlag();
                    }
                }, 2000);
            } else {
                // COMPLETED: Nested in this decision, if the user has completed all 10 questions, show an AlertDialog
                // Show an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // COMPLETED: with the statistics and an option to Reset Quiz
                builder.setMessage(getString(R.string.results, mTotalGuesses, ((double) mCorrectGuesses / mTotalGuesses)*100 ));
                // resetQuiz
                builder.setPositiveButton(getString(R.string.reset_quiz), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resetQuiz();
                    }
                });

                builder.setCancelable(false);
                builder.create();
                builder.show();
            }
        }
        else {
            // COMPLETED: Else, the answer is incorrect, so display "Incorrect Guess!" in red
            mAnswerTextView.setText(getString(R.string.incorrect_answer));
            mAnswerTextView.setTextColor(ContextCompat.getColor(this, R.color.incorrect_answer));
            // COMPLETED: and disable just the incorrect button.
            clickedButton.setEnabled(false);
        }

    }

}
