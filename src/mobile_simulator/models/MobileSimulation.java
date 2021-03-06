package mobile_simulator.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.lang.Math;
import mobile_simulator.models.TrafficCell.Direction;

import mobile_simulator.models.TrafficCell.CellType;

public class MobileSimulation {
	//---------------GLOBALS START---------------------
	private int count=0;
	public static final int TICK_TIME = 1;
	private static TrafficCell [][] grid; // The first index is the y coordinate measured from the top; the second index is the x coordinate measured from the left.
	private String inputFilename="simulation_input.txt";
	private int height,width;
	public static boolean[] trafficLightStatus = { true, true, true }; //true means that vehicles on the vertical are allowed to move
	public static HashMap<Integer,StreetData> streetData;
	private static int targetVehicleId = 5;
	private int simulationTime;
	private TrafficStatistics statistics;
	public static final int TICK_CHANGE = 1;
	public static HashMap<Integer,Integer> streetIdToSignalId;
	public static int numInSystem;
	private HashMap<Integer,HashMap<TrafficCell,Integer>>sources;
	private double HIGH_TRAFFIC = 5;
	private double MEDIUM_TRAFFIC = 15;
	private double LOW_TRAFFIC = 30;
	private TrafficType simType;
	
	public enum TrafficType{
		LOW,MEDIUM,HIGH
	}
	public TrafficStatistics runSimulation() {
		
		return runSimulation(3000,5,TrafficType.MEDIUM);
	}
	public TrafficStatistics runSimulation(int ticks,int numberOfInitialCars){
		return  runSimulation(ticks,numberOfInitialCars,TrafficType.MEDIUM);
	}
	public TrafficStatistics runSimulation(int ticks, int numberOfInitialCars,TrafficType type){
		
		simType = type;
		simulationTime = ticks;
		statistics = new TrafficStatistics(ticks);
		numInSystem = 0;
		sources = new HashMap<Integer,HashMap<TrafficCell,Integer>>();
		setupStreetIdToSignalId();
		getData();
		
		//randomly setup lights;
		if( Math.random()<.5 ){
			trafficLightStatus[0]=false;
			trafficLightStatus[1]=false;
			trafficLightStatus[2]=false;
		}
		
		for(int tick = 0; tick < ticks; tick+=TICK_CHANGE ){
			statistics.updateTotalInSystem(tick, numInSystem);
			manageTrafficLights( tick );
			for( int streetId = 0; streetId < 11; streetId++ ){
				computeNewStreetState(streetData.get(streetId), tick);
			}
			manageArrivals2( tick );
		}	
		return statistics;
	}
	//---------------RUN LOGIC END-----------------	
	
	
	//-------------SETUP GRID START------------------------		
	public void getData(){
		//readFile();
		readFromMemory();
		//printGrid();
	}

	public void readFromMemory(){
		
		height = 69;
		width = 89;
		
		/**
		 * This data structure holds the data to setup the grid. Each row is 
		 * 
		 * street
		 * column - top left column of street segment
		 * row - top row of street segment
		 * width - width of the street segment
		 * height - height of the street segment
		 * directions - directions that vehicles can travel on this segment
		 * 		-1111 means NESW, 0111 means east, south, and west
		 * direction - direction that the street is going
		 * 		-North(0)	East(1)		South(2)	West(3)
		 * next street - streets that this segment goes into
		 * 		_463 - This means that the street cannot go north
		 * 		It can go to road segment 4 if headed east
		 * 		It can go to road segment 6 if headed south
		 * 		It can go to road segment 3 if headed west
		 * turn probabilities
		 * 		Probabilites for each direction 
		 * 		0 means that the segment cannot go in that direction
		 * 		a nonzero x means that the street has an x/10.0 probability of turning
		 * 		@ is for 1
		 */
		String [] data = {
				"0,35,0,4,20,0111,2,_463,0253",
				"1,83,0,4,20,1000,0,____,0000",
				"2,0,21,35,1,0110,1,046_,0640",
				"3,0,20,35,1,0001,3,____,0000",
				"4,39,21,44,1,1001,1,1___,@000",
				"5,39,20,44,1,0011,3,__63,0046",
				"6,35,22,4,47,0010,2,____,0000",
				"7,83,22,4,17,1001,0,1__5,6004",
				"8,87,39,2,1,1001,3,7___,@000",
				"9,87,40,2,1,0100,1,____,0000",
				"10,83,41,4,28,1100,0,79__,6400"
		};
		
		grid = new TrafficCell[height][width];
		streetData = new HashMap<Integer,StreetData>();
		
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				grid[i][j]=new TrafficCell(CellType.EMPTY,i,j,-1);
			}
		}
		for(int i=0;i<data.length;i++){
			updateGrid(data[i]);
		}
	}
	/**
	 * Update the grid based on street segment data
	 * @param line Line of data for street segment
	 */
	private void updateGrid(String line){
		
		//street id,x start,y start,width,height,directions,vertical(1)/horizontal(0)
		String [] toks = line.split(",");
		int street = Integer.parseInt( toks[0] );
		int startx = Integer.parseInt( toks[1] );
		int starty = Integer.parseInt( toks[2] );
		int width = Integer.parseInt( toks[3] );
		int height = Integer.parseInt( toks[4] );
		String directions = toks[5];
		int streetDirection = Integer.parseInt(toks[6]);
		String changeStreets = toks[7];
		String turnProbabilities = toks[8];
		
		int endx = startx + width;
		int endy = starty + height;
		
		Direction direction = Direction.newDirection(streetDirection);
		streetData.put(street, new StreetData(street,startx,starty,width,height,direction));
		
		for(int i=starty;i<endy;i++){
			
			for(int j=startx;j<endx;j++){
				//System.out.println("x: "+j+" y: "+i+" startx: "+startx+" endx: "+endx+" starty: "+starty+" endy: "+endy);
				CellType type = computeType(j,i,startx,endx,starty,endy,directions,streetDirection);
				grid[i][j] = new TrafficCell(type,i,j,street,directions,direction,changeStreets,turnProbabilities);
				if( type==CellType.SOURCE ){
					TrafficCell cell = grid[i][j];
					HashMap<TrafficCell,Integer> map = sources.get(street);
					if(map==null){
						map = new HashMap<TrafficCell,Integer>();
					}
					map.put(cell, simulationTime+10);
					sources.put(street, map);
				}
			}
		}
		
	}	
	/**
	 * Figure out the type of cell
	 * @param x - column
	 * @param y - row
	 * @param startx - start column of street segment
	 * @param endx - end row of street segment 
	 * @param starty - start row
	 * @param endy - end row
	 * @param directions - directions of street segment
	 * @param streetDirection - flow of street 
	 * @return CellType
	 */
	private CellType computeType(int x, int y, int startx, int endx, int starty, int endy, String directions, int streetDirection) {
		
		/*
		 * Directions NESW 
		 * 0 - NORTH
		 * 1 - EAST
		 * 2 - SOUTH
		 * 3 - WEST
		 */
		
		//on left border headed west is a sink
		if( x==0 && directions.charAt(3)=='1'){
			return CellType.SINK;
		}
		//on left border headed east is a source. streetid is 2
		if( x==0 && directions.charAt(1)=='1'){
			return CellType.SOURCE;
		}
		//on right border headed east is a sink
		if( x==width-1 && directions.charAt(1)=='1'){
			return CellType.SINK;
		}
		//on right border headed west is a source. streetid is 8
		if( x==width-1 && directions.charAt(3)=='1'){
			return CellType.SOURCE;
		}
		//on top border headed north
		if( y==0 && directions.charAt(0)=='1'){
			return CellType.SINK;
		}
		//on top border headed south. streetid is 0
		if( y==0 && directions.charAt(2)=='1'){
			return CellType.SOURCE;
		}
		//on bottom border headed south
		if( y==height-1 && directions.charAt(2)=='1'){
			return CellType.SINK;
		}
		//on bottom border headed north. streetid is 10
		if( y==height-1 && directions.charAt(0)=='1'){
			return CellType.SOURCE;
		}
		//headed south and at bottom of segment is a traffic light
		if( streetDirection==2 && y==endy-1 && endy!= height ){
			return CellType.TRAFFIC_LIGHT;
		}
		//headed north and at the top of segment is a traffic light
		if( streetDirection==0 && y==starty && y!=0 ){
			return CellType.TRAFFIC_LIGHT;
		}
		//headed east on end of segment
		if( streetDirection==1 && x == endx-1 ){
			return CellType.TRAFFIC_LIGHT;
		}
		//headed west on start of segment
		if( streetDirection==3 && x == startx ){
			return CellType.TRAFFIC_LIGHT;
		}
		if( streetDirection==3 && x == width-1){
			return CellType.SOURCE;
		}
		return CellType.NORMAL;
	}	

	//---------------RUN LOGIC END-----------------	
	
	
	private void setupStreetIdToSignalId(){
		//the signalId's are numbered in increasing order from top-left to bottom-right on the map
		streetIdToSignalId = new HashMap<Integer,Integer>();
		streetIdToSignalId.put(0, 0);
	    streetIdToSignalId.put(2, 0);	
		streetIdToSignalId.put(5, 0);
		
		streetIdToSignalId.put(4, 1);
		streetIdToSignalId.put(7, 1);
		
		streetIdToSignalId.put(8, 2);
		streetIdToSignalId.put(10, 2);		
		
		//These streets aren't associated to any signal
		streetIdToSignalId.put(1, -1);
		streetIdToSignalId.put(3, -1);
		streetIdToSignalId.put(6, -1);
		streetIdToSignalId.put(9, -1);			
	}
	/**
	 * Moves through the street segment in a type writer like fashion updating
	 * each cell
	 * @param sd - Street information for the current street
	 * @param currTime - current time;
	 */
	private void computeNewStreetState(StreetData sd, int currTime){
		
		TrafficCell cell = null;
		//set the hook first
		switch(sd.direction){
			case NORTH: cell = grid[sd.startY][sd.startX];break;
			case EAST: cell = grid[sd.startY][sd.startX+sd.width-1];break;
			case SOUTH: cell = grid[sd.startY+sd.height-1][sd.startX+sd.width-1];break;
			case WEST: cell = grid[sd.startY+sd.height-1][sd.startX];
		}
		
		do{
			cell.computeNextMove(currTime); //Found in TrafficCell.java: Manages vehicles moving from one cell to another.
		}while((cell = findNextCell(sd,cell)) != null );
		
	}

	//follow all cells in a street typewriter-style
	private TrafficCell findNextCell(StreetData sd, TrafficCell tc){
		
		int row = tc.row;
		int col = tc.col;
		
		switch( sd.direction ){
		
		/*
		 * If headed north
		 * 
		 * 		   NORTH     EAST               SOUTH             WEST
		 *         width     width              width             width
		 *         T   	     -------T               
		 *   height| |  |      <-     |height      | ^ |            -------
		 *         | *  |    --------              | | |height          ->   height
		 *         |    |                          |   T            T-------
		 *         
		 *         for each lane we compute starting at the position labeled T
		 *         going back
		 */
			case NORTH:{
				
				if(col==sd.startX+sd.width-1){
					if( row == sd.startY+sd.height-1 )
						return null;
					else
						return grid[++row][sd.startX];
				}else{
					return grid[row][++col];
				}
			}
			case EAST:{
				if(row==sd.startY+sd.height-1){
					if( col == sd.startX )
						return null;
					else
						return grid[sd.startY][--col];
				}else{
					return grid[++row][col];
				}
			}
			case SOUTH:{
				if(col==sd.startX){
					if( row == sd.startY )
						return null;
					else
						return grid[--row][sd.startX+sd.width];
				}else{
					return grid[row][--col];
				}
			}
			case WEST:{
				if(row==sd.startY+sd.height-1){
					if( col == sd.startX+sd.width-1 )
						return null;
					return grid[sd.startY][++col];
				}else{
					return grid[++row][col];
				}
			}
		}
		return null;
	}
	
	/**
	 * This method finds the next cell that a vehicle will move to.
	 * @param street - current street
	 * @param tc - Traffic Cell
	 * @return
	 */
	public static TrafficCell getNextCell(int street, TrafficCell tc) {
		
		
		int row = tc.row;
		int col = tc.col;
		
		StreetData sd = streetData.get(street);
		
		if(tc.type == CellType.TRAFFIC_LIGHT){
			Direction dir = tc.crossIntersection(targetVehicleId); //returns a direction
			int nextStreet = tc.streetChanges.get(dir);
			
			StreetData nsSd = streetData.get(nextStreet);
			return get1stCellNextStreet(nsSd,tc,sd.direction);
		}
		switch( sd.direction ){
		
		/*
		 * If headed north
		 * 
		 * 		   NORTH     EAST               SOUTH             WEST
		 *         width     width              width             width
		 *         T   	     -------T               
		 *   height| |  |      <-     |height      | ^ |            -------
		 *         | *  |    --------              | | |height          ->   height
		 *         |    |                          |   T            T-------
		 *         
		 *         for each lane we compute starting at the position labeled T
		 *         going back
		 */
		
			case NORTH:{
					return grid[--row][col];
			}
			case EAST:{
					return grid[row][++col];
				
			}
			case SOUTH:{
					return grid[++row][col];
			}
			case WEST:{
					return grid[row][--col];
			}
		}
		return null;
	}	
	/**
	 * If a vehicle is crossign the street, find out what cell it needs to move to
	 * @param sd - street data of current street
	 * @param oldCell - current cell
	 * @param direction - direction of movement across the intersection
	 * @return
	 */
	private static TrafficCell get1stCellNextStreet(StreetData sd,TrafficCell oldCell,Direction direction){
		Random rand = new Random();
		switch(sd.direction){
			case NORTH:{
				if(direction==Direction.NORTH){
					return grid[sd.startY+sd.height-1][oldCell.col];
				}else{
					
					int col = sd.startX + rand.nextInt(sd.width);
					return grid[sd.startY][sd.startX+sd.width-1];
				}
			}
			case EAST:{
				if(direction==Direction.EAST){
					return grid[oldCell.row][sd.startX];
				}else{
					return grid[sd.startY][sd.startX];
				}
			}
			case SOUTH:{
				if(direction==Direction.SOUTH){
					return grid[sd.startY][oldCell.col];
				}else{
					
					int col = sd.startX + rand.nextInt(sd.width);
					return grid[sd.startY][col];
				}
			}
			case WEST:{
				if(direction==Direction.WEST){
					return grid[oldCell.row][sd.startX+sd.width-1];
				}else{
					return grid[sd.startY][sd.startX+sd.width-1];
				}
			}
		}
		return null;
		
	}
	/**
	 * Toggles light booleans
	 * @param tick
	 */
	private void manageTrafficLights( int tick ){
		if ( tick != 0 ) {
			//initialize traffic lights
			for ( int i = 0; i < 3; i++ ){
				if ( tick%30 == 0 ) {
					trafficLightStatus[i] = !trafficLightStatus[i];
				}
			}
		}	
	}
	/**
	 * 
	 * @param tick
	 */
	private void manageArrivals2( int tick ){
		
		double everyXseconds = 0;
		
		switch( simType ){
			case LOW:everyXseconds = LOW_TRAFFIC;break;
			case MEDIUM:everyXseconds = MEDIUM_TRAFFIC;break;
			case HIGH:everyXseconds = HIGH_TRAFFIC;break;
		}
		
		if ( tick == 0 ) {
			for(Integer street:sources.keySet()){
				int time = (int)Math.round( -1/(1/everyXseconds)*Math.log( Math.random() ) );
				TrafficCell cell = getNextSourceCell(street);
				sources.get(street).put(cell, time);
			}
		}else{
			
			for(Integer street:sources.keySet()){
				
				for(TrafficCell cell:sources.get(street).keySet()){
					if(sources.get(street).get(cell)<=tick){
						if(cell.vehicle==null){
							
							cell.vehicle=new Vehicle(tick,cell.street,statistics);
							numInSystem++;
							statistics.created++;
							int time = (int)Math.round( -1/(1/everyXseconds)*Math.log( Math.random() ) );
							count++;
							sources.get(street).put(cell, simulationTime+10);
							TrafficCell next = getNextSourceCell(street);
							sources.get(street).put(next, tick+time);
							
							break;
						}
					}
				}
			}
		}
	}	
	/**
	 * Helper function which determines a source cell to generate an arrival from
	 * next
	 * @param street - Street of arrival
	 * @return TrafficCell
	 */
	private TrafficCell getNextSourceCell(int street){
		
		HashMap<TrafficCell,Integer> map = sources.get(street);
		ArrayList<TrafficCell> cells = new ArrayList<TrafficCell>(map.keySet());
		int ind = (int)(Math.random()*cells.size());
		return cells.get(ind);
	}
	public boolean isStreetFlowing( int streetId ){
		boolean isFlowing = true;
		//loop through the roads and check if there is a signal thats red, if so, set isFlowing to false
		return isFlowing;
	}		
	public void printGrid(){
		System.out.println(getGrid());
	}
	public String getGrid(){
		String str = "";
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				str+=grid[i][j].type.value+" ";
			}
			str+="\n";
		}
		return str;
	}
	public TrafficCell[][] getGridCells()
	{
		return grid;
	}
	public static void main(String[]args){
		MobileSimulation sim = new MobileSimulation();
		TrafficStatistics stats = sim.runSimulation(3000,10,TrafficType.LOW);
		System.out.println(stats.getStats());
		//System.out.println(stats.getVehicleStats(1));
		//stats.writeNumInSystemToFile("numInSystemOverTimeLow.csv");
	}
}