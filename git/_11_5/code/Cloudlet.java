package code;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*	������*/
public class Cloudlet {
	
	/*	cloudletId*/
	private final int cloudletId;
	
	/*	������ǰ�������б�*/
	private List<Integer>preCloudletList;
	
	/*	�������������б�*/
	private List<Integer>sucCloudletList;
	
	/*	���������������ֵ*/
	private double upRankValue;
	
	/*	ִ�и������vmId*/
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
	
	//��������翪ʼʱ�䣬������ʱ��
	private double est;
	private double lft;
	//������������翪ʼʱ�䣬������ʱ��
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

	
	/*	���ø������ǰ�������б�*/
	public void setPreCloudletList(List pcl){
		this.preCloudletList = pcl;
	}
	
	/*	���캯��*/
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
	
	
	/*	��ȡ������ǰ�������б�*/
	public List getPreCloudletList(){
		return preCloudletList;
	}
	
	/*	���ø�����ĺ�������б�*/
	public void setSucCloudletList(List scl){
		this.preCloudletList = scl;
	}
	
	/*	��ȡ�������������б�*/
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
	
	/*	����upRankValueֵ*/
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
