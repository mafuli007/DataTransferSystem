package BrazilCenter.UploadClient.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import BrazilCenter.UploadClient.Reuploader.ReUploadMsg;
import BrazilCenter.UploadClient.filter.RuleObj;
import BrazilCenter.UploadClient.heartbeat.HardwareObj;
import BrazilCenter.UploadClient.heartbeat.HeartbeatObj;
import BrazilCenter.UploadClient.scanner.FileObj;

/**
 * xml tools
 * @author phoenix
 */
public class XMLOperator {

	private String filePath = "UpLoadClientConfig.xml";
	private String ruleFileName = "rules.xml";

	private Configuration conf;

	public Configuration getConf() {
		return conf;
	}

	public XMLOperator() {
		this.conf = new Configuration();
		this.conf.setBackupSfId("null");
	}

	public void print() {
		System.out.println(this.conf.toString());
	}

	public static void main(String[] args) {
		XMLOperator op = new XMLOperator();
		op.Initial();
		op.ReadRules();
		System.out.println(Utils.rulesList.size());
	}

	/**
 	 */
	public boolean ReadRules() {

 		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(ruleFileName);

			NodeList items = document.getChildNodes();
			for (int i = 0; i < items.getLength(); i++) {
				Node value = items.item(i);
				NodeList values = value.getChildNodes();
				for (int j = 0; j < values.getLength(); j++) {
					Node tmp = values.item(j);
					String strvalue = tmp.getTextContent();
					if (tmp.getNodeName().compareTo("rule") == 0) {
						RuleObj obj = new RuleObj();
						obj.setRule(strvalue);
						Utils.rulesList.add(obj);
					} else {

					}
				}
			}
		} catch (FileNotFoundException e) {
			LogUtils.logger.error(e.getMessage());
			return false;
		} catch (ParserConfigurationException e) {
			LogUtils.logger.error(e.getMessage());
			return false;
		} catch (SAXException e) {
			LogUtils.logger.error(e.getMessage());
			return false;
		} catch (IOException e) {
			LogUtils.logger.error(e.getMessage());
			return false;
		}
		return true;
	}

 	public boolean Initial() {

 		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(filePath);

			NodeList items = document.getChildNodes();
			for (int i = 0; i < items.getLength(); i++) {
				Node value = items.item(i);
				NodeList values = value.getChildNodes();
				for (int j = 0; j < values.getLength(); j++) {
					Node tmp = values.item(j);
					String strvalue = tmp.getTextContent();
					if (tmp.getNodeName().compareTo("SoftwareId") == 0) {
						this.conf.setSoftwareId(strvalue);
					} else if (tmp.getNodeName().compareTo("TargetSoftwareId") == 0) {
						this.conf.setTargetSoftwareId(strvalue);
					}else if (tmp.getNodeName().compareTo("UpLoadThread") == 0) {
						this.conf.setUpLoadThreadNum(Integer.parseInt(strvalue));
					} else if (tmp.getNodeName().compareTo("BackUpSoftwareId") == 0) {
						this.conf.setBackupSfId(strvalue);
					} else if (tmp.getNodeName().compareTo("ScanInterval") == 0) {
						this.conf.setScanInterval(Integer.parseInt(strvalue));
					} else if (tmp.getNodeName().compareTo("ReuploaderPort") == 0) {
						this.conf.setReuploaderPort(Integer.parseInt(strvalue));
					} else if (tmp.getNodeName().compareTo("InvalidFileDir") == 0) {
						this.conf.setInvaildFileDir(strvalue);
					} else if (tmp.getNodeName().compareTo("HeartbeatInterval") == 0) {
						this.conf.setHeartbeatInterval(Integer.parseInt(strvalue));
					} else if (tmp.getNodeName().compareTo("Addresses") == 0) {
						NodeList addList = document.getElementsByTagName("Address");
						for (int index = 0; index < addList.getLength(); index++) {
							Node add = addList.item(index);
							String value1 = null;
							String value2 = null;
							for (Node node = add.getFirstChild(); node != null; node = node.getNextSibling()) {
								if (node.getNodeType() == Node.ELEMENT_NODE) {
									if (node.getNodeName().compareTo("ScanAddresss") == 0) {
										value1 = node.getFirstChild().getNodeValue();
									}
									if (node.getNodeName().compareTo("DataBackUpAddress") == 0) {
										value2 = node.getFirstChild().getNodeValue();
									}
									this.conf.putAddress(value1, value2);
								}
							}
						}
					} else if (tmp.getNodeName().compareTo("ExchangeCenterTcpServerInfo") == 0) {
						NodeList innernodelist = tmp.getChildNodes();
						for (int m = 0; m < innernodelist.getLength(); m++) {
							Node innernode = innernodelist.item(m);
							String inerstrvalue = innernode.getTextContent();
							if (innernode.getNodeName().compareTo("MessageIP") == 0) {
								this.conf.setMessageIp(inerstrvalue);
							} else if (innernode.getNodeName().compareTo("MessagePort") == 0) {
								this.conf.setMessagePort(Integer.parseInt(inerstrvalue));
							} else {
							}
						}
					} else if (tmp.getNodeName().compareTo("MonitorServerInfo") == 0) {
						NodeList innernodelist = tmp.getChildNodes();
						for (int m = 0; m < innernodelist.getLength(); m++) {
							Node innernode = innernodelist.item(m);
							String inerstrvalue = innernode.getTextContent();
							if (innernode.getNodeName().compareTo("MonitorServerIP") == 0) {
								this.conf.setMonitorServerIp(inerstrvalue);
							} else if (innernode.getNodeName().compareTo("MonitorServerPort") == 0) {
								this.conf.setMonitorServerPort(Integer.parseInt(inerstrvalue));
							} else {
							}
						}
					} else if (tmp.getNodeName().compareTo("FtpServerInfo") == 0) {
						NodeList innernodelist = tmp.getChildNodes();
						for (int m = 0; m < innernodelist.getLength(); m++) {
							Node innernode = innernodelist.item(m);
							String inerstrvalue = innernode.getTextContent();
							if (innernode.getNodeName().compareTo("FtpServerIP") == 0) {
								this.conf.setFtpIp(inerstrvalue);
							} else if (innernode.getNodeName().compareTo("FtpServerPort") == 0) {
								this.conf.setFtpPort(Integer.parseInt(inerstrvalue));
							} else if (innernode.getNodeName().compareTo("FtpServerUserName") == 0) {
								this.conf.setFtpusername(inerstrvalue);
							} else if (innernode.getNodeName().compareTo("FtpServerUserPasswd") == 0) {
								this.conf.setFtppasswd(inerstrvalue);
							} else {
							}
						}
					} else {

					}
				}
			}
		} catch (FileNotFoundException e) {
			LogUtils.logger.error(e.getMessage());
			return false;
		} catch (ParserConfigurationException e) {
			LogUtils.logger.error(e.getMessage());
			return false;
		} catch (SAXException e) {
			LogUtils.logger.error(e.getMessage());
			return false;
		} catch (IOException e) {
			LogUtils.logger.error(e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
 	 */
	public static String MakeXMLHeartbeat(HeartbeatObj heartbeatobj, HardwareObj hardwareobj) {
		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}

		String heartbeatstr = null;
		Element root = document.createElement("info");
		document.appendChild(root);

 		Element msgType = document.createElement("MessageType");
		msgType.appendChild(document.createTextNode("HeartBeat"));
		root.appendChild(msgType);

 		Element softwareid = document.createElement("SoftwareId");
		softwareid.appendChild(document.createTextNode(heartbeatobj.getSoftwareId()));
		root.appendChild(softwareid);

 		Element localip = document.createElement("LocalIp");
		localip.appendChild(document.createTextNode(hardwareobj.getLocalIp()));
		root.appendChild(localip);

 		/*
		 * Element status = document.createElement("Status");
		 * status.appendChild(document.createTextNode(heartbeatobj.getStatus()))
		 * ; root.appendChild(status);
		 */

 		Element currenttime = document.createElement("CurrentTime");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String datestr = format.format(new Date());
		currenttime.appendChild(document.createTextNode(datestr));
		root.appendChild(currenttime);

		/** Duration */
		Element duration = document.createElement("Duration");
		duration.appendChild(document.createTextNode(heartbeatobj.getDuration()));
		root.appendChild(duration);

 		Element hostname = document.createElement("HostName");
		hostname.appendChild(document.createTextNode(hardwareobj.getHostname()));
		root.appendChild(hostname);

		/** CPU info */
		Element cpuPercent = document.createElement("CpuPercent");
		cpuPercent.appendChild(document.createTextNode(String.valueOf(hardwareobj.getCpuPercent())));
		root.appendChild(cpuPercent);

		/** Memory info */
		Element memoryPercent = document.createElement("MemoryPercent");
		memoryPercent.appendChild(document.createTextNode(String.valueOf(hardwareobj.getMemoryPercent())));
		root.appendChild(memoryPercent);

		/** disk info */
		Element diskPercent = document.createElement("DiskPercent");
		diskPercent.appendChild(document.createTextNode(String.valueOf(hardwareobj.getDiskPercent())));
		root.appendChild(diskPercent);

 		TransformerFactory tf = TransformerFactory.newInstance();
		ByteArrayOutputStream bos = null;
		try {
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(document), new StreamResult(bos));
			heartbeatstr = bos.toString();
		} catch (Exception e) {
			LogUtils.logger.error(e.getMessage());
		}

		return heartbeatstr;
	}

	/**
 	 */
	public static String MakeXMLUploadReport(UploadReport report) {

		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}

		String uploadreportstr = null;
		FileObj srcFileObj = report.getSrcFileObj();
		Element root = document.createElement("info");
		document.appendChild(root);

 		Element messagetype = document.createElement("MessageType");
		messagetype.appendChild(document.createTextNode("UploadReport"));
		root.appendChild(messagetype);

 		Element softwareid = document.createElement("SoftwareId");
		softwareid.appendChild(document.createTextNode(report.getSoftwareId()));
		root.appendChild(softwareid);

 		Element sendtime = document.createElement("SendTime");
		sendtime.appendChild(document.createTextNode(report.getEndSendTime()));
		root.appendChild(sendtime);

 		Element sourceaddress = document.createElement("SourceAddress");
		sourceaddress.appendChild(document.createTextNode(report.getSourceAddress()));
		root.appendChild(sourceaddress);

 		Element destinationaddress = document.createElement("DestinationAddress");
		destinationaddress.appendChild(document.createTextNode(report.getDestinationAddress()));
		root.appendChild(destinationaddress);

 		Element filename = document.createElement("FileName");
		filename.appendChild(document.createTextNode(srcFileObj.getFilename()));
		root.appendChild(filename);

 		Element md5value = document.createElement("Md5Value");
		md5value.appendChild(document.createTextNode(report.getMd5value()));
		root.appendChild(md5value);

 		Element result = document.createElement("Result");
		String resultstr = null;
		if (report.getResult()) {
			resultstr = "success";
		} else {
			resultstr = "fail";
		}
		result.appendChild(document.createTextNode(resultstr));
		root.appendChild(result);

 		Element failReason = document.createElement("FailReason");
		failReason.appendChild(document.createTextNode(report.getFailReason()));
		root.appendChild(failReason);

 		TransformerFactory tf = TransformerFactory.newInstance();
		ByteArrayOutputStream bos = null;
		try {
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(document), new StreamResult(bos));
			uploadreportstr = bos.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return uploadreportstr;
	}

	/**
 	 */
	public static String MakeXMLUploadTaskInfo(UploadReport report, Configuration conf) {

		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}

		FileObj srcFileObj = report.getSrcFileObj();
		String uploadreportstr = null;
		Element root = document.createElement("info");
		document.appendChild(root);

 		Element messagetype = document.createElement("MessageType");
		messagetype.appendChild(document.createTextNode("TaskInfo"));
		root.appendChild(messagetype);

 		Element softwareid = document.createElement("SoftwareId");
		String clientId = report.getSoftwareId();
		softwareid.appendChild(document.createTextNode(clientId));
		root.appendChild(softwareid);

 		Element startSendtime = document.createElement("StartTime");
		startSendtime.appendChild(document.createTextNode(report.getStartSendTime()));
		root.appendChild(startSendtime);

		Element sendtime = document.createElement("EndTime");
		sendtime.appendChild(document.createTextNode(report.getEndSendTime()));
		root.appendChild(sendtime);
		
 		Element targetCenterName = document.createElement("TargetSoftwareId");
		targetCenterName.appendChild(document.createTextNode(conf.getTargetSoftwareId()));
		root.appendChild(targetCenterName);

 		Element filename = document.createElement("FileName");
		filename.appendChild(document.createTextNode(srcFileObj.getFilename()));
		root.appendChild(filename);

 		Element filesize = document.createElement("FileSize");
		filesize.appendChild(document.createTextNode(String.valueOf(srcFileObj.getFilesize())));
		root.appendChild(filesize);

 		Element sourceaddress = document.createElement("SourceAddress");
		sourceaddress.appendChild(document.createTextNode(report.getSourceAddress()));
		root.appendChild(sourceaddress);

 		Element result = document.createElement("Result");
		String resultstr = null;
		if (report.getResult()) {
			resultstr = "success";
		} else {
			resultstr = "fail";
		}
		result.appendChild(document.createTextNode(resultstr));
		root.appendChild(result);

 		Element failReason = document.createElement("FailReason");
		failReason.appendChild(document.createTextNode(report.getFailReason()));
		root.appendChild(failReason);
		
 		TransformerFactory tf = TransformerFactory.newInstance();
		ByteArrayOutputStream bos = null;
		try {
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(document), new StreamResult(bos));
			uploadreportstr = bos.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return uploadreportstr;
	}
	
	/**
	 * parse msg into Reupload object.
	 */
	public static ReUploadMsg ParseReUploadMsgXML(String msg) {
		ReUploadMsg msgObj = new ReUploadMsg();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(msg)));

			NodeList items = document.getChildNodes();
			for (int i = 0; i < items.getLength(); i++) {
				Node value = items.item(i);
				NodeList values = value.getChildNodes();
				for (int j = 0; j < values.getLength(); j++) {
					Node tmp = values.item(j);
					String strvalue = tmp.getTextContent();
					if (tmp.getNodeName().compareTo("MessageType") == 0) {

					} else if (tmp.getNodeName().compareTo("CenterName") == 0) {
						msgObj.setCenterName(strvalue);
					} else if (tmp.getNodeName().compareTo("FileName") == 0) {
						msgObj.setFileName(strvalue);
					} else {

					}
				}
			}
		} catch (Exception e) {
			LogUtils.logger.error("Parse reupload message error.");
			return null;
		}

		return msgObj;
	}
}
