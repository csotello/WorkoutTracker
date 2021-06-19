package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONException;
import org.json.JSONObject;


public class InputTableActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "InputTableActivity";
    private DB db;
    private TextView name;
    private TextView weeks;
    private Button addbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_table);
        db = new DB(this);
        name =  findViewById(R.id.name);
        weeks =  findViewById(R.id.weeks);
        addbtn =  findViewById(R.id.addbtn);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null && !bundle.getString("name").equals("") && !bundle.getString("weeks").equals("")) {
            name.setText(bundle.getString("name"));
            weeks.setText(bundle.getString("weeks"));
            TableFragment fragment = new TableFragment();
            fragment.setArguments(bundle);
            FragmentManager manager = getSupportFragmentManager();
            int transaction = manager.beginTransaction()
                    .replace(R.id.tableFragment, fragment)
                    .commit();
        }
        else{
            Toast.makeText(this, "Please enter the name and number of weeks", Toast.LENGTH_SHORT).show();
        }
        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        toolbar.setTitle("Workout Tracker");
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_closed);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.navDrawer);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Adds a workout to DB
     */
    public void AddWorkout(View view) throws JSONException {
        Log.d(TAG, "AddWorkout: started");
        TableLayout table = TableFragment.getTable();
        JSONObject program = new JSONObject();
        JSONObject sched = new JSONObject();
        program.put("Name",name.getText().toString());
        program.put("Weeks",Integer.parseInt(weeks.getText().toString()));
        for(int i = 1; i < table.getChildCount(); i++){//loop through each row(week) in the table
            TableRow row = (TableRow) table.getChildAt(i);
            for(int j = 1; j < row.getChildCount(); j++){//Each day in the week
                TextView cell = (TextView) row.getChildAt(j);
                String workout = cell.getText().toString();
                if(!workout.equals("")){
                    JSONObject day = new JSONObject();
                    day.put("name",workout);
                    day.put("Week", i);
                    day.put("Day", j);
                    sched.putOpt(cell.getTag().toString(),day);
                }
            }
        }
            program.putOpt("sched", sched);
        try{
            Log.d(TAG, "Input:" + program.toString());
            DB db = new DB(getApplicationContext());
            db.add(program);
        }catch (NullPointerException e){
            Log.d(TAG, "AddWorkout: NullPointer" + program);
        }
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected: item:" + item.toString());
        switch (item.getItemId()) {
            case R.id.nav_add:
                Intent intent = new Intent(this, CreateActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_view:
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
