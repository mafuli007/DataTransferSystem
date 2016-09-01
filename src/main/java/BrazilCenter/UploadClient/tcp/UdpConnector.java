package BrazilCenter.UploadClient.tcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpConnector extends Thread{

	private DatagramSocket socket;
	private String ip;
	private int port;

	public UdpConnector(String ipAddress, int portNum) {
		try {
			this.ip = ipAddress;
			this.port = portNum;
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void SendMessage() {
		byte buf[] = "Message".getBytes();
		try {
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					InetAddress.getByName(this.ip), this.port);
			this.socket.send(packet);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
	}
	

}
