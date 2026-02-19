package io.jmix.datatoolsflowui.view.datamodel;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.datatools.datamodel.DataModel;
import io.jmix.datatools.datamodel.DataModelGenerationSupport;
import io.jmix.datatools.datamodel.DataModelRegistry;
import io.jmix.datatools.datamodel.engine.DiagramService;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;
import io.jmix.datatoolsflowui.datamodel.DataDiagramViewSupport;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.SupportsTypedValue.TypedValueChangeEvent;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.urlqueryparameters.AbstractUrlQueryParametersBinder;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Route(value = "datatl/data-model", layout = DefaultMainViewParent.class)
@ViewController(id = "datatl_dataModelListView")
@ViewDescriptor(path = "data-model-list-view.xml")
@DialogMode(width = "50em")
public class DataModelListView extends StandardView {

    protected static final String FILTER_URL_PARAM = "filter";
    protected static final String SHOW_SYSTEM_URL_PARAM = "show-system";
    public static final String REGEXP_PREFIX = "regexp:";

    @ViewComponent
    protected DataGrid<EntityModel> entityModelsDataGrid;
    @ViewComponent
    protected JmixCheckbox showSystemCheckBox;
    @ViewComponent
    protected TypedTextField<String> entityNameFilter;

    @ViewComponent
    protected CollectionContainer<EntityModel> entityModelsDc;
    @ViewComponent
    protected CollectionLoader<EntityModel> entityModelsDl;
    @ViewComponent
    protected CollectionLoader<AttributeModel> attributeModelsDl;

    @ViewComponent
    protected UrlQueryParametersFacet urlQueryParametersFacet;

    @Autowired
    protected Icons icons;
    @Autowired
    protected DataModelGenerationSupport dataModelGenerationSupport;
    @Autowired
    protected DiagramService diagramService;
    @Autowired
    protected DataDiagramViewSupport dataDiagramViewSupport;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected DataModelRegistry dataModelRegistry;

    protected Set<String> dataStoreNames;

    @Subscribe
    public void onInit(final InitEvent event) {
        urlQueryParametersFacet.registerBinder(new EntityNameUrlQueryParametersBinder());
        initDataStoreNames();
    }

    protected void initDataStoreNames() {
        this.dataStoreNames = getDataModelProvider().getDataModels().keySet();
    }

    private DataModelRegistry getDataModelProvider() {
        return dataModelRegistry;
    }

    @Install(to = "entityModelsDl", target = Target.DATA_LOADER)
    protected List<EntityModel> entityModelsDlLoadDelegate(LoadContext<EntityModel> loadContext) {
        List<String> entityNames = collectEntityNamesToFilter();
        if (entityNames.isEmpty() && !entityNameFilter.isEmpty()) {
            return Collections.emptyList();
        }

        List<EntityModel> models;
        DataModelRegistry dataModelRegistry = getDataModelProvider();

        if (entityNames.isEmpty()) {
            models = dataModelRegistry.getDataModels().values().stream()
                    .flatMap(e -> e.values().stream())
                    .map(DataModel::entityModel)
                    .filter(entityModel ->
                            Boolean.TRUE.equals(showSystemCheckBox.getValue())
                                    || !Boolean.TRUE.equals(entityModel.getIsSystem()))
                    .toList();
        } else {
            models = entityNames.stream()
                    .flatMap(entityName -> dataStoreNames.stream()
                            .map(dataStore ->
                                    dataModelRegistry.getEntityModel(dataStore, entityName)))
                    .filter(Objects::nonNull)
                    .filter(entityModel ->
                            Boolean.TRUE.equals(showSystemCheckBox.getValue())
                                    || !Boolean.TRUE.equals(entityModel.getIsSystem()))
                    .toList();
        }

        changeDataStoreColumnVisibility(models);

        return models;
    }

    protected List<String> collectEntityNamesToFilter() {
        String filterValue = entityNameFilter.getTypedValue();
        if (Strings.isNullOrEmpty(filterValue)) {
            return Collections.emptyList();
        }

        DataModelRegistry dataModelRegistry = getDataModelProvider();

        if (filterValue.matches("^%s.*".formatted(REGEXP_PREFIX))) {
            Pattern pattern = Pattern.compile(filterValue.substring(REGEXP_PREFIX.length()), Pattern.CASE_INSENSITIVE);

            return dataStoreNames.stream()
                    .flatMap(dataStore ->
                            dataModelRegistry.getDataModels(dataStore).keySet().stream())
                    .filter(name -> name.matches(pattern.pattern()))
                    .toList();

        } else {
            List<String> requestedNames = Stream.of(filterValue.split(","))
                    .map(String::strip)
                    .toList();

            return dataStoreNames.stream()
                    .flatMap(dataStore ->
                            dataModelRegistry.getDataModels(dataStore).keySet().stream())
                    .distinct()
                    .filter(entityName -> requestedNames.stream()
                            .anyMatch(reqName ->
                                    entityName.matches("(?i)" + ".*" + reqName + ".*")))
                    .toList();
        }
    }

    protected void changeDataStoreColumnVisibility(List<EntityModel> model) {
        long dataStoreCount = model.stream()
                .map(EntityModel::getDataStore)
                .distinct()
                .count();

        Grid.Column<EntityModel> dataStoreColumn = entityModelsDataGrid.getColumnByKey("dataStore");
        if (dataStoreColumn != null) {
            dataStoreColumn.setVisible(dataStoreCount > 1);
        }
    }

    @Install(to = "attributeModelsDl", target = Target.DATA_LOADER)
    protected List<AttributeModel> attributeModelsDlLoadDelegate(LoadContext<AttributeModel> loadContext) {
        EntityModel selectedModel = entityModelsDataGrid.getSingleSelectedItem();
        return selectedModel != null
                ? getDataModelProvider()
                .getEntityAttributes(selectedModel.getDataStore(), selectedModel.getName())
                : Collections.emptyList();
    }

    @Subscribe("entityNameFilter")
    public void onEntityNameFilterTypedValueChange(final TypedValueChangeEvent<TypedTextField<String>, String> event) {
        entityModelsDl.load();
    }

    @Subscribe("showSystemCheckBox")
    public void onShowSystemCheckBoxComponentValueChange(final ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {
        entityModelsDl.load();
    }

    @Subscribe("entityModelsDataGrid")
    public void onEntityModelsDataGridSelection(final SelectionEvent<DataGrid<EntityModel>, EntityModel> event) {
        attributeModelsDl.load();
    }

    @Subscribe(id = "diagramButton", subject = "clickListener")
    public void onDiagramButtonClick(final ClickEvent<JmixButton> event) {
        if (!diagramService.pingService()) {
            notifications.create("Remote diagramming service is not available.")
                    .withType(Notifications.Type.ERROR)
                    .show();

            return;
        }

        byte[] diagramData = dataModelGenerationSupport.generateDiagram(entityModelsDc.getItems());
        dataDiagramViewSupport.open(diagramData);
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

    protected class EntityNameUrlQueryParametersBinder extends AbstractUrlQueryParametersBinder {

        public EntityNameUrlQueryParametersBinder() {
            entityNameFilter.addValueChangeListener(this::onFilter);
            showSystemCheckBox.addValueChangeListener(this::onFilter);
        }

        @Override
        public void updateState(QueryParameters queryParameters) {
            String filter = queryParameters.getSingleParameter(FILTER_URL_PARAM).orElse(null);
            entityNameFilter.setTypedValue(filter);

            Boolean showSystem = queryParameters.getSingleParameter(SHOW_SYSTEM_URL_PARAM)
                    .map(Boolean::parseBoolean)
                    .orElse(false);
            showSystemCheckBox.setValue(showSystem);
        }

        @Override
        public Component getComponent() {
            return null;
        }

        protected void onFilter(ComponentValueChangeEvent<?, ?> event) {
            QueryParameters qp = QueryParameters.simple(ImmutableMap.of(
                    FILTER_URL_PARAM, Strings.nullToEmpty(entityNameFilter.getTypedValue()),
                    SHOW_SYSTEM_URL_PARAM, urlParamSerializer.serialize(showSystemCheckBox.getValue()))
            );

            fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, qp));
        }
    }
}
