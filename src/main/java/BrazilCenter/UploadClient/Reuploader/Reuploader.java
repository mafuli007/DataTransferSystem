package BrazilCenter.UploadClient.Reuploader;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import BrazilCenter.UploadClient.Utils.Configuration;
import BrazilCenter.UploadClient.Utils.LogUtils;
import BrazilCenter.UploadClient.Utils.Utils;
import BrazilCenter.UploadClient.Utils.XMLOperator;
import BrazilCenter.UploadClient.scanner.FileObj;
import BrazilCenter.UploadClient.task.TASKSTATUS;
import BrazilCenter.UploadClient.task.TASKTYPE;
import BrazilCenter.UploadClient.task.UploadTask;

/**
 * 
 * @author maful
 *
 */
public class Reuploader {

	private ServerSocket s;
	private boolean connected = false;
	private Configuration conf;

	public Reuploader(Configuration confr) {
		this.conf = confr;
		try {
			this.s = new ServerSocket(this.conf.getReuploaderPort());
			setConnected(true);
			LogUtils.logger.info("Reuploader Server Started!");
		} catch (IOException e) {
			LogUtils.logger.error("TCP Server Started Failed! :" + e.getMessage());
		}
	}

	public void StartServer() {
		while (true) {
			try {
				Socket cs = this.s.accept();
				LogUtils.logger.info(cs.getInetAddress() + " Online!");
				new ServerThread(cs, this.conf).start();
			} catch (IOException e) {
				LogUtils.logger.error(e.getMessage());
			}
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}

class ServerThread extends Thread {
	private Socket sock;
	private Map<String, String> addresses;

	public ServerThread(Socket s, Configuration conf) {
		sock = s;
		this.addresses = conf.getAddress();
	}

	/**
	 */
	private boolean isFileReady(File file) {
		if (file.renameTo(file)) {
			return true;
		} else {
			return false;
		}
	}

	public void DirectoryScan(String scanAddress, String relativePath, List<FileObj> flist) {

		String address = scanAddress + File.separator + relativePath;
		File parentF = new File(address);
		if (!parentF.exists()) {
			LogUtils.logger.warn("Directory: " + address + " does not exist!");
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

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public FileObj findFile(String fileName) {
		List<FileObj> tmplist = new LinkedList<FileObj>();
		for (String key : this.addresses.keySet()) {
			String scanAddress = this.addresses.get(key);
			this.DirectoryScan(scanAddress, "", tmplist);
			for (FileObj fileobj : tmplist) {
				if (fileobj.getFilename().contains(fileName)) {
					return fileobj;
				}
			}
			tmplist.clear();
		}
		return null;
	}

	public void run() {
		try {
			while (true) {
				InputStream in = sock.getInputStream();
				DataInputStream din = new DataInputStream(in);
				String msg = din.readUTF();
				if (msg.length() > 0) {
					ReUploadMsg msgObj = XMLOperator.ParseReUploadMsgXML(msg);
					LogUtils.logger.info("Reupload file: " + msgObj.getFileName());
					String fileName = msgObj.getFileName();
					FileObj fileObj = this.findFile(fileName);
					if (fileObj != null) {
						File tmpfile = new File(fileObj.getFilePath() + File.separator + fileObj.getFilename());
						if (this.isFileReady(tmpfile)) {

							UploadTask task = new UploadTask();
							task.setDestinationAddress("/DATA/");
							task.setSourceAddress(fileObj.getFilePath());
							task.setFileobj(fileObj);
							task.setTaskStatus(TASKSTATUS.WAIT);
							task.setTaskType(TASKTYPE.ReTransTask);

							Utils.TaskQueue.AddTask(task);
						}
					} else {
						LogUtils.logger.error("Reupload file is not exist: " + fileName);
					}
				}
			}
		} catch (IOException e) {
			try {
				this.sock.close();
				LogUtils.logger.info(sock.getInetAddress() + " closed, " + e.getMessage());
			} catch (IOException e1) {
				LogUtils.logger.error("closed unexcepted, " + e.getMessage());
			}
		}
	}
}
