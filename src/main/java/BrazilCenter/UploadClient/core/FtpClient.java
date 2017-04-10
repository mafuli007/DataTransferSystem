package BrazilCenter.UploadClient.core;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import BrazilCenter.UploadClient.Utils.Configuration;
import BrazilCenter.UploadClient.Utils.LogUtils;
import BrazilCenter.UploadClient.Utils.MD5Util;
import BrazilCenter.UploadClient.Utils.UploadReport;
import BrazilCenter.UploadClient.Utils.Utils;
import BrazilCenter.UploadClient.Utils.XMLOperator;
import BrazilCenter.UploadClient.scanner.FileObj;
import BrazilCenter.UploadClient.task.UploadTask;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author phoenix
 */
public class FtpClient {

	private FTPClient _ftpclient = null;
	private Configuration conf;
	private boolean isConnected = false;

	public boolean ReConnect() {
		synchronized (this) {
			if (this.isConnected == true) {
				return true;
			} else {
				while (true) {
					try {
						this._ftpclient.connect(conf.getFtpIp(), conf.getFtpPort());
						this._ftpclient.setControlEncoding("UTF-8");

						int reply = this._ftpclient.getReplyCode();
						if (!FTPReply.isPositiveCompletion(reply)) {
							this._ftpclient.disconnect();
							LogUtils.logger.error(Thread.currentThread().getName() + " FTP connecting failed!");
						} else {
							if (!this._ftpclient.login(conf.getFtpusername(), conf.getFtppasswd())) {
								LogUtils.logger.error("FTP failed to log in!");
							} else {
								LogUtils.logger.info(Thread.currentThread().getName() + " FTP relogin successfully!");
								this._ftpclient.enterLocalPassiveMode();
								this.isConnected = true;
								return true;
							}
						}
					} catch (UnknownHostException e) {
						LogUtils.logger.error(
								Thread.currentThread().getName() + " FTP connecting failed! : UnknowHostExcpetion ");
					} catch (Exception e) {
						LogUtils.logger
								.error(Thread.currentThread().getName() + " FTP connecting failed!" + e.toString());
					}
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e) {
						LogUtils.logger.error("stoped mannually!");
					}
				}
			}
		}
	}

	public boolean ConnectServer() {
		try {
			this._ftpclient.connect(conf.getFtpIp(), conf.getFtpPort());
			this._ftpclient.setControlEncoding("UTF-8");

			int reply = this._ftpclient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				this._ftpclient.disconnect();
				return false;
			} else {
				if (!this._ftpclient.login(conf.getFtpusername(), conf.getFtppasswd())) {
					return false;
				} else {
					this.isConnected = true;
				}
			}
		} catch (UnknownHostException e) {
			LogUtils.logger.error(Thread.currentThread().getName() + " FTP connecting failed! UnknowHostExcpetion ");
			return false;
		} catch (Exception e) {
			LogUtils.logger.error(Thread.currentThread().getName() + " FTP connecting failed!" + e.getMessage());
			return false;
		}
		_ftpclient.enterLocalPassiveMode();
		return true;
	}

	public FtpClient(Configuration config) {
		this.conf = config;
		_ftpclient = new FTPClient();
		_ftpclient.setControlEncoding("UTF-8");
		_ftpclient.setControlKeepAliveTimeout(300);
	}

	/** generate report file's name */
	private String MakeReportFileName(UploadReport rpt){

		FileObj srcFileObj = rpt.getSrcFileObj();
		
		String date_str = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String sourceFileName = srcFileObj.getFilename();
		String fileNameWithoutPath = sourceFileName.substring(0, sourceFileName.lastIndexOf('.'));
		String suffix = sourceFileName.substring(sourceFileName.lastIndexOf('.') + 1);
		String rptFileName = Utils.REPORT_Local_DIR + date_str + "_" + fileNameWithoutPath +'.' + suffix + ".xml";
		
		return rptFileName;
	}
	
	/**
	 * upload the report to the server.
	 */
	public boolean FtpUploadReport(UploadReport report) {

		boolean uploadresult = false;
		if (this.getConnectedStatus() == false) {
			this.ReConnect();
		}
		
		/**1. create a local report. */
		String rptFileName = this.MakeReportFileName(report);
		report.setReportName(rptFileName);
		String data = XMLOperator.MakeXMLUploadReport(report);
		File tmpfile = null;
		if (Utils.CreateFile(rptFileName, data)) {
			tmpfile = new File(rptFileName);
		} else {
			LogUtils.logger.error("Creating " + rptFileName + " failed!");
			return uploadresult;
		}

		/**2. begin to upload the local report file to the server. */
		FileInputStream fis = null;
		String workingDir = this.conf.getDestinationAddress() + "CTL/";
		try {
			fis = new FileInputStream(tmpfile);
			while (!(this._ftpclient.changeWorkingDirectory(workingDir))) {
				if (!_ftpclient.makeDirectory(workingDir)) {
					LogUtils.logger.error("Create directory failed at FTP server!");
					return uploadresult;
				}
			}
			this._ftpclient.setBufferSize(1024);
			this._ftpclient.setControlEncoding("UTF-8");
			this._ftpclient.setFileType(FTPClient.BINARY_FILE_TYPE);
			if (this._ftpclient.storeFile(new String(tmpfile.getName()), fis)) {
				uploadresult = true;
			} else {
				LogUtils.logger.error("Upload " + tmpfile.getName() + " failed!");
				return uploadresult;
			}
		} catch (FileNotFoundException e) {
			LogUtils.logger.error("Can not find report file:" + tmpfile.getName());
			return uploadresult;
		} catch (SocketException e) {
			LogUtils.logger.error("Network error:" + e.getMessage());
			try {
				this._ftpclient.disconnect();
				LogUtils.logger.error("FTP disconnected! Trying to reconnect.....");
				if (this.ReConnect() == true) {
					return this.FtpUploadReport(report);
				}

			} catch (Exception ea) {
				ea.printStackTrace();
			}
		} catch (IOException e) {
			LogUtils.logger.error("Upload failed: " + tmpfile.getName() + " " + e.getMessage());
			if (!this._ftpclient.isConnected()) {
				this.isConnected = false;
			}
			return uploadresult;
		} catch (Exception e) {
			LogUtils.logger.error("Upload failed:" + tmpfile.getName() + " " + e.getMessage());
			return uploadresult;
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
				if (uploadresult == false) { // if failed to upload the report, then delete it. 
					Utils.delFile(new File(rptFileName));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public boolean setWorkingPlace(String workingDir) {
		try {
			while (!(this._ftpclient.changeWorkingDirectory(workingDir))) {
				if (!_ftpclient.makeDirectory(workingDir)) {
					LogUtils.logger.error("Create directory failed at FTP server!");
					return false;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param task
	 * @return null, if failed to upload due the ftp connection.
	 */
	public UploadReport FtpUploadFile(UploadTask task) {
		if (this.isConnected == false) {
			this.ReConnect();
		}

		boolean uploadresult = false;
		UploadReport report = new UploadReport();
		SimpleDateFormat timeFormat = new SimpleDateFormat(Utils.dateFormat24Mis);

		/***
		 * Try fo find if the connection is usable!
		 */
		int reply = this._ftpclient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			try {
				this._ftpclient.disconnect();
				this.isConnected = false;
				if (this.ReConnect() == true) {
					report = this.FtpUploadFile(task);
					return report;
				}
			} catch (Exception ea) {
				ea.printStackTrace();
			}
		}

		FileObj srcFileobj = task.getFileobj();
		String workingDir = task.getDestinationAddress();
		File srcFile = new File(task.getSourceAddress() + File.separator + srcFileobj.getFilename());
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(srcFile);
			if (!(this._ftpclient.changeWorkingDirectory(workingDir))) {
				LogUtils.logger.error("FTP client enter into workplace failed:" + workingDir);
				return null;
			}
			this._ftpclient.setBufferSize(1024);
			this._ftpclient.setControlEncoding("UTF-8");
			this._ftpclient.setFileType(FTPClient.BINARY_FILE_TYPE);

			/** begin to upload the file. */
			LogUtils.logger.info("Start to upload: " + srcFileobj.getFilename());
			report.setStartSendTime(timeFormat.format(new Date()));
			if (this._ftpclient.storeFile(new String(srcFileobj.getFilename()), fis)) {
				LogUtils.logger.info(Thread.currentThread().getName() + " " + ": Upload " + srcFileobj.getFilename()
						+ " successfully!");
				uploadresult = true;
			} else {
				LogUtils.logger
						.error(Thread.currentThread().getName() + " upload " + srcFileobj.getFilename() + " failed!");
				report.setFailReason("FTP sotre file return false as a result!");
			}
			report.setEndSendTime(timeFormat.format(new Date()));
		} catch (FileNotFoundException e) {
			
			LogUtils.logger.error("Can not find file: " + srcFile.getAbsolutePath());
			report.setFailReason("Can not find file: " + srcFile.getAbsolutePath());
			
		} catch (SocketException e) { // may be the ftp is disconnected!
			
			LogUtils.logger.error("Network Error: " + e.getMessage());
			try {
				this._ftpclient.disconnect();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			LogUtils.logger.error(Thread.currentThread().getName() + " FTP disconnected! Try to reconnect.....");
			if (this.ReConnect() == true) {
				report = this.FtpUploadFile(task);
				return report;
			}

		} catch (IOException e) {
			LogUtils.logger.error(Thread.currentThread().getName() + " Uploading failed:" + task.getTryCount() + ":"
					+ srcFile.getName() + " " + e.getMessage());
			report.setFailReason("IO error: " + e.getMessage());

		} catch (Exception e) {
			LogUtils.logger.error(" Uploading failed:" + srcFileobj.getFilename() + ", " + e.getMessage());
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		report.setSoftwareId(this.conf.getSoftwareId());
		report.setResult(uploadresult);
 		report.setDestinationAddress(task.getDestinationAddress());
		report.setSoftwareId(this.conf.getSoftwareId());
		report.setSourceAddress(task.getSourceAddress());
		if (true == uploadresult) {
			String value = MD5Util.getFileMD5(srcFile);
			if (value == null) {
				report.setMd5value("none");
			} else {
				report.setMd5value(value);
			}
		}
 		report.setSrcFileObj(srcFileobj);
		return report;
	}

	public void Close() {
		try {
			_ftpclient.logout();
			if (_ftpclient.isConnected()) {
				_ftpclient.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 */
	public boolean getConnectedStatus() {
		return this.isConnected;
	}

	public void setConnected(boolean status) {
		synchronized (this) {
			this.isConnected = status;
		}
	}

	public void CheckStatus() {
		// TODO Auto-generated method stub
		try {
			if (this._ftpclient.sendNoOp() == false) {
				this.setConnected(false);
				this.ReConnect();
			}
		} catch (IOException e1) {
			this.setConnected(false);
			this.ReConnect();
		}
	}
}
