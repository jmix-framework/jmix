package test_support;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletService;
import org.springframework.mock.web.MockHttpServletRequest;

public class TestVaadinRequest extends VaadinServletRequest implements VaadinRequest {

    public TestVaadinRequest(VaadinServletService vaadinService) {
        super(new MockHttpServletRequest(), vaadinService);
    }
}
