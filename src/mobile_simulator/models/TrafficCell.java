package mobile_simulator.models;

import java.util.HashMap;

import mobile_simulator.models.MobileSimulation;

public class TrafficCell {
	//---------------------------FIELDS AND ENUMS---------------------------------
	private TrafficLight light;
	public Vehicle vehicle;
	private double [] turnProbabilities;
	public int row;
	public int col;
	public int vehicleLeaveTime;
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
		
		private int value;
		
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

	public void computeProbabilities(String directions) {
			if( streetDirection == Direction.NORTH ){
				turnProbabilities[0] = .5;
				turnProbabilities[1] = .3;
				turnProbabilities[2] = 0;
				turnProbabilities[3] = .2;
			}
			if( streetDirection == Direction.EAST ){
				turnProbabilities[0] = .2;
				turnProbabilities[1] = .5;
				turnProbabilities[2] = .3;
				turnProbabilities[3] = 0;
			}
			if( streetDirection == Direction.SOUTH ){
				turnProbabilities[0] = 0;
				turnProbabilities[1] = .2;
				turnProbabilities[2] = .5;
				turnProbabilities[3] = .3;
			}
			if( streetDirection == Direction.WEST ){
				turnProbabilities[0] = .3;
				turnProbabilities[1] = 0;
				turnProbabilities[2] = .2;
				turnProbabilities[3] = .5;
			}
	}
	
	//---------------------------METHODS CALLED FROM MobileSImulation.java---------------------------------	
	
	//manages vehicles moving from one cell to another
	//FIXME needs the time increment, not the actual tick time
	public void computeNextMove(int time) {
		
		
		if( vehicle != null ){
			
			int timeInCell = vehicle.getTimeInCell(); //returns 5
			if( time >= vehicleLeaveTime ){ // A vehicle has stayed at a cell long enough, so it jumps ahead:
				if( type == CellType.SINK ){
					vehicle.destroy(time,street);
					vehicle = null;
				//FIXME 12 add elseif for type == CellType.TRAFFIC_LIGHT: make sure to block movement if the corresponding signal is red and make sure to not go to another TRAFFIC_LIGHT while turning
				}else{ //the cell a vehicle currently is on is a SOURCE, NORMAL, or a TRAFFIC_LIGHT
					
					if( type == CellType.TRAFFIC_LIGHT){
						int val = MobileSimulation.streetIdToSignalId.get(street);
						boolean bool = MobileSimulation.trafficLightStatus[val];
						if( (streetDirection == Direction.EAST || streetDirection == Direction.WEST) && !bool ){
							return;
						}
						if( (streetDirection == Direction.NORTH || streetDirection == Direction.SOUTH) && bool ){
							return;
						}
					}
					TrafficCell nextCell = MobileSimulation.getNextCell(street,this);
					
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
