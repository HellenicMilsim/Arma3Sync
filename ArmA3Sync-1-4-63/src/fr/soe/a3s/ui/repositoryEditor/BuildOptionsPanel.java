package fr.soe.a3s.ui.repositoryEditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import fr.soe.a3s.dto.configuration.FavoriteServerDTO;
import fr.soe.a3s.exception.WritingException;
import fr.soe.a3s.service.ConfigurationService;
import fr.soe.a3s.service.RepositoryService;
import fr.soe.a3s.ui.CheckBoxList;
import fr.soe.a3s.ui.Facade;
import fr.soe.a3s.ui.UIConstants;

public class BuildOptionsPanel extends JDialog implements UIConstants {

	private final Facade facade;
	private JList excludedFilesFromBuildList;
	private JScrollPane scrollPane1;
	private JList excludedFoldersFromSyncList;
	private JScrollPane scrollPane2;
	private CheckBoxList checkBoxListFavoriteServers;
	private JScrollPane scrollPane3;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JButton buttonAdd1;
	private JButton buttonRemove1;
	private JButton buttonAdd2;
	private JButton buttonRemove2;
	private JButton buttonAdd3;
	private JButton buttonRemove3;
	private final String repositoryName;
	private JComboBox<Integer> comboBoxConnections;
	/* Services */
	private final ConfigurationService configurationService = new ConfigurationService();
	private final RepositoryService repositoryService = new RepositoryService();

	public BuildOptionsPanel(Facade facade, String repositoryName) {
		super(facade.getMainPanel(), "Build options", true);
		this.facade = facade;
		this.repositoryName = repositoryName;
		setLocationRelativeTo(facade.getMainPanel());
		this.setResizable(true);
		this.setMinimumSize(new Dimension(500, 600));
		setIconImage(ICON);
		this.setLocation(
				(int) facade.getMainPanel().getLocation().getX()
						+ facade.getMainPanel().getWidth() / 2
						- this.getWidth() / 2,
				(int) facade.getMainPanel().getLocation().getY()
						+ facade.getMainPanel().getHeight() / 2
						- this.getHeight() / 2);

		this.setLayout(new BorderLayout());

		Box vertBox1 = Box.createVerticalBox();
		this.add(vertBox1, BorderLayout.CENTER);

		JPanel centerPanel = new JPanel();
		vertBox1.add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		{
			JPanel connectionsPanel = new JPanel();
			connectionsPanel.setLayout(new BorderLayout());
			{
				JPanel panel = new JPanel();
				panel.setLayout(new FlowLayout(FlowLayout.LEFT));
				panel.setBorder(BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(),
						"Clients connections"));
				JLabel label = new JLabel();
				label.setText("Set maximum number of connections per client: ");
				comboBoxConnections = new JComboBox<Integer>();
				ComboBoxModel comboBoxModel = new DefaultComboBoxModel(
						new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
				comboBoxConnections.setModel(comboBoxModel);
				comboBoxConnections.setFocusable(false);
				comboBoxConnections.setPreferredSize(new Dimension(50, 25));
				comboBoxConnections.setMaximumRowCount(10);
				panel.add(label);
				panel.add(comboBoxConnections);
				connectionsPanel.add(panel, BorderLayout.CENTER);
			}
			{
				JPanel panel = new JPanel();
				buttonAdd1 = new JButton("");
				ImageIcon addIcon = new ImageIcon(ADD);
				buttonAdd1.setIcon(addIcon);
				panel.setPreferredSize(new Dimension(buttonAdd1
						.getPreferredSize()));
				connectionsPanel.add(panel, BorderLayout.EAST);
			}

			JPanel favoriteServersPanel = new JPanel();
			favoriteServersPanel.setLayout(new BorderLayout());
			{
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				panel.setBorder(BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(),
						"Favorite servers infos set to autoconfig"));
				checkBoxListFavoriteServers = new CheckBoxList();
				scrollPane1 = new JScrollPane(checkBoxListFavoriteServers);
				scrollPane1.setBorder(BorderFactory
						.createEtchedBorder(BevelBorder.LOWERED));
				panel.add(scrollPane1, BorderLayout.CENTER);
				favoriteServersPanel.add(panel, BorderLayout.CENTER);
			}
			{
				JPanel panel = new JPanel();
				buttonAdd1 = new JButton("");
				ImageIcon addIcon = new ImageIcon(ADD);
				buttonAdd1.setIcon(addIcon);
				panel.setPreferredSize(new Dimension(buttonAdd1
						.getPreferredSize()));
				favoriteServersPanel.add(panel, BorderLayout.EAST);
			}

			JPanel excludedFilesPanel = new JPanel();
			excludedFilesPanel.setLayout(new BorderLayout());
			{
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				panel.setBorder(BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(),
						"Repository files excluded from build"));
				excludedFilesFromBuildList = new JList();
				scrollPane2 = new JScrollPane(excludedFilesFromBuildList);
				scrollPane2.setBorder(BorderFactory
						.createEtchedBorder(BevelBorder.LOWERED));
				panel.add(scrollPane2, BorderLayout.CENTER);
				excludedFilesPanel.add(panel, BorderLayout.CENTER);
			}
			{
				Box vertBox = Box.createVerticalBox();
				vertBox.add(Box.createVerticalStrut(15));
				buttonAdd2 = new JButton("");
				ImageIcon addIcon = new ImageIcon(ADD);
				buttonAdd2.setIcon(addIcon);
				vertBox.add(buttonAdd2);
				buttonRemove2 = new JButton("");
				ImageIcon deleteIcon = new ImageIcon(DELETE);
				buttonRemove2.setIcon(deleteIcon);
				vertBox.add(buttonRemove2);
				vertBox.add(Box.createVerticalStrut(80));
				excludedFilesPanel.add(vertBox, BorderLayout.EAST);
			}

			JPanel excludedFoldersWithExtraLocalContentPanel = new JPanel();
			excludedFoldersWithExtraLocalContentPanel
					.setLayout(new BorderLayout());
			{
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				panel.setBorder(BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(),
						"Repository folders with excluded extra local content when sync"));
				excludedFoldersFromSyncList = new JList();
				scrollPane3 = new JScrollPane(excludedFoldersFromSyncList);
				scrollPane3.setBorder(BorderFactory
						.createEtchedBorder(BevelBorder.LOWERED));
				panel.add(scrollPane3, BorderLayout.CENTER);
				excludedFoldersWithExtraLocalContentPanel.add(panel,
						BorderLayout.CENTER);
			}
			{
				Box vertBox = Box.createVerticalBox();
				vertBox.add(Box.createVerticalStrut(15));
				buttonAdd3 = new JButton("");
				ImageIcon addIcon = new ImageIcon(ADD);
				buttonAdd3.setIcon(addIcon);
				vertBox.add(buttonAdd3);
				buttonRemove3 = new JButton("");
				ImageIcon deleteIcon = new ImageIcon(DELETE);
				buttonRemove3.setIcon(deleteIcon);
				vertBox.add(buttonRemove3);
				vertBox.add(Box.createVerticalStrut(80));
				excludedFoldersWithExtraLocalContentPanel.add(vertBox,
						BorderLayout.EAST);
			}
			centerPanel.add(connectionsPanel);
			centerPanel.add(favoriteServersPanel);
			centerPanel.add(excludedFilesPanel);
			centerPanel.add(excludedFoldersWithExtraLocalContentPanel);
		}
		{
			JPanel panelNorth = new JPanel();
			this.add(panelNorth, BorderLayout.NORTH);
			JPanel panelEast = new JPanel();
			this.add(panelEast, BorderLayout.EAST);
			JPanel panelWest = new JPanel();
			this.add(panelWest, BorderLayout.WEST);
		}
		{
			JPanel controlPanel = new JPanel();
			buttonOK = new JButton("OK");
			buttonOK.setPreferredSize(new Dimension(75, 25));
			buttonCancel = new JButton("Cancel");
			buttonCancel.setPreferredSize(new Dimension(75, 25));
			FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
			controlPanel.setLayout(flowLayout);
			controlPanel.add(buttonOK);
			controlPanel.add(buttonCancel);
			this.add(controlPanel, BorderLayout.SOUTH);
		}
		buttonAdd2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonAdd2Performed();
			}
		});
		buttonRemove2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonRemove2Performed();
			}
		});
		buttonAdd3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonAdd3Performed();
			}
		});
		buttonRemove3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonRemove3Performed();
			}
		});
		buttonOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						buttonOKPerformed();
					}
				});
			}
		});
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonCancelPerformed();
			}
		});
		// Add Listeners
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				menuExitPerformed();
			}
		});
	}

	public void init() {

		// Client connections
		int numberOfConnections = repositoryService
				.getNumberOfConnections(repositoryName);
		if (numberOfConnections == 0) {
			comboBoxConnections.setSelectedIndex(0);
		} else {
			comboBoxConnections.setSelectedItem(numberOfConnections);
		}

		// Favorite servers
		List<FavoriteServerDTO> favoriteServerDTOs = configurationService
				.getFavoriteServers();

		List<FavoriteServerDTO> list = repositoryService
				.getFavoriteServerSetToAutoconfig(repositoryName);

		JCheckBox[] tab2 = new JCheckBox[favoriteServerDTOs.size()];
		for (int i = 0; i < favoriteServerDTOs.size(); i++) {
			String name = favoriteServerDTOs.get(i).getName();
			JCheckBox checkBox = new JCheckBox();
			checkBox.setText(name);
			for (FavoriteServerDTO f : list) {
				if (f.getName().equals(name)) {
					checkBox.setSelected(true);
					break;
				}
			}
			tab2[i] = checkBox;
		}
		checkBoxListFavoriteServers.setListData(tab2);

		// Excluded files from build
		Collection<String> excludedFilesFromBuildList = repositoryService
				.getExcludedFilesPathFromBuild(repositoryName);
		updateExcludedFilesFromBuild(excludedFilesFromBuildList);

		// Excluded extra local folder content from sync
		Collection<String> excludedFoldersFromSyncList = repositoryService
				.getExcludedFoldersFromSync(repositoryName);
		updateExcludedFoldersFromSync(excludedFoldersFromSyncList);
	}

	public void updateExcludedFilesFromBuild(Collection<String> list) {

		String[] paths = new String[list.size()];
		Iterator iter = list.iterator();
		int i = 0;
		while (iter.hasNext()) {
			paths[i] = (String) iter.next();
			i++;
		}

		excludedFilesFromBuildList.clearSelection();
		excludedFilesFromBuildList.setListData(paths);
		int numberLigneShown = list.size();
		excludedFilesFromBuildList.setVisibleRowCount(numberLigneShown);
		excludedFilesFromBuildList.setPreferredSize(excludedFilesFromBuildList
				.getPreferredScrollableViewportSize());
		scrollPane2.updateUI();
	}

	private void updateExcludedFoldersFromSync(Collection<String> list) {

		String[] paths = new String[list.size()];
		Iterator iter = list.iterator();
		int i = 0;
		while (iter.hasNext()) {
			paths[i] = (String) iter.next();
			i++;
		}

		excludedFoldersFromSyncList.clearSelection();
		excludedFoldersFromSyncList.setListData(paths);
		int numberLigneShown = list.size();
		excludedFoldersFromSyncList.setVisibleRowCount(numberLigneShown);
		excludedFoldersFromSyncList
				.setPreferredSize(excludedFoldersFromSyncList
						.getPreferredScrollableViewportSize());
		scrollPane3.updateUI();
	}

	private void buttonAdd2Performed() {

		JFileChooser fc = new JFileChooser(
				repositoryService.getRepositoryPath(repositoryName));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fc.showOpenDialog(BuildOptionsPanel.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file != null) {
				String path = file.getAbsolutePath();
				int size = excludedFilesFromBuildList.getModel().getSize();
				List<String> list = new ArrayList<String>();
				for (int i = 0; i < size; i++) {
					list.add((String) excludedFilesFromBuildList.getModel()
							.getElementAt(i));
				}
				boolean contains = false;
				for (int i = 0; i < list.size(); i++) {
					String osName = System.getProperty("os.name");
					if (osName.contains("Windows")) {
						if (path.equalsIgnoreCase(list.get(i))) {
							contains = true;
						}
					} else {
						if (path.equals(list.get(i))) {
							contains = true;
						}
					}
				}

				if (!contains) {
					list.add(path);
					updateExcludedFilesFromBuild(list);
				}
			}
		}
	}

	private void buttonRemove2Performed() {

		List<String> paths = excludedFilesFromBuildList.getSelectedValuesList();

		if (paths != null) {
			int size = excludedFilesFromBuildList.getModel().getSize();
			Collection<String> list = new ArrayList<String>();
			for (int i = 0; i < size; i++) {
				list.add((String) excludedFilesFromBuildList.getModel()
						.getElementAt(i));
			}
			for (String path : paths) {
				list.remove(path);
			}
			updateExcludedFilesFromBuild(list);
		}
	}

	private void buttonAdd3Performed() {

		JFileChooser fc = new JFileChooser(
				repositoryService.getRepositoryPath(repositoryName));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(BuildOptionsPanel.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file != null) {
				String path = file.getAbsolutePath();
				int size = excludedFoldersFromSyncList.getModel().getSize();
				List<String> list = new ArrayList<String>();
				for (int i = 0; i < size; i++) {
					list.add((String) excludedFoldersFromSyncList.getModel()
							.getElementAt(i));
				}
				boolean contains = false;
				for (int i = 0; i < list.size(); i++) {
					String osName = System.getProperty("os.name");
					if (osName.contains("Windows")) {
						if (path.equalsIgnoreCase(list.get(i))) {
							contains = true;
						}
					} else {
						if (path.equals(list.get(i))) {
							contains = true;
						}
					}
				}

				if (!contains) {
					list.add(path);
					updateExcludedFoldersFromSync(list);
				}
				list.add(path);
			}
		}
	}

	private void buttonRemove3Performed() {

		List<String> paths = excludedFoldersFromSyncList
				.getSelectedValuesList();

		if (paths != null) {
			int size = excludedFoldersFromSyncList.getModel().getSize();
			Collection<String> list = new ArrayList<String>();
			for (int i = 0; i < size; i++) {
				list.add((String) excludedFoldersFromSyncList.getModel()
						.getElementAt(i));
			}
			for (String path : paths) {
				list.remove(path);
			}
			updateExcludedFoldersFromSync(list);
		}
	}

	private void buttonOKPerformed() {

		// Set number of connections
		repositoryService.setNumberOfConnections(repositoryName,
				(int) comboBoxConnections.getSelectedItem());

		// Set favorite servers
		List<FavoriteServerDTO> favoriteServerDTOs = configurationService
				.getFavoriteServers();

		List<FavoriteServerDTO> selectedServerDTOs = new ArrayList<FavoriteServerDTO>();

		List<String> list = checkBoxListFavoriteServers.getSelectedItems();
		for (int i = 0; i < list.size(); i++) {
			String name = list.get(i);
			for (FavoriteServerDTO f : favoriteServerDTOs) {
				if (f.getName().equals(name)) {
					selectedServerDTOs.add(f);
					f.setRepositoryName(repositoryName);
					break;
				}
			}
		}

		repositoryService.setFavoriteServerToAutoconfig(repositoryName,
				selectedServerDTOs);

		// Set excluded files from build
		int size = excludedFilesFromBuildList.getModel().getSize();
		list = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			list.add((String) excludedFilesFromBuildList.getModel()
					.getElementAt(i));
		}
		repositoryService.setExcludedFilesPathFromBuild(repositoryName, list);

		// Set excluded folders from sync
		size = excludedFoldersFromSyncList.getModel().getSize();
		list = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			list.add((String) excludedFoldersFromSyncList.getModel()
					.getElementAt(i));
		}
		repositoryService.setExcludedFoldersFromSync(repositoryName, list);

		try {
			repositoryService.write(repositoryName);
			this.dispose();
		} catch (WritingException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void buttonCancelPerformed() {
		this.dispose();
	}

	private void menuExitPerformed() {
		this.dispose();
	}
}