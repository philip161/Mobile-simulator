package mobile_simulator.models;

import mobile_simulator.models.MobileSimulation;

public class TrafficCell {
	//---------------------------FIELDS AND ENUMS---------------------------------
	private TrafficLight light;
	public Vehicle vehicle;
	private double [] turnProbabilities;
	private Direction direction;
	public int row;
	public int col;
	public int vehicleLeaveTime;
	public int street;
	public CellType type;
	private Direction streetDirection;
	
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
	
	public enum Direction{
		NORTH (0),
		SOUTH (1),
		EAST (2),
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
	
	public TrafficCell(CellType type,int row,int col,int street,String directions, Direction streetDirection) {
		this.type = type;
		this.row = row;
		this.col = col;
		this.street = street;
		this.streetDirection = streetDirection;
		turnProbabilities = new double[4];
		if( type == CellType.TRAFFIC_LIGHT ){
			computeProbabilities(directions);
		}
		vehicleLeaveTime = MobileSimulation.TICK_TIME;
	}

	public void computeProbabilities(String directions) {
			if( direction == Direction.NORTH ){
				turnProbabilities[0] = .5;
				turnProbabilities[1] = .3;
				turnProbabilities[2] = 0;
				turnProbabilities[3] = .2;
			}
			if( direction == Direction.EAST ){
				turnProbabilities[0] = .2;
				turnProbabilities[1] = .5;
				turnProbabilities[2] = .3;
				turnProbabilities[3] = 0;
			}
			if( direction == Direction.SOUTH ){
				turnProbabilities[0] = 0;
				turnProbabilities[1] = .2;
				turnProbabilities[2] = .5;
				turnProbabilities[3] = .3;
			}
			if( direction == Direction.WEST ){
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
		int timeInCell = vehicle.getTimeInCell(); //returns 5
		if( vehicle != null ){
			if( time >= vehicleLeaveTime ){ // A vehicle has stayed at a cell long enough, so it jummps ahead:
				if( type == CellType.SINK ){
					vehicle.destroy(time);
					vehicle = null;
				//FIXME 12 add elseif for type == CellType.TRAFFIC_LIGHT: make sure to block movement if the corresponding signal is red and make sure to not go to another TRAFFIC_LIGHT while turning
				}else{ //the cell a vehicle currently is on is a SOURCE, NORMAL, or a TRAFFIC_LIGHT
					TrafficCell nextCell = MobileSimulation.getNextCell(street,this);
					if(nextCell.vehicle == null){
						nextCell.vehicle = vehicle;
						nextCell.vehicleLeaveTime = time + timeInCell;
						vehicle = null;
					}else{
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
					if( rand < sum ){
						return Direction.newDirection(i);
					}
				}
			}
			return null;
		}
		return null;
	}
	
	//---------------------------CODE GRAVEYARD---------------------------------	
	/* FIXME: Below comment is not valid any more; decided to go for static traffic light array
	 * If a cell is in the front of a lane, it holds a TrafficLight object.
	 * This object is used to see if a vehicle can advance to the next cell
	 * 
	 * Other cells will have a null TrafficLight object
	 */
	 
	public String toString(){
		return type.value+"";
	}
}
