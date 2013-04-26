package mobile_simulator.models;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TrafficStatistics {

	
	
	private double sumTimeInSystem;
	public HashMap<Integer,Integer> carsThroughSink;
	private LinkedList<Vehicle> vehicles;
	private HashMap<Integer,Integer> numInSystem;
	private int sumNumInSystem;
	private int simulationTime;
	public int created;
	public int destroyed;
	
	public TrafficStatistics(int simTime){
		
		sumTimeInSystem = 0;
		sumNumInSystem = 0;
		destroyed = 0;
		simulationTime = simTime;
		created = 0;
		carsThroughSink = new HashMap<Integer,Integer>();
		vehicles = new LinkedList<Vehicle>();
		numInSystem = new HashMap<Integer,Integer>();
		
		
	}
	public void vehicleDestroyed(Vehicle vehicle,int street){
		
		vehicles.add( vehicle );
		if( carsThroughSink.containsKey(street))
			carsThroughSink.put(street,carsThroughSink.get(street)+1);
		else
			carsThroughSink.put(street,1);
		sumTimeInSystem+=vehicle.timeInSystem;
		destroyed++;
		
	}
	public void updateTotalInSystem(int tick,int numCars){
		numInSystem.put(tick, numCars);
		sumNumInSystem+=numCars;
	}
	public void writeNumInSystemToFile(String filename){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filename);
		} catch (FileNotFoundException e) {e.printStackTrace();}
		
		List<Integer> ticks = new ArrayList<Integer>(numInSystem.keySet());
		Collections.sort(ticks);
		
		for(Integer tick:ticks){
			writer.println(tick+","+numInSystem.get(tick));
		}
		writer.close();
	}
	public double getAverageVehicleBackup(){
		double sum = 0;
		for(Vehicle vehicle:vehicles){
			sum+=vehicle.totalBackup;
		}
		return sum/vehicles.size();
	}
	public double getAverageTimeInSystem() {
		// TODO Auto-generated method stub
		return (double)sumTimeInSystem/destroyed;
	}
	public double getAverageNumInSystem(){
		return sumNumInSystem/simulationTime;
	}
	public void writeCarsThroughSink(String filename){
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filename);
		} catch (FileNotFoundException e) {e.printStackTrace();}
		
		List<Integer> ticks = new ArrayList<Integer>(carsThroughSink.keySet());
		Collections.sort(ticks);
		
		for(Integer tick:ticks){
			writer.println(tick+","+carsThroughSink.get(tick));
		}
		
	}
	public String getVehicleStats(int vehicleId){
		Vehicle vehicle = null;
		for(Vehicle v:vehicles){
			if(v.vehicleId==vehicleId)
				vehicle = v;
		}
		if(vehicle == null ){
			return null;
		}
		String str = "Vehicle "+vehicle.vehicleId+"\n\n";
		str += "Arrived in system at time: "+vehicle.startTick+" at street: "+vehicle.arrivalStreet+"\n";
		str += "Left the system at time: "+vehicle.startTick+vehicle.timeInSystem+" at street: "+vehicle.departureStreet+"\n";
		str += "Total time in system: "+vehicle.timeInSystem+"\n";
		str += "Total backup: "+vehicle.totalBackup+"\n";
		return str;
	}
	public String getStats() {
		
		String str = "Num created: "+Vehicle.staticVehicleId+"\n";
		str += "Num that left the system: "+destroyed+"\n";
		str += "Average num in system: "+getAverageNumInSystem()+"\n";
		str += "Average vehicle backup: "+getAverageVehicleBackup()+"\n";
		str += "Average time in system: "+getAverageTimeInSystem()+"\n";
		List<Integer>keys = new ArrayList<Integer>(carsThroughSink.keySet());
		Collections.sort(keys);
		
		str+="\nSink throughput\n";
		for(Integer sink:keys){
			str += sink+" : "+carsThroughSink.get(sink)+"\n";
		}
		return str;
	}
}