package code;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/*	DAG��*/
public class DAG {

	/*	�����б�*/
	private List<Cloudlet>cloudletList;
	
	/*	�����������ϵMap��*/
	private Map<String,String>cloudletDependMap;
	
	/*	����䴫��������Map��*/
	private Map<String,Double>cloudletDependValueMap;

	/*	���캯��*/
	DAG(){
		
	}
	
//	/*	�ύcloudletList��DAG*/
//	public void submitCloudletList(List cl){
//		this.cloudletList = cl;
//	}
//	
//	/*	�ύ�����������ϵMap��*/
//	public void submitCloudletDependMap(Map cd){
//		this.cloudletDependMap = cd;
//	}
//	
//	/*	�ύ�����������ϵֵMap��*/
//	public void submitCloudletDependValueMap(Map cdv){
//		this.cloudletDependValueMap = cdv;
//	}
//	
	/*	�ж�cloudlet���Ƿ���������ϵ*/
	public boolean isDepend(String src,String des){
		if(cloudletDependValueMap.containsKey(src+" "+des)){
			return true;
		}
		else return false;
	}
	
	/*	��ȡcloudlet������ֵ*/
	public double getDependValue(int src,int des){
		return cloudletDependValueMap.get(String.valueOf(src)+" "+String.valueOf(des));
	}
	
	/*	����cloudletList*/
	public void setCloudletList(List cl){
		this.cloudletList = cl;
	}
	/*	���cloudletList*/
	public List getCloudletList(){
		return cloudletList;		
	}
	
	/*	����cloudlet������*/
	public void setCloudletDependMap(Map cd){
		this.cloudletDependMap = cd;
	}
	/*	���cloudletDependMap*/
	public Map getCloudletDependMap(){
		return cloudletDependMap;
	}
	
	/*	����cloudlet������ֵ*/
	public void setCloudletDependValueMap(Map cdv){
		this.cloudletDependValueMap = cdv;
	}
	/*	���cloudlet������ֵ*/
	public Map getCloudletDependValueMap(){
		return cloudletDependValueMap;
	}

}
