package BrazilCenter.UploadClient.scanner;

/**
  *  */
public class FileObj {

	private String filename;  
	private long filesize; 		 
	private String filePath; 	 
	private String relativePath; 
	private String backUpAddress;	
	
	public FileObj(){
		this.filesize = 0;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getFilesize() {
		return filesize;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getBackUpAddress() {
		return backUpAddress;
	}

	public void setBackUpAddress(String backUpAddress) {
		this.backUpAddress = backUpAddress;
	}
}
