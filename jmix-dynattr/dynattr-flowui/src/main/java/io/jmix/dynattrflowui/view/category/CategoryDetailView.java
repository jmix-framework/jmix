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
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
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
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattrflowui.impl.model.AttributeLocalizedValue;
import io.jmix.dynattrflowui.utils.DataProviderUtils;
import io.jmix.dynattrflowui.utils.DynAttrUiHelper;
import io.jmix.dynattrflowui.view.categoryattr.CategoryAttributesDetailView;
import io.jmix.dynattrflowui.view.localization.AttributeLocalizationComponent;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.Views;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
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
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredFieldsWarningInspection"})
@Route(value = "dynat/category/:id", layout = DefaultMainViewParent.class)
@ViewController("dynat_CategoryView.detail")
@ViewDescriptor("category-detail-view.xml")
@PrimaryDetailView(Category.class)
@EditedEntityContainer("categoryDc")
@DialogMode(width = "50em", height = "37.5em")
public class CategoryDetailView extends StandardDetailView<Category> {
    private static final int LOWEST_COUNT = 1;
    private static final int HIGHEST_COUNT = 5;
    private static final List<Integer> COL_POSITIONS = IntStream.range(LOWEST_COUNT, HIGHEST_COUNT).boxed().toList();
    private static final String DND_CONTENT_ROW_KEY = "text/plain";
    private static final String NAME_COLUMN = "name";

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected DataManager dataManager;
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
    protected DynAttrUiHelper dynAttrUiHelper;
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
    protected DataGrid<CategoryAttribute> categoryAttrsGrid;
    @ViewComponent
    protected Button moveUpBtn;
    @ViewComponent
    protected Button moveDownBtn;
    @ViewComponent("categoryAttrsGrid.edit")
    protected Action editAction;
    @ViewComponent("categoryAttrsGrid.remove")
    protected Action removeAction;
    @ViewComponent("categoryAttrsGrid.moveUp")
    protected Action moveUpAction;
    @ViewComponent("categoryAttrsGrid.moveDown")
    protected Action moveDownAction;


    protected AttributeLocalizationComponent localizationFragment;

    protected Map<Integer, List<CategoryAttribute>> attributesLocationMapping = new HashMap<>();
    protected Map<String, GridListDataView<CategoryAttribute>> attributesViewsLocationMapping = new HashMap<>();

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

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initActionsVisibleListener();
        initEntityTypeField();
        initLocalizationTab();
        setupFieldsLock();
        setGridActionsEnabled(!categoryAttrsGrid.getSelectedItems().isEmpty());
    }

    private void initActionsVisibleListener() {
        categoryAttrsGrid.addSelectionListener(e -> setGridActionsEnabled(!e.getAllSelectedItems().isEmpty()));
    }

    private void setGridActionsEnabled(boolean enabled) {
        List.of(editAction, removeAction, moveUpAction, moveDownAction)
                .forEach(button -> button.setEnabled(enabled));
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
        Map<MetaClass, String> options = new HashMap<>(); //the map sorts metaclasses by the string key
        for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
            if (metadataTools.hasCompositePrimaryKey(metaClass) && !metadataTools.hasUuid(metaClass)) {
                continue;
            }
            options.put(metaClass, messageTools.getDetailedEntityCaption(metaClass));
        }
        entityTypeField.setItemLabelGenerator(options::get);
        //noinspection unchecked
        entityTypeField.setItems(DataProviderUtils.createCallbackDataProvider(options.keySet().stream()
                .sorted(Comparator.comparing(MetadataObject::getName))
                .toList()));

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
                dataType = "";
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
                .withAfterCloseListener(e -> {
                    // todo also bug caused by data components
                    if (e.getCloseAction().equals(StandardOutcome.SAVE.getCloseAction())) {
                        categoryAttributesDc.replaceItem(e.getView().getEditedEntity());
                        categoryAttrsGrid.getDataProvider().refreshAll();
                    }
                })
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
                .withAfterCloseListener(e -> {
                    if (e.getCloseAction().equals(StandardOutcome.SAVE.getCloseAction())) {
                        categoryAttributesDc.replaceItem(e.getView().getEditedEntity());
                        categoryAttrsGrid.getDataProvider().refreshAll();
                    }
                })
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
        dynAttrUiHelper.moveTableItemUp(categoryAttributesDc, categoryAttrsGrid, () ->
                categoryAttributesDc.getMutableItems().forEach(item -> {
                    item.setOrderNo(categoryAttributesDc.getMutableItems().indexOf(item));
                    getViewData().getDataContext().setModified(item, true);
                    getViewData().getDataContext().merge(item);
                }));

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Subscribe("categoryAttrsGrid.moveDown")
    protected void onCategoryAttrsGridMoveDown(ActionPerformedEvent event) {
        dynAttrUiHelper.moveTableItemDown(categoryAttributesDc, categoryAttrsGrid, () ->
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
        if (localizationFragment != null) {
            getEditedEntity().setLocaleNames(localizationFragment.getNameMsgBundle());
        }
    }

    public Category getCategory() {
        return getEditedEntity();
    }
}
