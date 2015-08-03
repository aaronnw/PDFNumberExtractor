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
	private String[] allWords;
	private DefaultListModel<String> fileList; 
	private ArrayList<String> accountNumbers;
	private ArrayList<CSVEntry> csvEntries;

	//Default options
	private String input = System.getProperty("user.dir");
	private String output = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + "\\pdfOutput.csv";
	private boolean removeDuplicates = false;
	private boolean limitPages = false;
	private int startPage = 0;
	private int endPage = 0;
	private int selectionLength = 8;
	
	//Output messages
	private final String DEFAULT_SELECT_OUTPUT = "Select an output file";
	private final String MESSAGE_COMPLETION = "Conversion completed! \n" + "File: ";
	private final String VIEW_TITLE = "PDF Account Number Extraction";
	private final String OPTIONS_TITLE = "Options";
	private final String PAGE_VALUE_EMPTY = "Please enter a value for the page numbers";
	private final String PAGE_VALUE_NOT_NUMERIC = "Please enter only numbers for the page value";
	private final String LENGTH_VALUE_INCORRECT = "Please enter a valid number for the length to find";
	private final String OUTPUT_SELECTION_TITLE = "Select an output file";
	private final String OUTPUT_SELECTIONI_DEFAULT = "PDFoutput.csv";
	private final String PAGE_VALUE_INCORRECT = "Please enter a end page later than the start page";

	public void setMainView(MainView view) {
		this.mainView = view;
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
		menuOptionsView.getJtfLength().setText(Integer.toString(selectionLength));
		menuOptionsView.getJtfLength().setText(Integer.toString(selectionLength));
		menuOptionsView.getJcbRemoveDuplicates().setSelected(removeDuplicates);
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
			removeDuplicates = menuOptionsView.getJcbRemoveDuplicates().isSelected();
			//TODO add more restrictions
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
			}else{
				if(!menuOptionsView.getJtfLength().getText().equals(" ") && menuOptionsView.getJtfLength().getText().matches("[0-9]+")){
					selectionLength = Integer.parseInt(menuOptionsView.getJtfLength().getText());
					dispose = true;
				}else{
					JOptionPane.showMessageDialog(menuOptionsView, LENGTH_VALUE_INCORRECT);
					dispose = false;
				}
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
			chooser.setSelectedFile(new File(OUTPUT_SELECTIONI_DEFAULT));
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
				filename = fileList.getElementAt(i);
				text = null;
				try {
					convert(filename);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				extractNumber();	
				extractDate();
				
				CSVEntry entry = new CSVEntry(time, filename, accountNumbers);
				csvEntries.add(entry);
			}
			//Removes duplicate account numbers found in the same document
			if(removeDuplicates){		
				removeDuplicates();
			}
			try {
				exportCSV();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
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
	/**
	 * For each file, extract the name and account number and add it to a list
	 */
	public void extractNumber(){
		allWords = text.split("[\\s]");	for(int i = 0; i< allWords.length; i++){
			if(allWords[i].length() == selectionLength && allWords[i].matches("[0-9]+")){
				accountNumbers.add(allWords[i]);
			}
		}	
	}
	public void extractDate(){
		Path p = Paths.get(filename);
		BasicFileAttributes a = null;
		try {
			a = Files.readAttributes(p, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
		formattedTime = month + "/" + day + "/" + year + " " + time + " " + period;
		return formattedTime;
	}
	/**
	 * Remove all duplicate numbers from the same file
	 * @return void
	 */
	public void removeDuplicates(){
		//For each entry, remove the duplicates
		for(CSVEntry e:csvEntries){
			removeIndividualDuplicate(e, 0);
		}
	}
	/**
	 * Remove all the duplicates for an individual entry
	 * @param e The entry to edit
	 * @param position The position to start from
 	 */
	
	public void removeIndividualDuplicate(CSVEntry e, int position){
		ArrayList<String> list = e.getAccountNumbers();
		//If the size of the number list is larger than the position index
		if(list.size() > position){
			//Get the number at the first position
			String comparisonNumber = list.get(position);
			//For each number after the comparison number, delete all matching numbers
			for(int i = position + 1; i < list.size(); i++){
				if(list.get(i).equals(comparisonNumber)){
					list.remove(i);
					removeIndividualDuplicate(e, position);
				}
			}
			removeIndividualDuplicate(e, position +1);
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
			for(String s:e.getAccountNumbers()){
				writer.append(',' + s);
			}
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			extractNumber();			
			CSVEntry entry = new CSVEntry(time, filename, accountNumbers);
			csvEntries.add(entry);
		}
		//Removes duplicate account numbers found in the same document
		if(removeDuplicates){		
			removeDuplicates();
		}
		try {
			exportCSV();
			System.out.println("Job finished! Result file: " + output);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void alertUser(){
		System.out.println("Launching job with following settings:");
		System.out.println("Input -- " + input);
		System.out.println("Output -- " + output);
		if(removeDuplicates){
			System.out.println("Duplicate removal enabled"); 
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
		System.out.println("Number length to extract -- " + selectionLength);
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
	public boolean isRemoveDuplicates() {
		return removeDuplicates;
	}
	public void setRemoveDuplicates(boolean removeDuplicates) {
		this.removeDuplicates = removeDuplicates;
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
		return selectionLength;
	}
	public void setSelectionLength(int selectionLength) {
		this.selectionLength = selectionLength;
	}
	public String getInput() {
		return input;
	}
	public void setInput(String input) {
		this.input = input;
	}
	
}
