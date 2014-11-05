package code;
import java.util.List;
import java.util.ArrayList;

public class Vm {
	/** The id */
	private int id;
	
	/** Vm avail time */
	private double availTime;
	
	/*	The cloudlet id assigned to the virtual machine */
	private List<Integer>cloudletIdInVm;
	
	/** The vm's voltage and frequency */
	private List<Double[]>vfList;
	
	/*	The maximize level of voltage and frequency*/
	private int maxVfLevel;
	
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
		setCloudletIdInVm(null);
		cloudletIdInVm = new ArrayList<Integer>();
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
	
	/*
	 * Get the virtual machine id
	 */
	public int getVmId(){
		return this.id;
	}
	
	/*
	 * Set the cloudlet id list in the virtual machine
	 */
	public void setCloudletIdInVm(List<Integer>cloudletInVm){
		this.cloudletIdInVm = cloudletInVm;
	}

	/*
	 * Get the cloudlet id list in the virtual machine
	 */
	public List<Integer>getCloudletInVm(){
		return this.cloudletIdInVm;
	}
	/*
	 * Insert cloudlet id to the list
	 */
	public void insertToVm(int index,Integer cloudletId){
		this.cloudletIdInVm.add(index, cloudletId);
	}
	/*
	 * Set the maximize voltage and frequency level for the virtual machine
	 */
	public void setMaxVfLevel(int maxVfLevel){
		this.maxVfLevel = maxVfLevel;
	}
	/*
	 * Get the maximize voltage and frequency level for the virtual machine
	 */
	public int getMaxVfLevel(){
		return this.maxVfLevel;
	}
}
