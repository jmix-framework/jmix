package ${packageName};

import ${entity.fqn};<%if (!api.jmixProjectModule.isApplication()) {%>
import io.jmix.flowui.view.DefaultMainViewParent;
<%} else {%>
import ${routeLayout.getControllerFqn()};
<%}%>import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.*;

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent.class <%} else {%>${routeLayout.getControllerClassName()}.class<%}%>)
@ViewController("${viewId}")
@ViewDescriptor("${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "64em")
public class ${viewControllerName} extends StandardListView<${entity.className}> {

    @ViewComponent
    private DataContext dataContext;

    @ViewComponent
    private CollectionContainer<${entity.className}> ${tableDc};

    @ViewComponent
    private InstanceContainer<${entity.className}> ${detailDc};

    @ViewComponent
    private InstanceLoader<${entity.className}> ${detailDl};

    @ViewComponent
    private VerticalLayout listLayout;

    @ViewComponent
    private FormLayout form;

    @ViewComponent
    private HorizontalLayout detailActions;

    @Subscribe
    public void onInit(final InitEvent event) {
        updateControls(false);
    }

    @Subscribe("${tableId}.create")
    public void on${tableId.capitalize()}Create(final ActionPerformedEvent event) {
        dataContext.clear();
        ${entity.className} entity = dataContext.create(${entity.className}.class);
        ${detailDc}.setItem(entity);
        updateControls(true);
    }

    @Subscribe("${tableId}.edit")
    public void on${tableId.capitalize()}Edit(final ActionPerformedEvent event) {
        updateControls(true);
    }

    @Subscribe("saveBtn")
    public void onSaveButtonClick(final ClickEvent<JmixButton> event) {
        dataContext.save();
        ${tableDc}.replaceItem(${detailDc}.getItem());
        updateControls(false);
    }

    @Subscribe("cancelBtn")
    public void onCancelButtonClick(final ClickEvent<JmixButton> event) {
        dataContext.clear();
        ${detailDl}.load();
        updateControls(false);
    }

    @Subscribe(id = "${tableDc}", target = Target.DATA_CONTAINER)
    public void on${tableDc.capitalize()}ItemChange(final InstanceContainer.ItemChangeEvent<${entity.className}> event) {
        ${entity.className} entity = event.getItem();
        dataContext.clear();
        if (entity != null) {
            ${detailDl}.setEntityId(entity.getId());
            ${detailDl}.load();
        } else {
            ${detailDl}.setEntityId(null);
            ${detailDc}.setItem(null);
        }
    }

    private void updateControls(boolean editing) {
        form.getChildren().forEach(component -> {
            if (component instanceof HasValueAndElement<?, ?> field) {
                field.setReadOnly(!editing);
            }
        });

        detailActions.setVisible(editing);
        listLayout.setEnabled(!editing);
    }
}