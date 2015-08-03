import java.util.ArrayList;

public class CSVEntry {
	private String filename;
	private String time;
	private ArrayList<String> accountNumbers;
	public CSVEntry(String time, String filename, ArrayList<String> accountNumbers){
		this.filename = filename;
		this.accountNumbers = accountNumbers;
		this.time = time;
	}
	public CSVEntry(String time, String filename, String accountNumber){
		this.filename = filename;
		this.accountNumbers.set(0, accountNumber);
		this.time = time;
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
	
}
