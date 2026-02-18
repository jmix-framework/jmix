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
import io.jmix.datatools.datamodel.DataModelSupport;
import io.jmix.datatools.datamodel.engine.DiagramConstructor;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;
import io.jmix.datatoolsflowui.view.navigation.DataDiagramViewSupport;
import io.jmix.flowui.component.SupportsTypedValue.TypedValueChangeEvent;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
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
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Route(value = "datatl/data-model", layout = DefaultMainViewParent.class)
@ViewController(id = "datatl_dataModelListView")
@ViewDescriptor(path = "data-model-list-view.xml")
@DialogMode(width = "50em")
public class DataModelListView extends StandardView {

    protected static final String FILTER_URL_PARAM = "filter";
    protected static final String SHOW_SYSTEM_URL_PARAM = "show-system";
    public static final String REGEXP_PREFIX = "^regexp:.*";

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
    protected DataModelSupport dataModelSupport;
    @Autowired
    protected DiagramConstructor diagramConstructor;
    @Autowired
    protected ViewValidation viewValidation;
    @Autowired
    protected DataDiagramViewSupport dataDiagramViewSupport;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;

    private Set<String> dataStoreNames;

    @Subscribe
    public void onInit(final InitEvent event) {
        urlQueryParametersFacet.registerBinder(new EntityNameUrlQueryParametersBinder());
        initDataStoreNames();
    }

    protected void initDataStoreNames() {
        this.dataStoreNames = dataModelSupport.getDataModelProvider().getDataModels().keySet();
    }

    @Install(to = "entityModelsDl", target = Target.DATA_LOADER)
    protected List<EntityModel> entityModelsDlLoadDelegate(LoadContext<EntityModel> loadContext) {
        List<String> entityNames = new ArrayList<>();

        String filterValue = entityNameFilter.getTypedValue();
        if (!Strings.isNullOrEmpty(filterValue)) {
            if (filterValue.matches(REGEXP_PREFIX)) {
                Pattern pattern = Pattern.compile(filterValue.substring(REGEXP_PREFIX.length()), Pattern.CASE_INSENSITIVE);
                for (String dataStore : dataStoreNames) {
                    entityNames.addAll(dataModelSupport.getDataModelProvider().getDataModels(dataStore).keySet().stream()
                            .filter(name -> name.matches(pattern.pattern())).toList());
                }
            } else {
                List<String> requestedNames = Stream.of(filterValue.split(","))
                        .map(String::strip).toList();

                Set<String> allEntityNames = new HashSet<>();

                for (String dataStore : dataStoreNames) {
                    allEntityNames.addAll(dataModelSupport.getDataModelProvider().getDataModels(dataStore).keySet());
                }

                // TODO: gg, refactor
                for (String entityName : allEntityNames) {
                    for (String reqName : requestedNames) {
                        if (entityName.matches("(?i)" + ".*" + reqName + ".*")) {
                            entityNames.add(entityName);
                        }
                    }
                }
            }
        }

        // TODO: gg, refactor
        List<EntityModel> models;
        if (entityNames.isEmpty()) {
            models = dataModelSupport.getDataModelProvider().getDataModels().values().stream()
                    .flatMap(e -> e.values().stream())
                    .map(DataModel::entityModel)
                    .filter(entityModel -> {
                        if (showSystemCheckBox.getValue()) {
                            return true;
                        } else {
                            return entityModel.getIsSystem().equals(false);
                        }
                    })
                    .toList();
        } else {
            models = entityNames.stream()
                    .flatMap(entityName -> {
                        List<EntityModel> result = new ArrayList<>();

                        for (String dataStore : dataStoreNames) {
                            result.add(dataModelSupport.getDataModelProvider().getEntityModel(dataStore, entityName));
                        }

                        return result.stream();
                    })
                    .filter(entityModel -> {
                        if (showSystemCheckBox.getValue()) {
                            return true;
                        } else {
                            return entityModel.getIsSystem().equals(false);
                        }
                    })
                    .toList();
        }

        changeDataStoreColumnVisibility(models);

        return models;
    }

    protected void changeDataStoreColumnVisibility(List<EntityModel> model) {
        Set<String> dataStores = new HashSet<>();

        for (EntityModel em : model) {
            dataStores.add(em.getDataStore());
        }

        Grid.Column<EntityModel> dataStoreColumn = entityModelsDataGrid.getColumnByKey("dataStore");

        if (dataStores.size() <= 1) {
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
        EntityModel selectedModel = entityModelsDataGrid.getSingleSelectedItem();
        return selectedModel != null
                ? dataModelSupport.getDataModelProvider()
                .getAttributesByEntity(selectedModel.getDataStore(), selectedModel.getName())
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
        ValidationErrors errors = new ValidationErrors();

        if (!diagramConstructor.pingService()) {
            errors.add("Remote diagramming service is not available.");
        }

        if (!errors.isEmpty()) {
            viewValidation.showValidationErrors(errors);
            return;
        }

        // TODO: gg, refactor
        dataModelSupport.setFilteredModels(entityModelsDc.getItems());

        // navigate to DataDiagramView
        dataDiagramViewSupport.open();
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

    private class EntityNameUrlQueryParametersBinder extends AbstractUrlQueryParametersBinder {

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

        private void onFilter(ComponentValueChangeEvent<?, ?> event) {
            QueryParameters qp = QueryParameters.simple(ImmutableMap.of(
                    FILTER_URL_PARAM, Strings.nullToEmpty(entityNameFilter.getTypedValue()),
                    SHOW_SYSTEM_URL_PARAM, urlParamSerializer.serialize(showSystemCheckBox.getValue()))
            );

            fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, qp));
        }
    }
}
