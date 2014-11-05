package code;
import java.util.Map;
import java.util.HashMap;

public class VmComputeCost {

	/*	���������ڸ���vm��ִ�д���*/
	private static Map<Integer,double[]>vmComputeCostMap;
	/*	���������ڸ���vm��ƽ��ִ�д���*/
	private static Map<Integer,Double>vmAveComputeCostMap;
	
	/*	����vmComputeCostMap*/
	public void setVmComputeCostMap(Map cch){
		this.vmComputeCostMap = cch;
	}
	/*	���computeCost*/
	public static double getVmComputeCost(int cloudletId,int vmId){
		return vmComputeCostMap.get(cloudletId)[vmId];
	}
	
	/*	����vmAveComputeCostMap*/
	public void setAveVmComputeCostMap(Map acch){
		this.vmAveComputeCostMap = acch;
	}
	/*	��ȡvmAveComputeCost*/
	public static double getVmAveComputeCost(int cloudletId){
		return vmAveComputeCostMap.get(cloudletId);
	}
}
