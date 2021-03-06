package code;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*	任务类*/
public class Cloudlet {
	
	/*	cloudletId*/
	private final int cloudletId;
	
	/*	该任务前驱任务列表*/
	private List<Integer>preCloudletList;
	
	/*	该任务后继任务列表*/
	private List<Integer>sucCloudletList;
	
	/*	该任务的向上排序值*/
	private double upRankValue;
	
	/*	执行该任务的vmId*/
	private int vmId;
	
	/*	The current voltage and frequency level of virtual machine assigned to*/
	private int level;
	
	/*	Actual start time and actual finish time of the cloudlet*/
	private double ast;
	private double aft;

	public double getAst() {
		return ast;
	}

	public void setAst(double ast) {
		this.ast = ast;
	}

	public double getAft() {
		return aft;
	}

	public void setAft(double aft) {
		this.aft = aft;
	}
	
	//任务的最早开始时间，最迟完成时间
	private double est;
	private double lft;
	//设置任务的最早开始时间，最迟完成时间
	public void setEST(double est){
		this.est = est;
	}
	public double getEST(){
		return this.est;
	}
	public void setLFT(double lft){
		this.lft = lft;
	}
	public double getLFT(){
		return this.lft;
	}

	
	/*	设置该任务的前驱任务列表*/
	public void setPreCloudletList(List pcl){
		this.preCloudletList = pcl;
	}
	
	/*	构造函数*/
	Cloudlet(int cloudletId,int vmId,int upRankValue){

		preCloudletList = new ArrayList<Integer>();
		sucCloudletList = new ArrayList<Integer>();

		this.cloudletId = cloudletId;
		this.vmId = -1;
		this.upRankValue = upRankValue;
		this.aft = 0;
		this.aft = 0;
		this.level = 0;
	}
	
	
	/*	获取该任务前驱任务列表*/
	public List getPreCloudletList(){
		return preCloudletList;
	}
	
	/*	设置该任务的后继任务列表*/
	public void setSucCloudletList(List scl){
		this.preCloudletList = scl;
	}
	
	/*	获取该任务后继任务列表*/
	public List getSucCloudletList(){
		return sucCloudletList;
	}

	public int getCloudletId() {
		return cloudletId;
	}
	public void addToPreCloudletList(int preCloudletId){
		this.preCloudletList.add(preCloudletId);
	}

	public void addToSucCloudletList(int sucCloudletId){
		this.sucCloudletList.add(sucCloudletId);
	}
	
	/*	设置upRankValue值*/
	public void setUpRankValue(double upRankValue){
		this.upRankValue = upRankValue;
	}
	public double getUpRankValue(){
		return upRankValue;
	}
	/*	setVmId()*/
	public void setVmId(int vmId){
		this.vmId = vmId;
	}
	/*	getVmId()*/
	public int getVmId(){
		return vmId;
	}
	/*
	 * Get current voltage and frequency level
	 */
	public int getLevel(){
		return this.level;
	}
	/*
	 * Set current voltage and frequency level
	 */
	public void setLevel(int level){
		this.level = level;
	}

}
