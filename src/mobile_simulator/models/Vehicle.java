package mobile_simulator.models;

public class Vehicle {
    public static int staticVehicleId = 0;
	public int vehicleId = 0;
	public int startTick;
	
	public enum Direction{
		
	}
	private Direction direction;
	
	
	Vehicle() {
		staticVehicleId++;
		vehicleId = staticVehicleId;
		startTick = -1;
    }	
	
	Vehicle( int tick ) {
		staticVehicleId++;
		vehicleId = staticVehicleId;
		startTick = tick;
    }
}
