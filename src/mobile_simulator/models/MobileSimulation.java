package mobile_simulator.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import mobile_simulator.models.TrafficCell.CellType;

public class MobileSimulation {
	
	private TrafficCell [][] grid;
	private String inputFilename="simulation_input.txt";
	private int height,width;
	
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
				grid[i][j]=new TrafficCell(CellType.NORMAL);
			}
		}
		//System.out.println("Width: "+width+" height: "+height);
		
		while( scan.hasNext() ){
			line = scan.nextLine();
			//System.out.println(line);
			updateGrid(line);
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
		
		int endx = startx + width;
		int endy = starty + height;
		
		for(int i=starty;i<endy;i++){
			
			for(int j=startx;j<endx;j++){
				//System.out.println("x: "+j+" y: "+i+" startx: "+startx+" endx: "+endx+" starty: "+starty+" endy: "+endy);
				CellType type = computeType(j,i,startx,endx,starty,endy,directions,streetDirection);
				grid[i][j] = new TrafficCell(type);
				grid[i][j].computeProbabilities(directions);
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
		//on left border headed east is a source
		if( x==0 && directions.charAt(1)=='1'){
			return CellType.SOURCE;
		}
		//on right border headed east is a sink
		if( x==width-1 && directions.charAt(1)=='1'){
			return CellType.SINK;
		}
		//on right border headed west is a source
		if( x==width-1 && directions.charAt(3)=='1'){
			return CellType.SOURCE;
		}
		//on top border headed north
		if( y==0 && directions.charAt(0)=='1'){
			return CellType.SINK;
		}
		//on top border headed south
		if( y==0 && directions.charAt(2)=='1'){
			return CellType.SOURCE;
		}
		//on bottom border headed south
		if( y==height-1 && directions.charAt(2)=='1'){
			return CellType.SINK;
		}
		//on bottom border headed north
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
	public void readFromMemory(){
		String [] data = {
				"69",
				"89",
				"street id,x start,y start,width,height,directions,street direction north(0)/east(1)/south(2)/west(3)",
				"0,35,0,4,20,0111,2",
				"1,83,0,4,20,1000,0",
				"2,0,21,35,1,0110,1",
				"3,0,20,35,1,0001,3",
				"4,39,21,44,1,1100,1",
				"5,39,20,44,1,0011,3",
				"6,35,22,4,47,0111,2",
				"7,83,22,4,17,1001,0",
				"8,87,39,2,1,1000,3",
				"9,87,40,2,1,0100,1",
				"10,83,41,4,28,1100,0"
		};
		
		height = Integer.parseInt(data[0]);;
		width = Integer.parseInt(data[1]);
		grid = new TrafficCell[height][width];
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				grid[i][j]=new TrafficCell(CellType.NORMAL);
			}
		}
		for(int i=3;i<data.length;i++){
			updateGrid(data[i]);
		}
		
	}
	public void getData(){
		
		//readFile();
		readFromMemory();
		printGrid();
		
	}
	public void printGrid(){
		
		System.out.println(getGrid());
	}
	public void runSimulation(){
		
		getData();
		
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
	public static void main(String[]args){
		
		new MobileSimulation().runSimulation();
	}
}
