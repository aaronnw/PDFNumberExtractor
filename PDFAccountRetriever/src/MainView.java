import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class MainView extends JFrame{

	/**
	 * Auto-generated svid
	 */
	private static final long serialVersionUID = 3381539745365305131L;
	//Declare the variables
	private static JMenuBar jmbMenu;
	private static JMenu jmFile;
	private static JMenuItem jmiOptions;
	private static JMenuItem jmiExit;
	private static JPanel jpInput;
	private static JPanel jpInputButtons;
	private static JPanel jpOutput;
	private static JPanel jpMain;
	private static JButton jbSelectInput;
	private static JButton jbSelectOutput;
	private static JButton jbConvert;
	private static JButton jbRemoveInput;
	private static JButton jbClearInput;
	private static JScrollPane jspInput;
	private static JTextField jtfOutput;
	private static JLabel jlOutput;
	private static JList<String> jlInputFiles;
	private static DefaultListModel<String> listModel = new DefaultListModel<>();


	public MainView(){
		//Set up the scroll pane of input files	
		jlInputFiles = new JList<String>(listModel);
		jlInputFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlInputFiles.setLayoutOrientation(JList.VERTICAL_WRAP);
		jspInput = new JScrollPane(jlInputFiles);
		jspInput.setPreferredSize(new Dimension(400,200));
		
		
		//Set up the input buttons panel
		jbRemoveInput = new JButton("Remove entry");
		jbClearInput = new JButton("Clear all");
		jbSelectInput = new JButton("Select PDFs");
		jpInputButtons = new JPanel();
		jpInputButtons.setLayout(new BoxLayout(jpInputButtons, BoxLayout.Y_AXIS));
		jpInputButtons.add(jbSelectInput);
		jpInputButtons.add(Box.createRigidArea(new Dimension(0,5)));
		jpInputButtons.add(jbRemoveInput);
		jpInputButtons.add(Box.createRigidArea(new Dimension(0,5)));
		jpInputButtons.add(jbClearInput);
		
		//Set up the input panel
		jpInput = new JPanel();
		jpInput.add(jspInput);
		jpInput.add(jpInputButtons);

		//Set up output panel
		jpOutput = new JPanel();
		jtfOutput = new JTextField();
		jtfOutput.setEditable(false);
		jtfOutput.setFocusable(true);
		jtfOutput.setAutoscrolls(true);
		jtfOutput.setPreferredSize(new Dimension(200,20));
		jlOutput = new JLabel("Output File (csv): ");
		jbSelectOutput = new JButton("Change");
		jpOutput.add(jlOutput);
		jpOutput.add(jtfOutput);
		jpOutput.add(jbSelectOutput);

		//Set up main panel
		jpMain = new JPanel();
		jbConvert = new JButton("Convert");
		jbConvert.setEnabled(false);
		jpMain.add(jpInput);
		jpMain.add(jpOutput);
		jpMain.add(jbConvert);

		//Set up the menu
		jmbMenu = new JMenuBar();
		jmFile = new JMenu("File");
		jmiOptions = new JMenuItem("Options");
		jmiExit = new JMenuItem("Exit");
		jmFile.add(jmiOptions);
		jmFile.add(jmiExit);
		jmbMenu.add(jmFile);
		setJMenuBar(jmbMenu);

		//Add panel
		add(jpMain);
		//Set frame size to fit to content
		pack();
		//Set program to exit on close
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);		
	}
	public JButton getJbSelectInput() {
		return jbSelectInput;
	}

	public JButton getJbSelectOutput() {
		return jbSelectOutput;
	}

	public JButton getJbConvert() {
		return jbConvert;
	}
	public JButton getJbRemoveInput(){
		return jbRemoveInput;
	}
	public JButton getJbClearInput(){
		return jbClearInput;
	}
	public void setListModel(ArrayList<String> newArrayList){
		for(String s:newArrayList){
			listModel.addElement(s);
		}
		if(!listModel.isEmpty()){
			jbClearInput.setEnabled(true);
		}
	}
	public DefaultListModel<String> getDefaultListModel(){
		return listModel;
	}
	public JList<String> getJlInputFiles(){
		return jlInputFiles;
	}
	public JTextField getJtfOutput(){
		return jtfOutput;
	}
	public void setJtfOutput(String newOutput){
		jtfOutput.setText(newOutput);
	}
	public JMenuItem getJmiOptions(){
		return jmiOptions;
	}
	public JMenuItem getJmiExit(){
		return jmiExit;
	}
	public static JScrollPane getJspInput() {
		return jspInput;
	}
	public static void setJspInput(JScrollPane jspInput) {
		MainView.jspInput = jspInput;
	}
	
}
