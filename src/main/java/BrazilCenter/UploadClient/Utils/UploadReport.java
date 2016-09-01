package BrazilCenter.UploadClient.Utils;

import BrazilCenter.UploadClient.scanner.FileObj;

/**
*
 * @author phoenix
 * @Time 20150128
 *
 */
public class UploadReport {

	private String messageType;  
	private String softwareId;  // source softwareId
	private String startSendTime;  // the time start to upload file
	private String endSendTime;  // the time that uploading ended. 
	private String sourceAddress;  // source address. 
	private String destinationAddress; // target address. 
 	private String md5value; // source file's md5 value. 
	private boolean result; // upload result. 
 	private String failReason;	
	private FileObj srcFileObj;	// source file information. 
	private String reportName; // report file name.

	public UploadReport(){
		this.failReason = "";
		this.md5value = "0";
		this.startSendTime = "000-00-00 00:00:00";
		this.endSendTime = "000-00-00 00:00:00";
	}
	
	public String getFailReason() {
		return failReason;
	}

	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}

	public String getMessageType() {
		return messageType;
	}

	public String getSoftwareId() {
		return this.softwareId;
	}

	public void setSoftwareId(String softwareId) {
		this.softwareId = softwareId;
	}

	public String getStartSendTime() {
		return startSendTime;
	}

	public void setStartSendTime(String startSendTime) {
		this.startSendTime = startSendTime;
	}

	public String getEndSendTime() {
		return endSendTime;
	}

	public void setEndSendTime(String endSendTime) {
		this.endSendTime = endSendTime;
	}

	public String getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public String getMd5value() {
		return md5value;
	}

	public void setMd5value(String md5value) {
		this.md5value = md5value;
	}

	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public FileObj getSrcFileObj() {
		return srcFileObj;
	}

	public void setSrcFileObj(FileObj srcFileObj) {
		this.srcFileObj = srcFileObj;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

}
