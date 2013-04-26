package mobile_simulator.models;

import mobile_simulator.models.TrafficCell.Direction;

public class Vehicle {
    public static int staticVehicleId = 0;
	public int vehicleId = 0;
	public int startTick;
	public int totalBackup;
	public double timeInSystem;
	private TrafficStatistics statistics;
	public int arrivalStreet;
	public int departureStreet;
	
	public Vehicle(TrafficStatistics stats) {
		staticVehicleId++;
		vehicleId = staticVehicleId;
		startTick = -1;
		totalBackup = 0;
		timeInSystem = 0;
		statistics = stats;
    }	
	
	public Vehicle( int tick,int sourceStreet,TrafficStatistics stats ) {
		staticVehicleId++;
		vehicleId = staticVehicleId;
		startTick = tick;
		arrivalStreet = sourceStreet;
		statistics = stats;
    }

	public double getTimeInCell() {
		
		return .5;
	}

	public void destroy(int time,int street) {
		
		timeInSystem = time - startTick;
		departureStreet = street;
		//System.out.println("Destroying vehicle "+vehicleId+"/ "+staticVehicleId);
		statistics.vehicleDestroyed(this, street);
		
		MobileSimulation.numInSystem--;
		
	}

	public void updateDelay(int tickChange) {
		
		totalBackup+=tickChange;
		
	}
}
