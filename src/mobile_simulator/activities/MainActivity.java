package mobile_simulator.activities;

import mobile_simulator.graphics.GraphicsHelper;
import mobile_simulator.models.MobileSimulation;
import mobile_simulator.models.TrafficStatistics;

import com.example.mobile_simulator.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;;

public class MainActivity extends Activity {

	private TextView tv;
	private ImageView m_imageViewMap;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        tv = (TextView)this.findViewById(R.id.textView);
        m_imageViewMap = (ImageView)this.findViewById(R.id.imageViewMap);
    	MobileSimulation sim = new MobileSimulation();    
		final int TICKS = 3600;
		final int NUMBER_OF_INITIAL_CARS = 30;
    	TrafficStatistics stats = sim.runSimulation(TICKS, NUMBER_OF_INITIAL_CARS);
    	tv.setText(stats.getVehicleStats(50));
    	m_imageViewMap.setImageBitmap(GraphicsHelper.CreateBitmapFromGridOfCells(sim.getGridCells(),0,true));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private int dpToPx(int dp)
    {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }
}
