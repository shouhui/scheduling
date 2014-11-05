package code;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;

/*
 * The broker
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
	
	/*	Cloudlet id in virtual machine*/
	List<Integer>cloudletIdInVm = new ArrayList<Integer>();
	
	/*	The current makespan of all the cloudlets*/
	double curM = 0;
	
	/*	The makespan when all the cloudlets in maximize frequency*/
	double baseM = 0;
	
	/*	The makespan when all the cloudlets in minimize frequency*/
	double extraM = 0;
	
	/*	The current overall energy consumption of all the cloudlets*/
	double curE = 0;
	
	/*	The overall energy consumption of all the cloudlets after HEFT scheduling*/
	double baseE = 0;
	
	/*	The current overall direct evergy consumption of all the cloudlets */
	double curED = 0;
	
	/*	The deadline */
	double deadline = 0;
	
	/*	The constant for calculating energy consumption*/
	double P = 1;
	
	/*	The constant for calculating deadline*/
	double B = 1.0;
	
	/*	The constant for selecting virtual machine when rescaling by group*/
	double C = 1.01;
	
	/*	The temporary cost of all the cloudlets for vms*/
	private List<Double[]>computeCost;
	
	/*	The temporary cost of all the cloudlets in the assigned virtual machine after HEFT scheduling*/
	private List<Double>computeCostA;
	
	/*	The temporary actual start time and actual finish time of all the cloudlets*/
	private List<Double[]>exeTime;
	
	/*	The backup actual start time and actual finish time of all the cloudlets*/
	private List<Double[]>exeTimeBackup;
	
	/*
	 * Start the scheduling
	 */
	public void start(){
		initialize();
		double savedRatioGTI =  GTI();
		double savedRatioEES = EES();
		double savedRatioESFS = ESFS();
		
		//	Write the saved ratio result to result.txt
		writeFile(savedRatioGTI,savedRatioEES,savedRatioESFS);
	}
	
	public void writeFile(double a,double b,double c){
		try{
			String fileName = "src/code/result.txt";
//			BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
			BufferedWriter output = new BufferedWriter(new FileWriter(fileName,true));
		
			output.write(a + " " + b + " " + c);
			output.write('\n');
			
			output.flush();
			output.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	
	/*
	 * Initiaize
	 */
	public void initialize(){
		/*	获取cloudletList*/
		setCloudletList(dag.getCloudletList());
		computeCost = new ArrayList<Double[]>();
		exeTime = new ArrayList<Double[]>();
		
		//	Using HEFT scheduling with minimize frequency
		//	Initialize list exeTime
		for(int i = 0;i < cloudletList.size();i++){
			Double[]temp = {(double) 0,(double) 0};
			exeTime.add(temp);
		}

		//	Set the temporary cost of all the cloudlets for vms*/
		for(int i = 0;i < cloudletList.size();i++){
			Cloudlet cl = getCloudletById(i);
			Double[]costTemp = new Double[vmList.size()];
			for(Vm vm:vmList){
				int vmId = vm.getVmId();
				costTemp[vmId] = VmComputeCost.getVmComputeCost(i, vmId)*(vm.getVfListByLevel(0)[1]/vm.getVfListByLevel(vm.getMaxVfLevel())[1]);
			}
			computeCost.add(costTemp);
		}
	}
	
	/*
	 * ESFS algorithm
	 */
	public double ESFS(){
		//	The maxMode for all the virtual machine, is used to set a lower bound in each iteration.
		int maxMode = 0;
		//	The current mode in the iteration
		int curMode = 0;
		//	The phase for different period
		int phase = 1;
		//	The threshold for energy gain
		double thre = 0;
		
		System.out.println("Starting the ESFS algorithm....");
		//	Recover the HEFT scheduling result
		for(Cloudlet cl:cloudletList){
			int cloudletId = cl.getCloudletId();
			Double[]temp = { exeTimeBackup.get(cloudletId)[0],exeTimeBackup.get(cloudletId)[1]};
			cl.setAst(temp[0]);
			cl.setAft(temp[1]);
			cl.setLevel(0);
		}
		for(int i = 0;i <= cloudletList.size() - 1;i++){
			Double[]temp = { exeTimeBackup.get(i)[0],exeTimeBackup.get(i)[1]};
//			computeCost.set(i, temp);
			setExeTimeTemp(i,temp);
		}
		List<Double>computeCostTemp = new ArrayList<Double>();
		for(int i = 0;i <= cloudletList.size() - 1;i++){
			computeCostTemp.add(getCloudletById(i).getAft() - getCloudletById(i).getAst());
		}
		List<Integer>vmLevelTemp = new ArrayList<Integer>();
		for(int i = 0;i <= cloudletList.size() - 1;i++){
			vmLevelTemp.add(0);
		}

//		for(Cloudlet cl:cloudletList){
//			System.out.println("cloudletId:" + cl.getCloudletId() + "    vmId:" + cl.getVmId() + "    ast:" + cl.getAst() + "    aft:" + cl.getAft() + "    exeTime:" + (cl.getAft() - cl.getAst()) +"  level:" + cl.getLevel());
//		}
		
		//	Set maxMode
		for(Vm vm:vmList){
			maxMode = ( maxMode > vm.getMaxVfLevel() )?maxMode:vm.getMaxVfLevel();
		}

		//	While the current mode is not corresponded to the lowest frequency continue loop.
		List<Cloudlet>energySortedCloudlet = new ArrayList<Cloudlet>();
		List<Integer>w = new ArrayList<Integer>();
		List<Double[]>exeLoopBack = new ArrayList<Double[]>();
		for(int i = 0;i <= cloudletList.size() - 1;i++){
			Double[]temp = { exeTimeBackup.get(i)[0],exeTimeBackup.get(i)[1]};
			exeLoopBack.add(i, temp);
		}
		while( curMode <= maxMode ){
			//	Get current energy
			//	Compute the energy consumption of all the cloudlets, and the idle energy consumption.
			double cloudletESum = 0;
			double cloudletEDSum = 0;
			double makespanTemp = 0;
			for(Cloudlet cl:cloudletList){
				int level = cl.getLevel();
				double exe = exeTime.get(cl.getCloudletId())[1] - exeTime.get(cl.getCloudletId())[0];
				cloudletEDSum += computeCloudletE(cl,level,exe);
			}
			makespanTemp = exeTime.get(cloudletList.size() - 1)[1];
			double vmETemp = 0;
			for(Vm vmIdle:vmList){
				double workTime = 0;
				for(int cloudletIdInVm:vmIdle.getCloudletInVm()){
					workTime += computeCostTemp.get(cloudletIdInVm);
				}
				vmETemp += computeIdleE(vmIdle,makespanTemp - workTime);
			}
			curE = cloudletEDSum + vmETemp;
//			System.out.println(curE);
			//	Next iterate
			curMode++;
			//	Add all the cloudlets in cloudletList copy into list w
			for(int i = 0;i <= cloudletList.size() - 1;i++){
				w.add(cloudletList.get(i).getCloudletId());
				energySortedCloudlet.add(cloudletList.get(i));
			}

			//	Iterator in one mode
			while(!energySortedCloudlet.isEmpty()){
				
				energySortedCloudlet.removeAll(energySortedCloudlet);
//				computeLFT(computeCostTemp);
//				for(Cloudlet cl:cloudletList){
//					System.out.println(cl.getCloudletId() + " " + cl.getLFT());
//				}
				double maxGain = 0;
				int maxGainClId = -1;
				for(int clEGainId:w){
					//	Get energy gain
					Cloudlet clEGain = getCloudletById(clEGainId);
					double gainTemp = getEnergyGain(clEGain,vmLevelTemp);
//					System.out.println(gainTemp);
					if(gainTemp > thre){
						energySortedCloudlet.add(clEGain);
						if( gainTemp > maxGain ){
							maxGain = gainTemp;
							maxGainClId = clEGain.getCloudletId();
						}
					}
					
				}
				w.remove(Integer.valueOf(maxGainClId));
				if(maxGainClId == -1){
					break;
				}
				//	The select cloudlet id is maxGainClId
				Cloudlet selectCl = getCloudletById(maxGainClId);
				Vm vm = vmList.get(selectCl.getVmId());
				if( (vmLevelTemp.get(maxGainClId) == vm.getMaxVfLevel()) || (curMode > vm.getMaxVfLevel() ) ){
					continue;
				}else{
					double computeCostBack = computeCostTemp.get(maxGainClId);
					double exeTemp = computeCostTemp.get(maxGainClId)*vm.getVfListByLevel(vmLevelTemp.get(maxGainClId))[1]/vm.getVfListByLevel(curMode)[1];
					double temp = exeTime.get(maxGainClId)[0] + exeTemp;

					//	Backup the execute time
					for(int i = 0;i <= cloudletList.size() - 1;i++){
						Double[]exeBackT = { exeTime.get(i)[0] , exeTime.get(i)[1] };
						exeLoopBack.set(i, exeBackT);
					}
					
//					if( temp <= selectCl.getLFT() ){
//						Double[] tempA = { exeTime.get(maxGainClId)[0],temp };
//						exeTime.set(maxGainClId, tempA);
//						computeCostTemp.set(maxGainClId, exeTemp);
//						vmLevelTemp.set(maxGainClId, curMode);
//						//	Reassign the cloudlets
//							//	Reset the state
//						for(Vm _vm:vmList){
//							_vm.setAvailTime(0);
//						}
//						reAssignCloudlet(cloudletList, computeCostTemp);
//					}
					
					
					//	Reassign the cloudlets
					//	Reset the state
					for(Vm _vm:vmList){
						_vm.setAvailTime(0);
					}

					reAssignCloudlet(cloudletList, computeCostTemp);
					if( exeTime.get(cloudletList.size() - 1)[1] > deadline ){
						computeCostTemp.set(maxGainClId, computeCostBack);
						for(int i = 0;i <= cloudletList.size() - 1;i++){
							Double[]exeBackT = { exeLoopBack.get(i)[0] , exeLoopBack.get(i)[1] };
							exeTime.set(i, exeBackT);
						}
						continue;
					}else{
						computeCostTemp.set(maxGainClId, exeTemp);
						vmLevelTemp.set(maxGainClId, curMode);
					}
				}
			}
			
			//	Get the current energy consumption
			//	Compute the energy consumption of all the cloudlets, and the idle energy consumption.
			cloudletESum = 0;
			cloudletEDSum = 0;
			makespanTemp = 0;
			for(Cloudlet cl:cloudletList){
				int level = vmLevelTemp.get(cl.getCloudletId());
				double exe = exeTime.get(cl.getCloudletId())[1] - exeTime.get(cl.getCloudletId())[0];
				cloudletEDSum += computeCloudletE(cl,level,exe);
			}
			makespanTemp = exeTime.get(cloudletList.size() - 1)[1];
			vmETemp = 0;
			for(Vm vmIdle:vmList){
				double workTime = 0;
				for(int cloudletIdInVm:vmIdle.getCloudletInVm()){
					workTime += computeCostTemp.get(cloudletIdInVm);
				}
				vmETemp += computeIdleE(vmIdle,makespanTemp - workTime);
			}
			double newE = cloudletEDSum + vmETemp;
			
			//	If reduction in frequency doesn't bring energy gain in phase 1, the loop continues in phase 2, else accept the reduction in frequency.
			if( (newE >= curE) && (phase == 1)){
				phase = 2;
				curMode--;
				deadline = exeTime.get(cloudletList.size() - 1)[1];
			}else{
				for(Cloudlet cl:cloudletList){
					cl.setAst(exeTime.get(cl.getCloudletId())[0]);
					cl.setAft(exeTime.get(cl.getCloudletId())[1]);
					cl.setLevel(vmLevelTemp.get(cl.getCloudletId()));
				}
			}
		}
//		for(Cloudlet cl:cloudletList){
//			System.out.println("cloudlet" + (cl.getCloudletId()+1) + "  vm:" + (cl.getVmId() + 1) + "  level:" + vmLevelTemp.get(cl.getCloudletId()) + "    " + exeTime.get(cl.getCloudletId())[0] + " " + exeTime.get(cl.getCloudletId())[1]);
//		}
		for(Cloudlet cl:cloudletList){
			System.out.println("cloudletId:" + cl.getCloudletId() + "    vmId:" + cl.getVmId() + "    ast:" + cl.getAst() + "    aft:" + cl.getAft() + "    exeTime:" + (cl.getAft() - cl.getAst()) +"  level:" + cl.getLevel());
		}
		//	Get current energy
		//	Compute the energy consumption of all the cloudlets, and the idle energy consumption.
		//	Compute the energy consumption of all the cloudlets
		double cloudletESum = 0;
		double cloudletEDSum = 0;
		double makespanTemp = 0;
		for(int i = 0;i <= cloudletList.size() - 1;i++){
			Cloudlet curCl = getCloudletById(i);
			int level = curCl.getLevel();
			double exeTime = curCl.getAft() - curCl.getAst();
			cloudletEDSum += computeCloudletE(curCl,level,exeTime);
		}
		
		makespanTemp = cloudletList.get(cloudletList.size() - 1).getAft();
		double vmETemp = 0;
		double[]workTime = new double[vmList.size()];
		for(Cloudlet clWork:cloudletList){
			for(Vm vmWork:vmList){
				if(clWork.getVmId() == vmWork.getVmId()){
					workTime[clWork.getVmId()] += ( clWork.getAft() - clWork.getAst() );
				}
			}
		}
		for(Vm vmE:vmList){
			vmETemp += computeIdleE(vmE,makespanTemp - workTime[vmE.getVmId()]);
		}
		curE = cloudletEDSum + vmETemp;
		System.out.println(cloudletEDSum + " " + vmETemp);
		double savedRatioESFS = (baseE - curE)/baseE;
		System.out.println( "B:" + B + " " + "    ESFS Energy saving ratio:" + savedRatioESFS );
		return savedRatioESFS;
	}
	
	/*
	 * Get energy gain
	 */
	public double getEnergyGain(Cloudlet cl,List<Integer>vmLevelTemp){
		int vmId = cl.getVmId();
		int level = vmLevelTemp.get(cl.getCloudletId());
		if( level == vmList.get(vmId).getMaxVfLevel() ){
			return 0;
		}
		double timeTemp = (exeTimeBackup.get(cl.getCloudletId())[1] - exeTimeBackup.get(cl.getCloudletId())[0]) * vmList.get(vmId).getVfListByLevel(0)[1]/vmList.get(vmId).getVfListByLevel(level)[1];  
		double preEnergy = computeCloudletE(cl,level,timeTemp);
		double sucEnergy = computeCloudletE(cl,level + 1,timeTemp*vmList.get(vmId).getVfListByLevel(level)[1]/vmList.get(vmId).getVfListByLevel(level+1)[1]);
		return ( preEnergy - sucEnergy )/preEnergy;
	}
	
	/*
	 * EES algorithm
	 */
	public double EES(){
		System.out.println("Starting the EES algorithm....");
		//	Recover the HEFT scheduling result
		for(Cloudlet cl:cloudletList){
			int cloudletId = cl.getCloudletId();
			Double[]temp = exeTimeBackup.get(cloudletId);
			cl.setAst(temp[0]);
			cl.setAft(temp[1]);
			cl.setLevel(0);
		}
//		for(Cloudlet cl:cloudletList){
//			System.out.println("cloudletId:" + cl.getCloudletId() + "    vmId:" + cl.getVmId() + "    ast:" + cl.getAst() + "    aft:" + cl.getAft() + "    exeTime:" + (cl.getAft() - cl.getAst()) +"  level:" + cl.getLevel());
//		}
		System.out.println(" deadline:" + deadline + "    baseEnergy:" + baseE);
		
		//	Reassign the actual finish time and actual start time based on deadline
		reAssignExeTime();
		//	Compute the earliest start time and latest finish time
		computeEST();
		computeLFT();
		
		List<Cloudlet> slackList = new ArrayList<Cloudlet>();
		for(int i=0;i<cloudletList.size();i++){
			slackList.add(cloudletList.get(i));
		}
		List<Integer>cloudletIdInVm = new ArrayList<Integer>();
		List<Cloudlet>sTemp = new ArrayList<Cloudlet>();
		Cloudlet cl;
		Cloudlet cloudletCurrent;
		//	Select the cloudlet with maximize latest finish time, until list slackList is null
		while(true)
		{	
			int curVmId = -1;
			double max = 0;
			//	If list  slackList is empty break the loop
			if(slackList.isEmpty()){
				break;
			}
			//	Select the cloudlet with maximize latest finish time
			for(int i = 0;i < vmList.size();i++){
				double temp = 0;
				if(vmList.get(i).getCloudletInVm().isEmpty()){
					continue;
				}else{
					temp = getCloudletById(vmList.get(i).getCloudletInVm().get(vmList.get(i).getCloudletInVm().size()-1)).getLFT();
				}
				if(temp > max){
					max = temp;
					curVmId = i;
				}
			}
//			System.out.println(vmList.get(curVmId).getCloudletInVm());
			if(curVmId == -1){
				break;
			}
			cloudletIdInVm = vmList.get(curVmId).getCloudletInVm();
			cl = getCloudletById(cloudletIdInVm.get(cloudletIdInVm.size()-1));
//			System.out.println(cl.getCloudletId());
			cloudletCurrent = getCloudletById(cloudletIdInVm.get(cloudletIdInVm.size()-1));
//			System.out.println(curVmId);
			sTemp.add(cloudletCurrent);
			if(vmList.get(curVmId).getCloudletInVm().size() > 1){
				for(int i = vmList.get(curVmId).getCloudletInVm().size() - 2;i>=0;i--){
					Cloudlet cloudlet_1 = getCloudletById(cloudletIdInVm.get(i));
					if(cloudlet_1.getLFT() > cl.getEST()){
						sTemp.add(cloudlet_1);
						cl = cloudlet_1;
					}else{
						break;
					}
				}
			}
		
		
			//为该任务选取频率值
			double fTemp_1 = 0;
			double fTemp_2 = 0;
			fTemp_1 = (cloudletCurrent.getAft() - cloudletCurrent.getAst())/(cloudletCurrent.getLFT() - cloudletCurrent.getEST());
//			System.out.println(fTemp_1);
//			System.out.println(cloudletCurrent.getCloudletId());
			double sTempExe = 0;
			for(int i = 0;i<sTemp.size();i++){
				sTempExe += (sTemp.get(i).getAft() - sTemp.get(i).getAst() );
			}
			fTemp_2 = sTempExe/( sTemp.get(0).getLFT()  - sTemp.get(sTemp.size()-1).getEST());
			
			double fTemp = vmList.get(curVmId).getvfList().get(0)[1] * ((fTemp_1>fTemp_2)?fTemp_1:fTemp_2);
			
			//	Select the frequency for the select virtual machine
			int levelTemp = -1;
			double f = 0;
			for(int i = vmList.get(curVmId).getvfList().size()-1;i >= 0;i--){
				f = vmList.get(curVmId).getvfList().get(i)[1];
				levelTemp = i;
				if(f >= fTemp){
					break;
				}
			}
//			System.out.println(f);
			cloudletCurrent.setLevel(levelTemp);
			cloudletCurrent.setAft(cloudletCurrent.getLFT());
			cloudletCurrent.setAst( cloudletCurrent.getLFT() - VmComputeCost.getVmComputeCost(cloudletCurrent.getCloudletId(), curVmId)*( vmList.get(curVmId).getvfList().get(0)[1]/f ));
		
			computeLFT();
	
			//	Remove all the cloudlets in list sTemp
			while(!sTemp.isEmpty()){
				sTemp.remove(0);
			}

			//从slackList中移除该任务，当slacList为空时候则跳出循环
			for(int i = 0;i < slackList.size();i++){
				if(slackList.get(i).getCloudletId() == cloudletCurrent.getCloudletId()){
					slackList.remove(i);
					vmList.get(curVmId).getCloudletInVm().remove(vmList.get(curVmId).getCloudletInVm().size()-1);
				}
			}
//			System.out.println(cloudletIdInVm);

//			for(Cloudlet cP:cloudletList){
//				System.out.println("cloudlet"+(cP.getCloudletId()+1)+"    vm"+(cP.getVmId()+1)+"    "+cP.getAst()+" "+ cP.getAft() + " " + cP.getLevel());
//			}
//			System.out.println();
		}//while(true) end....	
		//	Compute the energy consumption of all the cloudlets
		double cloudletESum = 0;
		double cloudletEDSum = 0;
		double makespanTemp = 0;
		for(int i = 0;i <= cloudletList.size() - 1;i++){
			Cloudlet curCl = getCloudletById(i);
			int level = curCl.getLevel();
			double exeTime = curCl.getAft() - curCl.getAst();
			cloudletEDSum += computeCloudletE(curCl,level,exeTime);
		}
		
		makespanTemp = cloudletList.get(cloudletList.size() - 1).getAft();
		double vmETemp = 0;
		double[]workTime = new double[vmList.size()];
		for(Cloudlet clWork:cloudletList){
			for(Vm vmWork:vmList){
				if(clWork.getVmId() == vmWork.getVmId()){
					workTime[clWork.getVmId()] += ( clWork.getAft() - clWork.getAst() );
				}
			}
		}
		for(Vm vmE:vmList){
			vmETemp += computeIdleE(vmE,makespanTemp - workTime[vmE.getVmId()]);
		}
		curE = cloudletEDSum + vmETemp;
		for(Cloudlet clC:cloudletList){
			System.out.println("cloudletId:" + clC.getCloudletId() + "    vmId:" + clC.getVmId() + "    ast:" + clC.getAst() + "    aft:" + clC.getAft() + "    exeTime:" + (clC.getAft() - clC.getAst()) +"  level:" + clC.getLevel());
		}
		System.out.println(cloudletEDSum + " " + vmETemp);
		double savedRatio = (baseE - curE)/baseE;
		System.out.println( "B:" + B + " " + "    EES Energy saving ratio:" + savedRatio );
		return savedRatio;
	}
	
	/*
	 * Reassign the actual  finish time and actual start time based on deadline
	 */
	public void reAssignExeTime(){
		double deadlineRatio = deadline/baseM;
//		System.out.println(deadlineRatio + " " + deadline + " " + baseM);
		for(Cloudlet cl:cloudletList){
			if(cl.getAst() != 0){
				double exeTemp = cl.getAft() - cl.getAst();
				cl.setAft(cl.getAft() * deadlineRatio);
				cl.setAst(cl.getAft() - exeTemp);
			}
		}
//		for(Cloudlet cl:cloudletList){
//			System.out.println("cloudletId:" + cl.getCloudletId() + "    vmId:" + cl.getVmId() + "    ast:" + cl.getAst() + "    aft:" + cl.getAft() + "    exeTime:" + (cl.getAft() - cl.getAst()) +"  level:" + cl.getLevel());
//		}
	}
	/*
	 * Compute the latest finish time of all the cloudlets
	 */
	private void computeLFT() {
		cloudletList.get(cloudletList.size()-1).setLFT(deadline);
		for(Vm vm:vmList){
			vm.setAvailTime(deadline);
		}
		
		for(int cloudletListNum = cloudletList.size()-2;cloudletListNum>=0;cloudletListNum--){
			Cloudlet cloudletCurrent = cloudletList.get(cloudletListNum);
			Iterator<Integer> it = cloudletCurrent.getSucCloudletList().iterator();
			double max = Double.MAX_VALUE;
			while(it.hasNext()){
				double tem = 0;
				int sucCloudletIdCurrent = it.next();
				Cloudlet sucCloudletCurrent = getCloudletById(sucCloudletIdCurrent); 
				tem = sucCloudletCurrent.getLFT() - (sucCloudletCurrent.getAft() - sucCloudletCurrent.getAst()) - dag.getDependValue(cloudletCurrent.getCloudletId(), sucCloudletIdCurrent);
				if(cloudletCurrent.getVmId() == sucCloudletCurrent.getVmId()){
					tem += dag.getDependValue(cloudletCurrent.getCloudletId(), sucCloudletIdCurrent);
				}
				max = (tem < max)?tem:max;
			}
			if(vmList.get(cloudletCurrent.getVmId()).getAvailTime() < max){
				max = vmList.get(cloudletCurrent.getVmId()).getAvailTime();
			}
			cloudletCurrent.setLFT(max);
			vmList.get(cloudletCurrent.getVmId()).setAvailTime(max-( cloudletCurrent.getAft() - cloudletCurrent.getAst() ));
			
		}
//		for(Cloudlet cl:cloudletList){
//			System.out.println("cloudlet"+(cl.getCloudletId()+1) + "    lft:" + cl.getLFT());
//		}
	}
	private void computeLFT(List<Double>computeCostTemp){
		cloudletList.get(cloudletList.size()-1).setLFT(deadline);
		for(Vm vm:vmList){
			vm.setAvailTime(deadline);
		}
		for(int cloudletListNum = cloudletList.size()-2;cloudletListNum>=0;cloudletListNum--){
			Cloudlet cloudletCurrent = cloudletList.get(cloudletListNum);
			Iterator<Integer> it = cloudletCurrent.getSucCloudletList().iterator();
			double max = Double.MAX_VALUE;
			while(it.hasNext()){
				double tem = 0;
				int sucCloudletIdCurrent = it.next();
				Cloudlet sucCloudletCurrent = getCloudletById(sucCloudletIdCurrent); 
				tem = sucCloudletCurrent.getLFT() - computeCostTemp.get(sucCloudletIdCurrent) - dag.getDependValue(cloudletCurrent.getCloudletId(), sucCloudletIdCurrent);
				if(cloudletCurrent.getVmId() == sucCloudletCurrent.getVmId()){
					tem += dag.getDependValue(cloudletCurrent.getCloudletId(), sucCloudletIdCurrent);
				}
				max = (tem < max)?tem:max;
			}
			if(vmList.get(cloudletCurrent.getVmId()).getAvailTime() < max){
				max = vmList.get(cloudletCurrent.getVmId()).getAvailTime();
			}
			cloudletCurrent.setLFT(max);
			vmList.get(cloudletCurrent.getVmId()).setAvailTime(max - computeCostTemp.get(cloudletListNum));
			
		}
//		for(Cloudlet cl:cloudletList){
//			System.out.println("cloudlet"+(cl.getCloudletId()+1) + "    lft:" + cl.getLFT());
//		}

	}
	/*
	 * Compute the earliest start time of all the cloudlets
	 */
	private void computeEST() {
		cloudletList.get(0).setEST(0);
		for(Vm vm:vmList){
			vm.setAvailTime(0);
		}
		for(int cloudletListNum = 1;cloudletListNum<cloudletList.size();cloudletListNum++){
			Cloudlet cloudletCurrent = cloudletList.get(cloudletListNum);
			Iterator<Integer> it = cloudletCurrent.getPreCloudletList().iterator();
			double min = Double.MIN_VALUE;
			while(it.hasNext()){
				double tem = 0;
				int preCloudletIdCurrent = it.next();
				Cloudlet preCloudletCurrent = getCloudletById(preCloudletIdCurrent);
				tem = preCloudletCurrent.getEST() + (preCloudletCurrent.getAft() - preCloudletCurrent.getAst()) + dag.getDependValue(preCloudletIdCurrent, cloudletCurrent.getCloudletId());
				if(preCloudletCurrent.getVmId() == cloudletCurrent.getVmId()){
					tem -= dag.getDependValue(preCloudletIdCurrent, cloudletCurrent.getCloudletId());
				}
				min = (tem > min)?tem:min;
			}
			if(min < vmList.get(cloudletCurrent.getVmId()).getAvailTime()){
				min = vmList.get(cloudletCurrent.getVmId()).getAvailTime();
			}
			cloudletCurrent.setEST(min);
			vmList.get(cloudletCurrent.getVmId()).setAvailTime(min+cloudletCurrent.getAft() - cloudletCurrent.getAst());
		}
//		for(Cloudlet cl:cloudletList){
//			System.out.println("cloudlet"+(cl.getCloudletId()+1)+"   est:"+cl.getEST());
//		}
	}
	
	public void HEFT(){
		System.out.println("Using the minimize frequency....");
		bindCloudletToVmHEFT();
		extraM = getExeTimeTemp(cloudletList.size() - 1)[1];
		
		//	Using HEFT scheduling with maximize frequency
		//	Set the temporary cost of all the cloudlets for vms*/
		computeCost = new ArrayList<Double[]>();
		exeTime = new ArrayList<Double[]>();
		for(int i = 0;i < cloudletList.size();i++){
			Cloudlet cl = getCloudletById(i);
			Double[]costTemp = new Double[vmList.size()];
			for(Vm vm:vmList){
				int vmId = vm.getVmId();
				costTemp[vmId] = VmComputeCost.getVmComputeCost(i, vmId);
			}
			computeCost.add(i,costTemp);
		}
		for(int i = 0;i < cloudletList.size();i++){
			Double[]temp = {(double) 0,(double)0};
			exeTime.add(temp);
		}
		for(Vm vm:vmList){
			vm.setAvailTime(0);
			vm.setCloudletIdInVm(new ArrayList<Integer>());
		}
		
		System.out.println("Using the maximize frequency....");
		bindCloudletToVmHEFT();
		baseM = getExeTimeTemp(cloudletList.size() - 1)[1];
		
		//	Set the execute time for all the cloudlets after HEFT scheduling
		computeCostA = new ArrayList<Double>();
		for(int i = 0;i <= cloudletList.size() - 1;i++){
			computeCostA.add( (Double)( getExeTimeTemp(i)[1] - getExeTimeTemp(i)[0]) );
//			System.out.println(getExeTimeTemp(i)[1] - getExeTimeTemp(i)[0]);
		}
//		System.out.println(computeCostA);
		
		//	Set actual start time and actual finish time
		for(Cloudlet cl:cloudletList){
			int cloudletId = cl.getCloudletId();
			cl.setAst(getExeTimeTemp(cloudletId)[0]);
			cl.setAft(getExeTimeTemp(cloudletId)[1]);
		}
		
		curM = cloudletList.get(cloudletList.size() - 1).getAft();
		//	Compute the overall energy consumption after HEFT scheduling
		double energyDTemp = 0;
		double energyITemp = 0;
		for(Cloudlet cl:cloudletList){
			int level = cl.getLevel();
			double exeTime = cl.getAft() - cl.getAst();
			energyDTemp += computeCloudletE(cl,level,exeTime);
		}
		for(Vm vm:vmList){
			double workTime = 0;
			Iterator<Integer>it = vm.getCloudletInVm().iterator();
			while(it.hasNext()){
				int cloudletId = it.next();
				Cloudlet cl = getCloudletById(cloudletId);
				workTime += ( cl.getAft() - cl.getAst() );
			}
			double idleTime = curM - workTime;
			energyITemp += computeIdleE(vm,idleTime);
		}
		curED = energyDTemp;
		curE = energyDTemp + energyITemp;
		baseE = curE;
		
		Collections.sort(cloudletList, new Comparator<Cloudlet>(){
			public int compare(Cloudlet cl1,Cloudlet cl2){
				return ((Double)cl1.getAst()).compareTo((Double)cl2.getAst());
			}
		});
		
		//	Generate the deadline
		deadline = baseM + B*(extraM - baseM);
//		System.out.println( extraM + " " + baseM + " " + deadline + " " + baseE);
		System.out.println(" deadline: " + deadline + "    baseEnergy:" + baseE);
		
		for(Cloudlet cl:cloudletList){
			System.out.println("cloudletId:" + cl.getCloudletId() + "    vmId:" + cl.getVmId() + "    ast:" + cl.getAst() + "    aft:" + cl.getAft() + " " + (cl.getAft() - cl.getAst()));
		}

		//	Back up the HEFT scheduling result
		exeTimeBackup = new ArrayList<Double[]>();
		for(int i = 0;i <= cloudletList.size() - 1;i++){
			Cloudlet cl = getCloudletById(i);
			Double[]temp = {cl.getAst(),cl.getAft()};
			exeTimeBackup.add(temp);
		}
	}
	
	/*
	 * The GTI algorithm
	 */
	public double GTI(){
		System.out.println("Strarting the GTI algorithm....");
		HEFT();
		
		//	Reset the state of all the virtual machines
		for(Vm vm:vmList){
			vm.setAvailTime(0);
		}
		//	Rescaling by group
		rescaleByG();
		for(Cloudlet cl:cloudletList){
			System.out.println("cloudletId:" + cl.getCloudletId() + "    vmId:" + cl.getVmId() + "    ast:" + cl.getAst() + "    aft:" + cl.getAft() + " " + (cl.getAft() - cl.getAst()) + "    levle:" + cl.getLevel());
		}

		//	Rescaling by individual
		rescaleByI();
		for(Cloudlet cl:cloudletList){
			System.out.println("cloudletId:" + cl.getCloudletId() + "    vmId:" + cl.getVmId() + "    ast:" + cl.getAst() + "    aft:" + cl.getAft() + " " + (cl.getAft() - cl.getAst()) + "    levle:" + cl.getLevel());
		}
		double savedRatio = (baseE - curE)/baseE;
		System.out.println( "B:" + B + " " + "   GIT Energy saving ratio:" + savedRatio );
		return savedRatio;
	}
	
	/*	HEFT algorithm*/
	public void bindCloudletToVmHEFT(){
		System.out.println("    apply the HEFT propicy.");
		/*	Set cloudletIdToCloudletMap*/
		cloudletIdToCloudletMap = new HashMap<Integer,Cloudlet>();
		for(int i=0;i<cloudletList.size();i++){
			cloudletIdToCloudletMap.put(i, cloudletList.get(i));
		}

		/*	Compute up rank value of all the cloudlets*/
		computeUpRankValue();
		
		/*	Sort the cloudlet list with up rank value*/
		ComparatorCloudlet comparator = new ComparatorCloudlet();
		Collections.sort(cloudletList,comparator);

		//	Assign all the cloudlet to the virtual machines
		for(Cloudlet cl:cloudletList){
			assignInsert(cl);
		}
	}

	
	/*
	 * Rescaling all the cloudlets by individual
	 */
	public void rescaleByI(){
		System.out.println("    rescaling all the cloudlets by individual");
		boolean contI = true;
//		System.out.println(computeCostA);
		List<Double>computeCostTemp = new ArrayList<Double>();
		for(int i = 0;i <= cloudletList.size() - 1;i++){
			computeCostTemp.add(computeCostA.get(i));
		}
		while(contI){
			int selectCloudletId = -1;
			double maxSavedE = Double.NEGATIVE_INFINITY;
			//	Reset the temporary cloudlet compute cost for all the cloudlets after scaling in some virtual machine
			for(Cloudlet cl:cloudletList){
				computeCostTemp.set(cl.getCloudletId(),computeCostA.get(cl.getCloudletId()));
			}
			for(Cloudlet cl:cloudletList){
				int cloudletId = cl.getCloudletId();
				Vm vm = vmList.get(cl.getVmId());
				double cloudletESum = 0;
				double cloudletEDSum = 0;
				double savedETemp = 0;
				double makespanTemp = 0;
				for(int i = 0;i <= cloudletList.size() - 1;i++){
					Double[] temp = {(Double)0.0,(Double)0.0};
					setExeTimeTemp(i, temp);
				}
				for(int i = 0;i <= vmList.size() - 1;i++){
					vmList.get(i).setAvailTime(0);
				}
				//	Reset the temporary cloudlet compute cost for all the cloudlets after scaling in some virtual machine
				for(int i = 0;i <= cloudletList.size() - 1;i++){
					computeCostTemp.set(i, computeCostA.get(i));
				}
				if(cl.getLevel() == vm.getMaxVfLevel()){
					continue;
				}else{
					Double temp = computeCostA.get(cloudletId) * ( vm.getVfListByLevel(cl.getLevel())[1]/vm.getVfListByLevel(cl.getLevel() + 1)[1] );
					computeCostTemp.set(cloudletId, temp);
				}
				//	Reassign the cloudlet cl
				reAssignCloudlet(cloudletList,computeCostTemp);
				//	Compute the energy consumption of all the cloudlets
				for(int i = 0;i <= cloudletList.size() - 1;i++){
					Cloudlet curCl = getCloudletById(i);
					Vm curVm = vmList.get(curCl.getVmId());
					int level = curCl.getLevel();
					if( (cloudletId == i)&&( level != curVm.getMaxVfLevel() )){
						level += 1;
					}
					double exeTime = getExeTimeTemp(i)[1] - getExeTimeTemp(i)[0];
					cloudletEDSum += computeCloudletE(curCl,level,exeTime);
				}
				makespanTemp = getExeTimeTemp(cloudletList.size() - 1)[1];
				double vmETemp = 0;
				for(Vm vmIdle:vmList){
					double workTime = 0;
					for(int cloudletIdInVm:vmIdle.getCloudletInVm()){
						workTime += computeCostTemp.get(cloudletIdInVm);
					}
					vmETemp += computeIdleE(vmIdle,makespanTemp - workTime);
				}
				cloudletESum = cloudletEDSum + vmETemp;
				savedETemp = curE - cloudletESum;
				
				if( (makespanTemp <= deadline)&&(savedETemp >= 0)&&( savedETemp >= maxSavedE ) ){
					selectCloudletId = cloudletId;
					maxSavedE = savedETemp;
				}
			}
			if( selectCloudletId == -1 ){
				contI = false;
			}else{
				//	Apply the scaling in the selected cloudlet
				//	Reset the state
				for(Cloudlet cl:cloudletList){
					Double[] temp = {(Double)0.0,(Double)0.0};
					setExeTimeTemp(cl.getCloudletId(),temp);
				}
				for(Vm vm_reset:vmList){
					vm_reset.setAvailTime(0);
				}
				//	Reset the temporary cloudlet compute cost for all the cloudlets after scaling in some virtual machine
				for(Cloudlet cl:cloudletList){
					computeCostTemp.set(cl.getCloudletId(),computeCostA.get(cl.getCloudletId()));
				}
				//	Increase the level for the selected cloudlet, and calculate the makespan and energy saving value after that
				Cloudlet selectCl = getCloudletById(selectCloudletId);
				
				Double temp = computeCostA.get(selectCloudletId) * ( vmList.get(selectCl.getVmId()).getVfListByLevel(selectCl.getLevel())[1]/vmList.get(selectCl.getVmId()).getVfListByLevel(selectCl.getLevel() + 1)[1] );
				computeCostTemp.set(selectCloudletId, temp);
				if(selectCl.getLevel()!=vmList.get(selectCl.getVmId()).getMaxVfLevel()){
					selectCl.setLevel(selectCl.getLevel() + 1);
				}else{
					contI = false;
				}
				reAssignCloudlet(cloudletList,computeCostTemp);
				for(Cloudlet cl:cloudletList){
					computeCostA.set(cl.getCloudletId(), computeCostTemp.get(cl.getCloudletId()));
				}
				for(Cloudlet cl:cloudletList){
					cl.setAst(getExeTimeTemp(cl.getCloudletId())[0]);
					cl.setAft(getExeTimeTemp(cl.getCloudletId())[1]);
				}
				//	Compute the energy consumption of all the cloudlets
				double cloudletESum = 0;
				double cloudletEDSum = 0;
				double makespanTemp = 0;
				for(Cloudlet cl:cloudletList){
					int level = cl.getLevel();
					double exeTime = getExeTimeTemp(cl.getCloudletId())[1] - getExeTimeTemp(cl.getCloudletId())[0];
					cloudletEDSum += computeCloudletE(cl,level,exeTime);
				}
				makespanTemp = getExeTimeTemp(cloudletList.size() - 1)[1];
				double vmETemp = 0;
				for(Vm vmIdle:vmList){
					double workTime = 0;
					for(int cloudletIdInVm:vmIdle.getCloudletInVm()){
						workTime += computeCostTemp.get(cloudletIdInVm);
					}
					vmETemp += computeIdleE(vmIdle,makespanTemp - workTime);
				}
				curE = cloudletEDSum + vmETemp;
			}
		}
	}
	
	/*
	 * Rescaling all the cloudlets by group
	 */
	public void rescaleByG(){
		System.out.println("    rescaling all the cloudlets by group");
		boolean contG = true;
		List<Double>computeCostTemp = new ArrayList<Double>();
		for(int i = 0;i <= cloudletList.size() - 1;i++){
			computeCostTemp.add(computeCostA.get(i));
		}
		while(contG){
			int selectVmId = -1;
			double maxSavedED = Double.NEGATIVE_INFINITY;
			//	Reset the temporary cloudlet compute cost for all the cloudlets after scaling in some virtual machine
			for(Cloudlet cl:cloudletList){
				computeCostTemp.set(cl.getCloudletId(),computeCostA.get(cl.getCloudletId()));
			}
			
			for(Vm vm:vmList){
				double cloudletESum = 0;
				double cloudletEDSum = 0;
				double savedEDTemp = 0;
				double makespanTemp = 0;
				//	Reset the state
				for(Cloudlet cl:cloudletList){
					Double[] temp = {(Double)0.0,(Double)0.0};
					setExeTimeTemp(cl.getCloudletId(),temp);
				}
				for(Vm vm_reset:vmList){
					vm_reset.setAvailTime(0);
				}
				
				//	Reset the temporary cloudlet compute cost for all the cloudlets after scaling in some virtual machine
				for(Cloudlet cl:cloudletList){
					computeCostTemp.set(cl.getCloudletId(),computeCostA.get(cl.getCloudletId()));
				}
				
				if(vm.getCloudletInVm().isEmpty()){
					continue;
				}

				//	Tentatively increase the level for all the cloudlets in the vm, and calculate the makespan and energy saving value after that
				for(int cloudletId:vm.getCloudletInVm()){
					Cloudlet cl = getCloudletById(cloudletId);
					if(cl.getLevel() == vm.getMaxVfLevel()){
						continue;
					}
					Double temp = computeCostA.get(cloudletId) * ( vm.getVfListByLevel(cl.getLevel())[1]/vm.getVfListByLevel(cl.getLevel() + 1)[1] );
					computeCostTemp.set(cloudletId, temp);
				}
				
			//	If the energy saving value is heavier, and satisfied the deadline, slect the vm
				//	Reassign the cloudlets in the select virtual machine
				reAssignCloudlet(cloudletList,computeCostTemp);
				
				//	Compute the energy consumption of all the cloudlets
				for(Cloudlet cl:cloudletList){
					int level = cl.getLevel();
					if(vm.getCloudletInVm().contains(cl.getCloudletId()) && (cl.getLevel() != vm.getMaxVfLevel()) ){
						level += 1;
					}
					double exeTime = getExeTimeTemp(cl.getCloudletId())[1] - getExeTimeTemp(cl.getCloudletId())[0];
					cloudletEDSum += computeCloudletE(cl,level,exeTime);
				}
				savedEDTemp = curED - cloudletEDSum;
				makespanTemp = getExeTimeTemp(cloudletList.size() - 1)[1];
				double vmETemp = 0;
				for(Vm vmIdle:vmList){
					double workTime = 0;
					for(int cloudletIdInVm:vmIdle.getCloudletInVm()){
						workTime += computeCostTemp.get(cloudletIdInVm);
					}
					vmETemp += computeIdleE(vmIdle,makespanTemp - workTime);
				}
				cloudletESum = cloudletEDSum + vmETemp;
				
				if( (makespanTemp <= deadline)&&( cloudletESum <= C*baseE )&&( savedEDTemp > maxSavedED ) ){
					selectVmId = vm.getVmId();
					maxSavedED = savedEDTemp;
//					System.out.println("......");
				}
				
			}

			if(selectVmId == -1){
				//	Quite the loop
				contG = false;
			}else{
				//	Apply the scaling in the virtual machine whose id is selectVmId
				//	Reset the state
				for(Cloudlet cl:cloudletList){
					Double[] temp = {(Double)0.0,(Double)0.0};
					setExeTimeTemp(cl.getCloudletId(),temp);
				}
				for(Vm vm_reset:vmList){
					vm_reset.setAvailTime(0);
				}
				//	Reset the temporary cloudlet compute cost for all the cloudlets after scaling in some virtual machine
				for(Cloudlet cl:cloudletList){
					computeCostTemp.set(cl.getCloudletId(),computeCostA.get(cl.getCloudletId()));
				}
				//	Increase the level for all the cloudlets in the vm, and calculate the makespan and energy saving value after that
				Vm vm = vmList.get(selectVmId);
				for(int cloudletId:vm.getCloudletInVm()){
					Cloudlet cl = getCloudletById(cloudletId);
					if(cl.getLevel() == vm.getMaxVfLevel()){
						contG = false;
						break;
					}
					Double temp = computeCostA.get(cloudletId) * ( vm.getVfListByLevel(cl.getLevel())[1]/vm.getVfListByLevel(cl.getLevel() + 1)[1] );
					computeCostTemp.set(cloudletId, temp);
					cl.setLevel(cl.getLevel() + 1);
				}
				reAssignCloudlet(cloudletList,computeCostTemp);
				for(Cloudlet cl:cloudletList){
					computeCostA.set(cl.getCloudletId(), computeCostTemp.get(cl.getCloudletId()));
				}
				for(Cloudlet cl:cloudletList){
					cl.setAst(getExeTimeTemp(cl.getCloudletId())[0]);
					cl.setAft(getExeTimeTemp(cl.getCloudletId())[1]);
				}
				
				//	Compute the energy consumption of all the cloudlets
				double cloudletESum = 0;
				double cloudletEDSum = 0;
				double makespanTemp = 0;
				for(Cloudlet cl:cloudletList){
					int level = cl.getLevel();
//					System.out.println(level);
					double exeTime = getExeTimeTemp(cl.getCloudletId())[1] - getExeTimeTemp(cl.getCloudletId())[0];
					cloudletEDSum += computeCloudletE(cl,level,exeTime);
				}
				makespanTemp = getExeTimeTemp(cloudletList.size() - 1)[1];
				double vmETemp = 0;
				for(Vm vmIdle:vmList){
					double workTime = 0;
					for(int cloudletIdInVm:vmIdle.getCloudletInVm()){
						workTime += computeCostTemp.get(cloudletIdInVm);
					}
					vmETemp += computeIdleE(vmIdle,makespanTemp - workTime);
				}
				curE = cloudletEDSum + vmETemp;
				curED = cloudletEDSum;
			}
		}
		
	}
	
	/*
	 * Reassign the cloudlets in the select virtual machine
	 */
	public void reAssignCloudlet(List<Cloudlet>cloudletList,List<Double>computeCostTemp){
		for(Cloudlet cl:cloudletList){
			int cloudletId = cl.getCloudletId();
			int vmId = cl.getVmId();
			double preMax = 0;
			Iterator<Integer>preIt = cl.getPreCloudletList().iterator();
			while(preIt.hasNext()){
				int preCloudletId = preIt.next();
				Cloudlet preCl = getCloudletById(preCloudletId);
				double temp = getExeTimeTemp(preCloudletId)[1];
				if( preCl.getVmId() != cl.getVmId() ){
					temp += dag.getDependValue(preCloudletId, cloudletId);
				}
				preMax = (preMax > temp)?preMax:temp;
			}
			preMax = (preMax > vmList.get(cl.getVmId()).getAvailTime())?preMax:vmList.get(cl.getVmId()).getAvailTime();
			Double[]exeTemp = {preMax,preMax + computeCostTemp.get(cloudletId)};
			setExeTimeTemp(cl.getCloudletId(),exeTemp);
			vmList.get(cl.getVmId()).setAvailTime(preMax + computeCostTemp.get(cloudletId));
		}
	}
	
	/*
	 * Assign the cloudlet
	 */
	private void assignInsert(Cloudlet cl) {
		if(insert(cl)){
			return;
		}
		int minVmId = -1;
		double minValue = Double.MAX_VALUE;
		for(Vm vm:vmList){
			Iterator<Integer> itPre = cl.getPreCloudletList().iterator();
			int vmId = vm.getVmId();
			double maxTem = 0;
			while(itPre.hasNext()){
				int preCloudletIdTem = itPre.next();
				Cloudlet preCl = getCloudletById(preCloudletIdTem);
				double tem = getExeTimeTemp(preCloudletIdTem)[1];
				if(preCl.getVmId() != vmId){
					tem += dag.getDependValue(preCloudletIdTem, cl.getCloudletId());
				}
				maxTem = (maxTem > tem)?maxTem:tem;
			}
			maxTem = (maxTem > vm.getAvailTime())?maxTem:vm.getAvailTime();
			maxTem += computeCost.get(cl.getCloudletId())[vmId];
			if(maxTem < minValue){
				minValue = maxTem;
				minVmId = vmId;
			}
		}
		//	Assign the cloudlet cl to the virtual machine whose id is minVmId
		Double[] exeTime = {minValue - computeCost.get(cl.getCloudletId())[minVmId],minValue};
		setExeTimeTemp(cl.getCloudletId(),exeTime);
		
		cl.setVmId(minVmId);
		vmList.get(minVmId).setAvailTime(minValue);
		Integer id = cl.getCloudletId();
		vmList.get(minVmId).insertToVm(vmList.get(minVmId).getCloudletInVm().size(), id);
	}
	
	/*
	 * Judge whether the cloudlet cl could be insert
	 */
	private boolean insert(Cloudlet cl) {
		for(Vm vm:vmList){
			if(vm.getCloudletInVm().size() < 2){
				continue;
			}
			//	Compute the earliest start time of cloudlet cl
			double preMax = 0;
			Iterator<Integer>itPre = cl.getPreCloudletList().iterator();
			while(itPre.hasNext()){
				int preCloudletId = itPre.next();
				Cloudlet preCl = getCloudletById(preCloudletId);
				double preTem = getExeTimeTemp(preCloudletId)[1];
				if(preCl.getVmId() != vm.getVmId()){
					preTem += dag.getDependValue(preCloudletId, cl.getCloudletId());
				}
				preMax = (preMax > preTem)?preMax:preTem;
			}

			for(int i = 0;i <= vm.getCloudletInVm().size() - 2;i++){
				Cloudlet preCl = getCloudletById(vm.getCloudletInVm().get(i));
				Cloudlet sucCl = getCloudletById(vm.getCloudletInVm().get(i + 1));
				double temp = (getExeTimeTemp(preCl.getCloudletId())[1] > preMax)?getExeTimeTemp(preCl.getCloudletId())[1]:preMax;
				if( (getExeTimeTemp(sucCl.getCloudletId())[0] - temp) >= computeCost.get(cl.getCloudletId())[vm.getVmId()] ){
					Double[] exeTime = {getExeTimeTemp(preCl.getCloudletId())[1],getExeTimeTemp(preCl.getCloudletId())[1] + computeCost.get(cl.getCloudletId())[vm.getVmId()]};
					setExeTimeTemp(cl.getCloudletId(),exeTime);
					cl.setVmId(vm.getVmId());
					vm.insertToVm(i + 1, cl.getCloudletId());
					System.out.println("Insert cloudlet:" + cl.getCloudletId() + " to vm:" + vm.getVmId());
					return true;
				}
			}
		}
		return false;
	}

	/*	计算cloudlet的向上排序值*/
	public void computeUpRankValue(){
		for(int i = cloudletList.size()-1;i>=0;i--){
			Cloudlet cl = getCloudletById(i);
			Double[]temp = computeCost.get(i);
			double sum = 0;
			for(Double d:temp){
				sum += d;
			}
			cl.setUpRankValue(sum/temp.length);
			double tem = 0;
			Iterator <Integer>it = cl.getSucCloudletList().iterator();
			while(it.hasNext()){
				int sucCloudletIdTem = it.next();
				Cloudlet sucCl = getCloudletById(sucCloudletIdTem);
				if((sucCl.getUpRankValue()+dag.getDependValue(i, sucCloudletIdTem))>tem){
					tem = sucCl.getUpRankValue()+dag.getDependValue(i, sucCloudletIdTem);
				}
			}
			tem+=cl.getUpRankValue();
			tem = (int)(tem*1000)/1000.0;
			cl.setUpRankValue(tem);
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
	
	/*
	 * Compute the energy consumption of the cloudlet
	 */
	public double computeCloudletE(Cloudlet cl,int level,double exeTime){
		int vmId = cl.getVmId();
		Double[]temp = vmList.get(vmId).getVfListByLevel(level);
		return P*(temp[0])*(temp[0])*(temp[1])*exeTime;
	}
	
	/*
	 * Compute the energy consumption of the virtual machine in idle period
	 */
	public double computeIdleE(Vm vm,double idleTime){
		int level = vm.getMaxVfLevel();
		Double[]temp = vm.getVfListByLevel(level);
		return P*(temp[0])*(temp[0])*(temp[1])*idleTime;
	}
	/*
	 * Set the temporary actual start time and actual finish time of all the cloudlets 
	 */
	public void setExeTimeTemp(int cloudletId,Double[]exeTime){
		this.exeTime.set(cloudletId, exeTime);
	}
	/*
	 * Get the temporary actual start time and actual finish time of all the cloudlets
	 */
	public Double[] getExeTimeTemp(int cloudletId){
		return this.exeTime.get(cloudletId);
	}
	
}
