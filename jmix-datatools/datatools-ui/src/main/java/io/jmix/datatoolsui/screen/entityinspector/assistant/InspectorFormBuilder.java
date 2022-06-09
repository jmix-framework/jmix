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

package io.jmix.datatoolsui.screen.entityinspector.assistant;

import io.jmix.core.*;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.datatoolsui.screen.entityinspector.EntityInspectorBrowser;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.EntityLookupAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.Convert;
import java.util.Arrays;
import java.util.List;

import static io.jmix.datatoolsui.screen.entityinspector.EntityFormUtils.*;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component("datatl_EntityInspectorFormBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InspectorFormBuilder {

    public static final int MAX_TEXTFIELD_STRING_LENGTH = 255;

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;
    @Autowired
    protected Actions actions;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected AccessManager accessManager;

    private final InstanceContainer container;

    private String caption = null;
    private Integer maxCaptionLength = 50;
    private Integer captionWidth = 200;
    private String fieldWidth = "400px";
    private List<String> disabledProperties;
    private io.jmix.ui.component.Component ownerComponent;

    public static InspectorFormBuilder from(ApplicationContext applicationContext, InstanceContainer container) {
        return applicationContext.getBean(InspectorFormBuilder.class, container);
    }

    protected InspectorFormBuilder(InstanceContainer container) {
        this.container = container;
    }

    public InspectorFormBuilder withCaption(String caption) {
        this.caption = caption;
        return this;
    }

    public InspectorFormBuilder withMaxCaptionLength(Integer captionLength) {
        this.maxCaptionLength = captionLength;
        return this;
    }

    public InspectorFormBuilder withCaptionWidth(Integer captionWidth) {
        this.captionWidth = captionWidth;
        return this;
    }

    public InspectorFormBuilder withFieldWidth(String width) {
        this.fieldWidth = width;
        return this;
    }

    public InspectorFormBuilder withDisabledProperties(String... properties) {
        this.disabledProperties = Arrays.asList(properties);
        return this;
    }

    public InspectorFormBuilder withOwnerComponent(io.jmix.ui.component.Component component) {
        this.ownerComponent = component;
        return this;
    }

    public Form build() {
        MetaClass metaClass = container.getEntityMetaClass();
        Object item = getItem();

        Form form = uiComponents.create(Form.class);
        if (ownerComponent != null) {
            ((ComponentContainer) ownerComponent).add(form);
        }

        if (captionWidth != null) {
            form.setChildrenCaptionWidth(captionWidth);
        }
        if (caption != null) {
            form.setCaption(caption);
        }

        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(metaClass);

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            boolean isReadonly = metaProperty.isReadOnly();
            switch (metaProperty.getType()) {
                case DATATYPE:
                case ENUM:
                    boolean includeId = primaryKeyProperty != null
                            && primaryKeyProperty.equals(metaProperty)
                            && String.class.equals(metaProperty.getJavaType());
                    //skip system properties
                    if (metadataTools.isSystem(metaProperty) && !includeId) {
                        continue;
                    }
                    if (metaProperty.getType() != MetaProperty.Type.ENUM
                            && (isByteArray(metaProperty) || isUuid(metaProperty))) {
                        continue;
                    }
                    if (metadataTools.isJpa(metaProperty) && metadataTools.isAnnotationPresent(item, metaProperty.getName(), Convert.class)) {
                        continue;
                    }

                    if (includeId && !entityStates.isNew(item)) {
                        isReadonly = true;
                    }

                    addField(container, form, metaProperty, isReadonly);
                    break;
                case COMPOSITION:
                case ASSOCIATION:
                    if (!isMany(metaProperty)) {
                        addField(container, form, metaProperty, isReadonly);
                    }
                    break;
                default:
                    break;
            }
        }
        return form;
    }

    private Object getItem() {
        return container.getItem();
    }

    /**
     * Adds field to the specified form.
     * If the field should be custom, adds it to the specified customFields collection
     * which can be used later to create fieldGenerators
     *
     * @param metaProperty meta property of the item's property which field is creating
     * @param form         field group to which created field will be added
     */
    protected void addField(InstanceContainer container, Form form, MetaProperty metaProperty, boolean isReadonly) {
        MetaClass metaClass = container.getEntityMetaClass();
        Range range = metaProperty.getRange();

        boolean isRequired = isRequired(metaProperty);

        UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(metaClass, metaProperty.getName());
        accessManager.applyRegisteredConstraints(attributeContext);

        if (!attributeContext.canView())
            return;

        if (range.isClass()) {
            UiEntityContext entityContext = new UiEntityContext(range.asClass());
            accessManager.applyRegisteredConstraints(entityContext);
            if (!entityContext.isViewPermitted()) {
                return;
            }
        }

        ValueSource valueSource = new ContainerValueSource<>(container, metaProperty.getName());

        ComponentGenerationContext componentContext =
                new ComponentGenerationContext(metaClass, metaProperty.getName());
        componentContext.setValueSource(valueSource);

        Field field = (Field) uiComponentsGenerator.generate(componentContext);

        if (requireTextArea(metaProperty, getItem(), MAX_TEXTFIELD_STRING_LENGTH)) {
            field = uiComponents.create(TextArea.NAME);
        }

        if (isBoolean(metaProperty)) {
            field = createBooleanField();
        }

        if (range.isClass()) {
            EntityPicker pickerField = uiComponents.create(EntityPicker.class);

            EntityLookupAction lookupAction = actions.create(EntityLookupAction.class);
            lookupAction.setScreenClass(EntityInspectorBrowser.class);
            lookupAction.setScreenOptionsSupplier(() -> new MapScreenOptions(
                    ParamsMap.of("entity", metaProperty.getRange().asClass().getName())));
            lookupAction.setOpenMode(OpenMode.THIS_TAB);

            pickerField.addAction(lookupAction);
            pickerField.addAction(actions.create(EntityClearAction.class));

            field = pickerField;
        }

        field.setValueSource(valueSource);
        field.setCaption(getPropertyCaption(metaClass, metaProperty));
        field.setRequired(isRequired);

        isReadonly = isReadonly || (disabledProperties != null && disabledProperties.contains(metaProperty.getName()));
        if (range.isClass() && !metadataTools.isEmbedded(metaProperty)) {
            field.setEditable(metadataTools.isOwningSide(metaProperty) && !isReadonly);
        } else {
            field.setEditable(!isReadonly);
        }

        field.setWidth(fieldWidth);

        if (isRequired) {
            field.setRequiredMessage(messageTools.getDefaultRequiredMessage(metaClass, metaProperty.getName()));
        }
        form.add(field);
    }

    private Field createBooleanField() {
        ComboBox field = uiComponents.create(ComboBox.NAME);
        field.setOptionsMap(ParamsMap.of(
                messages.getMessage("trueString"), Boolean.TRUE,
                messages.getMessage("falseString"), Boolean.FALSE));
        field.setTextInputAllowed(false);
        return field;
    }

    protected String getPropertyCaption(MetaClass metaClass, MetaProperty metaProperty) {
        String caption = messageTools.getPropertyCaption(metaClass, metaProperty.getName());
        if (caption.length() < maxCaptionLength) {
            return caption;
        } else {
            return caption.substring(0, maxCaptionLength);
        }
    }
}
