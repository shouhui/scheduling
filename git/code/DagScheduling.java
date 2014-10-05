package code;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/*
 * Title: DAG scheduling Toolkit
 * Description: DAG scheduling Toolkit using different algorithm
 * Date: Oct3, 2014
 */
public class DagScheduling {
	/** The cloudlet list */
	private static List<Cloudlet>cloudletList;
	
	/** The vm list */
	private static List<Vm>vmList;
	
	/** The voltage and frequency list */
	private static List<Double[]>vfList;
	
	/**	The deadline*/
	private static double deadline;

	/** The dependency of all the cloudlets	*/
	private static Map<String,String>cloudletDependMap;
	
	/**	The dependency value of all the cloudlets */
	private static Map<String,Double>cloudletDependValueMap;
	
	/** The compute cost of the vm for the cloudlet */
	private static Map<Integer,double[]>vmComputeCostMap;
	
	/** The average compute cost of the vm for the cloudlet */
	private static Map<Integer,Double>vmAveComputeCostMap;

	/**
	 * Create the main() to run.
	 * @param args
	 * @throws Throwable 
	 */
	
	public static void main(String[] args) throws Throwable {
		
		System.out.println("Starting the main function....");
		
		try {
			// 1. Initialize the scheduling parameter.
			//	Generate the DAG, and then store in DAG.txt
			
			//	Generate the cloudlet processing time for all the vms, and then store in processorCost.txt
			
			
			// 2. Create cloudlet
			int cloudletNum = 10;
			DAG dag = new DAG();
			cloudletList = createCloudlet(cloudletNum);
			dag.setCloudletList(cloudletList);
			
			createCloudletDepend();
			dag.setCloudletDependMap(cloudletDependMap);
			dag.setCloudletDependValueMap(cloudletDependValueMap);
			
			// 3. Create broker
			Broker broker = new Broker();
			broker.submitDAG(dag);

			// 4. Create virtual machine
			
			//	vm properties
			int vmNum = 5;
			vmList = createVm(vmNum);
			broker.submitVm(vmList);

			createVmComputeCost();
			VmComputeCost vcc = new VmComputeCost();
			vcc.setVmComputeCostMap(vmComputeCostMap);
			vcc.setAveVmComputeCostMap(vmAveComputeCostMap);
			
//			for(int i = 0;i < cloudletNum;i++){
//				for(int j = 0;j < 3;j++){
//					System.out.print(vcc.getVmComputeCost(i, j)+" ");
//				}
//				System.out.println();
//			}
			// 6. Start the scheduling
			broker.bindCloudletToVmHEFT();			
			// 7. Print the result

		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Unwanted errors happen.");
		}
	}


	/*
	 * Create vms
	 */
	private static void createVM(int vmNum) throws IOException {
		int count = 0;
		BufferedReader bf = new BufferedReader(new FileReader("src/code/DV.txt"));
		String buffered ;
		while((buffered = bf.readLine()) != null){
			Vm vm = new Vm(count,0);
			vmList.add(vm);
			count++;
			vfList = new ArrayList<Double[]>();
			String bufferedArray[] = buffered.split(",");
			for(String s:bufferedArray){
				String strTemp[] = s.split(" ");
//				System.out.println(s);
				Double douTemp[] = {Double.parseDouble(strTemp[0]),Double.parseDouble(strTemp[1])};
				vfList.add(douTemp);
			}
			vm.setVfList(vfList);
		}
	}
	/*
	 * Create cloudlets
	 */
	private static List<Cloudlet>createCloudlet(int cloudletNum){
		List<Cloudlet>list = new ArrayList<Cloudlet>();
		for(int i = 0;i < cloudletNum;i++){
			Cloudlet cl = new Cloudlet(i,-1,0);
			list.add(cl);
		}
		return list;
	}
	/*
	 * Create cloudlet dependency
	 */
	private static void createCloudletDepend() throws Throwable{
		BufferedReader bd = new BufferedReader(new FileReader("src/code/DAG.txt"));
		String buffered;
		cloudletDependMap = new HashMap<String,String>();
		cloudletDependValueMap = new HashMap<String,Double>();
		
		while((buffered=bd.readLine())!=null){
			String bufferedArray[] = buffered.split(" ");
			cloudletDependMap.put(bufferedArray[0], bufferedArray[1]);
//			System.out.println(cloudletDependMap.get(bufferedArray[0]));
			cloudletDependValueMap.put(bufferedArray[0]+" "+bufferedArray[1], Double.parseDouble(bufferedArray[2]));
//			System.out.println(cloudletDependValueMap.get(bufferedArray[0]+" "+bufferedArray[1]));

		/*	添加任务的前驱，后继*/
			int tem0 = Integer.parseInt(bufferedArray[0]);
			int tem1 = Integer.parseInt(bufferedArray[1]);

			cloudletList.get(tem0).addToSucCloudletList(tem1);
			cloudletList.get(tem1).addToPreCloudletList(tem0);
		}
		bd.close();
	}
	/*
	 * Create virtual machines
	 */
	private static List<Vm> createVm(int vmNum){
		vmList = new ArrayList<Vm>();
		for(int iVmCreate = 0;iVmCreate<vmNum;iVmCreate++){
			Vm vm = new Vm(iVmCreate,0);
			vmList.add(vm);
		}
		return vmList;
	}
	/*
	 * Create virtual machine compute time
	 */
	private static void createVmComputeCost() throws IOException{
		vmComputeCostMap = new HashMap<Integer,double[]>();
		vmAveComputeCostMap = new HashMap<Integer,Double>();
		String buffered;
		BufferedReader bd = new BufferedReader(new FileReader("src/code/processorCost.txt"));
		int num = 0;
		while((buffered=bd.readLine())!=null){
			String bufferedArray[] = buffered.split(" ");
			double[] bufferedDouble = new double[bufferedArray.length];
			double sum = 0;
			for(int i=0;i<bufferedArray.length;i++){
				bufferedDouble[i] = Double.parseDouble(bufferedArray[i]);
				sum += bufferedDouble[i];
			}
			vmComputeCostMap.put(num, bufferedDouble);
			vmAveComputeCostMap.put(num, sum/bufferedArray.length);
			num++;
		}
		bd.close();
	}

}
