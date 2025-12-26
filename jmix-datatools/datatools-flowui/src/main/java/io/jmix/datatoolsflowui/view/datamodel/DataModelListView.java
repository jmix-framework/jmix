package io.jmix.datatoolsflowui.view.datamodel;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.datatools.datamodel.DataModel;
import io.jmix.datatools.datamodel.DataModelManager;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

@Route(value = "datatl/data-model", layout = DefaultMainViewParent.class)
@ViewController(id = "datatl_dataModelListView")
@ViewDescriptor(path = "data-model-list-view.xml")
@LookupComponent("entityModelsDataGrid")
@DialogMode(width = "50em")
public class DataModelListView extends StandardListView<EntityModel> {
    @Autowired
    protected DataModelManager dataModelManager;
    
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


    @Subscribe
    public void onInit(final InitEvent event) {
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
        dataModelManager.setFilteredModels(entityModelsDl.getContainer().getItems());
        UI.getCurrent().getPage().open("datatl/data-diagram");
    }

    @Subscribe("entityModelsDataGrid")
    public void onEntityModelsDataGridItemClick(final ItemClickEvent<EntityModel> event) {
        EntityModel selectedModel = event.getItem();

        attributeModelsDc.setItems(dataModelManager.getDataModelHolder().getAttributesByEntity(selectedModel.getName()));

    }

    @Subscribe(id = "searchButton", subject = "clickListener")
    public void onSearchButtonClick(final ClickEvent<JmixButton> event) {
        Optional <String> tempValue = entityNameFilter.getOptionalValue();
        List<String> entityNames = new ArrayList<>();

        if (tempValue.isPresent()) {
            entityNames = Stream.of(tempValue.get().split(","))
                    .map(String::strip).toList();
        }

        if (!entityNames.isEmpty()) {
            List<String> findedEntityNames = entityNames.stream()
                    .filter(e -> dataModelManager.getDataModelHolder().isModelExists(e))
                    .map(e -> dataModelManager.getDataModelHolder().getDataModel(e).getEntityModel().getName()).toList();

            if (findedEntityNames.isEmpty()) {
                entityNameFilter.setErrorMessage("Entities not found");
                entityNameFilter.setInvalid(true);
                return;
            }
            LoadContext<EntityModel> loadContext = entityModelsDl.createLoadContext();
            Map<String, Serializable> hints = Map.of("entityNames", new ArrayList<>(findedEntityNames));
            findedEntityNames.forEach(e -> loadContext.setHints(hints));

            entityModelsDl.getLoadDelegate().apply(loadContext);
            return;
        }

        entityModelsDl.load();
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
