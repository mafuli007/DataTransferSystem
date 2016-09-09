package BrazilCenter.UploadClient.tcp;

import java.util.Timer;
import BrazilCenter.Tcp.Client.TcpClient;
import BrazilCenter.UploadClient.Utils.Utils;

/**
 * 
 * @author phoenix
 */
public class MonitorTcpClient extends TcpClient implements Runnable {

	public MonitorTcpClient(String server_ip, int server_port) {
		super(server_ip, server_port, new Timer());
	}

	public boolean SendHeartbeatMessage(String msg) {
		return this.send(msg);
	}

	public void SendRealTaskInfoMsg(String msg) {
		Utils.RealTaskInfoQueue.addRealTaskinfo(msg);
	}

	private boolean SendMsg(String msg) {
		return this.send(msg);

	}

	public void run() {
		
		/** connect to the serve. */
		this.RunClient();
		
		// TODO Auto-generated method stub
		while (true) {

			if (Utils.RealTaskInfoQueue.getRealTaskinfoSize() != 0) {
				String msg = Utils.RealTaskInfoQueue.getRealTaskinfo();
				if(!this.SendMsg(msg)){ // failed to send the message, then add to queue again.
					try {
						Thread.sleep(5 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Utils.RealTaskInfoQueue.addRealTaskinfo(msg);
				};
			} else {
				try {
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}