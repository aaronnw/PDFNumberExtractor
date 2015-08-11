import java.util.ArrayList;

public class CSVEntry {
	private String filename;
	private String time;
	private ArrayList<String> accountNumbers;
	private ArrayList<String> memberNumbers;
	String docType;
	
	public CSVEntry(String time, String filename, ArrayList<String> accountNumbers, ArrayList<String> memberNumbers, String docType){
		this.filename = filename;
		this.accountNumbers = accountNumbers;
		this.time = time;
		this.memberNumbers = memberNumbers;
		this.docType = docType;
	}	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public ArrayList<String> getAccountNumbers() {
		return accountNumbers;
	}
	public void setAccountNumbers(ArrayList<String> accountNumbers) {
		this.accountNumbers = accountNumbers;
	}
	public ArrayList<String> getMemberNumbers() {
		return memberNumbers;
	}
	public void setMemberNumbers(ArrayList<String> memberNumbers) {
		this.memberNumbers = memberNumbers;
	}
	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}
	
}
