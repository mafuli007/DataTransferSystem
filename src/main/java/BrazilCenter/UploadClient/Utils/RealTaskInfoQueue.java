package BrazilCenter.UploadClient.Utils;

import java.util.LinkedList;
import java.util.Queue;

public class RealTaskInfoQueue {
		
	private Queue<String> realTaskInfoList; 	//Store the task info.
	
	RealTaskInfoQueue() {
		realTaskInfoList = new LinkedList<String>();
	}

	/********************Methods about realtaskinfo********************/
	public void addRealTaskinfo(String msg){
		synchronized(this){
			if(this.realTaskInfoList.size() > Utils.QUEUETASKINFOSIZE){
				this.realTaskInfoList.poll();
			}
			this.realTaskInfoList.add(msg);
		}
	}
	public int getRealTaskinfoSize(){
		synchronized(this){
			return this.realTaskInfoList.size();
		}
	}
	
	public String getRealTaskinfo(){
		synchronized(this){
			return this.realTaskInfoList.poll();
		}
	}
	
}
