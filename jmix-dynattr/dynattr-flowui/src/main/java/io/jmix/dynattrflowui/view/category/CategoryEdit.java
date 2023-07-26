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
import com.vaadin.flow.component.tabs.TabSheet;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattrflowui.view.categoryattr.CategoryAttrsFragment;
import io.jmix.dynattrflowui.view.localization.AttributeLocalizationFragment;
import io.jmix.dynattrflowui.view.location.AttributeLocationFragment;
import io.jmix.flowui.Views;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ViewController("dynat_Category.edit")
@ViewDescriptor("category-edit.xml")
@EditedEntityContainer("categoryDc")
public class CategoryEdit extends StandardDetailView<Category> {

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
    protected JmixComboBox<MetaClass> entityTypeField;
    @Autowired
    protected VerticalLayout categoryAttrsBox;
    @Autowired
    protected JmixTabSheet tabSheet;

    @Autowired
    protected InstanceContainer<Category> categoryDc;
    @Autowired
    protected CollectionContainer<CategoryAttribute> categoryAttributesDc;
    @Autowired
    private Metadata metadata;

    protected AttributeLocalizationFragment localizationFragment;
    protected AttributeLocationFragment attributeLocationFragment;
    @Autowired
    private AccessManager accessManager;

    @Subscribe
    protected void onInit(InitEvent event) {
        CategoryAttrsFragment categoryAttrsFragment = views.create(CategoryAttrsFragment.class);
        categoryAttrsBox.add(categoryAttrsFragment);
        categoryAttrsBox.expand(categoryAttrsFragment);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
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
            LoadContext<Category> lc = new LoadContext(metadata.getClass(Category.class))
                    .setFetchPlan(fetchPlan);
            Category category = getEditedEntity();
            lc.setQueryString("select c from dynat_Category c where c.entityType = :entityType and not c.id = :id")
                    .setParameter("entityType", category.getEntityType())
                    .setParameter("id", category.getId());
            List<Category> result = dataManager.loadList(lc);
            for (Category cat : result) {
                cat.setIsDefault(false);
            }

            SaveContext saveContext = new SaveContext().saving(result);
            dataManager.save(saveContext);
        }
    }

    @Subscribe("tabSheet")
    protected void onTabSheetSelectedTabChange(TabSheet.SelectedChangeEvent event) {
        String tabName = event.getSelectedTab().getId().orElseThrow();
        if (ATTRIBUTES_LOCATION_TAB.equals(tabName)) {
            attributeLocationFragment.setCategoryAttributes(new ArrayList<>(categoryAttributesDc.getItems()));
        }
    }

    protected void initEntityTypeField() {
        Map<String, MetaClass> options = new TreeMap<>();//the map sorts meta classes by the string key
        for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
            if (metadataTools.hasCompositePrimaryKey(metaClass) && !metadataTools.hasUuid(metaClass)) {
                continue;
            }
            options.put(messageTools.getDetailedEntityCaption(metaClass), metaClass);
        }
        ComponentUtils.setItemsMap(entityTypeField, options.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));

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

            // todo
            VerticalLayout localizationTabComponent = (VerticalLayout) tabSheet.getContentByTab(tabSheet.getTabAt(0));
            localizationFragment = views.create(AttributeLocalizationFragment.class);
            localizationFragment.setNameMsgBundle(getEditedEntity().getLocaleNames());
            localizationFragment.setEnabled(crudEntityContext.isUpdatePermitted());

            localizationTabComponent.add(localizationFragment);
            localizationTabComponent.expand(localizationFragment);
        }
    }

    protected void initAttributeLocationTab() {
        CrudEntityContext crudEntityContext = new CrudEntityContext(categoryDc.getEntityMetaClass());
        accessManager.applyRegisteredConstraints(crudEntityContext);

        VerticalLayout attributesLocationTabComponent = (VerticalLayout) tabSheet.getContentByTab(tabSheet.getTabAt(1));  // 0 == "attributeLocationTab"
        attributeLocationFragment = views.create(AttributeLocationFragment.class);
        attributeLocationFragment.setEnabled(crudEntityContext.isUpdatePermitted());
        attributesLocationTabComponent.add(attributeLocationFragment);
        attributesLocationTabComponent.expand(attributeLocationFragment);
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPreCommit(BeforeSaveEvent event) { // todo was pre-commit
        if (localizationFragment != null) {
            getEditedEntity().setLocaleNames(localizationFragment.getNameMsgBundle());
        }
    }
}
