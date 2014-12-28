package fr.soe.a3s.service;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import fr.soe.a3s.controller.ObserverFileDownload;
import fr.soe.a3s.dao.AbstractConnexionDAO;
import fr.soe.a3s.dao.ConfigurationDAO;
import fr.soe.a3s.dao.DataAccessConstants;
import fr.soe.a3s.dao.HttpDAO;
import fr.soe.a3s.dao.RepositoryDAO;
import fr.soe.a3s.domain.configration.FavoriteServer;
import fr.soe.a3s.domain.repository.AutoConfig;
import fr.soe.a3s.domain.repository.Changelogs;
import fr.soe.a3s.domain.repository.Events;
import fr.soe.a3s.domain.repository.Repository;
import fr.soe.a3s.domain.repository.ServerInfo;
import fr.soe.a3s.domain.repository.SyncTreeDirectory;
import fr.soe.a3s.dto.AutoConfigDTO;
import fr.soe.a3s.dto.sync.SyncTreeDirectoryDTO;
import fr.soe.a3s.dto.sync.SyncTreeLeafDTO;
import fr.soe.a3s.dto.sync.SyncTreeNodeDTO;
import fr.soe.a3s.exception.FtpException;
import fr.soe.a3s.exception.HttpException;
import fr.soe.a3s.exception.RepositoryException;
import fr.soe.a3s.exception.WritingException;

public class HttpService extends AbstractConnexionService implements
		DataAccessConstants {

	private final List<HttpDAO> httpDAOPool = new ArrayList<HttpDAO>();
	private final Stack<SyncTreeNodeDTO> downloadFilesStack = new Stack();
	private final List<Exception> errors = new ArrayList<Exception>();
	private int semaphore = 1;
	private boolean end = false;
	private static final RepositoryDAO repositoryDAO = new RepositoryDAO();
	private static final ConfigurationDAO configurationDAO = new ConfigurationDAO();

	public HttpService(int nbConnections) {
		assert (nbConnections != 0);
		for (int i = 0; i < nbConnections; i++) {
			HttpDAO httpDAO = new HttpDAO();
			httpDAOPool.add(httpDAO);
		}
	}

	public HttpService() {
		HttpDAO httpDAO = new HttpDAO();
		httpDAOPool.add(httpDAO);
	}

	@Override
	public AutoConfigDTO importAutoConfig(String url) throws WritingException,
			HttpException, ConnectException {

		AutoConfig autoConfig = httpDAOPool.get(0).downloadAutoConfig(url);
		disconnect();
		if (autoConfig != null) {
			List<FavoriteServer> list1 = autoConfig.getFavoriteServers();
			List<FavoriteServer> list2 = configurationDAO.getConfiguration()
					.getFavoriteServers();
			List<FavoriteServer> newList = new ArrayList<FavoriteServer>();
			newList.addAll(list1);
			for (FavoriteServer favoriteServer2 : list2) {
				boolean contains = false;
				for (FavoriteServer favoriteServer : newList) {
					if (favoriteServer.getName().equals(
							favoriteServer2.getName())) {
						contains = true;
						break;
					}
				}
				if (!contains) {
					newList.add(favoriteServer2);
				}
			}

			configurationDAO.getConfiguration().getFavoriteServers().clear();
			configurationDAO.getConfiguration().getFavoriteServers()
					.addAll(newList);
			return transformAutoConfig2DTO(autoConfig);
		} else {
			return null;
		}
	}

	@Override
	public void checkRepository(String repositoryName)
			throws RepositoryException, WritingException, ConnectException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository == null) {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}

		try {
			SyncTreeDirectory syncTreeDirectory = httpDAOPool.get(0)
					.downloadSync(repository);
			repository.setSync(syncTreeDirectory);// null if not found
			ServerInfo serverInfo = httpDAOPool.get(0).downloadSeverInfo(
					repository);
			repository.setServerInfo(serverInfo);// null if not found
			if (serverInfo != null) {
				repository.getHiddenFolderPath().addAll(
						serverInfo.getHiddenFolderPaths());
			}
			Changelogs changelogs = httpDAOPool.get(0).downloadChangelogs(
					repository);
			repository.setChangelogs(changelogs);// null if not found
			Events events = httpDAOPool.get(0).downloadEvent(repository);
			repository.setEvents(events);// null if not found
		} catch (HttpException e) {
			// error http 404 may happen if repository has not been built so far
		}
	}

	@Override
	public void getSync(String repositoryName) throws RepositoryException,
			HttpException, WritingException, ConnectException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository == null) {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}

		SyncTreeDirectory syncTreeDirectory = httpDAOPool.get(0).downloadSync(
				repository);
		repository.setSync(syncTreeDirectory);// null if not found
	}

	@Override
	public void getServerInfo(String repositoryName)
			throws RepositoryException, ConnectException, WritingException,
			HttpException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository == null) {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}

		ServerInfo serverInfo = httpDAOPool.get(0)
				.downloadSeverInfo(repository);
		repository.setServerInfo(serverInfo);// null if not found
	}

	@Override
	public void getChangelogs(String repositoryName) throws ConnectException,
			RepositoryException, WritingException, HttpException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository == null) {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}

		Changelogs changelogs = httpDAOPool.get(0).downloadChangelogs(
				repository);
		repository.setChangelogs(changelogs);// null if not found
	}

	@Override
	public void downloadAddons(String repositoryName,
			List<SyncTreeNodeDTO> listFiles) throws Exception {

		final Repository repository = repositoryDAO.getMap()
				.get(repositoryName);
		if (repository == null) {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}

		assert (repository.getSync() != null);
		assert (repository.getServerInfo() != null);

		final String rootDestinationPath = repository
				.getDefaultDownloadLocation();

		downloadFilesStack.addAll(listFiles);
		this.semaphore = 1;
		this.end = false;

		for (final HttpDAO httpDAO : httpDAOPool) {
			httpDAO.addObserverFileDownload(new ObserverFileDownload() {
				@Override
				public void proceed() {
					if (!httpDAO.isCanceled()) {
						final SyncTreeNodeDTO node = popDownloadFilesStack();
						if (node != null) {
							Thread t = new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										if (aquireSemaphore()) {
											httpDAO.setAcquiredSmaphore(true);
										}

										httpDAO.setActiveConnection(true);
										httpDAO.updateObserverActiveConnection();

										downloadAddon(httpDAO, node,
												rootDestinationPath, repository);

									} catch (FileNotFoundException e) {
										String message = "File not found on repository : "
												+ node.getRelativePath();
										addError(new FileNotFoundException(
												message));
									} catch (Exception e) {
										if (!httpDAO.isCanceled()) {
											addError(e);
										}
									} finally {
										if (httpDAO.isAcquiredSmaphore()) {
											releaseSemaphore();
											httpDAO.setAcquiredSmaphore(false);
										}
										httpDAO.setActiveConnection(false);
										httpDAO.updateObserverActiveConnection();
										httpDAO.updateFileDownloadObserver();
									}
								}
							});
							t.start();
						} else {// no more file to download
							if (httpDAO.isAcquiredSmaphore()) {
								releaseSemaphore();
								httpDAO.setAcquiredSmaphore(false);
							}
							if (!end) {
								end = true;
								for (HttpDAO httpDAO : httpDAOPool) {
									if (httpDAO.isActiveConnection()) {
										end = false;
										break;
									}
								}
								if (end) {
									if (errors.isEmpty()) {
										httpDAO.updateObserverEnd();
									} else {
										httpDAO.updateObserverError(errors);
									}
								} else {
									for (HttpDAO httpDAO : httpDAOPool) {
										if (httpDAO.isActiveConnection()
												&& aquireSemaphore()) {
											httpDAO.setAcquiredSmaphore(true);
											break;
										}
									}
								}
							}
						}
					}
				}
			});
		}

		for (HttpDAO httpDAO : httpDAOPool) {
			if (!downloadFilesStack.isEmpty()) {// nb files < nb connections
				try {
					httpDAO.updateFileDownloadObserver();
				} catch (Exception e) {
					boolean isDowloading = false;
					httpDAO.setActiveConnection(false);
					for (HttpDAO hDAO : httpDAOPool) {
						if (hDAO.isActiveConnection()) {
							isDowloading = true;
							break;
						}
					}
					if (!isDowloading) {
						throw e;
					}
				}
			}
		}
	}

	private void downloadAddon(final HttpDAO httpDAO,
			final SyncTreeNodeDTO node, final String rootDestinationPath,
			final Repository repository) throws Exception,
			FileNotFoundException {

		String url = repository.getProtocole().getUrl();
		String hostname = url;
		String rootRemotePath = "";
		int index = url.indexOf("/");
		if (index != -1) {
			hostname = url.substring(0, index);
			rootRemotePath = url.substring(index);
		}
		String port = repository.getProtocole().getPort();
		String login = repository.getProtocole().getLogin();
		String password = repository.getProtocole().getPassword();

		String destinationPath = null;
		String remotePath = rootRemotePath;
		String path = determinePath(node);
		if (node.getDestinationPath() != null) {
			destinationPath = node.getDestinationPath();
			remotePath = remotePath + "/" + path;
		} else {
			destinationPath = rootDestinationPath + "/" + path;
			remotePath = remotePath + "/" + path;
		}

		httpDAO.downloadFile(hostname, login, password, port, remotePath,
				destinationPath, node);
	}

	private synchronized void addError(Exception e) {
		errors.add(e);
	}

	private synchronized SyncTreeNodeDTO popDownloadFilesStack() {

		if (downloadFilesStack.isEmpty()) {
			return null;
		} else {
			return downloadFilesStack.pop();
		}
	}

	private synchronized boolean aquireSemaphore() {

		if (this.semaphore == 1) {
			this.semaphore = 0;
			return true;
		} else {
			return false;
		}
	}

	private synchronized void releaseSemaphore() {
		semaphore = 1;
	}

	@Override
	public void determineCompletion(String repositoryName,
			SyncTreeDirectoryDTO parent) throws RepositoryException,
			HttpException, WritingException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository == null) {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}

		assert (repository.getSync() != null);
		assert (repository.getServerInfo() != null);
		String url = repository.getProtocole().getUrl();
		String hostname = url;
		String rootRemotePath = "";
		int index = url.indexOf("/");
		if (index != -1) {
			hostname = url.substring(0, index);
			rootRemotePath = url.substring(index);
		}
		String port = repository.getProtocole().getPort();
		String login = repository.getProtocole().getLogin();
		String password = repository.getProtocole().getPassword();

		String rootDestinationPath = repository.getDefaultDownloadLocation();

		for (SyncTreeNodeDTO node : parent.getList()) {
			if (node.isLeaf()) {
				SyncTreeLeafDTO leaf = (SyncTreeLeafDTO) node;
				try {
					String destinationPath = null;
					String remotePath = rootRemotePath;
					String path = determinePath(node);
					if (node.getDestinationPath() != null) {
						destinationPath = node.getDestinationPath();
						remotePath = remotePath + "/" + path;
					} else {
						destinationPath = rootDestinationPath + "/" + path;
						remotePath = remotePath + "/" + path;
					}

					if (httpDAOPool.get(0).isCanceled()) {
						break;
					}

					httpDAOPool.get(0).getFileCompletion(hostname, login,
							password, port, remotePath, destinationPath, node);
				} catch (Exception e) {
					e.printStackTrace();
					throw new WritingException(
							"An unexpected error has occured.\nInternal error.");
				}
			} else {
				SyncTreeDirectoryDTO directory = (SyncTreeDirectoryDTO) node;
				determineCompletion(repositoryName, directory);
			}
		}
	}

	@Override
	public boolean upLoadEvents(String repositoryName)
			throws RepositoryException, HttpException {

		Repository repository = repositoryDAO.getMap().get(repositoryName);
		if (repository == null) {
			throw new RepositoryException("Repository " + repositoryName
					+ " not found!");
		}

		boolean response = httpDAOPool.get(0).uploadEvents(repository);
		return response;
	}

	private String determinePath(SyncTreeNodeDTO syncTreeNodeDTO) {

		assert (syncTreeNodeDTO.getParent() != null);
		String path = "";
		while (syncTreeNodeDTO.getParent().getName() != "racine") {
			path = syncTreeNodeDTO.getParent().getName() + "/" + path;
			syncTreeNodeDTO = syncTreeNodeDTO.getParent();
		}
		return path;
	}

	@Override
	public void getSyncWithRepositoryUploadProtocole(String repositoryName)
			throws RepositoryException, WritingException, ConnectException,
			FtpException {
		// unimplemented
	}

	@Override
	public void uploadRepository(String repositoryName,
			List<SyncTreeNodeDTO> filesToUpload,
			List<SyncTreeNodeDTO> filesToDelete, boolean resume)
			throws RepositoryException, ConnectException, FtpException {
		// unimplemented
	}

	@Override
	public boolean remoteFileExists(String repositoryName,
			SyncTreeNodeDTO remoteNode) throws RepositoryException,
			ConnectException, FtpException {
		// unimplemented
		return false;
	}

	@Override
	public void cancel(boolean resumable) {
		for (HttpDAO httpDAO : httpDAOPool) {
			httpDAO.cancel(resumable);
		}
	}

	@Override
	public void disconnect() {
		for (HttpDAO httpDAO : httpDAOPool) {
			httpDAO.disconnect();
		}
	}

	@Override
	public HttpDAO getConnexionDAO() {
		return httpDAOPool.get(0);
	}

	@Override
	public List<AbstractConnexionDAO> getConnexionDAOs() {
		List<AbstractConnexionDAO> list = new ArrayList<>();
		for (HttpDAO httpDAO : httpDAOPool) {
			list.add(httpDAO);
		}
		return list;
	}

	@Override
	public int getNumberConnections() {
		return httpDAOPool.size();
	}
}