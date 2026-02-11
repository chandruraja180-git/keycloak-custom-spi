package listeners;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

public class BasicEventListener implements EventListenerProvider {

    @Override
    public void onEvent(Event event) {
        if (event.getType() == EventType.LOGIN) {
            System.out.println("LOGIN EVENT");
            System.out.println("User ID    : " + event.getUserId());
            System.out.println("Client ID  : " + event.getClientId());
            System.out.println("Session ID : " + event.getSessionId());
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
    }

    @Override
    public void close() {
    }
}
