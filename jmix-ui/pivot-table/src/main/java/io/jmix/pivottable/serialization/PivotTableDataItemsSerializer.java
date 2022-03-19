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

package io.jmix.pivottable.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.pivottable.widget.serialization.PivotJsonSerializationContext;
import io.jmix.pivottable.widget.serialization.PivotTableSerializationContext;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.impl.EntityDataItem;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.Consumer;

@Component("ui_PivotTableDataItemsSerializer")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PivotTableDataItemsSerializer {

    protected Messages messages;
    protected MessageTools messageTools;
    protected Metadata metadata;
    protected CurrentAuthentication currentAuthentication;
    protected MetadataTools metadataTools;
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    public JsonArray serialize(List<DataItem> items, JsonSerializationContext context) {
        return serialize(items, context, null);
    }

    public JsonArray serialize(List<DataItem> items, JsonSerializationContext context,
                               Consumer<PivotTableSerializationContext> postSerializationHandler) {
        JsonArray serialized = new JsonArray();

        if (context instanceof PivotJsonSerializationContext) {
            PivotJsonSerializationContext pivotContext = (PivotJsonSerializationContext) context;
            for (DataItem item : items) {
                JsonObject itemElement = new JsonObject();
                for (String property : pivotContext.getProperties()) {
                    Object value = item.getValue(property);
                    addProperty(itemElement, property, value, pivotContext, item);
                }

                if (postSerializationHandler != null) {
                    postSerializationHandler.accept(new PivotTableSerializationContext(item, itemElement, pivotContext));
                }

                serialized.add(itemElement);
            }
        }

        return serialized;
    }

    protected void addProperty(JsonObject jsonObject, String property, Object value,
                               PivotJsonSerializationContext context, DataItem item) {
        Object formattedValue;
        if (value == null) {
            formattedValue = StringUtils.EMPTY;
        } else if (EntityValues.isEntity(value)) {
            formattedValue = metadataTools.getInstanceName(value);
        } else if (value instanceof Enum) {
            formattedValue = messages.getMessage((Enum) value);
        } else if (value instanceof Date
                || value instanceof Temporal) {
            formattedValue = getFormattedValueByEntityDatatype(item, property, value)
                    .orElse(getFormattedValueByClassDatatype(value, getUserLocale()));
        } else if (value instanceof Boolean) {
            formattedValue = BooleanUtils.isTrue((Boolean) value)
                    ? messages.getMessage("boolean.yes")
                    : messages.getMessage("boolean.no");
        } else if (value instanceof Collection) {
            throw new GuiDevelopmentException(String.format("'%s' cannot be added as a property, because " +
                    "PivotTable doesn't support collections as properties", property), "");
        } else {
            formattedValue = value;
        }

        jsonObject.add(context.getLocalizedPropertyName(property), context.serialize(formattedValue));
    }

    protected Optional<String> getFormattedValueByEntityDatatype(DataItem item, String property, Object value) {
        if (item instanceof EntityDataItem) {
            EntityDataItem entityItem = (EntityDataItem) item;
            MetaClass metaClass = metadata.getClass(entityItem.getItem());
            MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, property);
            if (mpp != null) {
                return Optional.of(mpp.getRange().asDatatype().format(value, getUserLocale()));
            }
        }
        return Optional.empty();
    }

    protected MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String property) {
        MetaPropertyPath mpp = metaClass.getPropertyPath(property);

        /*todo: if (mpp == null && DynamicAttributesUtils.isDynamicAttribute(property)) {
            mpp = DynamicAttributesUtils.getMetaPropertyPath(metaClass, property);
        }*/

        return mpp;
    }

    protected String getFormattedValueByClassDatatype(Object value, Locale locale) {
        Datatype<?> datatype = datatypeRegistry.get(value.getClass());
        return datatype.format(value, locale);
    }

    protected Locale getUserLocale() {
        return currentAuthentication.isSet() ? currentAuthentication.getLocale() : messageTools.getDefaultLocale();
    }
}
