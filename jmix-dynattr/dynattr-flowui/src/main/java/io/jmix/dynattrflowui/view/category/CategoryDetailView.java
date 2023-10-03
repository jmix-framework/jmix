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

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattrflowui.utils.GridHelper;
import io.jmix.dynattrflowui.view.categoryattr.CategoryAttributesDetailView;
import io.jmix.dynattrflowui.view.localization.AttributeLocalizationComponent;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.Views;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredFieldsWarningInspection"})
@Route(value = "dynat/category/:id", layout = DefaultMainViewParent.class)
@ViewController("dynat_CategoryView.detail")
@ViewDescriptor("category-detail-view.xml")
@PrimaryDetailView(Category.class)
@EditedEntityContainer("categoryDc")
@DialogMode(width = "50em", height = "37.5em")
public class CategoryDetailView extends StandardDetailView<Category> {
    private static final List<Integer> COL_POSITIONS = IntStream.range(1, 5).boxed().toList();

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Views views;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    private Metadata metadata;
    @Autowired
    private AccessManager accessManager;
    @Autowired
    protected Messages messages;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected ReferenceToEntitySupport referenceToEntitySupport;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected GridHelper gridHelper;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected MsgBundleTools msgBundleTools;

    @ViewComponent
    protected InstanceContainer<Category> categoryDc;
    @ViewComponent
    protected CollectionContainer<CategoryAttribute> categoryAttributesDc;
    @ViewComponent
    protected JmixComboBox<MetaClass> entityTypeField;
    @ViewComponent
    protected JmixTabSheet tabSheet;
    @ViewComponent
    protected VerticalLayout localizationTabContainer;
    @ViewComponent
    protected HorizontalLayout attributesLocationTabContainer;
    @ViewComponent
    protected JmixComboBox<Integer> columnsCountLookupField;
    @ViewComponent
    protected DataGrid<CategoryAttribute> categoryAttrsGrid;
    @ViewComponent
    protected Button moveUpBtn;
    @ViewComponent
    protected Button moveDownBtn;

    protected AttributeLocalizationComponent localizationFragment;

    protected Map<Integer, List<CategoryAttribute>> attributesLocationMapping = new HashMap<>();
    protected Map<String, GridListDataView<CategoryAttribute>> attributesViewsLocationMapping = new HashMap<>();

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        columnsCountLookupField.setItems(COL_POSITIONS);
        columnsCountLookupField.addValueChangeListener(e -> {
            initLocationTab();
        });
        columnsCountLookupField.setValue(categoryAttributesDc.getItems()
                .stream()
                .filter(item -> item.getConfiguration().getColumnNumber() != null)
                .mapToInt(item -> item.getConfiguration().getColumnNumber())
                .max()
                .orElse(1));

        initLocationTab();
        initEntityTypeField();
        initLocalizationTab();
        setupFieldsLock();
    }

    protected void initLocationTab() {
        int size = columnsCountLookupField.getValue() == null ? 1 : columnsCountLookupField.getValue();
        if (size < attributesLocationMapping.size()) {
            unstashUnusedAttributes(size);
        }
        dislocateAttributesForMapping();
        attributesLocationTabContainer.removeAll();

        VerticalLayout sourceGrid = createGrid("0", "SourceGrid", attributesLocationMapping.get(0), "320px");

        HorizontalLayout targetsGridContainer = new HorizontalLayout();
        targetsGridContainer.setPadding(false);
        targetsGridContainer.setMargin(false);
        targetsGridContainer.setSpacing(false);

        for (int i = 1; i <= size; i++) {
            attributesLocationMapping.computeIfAbsent(i, k -> new ArrayList<>());
            targetsGridContainer.add(createGrid(String.valueOf(i), "Column " + i, attributesLocationMapping.get(i), "200px"));
        }
        attributesLocationTabContainer.add(sourceGrid, targetsGridContainer);
    }

    private void unstashUnusedAttributes(int size) {
        for (int i = ++size; i < attributesLocationMapping.size(); i++) {
            attributesLocationMapping.get(i).forEach(elem -> {
                elem.getConfiguration().setRowNumber(null);
                elem.getConfiguration().setColumnNumber(null);
            });
            attributesLocationMapping.remove(i);
            attributesViewsLocationMapping.remove(String.valueOf(i));
        }
    }

    private void dislocateAttributesForMapping() {
        attributesLocationMapping = new HashMap<>();
        attributesViewsLocationMapping = new HashMap<>();
        categoryAttributesDc.getItems()
                .forEach(categoryAttribute -> {
                    if (categoryAttribute.getConfiguration().getColumnNumber() == null) {
                        if (!attributesLocationMapping.containsKey(0)) {
                            attributesLocationMapping.put(0, new ArrayList<>());
                        }
                        attributesLocationMapping.get(0).add(categoryAttribute);
                    } else {
                        int colNumber = categoryAttribute.getConfiguration().getColumnNumber();
                        if (!attributesLocationMapping.containsKey(colNumber)) {
                            attributesLocationMapping.put(colNumber, new ArrayList<>());
                        }
                        attributesLocationMapping.get(colNumber).add(categoryAttribute);
                    }
                });

        if (!attributesLocationMapping.containsKey(0)) {
            attributesLocationMapping.put(0, new ArrayList<>());
        }
        attributesLocationMapping.values()
                .forEach(list -> {
                    list.sort((left, right) -> {
                        if (left.getConfiguration().getRowNumber() == null && right.getConfiguration().getRowNumber() == null) {
                            return 0;
                        }
                        if (left.getConfiguration().getRowNumber() == null && right.getConfiguration().getRowNumber() != null) {
                            return -1;
                        }
                        if (left.getConfiguration().getRowNumber() != null && right.getConfiguration().getRowNumber() == null) {
                            return 1;
                        }
                        return left.getConfiguration().getRowNumber() - right.getConfiguration().getRowNumber();
                    });
                });
    }

    protected VerticalLayout createGrid(String id, String girdName, List<CategoryAttribute> sourceItems, String width) {
        Grid<CategoryAttribute> grid = new Grid<>(CategoryAttribute.class, false);
        grid.setWidth(width);
        grid.addColumn("name");
        grid.setId(id);

        GridListDataView<CategoryAttribute> dataView = grid.setItems(sourceItems);
        attributesViewsLocationMapping.put(id, dataView);

        grid.setDropMode(GridDropMode.BETWEEN);
        grid.setRowsDraggable(true);

        AtomicReference<CategoryAttribute> draggedItem = new AtomicReference<>();
        grid.addColumnReorderListener(e -> snapshotLocation());
        grid.addDragStartListener(
                e -> draggedItem.set(e.getDraggedItems().get(0)));

        grid.addDropListener(e -> {

            Optional<CategoryAttribute> targetVal = attributesLocationMapping.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(item -> item.getName().equals(e.getDataTransferData().get("text/plain")))
                    .findFirst();
            boolean isSameGrid = e.getDropTargetItem().isPresent() &&
                    targetVal.isPresent() &&
                    Objects.equals(targetVal.get().getConfiguration().getColumnNumber(),
                            e.getDropTargetItem().get().getConfiguration().getColumnNumber());
            if (isSameGrid && draggedItem.get() != null) {

                CategoryAttribute targetAttr = e.getDropTargetItem().orElseThrow();
                GridDropLocation dropLocation = e.getDropLocation();

                boolean personWasDroppedOntoItself = draggedItem.get().equals(targetAttr);

                if (personWasDroppedOntoItself) {
                    return;
                }

                dataView.removeItem(draggedItem.get());

                if (dropLocation == GridDropLocation.BELOW) {
                    dataView.addItemAfter(draggedItem.get(), targetAttr);
                } else {
                    dataView.addItemBefore(draggedItem.get(), targetAttr);
                }
                draggedItem.set(null);
            } else {
                CategoryAttribute next = targetVal.orElseThrow();
                for (Map.Entry<Integer, List<CategoryAttribute>> item : attributesLocationMapping.entrySet()) {
                    if (item.getValue().contains(next)) {
                        attributesViewsLocationMapping.get(item.getKey().toString()).removeItem(next);
                        attributesViewsLocationMapping.get(item.getKey().toString()).refreshAll();
                    }
                }
                e.getSource().getListDataView().addItem(next);
            }
            snapshotLocation();
        });

        H4 targetGridLabel = new H4(girdName);

        VerticalLayout sourceGridLayout = new VerticalLayout();
        sourceGridLayout.setSpacing(false);
        sourceGridLayout.setPadding(false);
        sourceGridLayout.setMargin(false);

        sourceGridLayout.add(targetGridLabel, grid);
        return sourceGridLayout;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void snapshotLocation() {
        for (Map.Entry<Integer, List<CategoryAttribute>> listEntry : attributesLocationMapping.entrySet()) {
            List<CategoryAttribute> list = listEntry.getValue();
            Integer columnIndex = listEntry.getKey();
            for (int i = 0; i < list.size(); i++) {
                CategoryAttribute item = list.get(i);
                item.getConfiguration().setId(String.valueOf(UUID.randomUUID()));
                item.getConfiguration().setRowNumber(columnIndex == 0 ? null : i);
                item.getConfiguration().setColumnNumber(columnIndex == 0 ? null : columnIndex);
                getViewData().getDataContext().merge(item);
                getViewData().getDataContext().merge(item.getConfiguration());
                getViewData().getDataContext().setModified(item, true);
                getViewData().getDataContext().setModified(item.getConfiguration(), true);
            }
        }
    }


    protected void setupFieldsLock() {
        CrudEntityContext crudEntityContext = new CrudEntityContext(categoryDc.getEntityMetaClass());
        accessManager.applyRegisteredConstraints(crudEntityContext);
        if (!crudEntityContext.isUpdatePermitted()) {
            entityTypeField.setEnabled(false);
        }
    }

    @Subscribe("entityTypeField")
    protected void onEntityTypeFieldValueChange(HasValue.ValueChangeEvent<MetaClass> event) {
        if (event.getValue() != null) {
            getEditedEntity().setEntityType(event.getValue().getName());
        }
    }

    @Subscribe("isDefaultField")
    protected void onIsDefaultFieldValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (Boolean.TRUE.equals(event.getValue())) {
            FetchPlan fetchPlan = fetchPlans.builder(Category.class)
                    .add("isDefault")
                    .build();
            LoadContext<Category> loadContext = new LoadContext<Category>(metadata.getClass(Category.class))
                    .setFetchPlan(fetchPlan);
            Category category = getEditedEntity();
            loadContext.setQueryString("select c from dynat_Category c where c.entityType = :entityType and not c.id = :id")
                    .setParameter("entityType", category.getEntityType())
                    .setParameter("id", category.getId());
            List<Category> foundCategories = dataManager.loadList(loadContext);
            foundCategories.forEach(item -> item.setIsDefault(false));

            dataManager.save(new SaveContext().saving(foundCategories));
        }
    }

    protected void initEntityTypeField() {
        Map<String, MetaClass> options = new TreeMap<>(); //the map sorts metaclasses by the string key
        for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
            if (metadataTools.hasCompositePrimaryKey(metaClass) && !metadataTools.hasUuid(metaClass)) {
                continue;
            }
            options.put(messageTools.getDetailedEntityCaption(metaClass), metaClass);
        }
        ComponentUtils.setItemsMap(entityTypeField, options.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));
        entityTypeField.addValueChangeListener(e -> getEditedEntity().setEntityType(e.getValue().getName()));
        if (getEditedEntity().getEntityType() != null) {
            entityTypeField.setValue(extendedEntities.getEffectiveMetaClass(getEditedEntity().getEntityType()));
        }
    }

    protected void initLocalizationTab() {
        if (coreProperties.getAvailableLocales().size() > 1) {
            Tab localizationTab = tabSheet.getTabAt(1); // 0 == "localizationTab
            localizationTab.setVisible(true);

            CrudEntityContext crudEntityContext = new CrudEntityContext(categoryDc.getEntityMetaClass());
            accessManager.applyRegisteredConstraints(crudEntityContext);

            localizationFragment = new AttributeLocalizationComponent(coreProperties,
                    msgBundleTools,
                    metadata,
                    messages,
                    messageTools,
                    uiComponents,
                    dataComponents,
                    getViewData().getDataContext());

            localizationFragment.setNameMsgBundle(getEditedEntity().getLocaleNames());
            localizationFragment.removeDescriptionColumn();
            localizationFragment.setEnabled(crudEntityContext.isUpdatePermitted());

            localizationTabContainer.add(localizationFragment);
            localizationTabContainer.expand(localizationFragment);
        }
    }


    @Subscribe
    public void onInitEvent(InitEvent event) {
        sortCategoryAttrsGridByOrderNo();
        categoryAttrsGrid
                .addColumn(createCategoryAttrsGridDataTypeRenderer())
                .setHeader(messageTools.getPropertyCaption(metadata.getClass(CategoryAttribute.class), "dataType"));

        categoryAttrsGrid
                .addColumn(createCategoryAttrsGridDefaultValueRenderer())
                .setHeader(messages.getMessage(getClass(), "categoryAttrsGrid.defaultValue"));

    }

    protected ComponentRenderer<Span, CategoryAttribute> createCategoryAttrsGridDefaultValueRenderer() {
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
                    String dateTimeFormat = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getDateTimeFormat();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
                    defaultValue = simpleDateFormat.format(dateTime);
                } else if (BooleanUtils.isTrue(attribute.getDefaultDateIsCurrent())) {
                    defaultValue = messages.getMessage(getClass(), "categoryAttrsGrid.currentDate");
                }
            }
            case DATE_WITHOUT_TIME -> {
                LocalDate dateWoTime = attribute.getDefaultDateWithoutTime();
                if (dateWoTime != null) {
                    String dateWoTimeFormat = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getDateFormat();
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

    protected ComponentRenderer<Text, CategoryAttribute> createCategoryAttrsGridDataTypeRenderer() {
        return new ComponentRenderer<>(this::categoryAttrsGridDataTypeComponent,
                this::categoryAttrsGridDataTypeUpdater);
    }

    protected Text categoryAttrsGridDataTypeComponent() {
        return new Text(null);
    }

    protected void categoryAttrsGridDataTypeUpdater(Text text, CategoryAttribute categoryAttribute) {
        String dataType;
        if (BooleanUtils.isTrue(categoryAttribute.getIsEntity())) {
            Class<?> javaType = categoryAttribute.getJavaType();
            if (javaType != null) {
                MetaClass metaClass = metadata.getClass(javaType);
                dataType = messageTools.getEntityCaption(metaClass);
            } else {
                dataType = messages.getMessage("classNotFound");
            }
        } else {
            String key = AttributeType.class.getSimpleName() + "." + categoryAttribute.getDataType().toString();
            dataType = messages.getMessage(AttributeType.class, key);
        }

        text.setText(dataType);
    }

    @Subscribe("categoryAttrsGrid.create")
    protected void categoryAttrsGridCreateListener(ActionPerformedEvent event) {
        dialogWindows.detail(this, CategoryAttribute.class)
                .withViewClass(CategoryAttributesDetailView.class)
                .newEntity()
                .withParentDataContext(getViewData().getDataContext())
                .withInitializer(e -> e.setCategory(categoryDc.getItem()))
                .build()
                .open();
    }

    @Subscribe("categoryAttrsGrid.edit")
    protected void categoryAttrsGridEditListener(ActionPerformedEvent event) {
        CategoryAttribute categoryAttributeSelected = categoryAttrsGrid.getSingleSelectedItem();

        Assert.notNull(categoryAttributeSelected, "Selected attribute has to be not null");
        dialogWindows.detail(this, CategoryAttribute.class)
                .withViewClass(CategoryAttributesDetailView.class)
                .editEntity(categoryAttributeSelected)
                .withParentDataContext(getViewData().getDataContext())
                .build()
                .open();
    }

    @Subscribe("categoryAttrsGrid.remove")
    protected void categoryAttrsGridRemoveListener(ActionPerformedEvent event) {
        CategoryAttribute selected = Objects.requireNonNull(categoryAttrsGrid.getSingleSelectedItem());
        categoryAttributesDc.getMutableItems().remove(selected);
        getViewData().getDataContext().remove(selected);
        categoryAttrsGrid.getDataProvider().refreshAll();
    }


    @Subscribe("categoryAttrsGrid")
    protected void onCategoryAttrsGridSelection(SelectionEvent<DataGrid<CategoryAttribute>, CategoryAttribute> event) {
        Set<CategoryAttribute> selected = categoryAttrsGrid.getSelectedItems();
        if (selected.isEmpty()) {
            refreshMoveButtonsEnabled(null);
        } else {
            refreshMoveButtonsEnabled(selected.iterator().next());
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Subscribe("categoryAttrsGrid.moveUp")
    protected void onCategoryAttrsGridMoveUp(ActionPerformedEvent event) {
        gridHelper.moveTableItemUp(categoryAttributesDc, categoryAttrsGrid, () ->
                categoryAttributesDc.getMutableItems().forEach(item -> {
                    item.setOrderNo(categoryAttributesDc.getMutableItems().indexOf(item));
                    getViewData().getDataContext().setModified(item, true);
                    getViewData().getDataContext().merge(item);
                }));

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Subscribe("categoryAttrsGrid.moveDown")
    protected void onCategoryAttrsGridMoveDown(ActionPerformedEvent event) {
        gridHelper.moveTableItemDown(categoryAttributesDc, categoryAttrsGrid, () ->
                categoryAttributesDc.getMutableItems().forEach(item -> {
                    item.setOrderNo(categoryAttributesDc.getMutableItems().indexOf(item));
                    getViewData().getDataContext().setModified(item, true);
                    getViewData().getDataContext().merge(item);
                }));
    }

    protected CategoryAttribute getPrevAttribute(Integer orderNo) {
        return categoryAttributesDc.getMutableItems()
                .stream()
                .filter(categoryAttribute -> orderNo.compareTo(categoryAttribute.getOrderNo()) > 0)
                .max(Comparator.comparing(CategoryAttribute::getOrderNo))
                .orElse(null);
    }

    protected CategoryAttribute getNextAttribute(Integer orderNo) {
        return categoryAttributesDc.getMutableItems()
                .stream()
                .filter(categoryAttribute -> orderNo.compareTo(categoryAttribute.getOrderNo()) < 0)
                .min(Comparator.comparing(CategoryAttribute::getOrderNo))
                .orElse(null);
    }

    protected void sortCategoryAttrsGridByOrderNo() {
        Objects.requireNonNull(categoryAttributesDc.getSorter())
                .sort(Sort.by(Sort.Direction.ASC, "orderNo"));
    }

    protected void refreshMoveButtonsEnabled(@Nullable CategoryAttribute categoryAttribute) {
        moveUpBtn.setEnabled(categoryAttribute != null && getPrevAttribute(categoryAttribute.getOrderNo()) != null);
        moveDownBtn.setEnabled(categoryAttribute != null && getNextAttribute(categoryAttribute.getOrderNo()) != null);
    }

    public void setCategory(Category category) {
        categoryDc.setItem(category);
        categoryAttributesDc.setItems(dataManager.load(CategoryAttribute.class)
                .query("select e from dynat_CategoryAttribute e where e.category = :category")
                .parameter("category", category)
                .list());
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPreCommit(DataContext.PreSaveEvent event) {
        snapshotLocation();
        if (localizationFragment != null) {
            getEditedEntity().setLocaleNames(localizationFragment.getNameMsgBundle());
        }
    }
}
