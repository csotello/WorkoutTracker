package com.example.myapplication;

import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.androidnetworking.AndroidNetworking;
import com.google.android.material.navigation.NavigationView;
import com.jacksonandroidnetworking.JacksonParserFactory;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private DrawerLayout drawer;
    Toolbar toolbar;
    private DB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.setParserFactory(new JacksonParserFactory());
        db = new DB(getApplicationContext());
        Log.d(TAG, "onCreate: DB created:" + db.getArray());
        //TODO:Cache DB Array to SharePreference and/or file on start
        setContentView(R.layout.activity_main);
        initToolbar();
        NavigationView navigationView = findViewById(R.id.navDrawer);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void CreateView(View view) {
        Intent intent = new Intent(this, CreateActivity.class);
        startActivity(intent);
    }

    public void ViewView(View view) throws InterruptedException {
        Intent intent = new Intent(this, ViewActivity.class);
        while(!db.ready){ Thread.sleep(1); }//TODO:Add loading animation
        intent.putExtra("array", db.getArray().toString());
        intent.putExtra("page", 0);
        startActivity(intent);
    }

    private void initToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_closed);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected: item:" + item.toString());
        switch (item.getItemId()) {
            case R.id.nav_add:
                Intent intent = new Intent(this, CreateActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
            case R.id.nav_view:
                Intent intent2 = new Intent(this, ViewActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                while(!db.ready){
                    try {
                        Thread.sleep(100);
                        Log.d(TAG, "onNavigationItemSelected: Slept for a little");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }//TODO:Add loading animation
                startActivity(intent2);
                break;
            /*case R.id.nav_weekly:
                Intent intent3 = new Intent(this, WeeklyActivity.class);
                startActivity(intent3);
                break;*/
            default:
                break;
        }
        return true;
    }
}
