package mobile_simulator.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import mobile_simulator.models.TrafficCell;

public class GraphicsHelper {

	public GraphicsHelper() {
		// TODO Auto-generated constructor stub
	}
	
	// this function should be called by a graphics object which has access to the
	// grid of traffic cells in the MobileSimulation object
	public static Bitmap CreateBitmapFromGridOfCells(TrafficCell [][] i_grid)
	{
		int t_width = i_grid.length;
		int t_height = 0;
		if(i_grid[0]!=null)
			t_height = i_grid[0].length;
		
		Bitmap.Config t_conf = Bitmap.Config.ARGB_8888;
		Bitmap t_bitmap = Bitmap.createBitmap(t_width, t_height, t_conf);
		
		//this is a slow method
		//TODO: change this to setPixels or to copyPixelsFrom...
		for(int i=0;i<t_width;i++)
		{
			for(int j=0;j<t_height;j++)
			{
				t_bitmap.setPixel(i, j, CalulateColorFromTrafficCell(i_grid[i][j],0));
			}
		}
		return t_bitmap;
	}

	// this method is used by CreateBitmapFromGridOfCells to define several modes of
	// mapping the traffic cells into color
	private static int CalulateColorFromTrafficCell(TrafficCell i_cell, int i_mode)
	{
		int t_color = 0;
		//byte t_alpha = 0;
		//byte t_red = 0;
		//byte t_green = 0;
		//byte t_blue = 0;
		//t_color = Color.argb(t_alpha, t_red, t_green, t_blue);
		t_color = Color.BLACK;
		
		switch(i_mode)
		{
		case 0:
		switch(i_cell.type)
		{
			case NORMAL:
				t_color = Color.GRAY;
			break;
			case SINK:
				t_color = Color.RED;
			break;
			case SOURCE:
				t_color = Color.BLUE;
				break;
			case TRAFFIC_LIGHT:
				t_color = Color.YELLOW;
				break;
			default:
				t_color = Color.BLACK;
				break;
		}
		break;
		}
		
		return t_color;
	}
}
