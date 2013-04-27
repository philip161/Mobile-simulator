package mobile_simulator.models;

import java.util.*;

import mobile_simulator.models.MobileSimulation.TrafficType;

public class MobileSimExperiment {

	
	private TrafficType type;
	private int numRuns;
	private TrafficStatistics [] stats;
	private HashMap<Integer,Double>aveSinks = new HashMap<Integer,Double>();
	private HashMap<Integer,Double>stdevSinks = new HashMap<Integer,Double>();
	double averageNumInSystem;
	double averageTimeInSystem;
	double averageBackup;
	double averageCreated;
	double averageDestroyed;
	double stdevnumInSystem;
	double stdevtimeInSystem;
	double stdevvehicleBackup;
	double stdevnumCreated;
	double stdevnumDestroyed;
	
	public MobileSimExperiment(int numRuns){
		this(numRuns,TrafficType.MEDIUM);
	}
	public MobileSimExperiment(int numRuns,TrafficType type){
		this.numRuns = numRuns;
		this.type = type;
		stats = new TrafficStatistics[numRuns];
		averageNumInSystem = 0;
		averageTimeInSystem = 0;
		averageBackup = 0;
		averageCreated = 0;
		averageDestroyed = 0;
		stdevnumInSystem = 0;
		stdevtimeInSystem = 0;
		stdevvehicleBackup = 0;
		stdevnumCreated = 0;
		stdevnumDestroyed = 0;
	}
	/**
	 * Runs multiple traffic simulations
	 * @return results of experiment
	 */
	public String runExperiment(){
		for(int i=0;i<numRuns;i++){
			
			TrafficStatistics ts = new MobileSimulation().runSimulation(3200,10,type);
			stats[i] = ts;
		}
		getStats();
		return summarize();
		
	}
	/**
	 * Get statistics from experiment
	 */
	public void getStats(){
		
		double numInSystem = 0;
		double timeInSystem = 0;
		double vehicleBackup = 0;
		double numCreated = 0;
		double numDestroyed = 0;
		HashMap<Integer,Integer>experimentSinks = new HashMap<Integer,Integer>();

		
		for(int i=0;i<numRuns;i++){
			
			TrafficStatistics ts = stats[i];
			numInSystem += ts.getAverageNumInSystem();
			timeInSystem += ts.getAverageTimeInSystem();
			vehicleBackup += ts.getAverageVehicleBackup();
			numCreated += ts.created;
			numDestroyed += ts.destroyed;
			HashMap<Integer,Integer>sinkInfo = ts.carsThroughSink;
			
			for(Integer sink:sinkInfo.keySet()){
				
				if( experimentSinks.containsKey(sink))
					experimentSinks.put(sink,sinkInfo.get(sink)+experimentSinks.get(sink));
				else
					experimentSinks.put(sink,sinkInfo.get(sink));
			}
		}
		averageNumInSystem = (double)numInSystem/numRuns;
		averageTimeInSystem = (double)timeInSystem/numRuns;
		averageBackup = (double)vehicleBackup/numRuns;
		averageCreated = (double)numCreated/numRuns;
		averageDestroyed = (double)numDestroyed/numRuns;
		
		for(Integer sink:experimentSinks.keySet()){
			aveSinks.put(sink, (double)experimentSinks.get(sink)/numRuns);
		}
		
		for(int i=0;i<numRuns;i++){
			
			TrafficStatistics ts = new MobileSimulation().runSimulation();
			stats[i] = ts;
			
			stdevnumInSystem += Math.pow((ts.getAverageNumInSystem() - averageNumInSystem),2);
			stdevtimeInSystem += Math.pow((ts.getAverageTimeInSystem() - averageTimeInSystem),2);
			stdevvehicleBackup += Math.pow((ts.getAverageVehicleBackup() - averageBackup),2);
			stdevnumCreated += Math.pow((ts.created - averageCreated ),2);
			stdevnumDestroyed += Math.pow((ts.destroyed - averageDestroyed),2);
			HashMap<Integer,Integer>sinkInfo = ts.carsThroughSink;
			
			for(Integer sink:sinkInfo.keySet()){
				
				double val = Math.pow((sinkInfo.get(sink) - aveSinks.get(sink)),2);
				if( stdevSinks.containsKey(sink)){
					stdevSinks.put(sink,val+stdevSinks.get(sink));
				}else{
					stdevSinks.put(sink,val);
				}
			}
		}
		stdevnumInSystem = Math.sqrt(stdevnumInSystem/numRuns);
		stdevtimeInSystem = Math.sqrt(stdevtimeInSystem/numRuns);
		stdevvehicleBackup = Math.sqrt(stdevvehicleBackup/numRuns);
		stdevnumCreated = Math.sqrt(stdevnumCreated/numRuns);
		stdevnumDestroyed = Math.sqrt(stdevnumDestroyed/numRuns);
		
		for(Integer sink:stdevSinks.keySet()){
			
			double val = stdevSinks.get(sink);
			stdevSinks.put(sink, Math.sqrt(val/numRuns));
		}
	}
	/**
	 * 
	 * @return results of experiment
	 */
	public String summarize(){
		
		String str = "";
		str += new ConfidenceInterval("Average Number in System",averageNumInSystem,stdevnumInSystem)+"\n";
		str += new ConfidenceInterval("Average Time in System",averageTimeInSystem,stdevtimeInSystem)+"\n";
		str += new ConfidenceInterval("Average Vehicle Backup",averageBackup,stdevvehicleBackup)+"\n";
		str += new ConfidenceInterval("Average Number Created",averageCreated,stdevnumCreated)+"\n";
		str += new ConfidenceInterval("Average Number Destroyed",averageDestroyed,stdevnumDestroyed)+"\n";
		
		List<Integer> sortedSinks = new ArrayList<Integer>(aveSinks.keySet());
		Collections.sort(sortedSinks);
		
		str+= "\nSink Throughput\n";
		for(Integer sink:sortedSinks){
			str += new ConfidenceInterval(sink+"",aveSinks.get(sink),stdevSinks.get(sink))+"\n";
		}
		return str;
	}
	public static void main(String[] args) {
		MobileSimExperiment experiment = new MobileSimExperiment(20,TrafficType.LOW);
		System.out.println(experiment.runExperiment());
	}

}
