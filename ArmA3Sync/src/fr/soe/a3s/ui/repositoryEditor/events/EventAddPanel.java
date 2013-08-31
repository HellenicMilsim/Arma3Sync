package fr.soe.a3s.ui.repositoryEditor.events;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import javax.swing.JOptionPane;

import fr.soe.a3s.dto.EventDTO;
import fr.soe.a3s.exception.RepositoryException;
import fr.soe.a3s.ui.Facade;

public class EventAddPanel extends EventEditPanel {

	public EventAddPanel(Facade facade, String repositoryName) {
		super(facade, repositoryName);
	}

	public void init() {
		setTitle("New event");
	}

	public void buttonOKPerformed(String newEventName, String description) {

		try {
			// No duplicate event name
			List<EventDTO> eventDTOs = repositoryService
					.getEvents(repositoryName);
			if (eventDTOs!=null){
				for (EventDTO eventDTO:eventDTOs){
					if (eventDTO.getName().equals(newEventName)){
						labelWarning.setText("duplicate name!");
						labelWarning.setFont(new Font("Tohama", Font.ITALIC, 11));
						labelWarning.setForeground(Color.RED);
						return;
					}
				}
			}

			EventDTO eventDTO = new EventDTO();
			eventDTO.setName(newEventName);
			eventDTO.setDescription(description);
			repositoryService.addEvent(repositoryName, eventDTO);
			facade.getEventsPanel().updateListEvents();
			this.dispose();
		} catch (RepositoryException e) {
			JOptionPane.showMessageDialog(facade.getMainPanel(),
					e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
