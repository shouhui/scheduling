package code;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/*	DAG类*/
public class DAG {

	/*	任务列表*/
	private List<Cloudlet>cloudletList;
	
	/*	任务间依赖关系Map表*/
	private Map<String,String>cloudletDependMap;
	
	/*	任务间传递数据量Map表*/
	private Map<String,Double>cloudletDependValueMap;

	/*	构造函数*/
	DAG(){
		
	}
	
//	/*	提交cloudletList至DAG*/
//	public void submitCloudletList(List cl){
//		this.cloudletList = cl;
//	}
//	
//	/*	提交任务间依赖关系Map表*/
//	public void submitCloudletDependMap(Map cd){
//		this.cloudletDependMap = cd;
//	}
//	
//	/*	提交任务间依赖关系值Map表*/
//	public void submitCloudletDependValueMap(Map cdv){
//		this.cloudletDependValueMap = cdv;
//	}
//	
	/*	判断cloudlet间是否有依赖关系*/
	public boolean isDepend(String src,String des){
		if(cloudletDependValueMap.containsKey(src+" "+des)){
			return true;
		}
		else return false;
	}
	
	/*	获取cloudlet间依赖值*/
	public double getDependValue(int src,int des){
		return cloudletDependValueMap.get(String.valueOf(src)+" "+String.valueOf(des));
	}
	
	/*	设置cloudletList*/
	public void setCloudletList(List cl){
		this.cloudletList = cl;
	}
	/*	获得cloudletList*/
	public List getCloudletList(){
		return cloudletList;		
	}
	
	/*	设置cloudlet间依赖*/
	public void setCloudletDependMap(Map cd){
		this.cloudletDependMap = cd;
	}
	/*	获得cloudletDependMap*/
	public Map getCloudletDependMap(){
		return cloudletDependMap;
	}
	
	/*	设置cloudlet间依赖值*/
	public void setCloudletDependValueMap(Map cdv){
		this.cloudletDependValueMap = cdv;
	}
	/*	获得cloudlet间依赖值*/
	public Map getCloudletDependValueMap(){
		return cloudletDependValueMap;
	}

}
