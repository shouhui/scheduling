package code;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
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
	
	private static double ccrRatio = 1;

	/**
	 * Create the main() to run.
	 * @param args
	 * @throws Throwable 
	 */
	
	public static void main(String[] args) throws Throwable {
		int times = 50;
		System.out.println("Starting the main function....");
		
		//	Delete the ratio result
		try{
			String fileName = "src/code/result.txt";
			BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
			output.write("");
			output.flush();
			output.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		
		try {
			//	Execute the experiment
			for(int i = 0;i < times;i++){
				metaExe();
			}
			
			//	Compute the average saved ratio
			computeAveR(times);
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Unwanted errors happen.");
		}
		
		
	}
	
	public static void computeAveR(int times) throws IOException{
		String fileName = "src/code/result.txt";
		String buffered;
		double[]ratio = new double[3];
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		while((buffered = br.readLine()) != null){
//			System.out.println(buffered);
			String bufferedStr[] = buffered.split(" ");
			for(int i = 0;i <= ratio.length - 1;i++){
				ratio[i] += Double.parseDouble(bufferedStr[i]);
			}
		}
		System.out.println("The average saved ratio is " + "    GTI:" + ratio[0]/times + "    EES:" + ratio[1]/times + "    ESFS:" + ratio[2]/times);
	}
	
	//	Base execute
	public static void metaExe() throws Throwable{
		// 1. Initialize the scheduling parameter.
		Broker broker = new Broker();

		//	Montage DAG generate
		int cloudletNum = DAGGeneInitial(broker);		//	CHANGE..................................
		
		//	Generate the DAG, and then store in DAG.txt

		// 2. Create cloudlet

		// 4. Create virtual machine
		
		//	vm properties
		int vmNum = 8;
//		vmList = createVm(vmNum);
		vmList = createVmRandom(vmNum);
		broker.submitVm(vmList);
		
		generateCost(cloudletNum,vmNum);

		createVmComputeCost();
		VmComputeCost vcc = new VmComputeCost();
		vcc.setVmComputeCostMap(vmComputeCostMap);
		vcc.setAveVmComputeCostMap(vmAveComputeCostMap);
		
//		for(int i = 0;i < cloudletNum;i++){
//			for(int j = 0;j < 3;j++){
//				System.out.print(vcc.getVmComputeCost(i, j)+" ");
//			}
//			System.out.println();
//		}
		// 6. Start the scheduling
		broker.start();	
		// 7. Print the result

	}

	public static int DAGGeneInitial(Broker broker) throws Throwable{
//		DAGMontage montage = new DAGMontage();		//	CHANGE.............................................
		DAGMontage dagAll = new DAGMontage();
		dagAll.generateRandomFile(ccrRatio);
		int cloudletNum = 34;		//	CHANGE..............................................................
		DAG dag = new DAG();
		cloudletList = createCloudlet(cloudletNum);
		dag.setCloudletList(cloudletList);
		
		createCloudletDepend();
		dag.setCloudletDependMap(cloudletDependMap);
		dag.setCloudletDependValueMap(cloudletDependValueMap);
		
		// 3. Create broker
		broker.submitDAG(dag);
		return cloudletNum;
	}
	
	
	/*
	 * Generate the of all the cloudlets for all the virtual machines
	 */
	public static void generateCost(int cloudletNum,int vmNum){
		try{
			String fileName = "src/code/proCostAll.txt";		//		
			BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
			
			for(int i = 0;i <= cloudletNum - 1;i++){
				for(int j = 0;j <= vmNum - 1;j++){
//					output.write(Math.random()*100,0,5);
					Double temp = Math.random()*100;
					output.write(temp.toString(),0,6);
					if(j != vmNum - 1){
						output.write(' ');
					}else{
						output.write('\n');
					}
				}
			}
			output.flush();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Create vms
	 */
	private static List<Vm> createVm(int vmNum) throws IOException {
		List<Vm>list = new ArrayList<Vm>();
		int count = 0;
		BufferedReader bf = new BufferedReader(new FileReader("src/code/DV.txt"));
		String buffered ;
		while( ((buffered = bf.readLine()) != null) && (count < vmNum)){
			Vm vm = new Vm(count,0);
			list.add(vm);
			count++;
			vfList = new ArrayList<Double[]>();
			String bufferedArray[] = buffered.split(",");
			for(String s:bufferedArray){
				String strTemp[] = s.split(" ");
				Double douTemp[] = {Double.parseDouble(strTemp[0]),Double.parseDouble(strTemp[1])};
				vfList.add(douTemp);
			}
			vm.setVfList(vfList);
			vm.setMaxVfLevel(vfList.size() - 1);
		}
		bf.close();
		return list;
	}
	private static List<Vm> createVmRandom(int vmNum) throws IOException {
		List<Vm>list = new ArrayList<Vm>();
		int count = 0;
		while( count < vmNum ){
			String buffered ;
			BufferedReader bf = new BufferedReader(new FileReader("src/code/DV.txt"));
			double border = (int)(Math.random() * 5);		//	The number of DVs is 5.
//			System.out.println(border);
			int i = 0;
			while( ((buffered = bf.readLine()) != null) && i < border ){
				i++;
			}
//			System.out.println(buffered);
			

			Vm vm = new Vm(count,0);
			list.add(vm);
			count++;
			vfList = new ArrayList<Double[]>();
			String bufferedArray[] = buffered.split(",");
			for(String s:bufferedArray){
				String strTemp[] = s.split(" ");
				Double douTemp[] = {Double.parseDouble(strTemp[0]),Double.parseDouble(strTemp[1])};
				vfList.add(douTemp);
			}
			vm.setVfList(vfList);
			vm.setMaxVfLevel(vfList.size() - 1);
			bf.close();
		}
		return list;
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
//		BufferedReader bd = new BufferedReader(new FileReader("src/code/DAGMontage.txt"));
		BufferedReader bd = new BufferedReader(new FileReader("src/code/DAGAll.txt"));

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
	 * Create virtual machine compute time
	 */
	private static void createVmComputeCost() throws IOException{
		vmComputeCostMap = new HashMap<Integer,double[]>();
		vmAveComputeCostMap = new HashMap<Integer,Double>();
		String buffered;
		BufferedReader bd = new BufferedReader(new FileReader("src/code/proCostAll.txt"));
//		BufferedReader bd = new BufferedReader(new FileReader("src/code/processorCost.txt"));

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
