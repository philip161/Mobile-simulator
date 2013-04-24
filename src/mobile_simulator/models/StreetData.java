package mobile_simulator.models;

import mobile_simulator.models.TrafficCell.Direction;

public class StreetData {
	
	public int street;
	public int startX;
	public int startY;
	public int width;
	public int height;
	public Direction direction;
	
	public StreetData(int street, int startX, int startY, int width, int height,
			Direction direction) {
		super();
		
		this.street = street;
		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;
		this.direction = direction;
	}
	
	

}