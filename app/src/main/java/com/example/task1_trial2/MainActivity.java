package com.example.task1_trial2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    int high_score;
    TextView tvBestScore;
    static final String BEST_STREAK_KEY = "BEST_STREAK";
    int best_score ;
    static final int REQUEST_CODE = 100;

    Switch dark_mode_menu;
    boolean dark_mode;

    public static final String SHARED_PREFS = "sharedPref";
    public static final String SCORE = "score";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.DARK_MODE);
        }
        else{
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvBestScore = findViewById(R.id.tvBestScore);
        tvBestScore.setText("Best Streak : "+best_score);
        Log.d("created", "best_score: " + best_score);
        loadData();
        dark_mode_menu = findViewById(R.id.switchDarkMode);
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            dark_mode_menu.setChecked(true);
        }
        dark_mode_menu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    saveData();
                    //Log.d("theme_change", "best_sore:"+best_score);
                    restartApp();
                    saveData();
                    Log.d("theme_change", "best_sore:"+best_score);

                }
                else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    saveData();
                    //Log.d("theme_change", "best_sore:"+best_score);
                    restartApp();
                    saveData();
                    Log.d("theme_change", "best_sore:"+best_score);

                }
            }
        });
    }




    public void restartApp(){
        saveData();
       // Log.d("theme_change", "best_sore:"+best_score);
        Intent restart = new Intent(getApplicationContext(),MainActivity.class);
        saveData();
        startActivity(restart);
        saveData();
        finish();
        Log.d("finish", "best_score: "+best_score);

    }



    public void buNewGame(View view) {
        Intent new_game = new Intent(MainActivity.this,Gayme.class);
        new_game.putExtra(BEST_STREAK_KEY,best_score);
        startActivityForResult(new_game,REQUEST_CODE);
        Log.d("NEW", "GAME: "+ best_score);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("req_code", ": "+requestCode);
        Log.d("res_code", ": "+resultCode);
      // Log.d("data", ":"+ data.getIntExtra("high_score",0));
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            if(resultCode == RESULT_OK){
                int check_score = data.getIntExtra("high_score",0);
                Log.d("result", " = "+check_score );
                if(check_score > best_score){
                    best_score = check_score;
                    tvBestScore.setText("Best Streak : "+ best_score);
                    saveData();

                }
            }
        }

    }

    public void buQuit(View view) {
        saveData();
        finish();
    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(SCORE,best_score);

        editor.commit();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);

        best_score = sharedPreferences.getInt(SCORE,0);

        tvBestScore.setText("Best Streak : "+ best_score);
    }

    @Override
    protected void onDestroy() {
        saveData();
        super.onDestroy();
    }
}
