package mobile_simulator.models;

import mobile_simulator.models.TrafficCell.Direction;

public class Vehicle {
    public static int staticVehicleId = 0;
	public int vehicleId = 0;
	public int startTick;
	
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

	public int getTimeInCell() {
		
		
		return 5;
	}

	public void destroy(int time) {
		
		int timeInSystem = time - startTick;
		System.out.println("Destroying vehicle "+vehicleId+"/ "+staticVehicleId);
	}
}
