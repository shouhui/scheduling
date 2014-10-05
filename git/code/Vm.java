package code;
import java.util.List;
import java.util.ArrayList;

public class Vm {
	/** The id */
	private int id;
	
	/** Vm avail time */
	private double availTime;
	
	/** Cloudlets in the vm, the cloudletId, actual start time, actual finish time */
	private List<Double[]>cloudletInVm = new ArrayList<Double[]>();
	
	/** The vm's voltage and frequency */
	private List<Double[]>vfList;
	
	/*
	 * Create a new vm 
	 * 
	 * @param id unique id of the vm
	 * @param availTime the avail time of the vm
	 * 
	 */
	public Vm(int id,double availTime){
		this.id = id;
		this.availTime = 0;
		
	}
	
	/*
	 * Set the avail time
	 * 
	 * @param availTime the new avail time
	 */
	public void setAvailTime(double availTime){
		this.availTime = availTime;
	}
	
	/*
	 * Get the avail time
	 * 
	 */
	public double getAvailTime(){
		return this.availTime;
	}
	
	/*
	 * Set the voltage and frequency list
	 */
	public void setVfList(List<Double[]>vfList){
		this.vfList = vfList;
	}
	
	/*
	 * Get the voltage and frequency list by level
	 */
	public Double[] getVfListByLevel(int level){
		return this.vfList.get(level);
	}
	
	public List<Double[]> getvfList(){
		return this.vfList;
	}
	
}
