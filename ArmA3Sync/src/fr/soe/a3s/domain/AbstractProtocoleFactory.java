package fr.soe.a3s.domain;

import fr.soe.a3s.constant.ProtocolType;

public class AbstractProtocoleFactory {

	public static AbstractProtocole getProtocol(String url, String port,
			String login, String password, String connectionTimeOut,
			String readTimeOut, ProtocolType protocolType) {

		if (protocolType.equals(ProtocolType.FTP)) {
			return new Ftp(url, port, login, password, connectionTimeOut,
					readTimeOut, protocolType);
		} else if (protocolType.equals(ProtocolType.HTTP)) {
			return new Http(url, port, login, password, connectionTimeOut,
					readTimeOut, protocolType);
		} else if (protocolType.equals(ProtocolType.HTTPS)) {
			return new Http(url, port, login, password, connectionTimeOut,
					readTimeOut, protocolType);
		} else if (protocolType.equals(ProtocolType.HTTP_WEBDAV)) {
			return new Http(url, port, login, password, connectionTimeOut,
					readTimeOut, protocolType);
		} else if (protocolType.equals(ProtocolType.HTTPS_WEBDAV)) {
			return new Http(url, port, login, password, connectionTimeOut,
					readTimeOut, protocolType);
		} else {
			return null;
		}
	}

	public static AbstractProtocole getProtocol(String url, String port,
			String login, String password, ProtocolType protocolType) {

		if (protocolType.equals(ProtocolType.FTP)) {
			return new Ftp(url, port, login, password, protocolType);
		} else if (protocolType.equals(ProtocolType.HTTP)) {
			return new Http(url, port, login, password, protocolType);
		} else if (protocolType.equals(ProtocolType.HTTPS)) {
			return new Http(url, port, login, password, protocolType);
		} else if (protocolType.equals(ProtocolType.HTTP_WEBDAV)) {
			return new Http(url, port, login, password, protocolType);
		} else if (protocolType.equals(ProtocolType.HTTPS_WEBDAV)) {
			return new Http(url, port, login, password, protocolType);
		} else {
			return null;
		}
	}
}
