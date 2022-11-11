/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dynattrui.panel;

import com.google.common.base.Strings;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.CategoryDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.dynattr.model.Categorized;
import io.jmix.dynattr.model.Category;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.component.data.value.ContainerValueSourceProvider;
import io.jmix.ui.meta.CanvasIconSize;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.HasLoader;
import io.jmix.ui.model.InstanceContainer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.validation.constraints.Positive;
import java.util.*;
import java.util.stream.Collectors;

@StudioComponent(category = "Components",
        unsupportedProperties = {"enable", "responsive"},
        xmlns = "http://jmix.io/schema/dynattr/ui",
        xmlnsAlias = "dynattr",
        icon = "io/jmix/dynattrui/icon/component/dynamicAttributesPanel.svg",
        canvasIcon = "io/jmix/dynattrui/icon/component/dynamicAttributesPanel_canvas.svg",
        canvasIconSize = CanvasIconSize.LARGE)
@CompositeDescriptor("dynamic-attributes-panel.xml")
public class DynamicAttributesPanel extends CompositeComponent<VBoxLayout> implements Validatable, HasMargin {

    public static final String NAME = "dynamicAttributesPanel";

    public static final String DEFAULT_FIELD_WIDTH = null;

    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Messages messages;
    @Autowired
    protected DynAttrMetadata dynAttrMetadata;

    protected InstanceContainer<?> instanceContainer;

    protected Integer cols;
    protected Integer rows;
    protected String fieldWidth = DEFAULT_FIELD_WIDTH;
    protected String fieldCaptionWidth;

    protected Form propertiesForm;
    protected HBoxLayout categoryFieldBox;
    protected ComboBox<Category> categoryField;

    public DynamicAttributesPanel() {
        addCreateListener(this::onCreate);
    }

    protected void onCreate(CreateEvent createEvent) {
        categoryField = getInnerComponent("categoryField");
        categoryField.addValueChangeListener(e -> initPropertiesForm());

        propertiesForm = getInnerComponent("propertiesForm");
        propertiesForm.setHeightAuto();

        categoryFieldBox = getInnerComponent("categoryFieldBox");
        categoryField.setWidth(fieldWidth);
        getComposition().setMargin(false);
    }

    protected void initPropertiesForm() {
        propertiesForm.removeAll();

        Map<AttributeDefinition, Component> fields = new HashMap<>();
        for (AttributeDefinition attribute : getAttributesByCategory()) {
            Component resultComponent = generateFieldComponent(attribute);
            fields.put(attribute, resultComponent);
        }

        addFieldsToForm(propertiesForm, fields);
        initFieldCaptionWidth(propertiesForm);
    }

    protected void addFieldsToForm(Form newPropertiesForm, Map<AttributeDefinition, Component> fields) {
        if (fields.keySet().stream().anyMatch(attr -> attr.getConfiguration().getColumnNumber() != null
                && attr.getConfiguration().getRowNumber() != null)) {

            List<AttributeDefinition> attributesToAdd = fields.keySet().stream()
                    .filter(attr -> attr.getConfiguration().getColumnNumber() != null
                            && attr.getConfiguration().getRowNumber() != null)
                    .collect(Collectors.toList());

            int maxColumnIndex = attributesToAdd.stream()
                    .mapToInt(attr -> attr.getConfiguration().getColumnNumber())
                    .max()
                    .orElse(0);

            newPropertiesForm.setColumns(maxColumnIndex + 1);

            for (int i = 0; i <= maxColumnIndex; i++) {
                int columnIndex = i;
                List<AttributeDefinition> columnAttributes = attributesToAdd.stream()
                        .filter(attr -> columnIndex == attr.getConfiguration().getColumnNumber())
                        .sorted(Comparator.comparing(attr -> attr.getConfiguration().getRowNumber()))
                        .collect(Collectors.toList());

                int currentRowNumber = 0;
                for (AttributeDefinition attr : columnAttributes) {
                    while (attr.getConfiguration().getRowNumber() > currentRowNumber) {
                        //add empty row
                        newPropertiesForm.add(createEmptyComponent(), columnIndex, currentRowNumber);
                        currentRowNumber++;
                    }
                    newPropertiesForm.add(fields.get(attr), columnIndex, currentRowNumber);
                    currentRowNumber++;
                }
            }
        } else {
            int propertiesCount = getAttributesByCategory().size();
            int rowsPerColumn = getRowsPerColumn(propertiesCount);
            int columnNo = 0;
            int fieldsCount = 0;
            for (Component field : fields.values()) {
                fieldsCount++;
                newPropertiesForm.add(field, columnNo);
                if (fieldsCount % rowsPerColumn == 0) {
                    columnNo++;
                    newPropertiesForm.setColumns(columnNo + 1);
                }
            }
        }
    }

    private Component createEmptyComponent() {
        Label<String> component = uiComponents.create(Label.TYPE_STRING);
        component.setValue("\u2060");
        return component;
    }

    protected int getRowsPerColumn(int propertiesCount) {
        if (cols != null) {
            if (propertiesCount % cols == 0) {
                return propertiesCount / cols;
            }
            return propertiesCount / cols + 1;
        }
        if (rows != null) {
            return rows;
        }
        return propertiesCount;
    }

    protected Collection<AttributeDefinition> getAttributesByCategory() {
        Category category = categoryField.getValue();
        if (category != null) {
            for (CategoryDefinition categoryDefinition : dynAttrMetadata.getCategories(instanceContainer.getEntityMetaClass())) {
                if (category.equals(categoryDefinition.getSource())) {
                    return categoryDefinition.getAttributeDefinitions();
                }
            }
        }
        return Collections.emptyList();
    }

    protected Component generateFieldComponent(AttributeDefinition attribute) {
        MetaProperty metaProperty = attribute.getMetaProperty();
        ValueSource valueSource = new ContainerValueSource<>(instanceContainer, metaProperty.getName());

        ComponentGenerationContext componentContext =
                new ComponentGenerationContext(instanceContainer.getEntityMetaClass(), metaProperty.getName());
        componentContext.setValueSource(valueSource);

        Component resultComponent = uiComponentsGenerator.generate(componentContext);

        setWidth(resultComponent, attribute);

        return resultComponent;
    }

    protected void setWidth(Component component, AttributeDefinition attribute) {
        String formWidth = attribute.getConfiguration().getFormWidth();
        if (!Strings.isNullOrEmpty(formWidth)) {
            component.setWidth(formWidth);
        } else {
            component.setWidth(fieldWidth);
        }
    }

    protected void initCategoryField(InstanceContainer<?> instanceContainer) {
        if (!Categorized.class.isAssignableFrom(instanceContainer.getEntityMetaClass().getJavaClass())
                || instanceContainer.getEntityMetaClass().getPropertyPath("category") == null) {
            throw new DevelopmentException("Entity must implement 'io.jmix.dynattr.model.Categorized' and contain " +
                    "'category' attribute in order to use DynamicAttributesPanel.");
        }

        categoryField.setOptionsList(getCategoriesOptionsList());
        categoryField.setValueSource(new ContainerValueSource<>(instanceContainer, "category"));
    }

    @Nullable
    protected Category getDefaultCategory() {
        for (CategoryDefinition category : getCategoryDefinitions()) {
            if (category != null && category.isDefault()) {
                return (Category) category.getSource();
            }
        }
        return null;
    }

    protected Collection<CategoryDefinition> getCategoryDefinitions() {
        return dynAttrMetadata.getCategories(instanceContainer.getEntityMetaClass());
    }

    protected List<Category> getCategoriesOptionsList() {
        Collection<CategoryDefinition> options = getCategoryDefinitions();
        return options.stream().
                map(definition -> (Category) definition.getSource())
                .collect(Collectors.toList());
    }

    protected void initFieldCaptionWidth(Form newRuntimeForm) {
        if (fieldCaptionWidth != null) {
            SizeWithUnit sizeWithUnit = SizeWithUnit.parseStringSize(fieldCaptionWidth);
            if (SizeUnit.PERCENTAGE.equals(sizeWithUnit.getUnit())) {
                throw new IllegalStateException("DynamicAttributesPanel fieldCaptionWidth with '%' unit is unsupported");
            }
            newRuntimeForm.setChildrenCaptionWidth(Math.round(sizeWithUnit.getSize()));
        }
    }

    protected void onInstanceContainerItemChangeEvent(InstanceContainer.ItemChangeEvent<?> event) {
        if (event.getItem() instanceof Categorized
                && ((Categorized) event.getItem()).getCategory() == null) {
            ((Categorized) event.getItem()).setCategory(getDefaultCategory());
        }
        if (event.getItem() == null) {
            propertiesForm.removeAll();
        }
    }

    /**
     * Defines InstanceContainer for DynamicAttributesPanel.
     *
     * @param container {@link InstanceContainer} object with editing entity
     */
    @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF, required = true)
    public void setInstanceContainer(InstanceContainer<Object> container) {
        this.instanceContainer = container;
        propertiesForm.setValueSourceProvider(new ContainerValueSourceProvider<>(instanceContainer));
        initCategoryField(instanceContainer);
        initPropertiesForm();
        instanceContainer.addItemChangeListener(this::onInstanceContainerItemChangeEvent);
        if (instanceContainer instanceof HasLoader) {
            DataLoader loader = ((HasLoader) instanceContainer).getLoader();
            if (loader != null) {
                loader.setHint(DynAttrQueryHints.LOAD_DYN_ATTR, true);
            }
        }
    }

    /**
     * Sets the number of columns. If {@code null} value is passed, columns count will be determined
     * based on the {@code rows} parameter.
     *
     * @param cols positive integer or {@code null}
     */
    @StudioProperty(name = "cols")
    @Positive
    public void setColumnsCount(Integer cols) {
        if (cols != null && cols <= 0) {
            throw new GuiDevelopmentException(
                    "DynamicAttributesPanel element has incorrect value of the 'cols' attribute", this.id);
        }
        this.cols = cols;
    }

    /**
     * Sets the number of rows. This parameter will only be taken into account if {@code cols == null}.
     *
     * @param rows positive integer or {@code null}
     */
    @StudioProperty(name = "rows")
    @Positive
    public void setRowsCount(Integer rows) {
        if (rows != null && rows <= 0) {
            throw new GuiDevelopmentException(
                    "DynamicAttributesPanel element has incorrect value of the 'rows' attribute", this.id);
        }
        this.rows = rows;
    }

    /**
     * Sets the width of the fields. This parameter is used if some dynamic attribute does not have own width value.
     *
     * @param fieldWidth width of the fields
     */
    @StudioProperty(type = PropertyType.SIZE)
    public void setFieldWidth(String fieldWidth) {
        this.fieldWidth = fieldWidth;
    }


    /**
     * Sets the width of the fields caption. {@code fieldCaptionWidth} with '%' unit is unsupported.
     *
     * @param fieldCaptionWidth width of the fields caption
     */
    @StudioProperty(type = PropertyType.SIZE)
    public void setFieldCaptionWidth(String fieldCaptionWidth) {
        this.fieldCaptionWidth = fieldCaptionWidth;
    }

    /**
     * Sets visibility of the {@code CategoryField} component.
     *
     * @param visible visibility flag
     */
    public void setCategoryFieldVisible(boolean visible) {
        categoryFieldBox.setVisible(visible);
    }

    @Override
    public boolean isValid() {
        Collection<Component> components = ComponentsHelper.getComponents(propertiesForm);
        for (Component component : components) {
            if (component instanceof Validatable) {
                Validatable validatable = (Validatable) component;
                if (validatable.isValidateOnCommit() && !validatable.isValid())
                    return false;
            }
        }
        return true;
    }

    @Override
    public void validate() throws ValidationException {
        ComponentsHelper.traverseValidatable(propertiesForm, Validatable::validate);
    }

    @Override
    public void setMargin(MarginInfo marginInfo) {
        getComposition().setMargin(marginInfo);
    }

    @Override
    public MarginInfo getMargin() {
        return getComposition().getMargin();
    }
}