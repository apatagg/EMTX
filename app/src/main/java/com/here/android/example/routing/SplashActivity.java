package com.here.android.example.routing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import static java.lang.Thread.sleep;

public abstract class SplashActivity extends AppCompatActivity implements Runnable {

    private boolean isAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        new Thread(this).start();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        isAvailable = true;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        isAvailable = false;
    }

    @Override
    public void run()
    {
        try { sleep(getSplashTime()); } catch (Exception e) { isAvailable = false; handleError(e); }
        if (isAvailable) changeActivity();
    }

    private void changeActivity()
    {
        Intent intent = new Intent(this, getNextActivity());
        startActivity(intent); finish();
    }

    abstract int getSplashTime();

    abstract Class<? extends AppCompatActivity> getNextActivity();

    abstract void handleError(Exception e);
}