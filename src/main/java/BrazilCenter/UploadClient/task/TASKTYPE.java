package BrazilCenter.UploadClient.task;

/**
 * 
 * @author Fuli Ma
 *
 */
public enum TASKTYPE {
	NewTask, // get the task from the scan directory.
	FailedTask,	 // get the task from the FailedRecords directory.
	ReTransTask, // receive the task from the reuploadSerivce. 
	LogFile	
}
