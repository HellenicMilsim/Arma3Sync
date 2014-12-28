package fr.soe.a3s.service;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import fr.soe.a3s.constant.Protocole;
import fr.soe.a3s.dao.AddonDAO;
import fr.soe.a3s.dao.DataAccessConstants;
import fr.soe.a3s.dao.RepositoryBuilderDAO;
import fr.soe.a3s.dao.RepositoryDAO;
import fr.soe.a3s.domain.AbstractProtocole;
import fr.soe.a3s.domain.Addon;
import fr.soe.a3s.domain.Ftp;
import fr.soe.a3s.domain.Http;
import fr.soe.a3s.domain.TreeDirectory;
import fr.soe.a3s.domain.TreeLeaf;
import fr.soe.a3s.domain.TreeNode;
import fr.soe.a3s.domain.configration.FavoriteServer;
import fr.soe.a3s.domain.repository.Changelog;
import fr.soe.a3s.domain.repository.Changelogs;
import fr.soe.a3s.domain.repository.Event;
import fr.soe.a3s.domain.repository.Events;
import fr.soe.a3s.domain.repository.Repository;
import fr.soe.a3s.domain.repository.ServerInfo;
import fr.soe.a3s.domain.repository.SyncTreeDirectory;
import fr.soe.a3s.domain.repository.SyncTreeLeaf;
import fr.soe.a3s.domain.repository.SyncTreeNode;
import fr.soe.a3s.dto.ChangelogDTO;
import fr.soe.a3s.dto.EventDTO;
import fr.soe.a3s.dto.RepositoryDTO;
import fr.soe.a3s.dto.ServerInfoDTO;
import fr.soe.a3s.dto.TreeDirectoryDTO;
import fr.soe.a3s.dto.configuration.FavoriteServerDTO;
import fr.soe.a3s.dto.sync.SyncTreeDirectoryDTO;
import fr.soe.a3s.exception.CheckException;
import fr.soe.a3s.exception.LoadingException;
import fr.soe.a3s.exception.RepositoryCheckException;
import fr.soe.a3s.exception.RepositoryException;
import fr.soe.a3s.exception.ServerInfoNotFoundException;
import fr.soe.a3s.exception.SyncFileNotFoundException;
import fr.soe.a3s.exception.WritingException;

public class RepositoryService extends ObjectDTOtransformer implements
		DataAccessConstants {

	private static final RepositoryDAO repositoryDAO = new RepositoryDAO();
	private final RepositoryBuilderDAO repositoryBuilderDAO = new RepositoryBuilderDAO();
	private static final AddonDAO addonDAO = new AddonDAO();

	private static final byte[] secreteKey = new byte[] { 0x01, 0x72, 0x43,
			0x3E, 0x1C, 0x7A, 0x55, 0, 0x01, 0x72, 0x43, 0x3E, 0x1C, 0x7A,
			0x55, 0x4F };

	public void readAll() throws LoadingException {

		try {
			Cipher cipher = getDecryptionCipher();
			repositoryDAO.readAll(cipher);
		} catch (Exception e) {
			e.printStackTrace();
			throw new LoadingException();
		}
	}

	public void writeAll() throws WritingException {

		for (Iterator<String> iter = repositoryDAO.getMap().keySet().iterator(); iter
				.hasNext();) {
			String repositoryName = iter.next();
			write(repositoryName);
		}
	}

	public void write(String repositoryName) throws WritingException {

		try {
			Cipher cipher = getEncryptionCipher();
			repositoryDAO.write(cipher, repositoryName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new WritingException("Failed to write repository.");
		}
	}

	public void createRepository(String name, String url, String port,
			String login, String password, Protocole protocole)
			throws CheckException {

		if (name == null || "".equals(name)) {
			throw new CheckException("Repository name can't be empty.");
		}

		if (repositoryDAO.getMap().containsKey(name)) {
			throw new CheckException("Repository with name " + name
					+ " already exists.");
		}

		AbstractProtocole abstractProtocole = null;
		if (protocole.equals(Protocole.FTP)) {
			abstractProtocole = new Ftp(url, port, login, password);
		} else if (protocole.equals(Protocole.HTTP)) {
			abstractProtocole = new Http(url, port, login, password);
		} else {
			throw new CheckException("Protocole not supported yet.");
		}
		abstractProtocole.checkData();

		Repository repository = new Repository(name, abstractProtocole);
		repositoryDAO.getMap().put(repository.getName(), repository);
	}

	public void removeRepository(String repositoryName) {
		repositoryDAO.remove(repositoryName);
		repositoryDAO.getMap().remove(repositoryName);
	}

	public List<RepositoryDTO> getRepositories() {

		List<RepositoryDTO> repositoryDTOs = new ArrayList<RepositoryDTO>();
		for (Iterator<String> i = repositoryDAO.getMap().keySet().iterator(); i
				.hasNext();) {
			Repository repository = repositoryDAO.getMap().get(i.next());
			RepositoryDTO repositoryDTO = transformRepository2DTO(repository);
			repositoryDTOs.add(repositoryDTO);
		}
		return repositoryDTOs;
	}

	public RepositoryDTO getRepository(String repositoryName)
			throws RepositoryException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			RepositoryDTO repositoryDTO = transformRepository2DTO(repository);
			return repositoryDTO;
		} else {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}
	}

	public void setRepositoryPath(String repositoryName, String repositoryPath)
			throws RepositoryException {
		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.setPath(repositoryPath);
		} else {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}
	}

	public String getRepositoryPath(String repositoryName) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			return repository.getPath();
		} else {
			return null;
		}
	}

	public void setAutoConfigURL(String repositoryName, String autoConfigURL)
			throws RepositoryException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.setAutoConfigURL(autoConfigURL);
		} else {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}

	}

	public ServerInfoDTO getServerInfo(String repositoryName)
			throws RepositoryException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			ServerInfo serverInfo = repository.getServerInfo();
			if (serverInfo != null) {
				ServerInfoDTO serverInfoDTO = transformServerInfo2DTO(serverInfo);
				return serverInfoDTO;
			} else {
				return null;
			}
		} else {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}
	}

	public List<ChangelogDTO> getChangelogs(String repositoryName)
			throws RepositoryException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			Changelogs changelogs = repository.getChangelogs();
			if (changelogs != null) {
				List<Changelog> list = changelogs.getList();
				List<ChangelogDTO> changelogDTOs = new ArrayList<ChangelogDTO>();
				for (Changelog changelog : list) {
					ChangelogDTO changelogDTO = transformChangelog2DTO(changelog);
					changelogDTOs.add(changelogDTO);
				}
				return changelogDTOs;
			} else {
				return null;
			}
		} else {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}
	}

	public void buildRepository(String repositoryName)
			throws RepositoryException, WritingException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository == null) {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}
		repositoryBuilderDAO.buildRepository(repository);
	}

	public void buildRepository(String repositoryName, String path)
			throws RepositoryException, WritingException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository == null) {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}
		repository.setPath(path);
		repositoryBuilderDAO.buildRepository(repository);
	}

	public SyncTreeDirectoryDTO getSync(String repositoryName) throws Exception {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository == null) {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}

		if (repository.getServerInfo() == null) {
			throw new ServerInfoNotFoundException();
		}

		if (repository.getSync() == null) {
			throw new SyncFileNotFoundException();
		}

		boolean noAutoDiscover = repository.isNoAutoDiscover();
		Set<String> hiddenFolderPaths = repository.getHiddenFolderPath();

		SyncTreeDirectory parent = repository.getSync();

		determineDestinationPaths(parent,
				repository.getDefaultDownloadLocation(), noAutoDiscover);
		determineHiddenFiles(parent, hiddenFolderPaths);

		// determineAddonFilesToDelete(parent, hiddenFolderPaths,
		// repository.getDefaultDownloadLocation(), noAutoDiscover);
		// determineAddonFoldersToDelete(parent, hiddenFolderPaths);

		determineFilesToDelete(parent);

		repositoryBuilderDAO.determineLocalSHA1(parent, repository);
		try {
			Cipher cipher = getEncryptionCipher();
			repositoryDAO.write(cipher, repositoryName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SyncTreeDirectoryDTO parentDTO = new SyncTreeDirectoryDTO();
		parentDTO.setName("racine");
		parentDTO.setParent(null);
		transformSyncTreeDirectory2DTO(parent, parentDTO);
		return parentDTO;
	}

	private void determineDestinationPaths(SyncTreeNode syncTreeNode,
			String defaultDestinationPath, boolean noAutoDiscover) {

		if (!syncTreeNode.isLeaf()) {
			SyncTreeDirectory directory = (SyncTreeDirectory) syncTreeNode;
			SyncTreeDirectory parent = directory.getParent();
			if (parent == null) {
				directory.setDestinationPath(null);
			} else {
				String path = directory.getParent().getDestinationPath();
				if (path != null) {
					directory.setDestinationPath(new File(path + "/"
							+ directory.getParent().getName())
							.getAbsolutePath());
				} else {
					directory.setDestinationPath(defaultDestinationPath);
				}
				if (!noAutoDiscover
						&& directory.isMarkAsAddon()
						&& addonDAO.getMap().containsKey(
								directory.getName().toLowerCase())) {
					Addon addon = addonDAO.getMap().get(
							directory.getName().toLowerCase());
					String newPath = addon.getPath();
					directory.setDestinationPath(newPath);
				}
			}
			for (SyncTreeNode n : directory.getList()) {
				determineDestinationPaths(n, defaultDestinationPath,
						noAutoDiscover);
			}
		} else {
			SyncTreeLeaf leaf = (SyncTreeLeaf) syncTreeNode;
			String path = leaf.getParent().getDestinationPath();
			if (path == null) {
				leaf.setDestinationPath(defaultDestinationPath);
			} else {
				leaf.setDestinationPath(new File(path + "/"
						+ leaf.getParent().getName()).getAbsolutePath());
			}
		}
	}

	private void determineHiddenFiles(SyncTreeNode node,
			Set<String> hiddenFolderPaths) {

		if (!node.isLeaf()) {
			SyncTreeDirectory directory = (SyncTreeDirectory) node;
			SyncTreeNode parent = directory.getParent();
			if (parent == null) {
				for (SyncTreeNode n : directory.getList()) {
					determineHiddenFiles(n, hiddenFolderPaths);
				}
			}

			File file = new File(directory.getDestinationPath() + "/"
					+ directory.getName());
			boolean contains = false;
			for (String stg : hiddenFolderPaths) {
				if (file.getAbsolutePath().toLowerCase()
						.contains(new File(stg.toLowerCase()).getPath())) {
					contains = true;
					break;
				}
			}
			if (contains) {
				directory.setHidden(true);
			} else {
				directory.setHidden(false);
			}
			for (SyncTreeNode n : directory.getList()) {
				determineHiddenFiles(n, hiddenFolderPaths);
			}
		}
	}

	private void determineFilesToDelete(SyncTreeNode node) {

		if (!node.isLeaf()) {
			SyncTreeDirectory directory = (SyncTreeDirectory) node;
			SyncTreeNode parent = directory.getParent();
			if (parent == null) {
				for (SyncTreeNode n : directory.getList()) {
					determineFilesToDelete(n);
				}
			} else if (!directory.isHidden()) {
				File file = new File(directory.getDestinationPath() + "/"
						+ directory.getName());
				// folder must exists locally and remotely
				File[] subFiles = file.listFiles();
				if (subFiles != null) {
					List<String> listNames = new ArrayList<String>();
					for (SyncTreeNode n : directory.getList()) {
						listNames.add(n.getName().toLowerCase());
					}
					for (File f : subFiles) {
						if (!listNames.contains(f.getName().toLowerCase())) {
							if (f.isDirectory()) {
								SyncTreeDirectory d = new SyncTreeDirectory(
										f.getName(), directory);
								directory.addTreeNode(d);
								d.setDeleted(true);
								d.setDestinationPath(directory
										.getDestinationPath()
										+ "/"
										+ directory.getName());
							} else if (!f.getName().contains(PART_EXTENSION)) {
								SyncTreeLeaf l = new SyncTreeLeaf(f.getName(),
										directory);
								directory.addTreeNode(l);
								l.setDeleted(true);
								l.setDestinationPath(directory
										.getDestinationPath()
										+ "/"
										+ directory.getName());
							}
						}
					}
				}
				for (SyncTreeNode n : directory.getList()) {
					determineFilesToDelete(n);
				}
			}
		}
	}

	private void determineAddonFilesToDelete(SyncTreeNode syncTreeNode,
			Set<String> hidedFolderPaths, String defaultDownloadLocation,
			boolean noAutoDiscover) {

		if (!syncTreeNode.isLeaf()) {
			SyncTreeDirectory directory = (SyncTreeDirectory) syncTreeNode;

			if (directory.isMarkAsAddon()
					&& addonDAO.getMap().containsKey(
							directory.getName().toLowerCase())) {

				Addon addon = addonDAO.getMap().get(
						directory.getName().toLowerCase());

				if (defaultDownloadLocation.equals(addon.getPath())
						|| !noAutoDiscover) {
					File file = new File(addon.getPath() + "/"
							+ addon.getName());
					if (!directory.isHidden()) {
						boolean contains = false;
						for (String stg : hidedFolderPaths) {
							if (file.getAbsolutePath().toLowerCase()
									.contains(stg.toLowerCase())) {
								contains = true;
								break;
							}
						}
						if (!contains) {
							addFilesToDelete(directory, file);
						} else {
							directory.setHidden(true);
						}
					}
				} else {
					for (SyncTreeNode n : directory.getList()) {
						determineAddonFilesToDelete(n, hidedFolderPaths,
								defaultDownloadLocation, noAutoDiscover);
					}
				}
			} else {
				for (SyncTreeNode n : directory.getList()) {
					determineAddonFilesToDelete(n, hidedFolderPaths,
							defaultDownloadLocation, noAutoDiscover);
				}
			}
		}
	}

	private void addFilesToDelete(SyncTreeDirectory directory, File file) {

		File[] subFiles = file.listFiles();

		if (subFiles == null) {
			return;
		}

		List<SyncTreeNode> nodes = directory.getList();
		List<String> listNames = new ArrayList<String>();
		for (SyncTreeNode node : nodes) {
			listNames.add(node.getName().toLowerCase());
		}
		for (File f : subFiles) {
			if (!listNames.contains(f.getName().toLowerCase())
					&& !f.getName().contains(PART_EXTENSION)) {
				if (f.isDirectory()) {
					SyncTreeDirectory d = new SyncTreeDirectory(f.getName(),
							directory);
					directory.addTreeNode(d);
					d.setDeleted(true);
					d.setDestinationPath(directory.getDestinationPath() + "/"
							+ directory.getName());
				} else {
					SyncTreeLeaf l = new SyncTreeLeaf(f.getName(), directory);
					directory.addTreeNode(l);
					l.setDeleted(true);
					l.setDestinationPath(directory.getDestinationPath() + "/"
							+ directory.getName());
				}
			}
		}

		for (File f : subFiles) {
			if (f.isDirectory()) {
				for (SyncTreeNode node : nodes) {
					if (node.getName().equals(f.getName()) && !node.isLeaf()) {
						SyncTreeDirectory d = (SyncTreeDirectory) node;
						addFilesToDelete(d, f);
						break;
					}
				}
			}
		}
	}

	private void determineAddonFoldersToDelete(SyncTreeNode syncTreeNode,
			Set<String> hidedFolderPaths) {

		if (!syncTreeNode.isLeaf()) {
			SyncTreeDirectory directory = (SyncTreeDirectory) syncTreeNode;
			if (!directory.isMarkAsAddon() && directory.getParent() != null) {
				File file = new File(directory.getDestinationPath() + "/"
						+ directory.getName());
				// folder must exists locally and remotely
				File[] subFiles = file.listFiles();
				if (subFiles != null) {
					List<String> listNames = new ArrayList<String>();
					for (SyncTreeNode node : directory.getList()) {
						listNames.add(node.getName().toLowerCase());
					}
					for (File f : subFiles) {
						if (!listNames.contains(f.getName().toLowerCase())) {
							if (!directory.isHidden()) {
								boolean contains = false;
								for (String stg : hidedFolderPaths) {
									if (file.getAbsolutePath().toLowerCase()
											.contains(stg.toLowerCase())) {
										contains = true;
										break;
									}
								}
								if (!contains) {
									addFilesToDelete(directory, file);
								} else {
									directory.setHidden(true);
								}
							}
						}
					}
				}
			}
			for (SyncTreeNode n : directory.getList()) {
				determineAddonFoldersToDelete(n, hidedFolderPaths);
			}
		}
	}

	public void addFilesToHide(String folderPath, String repositoryName) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.getHiddenFolderPath().add(folderPath);
		}
	}

	public void removeFilesToHide(String folderPath, String repositoryName) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.getHiddenFolderPath().remove(folderPath);
		}
	}

	private void determineDestinationPathsForAddonFiles(
			SyncTreeNode syncTreeNode) {

		if (!syncTreeNode.isLeaf()) {
			SyncTreeDirectory directory = (SyncTreeDirectory) syncTreeNode;
			String path = directory.getParent().getDestinationPath();
			directory.setDestinationPath(new File(path + "/"
					+ directory.getParent().getName()).getAbsolutePath());
		} else {
			SyncTreeLeaf leaf = (SyncTreeLeaf) syncTreeNode;
			String path = leaf.getParent().getDestinationPath();
			leaf.setDestinationPath(new File(path + "/"
					+ leaf.getParent().getName()).getAbsolutePath());
		}
	}

	public void checkRepository(String repositoryName, String path)
			throws RepositoryException, ServerInfoNotFoundException,
			SyncFileNotFoundException, RepositoryCheckException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository == null) {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}

		try {
			ServerInfo serverInfo = repositoryDAO
					.readServerInfo(repositoryName);
			repository.setServerInfo(serverInfo);
		} catch (Exception e) {
			throw new ServerInfoNotFoundException();
		}

		try {
			SyncTreeDirectory sync = repositoryDAO.readSync(repositoryName);
			repository.setSync(sync);
		} catch (Exception e) {
			throw new SyncFileNotFoundException();
		}

		if (!repository.getPath().equals(path) || path.isEmpty()) {
			throw new RepositoryException("Repository path does not match "
					+ path + "!");
		}
		repositoryBuilderDAO.checkRepository(repository);
	}

	public String getDefaultDownloadLocation(String repositoryName) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository == null) {
			return null;
		} else {
			return repository.getDefaultDownloadLocation();
		}
	}

	public void setDefaultDownloadLocation(String repositoryName,
			String defaultDownloadLocation) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.setDefaultDownloadLocation(defaultDownloadLocation);
			try {
				write(repositoryName);
			} catch (WritingException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateRepositoryRevision(String repositoryName) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			ServerInfo serverInfo = repository.getServerInfo();
			if (serverInfo != null) {
				repository.setRevision(serverInfo.getRevision());
			}
			try {
				write(repositoryName);
			} catch (WritingException e) {
			}
		}
	}

	public void setRepositoryNotification(String repositoryName, boolean notify) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.setNotify(notify);
			try {
				write(repositoryName);
			} catch (WritingException e) {
			}
		}
	}

	public void setOutOfSync(String repositoryName, boolean value) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.setOutOfSynk(value);
		}
	}

	public void setDownloading(String repositoryName, boolean value) {
		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.setDownloading(value);
		}
	}

	public boolean isDownloading(String repositoryName) {
		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			return repository.isDownloading();
		} else {
			return false;
		}
	}

	public boolean isDownloading() {
		boolean response = false;
		for (Iterator<String> i = repositoryDAO.getMap().keySet().iterator(); i
				.hasNext();) {
			Repository repository = repositoryDAO.getMap().get(i.next());
			if (repository.isDownloading()) {
				response = true;
				break;
			}
		}
		return response;
	}

	public void saveDownloadParameters(String repositoryName,
			long incrementedFilesSize, int lastIndexFileDownloaded,
			boolean resume) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.setIncrementedFilesSize(incrementedFilesSize);
			repository.setLastIndexFileDownloaded(lastIndexFileDownloaded);
			repository.setResume(resume);
		}
	}

	public int getLastIndexFileDownloaded(String repositoryName) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			return repository.getLastIndexFileDownloaded();
		} else {
			return 0;
		}
	}

	public long getIncrementedFilesSize(String repositoryName) {
		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			return repository.getIncrementedFilesSize();
		} else {
			return 0;
		}
	}

	public boolean isResume(String repositoryName) {
		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			return repository.isResume();
		} else {
			return false;
		}
	}

	public boolean isAutoDiscover(String repositoryName) {
		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			return repository.isNoAutoDiscover();
		} else {
			return false;
		}
	}

	public void setAutoDiscover(boolean value, String repositoryName) {
		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.setNoAutoDiscover(value);
			try {
				write(repositoryName);
			} catch (WritingException e) {
				e.printStackTrace();
			}
		}
	}

	public static RepositoryDAO getRepositoryDAO() {
		return repositoryDAO;
	}

	public RepositoryBuilderDAO getRepositoryBuilderDAO() {
		return repositoryBuilderDAO;
	}

	public List<EventDTO> getEvents(String repositoryName)
			throws RepositoryException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			Events events = repository.getEvents();
			if (events != null) {
				List<Event> list = events.getList();
				List<EventDTO> eventDTOs = new ArrayList<EventDTO>();
				for (Event event : list) {
					EventDTO eventDTO = transformEvent2DTO(event);
					eventDTOs.add(eventDTO);
				}
				return eventDTOs;
			} else {
				return null;
			}
		} else {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}
	}

	public void addEvent(String repositoryName, EventDTO eventDTO)
			throws RepositoryException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			Events events = repository.getEvents();
			if (events == null) {
				events = new Events();
				repository.setEvents(events);
			}
			Event event = transformDTO2Event(eventDTO);
			events.getList().add(event);
		} else {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}
	}

	public void renameEvent(String repositoryName, String eventName,
			String newEventName, String description) throws RepositoryException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			Events events = repository.getEvents();
			if (events != null) {
				for (Event event : events.getList()) {
					if (event.getName().equals(eventName)) {
						event.setName(newEventName);
						event.setDescription(description);
					}
				}
			}
		} else {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}
	}

	public void removeEvent(String repositoryName, String eventName)
			throws RepositoryException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			Events events = repository.getEvents();
			if (events != null) {
				Event eventFound = null;
				for (Event event : events.getList()) {
					if (event.getName().equals(eventName)) {
						eventFound = event;
						break;
					}
				}
				if (eventFound != null) {
					events.getList().remove(eventFound);
				}
			}
		} else {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}
	}

	public void saveToDiskEvents(String repositoryName)
			throws RepositoryException, WritingException, CheckException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			Events events = repository.getEvents();
			if (events != null) {
				String repositoryPath = repository.getPath();
				if ("".equals(repositoryPath) || repositoryPath == null) {
					throw new CheckException(
							"Repository folder location is missing.\n"
									+ "Please check out Repository panel informations.");
				}
				repositoryDAO.saveToDiskEvents(events, repositoryPath);
			}
		} else {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}
	}

	public TreeDirectoryDTO getAddonTreeFromRepository(String repositoryName,
			boolean withUserconfig) throws RepositoryException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			SyncTreeDirectory parentSyncTreeDirectory = repository.getSync();
			if (parentSyncTreeDirectory == null) {
				return null;
			} else {
				TreeDirectory parentTreeDirectory = new TreeDirectory(
						parentSyncTreeDirectory.getName(), null);
				extractAddons(parentSyncTreeDirectory, parentTreeDirectory);

				// Keep marked directory, change terminal directory to leaf
				TreeDirectory racineCleaned = new TreeDirectory("racine1", null);

				for (TreeNode directory : parentTreeDirectory.getList()) {
					TreeDirectory d = (TreeDirectory) directory;
					cleanTree(d, racineCleaned);
				}

				// Userconfig
				if (withUserconfig) {
					for (SyncTreeNode node : parentSyncTreeDirectory.getList()) {
						if (node.getName().toLowerCase().equals("userconfig")
								&& !node.isLeaf()) {
							TreeDirectory d = new TreeDirectory(node.getName(),
									racineCleaned);
							racineCleaned.addTreeNode(d);
							for (SyncTreeNode n : ((SyncTreeDirectory) node)
									.getList()) {
								TreeLeaf l = new TreeLeaf(n.getName(), d);
								d.addTreeNode(l);
							}
						}
					}
				}

				TreeDirectoryDTO treeDirectoryDTO = new TreeDirectoryDTO();
				treeDirectoryDTO.setName("racine1");
				treeDirectoryDTO.setParent(null);
				transformTreeDirectory2DTO(racineCleaned, treeDirectoryDTO);
				return treeDirectoryDTO;
			}
		} else {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}
	}

	private void cleanTree(TreeDirectory directory,
			TreeDirectory directoryCleaned) {

		if (directory.isMarked() && directory.getList().size() != 0) {
			TreeDirectory newDirectory = new TreeDirectory(directory.getName(),
					directoryCleaned);
			directoryCleaned.addTreeNode(newDirectory);
			for (TreeNode n : directory.getList()) {
				TreeDirectory d = (TreeDirectory) n;
				cleanTree(d, newDirectory);
			}
		} else if (directory.isMarked() && directory.getList().size() == 0) {
			TreeLeaf newTreelLeaf = new TreeLeaf(directory.getName(),
					directoryCleaned);
			directoryCleaned.addTreeNode(newTreelLeaf);
		}
	}

	private void extractAddons(SyncTreeDirectory syncTreeDirectory,
			TreeDirectory treeDirectory) {

		List<SyncTreeNode> list = syncTreeDirectory.getList();

		for (SyncTreeNode node : list) {
			if (!node.isLeaf()) {
				SyncTreeDirectory syncTreeDirectory2 = (SyncTreeDirectory) node;
				TreeDirectory treeDirectory2 = new TreeDirectory(
						node.getName(), treeDirectory);
				if (syncTreeDirectory2.isMarkAsAddon()) {
					treeDirectory.addTreeNode(treeDirectory2);
					markRecursively(treeDirectory2);
				} else {
					treeDirectory.addTreeNode(treeDirectory2);
					extractAddons(syncTreeDirectory2, treeDirectory2);
				}
			}
		}
	}

	private void markRecursively(TreeDirectory treeDirectory) {

		treeDirectory.setMarked(true);
		TreeDirectory parent = treeDirectory.getParent();
		if (parent != null) {
			markRecursively(parent);
		}
	}

	public void saveEvent(String repositoryName, EventDTO eventDTO) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			Events events = repository.getEvents();
			if (events != null) {
				for (Event event : events.getList()) {
					if (event.getName().equals(eventDTO.getName())) {
						event.getAddonNames().clear();
						event.getUserconfigFolderNames().clear();
						for (Iterator<String> iter = eventDTO.getAddonNames()
								.keySet().iterator(); iter.hasNext();) {
							String key = iter.next();
							boolean value = eventDTO.getAddonNames().get(key);
							event.getAddonNames().put(key, value);
						}
						for (Iterator<String> iter = eventDTO
								.getUserconfigFolderNames().keySet().iterator(); iter
								.hasNext();) {
							String key = iter.next();
							boolean value = eventDTO.getUserconfigFolderNames()
									.get(key);
							event.getUserconfigFolderNames().put(key, value);
						}
					}
				}
			}
		}
	}

	public void addExcludedFilesPathFromBuild(String repositoryName, String path) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.getExcludedFilesFromBuild().add(path);
		}
	}

	public Collection<String> getExcludedFilesPathFromBuild(
			String repositoryName) {

		Collection<String> list = new HashSet<String>();
		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			list.addAll(repository.getExcludedFilesFromBuild());
		}
		return list;
	}

	public void removeExcludedFilesPathFromBuild(String repositoryName,
			String path) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.getExcludedFilesFromBuild().remove(path);
		}
	}

	public void addExcludedFoldersFromSync(String repositoryName, String path) {
		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.getExcludedFoldersFromSync().add(path);
		}
	}

	private void retrievePaths(List<String> paths, File file) {

		if (file.isDirectory()) {
			File[] subfiles = file.listFiles();
			for (File f : subfiles) {
				retrievePaths(paths, f);
			}
		} else {
			paths.add(file.getAbsolutePath().toLowerCase());
		}
	}

	public Collection<String> getExcludedFoldersFromSync(String repositoryName) {

		Collection<String> list = new HashSet<String>();
		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			list.addAll(repository.getExcludedFoldersFromSync());
		}
		return list;
	}

	public void removeExcludedFoldersFromSync(String repositoryName, String path) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.getExcludedFoldersFromSync().remove(path);
		}
	}

	public List<FavoriteServerDTO> getFavoriteServerToAutoconfig(
			String repositoryName) {

		List<FavoriteServerDTO> favoriteServerDTOs = new ArrayList<FavoriteServerDTO>();
		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			List<FavoriteServer> list = repository
					.getFavoriteServersSetToAutoconfig();
			for (FavoriteServer favoriteServer : list) {
				FavoriteServerDTO f = transformFavoriteServers2DTO(favoriteServer);
				favoriteServerDTOs.add(f);
			}
		}
		return favoriteServerDTOs;
	}

	public void setFavoriteServerToAutoconfig(String repositoryName,
			List<FavoriteServerDTO> favoriteServerDTOs) {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository != null) {
			repository.getFavoriteServersSetToAutoconfig().clear();
			for (FavoriteServerDTO favoriteServerDTO : favoriteServerDTOs) {
				FavoriteServer favoriteServer = transformDTO2FavoriteServer(favoriteServerDTO);
				repository.getFavoriteServersSetToAutoconfig().add(
						favoriteServer);
			}
		}
	}

	private Cipher getEncryptionCipher() throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance("AES");
		SecretKey key = new SecretKeySpec(secreteKey, "AES");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher;
	}

	private Cipher getDecryptionCipher() throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance("AES");
		SecretKey key = new SecretKeySpec(secreteKey, "AES");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher;
	}
}