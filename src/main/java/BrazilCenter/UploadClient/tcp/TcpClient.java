package BrazilCenter.UploadClient.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import BrazilCenter.UploadClient.Utils.LogUtils;
import BrazilCenter.UploadClient.Utils.Utils;

/**
  * 
 * @author phoenix
 */
public class TcpClient extends Thread {
	private Socket s;
	private String serverIp;
	private int serverPort;
	private OutputStream out;
	private DataOutputStream dout;
	private boolean connected = false; 

	public TcpClient(String server_ip, int server_port) {
		try {
			this.serverIp = server_ip;
			this.serverPort = server_port;
			this.s = new Socket(server_ip, server_port);
			setConnected(true);
		} catch (IOException e) {
			LogUtils.logger.error("Tcp connecting failed! IP:" + server_ip + " PORT:" + server_port);
		}
	}

	public void Reconnect(){
		try {
			this.s = new Socket(this.serverIp,this.serverPort);
			setConnected(true);
			LogUtils.logger.info("TCP Reconnected Successfully!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LogUtils.logger.error("Tcp cann't connect to " + this.serverIp + " port:" + this.serverPort + " reason:"+ e.getMessage());
		}
	}
	
	public void SendHeartbeatMessage(String msg) {
		if (false == this.connected) {
			return;
		}
		try {

			this.out = this.s.getOutputStream();
			this.dout = new DataOutputStream(this.out);
			this.dout.writeUTF(msg);

		} catch (IOException e) {
			LogUtils.logger.error("TCP failed to send: " + e.getMessage());
			this.connected = false;
			this.Close();
		}
	}
	
	public void SendRealTaskInfoMsg(String msg){
		Utils.RealTaskInfoQueue.addRealTaskinfo(msg);
	}

	private void SendMsg(String msg){
		if (false == this.connected) {
			return;
		}
		try {

			this.out = this.s.getOutputStream();
			this.dout = new DataOutputStream(this.out);
			this.dout.writeUTF(msg);

		} catch (IOException e) {
			LogUtils.logger.error("TCP failed to send: " + e.getMessage());
			this.connected = false;
			this.Close();
		}
	}
	
	public String RecvMessage() {
		if (false == this.connected) {
			return "";
		}
		String recvmsg = null;
		try {
			InputStream in = this.s.getInputStream();
			DataInputStream din = new DataInputStream(in);
			recvmsg = din.readUTF();
		} catch (IOException e) {
			LogUtils.logger.error("TCP failed to receive: " + e.getMessage());
		}
		return recvmsg;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public void Close() {
		try {
			this.s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		while(true){
			if(this.connected == false){
				this.Reconnect();
			}
			
			while(Utils.RealTaskInfoQueue.getRealTaskinfoSize() != 0){
				String msg = Utils.RealTaskInfoQueue.getRealTaskinfo();
				this.SendMsg(msg);
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