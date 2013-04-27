package mobile_simulator.models;

import java.util.HashMap;

import mobile_simulator.models.MobileSimulation;

public class TrafficCell {
	//---------------------------FIELDS AND ENUMS---------------------------------
	public Vehicle vehicle;
	private double [] turnProbabilities;
	public int row;
	public int col;
	public double vehicleLeaveTime;
	public int street;
	public CellType type;
	private Direction streetDirection;
	public HashMap<Direction,Integer>streetChanges;
	
	public enum CellType{
		EMPTY (0),
		NORMAL (1),
		SOURCE (2),
		SINK (3),
		TRAFFIC_LIGHT (4);
		
		public int value;
		
		CellType(int value){
			this.value = value;
		}
	}
	
	public enum Direction{
		NORTH (0),
		EAST (1),
		SOUTH (2),
		WEST (3);
		
		private int value;
		
		Direction(int value){
			this.value= value;
		}
		public static Direction newDirection(int value){

			switch(value){
				case 0:return Direction.NORTH;
				case 1:return Direction.EAST;
				case 2:return Direction.SOUTH;
				case 3:return Direction.WEST;
				default:return null;
			}
		}
	}
	
	//---------------------------CONSTRUCTORS---------------------------------
	public TrafficCell(CellType type,int row,int col,int street){
		this.type = type;
		this.row = row;
		this.col = col;
		this.street = street;
		turnProbabilities = new double[4];
		vehicleLeaveTime = MobileSimulation.TICK_TIME;
	}
	
	public TrafficCell(CellType type,int row,int col,int street,String directions, Direction streetDirection, String changeStreets, String turnProbabilities2) {
		this.type = type;
		this.row = row;
		this.col = col;
		this.street = street;
		this.streetDirection = streetDirection;
		turnProbabilities = new double[4];
		
		if( type == CellType.TRAFFIC_LIGHT ){
			
			char [] charProbs = turnProbabilities2.toCharArray(); 
			for(int i=0;i<turnProbabilities.length;i++){
				
				if( charProbs[i] == '@' ){
					turnProbabilities[i]=1;
				}else{
					turnProbabilities[i]= Integer.parseInt(""+charProbs[i])/10.0;
				}
				//System.out.println(turnProbabilities[i]+" ");
			}
			//System.out.println();
			streetChanges = new HashMap<Direction,Integer>();
			
			char [] chStreets = changeStreets.toCharArray();
			//System.out.println("Streets: "+chStreets.length);
			//for(char c:chStreets)System.out.println("*"+c);
			for(int i=0;i<chStreets.length;i++){
				//System.out.println("CHAR: "+chStreets[i]);
				if(chStreets[i]!='_'){
					Direction dir = Direction.newDirection(i);
				
					streetChanges.put(dir, Integer.parseInt(""+chStreets[i]));
				}
			}
		}
		vehicleLeaveTime = MobileSimulation.TICK_TIME;
	}
	/**
	 * TrafficCell will look at next cell and tick to determine if it should move.
	 * @param time
	 */
	public void computeNextMove(int time) {
		
		
		if( vehicle != null ){
			
			double timeInCell = vehicle.getTimeInCell(); //returns 5
			if( time >= vehicleLeaveTime ){ // A vehicle has stayed at a cell long enough, so it jumps ahead:
				
				//vehicle leaves the system
				if( type == CellType.SINK ){
					vehicle.destroy(time,street);
					vehicle = null;
				
				}else{ 
					
					if( type == CellType.TRAFFIC_LIGHT){
						int val = MobileSimulation.streetIdToSignalId.get(street);
						boolean bool = MobileSimulation.trafficLightStatus[val];
						
						//traffic light is red do nothing
						if( (streetDirection == Direction.EAST || streetDirection == Direction.WEST) && !bool ){
							return;
						}
						if( (streetDirection == Direction.NORTH || streetDirection == Direction.SOUTH) && bool ){
							return;
						}
					}
					//find the next cell
					TrafficCell nextCell = MobileSimulation.getNextCell(street,this);
					
					//if its empty move there
					if(nextCell.vehicle == null){
						nextCell.vehicle = vehicle;
						nextCell.vehicleLeaveTime = time + timeInCell;
						vehicle = null;
					}else{
						vehicle.updateDelay(MobileSimulation.TICK_CHANGE);
						++vehicleLeaveTime;
					}
				}
			}
		}
	}
	
	//FIXME might be obsolete based on above fix-me
	//FIXME needs to return a cell, not a direction, and we have to be careful to make it not go to another TRAFFIC_LIGHT cell
	public Direction crossIntersection(int targetVehicleId) {
		
		if( vehicle != null ){
			
			if( vehicle.vehicleId == targetVehicleId ){
				int max = 0;
				for(int i=0;i<turnProbabilities.length;i++){
					if( turnProbabilities[i] > turnProbabilities[max] ){
						max = i;
					}
				}
				return Direction.newDirection(max);
			}else{
				double rand = Math.random();
				double sum = 0;
				for(int i=0;i<turnProbabilities.length;i++){
					sum += turnProbabilities[i];
					//for(int a=0;a<turnProbabilities.length;a++)System.out.print(turnProbabilities[a]+" ");
					//System.out.println("RAND: "+rand+" SUM: "+sum);
					if( rand < sum ){
						return Direction.newDirection(i);
					}
				}
				System.out.println("HERE");
			}
			return null;
		}
		return null;
	}

	@Override
	public String toString() {
		return "TrafficCell [row=" + row + ", col=" + col + ", street="
				+ street + ", type=" + type + "]";
	}
	
	//---------------------------CODE GRAVEYARD---------------------------------	
	/* FIXME: Below comment is not valid any more; decided to go for static traffic light array
	 * If a cell is in the front of a lane, it holds a TrafficLight object.
	 * This object is used to see if a vehicle can advance to the next cell
	 * 
	 * Other cells will have a null TrafficLight object
	 */
	 
}
