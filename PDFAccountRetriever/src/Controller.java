/**
 * Created by Aaron Wilson
 * 8/14/2015
 * 
 * Uses the open-source apache pdfbox library
 * https://pdfbox.apache.org/
 */
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
import java.nio.file.StandardCopyOption;
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
	private ArrayList<String> precedingWordFilter;
	private int errorCount;

	//Default options
	private String input = System.getProperty("user.dir");
	private String output = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + "\\pdfOutput.csv";
	private String errorFolder = output.substring(0, output.lastIndexOf("\\")) + "\\PDFErrors\\";
	private boolean removeAccountDuplicates = false;
	private boolean removeMemberDuplicates = false;
	private boolean limitPages = false;
	private boolean showTime = false;
	private boolean refineSearch = true;
	private int startPage = 0;
	private int endPage = 0;
	private int accountSelectionLength = 8;
	private int memberSelectionLength = 7;

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
	private final String ERROR_PDF_INPUT_FAILED = "PDF input failed.";
	private final String ERROR_EXPORT_FAILED = "Exporting results failed";
	private final String ERROR_DATE_EXTRACTION_FAILED = "Time extraction error. Some files may not have correct file creation times.";
	private final String ERROR_HANDLE_ERROR_FAILED = "Copying error files to " + errorFolder + " failed.";
	/**
	 * Sets up the main gui view
	 * @param view The view to use
	 */
	public void setMainView(MainView view) {
		this.mainView = view;
		//Fills the list of preceding words to filter
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
	/**
	 * Sets up the menu view
	 */
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
	/**
	 * Listens for a selection in the file list
	 */
	private class inputListSelectionListener implements ListSelectionListener{
		public void valueChanged(ListSelectionEvent e) {
			//When something changes in the list, check if a file is selected
			if(mainView.getJlInputFiles().isSelectionEmpty()){
				//Enable the remove input button if a file is selected
				mainView.getJbRemoveInput().setEnabled(false);
			}else{
				//Disable the romve input button if no file is selected
				mainView.getJbRemoveInput().setEnabled(true);
			}
		}
	}
	/**
	 * Listens for the apply button to be pressed in the menu view
	 */
	private class applyOptionsListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			boolean dispose = false;
			//Sets the variable in the controller based on menu options
			showTime = menuOptionsView.getJcbShowTime().isSelected();
			removeAccountDuplicates = menuOptionsView.getJcbRemoveAccountDuplicates().isSelected();
			removeMemberDuplicates = menuOptionsView.getJcbRemoveMemberDuplicates().isSelected();
			//Gets the start and end pages only if the limit is checked and valid values are entered
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
			//Change the account selection length if a valid value is entered
			if(!menuOptionsView.getJtfAccountLength().getText().equals(" ") && menuOptionsView.getJtfAccountLength().getText().matches("[0-9]+")){
				accountSelectionLength = Integer.parseInt(menuOptionsView.getJtfAccountLength().getText());
				dispose = true;
			}else{
				JOptionPane.showMessageDialog(menuOptionsView, LENGTH_VALUE_INCORRECT);
				dispose = false;
			}
			//Change the member selection length if a valid value is entered
			if(!menuOptionsView.getJtfMemberLength().getText().equals(" ") && menuOptionsView.getJtfMemberLength().getText().matches("[0-9]+")){
				memberSelectionLength = Integer.parseInt(menuOptionsView.getJtfMemberLength().getText());
				dispose = true;
			}else{
				JOptionPane.showMessageDialog(menuOptionsView, LENGTH_VALUE_INCORRECT);
				dispose = false;
			}
			//If all the options are handled, close the options menu
			if(dispose){
				menuOptionsView.dispose();
			}
		}
	}
	/**
	 * Listens for the check box to limit pages in the menu view
	 */
	private class limitPagesListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			//Only if the checkbox is selected enable the text inputs
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
	/**
	 * Listens for the close button in the menu view
	 */
	private class closeOptionsListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			//Close the menu when pressed
			menuOptionsView.dispose();
		}
	}
	/**
	 * Listens for the menu bar to open the options
	 */
	private class menuOptionsListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			//Open the options view
			setMenuOptionsView();
		}
	}
	/**
	 * Listens for the menu bar to exit the program
	 */
	private class menuExitListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			//Completely exit the application
			System.exit(0);
		}
	}
	/**
	 * Listens for the button to select input in the main view
	 */
	private class selectInputListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			//When the input button is pressed
			ArrayList<String> fileList = getFilesChosen();
			//If there are files in the list, enable the clear input button
			if(fileList.size() != 0){
				mainView.setListModel(fileList);
				mainView.getJbClearInput().setEnabled(true);
			}
			//If an output has been defined enable the convert button
			if(!mainView.getJtfOutput().equals(DEFAULT_SELECT_OUTPUT)){
				mainView.getJbConvert().setEnabled(true);
			}
		}
	}
	/**
	 * Listens for the button to remove an input file
	 */
	private class removeInputListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			//Get the index of the file selected, remove it, and disable the remove button
			int removalIndex = mainView.getJlInputFiles().getSelectedIndex();
			mainView.getDefaultListModel().remove(removalIndex);
			mainView.getJbRemoveInput().setEnabled(false);
			//If this action empties the list, disable the clear input button
			if(mainView.getDefaultListModel().size() == 0){
				mainView.getJbClearInput().setEnabled(false);
			}
		}
	}
	/**
	 * Listens for the button to clear all input files
	 */
	private class clearInputListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			//Delete all the file entries and disable the clear button
			mainView.getDefaultListModel().removeAllElements();
			mainView.getJbClearInput().setEnabled(false);
		}
	}
	/**
	 * Listens for the button to change the output
	 */
	private class selectOutputListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			//Create a window to choose the output file
			JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
			chooser.setDialogTitle(OUTPUT_SELECTION_TITLE);
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			//Restrict the type of file to use
			FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV", "csv");
			chooser.setFileFilter(filter);
			chooser.setSelectedFile(new File(OUTPUT_SELECTION_DEFAULT));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = chooser.showSaveDialog(mainView);
			//Set a csv to output to based on the selection
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
	/**
	 * Listens for a press of the gui convert button
	 */
	private class convertListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			//Creates a list for the csv entries
			csvEntries = new ArrayList<CSVEntry>();
			//Gets the file list info from the scroll panel in the view
			fileList = mainView.getDefaultListModel();
			//Iterate through each file
			for(int i = 0; i<fileList.getSize(); i++){
				//Initialize variables
				accountNumbers = new ArrayList<String>();
				memberNumbers = new ArrayList<String>();
				filename = fileList.getElementAt(i);
				text = null;
				//Convert this file in the list
				try {
					convert(filename);
				//If it fails, show an error
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(mainView, ERROR_CONVERSION_FAILED + "\n" +  "File: " + filename);
				}
				//Extract the account numbers from the file
				extractAccountNumber();	
				//If no account numbers are found, extract member numbers
				if(accountNumbers.size() == 0){
					extractMemberNumber();
				}
				//Find the creation date of the file
				try {
					extractDate();
				//If the date cannot be found, show an error
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(mainView, ERROR_DATE_EXTRACTION_FAILED);
					time = "N/A";
					e1.printStackTrace();
				}
				//Analyze the file name to determine a document type
				findDocType(filename);
				//Create a new csv entry with the extracted information
				CSVEntry entry = new CSVEntry(time, filename, accountNumbers, memberNumbers, docType);
				//Add the entry to the list
				csvEntries.add(entry);
			}
			//Removes duplicate numbers found in the same document
			removeDuplicates();
			//Copies error files to the error folder
			try {
				handleErrorFiles();
			} catch (IOException e2) {
				JOptionPane.showMessageDialog(mainView, ERROR_HANDLE_ERROR_FAILED);
				e2.printStackTrace();
			}
			//Exports the data to the output csv file
			try {
				exportCSV();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(mainView, ERROR_EXPORT_FAILED + "\n" + "Attempted export file: " + output + "\n" + OUTPUT_OPEN_WARNING);
				e1.printStackTrace();
				return;
			}
			//Display the final message to the user
			JOptionPane.showMessageDialog(mainView, MESSAGE_COMPLETION + output + "\n" + errorCount + " error files located at " + errorFolder);
		}
	}
	/**
	 * Gets a list of pdfs from a chosen directory
	 * @return ArrayList<String> A list of pdf files
	 */
	public ArrayList<String> getFilesChosen(){
		ArrayList<String> fileList = new ArrayList<String>(0);
		//Opens a window to choose a file with a filter for pdf files
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF", "pdf");
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(filter);
		chooser.setMultiSelectionEnabled(true);
		int returnVal = chooser.showOpenDialog(mainView);
		//Add all the selected pdf files to the list
		if(returnVal == JFileChooser.APPROVE_OPTION){
			for(int i = 0; i < chooser.getSelectedFiles().length; i++){
				fileList.add(chooser.getSelectedFiles()[i].toString());
			}
		}
		return fileList;
	}
	/**
	 * Converts a pdf to a single string holding all the pdf text
	 * Uses methods from the pdfbox library to extract the text
	 * 
	 * @param pdf The filename for the pdf
	 * @throws IOException If a file IO error occurs
	 */
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
		//If the pages are limited, set the start and end
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
	/**
	 * Adds string values to the preceding word filter
	 */
	public void populateWordFilter(){
		precedingWordFilter = new ArrayList<String>();
		precedingWordFilter.add("box");
		precedingWordFilter.add("iksa00");
	}
	/**
	 * For each file, extract the account number and add it to a list
	 */
	public void extractAccountNumber(){
		boolean add;
		//Split all the text into an array of strings by space characters
		allWords = text.split("[\\s]");	
		//Iterate through all the words in the document
		for(int i = 0; i< allWords.length; i++){
			add = true;
			//Find the sequences of numbers with the correct length
			if(allWords[i].length() == accountSelectionLength && allWords[i].matches("[0-9]+")){
				//If refine search is enabled and it is not the first word
				if(refineSearch && i!=0){
					//For all the strings in the filter
					for(String f:precedingWordFilter){
						//If a filtered word precedes the word at the index, do not add it
						if(allWords[i-1].toLowerCase().equals(f)){
							add = false;
						}
					}
				}
				if(add){
					//Add the number sequence to the account number list
					accountNumbers.add(allWords[i]);
				}
			}
		}	
	}
	/**
	 * For each file, extract the member number and add it to a list
	 */
	public void extractMemberNumber(){
		boolean add;
		//Split all the text into an array of strings by space characters
		allWords = text.split("[\\s]");	
		//Iterate through all the words in the document
		for(int i = 0; i< allWords.length; i++){
			add = true;
			//Find the sequences of numbers with the correct length
			if(allWords[i].length() == memberSelectionLength && allWords[i].matches("[0-9]+")){
				//If refine search is enabled and it is not the first word
				if(refineSearch && i != 0){
					//For all the strings in the filter
					for(String f:precedingWordFilter){
						//If a filtered word precedes the word at the index, do not add it
						if(allWords[i-1].toLowerCase().equals(f)){
							add = false;
						}
					}
				}
				if(add){
					//Add the number sequence to the member number list
					memberNumbers.add(allWords[i]);
				}
			}
		}	
	}
	/**
	 * Gets the creation date and time from a file
	 * @throws IOException If there is an error reading the file attributes
	 */
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
	 * Changes the default filetime output to mm/dd/yyyy HH:MM:SS AM format in local time
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
		//If show time is enabled, return the time along with the day
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
	/**
	 * Finds the document type based on the file name
	 * @param name The name of the file
	 */
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
	 * Exports the list of csv entries to a file
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
			writer.append(e.getDocType());
			writer.append('\n');
		}
		writer.flush();
		writer.close();
	}
	/**
	 * Moves the error files to the error folder
	 * @throws IOException If there is a file IO exception
	 */
	public void handleErrorFiles() throws IOException{
		errorCount = 0;
		//Iterate through all the csv entries
		for(CSVEntry e:csvEntries){
			//If there are no account numbers or member numbers found
			if(e.getAccountNumbers().size() == 0 && e.getMemberNumbers().size() == 0){
				File f = new File(e.getFilename());
				File dest = new File(errorFolder + f.getName());
				//Create the error folder if it does not exist
				if(!Files.exists(Paths.get(errorFolder))){
					Files.createDirectory(Paths.get(errorFolder));
				}
				//Increment the count of error files
				errorCount++;
				//Copy the error file to the error folder
				Files.copy(Paths.get(f.getPath()),Paths.get(dest.getPath()), StandardCopyOption.REPLACE_EXISTING );
			}
		}
	}
	/**
	 * Runs without gui options if called from the driver
	 * @return void
	 */
	public void automate(){
		//Tells the user the selected arguments
		alertUser();
		//Fills the list of preceding words to filter
		populateWordFilter();
		csvEntries = new ArrayList<CSVEntry>();
		ArrayList<String> filesInDir = new ArrayList<String>();
		//Gets a list of pdf files from the input directory
		try {
			filesInDir = findPdfFiles();
		} catch (IOException e2) {
			System.err.println(ERROR_PDF_INPUT_FAILED);
			e2.printStackTrace();
			return;
		}
		//Iterate through each file in the list
		for(int i = 0; i<filesInDir.size(); i++){
			//Create new variables for each file
			accountNumbers = new ArrayList<String>();
			memberNumbers = new ArrayList<String>();
			filename = filesInDir.get(i);
			text = null;
			//Convert the pdf to a single string of text
			try {
				convert(filename);
			} catch (IOException e1) {
				System.err.println(ERROR_CONVERSION_FAILED + " File: " + filename);
				System.err.println("Error: " + e1.getMessage());
				e1.printStackTrace();
				return;
			}
			//Get the account number from the text
			extractAccountNumber();	
			//If no account numbers are found, look for member numbers
			if(accountNumbers.size() == 0){
				extractMemberNumber();
			}
			//Find the creation date of the file
			try {
				extractDate();
			} catch (IOException e) {
				System.err.println(ERROR_DATE_EXTRACTION_FAILED);
				time = "N/A";
				e.printStackTrace();
			}
			//Find the document type based on the name
			findDocType(filename);
			//Create a new entry with the extracted data
			CSVEntry entry = new CSVEntry(time, filename, accountNumbers, memberNumbers, docType);
			//Add the entry to the list
			csvEntries.add(entry);
		}
		//Remove duplicate account numbers found in the same document	
		removeDuplicates();
		//Copy error files to the error folder
		try {
			handleErrorFiles();
		} catch (IOException e) {
			System.err.println(ERROR_HANDLE_ERROR_FAILED);
			//Print out the error file names
			System.out.println("Error files: ");
			for(CSVEntry entry:csvEntries){
				if(entry.getAccountNumbers().size() == 0 && entry.getMemberNumbers().size() == 0){
					System.out.println(entry.getFilename());
				}
			}
			e.printStackTrace();
		}
		//Export the data to the output csv file
		try {
			exportCSV();
		} catch (IOException e1) {
			System.err.println(ERROR_EXPORT_FAILED + " Export file: " + output);
			System.err.println("Error message: " + e1.getMessage());
			e1.printStackTrace();
			return;
		}
		//Final message
		System.out.println("Job finished! Result file: " + output + "\n" + errorCount + " error files located at " + errorFolder);
	}
	public void alertUser(){
		System.out.println("Launching job with following settings:");
		System.out.println("Input -- " + input);
		System.out.println("Output -- " + output);
		System.out.println("Error files -- " + errorFolder);
		if(removeAccountDuplicates){
			System.out.println("Account number duplicate removal enabled"); 
		}else{
			System.out.println("Account number duplicate removal disabled"); 
		}
		if(removeMemberDuplicates){
			System.out.println("Member number duplicate removal enabled"); 
		}else{
			System.out.println("Member number duplicate removal disabled"); 
		}
		if(limitPages){
			System.out.println("Page limit enabled");
			System.out.println("Start page -- " + startPage);
			System.out.println("End page -- " + endPage);
		}else{
			System.out.println("Page limit disabled");
		}
		System.out.println("Account number length to extract -- " + accountSelectionLength);
		System.out.println("Member number length to extract -- " + memberSelectionLength);
		System.out.println();
	}
	public ArrayList<String> findPdfFiles() throws IOException{
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
	public int getAccountSelectionLength() {
		return accountSelectionLength;
	}
	public void setAccountSelectionLength(int selectionLength) {
		this.accountSelectionLength = selectionLength;
	}

	public int getMemberSelectionLength() {
		return memberSelectionLength;
	}
	public void setMemberSelectionLength(int memberSelectionLength) {
		this.memberSelectionLength = memberSelectionLength;
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
	public String getErrorFolder() {
		return errorFolder;
	}
	public void setErrorFolder(String errorFolder) {
		this.errorFolder = errorFolder;
	}

}
