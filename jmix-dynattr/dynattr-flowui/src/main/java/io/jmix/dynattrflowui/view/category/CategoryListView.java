/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.dynattrflowui.view.category;

import com.google.common.io.Files;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.accesscontext.EntityAttributeContext;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattr.model.ReferenceToEntity;
import io.jmix.dynattrflowui.utils.DynAttrUiHelper;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.jmix.dynattrflowui.view.categoryattr.CategoryAttributesDetailView.CATEGORY_ID_ROUTE_PARAMETER;
import static io.jmix.flowui.download.DownloadFormat.JSON;
import static io.jmix.flowui.download.DownloadFormat.ZIP;

@Route(value = "dynat/category", layout = DefaultMainViewParent.class)
@ViewController("dynat_CategoryView.list")
@ViewDescriptor("category-list-view.xml")
@PrimaryListView(Category.class)
@LookupComponent("categoriesGrid")
@DialogMode(width = "47.5em")
public class CategoryListView extends StandardListView<Category> {

    public static final String SELECTED_CATEGORY_QUERY_PARAMETER = "selectedCategory";

    private static final Logger log = LoggerFactory.getLogger(CategoryListView.class);

    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DynAttrMetadata dynAttrMetadata;
    @Autowired
    protected EntityImportExport entityImportExport;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected EntityImportPlans entityImportPlans;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;
    @Autowired
    protected RouteSupport routeSupport;
    @Autowired
    protected DynAttrUiHelper dynAttrUiHelper;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected ReferenceToEntitySupport referenceToEntitySupport;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;


    @ViewComponent
    protected DataGrid<Category> categoriesGrid;
    @ViewComponent
    protected CollectionContainer<Category> categoriesDc;
    @ViewComponent
    protected CollectionLoader<Category> categoriesDl;

    @ViewComponent
    protected DataGrid<CategoryAttribute> attributesTable;
    @ViewComponent
    protected CollectionContainer<CategoryAttribute> attributesDc;

    @ViewComponent("attributesTable.create")
    protected Action createAction;
    @ViewComponent("attributesTable.moveUp")
    protected Action moveUpAction;
    @ViewComponent("attributesTable.moveDown")
    protected Action moveDownAction;

    @ViewComponent
    protected FileUploadField importField;
    @ViewComponent
    protected Button applyChangesBtn;
    @ViewComponent
    protected DataContext dataContext;

    protected UUID selectedCategoryId;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        super.beforeEnter(event);

        processQueryParameters(event);
    }

    @Override
    protected void processBeforeEnterInternal(BeforeEnterEvent event) {
        super.processBeforeEnterInternal(event);

        processQueryParameters(event);
    }

    protected void processQueryParameters(BeforeEnterEvent event) {
        Map<String, List<String>> parameters = event.getLocation().getQueryParameters().getParameters();

        if (parameters.containsKey(SELECTED_CATEGORY_QUERY_PARAMETER)) {
            parameters.get(SELECTED_CATEGORY_QUERY_PARAMETER)
                    .stream()
                    .filter(StringUtils::isNotBlank)
                    .findAny()
                    .ifPresent(this::setSelectedCategory);
        }
    }

    protected void setSelectedCategory(String categoryUUid) {
        selectedCategoryId = UUID.fromString(categoryUUid);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        setupFieldsLock();
    }

    @Subscribe
    protected void onReady(ReadyEvent event) {
        setupSelectedCategory();
        categoriesGrid.addSelectionListener(this::onCategoryGridSelectionChange);
    }

    protected void setupFieldsLock() {
        CrudEntityContext crudEntityContext = new CrudEntityContext(categoriesDc.getEntityMetaClass());
        accessManager.applyRegisteredConstraints(crudEntityContext);
        if (!crudEntityContext.isUpdatePermitted()) {
            applyChangesBtn.setEnabled(false);
        }
    }

    protected void setupSelectedCategory() {
        if (selectedCategoryId == null || categoriesGrid.getItems() == null) {
            return;
        }

        categoriesGrid.getItems()
                .getItems()
                .stream()
                .filter(category -> selectedCategoryId.equals(category.getId()))
                .findAny()
                .ifPresent(categoriesGrid::select);
    }

    @Subscribe("categoriesGrid.applyChanges")
    protected void onCategoriesGridApplyChanges(ActionPerformedEvent event) {
        dynAttrMetadata.reload();
        notifications.create(messages.getMessage(CategoryListView.class, "notification.changesApplied"))
                .withType(Notifications.Type.SUCCESS)
                .show();
    }

    @Supply(to = "categoriesGrid.isDefault", subject = "renderer")
    protected Renderer<Category> categoriesGridIsDefaultRenderer() {
        return new ComponentRenderer<>(category ->
                createCheckboxIconByAttributeValue(category.getIsDefault())
        );
    }

    @Supply(to = "attributesTable.required", subject = "renderer")
    protected Renderer<CategoryAttribute> categoryAttrsGridRequiredRenderer() {
        return new ComponentRenderer<>(categoryAttribute ->
                createCheckboxIconByAttributeValue(categoryAttribute.getRequired())
        );
    }

    @Supply(to = "attributesTable.isCollection", subject = "renderer")
    protected Renderer<CategoryAttribute> categoryAttrsGridIsCollectionRenderer() {
        return new ComponentRenderer<>(categoryAttribute ->
                createCheckboxIconByAttributeValue(categoryAttribute.getIsCollection())
        );
    }

    protected Icon createCheckboxIconByAttributeValue(Boolean attributeValue) {
        Icon icon = uiComponents.create(Icon.class);

        if (Boolean.TRUE.equals(attributeValue)) {
            icon.setIcon(VaadinIcon.CHECK_SQUARE_O);
        } else {
            icon.setIcon(VaadinIcon.THIN_SQUARE);
        }

        return icon;
    }

    @Supply(to = "attributesTable.dataType", subject = "renderer")
    protected Renderer<CategoryAttribute> createCategoryAttrsGridDataTypeRenderer() {
        return new ComponentRenderer<>(this::categoryAttrsGridDataTypeComponent,
                this::categoryAttrsGridDataTypeUpdater);
    }

    protected Span categoryAttrsGridDataTypeComponent() {
        return uiComponents.create(Span.class);
    }

    protected void categoryAttrsGridDataTypeUpdater(Span text, CategoryAttribute categoryAttribute) {
        String dataType;
        if (Boolean.TRUE.equals(categoryAttribute.getIsEntity())) {
            Class<?> javaType = categoryAttribute.getJavaType();
            if (javaType != null) {
                MetaClass metaClass = metadata.getClass(javaType);
                dataType = messageTools.getEntityCaption(metaClass);
            } else {
                dataType = "";
            }
        } else {
            String key = AttributeType.class.getSimpleName() + "." + categoryAttribute.getDataType().toString();
            dataType = messages.getMessage(AttributeType.class, key);
        }

        text.setText(dataType);
    }

    @Supply(to = "attributesTable.defaultValue", subject = "renderer")
    protected Renderer<CategoryAttribute> createCategoryAttrsGridDefaultValueRenderer() {
        return new ComponentRenderer<>(this::categoryAttrsGridDefaultValueColumnComponent,
                this::categoryAttrsGridDefaultValueColumnUpdater);
    }

    protected Span categoryAttrsGridDefaultValueColumnComponent() {
        return uiComponents.create(Span.class);
    }

    protected void categoryAttrsGridDefaultValueColumnUpdater(Span defaultValueLabel, CategoryAttribute attribute) {
        String defaultValue = "";

        AttributeType dataType = attribute.getDataType();
        switch (dataType) {
            case BOOLEAN -> {
                Boolean b = attribute.getDefaultBoolean();
                if (b != null)
                    defaultValue = BooleanUtils.isTrue(b)
                            ? messages.getMessage("trueString")
                            : messages.getMessage("falseString");
            }
            case DATE -> {
                Date dateTime = attribute.getDefaultDate();
                if (dateTime != null) {
                    String dateTimeFormat =
                            formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getDateTimeFormat();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
                    defaultValue = simpleDateFormat.format(dateTime);
                } else if (BooleanUtils.isTrue(attribute.getDefaultDateIsCurrent())) {
                    defaultValue = messages.getMessage(getClass(), "categoryAttrsGrid.currentDate");
                }
            }
            case DATE_WITHOUT_TIME -> {
                LocalDate dateWoTime = attribute.getDefaultDateWithoutTime();
                if (dateWoTime != null) {
                    String dateWoTimeFormat =
                            formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getDateFormat();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateWoTimeFormat);
                    defaultValue = dateWoTime.format(formatter);
                } else if (BooleanUtils.isTrue(attribute.getDefaultDateIsCurrent())) {
                    defaultValue = messages.getMessage(getClass(), "categoryAttrsGrid.currentDate");
                }
            }
            case DECIMAL -> {
                BigDecimal defaultDecimal = attribute.getDefaultDecimal();
                if (defaultDecimal != null) {
                    defaultValue = defaultDecimal.toString();
                }
            }
            case DOUBLE -> {
                Double defaultDouble = attribute.getDefaultDouble();
                if (defaultDouble != null) {
                    defaultValue = defaultDouble.toString();
                }
            }
            case ENTITY -> {
                Class<?> entityClass = attribute.getJavaType();
                if (entityClass != null) {
                    defaultValue = "";
                    if (attribute.getObjectDefaultEntityId() != null) {
                        MetaClass metaClass = metadata.getClass(entityClass);
                        LoadContext<?> lc = new LoadContext<>(metadata.getClass(attribute.getJavaType()));
                        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.INSTANCE_NAME);
                        lc.setFetchPlan(fetchPlan);
                        String pkName = referenceToEntitySupport.getPrimaryKeyForLoadingEntity(metaClass);
                        lc.setQueryString(String.format("select e from %s e where e.%s = :entityId", metaClass.getName(), pkName))
                                .setParameter("entityId", attribute.getObjectDefaultEntityId());
                        Object entity = dataManager.load(lc);
                        if (entity != null) {
                            defaultValue = metadataTools.getInstanceName(entity);
                        }
                    }
                } else {
                    defaultValue = messages.getMessage(getClass(), "categoryAttrsGrid.entityNotFound");
                }
            }
            case ENUMERATION, STRING -> defaultValue = attribute.getDefaultString();
            case INTEGER -> {
                Integer defaultInt = attribute.getDefaultInt();
                if (defaultInt != null) {
                    defaultValue = defaultInt.toString();
                }
            }
        }

        defaultValueLabel.setText(defaultValue);
    }

    @Subscribe("attributesTable.moveUp")
    protected void onCategoryAttrsGridMoveUp(ActionPerformedEvent event) {
        dynAttrUiHelper.moveTableItemUp(attributesDc, attributesTable, () ->
                attributesDc.getMutableItems()
                        .forEach(item -> item.setOrderNo(attributesDc.getMutableItems().indexOf(item))));
        refreshMoveActionStates();
        dataContext.save();
    }

    @Install(to = "attributesTable.moveUp", subject = "enabledRule")
    protected boolean categoryAttrsGridMoveUpEnabledRule() {
        CategoryAttribute item = attributesTable.getSingleSelectedItem();
        if (item == null) {
            return false;
        }

        return getPrevAttribute(item.getOrderNo()) != null && checkOrderNoModificationPermissions();
    }

    @Subscribe("attributesTable.moveDown")
    protected void onCategoryAttrsGridMoveDown(ActionPerformedEvent event) {
        dynAttrUiHelper.moveTableItemDown(attributesDc, attributesTable, () ->
                attributesDc.getMutableItems()
                        .forEach(item -> item.setOrderNo(attributesDc.getMutableItems().indexOf(item))));
        refreshMoveActionStates();
        dataContext.save();
    }

    @Install(to = "attributesTable.moveDown", subject = "enabledRule")
    protected boolean categoryAttrsGridMoveDownEnabledRule() {
        CategoryAttribute item = attributesTable.getSingleSelectedItem();
        if (item == null) {
            return false;
        }

        return getNextAttribute(item.getOrderNo()) != null && checkOrderNoModificationPermissions();
    }

    protected boolean checkOrderNoModificationPermissions() {
        EntityAttributeContext context = new EntityAttributeContext(attributesDc.getEntityMetaClass(), "orderNo");
        accessManager.applyRegisteredConstraints(context);
        return context.canModify();
    }

    protected void refreshMoveActionStates() {
        moveUpAction.refreshState();
        moveDownAction.refreshState();
    }

    protected CategoryAttribute getPrevAttribute(Integer orderNo) {
        return attributesDc.getMutableItems()
                .stream()
                .filter(categoryAttribute -> orderNo.compareTo(categoryAttribute.getOrderNo()) > 0)
                .max(Comparator.comparing(CategoryAttribute::getOrderNo))
                .orElse(null);
    }

    protected CategoryAttribute getNextAttribute(Integer orderNo) {
        return attributesDc.getMutableItems()
                .stream()
                .filter(categoryAttribute -> orderNo.compareTo(categoryAttribute.getOrderNo()) < 0)
                .min(Comparator.comparing(CategoryAttribute::getOrderNo))
                .orElse(null);
    }

    @Install(to = "attributesTable.create", subject = "enabledRule")
    protected boolean categoryAttrsGridCreateEnabledRule() {
        return categoriesGrid.getSingleSelectedItem() != null;
    }

    @Subscribe("categoriesGrid")
    protected void onCategoriesGridSelectionEvent(SelectionEvent<Grid<Category>, Category> event) {
        createAction.refreshState();
    }

    protected void onCategoryGridSelectionChange(SelectionEvent<Grid<Category>, Category> event) {
        Object categoryUuid;

        Category category = categoriesGrid.getSingleSelectedItem();
        if (category == null) {
            categoryUuid = StringUtils.EMPTY;
        } else {
            categoryUuid = category.getId();
        }

        UI ui = UI.getCurrent();
        routeSupport.setQueryParameter(ui, SELECTED_CATEGORY_QUERY_PARAMETER, categoryUuid);
    }

    @Install(to = "attributesTable.create", subject = "routeParametersProvider")
    protected RouteParameters attributesTableCreateActionRouteParametersProvider() {
        Category category = categoriesGrid.getSingleSelectedItem();
        if (category == null) {
            return RouteParameters.empty();
        }

        RouteParam categoryParam = new RouteParam(
                CATEGORY_ID_ROUTE_PARAMETER, urlParamSerializer.serialize(category.getId())
        );
        RouteParam categoryAttributeParam = new RouteParam(
                StandardDetailView.DEFAULT_ROUTE_PARAM, StandardDetailView.NEW_ENTITY_ID
        );

        return new RouteParameters(categoryParam, categoryAttributeParam);
    }

    @Install(to = "attributesTable.edit", subject = "routeParametersProvider")
    protected RouteParameters attributesTableEditActionRouteParametersProvider() {
        Category category = categoriesGrid.getSingleSelectedItem();
        CategoryAttribute categoryAttribute = attributesTable.getSingleSelectedItem();
        if (category == null || categoryAttribute == null) {
            return RouteParameters.empty();
        }

        RouteParam categoryParam = new RouteParam(
                CATEGORY_ID_ROUTE_PARAMETER, urlParamSerializer.serialize(category.getId())
        );
        RouteParam categoryAttributeParam = new RouteParam(
                StandardDetailView.DEFAULT_ROUTE_PARAM, urlParamSerializer.serialize(categoryAttribute.getId())
        );

        return new RouteParameters(categoryParam, categoryAttributeParam);
    }

    @Subscribe("categoriesGrid.exportJSON")
    public void onExportBtnExportJSON(ActionPerformedEvent event) {
        export(JSON);
    }

    @Subscribe("categoriesGrid.exportZIP")
    public void onExportBtnExportZIP(ActionPerformedEvent event) {
        export(ZIP);
    }

    protected void export(DownloadFormat downloadFormat) {
        Collection<Category> selected = categoriesGrid.getSelectedItems();
        if (selected.isEmpty() && categoriesGrid.getItems() != null) {
            selected = categoriesGrid.getItems().getItems();
        }

        if (selected.isEmpty()) {
            notifications.create(messages.getMessage(CategoryListView.class, "nothingToExport"))
                    .withType(Notifications.Type.DEFAULT)
                    .show();
            return;
        }

        try {
            byte[] data;
            if (downloadFormat == JSON) {
                data = entityImportExport.exportEntitiesToJSON(new ArrayList<>(selected), buildExportFetchPlan()).getBytes(StandardCharsets.UTF_8);
            } else {
                data = entityImportExport.exportEntitiesToZIP(new ArrayList<>(selected), buildExportFetchPlan());
            }
            downloader.download(data, String.format("Categories.%s", downloadFormat.getFileExt()), downloadFormat);
        } catch (Exception e) {
            log.warn("Unable to export categories", e);
            notifications.create(messages.getMessage(CategoryListView.class, "exportFailed"), e.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected FetchPlan buildExportFetchPlan() {
        return fetchPlans.builder(Category.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("categoryAttrs", FetchPlan.BASE)
                .build();
    }

    @Subscribe("importField")
    public void onImportFieldFileUploadSucceed(FileUploadSucceededEvent<FileUploadField> event) {
        try {
            byte[] bytes = importField.getValue();
            if (bytes == null) {
                throw new IllegalStateException("File data is empty");
            }
            Collection<Object> importedEntities;
            if (JSON.getFileExt().equals(Files.getFileExtension(event.getFileName()))) {
                importedEntities = entityImportExport.importEntitiesFromJson(new String(bytes, StandardCharsets.UTF_8), createEntityImportPlan());
            } else {
                importedEntities = entityImportExport.importEntitiesFromZIP(bytes, createEntityImportPlan());
            }

            if (!importedEntities.isEmpty()) {
                categoriesDl.load();
                notifications.create(messages.getMessage(CategoryListView.class, "importSuccessful"))
                        .withType(Notifications.Type.DEFAULT)
                        .show();
            }
        } catch (Exception e) {
            log.warn("Unable to import categories", e);
            notifications.create(messages.getMessage(CategoryListView.class, "importFailed"), e.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected EntityImportPlan createEntityImportPlan() {
        return entityImportPlans.builder(Category.class)
                .addLocalProperties()
                .addProperty(new EntityImportPlanProperty(
                        "categoryAttrs",
                        entityImportPlans.builder(CategoryAttribute.class)
                                .addLocalProperties()
                                .addEmbeddedProperty(
                                        "defaultEntity",
                                        entityImportPlans.builder(ReferenceToEntity.class)
                                                .addLocalProperties().
                                                build()
                                )
                                .build(),
                        CollectionImportPolicy.KEEP_ABSENT_ITEMS)
                )
                .build();
    }
}
