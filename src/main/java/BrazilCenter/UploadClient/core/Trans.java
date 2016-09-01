package BrazilCenter.UploadClient.core;

import java.io.File;

import BrazilCenter.UploadClient.Utils.CacheScanFileList;
import BrazilCenter.UploadClient.Utils.Configuration;
import BrazilCenter.UploadClient.Utils.LogUtils;
import BrazilCenter.UploadClient.Utils.UploadReport;
import BrazilCenter.UploadClient.Utils.Utils;
import BrazilCenter.UploadClient.Utils.XMLOperator;
import BrazilCenter.UploadClient.scanner.FileObj;
import BrazilCenter.UploadClient.task.TASKSTATUS;
import BrazilCenter.UploadClient.task.TASKTYPE;
import BrazilCenter.UploadClient.task.UploadTask;
import BrazilCenter.UploadClient.tcp.TcpClient;

public class Trans implements Runnable {

	private Configuration conf;
	TcpClient monitor_client;

	public Trans(Configuration config) {
		this.conf = config;
		this.monitor_client = new TcpClient(conf.getMonitorServerIp(), conf.getMonitorServerPort());
		monitor_client.start();
	}

	/***
	 * (1) copy the file to the back up directory. (2) delete the source file.
	 * (3) delete the file name from the cache file list.
	 * @param report
	 * @param task
	 */
	public void handleSuccessedTask(UploadTask task) {

		FileObj srcFileObj = task.getFileobj();
		String fileName = srcFileObj.getFilename();
		String backUpPath = srcFileObj.getBackUpAddress();
		String srcFullFName = srcFileObj.getFilePath() + File.separator + srcFileObj.getFilename();
		String backUpFullFName = backUpPath + File.separator + srcFileObj.getRelativePath() + File.separator
				+ srcFileObj.getFilename();

		task.setTaskStatus(TASKSTATUS.SUCCESS);
		/** if the file type is newTask then back up the file. */
		if (task.getTaskType() == TASKTYPE.NewTask) {
			if (false == Utils.CopyFile(srcFullFName, backUpFullFName)) {
				LogUtils.logger.error("Failed to backup file: " + srcFileObj.getFilename());
			}
		}

		/** if the file type is newTask or FailedTask then delete the file. */
		if ((task.getTaskType() == TASKTYPE.NewTask) || (task.getTaskType() == TASKTYPE.FailedTask)) {
			if (false == Utils.delFile(new File(srcFullFName))) { // failed to delete the file. 
				LogUtils.logger.error("Failed to delete file: " + srcFullFName);
			}else{ // delete successfully. 
				CacheScanFileList.RemoveFromCacheScanFileList(fileName);
			}
		}
	}

	public void handleFaieldTask(UploadTask task) {
		
		task.setTaskStatus(TASKSTATUS.FAILED);
		int count = task.getTryCount();
		if (count < Utils.MaxTryCount) {
			task.addTryCount();
			Utils.TaskQueue.AddTask(task);
		} else {
			
			FileObj srcFileObj = task.getFileobj();
			String fileName = srcFileObj.getFilename();
			String backUpPath = srcFileObj.getBackUpAddress();
			/**source file Name with path */
			String srcFullFName = srcFileObj.getFilePath() + File.separator + srcFileObj.getFilename();
			/**back up file name with path */
			String backUpFullFName = backUpPath + File.separator + srcFileObj.getRelativePath() + File.separator
					+ srcFileObj.getFilename();
			/** file name that going to be stored in err directory. */
			String errFullFName = Utils.ErrDataDir + File.separator + srcFileObj.getFilename();

			if(task.getTaskType() == TASKTYPE.NewTask){
				/**back up the file */
				if (false == Utils.CopyFile(srcFullFName, backUpFullFName)) {
					LogUtils.logger.error("Failed to backup file: " + srcFileObj.getFilename());
				}
				/**copy the file to the FailedRecords directory. */
				if (false == Utils.CopyFile(srcFullFName, errFullFName)) {
					LogUtils.logger.error("Failed to move file to FailedRecords : " + srcFileObj.getFilename());
				}
				/**delete from the cache file list. */
				CacheScanFileList.RemoveFromCacheScanFileList(fileName);
			}else if(task.getTaskType() == TASKTYPE.FailedTask){
				
				/**delete from the cache file list. */
				CacheScanFileList.RemoveFromCacheScanFileList(fileName);

			}else if(task.getTaskType() == TASKTYPE.ReTransTask){
				/**copy the file to the FailedRecords directory. */
				if (false == Utils.CopyFile(srcFullFName, errFullFName)) {
					LogUtils.logger.error("Failed to move file to FailedRecords : " + srcFileObj.getFilename());
				}
			}
		}
	}

	public void run() {
		// TODO Auto-generated method stub

		/** start the ftp client thread. */
		FtpClient ftpclient = new FtpClient(this.conf);
		if (!ftpclient.ConnectServer()) {
			LogUtils.logger.error(Thread.currentThread().getName() + " FTP disconnected! Try to reconnect......");
			ftpclient.ReConnect();
		} else {
			LogUtils.logger.info(Thread.currentThread().getName() + " FTP Connected!");
		}
		Thread ftpThread = new Thread(ftpclient);
		ftpThread.start();
		ftpclient.setWorkingPlace(conf.getDestinationAddress() + File.separator + "DATA/");

		while (true) {

			UploadTask task = null;
			boolean uploadReportResult = false;
			while ((task = Utils.TaskQueue.GetTask()) != null) {
				task.setTaskStatus(TASKSTATUS.UPLOADING);
				UploadReport report = ftpclient.FtpUploadFile(task);
				if (report != null) {
					if (report.getResult()) { // upload file success.
						uploadReportResult = ftpclient.FtpUploadReport(report);
						if (uploadReportResult == true) { // upload report
															// success.
							this.handleSuccessedTask( task);
						} else { // failed to upload the report.
							this.handleFaieldTask(task);
						}
					} else { // upload failed.
						this.handleFaieldTask(task);
					}
					monitor_client.SendRealTaskInfoMsg(XMLOperator.MakeXMLUploadTaskInfo(report, this.conf));
				} else {
					task.setTaskStatus(TASKSTATUS.WAIT);
					Utils.TaskQueue.AddTask(task);
				}
			}

			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
