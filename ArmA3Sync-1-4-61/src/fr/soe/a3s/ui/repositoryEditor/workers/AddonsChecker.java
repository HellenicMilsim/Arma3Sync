package fr.soe.a3s.ui.repositoryEditor.workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import fr.soe.a3s.controller.ObserverFilesNumber3;
import fr.soe.a3s.dto.EventDTO;
import fr.soe.a3s.dto.sync.SyncTreeDirectoryDTO;
import fr.soe.a3s.dto.sync.SyncTreeLeafDTO;
import fr.soe.a3s.dto.sync.SyncTreeNodeDTO;
import fr.soe.a3s.exception.RepositoryException;
import fr.soe.a3s.service.AbstractConnexionService;
import fr.soe.a3s.service.AddonService;
import fr.soe.a3s.service.ConnexionServiceFactory;
import fr.soe.a3s.service.RepositoryService;
import fr.soe.a3s.ui.Facade;
import fr.soe.a3s.ui.repositoryEditor.DownloadPanel;

public class AddonsChecker extends Thread {

	private final Facade facade;
	private final String repositoryName;
	private final String eventName;
	private SyncTreeDirectoryDTO parent;
	private boolean found;
	private final boolean update;
	private final DownloadPanel downloadPanel;
	private boolean cancel = false;

	/* Services */
	private final RepositoryService repositoryService = new RepositoryService();
	private final AddonService addonService = new AddonService();
	private AbstractConnexionService connexionService;

	public AddonsChecker(Facade facade, String repositoryName,
			String eventName, boolean update, DownloadPanel downloadPanel) {
		this.facade = facade;
		this.repositoryName = repositoryName;
		this.eventName = eventName;
		this.update = update;
		this.downloadPanel = downloadPanel;
	}

	@Override
	public void run() {

		addonService.resetAvailableAddonTree();
		facade.getAddonsPanel().updateAvailableAddons();
		facade.getAddonsPanel().updateAddonGroups();
		facade.getAddonsPanel().expandAddonGroups();
		facade.getAddonOptionsPanel().updateAddonPriorities();

		downloadPanel.getButtonCheckForAddonsCancel().setEnabled(false);
		downloadPanel.getLabelCheckForAddonsStatus().setText("Checking files...");
		downloadPanel.getButtonCheckForAddonsStart().setEnabled(false);
		downloadPanel.getComBoxDestinationFolder().setEnabled(false);
		downloadPanel.getButtonDownloadStart().setEnabled(false);
		downloadPanel.getProgressBarCheckForAddons().setMinimum(0);
		downloadPanel.getProgressBarCheckForAddons().setMaximum(100);
		downloadPanel.getProgressBarCheckForAddons().setIndeterminate(true);

		try {
			connexionService = ConnexionServiceFactory
					.getServiceFromRepository(repositoryName);
			connexionService.getSync(repositoryName);
			connexionService.getServerInfo(repositoryName);
			connexionService.getChangelogs(repositoryName);

			repositoryService.getRepositoryCheckerDAO()
					.addObserverFilesNumber3(new ObserverFilesNumber3() {
						@Override
						public synchronized void update(int value) {
							downloadPanel.getProgressBarCheckForAddons().setIndeterminate(false);
							downloadPanel.getProgressBarCheckForAddons()
									.setValue(value);
						}
					});

			parent = repositoryService.getSyncForCheckForAddons(repositoryName);

			downloadPanel.getProgressBarCheckForAddons().setMinimum(0);
			downloadPanel.getProgressBarCheckForAddons().setMaximum(100);
			connexionService.getConnexionDAO().addObserverFilesNumber3(
					new ObserverFilesNumber3() {
						@Override
						public void update(int value) {
							downloadPanel.getProgressBarCheckForAddons().setIndeterminate(false);
							downloadPanel.getProgressBarCheckForAddons()
									.setValue(value);
						}
					});

			downloadPanel.getButtonCheckForAddonsCancel().setEnabled(true);
			
			// slower with http/zsync!
			connexionService.determineCompletion(repositoryName, parent);

			if (!cancel){
				if (eventName != null) {
					setEventAddonSelection();
				} else if (update) {
					selectAllDescending(parent);
				}
				downloadPanel.updateAddons(parent);
				downloadPanel.getRepositoryPanel().getAdminPanel()
						.init(repositoryName);
				downloadPanel.getRepositoryPanel().getEventsPanel()
						.init(repositoryName);
				facade.getSyncPanel().init();
				facade.getAddonsPanel().updateModsetSelection(repositoryName);
				downloadPanel.getLabelCheckForAddonsStatus().setText("Finished!");
			}
		} catch (Exception e) {
			if (!cancel){
				downloadPanel.getProgressBarCheckForAddons().setIndeterminate(false);
				e.printStackTrace();
				String message = "";
				if (e.getMessage() == null || "".equals(e.getMessage())) {
					message = "An unexpected error has occured.";
					String osName = System.getProperty("os.name");
					if (osName.contains("Windows")) {
						message = message + "\n" + "Try to run ArmA3Sync-DEBUG.exe";
					}
				} else {
					message = e.getMessage();
				}
				JOptionPane.showMessageDialog(facade.getMainPanel(), message,
						"Check for Addons", JOptionPane.ERROR_MESSAGE);
				downloadPanel.getLabelCheckForAddonsStatus().setText("Error!");
			}
		} finally {
			downloadPanel.getProgressBarCheckForAddons().setIndeterminate(false);
			downloadPanel.getComBoxDestinationFolder().setEnabled(true);
			downloadPanel.getButtonCheckForAddonsStart().setEnabled(true);
			downloadPanel.getButtonCheckForAddonsCancel().setEnabled(true);
			downloadPanel.getButtonDownloadStart().setEnabled(true);
			downloadPanel.getProgressBarCheckForAddons().setMaximum(0);
			downloadPanel.getArbre().setEnabled(true);
			this.interrupt();
			System.gc();
		}
	}
	
	public void cancel() {
		
		this.cancel = true;
		connexionService.cancel(false);
		downloadPanel.updateAddons(null);
		downloadPanel.getLabelCheckForAddonsStatus().setText("Canceled!");
		this.interrupt();
		System.gc();
	}

	private void setEventAddonSelection() {

		try {
			List<EventDTO> eventDTOs = repositoryService
					.getEvents(this.repositoryName);
			Map<String, Boolean> addonNames = new HashMap<String, Boolean>();
			Map<String, Boolean> userconfigFolderNames = new HashMap<String, Boolean>();
			if (eventDTOs != null) {
				for (EventDTO eventDTO : eventDTOs) {
					if (eventDTO.getName().equals(eventName)) {
						addonNames = eventDTO.getAddonNames();
						userconfigFolderNames = eventDTO
								.getUserconfigFolderNames();
						break;
					}
				}
			}

			SyncTreeDirectoryDTO newRacine = new SyncTreeDirectoryDTO();
			newRacine.setName(parent.getName());
			newRacine.setParent(null);
			if (!userconfigFolderNames.isEmpty()) {
				refineUserconfig(parent, newRacine, userconfigFolderNames);
			}
			if (!addonNames.isEmpty()) {
				refineAddons(parent, newRacine, addonNames);
			}
			parent = newRacine;
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	private void refineAddons(SyncTreeDirectoryDTO oldSyncTreeDirectoryDTO,
			SyncTreeDirectoryDTO newSyncTreeDirectoryDTO,
			Map<String, Boolean> addonNames) {

		for (SyncTreeNodeDTO nodeDTO : oldSyncTreeDirectoryDTO.getList()) {
			if (!nodeDTO.isLeaf()) {
				SyncTreeDirectoryDTO directoryDTO = (SyncTreeDirectoryDTO) nodeDTO;
				if (directoryDTO.isMarkAsAddon()
						&& addonNames.containsKey(nodeDTO.getName())) {
					SyncTreeDirectoryDTO newDirectory = new SyncTreeDirectoryDTO();
					newDirectory.setName(directoryDTO.getName());
					newDirectory.setDestinationPath(directoryDTO
							.getDestinationPath());
					newDirectory.setParent(newSyncTreeDirectoryDTO);
					newDirectory.setMarkAsAddon(true);
					boolean optional = addonNames.get(nodeDTO.getName());
					if (optional) {
						newDirectory.setOptional(true);
						newDirectory.setSelected(false);
					} else {
						newDirectory.setOptional(false);
						newDirectory.setSelected(true);
						selectAllAscending(newDirectory);
					}
					newSyncTreeDirectoryDTO.setHidden(directoryDTO.isHidden());
					newSyncTreeDirectoryDTO.addTreeNode(newDirectory);
					fill(directoryDTO, newDirectory);
				} else if (!directoryDTO.isMarkAsAddon()) {
					found = false;
					seek(directoryDTO, addonNames);
					if (found) {
						SyncTreeDirectoryDTO newDirectory = new SyncTreeDirectoryDTO();
						newDirectory.setName(directoryDTO.getName());
						newSyncTreeDirectoryDTO.addTreeNode(newDirectory);
						newDirectory.setParent(newSyncTreeDirectoryDTO);
						refineAddons(directoryDTO, newDirectory, addonNames);
					}
				}
			}
		}
	}

	private void fill(SyncTreeDirectoryDTO directoryDTO,
			SyncTreeDirectoryDTO newDirectoryDTO) {

		for (SyncTreeNodeDTO nodeDTO : directoryDTO.getList()) {
			if (nodeDTO.isLeaf()) {
				SyncTreeLeafDTO leafDTO = (SyncTreeLeafDTO) nodeDTO;
				SyncTreeLeafDTO newLeafDTO = new SyncTreeLeafDTO();
				newLeafDTO.setName(leafDTO.getName());
				newLeafDTO.setParent(newDirectoryDTO);
				newLeafDTO.setDeleted(leafDTO.isDeleted());
				newLeafDTO.setUpdated(leafDTO.isUpdated());
				newLeafDTO.setSelected(newDirectoryDTO.isSelected());
				newLeafDTO.setSize(leafDTO.getSize());
				newLeafDTO.setDestinationPath(leafDTO.getDestinationPath());
				newDirectoryDTO.addTreeNode(newLeafDTO);
				if (newLeafDTO.isUpdated() || newLeafDTO.isDeleted()) {
					SyncTreeDirectoryDTO parent = newLeafDTO.getParent();
					while (parent != null) {
						parent.setChanged(true);
						parent = parent.getParent();
					}
				}
			} else {
				SyncTreeDirectoryDTO dDTO = (SyncTreeDirectoryDTO) nodeDTO;
				SyncTreeDirectoryDTO newdDTO = new SyncTreeDirectoryDTO();
				newdDTO.setName(dDTO.getName());
				newdDTO.setParent(newDirectoryDTO);
				newdDTO.setUpdated(dDTO.isUpdated());
				newdDTO.setDeleted(dDTO.isDeleted());
				newdDTO.setChanged(dDTO.isChanged());
				newdDTO.setSelected(newDirectoryDTO.isSelected());
				newdDTO.setDestinationPath(dDTO.getDestinationPath());
				newdDTO.setMarkAsAddon(dDTO.isMarkAsAddon());
				newdDTO.setHidden(dDTO.isHidden());
				newDirectoryDTO.addTreeNode(newdDTO);
				if (newdDTO.isUpdated() || newdDTO.isDeleted()
						|| newdDTO.isChanged()) {
					SyncTreeDirectoryDTO parent = newdDTO.getParent();
					while (parent != null) {
						parent.setChanged(true);
						parent = parent.getParent();
					}
				}
				fill(dDTO, newdDTO);
			}
		}
	}

	private void seek(SyncTreeDirectoryDTO seakDirectory,
			Map<String, Boolean> addonNames) {

		for (SyncTreeNodeDTO nodeDTO : seakDirectory.getList()) {
			if (!nodeDTO.isLeaf()) {
				SyncTreeDirectoryDTO directoryDTO = (SyncTreeDirectoryDTO) nodeDTO;
				if (directoryDTO.isMarkAsAddon()
						&& addonNames.containsKey(nodeDTO.getName())) {
					found = true;
				} else {
					seek(directoryDTO, addonNames);
				}
			}
		}
	}

	private void refineUserconfig(SyncTreeDirectoryDTO oldSyncTreeDirectoryDTO,
			SyncTreeDirectoryDTO newSyncTreeDirectoryDTO,
			Map<String, Boolean> userconfigFolderNames) {

		for (SyncTreeNodeDTO nodeDTO : oldSyncTreeDirectoryDTO.getList()) {
			if (!nodeDTO.isLeaf()
					&& nodeDTO.getName().toLowerCase().equals("userconfig")) {
				SyncTreeDirectoryDTO userconfig = (SyncTreeDirectoryDTO) nodeDTO;
				SyncTreeDirectoryDTO newUserconfig = new SyncTreeDirectoryDTO();
				newUserconfig.setName(userconfig.getName());
				newUserconfig.setDestinationPath(userconfig
						.getDestinationPath());
				newUserconfig.setParent(newSyncTreeDirectoryDTO);
				newUserconfig.setHidden(userconfig.isHidden());
				newSyncTreeDirectoryDTO.addTreeNode(newUserconfig);

				for (SyncTreeNodeDTO d : userconfig.getList()) {
					if (userconfigFolderNames.containsKey(d.getName())) {
						if (!d.isLeaf()) {
							SyncTreeDirectoryDTO folder = new SyncTreeDirectoryDTO();
							folder.setName(d.getName());
							folder.setDestinationPath(d.getDestinationPath());
							folder.setParent(newUserconfig);
							boolean optional = userconfigFolderNames.get(d
									.getName());
							if (optional) {
								folder.setOptional(true);
								folder.setSelected(false);
							} else {
								folder.setOptional(false);
								folder.setSelected(true);
								selectAllAscending(newUserconfig);
							}
							newUserconfig.addTreeNode(folder);
							folder.setHidden(((SyncTreeDirectoryDTO) d)
									.isHidden());
							folder.setUpdated(d.isUpdated());
							folder.setDeleted(d.isDeleted());
							folder.setChanged(((SyncTreeDirectoryDTO) d)
									.isChanged());
							if (folder.isUpdated() || folder.isDeleted()
									|| folder.isChanged()) {
								newUserconfig.setChanged(true);
							}
							fill((SyncTreeDirectoryDTO) d, folder);
						} else {
							SyncTreeLeafDTO leaf = new SyncTreeLeafDTO();
							leaf.setName(d.getName());
							leaf.setDestinationPath(d.getDestinationPath());
							leaf.setParent(newSyncTreeDirectoryDTO);
							leaf.setDeleted(d.isDeleted());
							leaf.setUpdated(d.isUpdated());
							leaf.setSelected(d.isSelected());
							leaf.setSize(((SyncTreeLeafDTO) d).getSize());
							boolean optional = userconfigFolderNames.get(d
									.getName());
							if (optional) {
								leaf.setOptional(true);
								leaf.setSelected(false);
							} else {
								leaf.setOptional(false);
								leaf.setSelected(true);
								selectAllAscending(newUserconfig);
							}
							newUserconfig.addTreeNode(leaf);
							if (leaf.isUpdated() || leaf.isDeleted()) {
								newUserconfig.setChanged(true);
							}
						}
					}
				}
			}
		}
	}

	private void selectAllAscending(SyncTreeNodeDTO syncTreeNodeDTO) {
		if (syncTreeNodeDTO != null) {
			syncTreeNodeDTO.setSelected(true);
			SyncTreeNodeDTO parent = syncTreeNodeDTO.getParent();
			selectAllAscending(parent);
		}
	}

	private void selectAllDescending(SyncTreeNodeDTO syncTreeNodeDTO) {
		syncTreeNodeDTO.setSelected(true);
		if (!syncTreeNodeDTO.isLeaf()) {
			SyncTreeDirectoryDTO syncTreeDirectoryDTO = (SyncTreeDirectoryDTO) syncTreeNodeDTO;
			for (SyncTreeNodeDTO t : syncTreeDirectoryDTO.getList()) {
				selectAllDescending(t);
			}
		}
	}
}