package declarative_screen_load.screen;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test_support.entity.sales.Customer;

@Route(value = "test-project-view")
@UiController("CustomerView")
@UiDescriptor("customer-view.xml")
public class CustomerView extends Screen {

    private static final Logger log = LoggerFactory.getLogger(CustomerView.class);

    @ComponentId
    public CollectionContainer<Customer> customersDc;

    public Button doBtn;

    @Subscribe
    public void onInit(Screen.InitEvent event) {
        doBtn = new Button("", __ -> {
            log.info("Button clicked!");
        });
    }
}
