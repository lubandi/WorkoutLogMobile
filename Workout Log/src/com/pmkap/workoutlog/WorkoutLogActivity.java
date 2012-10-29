package com.pmkap.workoutlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class WorkoutLogActivity extends Activity{
	private char cSwitchGroup = '\0';
	private SQLiteDatabase DB = null;
	private EditText txtWorkoutAdd;
	private Button btnWorkoutAdd;
	private Button btnWorkoutChest;
	private Button btnWorkoutArms;
	private Button btnWorkoutTriceps;
	private Button btnWorkoutLats;
	private Button btnWorkoutBack;
	private Button btnWorkoutLegs;
	private Button btnWorkoutAbs;
	private Button btnWorkoutShoulder;
	private TextView txtWorkoutNoDisplay;
	
	private Dialog dialog;
	private String sEditWorkoutDialogId;
	private EditText etEditWorkoutDialogWorkout;
	private Spinner spnEditWorkoutDialogMuscleGroup;
	private Button btnEditWorkoutDialogSave;
	private String MuscleGroupValues [];
	
	private ListView lvWorkouts;
	private SimpleAdapter adapter;
	private ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
	
	private final String DB_NAME = "WorkoutLog";
	private final String WORKOUTS_TABLE_NAME = "workouts";
	private final String SETS_TABLE_NAME = "sets";
	private final String WEIGHTREP_TABLE_NAME = "weightreps";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workouts);
        
        init();
        
        displayList('C');
        
        installAdd();
        
        installCore();
        
        installListClick();
        
	    registerForContextMenu(lvWorkouts);
        
    }
    
    private void installListClick(){
    	lvWorkouts.setOnItemClickListener(new OnItemClickListener(){
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			String workoutid = ((TextView)view.findViewById(R.id.workout_rowid)).getText().toString();
    			
    			Intent myIntent = new Intent(WorkoutLogActivity.this,DatesActivity.class);
    	        myIntent.putExtra("workoutid",workoutid);
    	        WorkoutLogActivity.this.startActivity(myIntent);
    		}
    	});
    }
    
    private void installCore(){
    	
    	btnWorkoutChest = (Button)findViewById(R.id.btnWorkoutChest);
    	btnWorkoutArms = (Button)findViewById(R.id.btnWorkoutArms);
    	btnWorkoutBack = (Button)findViewById(R.id.btnWorkoutBack);
    	btnWorkoutLegs = (Button)findViewById(R.id.btnWorkoutLegs);
    	btnWorkoutAbs = (Button)findViewById(R.id.btnWorkoutAbs);
    	btnWorkoutShoulder = (Button)findViewById(R.id.btnWorkoutShoulder);
    	btnWorkoutTriceps = (Button)findViewById(R.id.btnWorkoutTriceps);
		btnWorkoutLats = (Button)findViewById(R.id.btnWorkoutLats);
		
    	btnWorkoutChest.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				displayList('C');
			}
		});
    	btnWorkoutArms.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				displayList('A');
			}
		});
    	btnWorkoutBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				displayList('B');
			}
		});
    	btnWorkoutLegs.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				displayList('L');
			}
		});
    	btnWorkoutAbs.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				displayList('S');
			}
		});
    	btnWorkoutShoulder.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				displayList('H');
			}
		});
    	btnWorkoutTriceps.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				displayList('P');
			}
		});
    	btnWorkoutLats.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				displayList('T');
			}
		});
    }
    
    private void installAdd(){
    	 txtWorkoutAdd = (EditText)findViewById(R.id.txtWorkoutAdd);
         btnWorkoutAdd = (Button)findViewById(R.id.btnWorkoutAdd);
         btnWorkoutAdd.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				String workout = txtWorkoutAdd.getText().toString();
 				if(workout == null || workout.length() == 0){
 					return;
 				}
				workout = DatabaseUtils.sqlEscapeString(workout);
				DB.execSQL("INSERT INTO " +
		        		WORKOUTS_TABLE_NAME +
		    			" Values ("+workout+",'"+cSwitchGroup+"');");	
				displayList();
 				txtWorkoutAdd.setText("");
 			}
 		});
    }
    
    private void init(){
    	DB =  this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        
        DB.execSQL("CREATE TABLE IF NOT EXISTS " +
        		WORKOUTS_TABLE_NAME +
    			" (WorkoutName VARCHAR, MuscleGroupCode char(1));");
        
        txtWorkoutNoDisplay = (TextView)findViewById(R.id.txtWorkoutNoDisplay);

        lvWorkouts = (ListView)findViewById(R.id.lvWorkouts);
		String[] from = { "workout_name", "workout_rowid"};
		int[] to = { R.id.workout_name, R.id.workout_rowid };
		adapter = new SimpleAdapter (this, list, R.layout.workout_item_1, from, to);
		lvWorkouts.setAdapter(adapter);
        
    }
    
    private void displayList(char c){
    	cSwitchGroup = c;
    	displayList();
    }
    
    public void displayList(){

        lvWorkouts.setVisibility(View.VISIBLE);
                
        String qry = "SELECT rowid,WorkoutName FROM " +
        		WORKOUTS_TABLE_NAME +
    			" where MuscleGroupCode = '"+cSwitchGroup+"' "+
        		"order by WorkoutName";
        
        Cursor c = DB.rawQuery(qry, null);
        
        list.clear();

        int recordcount = 0;
    	if (c != null ) {
    		if  (c.moveToFirst()) {
    			do {
    				recordcount++;
    				String WorkoutName = c.getString(c.getColumnIndex("WorkoutName"));
    				String rowid = c.getString(c.getColumnIndex("rowid"));
    				HashMap<String, String> item = new HashMap<String, String>();
    				item.put("workout_name", WorkoutName);
    				item.put("workout_rowid", rowid);
    				list.add(item);
    				adapter.notifyDataSetChanged();
    			}while (c.moveToNext());
    		} 
    	}
    	if(recordcount>0){
    		txtWorkoutNoDisplay.setVisibility(View.GONE);
    		lvWorkouts.setVisibility(View.VISIBLE);
    	}
    	else{
    		txtWorkoutNoDisplay.setVisibility(View.VISIBLE);
    		lvWorkouts.setVisibility(View.GONE);
    	}
    	
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.context_workouts, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
	            .getMenuInfo();
	 
	    switch (item.getItemId()) {
	    case R.id.context_workouts_delete:
	        remove(info.position);
	        return true;
	    case R.id.context_workouts_edit:
	        edit(info.position);
	        return true;
	    }
	    return false;
	}
	
	private void edit(int pos){
		//fetch id
		View v = lvWorkouts.getChildAt(pos);
		TextView txtId = (TextView)v.findViewById(R.id.workout_rowid);
		TextView txtWorkout = (TextView)v.findViewById(R.id.workout_name);
		sEditWorkoutDialogId = txtId.getText().toString();
		String workout_name_initial = txtWorkout.getText().toString();
		String musclegroup_initial = String.valueOf(cSwitchGroup);
		
		//display menu
		dialog = new Dialog(WorkoutLogActivity.this);

		dialog.setContentView(R.layout.custom_dialog);
		dialog.setTitle("Edit Workout");

		etEditWorkoutDialogWorkout = (EditText) dialog.findViewById(R.id.etEditWorkoutDialogWorkout);
		spnEditWorkoutDialogMuscleGroup = (Spinner) dialog.findViewById(R.id.spnEditWorkoutDialogMuscleGroup);
		btnEditWorkoutDialogSave = (Button) dialog.findViewById(R.id.btnEditWorkoutDialogSave);
		MuscleGroupValues =  getResources().getStringArray(R.array.MuscleGroupValues);
		
		for(int i=0;i<MuscleGroupValues.length;i++){
			if(MuscleGroupValues[i].equalsIgnoreCase(musclegroup_initial)){
				/*COMPARING HARDCODED VALUES ON WORKOUTLOGACTIVITY.JAVA to STRINGS.XML,
				 * THERE MUST BE DEFINITE MATCH*/
				spnEditWorkoutDialogMuscleGroup.setSelection(i);
			}
		}
		etEditWorkoutDialogWorkout.setText(workout_name_initial);
		
		btnEditWorkoutDialogSave.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String workout = etEditWorkoutDialogWorkout.getText().toString();
				String musclegroupcode = MuscleGroupValues[spnEditWorkoutDialogMuscleGroup.getSelectedItemPosition()];
				if(workout == null || workout.length() == 0){
 					return;
 				}
				/*PROCESS*/
				DB.execSQL("update " +
						WORKOUTS_TABLE_NAME +
		    			" set WorkoutName='"+workout+"',MuscleGroupCode='"+musclegroupcode+"' where rowid="+sEditWorkoutDialogId+";");
				
				/*CLOSE DIALOG*/
				dialog.dismiss();
				
				/*UPDATE ACTIVITY*/
				displayList();
			}
		});
        dialog.show();
	}
	
	private void remove(int pos){
		View v = lvWorkouts.getChildAt(pos);
		TextView txtId = (TextView)v.findViewById(R.id.workout_rowid);
		String id = txtId.getText().toString();
		
		DB.execSQL("delete from " +
				WEIGHTREP_TABLE_NAME +
    			" where dayid in (select rowid from sets where workoutid = "+id+");");
		DB.execSQL("delete from " +
				SETS_TABLE_NAME +
    			" where workoutid = "+id+";");
		DB.execSQL("delete from " +
				WORKOUTS_TABLE_NAME +
    			" where rowid = "+id+";");
		displayList();
	}
}