package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DB {
    private static final String TAG = "DB";
    private final DBInterface DBinterface;
    private JSONArray array = null;
    public boolean ready;
    private static Context context;
    private final Thread thread;
    private final CognitoCachingCredentialsProvider credProvider;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    DB(Context context) {
        ready = false;
        sharedPreferences = context.getSharedPreferences("db.json",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        thread = new Thread(){
            @Override
            public void run() {
                AndroidNetworking.get(DB_API)
                .setTag("test").setPriority(Priority.LOW)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response == null)
                            array = new JSONArray();
                        else {
                            array = response;
                            editor.putString("db",response.toString());
                            editor.apply();
                        }
                        Log.d(TAG, "DB initialized from GET: Array set to:" + array);
                        ready = true;
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "BD error:" + anError.toString());
                    }
                });
            }
        };
        SharedPreferences sharedPreferences = context.getSharedPreferences("db.json",Context.MODE_PRIVATE);
        if(!sharedPreferences.contains("db") && !ready)
            thread.start();
        else {
            Log.d(TAG, "DB: Found data in SharedPref");
            try {
                array = new JSONArray(sharedPreferences.getString("db", ""));
            } catch (JSONException e){
                array = new JSONArray();
            }
            ready = true;

        }
        // Initialize the Amazon Cognito credentials provider
        credProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                IDENTITY_POOL, // Identity pool ID
                Regions.US_EAST_1 // Region
        );
        DBinterface = (DBInterface) LambdaInvokerFactory.builder().context(context).region(Regions.US_EAST_1).credentialsProvider(credProvider).build().build(DBInterface.class);
        this.context = context;
    }
    public JSONArray getArray(){
        if(array != null){Log.d(TAG, "getArray():" + array.toString());}
        return array;
    }

    /**
     * Add a workout to the DB
      * @param program to add
     * @throws JSONException when program has null reference
     */
    public void add(JSONObject program) throws JSONException {
        Log.d(TAG, "Adding Program:" + program);
        DBRequest request = new DBRequest(program.getString("Name"), program.getJSONObject("sched").toString(), program.getInt("Weeks"));
        request.setWeek(program.getInt("Weeks"));
        DB db = new DB(context.getApplicationContext());
        Thread addThread = new Thread(){

            @Override
            public void run() {
                try{
                    DBResponse response = DBinterface.addWorkout(request);
                    Log.d(TAG, "run: Response:" + response.getResponse());
                    db.update(response.getResponse());
                } catch (LambdaFunctionException e){
                    e.printStackTrace();
                    Log.d(TAG, "run: LambdaException:" + e);
                    Log.d(TAG, "run: Exception Details:" + e.getDetails());
                }
            }

        };
        addThread.start();
        while(addThread.isAlive()){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Update the array member after adding.
     * @param response DBResponse as string
     * @throws JSONException when null
     */
    public void update(String response) {
        Log.d(TAG, "update: attempting to update to" + response);
        if(response != null && array != null){
            if(!array.toString().equals(response)){
                try{
                    array = new JSONArray(response);
                }catch (JSONException e){
                    e.printStackTrace();
                    array = null;
                }
                if(array != null) {
                    editor.putString("db", response);
                }
                else{
                    editor.clear();
                }
                editor.apply();
            }
        }
    }

    public void finish(String name, int day, int week) {
        Log.d(TAG, "finished: " + name);
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    DBRequest request = new DBRequest(name, day, week);
                    Log.d(TAG, "run: Sending request:" + name + " " + week + " " + day);
                    DBResponse response = DBinterface.finish(request);
                    Log.d(TAG, "finish: Response:" + response.getResponse());
                    try {
                        array = new JSONArray(response.getResponse());
                    } catch (JSONException e) {
                        array = null;
                        e.printStackTrace();
                    }
                    if(array != null) {
                        editor.putString("db", array.toString());
                    }
                    else{
                        editor.clear();
                    }
                    editor.apply();
                }catch (LambdaFunctionException e){
                    e.printStackTrace();
                    Log.d(TAG, "run: LambdaException Details:" + e.getDetails());
                }
            }
        };
        thread.start();
        while(thread.isAlive()){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void delete(String name){
        Thread thread = new Thread(){
            @Override
            public void run() {
               try{
                   DBRequest request = new DBRequest(name);
                   Log.d(TAG, "DB Delete: Sending request:" + request.toString());
                   DBResponse response = DBinterface.deleteWorkout(request);
                   Log.d(TAG, "DB Delete: Response:" + response.getResponse());
                   try {
                       array = new JSONArray(response.getResponse());
                   } catch (JSONException e) {
                       array = null;
                       e.printStackTrace();
                   }
                   if(array != null) {
                       editor.putString("db", array.toString());
                   }
                   else{
                       editor.clear();
                   }
                       editor.apply();


               } catch (LambdaFunctionException e){
                   e.printStackTrace();
                   Log.d(TAG, "run: Details" + e.getDetails());
               }
            }
        };
        thread.start();
        while(thread.isAlive()){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private class DBRequest {
        String Name;
        String name;
        int Weeks;
        int day;
        int week;
        String sched;
        JsonObject schedObj;
        DBRequest(String program, String schedString, int week){//Request for creating a program
            this.Name = program;
            this.sched = schedString;
            this.Weeks = week;
            //this.schedObj = new JsonParser().parse(schedString).getAsJsonObject();
        }
        DBRequest(String name, int day, int week){//Request for finishing a workout
            this.name = name;
            this.day = day;
            this.week = week;
        }
        DBRequest(String name){//Request for deleting a program
            this.name = name;
        }

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            this.Name = name;
        }

        public void setWeek(int week) {
            this.week = week;
        }
    }

    private class DBResponse {
        String response;
        JSONArray array;
        DBResponse(){}
        DBResponse(String response) throws JSONException {
            Log.d(TAG, "DBResponse: Response Created:" + response);
            this.response = response;
            array = new JSONArray(response);
            Log.d(TAG, "DBResponse: Array Created:" + array.toString());
        }

        public String getResponse() {
            Log.d(TAG, "getResponse: " + response);
            return response;
        }
    }

    public interface DBInterface {
        @LambdaFunction
        DBResponse addWorkout(DBRequest request);

        @LambdaFunction
        DBResponse finish(DBRequest request);

        @LambdaFunction
        DBResponse deleteWorkout(DBRequest request);

    }

}
