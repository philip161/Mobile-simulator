package mobile_simulator.models;

import java.text.DecimalFormat;

public class ConfidenceInterval{
	
	String label;
	double average;
	double stdev;
	int dof;
	int n;
	
	//95 two tail
	double [] tValues = {	6.314000, 2.920000, 2.353000, 2.132000, 2.015000, 
			1.943000, 1.895000, 1.860000, 1.833000, 1.812000, 
			1.796000, 1.782000, 1.771000, 1.761000, 1.753000, 
			1.746000, 1.740000, 1.734000, 1.729000, 1.725000, 
			1.721000, 1.717000, 1.714000, 1.711000, 1.708000, 
			1.706000, 1.703000, 1.701000, 1.699000, 1.697000, 
			1.684000, 1.671000, 1.664000, 1.660000, 1.646000, 
			1.645000}; 
	
	public ConfidenceInterval(String label,double average,double stdev,int numRuns){
		this.label = label;
		this.average = average;
		this.stdev = stdev;
		dof = numRuns-1;
		n = numRuns;
		if(dof>tValues.length)
			dof = tValues.length;
	}
	public String toString(){
		DecimalFormat fmt = new DecimalFormat("#.00");
		double halfWidth = (tValues[dof-1]*stdev/Math.sqrt(n));
		double lend = average-halfWidth; 
		double rend = average+halfWidth;
		return label+": "+fmt.format(average)+" +/- "+fmt.format(halfWidth)+" ( "+fmt.format(lend)+" < x < "+ fmt.format(rend)+" )";
	}

}
