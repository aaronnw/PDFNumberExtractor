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
	private JCheckBox jcbShowTime;
	private JCheckBox jcbRemoveAccountDuplicates;
	private JCheckBox jcbRemoveMemberDuplicates;
	private JCheckBox jcbLimitPages;
	private JLabel jlPage;
	private JLabel jlStartPage;
	private JLabel jlEndPage;
	private JTextField jtfStartPage;
	private JTextField jtfEndPage;
	private JLabel jlAccountLength;
	private JLabel jlMemberLength;
	private JTextField jtfAccountLength;
	private JTextField jtfMemberLength;
	private JPanel jpPages;
	private JPanel jpAccountLength;
	private JPanel jpMemberLength;
	private JPanel jpOptions;
	private JPanel jpConfirmation;

	public MenuOptionsView(){

		Dimension numberField = new Dimension(75,20);
		
		//Set up the show time option
		jcbShowTime = new JCheckBox("Show time in date column");
		jcbShowTime.setHorizontalTextPosition(SwingConstants.LEFT);
		jcbShowTime.setAlignmentX(Component.CENTER_ALIGNMENT);
		
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
		
		//Set up the length of account number string to extract
		jlAccountLength = new JLabel("Account number length to search: ");
		jtfAccountLength = new JTextField();
		jtfAccountLength.setToolTipText("Enter a length for account numbers to find");
		jtfAccountLength.setPreferredSize(numberField);
		jpAccountLength = new JPanel();
		jpAccountLength.add(jlAccountLength);
		jpAccountLength.add(jtfAccountLength);
		
		//Set up the length of member number string to extract
		jlMemberLength = new JLabel("Member number length to search: ");
		jtfMemberLength = new JTextField();
		jtfMemberLength.setToolTipText("Enter a length for member numbers to find");
		jtfMemberLength.setPreferredSize(numberField);
		jpMemberLength = new JPanel();
		jpMemberLength.add(jlMemberLength);
		jpMemberLength.add(jtfMemberLength);
		
		//Set up the option to remove duplicate account number results
		jcbRemoveAccountDuplicates = new JCheckBox("Remove account number duplicates");
		jcbRemoveAccountDuplicates.setHorizontalTextPosition(SwingConstants.LEFT);
		jcbRemoveAccountDuplicates.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		//Set up the option to remove duplicate member number results
		jcbRemoveMemberDuplicates = new JCheckBox("Remove member number duplicates");
		jcbRemoveMemberDuplicates.setHorizontalTextPosition(SwingConstants.LEFT);
		jcbRemoveMemberDuplicates.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		//Add all the options to the option panel
		jpOptions = new JPanel();
		jpOptions.setLayout(new BoxLayout(jpOptions, BoxLayout.Y_AXIS));
		jpOptions.add(jcbShowTime);
		jpOptions.add(jcbRemoveAccountDuplicates);
		jpOptions.add(jcbRemoveMemberDuplicates);
		jpOptions.add(jcbLimitPages);
		jpOptions.add(jpPages);
		jpOptions.add(jpAccountLength);
		jpOptions.add(jpMemberLength);
		
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
	 * Gets the checkbox to show the time
	 * @return JCheckBox jcbShowTime
	 */
	public JCheckBox getJcbShowTime() {
		return jcbShowTime;
	}
	/**
	 * Sets the checkbox to show the time
	 * @param JCheckBoxjcbShowTime
	 */
	public void setJcbShowTime(JCheckBox jcbShowTime) {
		this.jcbShowTime = jcbShowTime;
	}
	/**
	 * Gets the checkbox to remove account duplicates
	 * @return JCheckBox jcbRemoveAccountDuplicates
	 */
	public JCheckBox getJcbRemoveAccountDuplicates() {
		return jcbRemoveAccountDuplicates;
	}
	/**
	 * Set the checkbox to remove account duplicates
	 * @param JCheckBox jcbRemoveAccountDuplicates
	 */
	public void setJcbRemoveAccountDuplicates(JCheckBox jcbRemoveAccountDuplicates) {
		this.jcbRemoveAccountDuplicates = jcbRemoveAccountDuplicates;
	}
	/**
	 * Gets the checkbox to remove member duplicates
	 * @return JCheckBox jcbRemoveMemberDuplicates
	 */
	public JCheckBox getJcbRemoveMemberDuplicates() {
		return jcbRemoveMemberDuplicates;
	}
	/**
	 * Set the checkbox to remove member duplicates
	 * @param JCheckBox jcbRemoveMemberDuplicates
	 */
	public void setJcbRemoveMemberDuplicates(JCheckBox jcbRemoveMemberDuplicates) {
		this.jcbRemoveMemberDuplicates = jcbRemoveMemberDuplicates;
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

	public JTextField getJtfAccountLength() {
		return jtfAccountLength;
	}

	public void setJtfAccountLength(JTextField jtfLength) {
		this.jtfAccountLength = jtfLength;
	}
	
	public JTextField getJtfMemberLength() {
		return jtfMemberLength;
	}

	public void setJtfMemberLength(JTextField jtfLength) {
		this.jtfMemberLength = jtfLength;
	}	
}	
