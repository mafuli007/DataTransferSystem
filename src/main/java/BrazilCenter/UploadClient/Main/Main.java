package BrazilCenter.UploadClient.Main;

import org.apache.log4j.PropertyConfigurator;

import BrazilCenter.UploadClient.Reuploader.Reuploader;
import BrazilCenter.UploadClient.Utils.Configuration;
import BrazilCenter.UploadClient.Utils.LogUtils;
import BrazilCenter.UploadClient.Utils.XMLOperator;
import BrazilCenter.UploadClient.core.Trans;
import BrazilCenter.UploadClient.heartbeat.HeartBeat;
import BrazilCenter.UploadClient.scanner.ErrDataScanner;
import BrazilCenter.UploadClient.scanner.Scanner;
import BrazilCenter.UploadClient.tcp.TcpClient;

public class Main {

	public static XMLOperator xmloperator = new XMLOperator();
	public static Configuration conf;

	public static void main(String args[]) {

		/** Initialize*/
		if (!xmloperator.Initial()) {
			LogUtils.logger.fatal("Parsing XML configuration failed!");
			return;
		}
		conf = xmloperator.getConf();
		PropertyConfigurator.configure("log4j.properties");
		LogUtils.logger.info("Parsing XML Configuration Successfully!");
		if(!xmloperator.ReadRules()){
			LogUtils.logger.fatal("Parsing File Name Filters Configuration Failed!");
			return;
		}
		LogUtils.logger.info("Parsing File Name Filters Configuration Successfully!");
		
		/** start a thread to send real time information, and heart beat thread  */
		TcpClient monitor_client = new TcpClient(conf.getMonitorServerIp(), conf.getMonitorServerPort());
		monitor_client.start();
		if (monitor_client.isConnected()) {
			LogUtils.logger.info("HeartBeat Thread Started Successfully!");
		} else {
			LogUtils.logger.error("HeartBeat Thread Started Failed!");
		}
		HeartBeat heatbeat = new HeartBeat(conf, monitor_client);
		heatbeat.start();
		
		/** start the upload thread. */
		Trans transfer = new Trans(conf);
		Thread thread = new Thread(transfer);
		thread.start();
		

		/** start scan directory for running tasks */
		LogUtils.logger.info(Main.conf.getSoftwareId() + " Start Scanning......");
		Scanner scanner = new Scanner(conf);
		scanner.start();
		
		/** start errData Scanner */
		ErrDataScanner errScanner = new ErrDataScanner();
		errScanner.start();
		
		/** start the reupload service. */
		Reuploader reuploader = new Reuploader(conf);
		reuploader.StartServer();
	}
}