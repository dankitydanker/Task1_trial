package com.example.task1_trial2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static java.lang.Integer.parseInt;

public class Gayme extends AppCompatActivity {
    ConstraintLayout qnBackground;
    TextView tvTimer;
    RadioGroup rgOptions;
    Button buSubmit;
    Button buContinue;
    EditText etQn;
    TextView tvScore;
    TextView tvStreak;
    TextView tvTimerLabel;
    TextView tvHighScore;
    TextView tvFramedQuestion;

    CountDownTimer cdTImer;
    static final long START_TIME_IN_MILLIS = 10100;
    long timeleftinmills = START_TIME_IN_MILLIS;
    int seconds_left;
    MediaPlayer mp = new MediaPlayer();

    static final long vib_millis = 500;

    public static final String SHARED_PREFS = "sharedPref";
    public static final String SCORE = "score";

    int high_score;

    int current_score = 0;
    int streak = 0;
    int no_of_questions = 0;

    ArrayList<Integer> factors_of_qn = new ArrayList<>();
    ArrayList<Integer> dummy_answers = new ArrayList<>();
    int[] rb_options = {R.id.rb1,R.id.rb2,R.id.rb3,};
    int[] timer_sounds = {R.raw.beep_sound,R.raw.wrong_timeout,};
    int[] answer_sounds = {R.raw.wrong_timeout,R.raw.crct_ans,};

    String question_string;
    int question;
    int crct_answer;

    boolean less_factors;
    boolean vibe_check = true;
    boolean sound_check = true;
    boolean qn_empty;
    boolean cdTImer_running;

    RadioButton crct_rbutton;
    int crct_rbutton_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.DARK_MODE);
        }
        else{
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gayme);
        qnBackground = findViewById(R.id.qnBackground);
        tvTimer = findViewById(R.id.tvTimer);
        rgOptions = findViewById(R.id.rgOptions);
        buSubmit = findViewById(R.id.buSubmit);
        buContinue = findViewById(R.id.buContinue);
        etQn = findViewById(R.id.etQn);
        tvScore = findViewById(R.id.tvScore);
        tvStreak = findViewById(R.id.tvStreak);
        tvTimerLabel = findViewById(R.id.tvTimerLabel);
        tvFramedQuestion = findViewById(R.id.tvFramedQuestion);
        tvHighScore = findViewById(R.id.tvHighScore);
        enable_clearet_bu();
        clear_radiogrp();
        tvTimerLabel.setVisibility(View.INVISIBLE);
        getSupportActionBar().setTitle("Find those Factors!");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        buContinue.setVisibility(View.INVISIBLE);

        Intent this_intent = getIntent();
        high_score = this_intent.getIntExtra("BEST_STREAK",0);
        Log.d("high_created", "onCreate: "+ high_score);
        tvHighScore.setText("High Score: "+high_score);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater in_gayme_options = getMenuInflater();
        in_gayme_options.inflate(R.menu.in_gayme_toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.vibrate_toggle:
                vibe_check = !vibe_check;
                if(!vibe_check){
                    item.setIcon(R.drawable.ic_vibration_white_24dp_off);
                }
                else{
                    item.setIcon(R.drawable.ic_vibration_white_24dp);
                }
                break;
            case R.id.sound_toggle:
                sound_check = !sound_check;
                if(!sound_check){
                    item.setIcon(R.drawable.ic_volume_off_white_24dp);
                }
                else{
                    item.setIcon(R.drawable.ic_volume_up_white_24dp);
                }
                break;
            case  android.R.id.home:
                if(TextUtils.isEmpty(question_string)){
                    AlertDialog.Builder adQuit = new AlertDialog.Builder(this);
                    adQuit.setTitle("Quit?")
                            .setMessage("Are you sure you want to quit?\nYou will lose your streak :(")
                            .setCancelable(false)
                            .setPositiveButton("Keep Going :)", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d("Timer", ":" + cdTImer_running);
                                    Log.d("Timeleft", ": " + timeleftinmills);
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton("Quit :(", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //mp.release();
                                    streak = 0;
                                    Log.d("gayme", "Gayme is kil ");
                                    int result = high_score;
                                    Intent result_intent = new Intent();
                                    result_intent.putExtra("high_score",result);
                                    Log.d("destroy", "high_score ="+result_intent.getIntExtra("high_score",0));
                                    setResult(RESULT_OK, result_intent);
                                    finish();
                                }
                            });
                    adQuit.show();
                    return true;
                }
                else {
                    if (cdTImer_running) {
                        pause_timer();
                    }
                    AlertDialog.Builder adQuit = new AlertDialog.Builder(this);
                    adQuit.setTitle("Quit?")
                            .setMessage("Are you sure you want to quit?\nYou will lose your streak :(")
                            .setCancelable(false)
                            .setPositiveButton("Keep Going :)", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d("Timer", ":" + cdTImer_running);
                                    Log.d("Timeleft", ": " + timeleftinmills);
                                    if (!cdTImer_running) {
                                        dialogInterface.dismiss();
                                        start_timer();
                                    } else {
                                        clear_everything_leave_timer();
                                    }
                                }
                            })
                            .setNegativeButton("Quit :(", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //mp.release();
                                    streak = 0;
                                    Log.d("gayme", "Gayme is kil ");
                                    int result = high_score;
                                    Intent result_intent = new Intent();
                                    result_intent.putExtra("high_score",result);
                                    Log.d("destroy", "high_score ="+result_intent.getIntExtra("high_score",0));
                                    setResult(RESULT_OK, result_intent);
                                    finish();
                                }
                            });
                    adQuit.show();
                    return true;
                }


        }
        return super.onOptionsItemSelected(item);
    }

    // main submit button function
    public void buSubmit(View view) {
       question_string = etQn.getText().toString();
       if(!TextUtils.isEmpty(question_string)){
           question = parseInt(question_string);
       }
        Log.d("question", "" + question);
        Log.d("TAG", "buSubmit: "+ qn_empty);
        if(TextUtils.isEmpty(question_string) || question == 0){
            AlertDialog.Builder adQnEmpty = new AlertDialog.Builder(this);
            adQnEmpty.setTitle("-_-")
                    .setMessage("You either didn't enter a question or you entered 0 \n -_-")
                    .setCancelable(false)
                    .setPositiveButton("Try with another number", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            clear_everything_leave_timer();
                            dialogInterface.dismiss();
                        }
                    });
            adQnEmpty.show();
        }
        else {
            question = parseInt(question_string);
            tvTimerLabel.setVisibility(View.VISIBLE);
            if(question == 6){
                question_is_six();
            }
           else {
                start_game();
            }
        }
    }
    public void buContinue(View view) {
        clear_everything();
        tvFramedQuestion.setText("");
    }

    //find actual factors of question
    public void find_factors(int qn){
        //till question bcoz it might be prime
        for(int i = 1; i <= qn; i++){
            if( qn % i == 0){
                factors_of_qn.add(i);
            }
            else{
                continue;
            }
        }

        Log.d("factors of "+ question, ": "+factors_of_qn.size());

    }

    //find a random factor from factors arraylist
    public void find_random_crct_factor(){
        Random r_crct_factor = new Random();
        int r_crct_factor_index = r_crct_factor.nextInt(factors_of_qn.size());
        //store the selected crct_factor in global variable
        crct_answer = factors_of_qn.get(r_crct_factor_index);
        find_dummy_answers();
    }

    //add dummy answers(not factors) to arraylist
    public void find_dummy_answers(){

        //till question/2 to increase difficulty(?)
        while(dummy_answers.size()+1 <= 3 ){
            if(question <=4 || question==6 ){
                //enter_bigger_number();
                break;
            }

            Log.d("find_dummy_answers: ", "" + dummy_answers.size());
            int dummy = (int) getRandomIntegerBetweenRange();
            Log.d("dummy", ""+ dummy);
            if(!factors_of_qn.contains(dummy) && !dummy_answers.contains(dummy) ){
                dummy_answers.add(dummy);
            }
        }

        Log.d("find_dummy_answers: ", "" + dummy_answers.size());
    }

    //fn to get random int between 2 numbers
    public double getRandomIntegerBetweenRange(){
        double x = (int)(Math.random()*((question-1)+1))+1;
        return x;
    }

    //insert dummy answers into rbOptions but also insert one crct_factor
    public void insert_dummy_answers(){
        RadioButton dummy_rbutton;

        try {
            for(int i = 0; i < 3; i++){
                Random r_dummy = new Random();
                int dummy_index = r_dummy.nextInt(dummy_answers.size());
                int dummy_answer = dummy_answers.get(dummy_index);
                dummy_rbutton = findViewById(rb_options[i]);
                dummy_rbutton.setText(""+dummy_answer);
                //remove inserted dummy_answer from arraylist just to be safe
                dummy_answers.remove(dummy_answers.get(dummy_index));}
        } catch (Exception e) {
            Log.d("TAG", "STOPPING TIMER ");
            Log.d("TAG", "caught exception ");
            AlertDialog.Builder adLessFactors = new AlertDialog.Builder(this);
            less_factors = true;
            no_of_questions -= 1;
            Log.d("TAG", "creating dialog ");
            adLessFactors.setTitle("Try a Bigger Number ")
                    .setMessage("The factors of "+question+" are more than half the number! \n Try a bigger number ;)")
                    .setCancelable(false)
                    .setPositiveButton("Try a Bigger Number", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            reset_timer();
                            clear_everything_leave_timer();
                            dialogInterface.dismiss();
                        }
                    });

            adLessFactors.show();
            Log.d("less_factors", "" + less_factors);
        }
        insert_one_crct_answer();
    }

    public void insert_one_crct_answer(){
        Random r_crct_rb_button = new Random();
        int r_crct_rb_button_index = r_crct_rb_button.nextInt(rb_options.length);
        //set the crct_rbutton to the global radio button
        crct_rbutton = findViewById(rb_options[r_crct_rb_button_index]);
        crct_rbutton.setText(""+crct_answer);
        //store crct_rbutton id just in case
        crct_rbutton_id = crct_rbutton.getId();
    }

    // disable both etQn and buSubmit while playing
    public void disable_et_bu(){
        etQn.setText("");
        buSubmit.setVisibility(View.INVISIBLE);
        buSubmit.setEnabled(false);
        etQn.setEnabled(false);
    }

    // enable etQn and buSubmit for new game
    public void enable_clearet_bu(){
        etQn.setEnabled(true);
        buSubmit.setVisibility(View.VISIBLE);
        etQn.setText("");
        buSubmit.setEnabled(true);
    }

    public void start_timer(){

        cdTImer = new CountDownTimer(timeleftinmills,1000) {
            @Override
            public void onTick(long millisuntilfinsih) {
                timeleftinmills = millisuntilfinsih;
                update_cdTimer();
                if(less_factors){
                    cdTImer.cancel();
                }
            }

            @Override
            public void onFinish() {
                timeleftinmills = START_TIME_IN_MILLIS;
                seconds_left = (int) timeleftinmills / 1000;
                vibrate_if_wrong_timeup();
                timeup();
            }
        }.start();
        cdTImer_running = true;

    }

    public void update_cdTimer(){
        tvTimer.setTextColor(getResources().getColor(R.color.crct_answer));
        seconds_left =(int) timeleftinmills / 1000;
        String timer_second = "" + seconds_left;
        if(seconds_left == 0 && sound_check) {
            mp = MediaPlayer.create(this, timer_sounds[1]);
            mp.start();
            mp.reset();
        }
        if(seconds_left<=3){
            tvTimer.setTextColor(Color.RED);

        }
        if(seconds_left<=3 && sound_check){
            mp = MediaPlayer.create(this,timer_sounds[0]);
            mp.start();

        }
        mp.reset();
        tvTimer.setText(timer_second+"s");
    }

    public void vibrate_if_wrong_timeup(){
        if(vibe_check) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(vib_millis, VibrationEffect.DEFAULT_AMPLITUDE));
            } else
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(vib_millis);
        }
    }

    // disable whole of rgOptions after selecting an answer
    public void disable_radiogroup(){
        for(int i =0 ; i<3; i++) {

            ((RadioButton) rgOptions.getChildAt(i)).setFocusable(false);
            ((RadioButton) rgOptions.getChildAt(i)).setClickable(false);
            ((RadioButton) rgOptions.getChildAt(i)).setChecked(false);

        }
    }

    public void clear_radiogrp(){
        for(int i =0 ; i<3; i++) {

            ((RadioButton) rgOptions.getChildAt(i)).setText("");
            ((RadioButton) rgOptions.getChildAt(i)).setBackgroundColor(getResources().getColor(R.color.trans_rbutton));
            ((RadioButton) rgOptions.getChildAt(i)).setVisibility(View.INVISIBLE);


        }
    }

    // enable full rgOptions after clicking buSubmit
    public void  enable_radiogroup(){
        for(int i =0 ; i<3; i++) {

            ((RadioButton) rgOptions.getChildAt(i)).setEnabled(true);
            ((RadioButton) rgOptions.getChildAt(i)).setChecked(false);
            ((RadioButton) rgOptions.getChildAt(i)).setClickable(true);
            ((RadioButton) rgOptions.getChildAt(i)).setVisibility(View.VISIBLE);

        }

    }

    public void start_game(){
        tvFramedQuestion.setText("The factor of "+question+" is :");
        factors_of_qn.clear();
        dummy_answers.clear();
        Log.d("srart game ", "start_game: done ");
        enable_radiogroup();
        no_of_questions += 1;
        disable_et_bu();
        find_factors(question);
        find_random_crct_factor();
        Log.d("dummy answers", "inserting");
        insert_dummy_answers(); // also inserts one crct_answer
        start_timer();
        rgOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                disable_radiogroup();
                int rb_selected_id = rgOptions.getCheckedRadioButtonId();
                RadioButton selected_rb = findViewById(rb_selected_id);
                if(selected_rb == crct_rbutton){
                    correct_answer();
                }
                else{
                    wrong_answer();
                    selected_rb.setBackgroundColor(getResources().getColor(R.color.wrong_answer));
                }
            }
        });

    }

    // if user selects crct_answer
    public void correct_answer(){
        cdTImer_running = false;
        question_string = etQn.getText().toString();
        cdTImer.cancel();
        streak += 1;
        mp.create(this,answer_sounds[1]);
        mp.start();
        mp.reset();
        current_score += 1;
        tvScore.setText("Score: "+current_score+"/"+no_of_questions);
        tvStreak.setText("Streak: "+streak);
        crct_rbutton.setBackgroundColor(getResources().getColor(R.color.crct_answer));
        buContinue.setVisibility(View.VISIBLE);
        check_if_high_score();
        //clear_everything();
        // open positive alert dialog
        /*AlertDialog.Builder adPositive = new AlertDialog.Builder(this);
        adPositive.setTitle("Good Job!")
                .setMessage("You selected the correct answer: "+ crct_answer+"!")
                .setCancelable(false)
                .setPositiveButton("Keep Going :)", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clear_everything();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Quit :(", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //mp.release();
                        finish();
                    }
                });
        adPositive.show();*/
    }

    // if user selects wrong_answer
    public void wrong_answer(){
        cdTImer_running = false;
        streak = 0;
        question_string = etQn.getText().toString();
        cdTImer.cancel();
        mp.create(this,answer_sounds[0]);
        mp.start();
        mp.reset();
        vibrate_if_wrong_timeup();
        tvScore.setText("Score: "+current_score+"/"+no_of_questions);
        tvStreak.setText("Streak: "+streak);
        crct_rbutton.setBackgroundColor(getResources().getColor(R.color.crct_answer));
        buContinue.setVisibility(View.VISIBLE);
        //clear_everything();

        // open negative alert dialog
        /*AlertDialog.Builder adNegative = new AlertDialog.Builder(this);
        adNegative.setTitle("Oops...")
                .setMessage("You selected the wrong answer :( \n"+"The correct answer was"+ crct_answer+".")
                .setCancelable(false)
                .setPositiveButton("Try again :)", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clear_everything();

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Give Up :(", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // mp.release();
                        finish();
                    }
                });
        adNegative.show();*/


    }

    public void timeup(){
        cdTImer_running = false;
        question_string = etQn.getText().toString();
        streak = 0;
        tvStreak.setText("Streak: "+streak);
        tvScore.setText("Score: "+current_score+"/"+no_of_questions);
        vibrate_if_wrong_timeup();
        buContinue.setVisibility(View.VISIBLE);
        crct_rbutton.setBackgroundColor(getResources().getColor(R.color.crct_answer));
        disable_radiogroup();
        //clear_everything();

        // open timeup dialog box
        /*AlertDialog.Builder adTimeup = new AlertDialog.Builder(this);
        adTimeup.setTitle("Oops...")
                .setMessage("Uh-oh... time's up :( \n"+"The correct answer was"+ crct_answer+".")
                .setCancelable(false)
                .setPositiveButton("Try again :)", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        clear_everything();
                        dialogInterface.dismiss();

                    }
                })
                .setNegativeButton("Give Up :(", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //mp.release();
                        finish();
                    }
                });
        adTimeup.show();

         */
    }

    public void clear_everything(){
        less_factors = false;
        enable_clearet_bu();
        reset_timer();
        clear_radiogrp();
        disable_radiogroup();
        buContinue.setVisibility(View.INVISIBLE);
        question_string = etQn.getText().toString();
    }

    public void clear_everything_leave_timer(){
        less_factors = false;
        enable_clearet_bu();
        clear_radiogrp();
        disable_radiogroup();
    }


    public void reset_timer(){
        timeleftinmills = START_TIME_IN_MILLIS;
        update_cdTimer();
    }

    public void pause_timer(){
        cdTImer.cancel();
        cdTImer_running = false;
    }

    public void enter_bigger_number(){

    }

    public void question_is_six(){
    ArrayList<Integer> options_for_six = new ArrayList<Integer>();{
            options_for_six.add(4);
            options_for_six.add(5);
        }

        tvFramedQuestion.setText("The factor of "+question+" is :");
        factors_of_qn.clear();
        dummy_answers.clear();
        Log.d("srart game ", "start_game: done ");
        enable_radiogroup();
        no_of_questions += 1;
        disable_et_bu();
        find_factors(question);
        start_timer();
        
        Random r_crct_factor = new Random();
        int r_crct_factor_ind = r_crct_factor.nextInt(factors_of_qn.size());
        int crct_factor_six = factors_of_qn.get(r_crct_factor_ind);
        options_for_six.add(crct_factor_six);
        Collections.shuffle(options_for_six);

        int crct_factor_six_pos = options_for_six.indexOf(crct_factor_six);

        for(int i = 0; i < 3; i++){
            ((RadioButton) rgOptions.getChildAt(i)).setText(""+options_for_six.get(i));
        }

        crct_rbutton = (RadioButton) rgOptions.getChildAt(crct_factor_six_pos);

        rgOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                disable_radiogroup();
                int rb_selected_id = rgOptions.getCheckedRadioButtonId();
                RadioButton selected_rb = findViewById(rb_selected_id);
                if(selected_rb == crct_rbutton){
                    correct_answer();
                }
                else{
                    wrong_answer();
                    selected_rb.setBackgroundColor(getResources().getColor(R.color.wrong_answer));
                }
            }
        });
    }
        

    public void check_if_high_score(){
        Log.d("HIGH", "high_score = "+high_score);
        if(streak > high_score){
            high_score = streak;
            final AlertDialog.Builder ad_new_high = new AlertDialog.Builder(this);
            ad_new_high.setTitle("Nice!")
                    .setMessage("New High Score : "+high_score+" !\n")
                    .setCancelable(false)
                    .setPositiveButton("Keep Going!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            ad_new_high.show();
           tvHighScore.setText("High Score: "+high_score);
           saveData();

        }
    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(SCORE,high_score);

        editor.commit();
    }

    @Override
    protected void onDestroy() {
        saveData();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(TextUtils.isEmpty(question_string)){
            AlertDialog.Builder adQuit = new AlertDialog.Builder(this);
            adQuit.setTitle("Quit?")
                    .setMessage("Are you sure you want to quit?\nYou will lose your streak")
                    .setCancelable(false)
                    .setPositiveButton("Keep Going", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("Timer", ":" + cdTImer_running);
                            Log.d("Timeleft", ": " + timeleftinmills);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //mp.release();
                            streak = 0;
                            Log.d("gayme", "Gayme is kil ");
                            int result = high_score;
                            Intent result_intent = new Intent();
                            result_intent.putExtra("high_score",result);
                            Log.d("destroy", "high_score ="+result_intent.getIntExtra("high_score",0));
                            setResult(RESULT_OK, result_intent);
                            finish();
                        }
                    });
            adQuit.show();
        }
        else {
            if (cdTImer_running) {
                pause_timer();
            }
            AlertDialog.Builder adQuit = new AlertDialog.Builder(this);
            adQuit.setTitle("Quit?")
                    .setMessage("Are you sure you want to quit?\nYou will lose your streak")
                    .setCancelable(false)
                    .setPositiveButton("Keep Going", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("Timer", ":" + cdTImer_running);
                            Log.d("Timeleft", ": " + timeleftinmills);
                            if (!cdTImer_running) {
                                dialogInterface.dismiss();
                                start_timer();
                            } else {
                                clear_everything_leave_timer();
                            }
                        }
                    })
                    .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //mp.release();
                            streak = 0;
                            Log.d("gayme", "Gayme is kil ");
                            int result = high_score;
                            Intent result_intent = new Intent();
                            result_intent.putExtra("high_score",result);
                            Log.d("destroy", "high_score ="+result_intent.getIntExtra("high_score",0));
                            setResult(RESULT_OK, result_intent);
                            finish();
                        }
                    });
            adQuit.show();
        }
    }
}
