package BrazilCenter.UploadClient.scanner;

import java.util.*;

import BrazilCenter.UploadClient.Utils.CacheScanFileList;
import BrazilCenter.UploadClient.Utils.Configuration;
import BrazilCenter.UploadClient.Utils.LogUtils;
import BrazilCenter.UploadClient.Utils.Utils;
import BrazilCenter.UploadClient.task.TASKSTATUS;
import BrazilCenter.UploadClient.task.TASKTYPE;
import BrazilCenter.UploadClient.task.UploadTask;

import java.io.*;

/**
 * DirectoryScan,RunTask
 */
public class Scanner extends Thread {

	private Configuration conf; //
	private Map<String, String> scanAddressMap;
	private List<FileObj> fileCacheList; // used to check if the file is ready.

	public Scanner(Configuration config) {
		this.conf = config;
		this.scanAddressMap = this.conf.getAddress();
		fileCacheList = new LinkedList<FileObj>();
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

	/** get all the files. */
	public void DirectoryScan(String scanAddress, String relativePath, List<FileObj> flist) {

		String address = scanAddress + File.separator + relativePath;
		File parentF = new File(address);
		if (!parentF.exists()) {
			LogUtils.logger.warn("Source Directory: " + address + " does not exist!");
			parentF.mkdirs();
			LogUtils.logger.warn("Source Directory: " + address + " Created!");
		} else {
			String[] subFiles = parentF.list();
			for (int i = 0; i < subFiles.length; i++) {

				File tmpfile = new File(address + File.separator + subFiles[i]);
				if (tmpfile.isFile()) {
					if (Utils.filter.isMatched(subFiles[i])) {
						/** create the file object. */
						FileObj file = new FileObj();
						file.setFilename(subFiles[i]);
						file.setFilesize(tmpfile.length());
						file.setFilePath(address);
						file.setRelativePath(relativePath);
						file.setBackUpAddress(scanAddressMap.get(scanAddress));
						LogUtils.logger.info("Find : " + file.getFilename());
						flist.add(file);
					} else {// invalid files
						String oldPath = address + File.separator + subFiles[i];
						File invalidFile = new File(this.conf.getInvaildFileDir() + File.separator + subFiles[i]);
						Utils.CopyFile(oldPath, invalidFile.getPath());
						tmpfile.delete();
					}
				} else { // file is a directory.
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

	public void run() {

		while (true) {
			for (String scanAddress : scanAddressMap.keySet()) {

				List<FileObj> tmplist = new LinkedList<FileObj>();

				this.DirectoryScan(scanAddress, "", tmplist);
				for (int i = 0; i < tmplist.size(); i++) {
					FileObj fileobj = tmplist.get(i);
					String filename = fileobj.getFilename();

					File srcFile = new File(fileobj.getFilePath() + File.separator + filename);
					if (this.isFileReady(srcFile)) {
						if (!CacheScanFileList.IfContainedInCacheScanFileList(filename)) {
							
							/** 1. create a new task. */
							UploadTask task = new UploadTask();
							
							task.setTaskId(Utils.globalTaskIdNum++);
							task.setDestinationAddress("/DATA/");
							task.setSourceAddress(fileobj.getFilePath());
							task.setFileobj(fileobj);
							task.setTaskStatus(TASKSTATUS.WAIT);
							task.setTaskType(TASKTYPE.NewTask);

							/** 2. put the file name into the cache file list. */
							CacheScanFileList.AddToCacheScanFileList(filename);
							
							/** 3. put the task into the task queue. */
							Utils.TaskQueue.AddTask(task);
							LogUtils.logger.info("Add task :" + srcFile.getName());
						}
					}
				}
			}

			try {
				Thread.sleep(this.conf.getScanInterval() * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
