package ${packageName};

import com.google.common.base.Strings;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

<%if (!updateLayoutProperty) {%>/*
 * To use the view as a main view don't forget to set
 * new value (see @ViewController) to 'jmix.ui.main-view-id' property.
 */
@Route(value = "${route}")<%} else {%>@Route("")<%}%>
@ViewController(id = "${id}")
@ViewDescriptor(path = "${descriptorName}.xml")
public class ${controllerName} extends StandardMainView {

    @Override
    protected void updateTitle() {
        super.updateTitle();

        String viewTitle = getTitleFromOpenedView();
        UiComponentUtils.findComponent(getContent(), "viewHeaderBox")
                .ifPresent(component -> component.setVisible(!Strings.isNullOrEmpty(viewTitle)));
    }
}