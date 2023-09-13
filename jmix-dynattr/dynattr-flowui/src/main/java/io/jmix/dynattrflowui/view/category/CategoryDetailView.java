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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattrflowui.view.categoryattr.CategoryAttributesViewFragment;
import io.jmix.dynattrflowui.view.localization.AttributeLocalizationViewFragment;
import io.jmix.dynattrflowui.view.location.AttributeLocationViewFragment;
import io.jmix.flowui.Views;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Route(value = "dynat/category/:id", layout = DefaultMainViewParent.class)
@ViewController("dynat_CategoryView.detail")
@ViewDescriptor("category-detail-view.xml")
@PrimaryDetailView(Category.class)
@EditedEntityContainer("categoryDc")
@DialogMode(width = "50em", height = "37.5em")
public class CategoryDetailView extends StandardDetailView<Category> {

    protected static final String ATTRIBUTES_LOCATION_TAB = "attributesLocationTab";

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

    @ViewComponent
    protected JmixComboBox<MetaClass> entityTypeField;
    @ViewComponent
    protected InstanceContainer<Category> categoryDc;
    @ViewComponent
    protected CollectionContainer<CategoryAttribute> categoryAttributesDc;
    @ViewComponent
    protected VerticalLayout categoryAttrsBox;
    @ViewComponent
    protected JmixTabSheet tabSheet;
    @ViewComponent
    protected VerticalLayout attributesLocationTabContainer;
    @ViewComponent
    protected VerticalLayout localizationTabContainer;


    protected AttributeLocalizationViewFragment localizationFragment;
    protected AttributeLocationViewFragment attributeLocationFragment;
    CategoryAttributesViewFragment categoryAttributesViewFragment;


    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        categoryAttributesViewFragment = views.create(CategoryAttributesViewFragment.class);
        categoryAttributesViewFragment.setCategory(this.getEditedEntity());
        categoryAttributesViewFragment.setDataContext(this.getViewData().getDataContext());
        categoryAttrsBox.add(categoryAttributesViewFragment);
        categoryAttrsBox.expand(categoryAttributesViewFragment);
        initEntityTypeField();
        initLocalizationTab();
        initAttributeLocationTab();
        setupFieldsLock();
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

    @Subscribe("tabSheet")
    protected void onTabSheetSelectedTabChange(JmixTabSheet.SelectedChangeEvent event) {
        String tabName = event.getSelectedTab().getId().orElseThrow();
        if (ATTRIBUTES_LOCATION_TAB.equals(tabName)) {
            attributeLocationFragment.setAttributes(categoryAttributesViewFragment.getAttributes());
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

            localizationFragment = views.create(AttributeLocalizationViewFragment.class);
            localizationFragment.setNameMsgBundle(getEditedEntity().getLocaleNames());
            localizationFragment.removeDescriptionColumn();
            localizationFragment.setEnabled(crudEntityContext.isUpdatePermitted());
            localizationFragment.setDataContext(getViewData().getDataContext());

            localizationTabContainer.add(localizationFragment);
            localizationTabContainer.expand(localizationFragment);
        }
    }

    protected void initAttributeLocationTab() {
        CrudEntityContext crudEntityContext = new CrudEntityContext(categoryDc.getEntityMetaClass());
        accessManager.applyRegisteredConstraints(crudEntityContext);

        attributeLocationFragment = views.create(AttributeLocationViewFragment.class);
        attributeLocationFragment.setAttributes(categoryAttributesViewFragment.getAttributes());
        attributeLocationFragment.setDataContext(getViewData().getDataContext());
        attributesLocationTabContainer.add(attributeLocationFragment);
        attributesLocationTabContainer.expand(attributeLocationFragment);
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPreCommit(DataContext.PreSaveEvent event) {
        if (localizationFragment != null) {
            getEditedEntity().setLocaleNames(localizationFragment.getNameMsgBundle());
        }
    }
}
