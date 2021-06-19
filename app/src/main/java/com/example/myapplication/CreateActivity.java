package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class CreateActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "CreateActivity";
    private DB db;
    private Button createbtn;
    private TextView name;
    private EditText weeks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        db = new DB(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.include);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_closed);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        createbtn = findViewById(R.id.createbtn);
        name = findViewById(R.id.name);
        weeks = findViewById(R.id.weeks);

        createbtn.setOnClickListener(view -> {
            if(name.getText().toString().equals("") || weeks.getText().toString().equals("")){
                Toast.makeText(this, "Please enter the name and number of weeks", Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent = new Intent(CreateActivity.this, InputTableActivity.class);
                intent.putExtra("name", name.getText().toString());
                intent.putExtra("weeks", weeks.getText().toString());
                startActivity(intent);
            }
        });
        NavigationView navigationView = findViewById(R.id.navDrawer);
        navigationView.setNavigationItemSelectedListener(this);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected: item:" + item.toString());
        switch (item.getItemId()) {
            case R.id.nav_add:
                break;
            case R.id.nav_view://TODO:use shared preference
                Intent intent2 = new Intent(this, ViewActivity.class);
                while(!db.ready){
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }//TODO:Add loading animation
                intent2.putExtra("array", db.getArray().toString());
                intent2.putExtra("page", 0);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
