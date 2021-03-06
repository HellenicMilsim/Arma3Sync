package fr.soe.a3s.ui.repository.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;

import fr.soe.a3s.dto.sync.SyncTreeDirectoryDTO;
import fr.soe.a3s.dto.sync.SyncTreeNodeDTO;
import fr.soe.a3s.service.ProfileService;
import fr.soe.a3s.service.RepositoryService;
import fr.soe.a3s.ui.Facade;
import fr.soe.a3s.ui.UIConstants;
import fr.soe.a3s.ui.repository.DownloadPanel;
import fr.soe.a3s.ui.repository.outline.AddonSyncRowModel;
import fr.soe.a3s.ui.repository.outline.RenderData;
import fr.soe.a3s.ui.repository.tree.AddonSyncTreeModel;

@Deprecated
public class AdvancedConfigurationPanel extends JFrame implements UIConstants {

	private final Facade facade;
	private final DownloadPanel downloadPanel;
	private JButton buttonClose;
	private JScrollPane scrollPane;
	private AddonSyncTreeModel treeModel;
	private final SyncTreeDirectoryDTO racine;
	private AddonSyncRowModel rowModel;
	private OutlineModel outlineModel;
	private Outline outline;
	private final String defaultDestinationPath;// may be null
	private final String repositoryName;
	// Services
	private final RepositoryService repositoryService = new RepositoryService();
	private final ProfileService profileService = new ProfileService();

	public AdvancedConfigurationPanel(Facade facade,
			SyncTreeDirectoryDTO racine, String defaultDestinationPath,
			String repositoryName, DownloadPanel downloadPanel) {

		this.facade = facade;
		this.downloadPanel = downloadPanel;
		this.racine = racine;
		this.defaultDestinationPath = defaultDestinationPath;
		this.repositoryName = repositoryName;
		this.setTitle("Advanced configuration");
		this.setResizable(true);
		this.setMinimumSize(new Dimension(400, 400));
		setIconImage(ICON);
		this.setLocation(
				(int) facade.getMainPanel().getLocation().getX()
						+ facade.getMainPanel().getWidth() / 2
						- this.getWidth() / 2,
				(int) facade.getMainPanel().getLocation().getY()
						+ facade.getMainPanel().getHeight() / 2
						- this.getHeight() / 2);
		this.setLayout(new BorderLayout());

		{
			JPanel topPanel = new JPanel();
			JLabel label = new JLabel("   Set files destination");
			FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
			topPanel.setLayout(flowLayout);
			topPanel.add(label);
			this.add(topPanel, BorderLayout.NORTH);
		}
		{
			JPanel controlPanel = new JPanel();
			buttonClose = new JButton("Close");
			FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
			controlPanel.setLayout(flowLayout);
			controlPanel.add(buttonClose);
			this.add(controlPanel, BorderLayout.SOUTH);
		}
		{
			treeModel = new AddonSyncTreeModel(this.racine);
			rowModel = new AddonSyncRowModel();
			outlineModel = DefaultOutlineModel.createOutlineModel(treeModel,
					rowModel, false, "File");
			outline = new Outline();
			outline.setModel(outlineModel);
			outline.setRootVisible(false);
			outline.setRenderDataProvider(new RenderData());
			outline.setShowGrid(false);
			outline.setFillsViewportHeight(true);
			outline.setCellSelectionEnabled(false);
			outline.setRowSelectionAllowed(true);
			outline.setAutoCreateRowSorter(false);
			outline.getTableHeader().setReorderingAllowed(false);

			scrollPane = new JScrollPane();
			scrollPane.setViewportView(outline);
			scrollPane.setColumnHeader(null);
			scrollPane.setBorder(BorderFactory
					.createEtchedBorder(BevelBorder.LOWERED));
			this.add(scrollPane, BorderLayout.CENTER);
		}
		{
			JPanel sidePanel1 = new JPanel();
			this.add(sidePanel1, BorderLayout.WEST);
			JPanel sidePanel2 = new JPanel();
			this.add(sidePanel2, BorderLayout.EAST);
		}

		getRootPane().setDefaultButton(buttonClose);

		TableColumn col2 = outline.getColumnModel().getColumn(2);
		TableCellRenderer tableCellRenderer = new ButtonRenderer();
		col2.setCellRenderer(tableCellRenderer);
		col2.setCellEditor(new ButtonEditor(new JCheckBox()));
		col2.setMinWidth(120);
		col2.setMaxWidth(120);

		TableColumn col1 = outline.getColumnModel().getColumn(1);
		col1.setMinWidth(100);
		col1.setPreferredWidth(250);

		TableColumn col0 = outline.getColumnModel().getColumn(0);
		col0.setMinWidth(100);

		// JTableHeader header = outline.getTableHeader();
		// header.setDefaultRenderer(new HeaderRenderer(outline));

		buttonClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				buttonClosePerformed();
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

	private void buttonClosePerformed() {
		this.dispose();
		this.downloadPanel.getButtonSettings().setEnabled(true);
	}

	private void menuExitPerformed() {
		this.dispose();
		this.downloadPanel.getButtonSettings().setEnabled(true);
	}

	class ButtonRenderer extends JButton implements TableCellRenderer {

		public ButtonRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(UIManager.getColor("Button.background"));
			}
			setText((value == null) ? "" : value.toString());
			if (value == null) {
				return null;
			} else {
				return this;
			}
		}
	}

	class MyTableCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int col) {

			Component c = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, col);
			return c;
		}
	}

	class ButtonEditor extends DefaultCellEditor {

		protected JButton button;
		private String label;
		private boolean isPushed;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			button = new JButton("");
			button.setOpaque(true);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			if (isSelected) {
				button.setForeground(table.getSelectionForeground());
				button.setBackground(table.getSelectionBackground());
			} else {
				button.setForeground(table.getForeground());
				button.setBackground(table.getBackground());
			}
			label = (value == null) ? "" : value.toString();
			button.setText(label);
			isPushed = true;
			if (value == null) {
				return null;
			} else {
				return button;
			}
		}

		@Override
		public Object getCellEditorValue() {
			if (isPushed) {
				// JOptionPane.showMessageDialog(button, label + ": Ouch!");
				int index = outline.getSelectedRow();
				if (index == -1 || index > outline.getRowCount()) {
					return null;
				}

				SyncTreeNodeDTO node = (SyncTreeNodeDTO) outlineModel
						.getValueAt(index, -1);

				String currentFolderPath = node.getDestinationPath() + "/"
						+ node.getName();

				if (new File(currentFolderPath).exists()) {
					JOptionPane.showMessageDialog(
							AdvancedConfigurationPanel.this,
							"Destination folder can't be changed. \n"
									+ "Folder " + node.getName()
									+ " already exists.", "Download",
							JOptionPane.WARNING_MESSAGE);
					return null;
				}

				JFileChooser fc = new JFileChooser(defaultDestinationPath);
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(facade.getMainPanel());

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String newPath = file.getAbsolutePath().toLowerCase();
					node.setDestinationPath(newPath);
					setDestinationPath(node);
					outlineModel.setValueAt(newPath, index, 1);
					scrollPane.updateUI();
					List<String> set = profileService
							.getAddonSearchDirectoryPaths();
					Iterator iter = set.iterator();
					List<String> list = new ArrayList<String>();
					while (iter.hasNext()) {
						list.add((String) iter.next());
					}
					boolean contains = false;
					for (int i = 0; i < list.size(); i++) {
						String osName = System.getProperty("os.name");
						if (osName.contains("Windows")) {
							if (newPath.equalsIgnoreCase(list.get(i))) {
								contains = true;
							}
						} else {
							if (newPath.equals(list.get(i))) {
								contains = true;
							}
						}
					}

					if (!contains) {
						// profileService.addAddonSearchDirectoryPath(newPath);
						// facade.getAddonOptionsPanel()
						// .init();
					}
				}
			}
			isPushed = false;
			return label;
		}

		private void setDestinationPath(SyncTreeNodeDTO node) {

			if (!node.isLeaf()) {
				SyncTreeDirectoryDTO syncTreeDirectoryDTO = (SyncTreeDirectoryDTO) node;
				for (SyncTreeNodeDTO n : syncTreeDirectoryDTO.getList()) {
					n.setDestinationPath(node.getDestinationPath() + "/"
							+ node.getName());
					setDestinationPath(n);
				}
			}
		}

		@Override
		public boolean stopCellEditing() {
			isPushed = false;
			return super.stopCellEditing();
		}

		@Override
		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}
	}

	class HeaderRenderer implements TableCellRenderer {

		DefaultTableCellRenderer renderer;

		public HeaderRenderer(JTable table) {
			renderer = (DefaultTableCellRenderer) table.getTableHeader()
					.getDefaultRenderer();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int col) {

			if (col == 2) {
				renderer.setHorizontalAlignment(JLabel.CENTER);
			} else {
				renderer.setHorizontalAlignment(JLabel.LEFT);
			}
			return renderer.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, col);
		}
	}
}
