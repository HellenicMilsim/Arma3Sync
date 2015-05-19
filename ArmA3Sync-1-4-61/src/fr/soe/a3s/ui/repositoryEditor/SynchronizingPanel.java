package fr.soe.a3s.ui.repositoryEditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import fr.soe.a3s.dto.RepositoryDTO;
import fr.soe.a3s.service.AbstractConnexionService;
import fr.soe.a3s.service.ConnexionServiceFactory;
import fr.soe.a3s.service.RepositoryService;
import fr.soe.a3s.ui.Facade;

public class SynchronizingPanel extends ProgressPanel {

	private final RepositoryService repositoryService = new RepositoryService();
	private AbstractConnexionService connexion;

	public SynchronizingPanel(Facade facade) {
		super(facade);
		labelTitle.setText("Checking repositories...");

		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				menuExitPerformed();
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

	public void init(final String repositoryName) {

		facade.getSyncPanel().getButtonSync1().setEnabled(false);
		facade.getSyncPanel().getButtonSync2().setEnabled(false);
		progressBar.setIndeterminate(true);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				if (repositoryName == null) {
					List<RepositoryDTO> list = repositoryService
							.getRepositories();
					for (final RepositoryDTO repositoryDTO : list) {
						if (canceled) {
							break;
						}
						try {
							connexion = ConnexionServiceFactory
									.getServiceFromRepository(repositoryDTO
											.getName());
							connexion.checkRepository(repositoryDTO.getName());
							facade.getAddonsPanel().updateModsetSelection(
									repositoryDTO.getName());
						} catch (Exception e) {
							System.out.println(e.getMessage());
						}
					}
				} else {
					try {
						connexion = ConnexionServiceFactory
								.getServiceFromRepository(repositoryName);
						connexion.checkRepository(repositoryName);
						facade.getAddonsPanel().updateModsetSelection(
								repositoryName);
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}

				facade.getSyncPanel().init();
				facade.getOnlinePanel().init();
				facade.getLaunchPanel().init();
				progressBar.setIndeterminate(false);
				dispose();
				facade.getSyncPanel().getButtonSync1().setEnabled(true);
				facade.getSyncPanel().getButtonSync2().setEnabled(true);
			}
		});
		t.start();
	}

	private void menuExitPerformed() {
		this.setVisible(false);
		canceled = true;
		if (connexion != null) {
			connexion.disconnect();
		}
		this.dispose();
	}
}