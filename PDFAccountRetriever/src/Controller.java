import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import com.ibm.icu.util.TimeZone;

public class Controller {

	//Declared variables
	private MainView mainView;
	private MenuOptionsView menuOptionsView;
	private PDFParser parser;
	private PDFTextStripper pdfStripper;
	private PDDocument pdDoc ;
	private COSDocument cosDoc ;
	private File file;
	private String text;
	private String filename;
	private String time;
	private String docType = "Unknown type";
	private String[] allWords;
	private DefaultListModel<String> fileList; 
	private ArrayList<String> accountNumbers;
	private ArrayList<String> memberNumbers;
	private ArrayList<CSVEntry> csvEntries;

	//Default options
	private String input = System.getProperty("user.dir");
	private String output = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + "\\pdfOutput.csv";
	private boolean removeAccountDuplicates = false;
	private boolean removeMemberDuplicates = false;
	private boolean limitPages = false;
	private boolean showTime = false;
	private boolean refineSearch = true;
	private int startPage = 0;
	private int endPage = 0;
	private int accountSelectionLength = 8;
	private int memberSelectionLength = 7;
	private ArrayList<String> precedingWordFilter;

	//Output messages
	private final String DEFAULT_SELECT_OUTPUT = "Select an output file";
	private final String MESSAGE_COMPLETION = "Conversion completed! \n" + "File: ";
	private final String VIEW_TITLE = "PDF Account Number Extraction";
	private final String OPTIONS_TITLE = "Options";
	private final String PAGE_VALUE_EMPTY = "Please enter a value for the page numbers";
	private final String PAGE_VALUE_NOT_NUMERIC = "Please enter only numbers for the page value";
	private final String LENGTH_VALUE_INCORRECT = "Please enter a valid number for the length to find";
	private final String OUTPUT_SELECTION_TITLE = "Select an output file";
	private final String OUTPUT_SELECTION_DEFAULT = "PDFoutput.csv";
	private final String OUTPUT_OPEN_WARNING = "Please make sure the output file is not open";
	private final String PAGE_VALUE_INCORRECT = "Please enter a end page later than the start page";
	private final String ERROR_CONVERSION_FAILED = "File conversion failed";
	private final String ERROR_EXPORT_FAILED = "Exporting results failed";
	private final String ERROR_DATE_EXTRACTION_FAILED = "Time extraction error. Some files may not have correct file creation times.";

	public void setMainView(MainView view) {
		this.mainView = view;
		populateWordFilter();
		mainView.setTitle(VIEW_TITLE);
		mainView.getJtfOutput().setText(DEFAULT_SELECT_OUTPUT);
		if(mainView.getDefaultListModel().isEmpty()){
			mainView.getJbRemoveInput().setEnabled(false);
			mainView.getJbClearInput().setEnabled(false);
		}else{
			if(mainView.getJlInputFiles().isSelectionEmpty()){
				mainView.getJbRemoveInput().setEnabled(false);
			}else{
				mainView.getJbRemoveInput().setEnabled(true);
			}
		}
		mainView.getJlInputFiles().addListSelectionListener(new inputListSelectionListener());
		mainView.getJmiOptions().addActionListener(new menuOptionsListener());
		mainView.getJmiExit().addActionListener(new menuExitListener());
		mainView.getJbSelectInput().addActionListener(new selectInputListener());
		mainView.getJbRemoveInput().addActionListener(new removeInputListener());
		mainView.getJbClearInput().addActionListener(new clearInputListener());
		mainView.getJbSelectOutput().addActionListener(new selectOutputListener());
		mainView.getJbConvert().addActionListener(new convertListener());
	}
	public void setMenuOptionsView(){
		this.menuOptionsView = new MenuOptionsView();
		menuOptionsView.setTitle(OPTIONS_TITLE);
		menuOptionsView.getJtfAccountLength().setText(Integer.toString(accountSelectionLength));
		menuOptionsView.getJtfMemberLength().setText(Integer.toString(memberSelectionLength));
		menuOptionsView.getJcbRemoveAccountDuplicates().setSelected(removeAccountDuplicates);
		menuOptionsView.getJcbRemoveMemberDuplicates().setSelected(removeMemberDuplicates);
		menuOptionsView.getJcbShowTime().setSelected(showTime);
		menuOptionsView.getJtfStartPage().setEnabled(limitPages);
		menuOptionsView.getJtfStartPage().setText(Integer.toString(startPage));
		menuOptionsView.getJtfEndPage().setText(Integer.toString(endPage));
		menuOptionsView.getJtfEndPage().setEnabled(limitPages);
		menuOptionsView.getJcbLimitPages().setSelected(limitPages);
		menuOptionsView.getJcbLimitPages().addActionListener(new limitPagesListener());
		menuOptionsView.getJbApply().addActionListener(new applyOptionsListener());
		menuOptionsView.getJbCancel().addActionListener(new closeOptionsListener());
	}
	private class inputListSelectionListener implements ListSelectionListener{
		public void valueChanged(ListSelectionEvent e) {
			if(mainView.getJlInputFiles().isSelectionEmpty()){
				mainView.getJbRemoveInput().setEnabled(false);
			}else{
				mainView.getJbRemoveInput().setEnabled(true);
			}
		}
	}
	private class applyOptionsListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			boolean dispose = false;
			showTime = menuOptionsView.getJcbShowTime().isSelected();
			removeAccountDuplicates = menuOptionsView.getJcbRemoveAccountDuplicates().isSelected();
			removeMemberDuplicates = menuOptionsView.getJcbRemoveMemberDuplicates().isSelected();
			if(limitPages){
				if(menuOptionsView.getJtfStartPage().getText().equals(" ") || menuOptionsView.getJtfEndPage().getText().equals(" ")){
					JOptionPane.showMessageDialog(menuOptionsView, PAGE_VALUE_EMPTY);
					dispose = false;
				}else{
					if(!menuOptionsView.getJtfStartPage().getText().matches("[0-9]+") || !menuOptionsView.getJtfEndPage().getText().matches("[0-9]+")){
						JOptionPane.showMessageDialog(menuOptionsView, PAGE_VALUE_NOT_NUMERIC);
						dispose = false;
					}else{
						startPage = Integer.parseInt(menuOptionsView.getJtfStartPage().getText());
						endPage = Integer.parseInt(menuOptionsView.getJtfEndPage().getText());
						if(startPage>endPage){
							JOptionPane.showMessageDialog(menuOptionsView, PAGE_VALUE_INCORRECT);
							dispose = false;
						}else{
							dispose = true;
						}	
					}
				}
			}
			if(!menuOptionsView.getJtfAccountLength().getText().equals(" ") && menuOptionsView.getJtfAccountLength().getText().matches("[0-9]+")){
				accountSelectionLength = Integer.parseInt(menuOptionsView.getJtfAccountLength().getText());
				dispose = true;
			}else{
				JOptionPane.showMessageDialog(menuOptionsView, LENGTH_VALUE_INCORRECT);
				dispose = false;
			}

			if(!menuOptionsView.getJtfMemberLength().getText().equals(" ") && menuOptionsView.getJtfMemberLength().getText().matches("[0-9]+")){
				memberSelectionLength = Integer.parseInt(menuOptionsView.getJtfMemberLength().getText());
				dispose = true;
			}else{
				JOptionPane.showMessageDialog(menuOptionsView, LENGTH_VALUE_INCORRECT);
				dispose = false;
			}

			if(dispose){
				menuOptionsView.dispose();
			}
		}
	}
	private class limitPagesListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(menuOptionsView.getJcbLimitPages().isSelected()){
				menuOptionsView.getJtfStartPage().setEnabled(true);
				menuOptionsView.getJtfEndPage().setEnabled(true);
				limitPages = true;
			}else{
				menuOptionsView.getJtfStartPage().setEnabled(false);
				menuOptionsView.getJtfEndPage().setEnabled(false);
				limitPages = false;
			}
		}
	}
	private class closeOptionsListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			menuOptionsView.dispose();
		}
	}
	private class menuOptionsListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			setMenuOptionsView();
		}
	}
	private class menuExitListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			System.exit(0);
		}
	}
	private class selectInputListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			//When the input button is pressed
			ArrayList<String> fileList = getFilesChosen();
			if(fileList.size() != 0){
				mainView.setListModel(fileList);
				mainView.getJbClearInput().setEnabled(true);
			}
			if(!mainView.getJtfOutput().equals(DEFAULT_SELECT_OUTPUT)){
				mainView.getJbConvert().setEnabled(true);
			}
		}
	}
	private class removeInputListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			int removalIndex = mainView.getJlInputFiles().getSelectedIndex();
			mainView.getDefaultListModel().remove(removalIndex);
			mainView.getJbRemoveInput().setEnabled(false);
			if(mainView.getDefaultListModel().size() == 0){
				mainView.getJbClearInput().setEnabled(false);
			}
		}
	}
	private class clearInputListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			mainView.getDefaultListModel().removeAllElements();
			mainView.getJbClearInput().setEnabled(false);
		}
	}
	private class selectOutputListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			//When the output button is pressed
			JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
			chooser.setDialogTitle(OUTPUT_SELECTION_TITLE);
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV", "csv");
			chooser.setFileFilter(filter);
			chooser.setSelectedFile(new File(OUTPUT_SELECTION_DEFAULT));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = chooser.showSaveDialog(mainView);
			if(returnVal == JFileChooser.APPROVE_OPTION){
				output = chooser.getSelectedFile().getAbsolutePath();
				if(!output.endsWith(".csv")){
					output = output + ".csv";
				}
				mainView.setJtfOutput(output);
				if(!mainView.getDefaultListModel().isEmpty()){
					mainView.getJbConvert().setEnabled(true);
				}
			}
		}
	}
	private class convertListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			csvEntries = new ArrayList<CSVEntry>();
			fileList = mainView.getDefaultListModel();
			for(int i = 0; i<fileList.getSize(); i++){
				accountNumbers = new ArrayList<String>();
				memberNumbers = new ArrayList<String>();
				filename = fileList.getElementAt(i);
				text = null;
				try {
					convert(filename);
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(mainView, ERROR_CONVERSION_FAILED + " File: " + filename);
					return;
				}
				extractAccountNumber();	
				if(accountNumbers.size() == 0){
					extractMemberNumber();
				}
				try {
					extractDate();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(mainView, ERROR_DATE_EXTRACTION_FAILED);
					time = "N/A";
					e1.printStackTrace();
				}
				findDocType(filename);
				CSVEntry entry = new CSVEntry(time, filename, accountNumbers, memberNumbers, docType);
				csvEntries.add(entry);
			}
			//Removes duplicate numbers found in the same document
			removeDuplicates();
			try {
				exportCSV();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(mainView, ERROR_EXPORT_FAILED + "\n" + "Attempted export file: " + output + "\n" + OUTPUT_OPEN_WARNING);
				e1.printStackTrace();
				return;
			}
			JOptionPane.showMessageDialog(mainView, MESSAGE_COMPLETION + output);
		}
	}

	public ArrayList<String> getFilesChosen(){
		ArrayList<String> fileList = new ArrayList<String>(0);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF", "pdf");
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(filter);
		chooser.setMultiSelectionEnabled(true);
		int returnVal = chooser.showOpenDialog(mainView);
		if(returnVal == JFileChooser.APPROVE_OPTION){
			for(int i = 0; i < chooser.getSelectedFiles().length; i++){
				fileList.add(chooser.getSelectedFiles()[i].toString());
			}
		}
		return fileList;
	}
	public void convert(String pdf) throws IOException{
		this.pdfStripper = null;
		this.pdDoc = null;
		this.cosDoc = null;

		file = new File(pdf);
		parser = new PDFParser(new FileInputStream(file));
		parser.parse();
		cosDoc = parser.getDocument();
		pdfStripper = new PDFTextStripper();
		pdDoc = new PDDocument(cosDoc);
		pdDoc.getNumberOfPages();
		if(limitPages){
			pdfStripper.setStartPage(startPage);
			pdfStripper.setEndPage(endPage);
		}else{
			pdfStripper.setStartPage(0);
			pdfStripper.setEndPage(pdDoc.getNumberOfPages());
		}
		text = pdfStripper.getText(pdDoc);
		cosDoc.close();
	}
	
	public void populateWordFilter(){
		precedingWordFilter = new ArrayList<String>();
		precedingWordFilter.add("box");
		precedingWordFilter.add("iksa00");
	}
	/**
	 * For each file, extract the name and account number and add it to a list
	 */
	public void extractAccountNumber(){
		boolean add = true;
		allWords = text.split("[\\s]");	
		for(int i = 0; i< allWords.length; i++){
			if(allWords[i].length() == accountSelectionLength && allWords[i].matches("[0-9]+")){
				if(refineSearch){
					for(String f:precedingWordFilter){
						if(allWords[i-1].toLowerCase().equals(f)){
							add = false;
						}
					}
				}
				if(add){
					accountNumbers.add(allWords[i]);
				}
			}
		}	
	}
	public void extractMemberNumber(){
		boolean add;
		allWords = text.split("[\\s]");	
		//Iterate through all the words in the document
		for(int i = 0; i< allWords.length; i++){
			add = true;
			//Find the sequences of numbers with the correct length
			if(allWords[i].length() == memberSelectionLength && allWords[i].matches("[0-9]+")){
				//If refine search has been chosen and it is not the first word in the document, filter numbers based on preceding words
				if(refineSearch && i != 0){
					//For every string in the word filter check if the preceding word equals it
					for(String f:precedingWordFilter){
						if(allWords[i-1].toLowerCase().equals(f)){
							add = false;
						}
					}
				}
				if(add){
					memberNumbers.add(allWords[i]);
				}
			}
		}	
	}
	public void extractDate() throws IOException{
		Path p = Paths.get(filename);
		BasicFileAttributes a = null;
		a = Files.readAttributes(p, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
		if(a != null){
			FileTime creationTime = a.creationTime();
			time = formatTime(creationTime);
		}else{
			time = "Invalid Date";
		}
	}
	/**
	 * Changes the default filetime output to mm/dd/yyyy 00:00:00 AM format in local time
	 * @param creationTime System reported file creation time
	 * @return String formatted time 
	 */
	public String formatTime(FileTime creationTime){
		String formattedTime = creationTime.toString();
		String year = formattedTime.substring(0, 4);
		String month = formattedTime.substring(5,7);
		String day = formattedTime.substring(8,10);
		String time = formattedTime.substring(11,16);
		String period = "AM";
		int hours = Integer.parseInt(time.substring(0, 2));
		//Find the current time zone
		TimeZone local = TimeZone.getDefault();
		//Find the difference in hours from UTC
		int offset = local.getOffset(new Date().getTime())/(3600000);
		//Get the local time
		int localHours = hours + offset;
		//Convert from 24 hour clock to 12 hour
		if(localHours > 12){
			int newHours = localHours - 12;
			time = newHours + time.substring(2, time.length());
			period = "PM";
		}else{
			time = localHours + time.substring(2, time.length());
		}
		if(showTime){
			formattedTime = month + "/" + day + "/" + year + " " + time + " " + period;
		}else{
			formattedTime =  month + "/" + day + "/" + year;
		}
		return formattedTime;
	}
	/**
	 * Remove all duplicate numbers from the same file
	 * @return void
	 */
	public void removeDuplicates(){
		//Remove account number duplicates
		if(removeAccountDuplicates && removeMemberDuplicates){
			for(CSVEntry e:csvEntries){
				removeIndividualAccountDuplicate(e, 0);
				removeIndividualMemberDuplicate(e, 0);
			}
			//Remove member number duplicates
		}else if(removeAccountDuplicates){
			for(CSVEntry e:csvEntries){
				removeIndividualAccountDuplicate(e, 0);
			}
			//Remove both duplicates
		}else if(removeMemberDuplicates){
			for(CSVEntry e:csvEntries){
				removeIndividualMemberDuplicate(e, 0);
			}
		}
	}
	/**
	 * Remove all the account number duplicates for an individual entry
	 * @param e The entry to edit
	 * @param position The position to start from
	 */

	public void removeIndividualAccountDuplicate(CSVEntry e, int position){
		ArrayList<String> list = e.getAccountNumbers();
		//If the size of the number list is larger than the position index
		if(list.size() > position){
			//Get the number at the first position
			String comparisonNumber = list.get(position);
			//For each number after the comparison number, delete all matching numbers
			for(int i = position + 1; i < list.size(); i++){
				if(list.get(i).equals(comparisonNumber)){
					list.remove(i);
					removeIndividualAccountDuplicate(e, position);
				}
			}
			removeIndividualAccountDuplicate(e, position +1);
		}
	}
	/**
	 * Remove all the member number duplicates for an individual entry
	 * @param e The entry to edit
	 * @param position The position to start from
	 */

	public void removeIndividualMemberDuplicate(CSVEntry e, int position){
		ArrayList<String> list = e.getMemberNumbers();
		//If the size of the number list is larger than the position index
		if(list.size() > position){
			//Get the number at the first position
			String comparisonNumber = list.get(position);
			//For each number after the comparison number, delete all matching numbers
			for(int i = position + 1; i < list.size(); i++){
				if(list.get(i).equals(comparisonNumber)){
					list.remove(i);
					removeIndividualMemberDuplicate(e, position);
				}
			}
			removeIndividualMemberDuplicate(e, position +1);
		}
	}
	public void findDocType(String name){
		name = name.toLowerCase();
		if(name.contains("modification")){
			docType = "Loan Modification";
		}else if(name.contains("loan")){
			docType = "Loan Documents";
		}else if(name.contains("debitcheck") || name.contains("completeapplication")){
			docType = "Member Docs";
		}else{
			docType = "Uncategorized";
		}
	}
	/**
	 * Export the list of csv entries to a file
	 * @throws IOException
	 * @return void
	 */
	public void exportCSV() throws IOException{
		FileWriter writer = new FileWriter(output);
		//For each entry add the required information
		for(CSVEntry e:csvEntries){
			writer.append(e.getTime());
			writer.append(',');
			writer.append(e.getFilename());
			writer.append(',');
			for(String s:e.getAccountNumbers()){
				writer.append(s + ' ');
			}
			writer.append(',');
			for(String s:e.getMemberNumbers()){
				writer.append(s + ' ');
			}
			writer.append(',');
			writer.append(docType);
			writer.append('\n');
		}
		writer.flush();
		writer.close();
	}
	/**
	 * Runs without gui options if called from the driver
	 * @return void
	 */
	public void automate(){
		//Tells the user the selected arguments
		alertUser();
		populateWordFilter();
		csvEntries = new ArrayList<CSVEntry>();
		ArrayList<String> filesInDir = new ArrayList<String>();
		filesInDir = findPdfFiles();
		for(int i = 0; i<filesInDir.size(); i++){
			accountNumbers = new ArrayList<String>();
			filename = filesInDir.get(i);
			text = null;
			try {
				convert(filename);
			} catch (IOException e1) {
				System.out.println(ERROR_CONVERSION_FAILED + " File: " + filename);
				System.out.println("Error: " + e1.getMessage());
				e1.printStackTrace();
				return;
			}
			extractAccountNumber();	
			if(accountNumbers.size() == 0){
				extractMemberNumber();
			}
			try {
				extractDate();
			} catch (IOException e) {
				System.out.println(ERROR_DATE_EXTRACTION_FAILED);
				time = "N/A";
				e.printStackTrace();
			}
			findDocType(filename);
			CSVEntry entry = new CSVEntry(time, filename, accountNumbers, memberNumbers, docType);
			csvEntries.add(entry);
		}
		//Removes duplicate account numbers found in the same document	
		removeDuplicates();
		try {
			exportCSV();
		} catch (IOException e1) {
			System.out.println(ERROR_EXPORT_FAILED + " Export file: " + output);
			System.out.println("Error message: " + e1.getMessage());
			e1.printStackTrace();
			return;
		}
		System.out.println("Job finished! Result file: " + output);
	}
	public void alertUser(){
		System.out.println("Launching job with following settings:");
		System.out.println("Input -- " + input);
		System.out.println("Output -- " + output);
		if(removeAccountDuplicates){
			System.out.println("Account Number duplicate removal enabled"); 
		}else{
			System.out.println("Duplicate removal disabled"); 
		}
		if(removeMemberDuplicates){
			System.out.println("Member Number duplicate removal enabled"); 
		}else{
			System.out.println("Duplicate removal disabled"); 
		}
		if(limitPages){
			System.out.println("Page limit enabled");
			System.out.println("Start page -- " + startPage);
			System.out.println("End page -- " + endPage);
		}else{
			System.out.println("Page limit disabled");
		}
		System.out.println("Number length to extract -- " + accountSelectionLength);
		System.out.println();
	}
	public ArrayList<String> findPdfFiles(){
		ArrayList<String> filesInDir = new ArrayList<String>();
		File directory = new File(input);
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				String lower = name.toLowerCase();
				if(lower.endsWith(".pdf")){
					return true;
				}else{
					return false;
				}
			}
		};
		for(File f:directory.listFiles(filter)){
			filesInDir.add(f.toString());
		}
		return filesInDir;
	}
	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}

	public boolean isRemoveAccountDuplicates() {
		return removeAccountDuplicates;
	}
	public void setRemoveAccountDuplicates(boolean removeAccountDuplicates) {
		this.removeAccountDuplicates = removeAccountDuplicates;
	}
	public boolean isRemoveMemberDuplicates() {
		return removeMemberDuplicates;
	}
	public void setRemoveMemberDuplicates(boolean removeMemberDuplicates) {
		this.removeMemberDuplicates = removeMemberDuplicates;
	}
	public boolean isLimitPages() {
		return limitPages;
	}
	public void setLimitPages(boolean limitPages) {
		this.limitPages = limitPages;
	}
	public int getStartPage() {
		return startPage;
	}
	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}
	public int getEndPage() {
		return endPage;
	}
	public void setEndPage(int endPage) {
		this.endPage = endPage;
	}
	public int getSelectionLength() {
		return accountSelectionLength;
	}
	public void setSelectionLength(int selectionLength) {
		this.accountSelectionLength = selectionLength;
	}
	public String getInput() {
		return input;
	}
	public void setInput(String input) {
		this.input = input;
	}
	public void setShowTime(boolean flag){
		showTime = flag;
	}
}
