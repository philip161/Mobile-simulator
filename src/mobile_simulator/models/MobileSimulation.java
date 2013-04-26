package mobile_simulator.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.lang.Math;
import mobile_simulator.models.TrafficCell.Direction;

import mobile_simulator.models.TrafficCell.CellType;

public class MobileSimulation {
	//---------------GLOBALS START---------------------
	public static final int TICK_TIME = 1;
	private static TrafficCell [][] grid; // The first index is the y coordinate measured from the top; the second index is the x coordinate measured from the left.
	private String inputFilename="simulation_input.txt";
	private int height,width;
	private int [] arrivalTimes = new int[4];
	private final int[] sourceX = { 35, 0, 88, 83 }; //these are the x coordinates of the four sources on the map; increasing steetid
	private final int[] sourceY = { 0, 21, 39, 68 };//these are the y coordinates of the four sources on the map; increasing steetid
	public static boolean[] trafficLightStatus = { true, true, true }; //true means that vehicles on the vertical are allowed to move
	public static HashMap<Integer,StreetData> streetData;
	private static int targetVehicleId = 5;
	private static final int SIM_TIME = 1500;
	
	public static HashMap<Integer,Integer> streetIdToSignalId;
	public static void setupStreetIdToSignalId(){
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
	//---------------GLOBALS END---------------------
	
	//---------------RUN LOGIC START-----------------
	public static void main(String[]args){
		new MobileSimulation().runSimulation(SIM_TIME,5);
	}	
	
	public void runSimulation(int ticks, int numberOfInitialCars){
		setupStreetIdToSignalId();
		getData();
		initializeRoads( numberOfInitialCars );
		for(int tick = 0; tick < ticks; tick++ ){
			manageTrafficLights( tick );
			for( int streetId = 0; streetId < 11; streetId++ ){
				computeNewStreetState(streetData.get(streetId), tick);
			}
			manageArrivals( tick );
		}	
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
				grid[i][j]=new TrafficCell(CellType.NORMAL,i,j,-1);
			}
		}
		for(int i=0;i<data.length;i++){
			updateGrid(data[i]);
		}
	}
	
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
			}
		}
	}	
	
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

//-------------SETUP GRID END------------------------	
	
//-------------MOVE VEHICLES START-------------------	
	//for a given street, manages all vehicles moving though its cells
	public void computeNewStreetState(StreetData sd, int currTime){
		
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
	public TrafficCell findNextCell(StreetData sd, TrafficCell tc){
		
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
				if(row==sd.startY){
					if( col == sd.startX+sd.width-1 )
					return grid[sd.startY+sd.height][++col];
				}else{
					return grid[--row][col];
				}
			}
		}
		return null;
	}
	
	// Only called from TrafficCell.java where a vehicle goes from one cell to another
	// Answers the question: Where is a particular vehicle going?
	public static TrafficCell getNextCell(int street, TrafficCell tc) {
		
		
		int row = tc.row;
		int col = tc.col;
		
		StreetData sd = streetData.get(street);
		
		if(tc.type == CellType.TRAFFIC_LIGHT){
			Direction dir = tc.crossIntersection(targetVehicleId); //returns a direction
			//System.out.println("Street: "+street);
			//System.out.println("Direction: "+dir);
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
		 
		 //if-else for TRAFFIC_LIGHT might be obsolete; see 'FIXME 12' in trafficcell.java
		
		
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
	private static TrafficCell get1stCellNextStreet(StreetData sd,TrafficCell oldCell,Direction direction){
		Random rand = new Random();
		switch(sd.direction){
			case NORTH:{
				if(direction==Direction.NORTH){
					return grid[sd.startY+sd.height][oldCell.col];
				}else{
					
					int col = sd.startX + rand.nextInt(sd.width);
					return grid[sd.startY+sd.height][col];
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
					return grid[oldCell.row][sd.startX+sd.width];
				}else{
					return grid[sd.startY][sd.startX+sd.width];
				}
			}
		}
		return null;
		
	}
//-------------MOVE VEHICLES END-------------------		

	public void manageTrafficLights( int tick ){
		if ( tick != 0 ) {
			//initialize traffic lights
			for ( int i = 0; i < 3; i++ ){
				if ( tick%30 == 0 ) {
					trafficLightStatus[i] = !trafficLightStatus[i];
				}
			}
		}	
	}		
	
	public void manageArrivals( int tick ){
		if ( tick == 0 ) {
			arrivalTimes[0] = (int)Math.round( -1/(1/60.0)*Math.log( Math.random() ) ); // draws an Exponential(lamda = 1/60 cars/tick) variate for steetId 0's source
			arrivalTimes[1] = (int)Math.round( -1/(1/60.0)*Math.log( Math.random() ) ); // draws an Exponential(lamda = 1/60 cars/tick) variate for steetId 2's source		
			arrivalTimes[2] = (int)Math.round( -1/(1/60.0)*Math.log( Math.random() ) ); // draws an Exponential(lamda = 1/60 cars/tick) variate for steetId 8's source
			arrivalTimes[3] = (int)Math.round( -1/(1/60.0)*Math.log( Math.random() ) ); // draws an Exponential(lamda = 1/60 cars/tick) variate for steetId 10's source
		} else {
			for ( int i = 0; i < 4; i++ ){
				if ( arrivalTimes[i] == tick ) {
					if ( grid[sourceY[i]][sourceX[i]].vehicle == null ) { //if there is no vehicle in the patch
						grid[sourceY[i]][sourceX[i]].vehicle = new Vehicle( tick ); //introduce a new car at the streetId's source
					}	
					arrivalTimes[i] = (int)Math.round( -1/(1/60.0)*Math.log( Math.random() ) +tick ); // draws a fresh Exponential(lamda = 1/60 cars/tick) variate for the given source where a car just arrived
				}
			}
		}
	}	
	

	
	//-------------CODE GRAVE START----------------
	//tools for I/O, debugging and some extension ideas
	/**
	 * Setup traffic grid
	 */
	private void readFile(){
	
		Scanner scan = null;
		try {
			scan = new Scanner( new File(inputFilename) );
		} catch (FileNotFoundException e) {e.printStackTrace();}
		
		height = Integer.parseInt( scan.nextLine() );
		width = Integer.parseInt( scan.nextLine() );
		String line = scan.nextLine();
		grid = new TrafficCell[height][width];
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				grid[i][j]=new TrafficCell(CellType.NORMAL,i,j,-1);
			}
		}
		//System.out.println("Width: "+width+" height: "+height);
		
		while( scan.hasNext() ){
			line = scan.nextLine();
			//System.out.println(line);
			updateGrid(line);
		}
	}
	
	public void printGrid(){
		System.out.println(getGrid());
	}	
	
		public String getGrid(){
		String str = "";
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				str+=grid[i][j]+" ";
			}
			str+="\n";
		}
		return str;
	}
		
	public TrafficCell[][] getGridCells()
	{
		return grid;
	}
	
	public void initializeRoads( int numberOfInitialCars ){
		//loop through the roads and randomly plant cars on ramdom cells that are free 
	}
	
	public boolean isStreetFlowing( int streetId ){
		boolean isFlowing = true;
		//loop through the roads and check if there is a signal thats red, if so, set isFlowing to false
		return isFlowing;
	}		
	
	//-------------CODE GRAVE END----------------	
}
