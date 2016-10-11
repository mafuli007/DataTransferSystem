package BrazilCenter.UploadClient.heartbeat;

import BrazilCenter.HeartBeat.Utils.HeartBeatUtils;
import BrazilCenter.UploadClient.Utils.Configuration;
import BrazilCenter.UploadClient.tcp.MonitorTcpClient;
import BrazilCenter.models.HeartBeatObj;

public class HeartBeat extends Thread {

	private Configuration conf;
	private HeartBeatObj hbobj;
	private MonitorTcpClient monitor_client;

	public HeartBeat(Configuration conf, MonitorTcpClient client) {
 		this.conf = conf;
 		hbobj = new HeartBeatObj();
		hbobj.setSoftwareid(this.conf.getSoftwareId());
		this.monitor_client = client;
	}

	@Override
	public void run() {
		
		while (true) {
			/** update sending time and hardware status. */

			hbobj.update();
			
 			String msg = HeartBeatUtils.MakeXMLHeartbeat(hbobj);
			monitor_client.SendHeartbeatMessage(msg);
			
			/** sleep for interval seconds. */
			try {
				long interval = this.conf.getHeartbeatInterval() * 1000;
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
