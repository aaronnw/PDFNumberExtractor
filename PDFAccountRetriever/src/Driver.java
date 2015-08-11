import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.UIManager;

public class Driver {
	private static Controller controller = new Controller();
	private static MainView mainView;

	public static void main(String[] args){
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		HashMap<String, ArrayList<String>> params = new HashMap<>();
		ArrayList<String> options = null;

		//For each of the arguments entered
		for (int i = 0; i < args.length; i++) {
			final String a = args[i];
			//If the first character of the argument is not a hyphen throw an error
			if (a.charAt(0) == '-') {
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
			System.out.println("-a -- Enable duplicate account number removal");
			System.out.println("-i -- Set input directory");
			System.out.println("-l -- Change length of number to search");
			System.out.println("-m -- Enable duplicate member number removal");
			System.out.println("-o -- Set output file");
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
				String outFile = params.get("o").get(0);
				if(outFile.endsWith(".csv") && !outFile.endsWith("\\.csv")){
					controller.setOutput(outFile);
				}else{
					System.err.println("Enter a csv file for the output");
					return;
				}
			}
		}
		//Account duplicates tag
		if(params.containsKey("a")){
			controller.setRemoveAccountDuplicates(true);
		}
		//Member duplicates tag
		if(params.containsKey("m")){
			controller.setRemoveMemberDuplicates(true);
		}
		//Show time tag
		if(params.containsKey("t")){
			controller.setShowTime(true);
		}
		//Length tag
		if(params.containsKey("l")){
			if(params.get("l").size() == 0){
				System.err.println("Add an argument for the length");
				return;
			}else{
				String length = params.get("l").get(0);
				if(length.matches("[0-9]+")){
					controller.setSelectionLength(Integer.parseInt(length));
				}else{
					System.err.println("Enter an integer for the length");
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
