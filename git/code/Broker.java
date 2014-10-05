package code;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;

/*
 * dag.getDependValue(int src,int des)
 * vcc.getVmComputeCost(int cloudletId, int processorId)
 * cloudlet.getSucCloudletList()
 * cloudlet.setUpRankValue()
 * vm.setAvail()
 */
public class Broker {

	/*	DAG*/
	DAG dag;
	
	/*	vmList*/
	private List<Vm>vmList;
	
	/*	cloudletList*/
	private List<Cloudlet>cloudletList;

	/*	各个cloudlet的ast，aft列表*/
	private static Map<Integer,double[]>cloudletExeTimeMap;

	/*	cloudlet到cloudletId映射*/
	private Map<Integer,Cloudlet>cloudletIdToCloudletMap;

	/*	upRankValueMap*/
	private Map<Integer,Double>upRankValueMap;
	
	
	/*	HEFT算法分配cloudlet到vm*/
	public void bindCloudletToVmHEFT(){

		/*	cloudletExtTimeMap，key为cloudletId，value为任务的ast，aft*/
		cloudletExeTimeMap = new HashMap<Integer,double[]>();
		
		/*	获取cloudletList*/
		setCloudletList(dag.getCloudletList());

		/*	设置cloudletIdToCloudletMap*/
		cloudletIdToCloudletMap = new HashMap<Integer,Cloudlet>();
		for(int i=0;i<cloudletList.size();i++){
			cloudletIdToCloudletMap.put(i, cloudletList.get(i));
		}

		upRankValueMap = new HashMap<Integer,Double>();
		
		
		/*	计算cloudlet的向上排序值*/
		computeUpRankValue();

		/*	对任务按照向上排序值从大到小进行排序*/
		ComparatorCloudlet comparator = new ComparatorCloudlet();
		Collections.sort(cloudletList,comparator);

		for(Cloudlet cl:cloudletList){
			System.out.println(cl.getCloudletId() + " " + cl.getUpRankValue());
		}
	}
	
	/*	计算cloudlet的向上排序值*/
	public void computeUpRankValue(){
		for(int i = cloudletList.size()-1;i>=0;i--){
			cloudletList.get(i).setUpRankValue(VmComputeCost.getVmAveComputeCost(i));
//			cloudletList.get(iRankTem).upRankValue = cloudletList.get(iRankTem).meanTime;
			double tem = 0;
			Iterator <Integer>it = cloudletList.get(i).getSucCloudletList().iterator();
			while(it.hasNext()){
				int sucCloudletIdTem = it.next();
				
				if((cloudletList.get(sucCloudletIdTem).getUpRankValue()+dag.getDependValue(i, sucCloudletIdTem))>tem){
					tem = cloudletList.get(sucCloudletIdTem).getUpRankValue()+dag.getDependValue(i, sucCloudletIdTem);
				}
//				System.out.println(sucCloudletIdTem);
			}
			tem+=cloudletList.get(i).getUpRankValue();
			tem = (int)(tem*1000)/1000.0;
			cloudletList.get(i).setUpRankValue(tem);
//			System.out.println(cloudletList.get(i).getUpRankValue());
		}
	}

	/*	提交dag至broker*/
	public void submitDAG(DAG dag){
		this.dag = dag;
	}
	/*	提交vm至broker*/
	public void submitVm(List vmList){
		this.vmList = vmList;
	}

	/*	getCloudletById()*/
	public Cloudlet getCloudletById(int cloudletId){
		
		for(Cloudlet cl:cloudletList){
			if(cl.getCloudletId() == cloudletId)
				return cl;
		}
		return null;
//		return cloudletIdToCloudletMap.get(cloudletId);
	}

	/*	setCloudletList*/
	public void setCloudletList(List cl){
		this.cloudletList = cl;
	}
	
	/*	getCloudletEST()*/
	public double getCloudletAST(int cloudletId){
		return cloudletExeTimeMap.get(cloudletId)[0];
	}
	/*	getCloudletEFT()*/
	public double getCloudletAFT(int cloudletId){
		return cloudletExeTimeMap.get(cloudletId)[1];
	}
}
