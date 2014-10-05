package code;
import java.util.Map;
import java.util.HashMap;

public class VmComputeCost {

	/*	各个任务在各个vm上执行代价*/
	private static Map<Integer,double[]>vmComputeCostMap;
	/*	各个任务在各个vm上平均执行代价*/
	private static Map<Integer,Double>vmAveComputeCostMap;
	
	/*	设置vmComputeCostMap*/
	public void setVmComputeCostMap(Map cch){
		this.vmComputeCostMap = cch;
	}
	/*	获得computeCost*/
	public static double getVmComputeCost(int cloudletId,int vmId){
		return vmComputeCostMap.get(cloudletId)[vmId];
	}
	
	/*	设置vmAveComputeCostMap*/
	public void setAveVmComputeCostMap(Map acch){
		this.vmAveComputeCostMap = acch;
	}
	/*	获取vmAveComputeCost*/
	public static double getVmAveComputeCost(int cloudletId){
		return vmAveComputeCostMap.get(cloudletId);
	}
}
