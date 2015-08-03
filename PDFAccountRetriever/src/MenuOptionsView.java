import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class MenuOptionsView extends JFrame {
	/**
	 * Generated svid
	 */
	private static final long serialVersionUID = -7590573328850174760L;
	private JButton jbApply;
	private JButton jbCancel;
	private JCheckBox jcbRemoveDuplicates;
	private JCheckBox jcbLimitPages;
	private JLabel jlPage;
	private JLabel jlStartPage;
	private JLabel jlEndPage;
	private JTextField jtfStartPage;
	private JTextField jtfEndPage;
	private JLabel jlLength;
	private JTextField jtfLength;
	private JPanel jpPages;
	private JPanel jpLength;
	private JPanel jpOptions;
	private JPanel jpConfirmation;

	public MenuOptionsView(){

		Dimension numberField = new Dimension(75,20);
		
		//Set up the page limit option
		jcbLimitPages = new JCheckBox("Limit Pages");
		jcbLimitPages.setHorizontalTextPosition(SwingConstants.LEFT);
		jcbLimitPages.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		//Set up the page specification
		jlPage = new JLabel("Pages");
		jlPage.setAlignmentX(Component.CENTER_ALIGNMENT);
		jtfStartPage = new JTextField();
		jtfEndPage = new JTextField();
		jtfStartPage.setToolTipText("Enter a whole number");
		jtfEndPage.setToolTipText("Enter a whole number");
		jtfStartPage.setPreferredSize(numberField);
		jtfEndPage.setPreferredSize(numberField);
		jlStartPage = new JLabel("Start on page: ");
		jlEndPage = new JLabel("End on page: ");
		jpPages = new JPanel();
		jpPages.add(jlStartPage);
		jpPages.add(jtfStartPage);
		jpPages.add(jlEndPage);
		jpPages.add(jtfEndPage);
		
		//Set up the length of string to extract
		jlLength = new JLabel("Length to search: ");
		jtfLength = new JTextField();
		jtfLength.setToolTipText("Enter a length to find");
		jtfLength.setPreferredSize(numberField);
		jpLength = new JPanel();
		jpLength.add(jlLength);
		jpLength.add(jtfLength);
		
		//Set up the option to remove duplicate results
		jcbRemoveDuplicates = new JCheckBox("Remove duplicates");
		jcbRemoveDuplicates.setHorizontalTextPosition(SwingConstants.LEFT);
		jcbRemoveDuplicates.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		//Add all the options to the option panel
		jpOptions = new JPanel();
		jpOptions.setLayout(new BoxLayout(jpOptions, BoxLayout.Y_AXIS));
		jpOptions.add(jcbRemoveDuplicates);
		jpOptions.add(jcbLimitPages);
		jpOptions.add(jpPages);
		jpOptions.add(jpLength);
		
		//Create the confirmation buttons and add them to a panel
		jbApply = new JButton("Apply");
		jbCancel = new JButton("Cancel");
		jpConfirmation= new JPanel();
		jpConfirmation.add(jbApply);
		jpConfirmation.add(jbCancel);
		
		//Set the frame layout and spacing
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		add(jpOptions);
		add(Box.createRigidArea(new Dimension(0,5)));
		add(jpConfirmation);
		//Set frame size to fit to content
		pack();
		//Set program to exit on close
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);		
	}
	/**
	 * Returns apply button
	 * @return JButton jbApply
	 */
	public JButton getJbApply() {
		return jbApply;
	}
	/**
	 * Sets apply button
	 * @param JButton jbApply
	 */
	public void setJbApply(JButton jbApply) {
		this.jbApply = jbApply;
	}
	/**
	 * Returns cancel button
	 * @return JButton jbCancel
	 */
	public JButton getJbCancel() {
		return jbCancel;
	}
	/**
	 * Sets cancel button
	 * @param JButton jbCancel
	 */
	public void setJbCancel(JButton jbCancel) {
		this.jbCancel = jbCancel;
	}
	/**
	 * Gets the checkbox to remove duplicates
	 * @return JCheckBox jcbRemoveDuplicates
	 */
	public JCheckBox getJcbRemoveDuplicates() {
		return jcbRemoveDuplicates;
	}
	/**
	 * Set the checkbox to remove duplicates
	 * @param JCheckBox jcbRemoveDuplicates
	 */
	public void setJcbRemoveDuplicates(JCheckBox jcbRemoveDuplicates) {
		this.jcbRemoveDuplicates = jcbRemoveDuplicates;
	}
	/**
	 * Gets the checkbox to limit pages
	 * @return CheckBox jcbLimitPages
	 */
	public JCheckBox getJcbLimitPages() {
		return jcbLimitPages;
	}

	public void setJcbLimitPages(JCheckBox jcbLimitPages) {
		this.jcbLimitPages = jcbLimitPages;
	}

	public JTextField getJtfStartPage() {
		return jtfStartPage;
	}

	public void setJtfStartPage(JFormattedTextField jtfStartPage) {
		this.jtfStartPage = jtfStartPage;
	}

	public JTextField getJtfEndPage() {
		return jtfEndPage;
	}

	public void setJtfEndPage(JFormattedTextField jtfEndPage) {
		this.jtfEndPage = jtfEndPage;
	}

	public JTextField getJtfLength() {
		return jtfLength;
	}

	public void setJtfLength(JTextField jtfLength) {
		this.jtfLength = jtfLength;
	}
	
}
