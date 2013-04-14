package mobile_simulator.models;

import mobile_simulator.models.TrafficCell.CellType;

public class TrafficCell {
	
	private TrafficLight light;
	public Vehicle vehicle;
	private double [] turnProbabilities;
	private CellType type;
	
	public enum CellType{
		NORMAL (0),
		SOURCE (1),
		SINK (2),
		TRAFFIC_LIGHT (3);
		
		private int value;
		
		CellType(int value){
			this.value = value;
		}
	}
	/*
	 * If a cell is in the front of a lane, it holds a TrafficLight object.
	 * This object is used to see if a vehicle can advance to the next cell
	 * 
	 * Other cells will have a null TrafficLight object
	 */
	
	public TrafficCell(){
		turnProbabilities = new double[4];
	}
	public TrafficCell(CellType type) {
		
		this.type = type;
		turnProbabilities = new double[4];
	}

	public TrafficCell computeNextCell(){
		return null;
	}

	public void computeProbabilities(String directions) {
		
		if( type == CellType.TRAFFIC_LIGHT ){
			/*
			int count = 0;
			for(int i=0;i<directions.length();i++)
				if(directions.charAt(i)=='1')
					count++;
			double prob = (count==0)?0:(1.0/count);
			
			for(int i=0;i<directions.length();i++)
				if(directions.charAt(i)=='1')
					turnProbabilities[i]=prob;
			*/
			
		}else{
			for(int i=0;i<turnProbabilities.length;i++)
				turnProbabilities[i]=0;
		}
		
		
	}
	public String toString(){
		return type.value+"";
	}
}
