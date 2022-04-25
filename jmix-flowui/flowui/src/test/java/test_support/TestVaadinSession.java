package test_support;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.SpringVaadinSession;

public class TestVaadinSession extends SpringVaadinSession {

    public TestVaadinSession(VaadinService service) {
        super(service);
    }

    @Override
    public boolean hasLock() {
        return true;
    }

    @Override
    public void lock() {
        // do nothing
    }

    @Override
    public void unlock() {
        // do nothing
    }
}
