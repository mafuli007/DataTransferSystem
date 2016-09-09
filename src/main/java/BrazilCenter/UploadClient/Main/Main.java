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
import BrazilCenter.UploadClient.tcp.MonitorTcpClient;

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

		/** start scan directory for running tasks */
		LogUtils.logger.info(Main.conf.getSoftwareId() + " Start Scanning......");
		Scanner scanner = new Scanner(conf);
		scanner.start();
		
		/** start errData Scanner */
		ErrDataScanner errScanner = new ErrDataScanner();
		errScanner.start();
		
		/** start a thread to send real time information, and heart beat thread  */
		MonitorTcpClient monitor_client = new MonitorTcpClient(conf.getMonitorServerIp(), conf.getMonitorServerPort());
		Thread monitor_thread = new Thread(monitor_client);
		
		/** start the upload thread. */
		Trans transfer = new Trans(conf, monitor_client);
		Thread thread = new Thread(transfer);
		thread.start();
		
		/** start the heartbeat thread*/
		HeartBeat heatbeat = new HeartBeat(conf, monitor_client);
		heatbeat.start();
		LogUtils.logger.info("Started!!!!!");
		
		monitor_thread.start();
		
		/** start the reupload service. ##### this should always be the last step!!!#####*/
		Reuploader reuploader = new Reuploader(conf);
		reuploader.StartServer();
		System.out.println("ddd");

	}
}