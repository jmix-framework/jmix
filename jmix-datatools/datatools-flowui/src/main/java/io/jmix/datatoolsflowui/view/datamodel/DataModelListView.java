package io.jmix.datatoolsflowui.view.datamodel;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.datatools.datamodel.DataModel;
import io.jmix.datatools.datamodel.DataModelRegistry;
import io.jmix.datatools.datamodel.Relation;
import io.jmix.datatools.datamodel.RelationType;
import io.jmix.datatools.datamodel.engine.DiagramEngine;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;
import io.jmix.datatoolsflowui.datamodel.DataModelDiagramViewSupport;
import io.jmix.datatoolsflowui.datamodel.DataModelProvider;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Route(value = "datatl/data-model", layout = DefaultMainViewParent.class)
@ViewController(id = "datatl_dataModelListView")
@ViewDescriptor(path = "data-model-list-view.xml")
@DialogMode(width = "50em")
public class DataModelListView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(DataModelListView.class);

    protected static final String REGEXP_PREFIX = "regexp:";
    protected static final String FILTER_URL_PARAM = "filter";
    protected static final String SHOW_SYSTEM_URL_PARAM = "show-system";

    @ViewComponent
    protected DataGrid<EntityModel> entityModelsDataGrid;
    @ViewComponent
    protected JmixCheckbox showSystemCheckBox;
    @ViewComponent
    protected TypedTextField<String> entityFilter;
    @ViewComponent
    protected Span entityModelCount;
    @ViewComponent
    protected Span attributesCount;

    @ViewComponent
    protected CollectionContainer<EntityModel> entityModelsDc;
    @ViewComponent
    protected CollectionLoader<EntityModel> entityModelsDl;
    @ViewComponent
    protected CollectionLoader<AttributeModel> attributeModelsDl;

    @ViewComponent
    protected UrlQueryParametersFacet urlQueryParametersFacet;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    protected Icons icons;
    @Autowired
    protected DiagramEngine diagramEngine;
    @Autowired
    protected DataModelDiagramViewSupport dataModelDiagramViewSupport;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected DataModelRegistry dataModelRegistry;

    protected DataModelProvider dataModelProvider;
    protected Set<String> dataStoreNames;

    @Subscribe
    public void onInit(final InitEvent event) {
        urlQueryParametersFacet.registerBinder(new EntityNameUrlQueryParametersBinder());
        initDataStoreNames();
    }

    protected void initDataStoreNames() {
        this.dataStoreNames = getDataModelProvider().getDataModels().keySet();
    }

    private DataModelProvider getDataModelProvider() {
        if (dataModelProvider == null) {
            dataModelProvider = new DataModelProvider(dataModelRegistry.getDataModels());
        }

        return dataModelProvider;
    }

    @Install(to = "entityModelsDl", target = Target.DATA_LOADER)
    protected List<EntityModel> entityModelsDlLoadDelegate(LoadContext<EntityModel> loadContext) {
        String filterValue = entityFilter.getTypedValue();
        DataModelProvider dataModelProvider = getDataModelProvider();
        List<EntityModel> models = dataStoreNames.stream()
                .flatMap(dataStore ->
                        dataModelProvider.getDataModels(dataStore).values().stream())
                .map(DataModel::entityModel)
                .filter(entityModel -> {
                    if (Strings.isNullOrEmpty(filterValue)) {
                        return true;
                    } else if (filterValue.matches("^%s.*".formatted(REGEXP_PREFIX))) {
                        return filterByRegexp(entityModel, filterValue);
                    } else {
                        return filterByString(entityModel, filterValue);
                    }
                })
                .filter(entityModel ->
                        Boolean.TRUE.equals(showSystemCheckBox.getValue())
                                || !Boolean.TRUE.equals(entityModel.getIsSystem()))
                .toList();

        changeDataStoreColumnVisibility(models);

        entityModelCount.setText("%d".formatted(models.size()));
        return models;
    }

    protected boolean filterByRegexp(EntityModel entityModel, String filterValue) {
        Pattern pattern = Pattern.compile(
                filterValue.substring(REGEXP_PREFIX.length()), Pattern.CASE_INSENSITIVE);

        return entityModel.getName().matches(pattern.pattern())
                || entityModel.getTableName().matches(pattern.pattern());
    }

    protected boolean filterByString(EntityModel entityModel, String filterValue) {
        List<String> requestedNames = Stream.of(filterValue.split(","))
                .map(String::strip)
                .toList();

        return requestedNames.stream()
                .anyMatch(reqName ->
                        entityModel.getName().matches("(?i)" + ".*" + reqName + ".*")
                                || entityModel.getTableName().matches("(?i)" + ".*" + reqName + ".*"));
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
        List<AttributeModel> attributeModels = selectedModel != null
                ? getDataModelProvider()
                .getEntityAttributes(selectedModel.getDataStore(), selectedModel.getName())
                : Collections.emptyList();

        attributesCount.setText("%d".formatted(attributeModels.size()));
        return attributeModels;
    }

    @Subscribe("entityFilter")
    public void onEntityFilterTypedValueChange(final TypedValueChangeEvent<TypedTextField<String>, String> event) {
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
        if (!diagramEngine.pingService()) {
            notifications.create(messageBundle.getMessage("diagramGeneration.error.serviceUnavailable.message"))
                    .withType(Notifications.Type.ERROR)
                    .show();

            return;
        }

        try {
            byte[] diagramData = generateDiagram(entityModelsDc.getItems());
            dataModelDiagramViewSupport.open(this, diagramData);
        } catch (Exception e) {
            log.error("Diagram generation failed", e);
            notifications.create(messageBundle.getMessage("diagramGeneration.error.generationFailed.message"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected byte[] generateDiagram(List<EntityModel> models) {
        StringBuilder tempEntitiesDescription = new StringBuilder();
        StringBuilder tempRelationsDescription = new StringBuilder();
        Set<String> completedModels = new HashSet<>();
        List<String> entityModelsNames = models.stream()
                .map(EntityModel::getName)
                .toList();

        for (EntityModel model : models) {
            for (String dataStore : dataStoreNames) {
                DataModel dataModel = dataModelProvider.getDataModel(dataStore, model.getName());
                if (dataModel == null) {
                    continue;
                }
                tempEntitiesDescription.append(dataModel.entityDescription());

                if (!dataModelProvider.hasRelations(dataStore, model.getName())) {
                    continue;
                }

                for (String referencedEntity : entityModelsNames) {
                    if (!model.getName().equals(referencedEntity)
                            && !completedModels.contains(referencedEntity)) {
                        constructRelations(model.getName(), referencedEntity, dataStore, tempRelationsDescription);
                    }
                }

                completedModels.add(model.getName());
            }
        }

        return diagramEngine.generateDiagram(tempEntitiesDescription.toString(), tempRelationsDescription.toString());
    }

    protected void constructRelations(String currentEntity, String referencedEntity,
                                      String dataStore, StringBuilder relationsDescription) {
        if (!(containsModel(dataStore, currentEntity)
                && containsModel(dataStore, referencedEntity))) {
            return;
        }

        Map<RelationType, List<Relation>> directRelations =
                dataModelProvider.getEntityRelations(dataStore, currentEntity);
        Map<RelationType, List<Relation>> referencedRelations =
                dataModelProvider.getEntityRelations(dataStore, referencedEntity);
        Set<RelationType> directRelationTypes = directRelations.keySet();

        if (directRelationTypes.isEmpty()) {
            return;
        }

        for (RelationType relationType : directRelationTypes) {
            referencedRelations.getOrDefault(RelationType.getReverseRelation(relationType),
                            crossRelationCheck(currentEntity, referencedEntity, dataStore, relationType))
                    .stream()
                    .filter(el ->
                            el.referencedClass().equals(currentEntity))
                    .forEach(e ->
                            relationsDescription.append(e.relationDescription()));
        }
    }

    protected boolean containsModel(String dataStore, String entityName) {
        return dataModelProvider.getDataModels(dataStore).containsKey(entityName);
    }

    protected List<Relation> crossRelationCheck(String currentEntity, String referencedEntity,
                                                String dataStore, RelationType relationType) {
        if (RelationType.getReverseRelation(relationType).equals(RelationType.ONE_TO_MANY)) {
            // inverse relation emulation for MANY_TO_ONE relation
            DataModel dataModel = dataModelProvider.getDataModel(dataStore, currentEntity);
            return dataModel != null
                    ? dataModel.relations().get(relationType).stream()
                    .filter(el ->
                            el.referencedClass().equals(referencedEntity))
                    .map(e ->
                            new Relation(dataStore, currentEntity, e.relationDescription()))
                    .toList()
                    : Collections.emptyList();
        }

        return Collections.emptyList();
    }

    @Supply(to = "attributeModelsDataGrid.isMandatory", subject = "renderer")
    protected Renderer<AttributeModel> loggedEntityTableAutoRenderer() {
        return new ComponentRenderer<>(entity ->
                createCheckboxIconByAttributeValue(entity.getIsMandatory())
        );
    }

    protected Component createCheckboxIconByAttributeValue(Boolean attributeValue) {
        return Boolean.TRUE.equals(attributeValue)
                ? icons.get(JmixFontIcon.CHECK_SQUARE_O)
                : icons.get(JmixFontIcon.THIN_SQUARE);
    }

    protected class EntityNameUrlQueryParametersBinder extends AbstractUrlQueryParametersBinder {

        public EntityNameUrlQueryParametersBinder() {
            entityFilter.addValueChangeListener(this::onFilter);
            showSystemCheckBox.addValueChangeListener(this::onFilter);
        }

        @Override
        public void updateState(QueryParameters queryParameters) {
            String filter = queryParameters.getSingleParameter(FILTER_URL_PARAM).orElse(null);
            entityFilter.setTypedValue(filter);

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
                    FILTER_URL_PARAM, Strings.nullToEmpty(entityFilter.getTypedValue()),
                    SHOW_SYSTEM_URL_PARAM, urlParamSerializer.serialize(showSystemCheckBox.getValue()))
            );

            fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, qp));
        }
    }
}
