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
}
