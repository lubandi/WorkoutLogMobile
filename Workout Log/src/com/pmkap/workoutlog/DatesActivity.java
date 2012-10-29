package com.pmkap.workoutlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class DatesActivity extends Activity {

	private SQLiteDatabase DB = null;
	private String workoutid;
	private Button btnSetAdd;
	private TextView txtSetNoDisplay;

	private ListView lvSets;
	private SimpleAdapter adapter;
	private ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
	
	private final String DB_NAME = "WorkoutLog";
	private final String SETS_TABLE_NAME = "sets";
	private final String WEIGHTREP_TABLE_NAME = "weightreps";
	public void onResume() {
		super.onResume();
		displayList();
	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.dates);
	
	    workoutid = getIntent().getStringExtra("workoutid");
	    
	    init();
	    
	    displayList();

	    installListClick();
	    
	    installAdd();
	    
	    registerForContextMenu(lvSets);
	    
	    
	}
	private void installAdd(){
		btnSetAdd = (Button)findViewById(R.id.btnSetAdd);
		btnSetAdd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				DB.execSQL("INSERT INTO " +
		        		SETS_TABLE_NAME +
		    			" Values (strftime('%s','now'),"+workoutid+");");
				displayList();
			}
		});
	}
	private void installListClick(){
		lvSets.setOnItemClickListener(new OnItemClickListener(){
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			String dayid = ((TextView)view.findViewById(R.id.date_dayid)).getText().toString();
    			Intent myIntent = new Intent(DatesActivity.this,WeightRepsActivity.class);
    	        myIntent.putExtra("dayid",dayid);
    	        DatesActivity.this.startActivity(myIntent);
    		}
    	});
	}
	private void init(){
    	DB =  this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        
    	/* SETS */
    	
        DB.execSQL("CREATE TABLE IF NOT EXISTS " +
        		SETS_TABLE_NAME +
    			" (dat INT, workoutid int);");
        
        /*DB.execSQL("DELETE FROM " + SETS_TABLE_NAME);
        
        DB.execSQL("INSERT INTO " +
        		SETS_TABLE_NAME +
    			" Values (strftime('%s','now'),1);");*/
        
        /* WEIGHT REP */
        
        DB.execSQL("CREATE TABLE IF NOT EXISTS " +
        		WEIGHTREP_TABLE_NAME +
    			" (weight REAL, rep int, dayid int);");
        
        /*DB.execSQL("DELETE FROM " + WEIGHTREP_TABLE_NAME);
        
        DB.execSQL("INSERT INTO " +
        		WEIGHTREP_TABLE_NAME +
    			" Values (10,15,1);");
        
        DB.execSQL("INSERT INTO " +
        		WEIGHTREP_TABLE_NAME +
    			" Values (12.5,10,1);");
        
        DB.execSQL("INSERT INTO " +
        		WEIGHTREP_TABLE_NAME +
    			" Values (15,6,1);");*/

        lvSets = (ListView)findViewById(R.id.lvSets);
		String[] from = { "date", "weightreps", "dayid"};
		int[] to = { R.id.date_name, R.id.date_weightreps, R.id.date_dayid };
		adapter = new SimpleAdapter (this, list, R.layout.dates_item_1, from, to);
		lvSets.setAdapter(adapter);
		
		txtSetNoDisplay = (TextView)findViewById(R.id.txtSetNoDisplay);
		txtSetNoDisplay.setVisibility(View.VISIBLE);
		lvSets.setVisibility(View.GONE);
        
    }
	private void displayList(){
                
        String qry = "SELECT rowid,date(dat,'unixepoch') as date FROM " +
        		SETS_TABLE_NAME +
    			" where workoutid = "+workoutid+" "+
        		"order by rowid desc";
        Cursor c = DB.rawQuery(qry, null);
        
        list.clear();
        int recordcount = 0;
    	if (c != null ) {
    		
    		if  (c.moveToFirst()) {
    			do {
    				recordcount++;
    				
    				String date = c.getString(c.getColumnIndex("date"));
    				String dayid = c.getString(c.getColumnIndex("rowid"));
    				
    				String qry2 = "SELECT weight,rep FROM " +
    						WEIGHTREP_TABLE_NAME +
    		    			" where dayid = "+dayid;
    		        Cursor c2 = DB.rawQuery(qry2, null);
    		        String Weightreps = "";
    		        if (c2 != null ) {
    		    		if  (c2.moveToFirst()) {
    		    			do {
    		    				Weightreps += " " + c2.getString(c2.getColumnIndex("weight"));
    		    				Weightreps += "x" + c2.getString(c2.getColumnIndex("rep"));
    		    			}while (c2.moveToNext());
    		    		} 
    		        }
    				
    				HashMap<String, String> item = new HashMap<String, String>();
    				item.put("date", date);
    				item.put("weightreps", Weightreps);
    				item.put("dayid", dayid);
    				list.add(item);
    				adapter.notifyDataSetChanged();
    			}while (c.moveToNext());
    		} 
    	}
    	if(recordcount>0){
    		txtSetNoDisplay.setVisibility(View.GONE);
    		lvSets.setVisibility(View.VISIBLE);
    	}
    	else{
    		txtSetNoDisplay.setVisibility(View.VISIBLE);
    		lvSets.setVisibility(View.GONE);
    	}
    }
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.context_workout, menu);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
	            .getMenuInfo();
	 
	    switch (item.getItemId()) {
	    case R.id.context_workout_delete:
	        remove(info.position);
	        return true;
	    }
	    return false;
	}
	private void remove(int pos){
		View v = lvSets.getChildAt(pos);
		TextView txtId = (TextView)v.findViewById(R.id.date_dayid);
		String id = txtId.getText().toString();
		DB.execSQL("DELETE FROM " +
				SETS_TABLE_NAME +
    			" where rowid = "+id+";");
		DB.execSQL("DELETE FROM " +
				WEIGHTREP_TABLE_NAME +
    			" where dayid = "+id+";");
		
		displayList();
	}
}
