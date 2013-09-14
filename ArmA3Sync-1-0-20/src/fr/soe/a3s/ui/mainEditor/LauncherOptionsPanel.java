package fr.soe.a3s.ui.mainEditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fr.soe.a3s.constant.MaxMemoryValues;
import fr.soe.a3s.dto.configuration.LauncherOptionsDTO;
import fr.soe.a3s.exception.ProfileException;
import fr.soe.a3s.service.AddonService;
import fr.soe.a3s.service.ConfigurationService;
import fr.soe.a3s.service.LaunchService;
import fr.soe.a3s.service.ProfileService;
import fr.soe.a3s.ui.Facade;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class LauncherOptionsPanel extends JPanel implements DocumentListener {

	private Facade facade;
	private JPanel launcherOptionsPanel, performancePanel, armaPanel,steamPanel;
	private JTextArea runParametersTextArea, additionalParametersTextArea;
	private JScrollPane scrollPaneRunParameters, scrollPaneAditionalParameters;
	private JTextField textFieldArmAExecutableLocation,textFieldSteamExecutableLocation;
	private JButton buttonSelectArmAExe,buttonSelectSteamExe;
	private JComboBox comboBoxProfiles, comboBoxMaxMemory, comboBoxCpuCount;
	private JCheckBox checkBoxProfiles, checkBoxNoPause, checkBoxWindowMode,
			checkBoxShowScriptErrors, checkBoxRunBeta,checkBoxMaxMemory, checkBoxCpuCount,
			checkBoxNoSplashScreen, checkBoxDefaultWorld;
	private ConfigurationService configurationService = new ConfigurationService();
	private ProfileService profileService = new ProfileService();
	private AddonService addonService = new AddonService();
	private LaunchService launchService = new LaunchService();

	public LauncherOptionsPanel(Facade facade) {
		this.facade = facade;
		this.facade.setLaunchOptionsPanel(this);
		this.setLayout(new BorderLayout());

		Box vertBox1 = Box.createVerticalBox();
		vertBox1.add(Box.createVerticalStrut(10));
		// JPanel containerPanel = new JPanel();
		// containerPanel.setBorder(BorderFactory.createTitledBorder(
		// BorderFactory.createEtchedBorder(), "Favorite servers"));
		// vertBox1.add(containerPanel);
		// this.add(vertBox1);
		// containerPanel.setLayout(new BorderLayout());

		JPanel centerPanel = new JPanel();
		GridLayout grid1 = new GridLayout(1, 2);
		centerPanel.setLayout(grid1);
		vertBox1.add(centerPanel, BorderLayout.CENTER);
		this.add(vertBox1);

		launcherOptionsPanel = new JPanel();
		launcherOptionsPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Launcher Options"));

		performancePanel = new JPanel();
		performancePanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Performance"));

		Box vertBox = Box.createVerticalBox();
		vertBox.add(launcherOptionsPanel);
		vertBox.add(performancePanel);
		centerPanel.add(vertBox);

		/* Launch options */
		launcherOptionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		Box vBox = Box.createVerticalBox();
		launcherOptionsPanel.add(vBox);
		{
			checkBoxProfiles = new JCheckBox();
			checkBoxProfiles.setText("Profile:   ");
			comboBoxProfiles = new JComboBox();
			javax.swing.filechooser.FileSystemView fsv = javax.swing.filechooser.FileSystemView
					.getFileSystemView();
			File myDocuments = fsv.getDefaultDirectory();
			List<String> listProfileNames = new ArrayList<String>();
			if (myDocuments != null) {
				File[] subfiles = myDocuments.listFiles();
				for (File file : subfiles) {
					String name = file.getName().toUpperCase();
					if (name.contains("ARMA 3")
							&& name.contains("OTHER PROFILES")) {
						File[] subf = file.listFiles();
						if (subf!=null){
							for (int i = 0; i < subf.length; i++) {
								listProfileNames.add(subf[i].getName());
							}
						}
					}
				}
			}
			String[] tab = new String[listProfileNames.size() + 1];
			tab[0] = "Default";
			for (int i = 0; i < listProfileNames.size(); i++) {
				tab[i + 1] = listProfileNames.get(i);
			}
			ComboBoxModel profilesModel = new DefaultComboBoxModel(tab);
			comboBoxProfiles.setModel(profilesModel);
			comboBoxProfiles.setPreferredSize(new Dimension(120, 8));
			Box hBox = Box.createHorizontalBox();
			hBox.add(checkBoxProfiles);
			hBox.add(comboBoxProfiles);
			vBox.add(hBox);
		}
		{
			checkBoxShowScriptErrors = new JCheckBox();
			checkBoxShowScriptErrors.setText("Show script errors");
			checkBoxShowScriptErrors.setFont(new Font("Tohama", Font.BOLD, 11));
			Box hBox = Box.createHorizontalBox();
			hBox.add(checkBoxShowScriptErrors);
			hBox.add(Box.createHorizontalGlue());
			vBox.add(hBox);
		}
		{
			checkBoxNoPause = new JCheckBox();
			checkBoxNoPause.setText("No Pause");
			Box hBox = Box.createHorizontalBox();
			hBox.add(checkBoxNoPause);
			hBox.add(Box.createHorizontalGlue());
			vBox.add(hBox);
		}
		{
			checkBoxWindowMode = new JCheckBox();
			checkBoxWindowMode.setText("Window Mode");
			Box hBox = Box.createHorizontalBox();
			hBox.add(checkBoxWindowMode);
			hBox.add(Box.createHorizontalGlue());
			vBox.add(hBox);
		}
		// {
		// checkBoxRunBeta = new JCheckBox();
		// checkBoxRunBeta.setText("Run beta");
		// JLabel labelLinkBeta = new JLabel(
		// "<HTML> <FONT color=\"#000099\"><U>Download beta</U></FONT>"
		// + "</HTML>");
		// Box hBox = Box.createHorizontalBox();
		// hBox.add(checkBoxRunBeta);
		// hBox.add(labelLinkBeta);
		// vBox.add(hBox);
		// }

		/* Performances */
		performancePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		vBox = Box.createVerticalBox();
		performancePanel.add(vBox);
		{
			checkBoxMaxMemory = new JCheckBox();
			checkBoxMaxMemory.setText("Max Memory:   ");
			comboBoxMaxMemory = new JComboBox();
			ComboBoxModel maxMemoryModel = new DefaultComboBoxModel(
					new String[] {
							"",
							Integer.toString(MaxMemoryValues.MIN.getValue()),
							Integer.toString(MaxMemoryValues.MEDIUM.getValue()),
							Integer.toString(MaxMemoryValues.MAX.getValue()) });
			comboBoxMaxMemory.setModel(maxMemoryModel);
			comboBoxMaxMemory.setPreferredSize(new Dimension(95, 8));
			Box hBox = Box.createHorizontalBox();
			hBox.add(checkBoxMaxMemory);
			hBox.add(comboBoxMaxMemory);
			vBox.add(hBox);
		}
		{
			checkBoxCpuCount = new JCheckBox();
			checkBoxCpuCount.setText("CPU Count:      ");
			comboBoxCpuCount = new JComboBox();
			Runtime runtime = Runtime.getRuntime();
			int nbProcessors = runtime.availableProcessors();
			String[] tab = new String[nbProcessors + 1];
			tab[0] = "";
			for (int i = 1; i <= nbProcessors; i++) {
				tab[i] = Integer.toString(i);
			}
			ComboBoxModel cpuCountModel = new DefaultComboBoxModel(tab);
			comboBoxCpuCount.setModel(cpuCountModel);
			comboBoxCpuCount.setPreferredSize(new Dimension(95, 8));
			Box hBox = Box.createHorizontalBox();
			hBox.add(checkBoxCpuCount);
			hBox.add(comboBoxCpuCount);
			vBox.add(hBox);
		}
		{
			checkBoxNoSplashScreen = new JCheckBox();
			checkBoxNoSplashScreen.setText("No Splash Screen");
			Box hBox = Box.createHorizontalBox();
			hBox.add(checkBoxNoSplashScreen);
			hBox.add(Box.createHorizontalGlue());
			vBox.add(hBox);
		}
		{
			checkBoxDefaultWorld = new JCheckBox();
			checkBoxDefaultWorld.setText("Default World Empty");
			Box hBox = Box.createHorizontalBox();
			hBox.add(checkBoxDefaultWorld);
			hBox.add(Box.createHorizontalGlue());
			vBox.add(hBox);
		}

		/* Run parameters */
		runParametersTextArea = new JTextArea();
		runParametersTextArea.setFont(new Font("Tohama", Font.ITALIC, 11));
		runParametersTextArea.setLineWrap(true);
		runParametersTextArea.setFont(new Font("Tohama", Font.PLAIN, 11));
		runParametersTextArea.setEditable(false);
		runParametersTextArea.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));
		scrollPaneRunParameters = new JScrollPane(runParametersTextArea);
		scrollPaneRunParameters.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Run Parameters"));

		/* Additional parameters */
		additionalParametersTextArea = new JTextArea();
		additionalParametersTextArea
				.setFont(new Font("Tohama", Font.ITALIC, 11));
		additionalParametersTextArea.setLineWrap(true);
		additionalParametersTextArea
				.setFont(new Font("Tohama", Font.PLAIN, 11));
		additionalParametersTextArea.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));
		scrollPaneAditionalParameters = new JScrollPane(
				additionalParametersTextArea);
		scrollPaneAditionalParameters.setBorder(BorderFactory
				.createTitledBorder(BorderFactory.createEtchedBorder(),
						"Additional Parameters"));

		vertBox = Box.createVerticalBox();
		vertBox.add(scrollPaneRunParameters);
		vertBox.add(scrollPaneAditionalParameters);
		centerPanel.add(vertBox);

		/* Executable locations */
		JPanel southPanel = new JPanel();
		GridLayout grid2 = new GridLayout(0, 1);
		southPanel.setLayout(grid2);
		this.add(southPanel, BorderLayout.SOUTH);
		{
			armaPanel = new JPanel();
			armaPanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(),
					"ArmA III Executable Location (game/server)"));
			southPanel.add(armaPanel);
			armaPanel.setLayout(new BorderLayout());

			vBox = Box.createVerticalBox();
			{
				JPanel panel1 = new JPanel();
				panel1.setLayout(new BorderLayout());
				textFieldArmAExecutableLocation = new JTextField();
				textFieldArmAExecutableLocation.setEditable(false);
				buttonSelectArmAExe = new JButton("Select");
				panel1.add(textFieldArmAExecutableLocation, BorderLayout.CENTER);
				panel1.add(buttonSelectArmAExe, BorderLayout.EAST);
				vBox.add(panel1);
			}
			armaPanel.add(vBox);
		}
//		{
//			steamPanel = new JPanel();
//			steamPanel.setBorder(BorderFactory.createTitledBorder(
//					BorderFactory.createEtchedBorder(),
//					"Steam Executable Location"));
//			southPanel.add(steamPanel);
//			steamPanel.setLayout(new BorderLayout());
//			
//			vBox = Box.createVerticalBox();
//			{
//				JPanel panel1 = new JPanel();
//				panel1.setLayout(new BorderLayout());
//				textFieldSteamExecutableLocation = new JTextField();
//				textFieldSteamExecutableLocation.setEditable(false);
//				buttonSelectSteamExe = new JButton("Select");
//				panel1.add(textFieldSteamExecutableLocation, BorderLayout.CENTER);
//				panel1.add(buttonSelectSteamExe, BorderLayout.EAST);
//				vBox.add(panel1);
//			}
//			steamPanel.add(vBox);
//		}
		
		
		// {
		// armaoaPanel = new JPanel();
		// armaoaPanel.setBorder(BorderFactory.createTitledBorder(
		// BorderFactory.createEtchedBorder(),
		// "ArmA II - OA Executable Location"));
		// southPanel.add(armaoaPanel);
		// armaoaPanel.setLayout(new BorderLayout());
		//
		// vBox = Box.createVerticalBox();
		// {
		// JPanel panel1 = new JPanel();
		// panel1.setLayout(new BorderLayout());
		// textFieldArmAOAExecutableLocation = new JTextField();
		// textFieldArmAOAExecutableLocation.setEditable(false);
		// buttonSelectArmAOAExe = new JButton("Select");
		// panel1.add(textFieldArmAOAExecutableLocation,
		// BorderLayout.CENTER);
		// panel1.add(buttonSelectArmAOAExe, BorderLayout.EAST);
		// vBox.add(panel1);
		// }
		// armaoaPanel.add(vBox);
		// }
		checkBoxProfiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkBoxProfilesPerformed();
			}
		});
		comboBoxProfiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				comboBoxProfilesPerformed();
			}
		});
		checkBoxShowScriptErrors.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkBoxShowScriptErrorsPerformed();
			}
		});
		checkBoxNoPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkBoxNoPausePerformed();
			}
		});
		checkBoxWindowMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkBoxWindowModePerformed();
			}
		});
		// checkBoxRunBeta.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// checkBoxRunPerformed();
		// }
		// });
		checkBoxMaxMemory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkBoxMaxMemoryPerformed();
			}
		});
		comboBoxMaxMemory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				comboBoxMaxMemoryPerformed();
			}
		});
		checkBoxCpuCount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkBoxCpuCountPerformed();
			}
		});
		comboBoxCpuCount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				comboBoxCpuCountPerformed();
			}
		});
		checkBoxNoSplashScreen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkBoxNoSplashScreenPerformed();
			}
		});
		checkBoxDefaultWorld.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkBoxDefaultWorldPerformed();
			}
		});
		buttonSelectArmAExe.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				buttonSelectArmAExePerformed();
			}
		});
//		buttonSelectSteamExe.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				buttonSelectSteamExePerformed();
//			}
//		});
		
		
		additionalParametersTextArea.getDocument().addDocumentListener(this);
		setContextualHelp();
	}

	private void setContextualHelp() {

		comboBoxProfiles
				.setToolTipText("(optional) Select in game profile name");
		checkBoxShowScriptErrors.setToolTipText("Show in game script error");
		checkBoxNoPause
				.setToolTipText("Don't suspend the game when placed in background");
		checkBoxWindowMode
				.setToolTipText("Display the game windowed instead of full screen");
		// checkBoxRunBeta.setToolTipText("Run the game with beta patch");
		comboBoxMaxMemory.setToolTipText("Restricts memory allocation");
		comboBoxCpuCount.setToolTipText("Restricts number of cores used");
		checkBoxNoSplashScreen.setToolTipText("Disables splash screens");
		checkBoxDefaultWorld.setToolTipText("No world loaded at game startup");
	}

	public void init() {

		LauncherOptionsDTO launcherOptionsDTO = configurationService
				.getLauncherOptions();

		/* Launcher options */
		if (launcherOptionsDTO.getGameProfile() != null) {
			comboBoxProfiles.setSelectedItem(launcherOptionsDTO
					.getGameProfile());
			checkBoxProfiles.setSelected(true);
		}

		checkBoxShowScriptErrors.setSelected(launcherOptionsDTO
				.isShowScriptError());
		checkBoxNoPause.setSelected(launcherOptionsDTO.isNoPause());
		checkBoxWindowMode.setSelected(launcherOptionsDTO.isWindowMode());
		// checkBoxRunBeta.setSelected(launcherOptionsDTO.isRunBeta());

		/* Performance */
		if (launcherOptionsDTO.getMaxMemorySelection() != null) {
			comboBoxMaxMemory.setSelectedItem(launcherOptionsDTO
					.getMaxMemorySelection());
			checkBoxMaxMemory.setSelected(true);
		}

		if (launcherOptionsDTO.getCpuCountSelection() != 0) {
			comboBoxCpuCount.setSelectedItem(Integer
					.toString(launcherOptionsDTO.getCpuCountSelection()));
			checkBoxCpuCount.setSelected(true);
		}

		checkBoxNoSplashScreen.setSelected(launcherOptionsDTO
				.isNoSplashScreen());
		checkBoxDefaultWorld.setSelected(launcherOptionsDTO.isDefaultWorld());

		/* Executable locations */
		textFieldArmAExecutableLocation.setText(launcherOptionsDTO.getArma3ExePath());

		/* Run parameters */
		updateRunParameters();
		
		/* Additional Parameters */
		try {
			String additionalParameters = profileService
					.getAdditionalParameters();
			if (additionalParameters != null) {
				additionalParametersTextArea.setText(additionalParameters);
			}
		} catch (ProfileException e) {
			e.printStackTrace();
		}
		
	
	}

	/* Components selection */
	private void checkBoxProfilesPerformed() {
		if (!checkBoxProfiles.isSelected()) {
			comboBoxProfiles.setSelectedIndex(0);
		}
		updateRunParameters();
	}

	private void comboBoxProfilesPerformed() {

		String gameProfileName = (String) comboBoxProfiles.getSelectedItem();
		if (gameProfileName == null) {
			return;
		}
		if (!gameProfileName.equals("Default")) {
			checkBoxProfiles.setSelected(true);
			configurationService.setGameProfile(gameProfileName);
		} else {
			checkBoxProfiles.setSelected(false);
			configurationService.setGameProfile(null);
		}
		updateRunParameters();
	}

	private void checkBoxShowScriptErrorsPerformed() {
		configurationService
				.setCheckBoxShowScriptErrors(checkBoxShowScriptErrors
						.isSelected());
		updateRunParameters();
	}

	private void checkBoxNoPausePerformed() {
		configurationService.setCheckBoxNoPause(checkBoxNoPause.isSelected());
		updateRunParameters();
	}

	private void checkBoxWindowModePerformed() {
		configurationService.setCheckBoxWindowMode(checkBoxWindowMode
				.isSelected());
		updateRunParameters();
	}

	private void checkBoxRunPerformed() {
		configurationService.setCheckBoxRun(checkBoxRunBeta.isSelected());
		updateRunParameters();
	}

	private void checkBoxMaxMemoryPerformed() {
		if (!checkBoxMaxMemory.isSelected()) {
			comboBoxMaxMemory.setSelectedIndex(0);
		}
		updateRunParameters();
	}

	private void comboBoxMaxMemoryPerformed() {

		String maxMemory = (String) comboBoxMaxMemory.getSelectedItem();
		if (maxMemory == null) {
			return;
		}
		if (!maxMemory.isEmpty()) {
			checkBoxMaxMemory.setSelected(true);
			configurationService.setMaxMemory(maxMemory);
		} else {
			checkBoxMaxMemory.setSelected(false);
			configurationService.setMaxMemory(null);
		}
		updateRunParameters();
	}

	private void checkBoxCpuCountPerformed() {
		if (!checkBoxCpuCount.isSelected()) {
			comboBoxCpuCount.setSelectedIndex(0);
		}
		updateRunParameters();
	}

	private void comboBoxCpuCountPerformed() {

		String cpuCount = (String) comboBoxCpuCount.getSelectedItem();
		if (cpuCount == null) {
			return;
		}
		if (!cpuCount.isEmpty()) {
			checkBoxCpuCount.setSelected(true);
			configurationService.setCpuCount(cpuCount);
		} else {
			checkBoxCpuCount.setSelected(false);
			configurationService.setCpuCount(null);
		}
		updateRunParameters();
	}

	private void checkBoxNoSplashScreenPerformed() {
		configurationService.setNoSplashScreen(checkBoxNoSplashScreen
				.isSelected());
		updateRunParameters();
	}

	private void checkBoxDefaultWorldPerformed() {
		configurationService.setDefaultWorld(checkBoxDefaultWorld.isSelected());
		updateRunParameters();
	}

	private void buttonSelectArmAExePerformed() {

		JFileChooser fc = null;
		String arma3Path = configurationService.determineArmA3Path();
		if (arma3Path==null){
			fc = new JFileChooser();
		}else {
			File arma3Folder = new File(arma3Path);
			if (arma3Folder.exists()){
				fc = new JFileChooser(arma3Path);
			}else {
				fc = new JFileChooser();
			}
		}

		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = fc.showOpenDialog(LauncherOptionsPanel.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String path = file.getAbsolutePath();
			String parentPath = file.getParentFile().getAbsolutePath();
			configurationService.setArmA3ExePath(path);
			configurationService.getAddonSearchDirectoryPaths().add(parentPath.toLowerCase());
			facade.getAddonOptionsPanel().updateAddonSearchDirectories();
			addonService.resetAvailableAddonTree();
			facade.getAddonsPanel().updateAvailableAddons();
			facade.getAddonsPanel().updateAddonGroups();
			facade.getAddonsPanel().expandAddonGroups();
			textFieldArmAExecutableLocation.setText(path);
		} else {
			configurationService.setArmA3ExePath(null);
			textFieldArmAExecutableLocation.setText("");
		}
	}
	
	private void buttonSelectSteamExePerformed() {
		
		String steamExePath = configurationService.determineSteamExePath();
		JFileChooser fc = new JFileChooser(steamExePath);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = fc.showOpenDialog(LauncherOptionsPanel.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String path = file.getAbsolutePath();
			configurationService.setSteamExePath(path);
			textFieldSteamExecutableLocation.setText(path);
		} else {
			configurationService.setSteamExePath(null);
			textFieldSteamExecutableLocation.setText("");
		}
	}

	/* Update Run Parameters */
	public void updateRunParameters() {

		runParametersTextArea.setText("");
		String runParameters = launchService.getRunParameters();

		if (runParameters == null) {
			return;
		}

		int runParametersRows = 1;
		String txt = "";
		StringTokenizer st = new StringTokenizer(runParameters, "-");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			txt = txt + ("-" + token + "\n");
		}
		runParametersTextArea.setText(txt);
		
/*		
		StringTokenizer st1 = new StringTokenizer(runParameters, ";");
		while (st1.hasMoreTokens()) {
			String token = st1.nextToken();
			txt = txt + (token + ";" + "\n");
		}
		int endIndex = txt.lastIndexOf("\"");
		if (endIndex != -1) {
			String txt1 = txt.substring(0, endIndex);
			runParametersTextArea.setText(txt1 + "\n");
		}
		String txt2 = txt.substring(endIndex + 1, txt.length() - 1);
		StringTokenizer st2 = new StringTokenizer(txt2, " ");
		while (st2.hasMoreTokens()) {
			String token = st2.nextToken();
			runParametersTextArea.append(token + "\n");
		}
*/
		runParametersTextArea.setRows(runParametersRows);
		runParametersTextArea.setCaretPosition(0);
	}

	/* Additional Parameters */
	public void setAdditionalParameters() {

		String additionalParameters = additionalParametersTextArea.getText()
				.trim();
		try {
			//System.out.println(additionalParameters);
			profileService.setAdditionalParameters(additionalParameters);
		} catch (ProfileException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void updateAdditionalParameters() {

		try {
			String additionalParameters = profileService
					.getAdditionalParameters();
			if (additionalParameters != null) {
				additionalParametersTextArea.setText(additionalParameters);
				additionalParametersTextArea.setCaretPosition(0);
				additionalParametersTextArea.updateUI();
			}else {
				additionalParametersTextArea.setText("");
			}
		} catch (ProfileException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/* additionalParametersTextArea modificiation listener */
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		String additionalParameters = additionalParametersTextArea.getText();
		try {
			profileService.setAdditionalParameters(additionalParameters);
		} catch (ProfileException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		String additionalParameters = additionalParametersTextArea.getText();
		try {
			profileService.setAdditionalParameters(additionalParameters);
		} catch (ProfileException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		String additionalParameters = additionalParametersTextArea.getText();
		try {
			profileService.setAdditionalParameters(additionalParameters);
		} catch (ProfileException e) {
			e.printStackTrace();
		}
	}
}