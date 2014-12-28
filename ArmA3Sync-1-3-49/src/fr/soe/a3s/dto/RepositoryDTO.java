package fr.soe.a3s.dto;

import fr.soe.a3s.domain.repository.SyncTreeLeaf;

public class RepositoryDTO implements java.lang.Comparable {

	private String name;
	private boolean notify;
	private ProtocoleDTO protocoleDTO;
	private int revision;
	private String path;
	private String autoConfigURL;
	private boolean outOfSynk;
	private ProtocoleDTO repositoryUploadProtocoleDTO;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public ProtocoleDTO getProtocoleDTO() {
		return protocoleDTO;
	}

	public void setProtocoleDTO(ProtocoleDTO protocoleDTO) {
		this.protocoleDTO = protocoleDTO;
	}

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAutoConfigURL() {
		return autoConfigURL;
	}

	public void setAutoConfigURL(String autoConfigURL) {
		this.autoConfigURL = autoConfigURL;
	}

	public boolean isOutOfSynk() {
		return outOfSynk;
	}

	public void setOutOfSynk(boolean outOfSynk) {
		this.outOfSynk = outOfSynk;
	}

	public ProtocoleDTO getRepositoryUploadProtocoleDTO() {
		return repositoryUploadProtocoleDTO;
	}

	public void setRepositoryUploadProtocoleDTO(
			ProtocoleDTO repositoryUploadProtocoleDTO) {
		this.repositoryUploadProtocoleDTO = repositoryUploadProtocoleDTO;
	}

	@Override
	public int compareTo(Object other) {
		String name = ((RepositoryDTO) other).getName();
		int result = 1;
		if (name.compareToIgnoreCase(getName()) > 0)
			result = -1;
		else if (name.compareToIgnoreCase(getName()) == 0)
			result = 0;
		return result;
	}
}