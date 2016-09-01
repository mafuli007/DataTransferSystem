package BrazilCenter.UploadClient.scanner;

import java.util.*;

import BrazilCenter.UploadClient.Utils.CacheScanFileList;
import BrazilCenter.UploadClient.Utils.LogUtils;
import BrazilCenter.UploadClient.Utils.Utils;
import BrazilCenter.UploadClient.task.TASKSTATUS;
import BrazilCenter.UploadClient.task.TASKTYPE;
import BrazilCenter.UploadClient.task.UploadTask;

import java.io.*;

/**
 * 
 * @author maful
 *
 */
public class ErrDataScanner extends Thread {
	private List<FileObj> fileCacheList; // used to check if the file is ready.

	public ErrDataScanner() {
		this.fileCacheList = new LinkedList<FileObj>();
	}

	/**
	 * to test if file ready to be copied. if the file size doesn't change
	 * anymore, it means it's ready.
	 */
	private boolean isFileReady(File file) {

		/**
		 * try to find if the the file already exist. if the file already exist,
		 * then check if the length changes, if not then copy the file else
		 * update the file's length.
		 */
		for (int i = 0; i < this.fileCacheList.size(); i++) {
			FileObj tmpObj = this.fileCacheList.get(i);
			if (file.getName().equals(tmpObj.getFilename())) {
				this.fileCacheList.remove(i);
				// file already exist.
				if (file.length() == tmpObj.getFilesize()) { // same size
					return true;
				} else { // different size;
					tmpObj.setFilesize(file.length());
					this.fileCacheList.add(tmpObj);
					return false;
				}
			}
		}
		/** new file */
		FileObj obj = new FileObj();
		obj.setFilename(file.getName());
		obj.setFilesize(file.length());
		this.fileCacheList.add(obj);

		return false;
	}

	public void DirectoryScan(String scanAddress, String relativePath, List<FileObj> flist) {

		String address = scanAddress + File.separator + relativePath;
		File parentF = new File(address);
		if (!parentF.exists()) {
			LogUtils.logger.warn("FailedRecords Directory :" + address + " does not exist!");
			parentF.mkdirs();
			LogUtils.logger.info(address + " Created!");
		} else {
			String[] subFiles = parentF.list();
			for (int i = 0; i < subFiles.length; i++) {

				File tmpfile = new File(address + File.separator + subFiles[i]);
				long size = tmpfile.length();
				if (size != -1) {
					if (tmpfile.isFile()) {
						if (Utils.filter.isMatched(subFiles[i])) {
							FileObj file = new FileObj();
							file.setFilename(subFiles[i]);
							file.setFilesize(size);
							file.setFilePath(address);
							file.setRelativePath(relativePath);
							file.setBackUpAddress(Utils.ErrDataDir);
							flist.add(file);
						}
					} else {
						String tmppath = null;
						if (relativePath.length() == 0) {
							tmppath = subFiles[i];
						} else {
							tmppath = relativePath + File.separator + subFiles[i];
						}
						DirectoryScan(scanAddress, tmppath, flist);
					}
				}
			}
		}
	}

	public void run() {

		while (true) {

			List<FileObj> tmplist = new LinkedList<FileObj>();

			this.DirectoryScan(Utils.ErrDataDir, "", tmplist);
			for (int i = 0; i < tmplist.size(); i++) {
				FileObj fileobj = tmplist.get(i);
				String filename = fileobj.getFilename();
				File srcFile = new File(fileobj.getFilePath() + File.separator + fileobj.getFilename());
				if (this.isFileReady(srcFile)) {
					if (!CacheScanFileList.IfContainedInCacheScanFileList(filename)) {
						
						/**1. create a task. */
						UploadTask task = new UploadTask();
						task.setTaskId(Utils.globalTaskIdNum++);
						task.setDestinationAddress("/DATA/");
						task.setSourceAddress(fileobj.getFilePath());
						task.setFileobj(fileobj);
						task.setTaskStatus(TASKSTATUS.WAIT);
						task.setTaskType(TASKTYPE.FailedTask);

						/** 2. put the file name into the memory cache. */
						CacheScanFileList.AddToCacheScanFileList(filename);

						/** 3. post the task to the task queue. */
						Utils.TaskQueue.AddTask(task);
					}
				}
			}

			try {
				Thread.sleep(Utils.ErrDirScanInterval * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
