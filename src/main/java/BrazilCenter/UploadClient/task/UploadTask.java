package BrazilCenter.UploadClient.task;

import BrazilCenter.UploadClient.scanner.FileObj;

/**
 * phoenix 20150127
 * */
public class UploadTask {
	
	private long taskId;
	private int tryCount;	 
	private String destinationAddress;   
	private String sourceAddress; 
	private FileObj fileobj; 
	
	private TASKSTATUS taskStatus;	
	private TASKTYPE taskType;	

	public UploadTask(){
		this.tryCount = 1;
	}
	
	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public String getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public FileObj getFileobj() {
		return fileobj;
	}

	public void setFileobj(FileObj fileobj) {
		this.fileobj = fileobj;
	}

	public TASKSTATUS getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(TASKSTATUS taskStatus) {
		this.taskStatus = taskStatus;
	}

	public int getTryCount() {
		return tryCount;
	}

	public void setTryCount(int tryCount) {
		this.tryCount = tryCount;
	}

	public void addTryCount(){
		this.tryCount++;
	}
	
	public void resetTryCount(){
		this.tryCount = 1;
	}
	
	public TASKTYPE getTaskType() {
		return taskType;
	}

	public void setTaskType(TASKTYPE taskType) {
		this.taskType = taskType;
	}

}
