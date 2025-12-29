package io.jmix.datatoolsflowui.view.datamodel;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.datatools.datamodel.DataModel;
import io.jmix.datatools.datamodel.DataModelManager;
import io.jmix.datatools.datamodel.engine.DiagramConstructor;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.urlqueryparameters.AbstractUrlQueryParametersBinder;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

@Route(value = "datatl/data-model", layout = DefaultMainViewParent.class)
@ViewController(id = "datatl_dataModelListView")
@ViewDescriptor(path = "data-model-list-view.xml")
@LookupComponent("entityModelsDataGrid")
@DialogMode(width = "50em")
public class DataModelListView extends StandardListView<EntityModel> {
    private static final String DETAILS_OPENED_URL_PARAM = "detailsOpened";
    private static final String FILTER_URL_PARAM = "entity-filter-text";

    @Autowired
    protected DataModelManager dataModelManager;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DiagramConstructor diagramConstructor;
    @Autowired
    protected ViewValidation viewValidation;

    @ViewComponent
    private JmixDetails filterFieldDetails;
    @ViewComponent
    private UrlQueryParametersFacet entityNameFilterQueryParameters;
    @ViewComponent
    private JmixCheckbox showSystemCheckBox;
    @ViewComponent
    private DataGrid<EntityModel> entityModelsDataGrid;
    @ViewComponent
    private CollectionLoader<EntityModel> entityModelsDl;
    @ViewComponent
    private CollectionContainer<EntityModel> entityModelsDc;
    @ViewComponent
    private CollectionContainer<AttributeModel> attributeModelsDc;
    @ViewComponent
    protected TypedTextField<String> entityNameFilter;

    @Autowired
    protected Icons icons;

    private class EntityNameUrlQueryParametersBinder extends AbstractUrlQueryParametersBinder {

        public EntityNameUrlQueryParametersBinder() {
            filterFieldDetails.addOpenedChangeListener(event -> {
                boolean opened = event.isOpened();
                QueryParameters qp = new QueryParameters(ImmutableMap.of(DETAILS_OPENED_URL_PARAM,
                        opened ? Collections.singletonList("1") : Collections.emptyList()));
                fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, qp));
            });

            entityNameFilter.addValueChangeListener(event -> {
                String text = event.getValue();
                QueryParameters qp = new QueryParameters(ImmutableMap.of(FILTER_URL_PARAM,
                        text != null ? Collections.singletonList(text) : Collections.emptyList()));
                fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, qp));
            });
        }

        @Override
        public void updateState(QueryParameters queryParameters) {
            List<String> detailsOpenedStrings = queryParameters.getParameters().get(DETAILS_OPENED_URL_PARAM);
            if (detailsOpenedStrings != null) {
                filterFieldDetails.setOpened("1".equals(detailsOpenedStrings.get(0)));
            }

            List<String> textStrings = queryParameters.getParameters().get(FILTER_URL_PARAM);
            if (textStrings != null && !textStrings.isEmpty()) {
                entityNameFilter.setValue(textStrings.get(0));
            }
        }

        @Override
        public Component getComponent() {
            return null;
        }
    }


    @Subscribe
    public void onInit(final InitEvent event) {
        entityNameFilterQueryParameters.registerBinder(new EntityNameUrlQueryParametersBinder());
        initEntityNameFilterTooltip();
    }

    @Install(to = "entityModelsDl", target = Target.DATA_LOADER)
    protected List<EntityModel> entityModelsDlLoadDelegate(LoadContext<EntityModel> loadContext) {
        List<String> entityNames = null;

        if (loadContext.getHints().containsKey("entityNames")) {
            List<?> tempHints = (List<?>) loadContext.getHints().get("entityNames");
            entityNames = new ArrayList<>();

            for (Object item : tempHints) {
                if (item instanceof String castedUuid) {
                    entityNames.add(castedUuid);
                }
            }
        }

        if (entityNames == null) {
            List<EntityModel> model = dataModelManager.getDataModelHolder().getDataModels().values()
                    .stream().map(DataModel::getEntityModel)
                    .filter(e -> {
                        if (showSystemCheckBox.getValue()) {
                            return true;
                        } else {
                            return e.getIsSystem().equals(false);
                        }
                    })
                    .toList();
            changeDataStoreColumnVisibility(model);
            entityModelsDc.setItems(model);
            return model;
        }

        List<EntityModel> model = entityNames.stream()
                .map(e -> dataModelManager.getDataModelHolder().getEntityModel(e))
                .filter(e -> {
                    if (showSystemCheckBox.getValue()) {
                        return true;
                    } else {
                        return e.getIsSystem().equals(false);
                    }
                })
                .toList();

        changeDataStoreColumnVisibility(model);
        entityModelsDc.setItems(model);
        return model;
    }

    protected void initEntityNameFilterTooltip() {
        JmixButton helperButton = createHelperButton();
        Tooltip tooltip = entityNameFilter.getTooltip();
        helperButton.addClickListener(e -> tooltip.setOpened(!tooltip.isOpened()));

        entityNameFilter.setSuffixComponent(helperButton);
    }

    protected JmixButton createHelperButton() {
        JmixButton helperButton = uiComponents.create(JmixButton.class);
        helperButton.setIcon(VaadinIcon.QUESTION_CIRCLE.create());
        helperButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);

        return helperButton;
    }

    protected void changeDataStoreColumnVisibility(List<EntityModel> model) {
        Set<String> byDataSet = new HashSet<>();

        for (EntityModel em : model) {
            byDataSet.add(em.getDataStore());
        }

        Grid.Column<EntityModel> dataStoreColumn = entityModelsDataGrid.getColumnByKey("dataStore");

        if (byDataSet.size() <= 1) {
            if (dataStoreColumn != null) {
                entityModelsDataGrid.removeColumn(dataStoreColumn);
            }
        } else {
            if (dataStoreColumn == null) {
                entityModelsDataGrid.addColumn("dataStore");
            }
        }
    }

    @Install(to = "attributeModelsDl", target = Target.DATA_LOADER)
    protected List<AttributeModel> attributeModelsDlLoadDelegate(LoadContext<AttributeModel> loadContext) {
        return List.of();
    }

    @Subscribe(id = "diagramButton", subject = "clickListener")
    public void onDiagramButtonClick(final ClickEvent<JmixButton> event) {
        ValidationErrors errors = new ValidationErrors();

        if (!diagramConstructor.pingService()) {
            errors.add("Remote diagramming service is not available.");
        };

        if (!errors.isEmpty()) {
            viewValidation.showValidationErrors(errors);
            return;
        }

        dataModelManager.setFilteredModels(entityModelsDl.getContainer().getItems());
        UI.getCurrent().getPage().open("datatl/data-diagram");
    }

    @Subscribe("entityModelsDataGrid")
    public void onEntityModelsDataGridItemClick(final ItemClickEvent<EntityModel> event) {
        EntityModel selectedModel = event.getItem();

        attributeModelsDc.setItems(dataModelManager.getDataModelHolder().getAttributesByEntity(selectedModel.getName()));

    }

    protected void searchByRegexp(String userInput) {
        Pattern pattern;

        try {
            pattern = Pattern.compile(userInput, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException pse) {
            entityNameFilter.setErrorMessage("Regular expression syntax is invalid");
            entityNameFilter.setInvalid(true);
            return;
        }

        List<String> findedEntityNames = dataModelManager.getDataModelHolder().getDataModels().keySet().stream()
                .filter(name -> name.matches(pattern.pattern())).toList();

        applyLoadContextWithFiltering(findedEntityNames);
    }

    protected void applyLoadContextWithFiltering(List<String> findedEntityNames) {
        if (findedEntityNames.isEmpty()) {
            entityNameFilter.setErrorMessage("Entities not found");
            entityNameFilter.setInvalid(true);
            return;
        }

        LoadContext<EntityModel> loadContext = entityModelsDl.createLoadContext();
        Map<String, Serializable> hints = Map.of("entityNames", new ArrayList<>(findedEntityNames));
        findedEntityNames.forEach(e -> loadContext.setHints(hints));

        entityModelsDl.getLoadDelegate().apply(loadContext);
    }

    protected void searchByEnumeration(List<String> requestedNames) {
        Set<String> allEntityNames = dataModelManager.getDataModelHolder().getDataModels().keySet();

        List<String> findedEntityNames = new ArrayList<>();

        for (String entityName : allEntityNames) {
            for (String reqName : requestedNames) {
                if (entityName.matches("(?i)" + ".*" + reqName + ".*")) {
                    findedEntityNames.add(entityName);
                }
            }
        }

//        List<String> findedEntityNames = requestedNames.stream()
//                .filter(e -> dataModelManager.getDataModelHolder().isModelExists(e))
//                .map(e -> dataModelManager.getDataModelHolder().getDataModel(e).getEntityModel().getName()).toList();

        /*
         TODO: Is it necessary to search by table names?
        if (findedEntityNames.isEmpty()) {

        }
         */

        applyLoadContextWithFiltering(findedEntityNames);
    }

    @Subscribe(id = "searchButton", subject = "clickListener")
    public void onSearchButtonClick(final ClickEvent<JmixButton> event) {
        Optional <String> tempValue = entityNameFilter.getOptionalValue();

        if (tempValue.isEmpty()) {
            entityModelsDl.load();
            return;
        }

        String userInput = tempValue.get();

        if (userInput.matches("^regexp:.*")) {
            userInput = userInput.substring(7);
            searchByRegexp(userInput);
            return;
        }

        List<String> requestedNames = Stream.of(userInput.split(","))
                .map(String::strip).toList();

        if (requestedNames.isEmpty()) {
            entityModelsDl.load();
            return;
        }

        searchByEnumeration(requestedNames);
    }

    @Supply(to = "attributeModelsDataGrid.isMandatory", subject = "renderer")
    protected Renderer<AttributeModel> loggedEntityTableAutoRenderer() {
        return new ComponentRenderer<>(entity ->
                createCheckboxIconByAttributeValue(entity.getIsMandatory())
        );
    }

    @Supply(to = "attributeModelsDataGrid.isNullable", subject = "renderer")
    protected Renderer<AttributeModel> loggedEntityTableManualRenderer() {
        return new ComponentRenderer<>(entity ->
                createCheckboxIconByAttributeValue(entity.getIsNullable())
        );
    }

    protected Component createCheckboxIconByAttributeValue(Boolean attributeValue) {
        return Boolean.TRUE.equals(attributeValue)
                ? icons.get(JmixFontIcon.CHECK_SQUARE_O)
                : icons.get(JmixFontIcon.THIN_SQUARE);
    }

    @Subscribe("entityNameFilter")
    public void onEntityNameFilterKeyPress(final KeyPressEvent event) {
        entityNameFilter.setInvalid(false);
    }

    @Subscribe("showSystemCheckBox")
    public void onShowSystemCheckBoxComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {
        entityModelsDl.load();
    }
}
