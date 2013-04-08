package mobile_simulator.activities;

import mobile_simulator.models.MobileSimulation;

import com.example.mobile_simulator.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;;

public class MainActivity extends Activity {

	private TextView tv;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        tv = (TextView)this.findViewById(R.id.textView);
    	MobileSimulation sim = new MobileSimulation();
    	sim.getData();
    	sim.runSimulation();
    	tv.setText("Done Simulating");

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
