package mobile_simulator.models;

import java.text.DecimalFormat;

public class ConfidenceInterval{
	
	String label;
	double average;
	double stdev;
	public ConfidenceInterval(String label,double average,double stdev){
		this.label = label;
		this.average = average;
		this.stdev = stdev;
	}
	public String toString(){
		DecimalFormat fmt = new DecimalFormat("#.00");
		double lend = average-stdev;
		double rend = average+stdev;
		return label+": "+fmt.format(average)+" +/- "+fmt.format(stdev)+" ( "+fmt.format(lend)+" < x < "+ fmt.format(rend)+" )";
	}

}
