package com.pmkap.workoutlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class WeightRepsActivity extends Activity {

	private SQLiteDatabase DB = null;
	private String dayid;
	private Button btnWeightRepsAdd;
	private EditText txtWeight;
	private EditText txtRep;
	private TextView txtWeightRepNoDisplay;

	private ListView lvWeightReps;
	private SimpleAdapter adapter;
	private ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();

	private final String DB_NAME = "WorkoutLog";
	private final String WEIGHTREP_TABLE_NAME = "weightreps";
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.weightreps);
		
	    dayid = getIntent().getStringExtra("dayid");
	    
	    init();
	    displayList();
	    installAdd();
	    
	    registerForContextMenu(lvWeightReps);
	}
	private void installAdd(){
		btnWeightRepsAdd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String weight = txtWeight.getText().toString();
				String rep = txtRep.getText().toString();
				if(weight == null || weight.length() == 0 || rep == null || rep.length() == 0){
					return;
				}
				DB.execSQL("INSERT INTO " +
			        		WEIGHTREP_TABLE_NAME +
			    			" Values ("+weight+","+rep+","+dayid+");");
				displayList();
				txtWeight.setText("");
				txtRep.setText("");
				txtWeight.setFocusable(true);
				txtWeight.requestFocus();
			}
		});
	}
	private void init(){
    	DB =  this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        
    	lvWeightReps = (ListView)findViewById(R.id.lvWeightReps);
		String[] from = { "weight", "rep", "weightrepid"};
		int[] to = { R.id.weightrep_weight, R.id.weightrep_rep, R.id.weightrep_id };
		adapter = new SimpleAdapter (this, list, R.layout.weightrep_item_1, from, to);
		lvWeightReps.setAdapter(adapter);
		
		btnWeightRepsAdd = (Button)findViewById(R.id.btnWeightRepsAdd);
		txtWeight = (EditText)findViewById(R.id.txtWeight);
		txtRep = (EditText)findViewById(R.id.txtRep);
		txtWeightRepNoDisplay = (TextView)findViewById(R.id.txtWeightRepNoDisplay);
    }
	private void displayList(){
                
        String qry = "SELECT rowid,weight,rep FROM " +
        		WEIGHTREP_TABLE_NAME +
    			" where dayid = "+dayid+" ";
        //Toast.makeText(this,qry, Toast.LENGTH_SHORT).show();
        Cursor c = DB.rawQuery(qry, null);
        
        list.clear();
        int recordcount = 0; 
    	if (c != null ) {
    		if  (c.moveToFirst()) {
    			do {
    				recordcount++;
    				String weight = c.getString(c.getColumnIndex("weight"));
    				String rowid = c.getString(c.getColumnIndex("rowid"));
    				String rep = c.getString(c.getColumnIndex("rep"));
    				HashMap<String, String> item = new HashMap<String, String>();
    				item.put("weight", weight);
    				item.put("weightrepid", rowid);
    				item.put("rep", rep);
    				list.add(item);
    				adapter.notifyDataSetChanged();
    			}while (c.moveToNext());
    		} 
    	}
    	if(recordcount>0){
    		txtWeightRepNoDisplay.setVisibility(View.GONE);
    		lvWeightReps.setVisibility(View.VISIBLE);
    	}
    	else{
    		txtWeightRepNoDisplay.setVisibility(View.VISIBLE);
    		lvWeightReps.setVisibility(View.GONE);
    	}
    }
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId()==R.id.lvWeightReps) {
		    MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.context_workout, menu);
			/*menu.setHeaderTitle("Title");
			for (int i = 0; i<2; i++) {
				menu.add(Menu.NONE, i, i, "hmm");
			}*/
		}
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
		View v = lvWeightReps.getChildAt(pos);
		TextView txtId = (TextView)v.findViewById(R.id.weightrep_id);
		String id = txtId.getText().toString();
		DB.execSQL("DELETE FROM " +
        		WEIGHTREP_TABLE_NAME +
    			" where rowid = "+id+";");
		displayList();
	}

}
