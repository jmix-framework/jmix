package main_view.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route("test-main-view")
@ViewController("MainView")
@ViewDescriptor("main-view.xml")
public class MainView extends StandardMainView {

    @ViewComponent
    public TypedTextField<String> textField;

    @Subscribe
    public void onInit(final InitEvent event) {
        textField.setWidth("25em");
    }
}
