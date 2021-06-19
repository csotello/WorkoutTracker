package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static android.graphics.Color.parseColor;


public class TableFragment extends Fragment {
    private static final String TAG = "TableFragment";
    public static TableLayout table;
    public interface GetSchedule{
        void getSchedule(JsonObject sched);
    }
    private GetSchedule getSchedule;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        int weeks = 0;
        String program = null;
        if(getArguments() != null) {
            Log.d(TAG, "onCreateView: Argument Retrieved");
            Bundle args = getArguments();
            weeks = Integer.parseInt(args.getString("weeks"));
            program = args.getString("sched");
        }
        View view = inflater.inflate(R.layout.table_fragment, container, false);
        table = view.findViewById(R.id.table);
        buildTable(table,weeks, program);

        return view;
    }

    /**
     * Builds a table to view or input a workout.
     * @param table TableLayout to add rows to
     * @param weeks Number of weeks(rows) in the table
     */
    private void buildTable(TableLayout table, int weeks, String program) {
        Log.d(TAG, "buildTable: started");
        JsonObject obj = null; //JSON element for building table from DB
        if(program != null) {//if there is a program string to parse
            JsonParser parser = new JsonParser();
            obj = parser.parse(program).getAsJsonObject();
            Log.d(TAG, "buildTable:sched value:"+obj.toString());
        }
        TableRow head = new TableRow(getContext());
        TextView blank = new TextView(getContext());
        blank.setMinWidth(15);
        blank.setText("              ");
        head.addView(blank);
        for(int i = 1; i <= 7; i++){//Top row of table is a title for each day
            TextView title = new TextView(getContext());
            title.setText(new StringBuilder().append("Day:").append(i));
            title.setTextSize(14);
            title.setPadding(5,0,5,-2);
            title.setGravity(Gravity.CENTER);
            head.addView(title);
        }
        table.addView(head);
        for (int i = 1; i <= weeks; i++){
            TableRow row = new TableRow(getContext());
            TextView week = new TextView(getContext());
            week.setText(new StringBuilder().append("Week:").append(i));
            week.setWidth(75);
            week.setPadding(5,5,5,5);
            week.setLeftTopRightBottom(1,10,1,1);
            week.setLayoutParams( new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            row.addView(week);
            for(int j = 1; j <= 7; j++){
                TextView day;
                if(obj != null){//if table is for ViewActivity
                    day = new TextView(getContext());
                }else{//table is for TableActivity
                    day = new EditText(getContext());
                }
                //EditText day = new EditText(getContext());
                day.setWidth(75);
                day.setHeight(50);
                day.setMinHeight(50);
                day.setTextSize(10);
                day.setPadding(5,5,5,5);
                day.setTag(new StringBuilder("week").append(i).append("day").append(j).toString());
                day.setLayoutParams( new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
                if(day instanceof TextView && obj != null && obj.has(day.getTag().toString())) { //Table is for ViewActivity
                    JsonObject current = obj.getAsJsonObject(day.getTag().toString());
                    day.setText(current.get("name").toString().replace('"',' '));
                    Log.d(TAG, "buildTable: finished:" + current.get("Finished").toString());
                    if(current.get("Finished") != null && current.get("Finished").getAsBoolean()) {
                        Log.d(TAG, "buildTable: Detected finished workout");
                        day.setBackgroundColor(parseColor("green"));
                        day.setTextColor(parseColor("black"));
                        day.setClickable(false);
                    }
                    day.setClickable(true);
                    JsonObject finalObj = obj;
                    DB db = new DB(getContext());
                    day.setOnClickListener(view -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Would you like to mark this workout as finished?");
                        builder.setPositiveButton(R.string.yes, (dialogInterface, i1) -> {
                            JsonObject curDay = finalObj.getAsJsonObject(day.getTag().toString());
                            int weekNum = curDay.get("Week").getAsInt();
                            int dayNum = curDay.get("Day").getAsInt();
                            db.finish(current.get("name").toString(), dayNum, weekNum);
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        });
                        builder.setNegativeButton(R.string.no, (dialogInterface, i1) -> dialogInterface.dismiss());
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    });
                }
                row.addView(day);
            }
            table.addView(row);
        }
        Log.d(TAG, "buildTable: returning");
    }

    /**
     * Return the TableLayout View in the fragment
     * @return TableLayout View
     */
    public static TableLayout getTable(){return table;}


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
