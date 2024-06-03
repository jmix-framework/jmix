<%
        def pluralForm = api.pluralForm(entity.uncapitalizedClassName)
        def tableDl = entity.uncapitalizedClassName.equals(pluralForm) ? pluralForm + "CollectionDl" : pluralForm + "Dl"
    %>
package ${packageName};

import ${entity.fqn};<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent;
<%} else {%>
import ${routeLayout.getControllerFqn()};
<%}%>import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.core.validation.group.UiCrossFieldChecks;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.*;
<%if (useDataRepositories){%>
import java.util.Collection;
import java.util.List;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static io.jmix.core.repository.JmixDataRepositoryUtils.*;
<%}%>



<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent.class <%} else {%>${routeLayout.getControllerClassName()}.class<%}%>)
@ViewController("${viewId}")
@ViewDescriptor("${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "64em")
public class ${viewControllerName} extends StandardListView<${entity.className}> {
<%if (useDataRepositories){%>
    @Autowired
    private ${repository.getQualifiedName()} repository;
<%}%>
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
        ${entity.className} item = ${detailDc}.getItem();
        ValidationErrors validationErrors = validateView(item);
        if (!validationErrors.isEmpty()) {
            ViewValidation viewValidation = getViewValidation();
            viewValidation.showValidationErrors(validationErrors);
            viewValidation.focusProblemComponent(validationErrors);
            return;
        }
        dataContext.save();
        ${tableDc}.replaceItem(item);
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

    protected ValidationErrors validateView(${entity.className} entity) {
        ViewValidation viewValidation = getViewValidation();
        ValidationErrors validationErrors = viewValidation.validateUiComponents(form);
        if (!validationErrors.isEmpty()) {
            return validationErrors;
        }
        validationErrors.addAll(viewValidation.validateBeanGroup(UiCrossFieldChecks.class, entity));
        return validationErrors;
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

    private ViewValidation getViewValidation() {
        return getApplicationContext().getBean(ViewValidation.class);
    }<%if (useDataRepositories){%>

    @Install(to = "${tableDl}", target = Target.DATA_LOADER)
    private List<${entity.className}> listLoadDelegate(LoadContext<${entity.className}> context){
        return repository.findAll(buildPageRequest(context), buildRepositoryContext(context)).getContent();
    }<%if (tableActions.contains("remove")) {%>

    @Install(to = "${tableId}.remove", subject = "delegate")
    private void ${tableId}RemoveDelegate(final Collection<${entity.className}> collection) {
        repository.deleteAll(collection);
    }<%}%>

    @Install(to = "${detailDl}", target = Target.DATA_LOADER)
    private ${entity.className} detailLoadDelegate(LoadContext<${entity.className}> context){
        return repository.getById(extractEntityId(context), context.getFetchPlan());
    }

    @Install(target = Target.DATA_CONTEXT)
    private Set<Object> saveDelegate(SaveContext saveContext) {
        <%def compositeAttrs = ''
          detailFetchPlan.orderedRootProperties.each {property ->
              def propAttr = detailFetchPlan.entity.getAttribute(property.name)
              if (propAttr != null && propAttr.hasAnnotation('Composition')) {
                  compositeAttrs = compositeAttrs + property.name + ', '
              }
          }
          if (compositeAttrs.length() > 0){
              compositeAttrs = compositeAttrs.substring(0, compositeAttrs.length() - 2);
              println """// ${entity.className} has the following @Composition attributes: $compositeAttrs.
                       // To save them, either add cascade in JPA annotation or pass to appropriate repository manually."""}
        %>return Set.of(repository.save(${detailDc}.getItem()));
    }<%}%>
}