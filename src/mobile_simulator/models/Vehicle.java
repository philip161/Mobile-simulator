package mobile_simulator.models;

public class Vehicle {
    public static int staticVehicleId = 0;
	public int vehicleId = 0;	
	
	public enum Direction{
		
	}
	private Direction direction;
	
	Vehicle() {
		staticVehicleId++;
		vehicleId = staticVehicleId;
    }
}
