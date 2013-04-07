package mobile_simulator.models;

public class TrafficCell {
	
	private TrafficLight light;
	private Vehicle vehicle;
	private double [] turnProbabilities;
	
	/*
	 * If a cell is in the front of a lane, it holds a TrafficLight object.
	 * This object is used to see if a vehicle can advance to the next cell
	 * 
	 * Other cells will have a null TrafficLight object
	 */
	
	public TrafficCell computeNextCell(){
		return null;
	}
}
