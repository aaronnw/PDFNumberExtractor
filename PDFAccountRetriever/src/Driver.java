import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.UIManager;

public class Driver {
	//Declare the controller and view objects
	private static Controller controller = new Controller();
	private static MainView mainView;
	static HashMap<String, ArrayList<String>> params = new HashMap<>();

	public static void main(String[] args){
		//Set the UI to the system default
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		//Create variables to handle command line arguments
		ArrayList<String> options = null;

		//For each of the arguments entered
		for (int i = 0; i < args.length; i++) {
			final String a = args[i];
			//If the first character of the argument is not a hyphen throw an error
			if (a.charAt(0) == '-') {
				//If it is less than 2 there is nothing after the hyphen
				if (a.length() < 2) {
					System.err.println("Error at argument " + a);
					return;
				}
				options = new ArrayList<>();
				//Create a parameter with the argument as a key and a list of options
				params.put(a.substring(1), options);
			}else if (options != null) {
				options.add(a);
			}
			else {
				System.err.println("Illegal parameter usage");
				return;
			}
		}
		//Help tag
		if(params.containsKey("help") || params.containsKey("h") || params.containsKey("H")){
			System.out.println("Available arguments: ");
			System.out.println("-i -- Set input directory");
			System.out.println("-o -- Set output file");
			System.out.println("-e -- Set error file output directory");
			System.out.println("-dt -- Add a new type of document name to filter");
			System.out.println("-la -- Change length of account number to search");
			System.out.println("-lm -- Change length of member number to search");
			System.out.println("-da -- Enable duplicate account number removal");
			System.out.println("-dm -- Enable duplicate member number removal");
			System.out.println("-ps -- Define page to start search from");
			System.out.println("-pe -- Define page to end search on");
			System.out.println("-f -- Enable preceding word filter");
			System.out.println("-w -- Add a word to the preceding word filter");
			System.out.println("-t -- Show time in date column");
			System.out.println("-gui -- Enable graphical user interface");
			return;
		}
		//Input tag
		if(params.containsKey("i")){
			if(params.get("i").size() == 0){
				System.err.println("Add an argument for the input");
				return;
			}else{
				String inputFile = params.get("i").get(0);
				//If there are any pdfs in the given folder, set the input
				File directory = new File(inputFile);
				//Restricts input to pdf files
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
				//Makes sure at least one pdf was added
				if(directory.listFiles(filter).length>0){
					controller.setInput(inputFile);
				}else{
					System.err.println("Enter a directory with at least one pdf");
					return;
				}
			}
		}
		//Output tag
		if(params.containsKey("o")){
			if(params.get("o").size() == 0){
				System.err.println("Add an argument for the output");
				return;
			}else{
				//Gets the file path from the argument and sets it in the controller
				String outFile = params.get("o").get(0);
				if(outFile.endsWith(".csv") && !outFile.endsWith("\\.csv")){
					controller.setOutput(outFile);
				}else{
					System.err.println("Enter a csv file for the output");
					return;
				}
			}
		}
		//Error tag
		if(params.containsKey("e")){
			if(params.get("e").size() == 0){
				System.err.println("Add an argument for the error directory");
				return;
			}else{
				//Gets the file path from the argument and sets it in the controller
				String errorFolder = params.get("e").get(0);
				if(errorFolder.endsWith("\\")){
					controller.setErrorFolder(errorFolder);
				}else{
					System.err.println("Enter a folder for the error files");
					return;
				}
			}
		}
		//Account duplicates tag
		if(params.containsKey("da")){
			controller.setRemoveAccountDuplicates(true);
		}
		//Member duplicates tag
		if(params.containsKey("dm")){
			controller.setRemoveMemberDuplicates(true);
		}
		//Show time tag
		if(params.containsKey("t")){
			controller.setShowTime(true);
		}
		//Enable filter tag
		if(params.containsKey("f")){
			controller.setRefineSearch(true);
		}
		//Add filter words
		if(params.containsKey("w")){
			if(params.get("w").size() == 0){
				System.err.println("Add a word to add to to the filter");
				return;
			}else{
				String[] words = params.get("w").get(0).split(",");
				for(int i = 0; i<words.length; i ++){
					if(words[i] != ""){
						controller.appendPrecedingWordFilter(words[i]);
					}
				}
			}

		}
		//Account number Length tag
		if(params.containsKey("la")){
			if(params.get("la").size() == 0){
				System.err.println("Add an argument for the length of the account numbers");
				return;
			}else{
				String length = params.get("la").get(0);
				//Makes sure the argument is a number
				if(length.matches("[0-9]+")){
					controller.setAccountSelectionLength(Integer.parseInt(length));
				}else{
					System.err.println("Enter an integer for the length of the account number");
					return;
				}
			}
		}
		//Member number Length tag
		if(params.containsKey("lm")){
			if(params.get("lm").size() == 0){
				System.err.println("Add an argument for the length of the member numbers");
				return;
			}else{
				String length = params.get("lm").get(0);
				//Makes sure the argument is a number
				if(length.matches("[0-9]+")){
					controller.setMemberSelectionLength(Integer.parseInt(length));
				}else{
					System.err.println("Enter an integer for the length of the member number");
					return;
				}
			}
		}
		//Page start tag
		if(params.containsKey("ps")){
			if(params.get("ps").size() == 0){
				System.err.println("Add an argument for the starting page");
				return;
			}else{
				//Makes sure the argument is a number
				if(params.get("ps").get(0).matches("[0-9]+")){
					controller.setStartPage(Integer.parseInt(params.get("ps").get(0)));
				}else{
					System.err.println("Enter an integer for the starting page");
					return;
				}

			}
		}
		//Page end tag
		if(params.containsKey("pe")){
			if(params.get("pe").size() == 0){
				System.err.println("Add an argument for the ending page");
				return;
			}else{
				//Makes sure the argument is a number
				if(params.get("pe").get(0).matches("[0-9]+")){
					controller.setEndPage(Integer.parseInt(params.get("pe").get(0)));
				}else{
					System.err.println("Enter an integer for the ending page");
					return;
				}

			}
		}
		//GUI tag
		if(params.containsKey("gui")){
			mainView = new MainView();
			controller.setMainView(mainView);
		}else{
			//Run the automated program
			controller.automate();
		}
	}
}
