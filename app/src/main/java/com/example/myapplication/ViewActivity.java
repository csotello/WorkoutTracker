package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class ViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "ViewActivity";
    private ImageButton rightArrow;
    private ImageButton leftArrow;
    private JSONArray array;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_view);
        Intent intent = getIntent();
        String array = intent.getExtras().getString("array");
        Integer page = intent.getExtras().getInt("page");
        JSONArray programArray;
        if (array != null && !(array.length() == 0)) {
            Log.d(TAG, "onCreate: Array:" + array);
            try {
                setContentView(R.layout.activity_view);
                TextView name = findViewById(R.id.name);
                TextView weeks = findViewById(R.id.weeks);
                rightArrow = findViewById(R.id.right_arrow);
                leftArrow = findViewById(R.id.left_arrow);
                programArray = new JSONArray(array);
                JSONObject program = (programArray.length() > 0 && page < programArray.length() ? programArray.getJSONObject(page) : null);
                Log.d(TAG, "onCreate: Array Lenght:" + programArray.length() + ", page#:" + page);
                if(page == 0) leftArrow.setVisibility(View.GONE);
                if(programArray.length() == 0 || page >= programArray.length() - 1) rightArrow.setVisibility(View.GONE);
                rightArrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ViewActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("array", array.toString());
                        intent.putExtra("page", page + 1);
                        startActivity(intent);
                    }
                });
                leftArrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ViewActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("array", array.toString());
                        intent.putExtra("page", page - 1);
                        startActivity(intent);
                    }
                });
                Log.d(TAG, "array:" + array);
                if (program != null) {
                    name.setText(program.getString("Name"));
                    weeks.setText(program.getString("weeks"));
                    JSONObject sched = program.getJSONObject("sched");
                    Button btn = findViewById(R.id.deletebtn);
                    btn.setOnClickListener(view -> new AlertDialog.Builder(ViewActivity.this)
                            .setMessage("Are you sure you want to delete this program?")
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                DB db = new DB(ViewActivity.this);
                                try {
                                    db.delete(program.getString("Name"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent1);
                            })
                            .setCancelable(false)
                            .show());
                    Bundle bundle = new Bundle();
                    bundle.putString("sched", sched.toString());
                    bundle.putString("name", program.getString("Name"));
                    bundle.putString("weeks", program.getString("weeks"));
                    TableFragment fragment = new TableFragment();
                    fragment.setArguments(bundle);
                    FragmentManager manager = getSupportFragmentManager();
                    int transaction = manager.beginTransaction()
                            .replace(R.id.tableFragment2, fragment)
                            .commit();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            Log.d(TAG, "onCreate: Toast ");
            Toast.makeText(getApplicationContext(),"Add a workout to have it appear here",Toast.LENGTH_SHORT).show();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_closed);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navDrawer);
        navigationView.setNavigationItemSelectedListener(this);
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
//                Intent intent1 = new Intent(this, ViewActivity.class);
//                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                intent1.putExtra("array", array != null ? array.toString() : "");
//                intent1.putExtra("page", 0);
//                startActivity(intent1);
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

