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

package io.jmix.dynattrui.screen.category;

import io.jmix.core.*;
import io.jmix.core.entity.HasUuid;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.impl.model.Category;
import io.jmix.dynattr.impl.model.CategoryAttribute;
import io.jmix.dynattrui.screen.categoryattr.CategoryAttrsFragment;
import io.jmix.dynattrui.screen.localization.AttributeLocalizationFragment;
import io.jmix.dynattrui.screen.location.AttributeLocationFragment;
import io.jmix.ui.Fragments;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.TabSheet;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.LoadDataBeforeShow;
import io.jmix.ui.screen.StandardEditor;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.Target;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@UiController("sys_Category.edit")
@UiDescriptor("category-edit.xml")
@EditedEntityContainer("categoryDc")
@LoadDataBeforeShow
public class CategoryEdit extends StandardEditor<Category> {

    protected static final String ATTRIBUTES_LOCATION_TAB = "attributesLocationTab";

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected ComboBox<MetaClass> entityTypeField;
    @Autowired
    protected VBoxLayout categoryAttrsBox;
    @Autowired
    protected TabSheet tabSheet;

    @Autowired
    protected InstanceContainer<Category> categoryDc;
    @Autowired
    protected CollectionContainer<CategoryAttribute> categoryAttributesDc;

    protected AttributeLocalizationFragment localizationFragment;
    protected AttributeLocationFragment attributeLocationFragment;

    @Subscribe
    protected void onInit(InitEvent event) {
        CategoryAttrsFragment categoryAttrsFragment = fragments.create(this, CategoryAttrsFragment.class);
        Fragment fragment = categoryAttrsFragment.getFragment();
        categoryAttrsBox.add(fragment);
        categoryAttrsBox.expand(fragment);
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        initEntityTypeField();
        initLocalizationTab();
        initAttributeLocationTab();
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
            FetchPlan fetchPlan = FetchPlanBuilder.of(Category.class)
                    .add("isDefault")
                    .build();
            LoadContext<Category> lc = new LoadContext<>(Category.class)
                    .setFetchPlan(fetchPlan);
            Category category = getEditedEntity();
            lc.setQueryString("select c from sys_Category c where c.entityType = :entityType and not c.id = :id")
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
    protected void onTabSheetSelectedTabChange(TabSheet.SelectedTabChangeEvent event) {
        String tabName = event.getSelectedTab().getName();
        if (ATTRIBUTES_LOCATION_TAB.equals(tabName)) {
            attributeLocationFragment.setCategoryAttributes(new ArrayList<>(categoryAttributesDc.getItems()));
        }
    }

    protected void initEntityTypeField() {
        Map<String, MetaClass> options = new TreeMap<>();//the map sorts meta classes by the string key
        for (MetaClass metaClass : metadataTools.getAllPersistentMetaClasses()) {
            if (metadataTools.hasCompositePrimaryKey(metaClass) && !HasUuid.class.isAssignableFrom(metaClass.getJavaClass())) {
                continue;
            }
            options.put(messageTools.getDetailedEntityCaption(metaClass), metaClass);
        }
        entityTypeField.setOptionsMap(options);

        if (getEditedEntity().getEntityType() != null) {
            entityTypeField.setValue(extendedEntities.getEffectiveMetaClass(getEditedEntity().getEntityType()));
        }
    }

    protected void initLocalizationTab() {
        if (coreProperties.getAvailableLocales().size() > 1) {
            TabSheet.Tab localizationTab = tabSheet.getTab("localizationTab");
            localizationTab.setVisible(true);

            VBoxLayout localizationTabComponent = (VBoxLayout) tabSheet.getTabComponent("localizationTab");
            localizationFragment = fragments.create(this, AttributeLocalizationFragment.class);
            localizationFragment.setNameMsgBundle(getEditedEntity().getLocaleNames());

            Fragment fragment = localizationFragment.getFragment();
            fragment.setWidth(Component.FULL_SIZE);
            localizationTabComponent.add(fragment);
            localizationTabComponent.expand(fragment);
        }
    }

    protected void initAttributeLocationTab() {
        VBoxLayout attributesLocationTabComponent = (VBoxLayout) tabSheet.getTabComponent(ATTRIBUTES_LOCATION_TAB);
        attributeLocationFragment = fragments.create(this, AttributeLocationFragment.class);

        Fragment fragment = attributeLocationFragment.getFragment();
        fragment.setWidth(Component.FULL_SIZE);
        attributesLocationTabComponent.add(fragment);
        attributesLocationTabComponent.expand(fragment);
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPreCommit(DataContext.PreCommitEvent event) {
        if (localizationFragment != null) {
            getEditedEntity().setLocaleNames(localizationFragment.getNameMsgBundle());
        }
    }
}
