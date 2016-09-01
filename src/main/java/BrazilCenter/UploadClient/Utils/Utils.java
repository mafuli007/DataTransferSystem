package BrazilCenter.UploadClient.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import BrazilCenter.UploadClient.core.UploadTaskQueue;
import BrazilCenter.UploadClient.filter.FileNameFilter;
import BrazilCenter.UploadClient.filter.RuleObj;

/**
 * common tools
 */
public class Utils {

	/************************************************************************************/

	public final static String dateFormat24 = "yyyy-MM-dd HH:mm:ss";
	public final static String dateFormat24Mis = "yyyy-MM-dd HH:mm:ss.SSS";
	public final static String dateFormat12 = "yyyy-MM-dd hh:mm:ss";
	public final static String dateDayFormat = "yyyyMMdd";
	public final static String REPORT_Local_DIR = "./report/";  
	public final static String ErrDataDir = "./FailedRecords/"; 
	public final static int ErrDirScanInterval = (24 * 60 * 60);  
	public static UploadTaskQueue TaskQueue = new UploadTaskQueue(); 
	public static RealTaskInfoQueue RealTaskInfoQueue = new RealTaskInfoQueue();
	public static List<RuleObj> rulesList = new LinkedList<RuleObj>();  
	public final static int MaxTryCount = 3; 
	public final static int FtpStatusCheckInterval = 5;  
	public final static int QUEUETASKINFOSIZE = 500;	// the size of queue for storing realtaskinfo in ShareData class 
	public static FileNameFilter filter = new FileNameFilter();
	//public static List<FileObj> OneDayList = new LinkedList<FileObj>(); 
	public static long globalTaskIdNum = 1;
	/************************************************************************************/
	/**
	 * Test if file is occupied by other program
	 */
	public static boolean isFileUnlocked(File file) {
		try {
			FileInputStream in = new FileInputStream(file);
			if (in != null)
				in.close();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
 	 */
	public static String formatTime(Long ms) {
		Integer ss = 1000;
		Integer mi = ss * 60;
		Integer hh = mi * 60;
		Integer dd = hh * 24;

		Long day = ms / dd;
		Long hour = (ms - day * dd) / hh;
		Long minute = (ms - day * dd - hour * hh) / mi;
		Long second = (ms - day * dd - hour * hh - minute * mi) / ss;

		StringBuffer sb = new StringBuffer();
		if (day >= 0) {
			sb.append(day + "d ");
		}
		if (hour >= 0) {
			sb.append(hour + "h ");
		}
		if (minute >= 0) {
			sb.append(minute + "m ");
		}
		if (second >= 0) {
			sb.append(second + "s");
		}
		return sb.toString();
	}

	/**
	 * delete file, exclude directory.
	 * 
	 * @param file
	 * @throws Exception
	 */
	public static boolean delFile(File file) {
		if (!file.exists()) {
			LogUtils.logger.error("File " + file.getName() + " doesn't exist!");
			return false;
		}
		if (file.isFile()) {
			if (Utils.isFileUnlocked(file)) {
				try {
					if (false == file.getAbsoluteFile().delete()) {
						LogUtils.logger.error(Thread.currentThread().getName() + " Failed to delete " + file.getPath());
						return false;
					}
				} catch (Exception e) {
					LogUtils.logger.error(
							Thread.currentThread().getName() + " Failed to delete " + file.getPath() + " " + e.getMessage());
					return false;
				}
			} else {
				LogUtils.logger.error(" File " + file.getName() + " is occupied! ");
				return false;
			}
		} else {
			LogUtils.logger.error("" + file.getName() + " is not a File!");
			return false;
		}
		return true;
	}

	/**
	 * copy file
	 * 
	 * @param oldPath: fullpath, include filename
	 * @param newPath: fullpath, include filename
	 */
	public static boolean CopyFile(String oldPath, String newPath) {
		File targetFile = new File(newPath);
		if (!targetFile.getParentFile().exists()) {
			targetFile.getParentFile().mkdirs();
		}
		int byteread = 0;
		File oldFile = new File(oldPath);
		FileInputStream fin = null;
		FileOutputStream fout = null;
		try {
			if (oldFile.exists()) {
				fin = new FileInputStream(oldFile);
				fout = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				while ((byteread = fin.read(buffer)) != -1) {
					fout.write(buffer, 0, byteread);
				}
				if (fin != null) {
					fin.close(); 
				}
			} else {
				LogUtils.logger.error("File " + oldPath + "does not exist!");
				return false;
			}
		} catch (Exception e) {
			LogUtils.logger.error(e.getMessage());
			return false;
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (fout != null) {
				try {
					fout.flush();
					fout.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	/**
	 * move file
	 * @param oldPath: fullpath, include filename
	 * @param newPath: fullpath, include filename
	 */
	public static boolean MoveFile(String oldPath, String newPath) {
		File targetFile = new File(newPath);
		if (!targetFile.getParentFile().exists()) {
			targetFile.getParentFile().mkdirs();
		}
		int byteread = 0;
		File oldFile = new File(oldPath);
		FileInputStream fin = null;
		FileOutputStream fout = null;
		try {
			if (oldFile.exists()) {
				fin = new FileInputStream(oldFile);
				fout = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				while ((byteread = fin.read(buffer)) != -1) {
					// LogUtils.logger.debug("byteread==" + byteread);
					fout.write(buffer, 0, byteread);
				}
				if (fin != null) {
					fin.close(); 
					return Utils.delFile(oldFile);
				}
			} else {
				LogUtils.logger.error("File " + oldPath + "does not exist!");
				return false;
			}
		} catch (Exception e) {
			LogUtils.logger.error(e.getMessage());
			return false;
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}
			if (fout != null) {
				try {
					fout.flush();
					fout.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;

				}
			}
		}
		return true;
	}

	public static boolean CreateFile(String fileName, String data) {
		File tmpfile = new File(fileName);
		try {
			if (!tmpfile.exists()) {
				if (!tmpfile.getParentFile().exists()) {
					tmpfile.getParentFile().mkdirs();
				}
				tmpfile.createNewFile();
			}
			FileOutputStream fop = null;
			try {
				fop = new FileOutputStream(tmpfile);
				byte[] contentInBytes = data.getBytes();
				fop.write(contentInBytes);
				fop.flush();
				fop.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fop != null) {
						fop.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
}