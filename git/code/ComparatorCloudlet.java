package code;
import java.util.Comparator;

public class ComparatorCloudlet implements Comparator {
	public int compare(Object arg0,Object arg1){
		Cloudlet cloudlet1 = (Cloudlet)arg0;
		Cloudlet cloudlet2 = (Cloudlet)arg1;
		if((cloudlet1.getUpRankValue() - cloudlet2.getUpRankValue()) > 0)
			return -1;
		else if((cloudlet1.getUpRankValue() - cloudlet2.getUpRankValue()) < 0)
			return 1;
		else return 0;
		
	}

}
