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

package io.jmix.reports.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.jmix.core.*;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import org.springframework.beans.factory.BeanFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static java.lang.String.format;

@NotThreadSafe
//todo add dynamic attributes support
//todo add not meta property objects support
public class GsonSerializationSupport {
    protected GsonBuilder gsonBuilder;
    protected Map<Object, Entity> processedObjects = new HashMap<>();
    protected ExclusionPolicy exclusionPolicy;

    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected ExtendedEntities extendedEntities;
    protected EntityStates entityStates;
    protected DatatypeRegistry datatypeRegistry;

    public interface ExclusionPolicy {
        boolean exclude(Class objectClass, String propertyName);
    }

    public GsonSerializationSupport(BeanFactory beanFactory) {
        this.metadata = beanFactory.getBean(Metadata.class);
        this.metadataTools = beanFactory.getBean(MetadataTools.class);
        this.extendedEntities = beanFactory.getBean(ExtendedEntities.class);
        this.entityStates = beanFactory.getBean(EntityStates.class);
        this.datatypeRegistry = beanFactory.getBean(DatatypeRegistry.class);
        gsonBuilder = new GsonBuilder()
                .registerTypeHierarchyAdapter(Entity.class, new TypeAdapter<Entity>() {
                    @Override
                    public void write(JsonWriter out, Entity entity) throws IOException {
                        writeEntity(out, entity);
                    }

                    @Override
                    public Entity read(JsonReader in) throws IOException {
                        return readEntity(in);
                    }
                })
                .registerTypeAdapterFactory(new TypeAdapterFactory() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                        if (Class.class.isAssignableFrom(type.getRawType())) {
                            return (TypeAdapter<T>) new TypeAdapter<Class>() {
                                @Override
                                public void write(JsonWriter out, Class value) throws IOException {
                                    MetaClass metaClass = metadata.getClass(value);
                                    if (metaClass != null) {
                                        metaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
                                        out.value(metaClass.getName());
                                    } else {
                                        out.value(value.getCanonicalName());
                                    }
                                }

                                @Override
                                public Class read(JsonReader in) throws IOException {
                                    String value = in.nextString();
                                    MetaClass metaClass = metadata.getClass(value);
                                    if (metaClass != null) {
                                        metaClass = extendedEntities.getEffectiveMetaClass(metaClass);
                                        return metaClass.getJavaClass();
                                    } else {
                                        return ReflectionHelper.getClass(value);
                                    }
                                }
                            };
                        } else {
                            return null;
                        }
                    }
                });

    }

    private Entity readEntity(JsonReader in) throws IOException {
        in.beginObject();
        in.nextName();
        String metaClassName = in.nextString();
        MetaClass metaClass = metadata.getSession().getClass(metaClassName);
        Entity entity = (Entity) metadata.create(metaClass);
        in.nextName();
        String id = in.nextString();
        MetaProperty idProperty = metadataTools.getPrimaryKeyProperty(metaClass);
        if (idProperty == null) {
            idProperty = metaClass.getProperty("id");
        }
        try {
            EntityValues.setValue(entity, idProperty.getName(), datatypeRegistry.find(idProperty.getJavaType()).parse(id));
        } catch (ParseException e) {
            throw new RuntimeException(
                    format("An error occurred while parsing id property. Class [%s]. Value [%s].", idProperty.getJavaType(), id), e);
        }

        Entity processedObject = processedObjects.get(Id.of(entity).getValue());
        if (processedObject != null) {
            entity = processedObject;
        } else {
            processedObjects.put(Id.of(entity).getValue(), entity);
            readFields(in, metaClass, entity);
        }
        in.endObject();
        return entity;
    }

    protected void readFields(JsonReader in, MetaClass metaClass, Entity entity) throws IOException {
        while (in.hasNext()) {
            String propertyName = in.nextName();
            MetaProperty property = metaClass.findProperty(propertyName);
            if (property != null && !property.isReadOnly() && !exclude(entity.getClass(), propertyName)) {
                Class<?> propertyType = property.getJavaType();
                Range propertyRange = property.getRange();
                if (propertyRange.isDatatype()) {
                    Object value = readSimpleProperty(in, propertyType);
                    EntityValues.setValue(entity, propertyName, value);
                } else if (propertyRange.isClass()) {
                    if (Entity.class.isAssignableFrom(propertyType)) {
                        EntityValues.setValue(entity, propertyName, readEntity(in));
                    } else if (Collection.class.isAssignableFrom(propertyType)) {
                        Collection entities = readCollection(in, propertyType);
                        EntityValues.setValue(entity, propertyName, entities);
                    } else {
                        in.skipValue();
                    }
                } else if (propertyRange.isEnum()) {
                    String stringValue = in.nextString();
                    try {
                        Object value = propertyRange.asEnumeration().parse(stringValue);
                        EntityValues.setValue(entity, propertyName, value);
                    } catch (ParseException e) {
                        throw new RuntimeException(
                                format("An error occurred while parsing enum. Class [%s]. Value [%s].", propertyType, stringValue), e);
                    }
                }
            } else {
                readUnresolvedProperty(entity, propertyName, in);
            }
        }
    }

    protected void readUnresolvedProperty(Entity entity, String propertyName, JsonReader in) throws IOException {
        in.skipValue();
    }

    @Nullable
    protected Object readSimpleProperty(JsonReader in, Class<?> propertyType) throws IOException {
        String value = in.nextString();
        Object parsedValue = null;
        try {
            Datatype<?> datatype = datatypeRegistry.find(propertyType);
            if (datatype != null) {
                parsedValue = datatype.parse(value);
            }
            return parsedValue;
        } catch (ParseException e) {
            throw new RuntimeException(
                    format("An error occurred while parsing property. Class [%s]. Value [%s].", propertyType, value), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Collection readCollection(JsonReader in, Class<?> propertyType) throws IOException {
        Collection entities;
        if (List.class.isAssignableFrom(propertyType)) {
            entities = new ArrayList<>();
        } else if (Set.class.isAssignableFrom(propertyType)) {
            entities = new LinkedHashSet<>();
        } else {
            throw new RuntimeException(format("Could not instantiate collection with class [%s].", propertyType));
        }
        in.beginArray();
        while (in.hasNext()) {
            Entity entityForList = readEntity(in);
            entities.add(entityForList);
        }
        in.endArray();
        return entities;
    }

    @SuppressWarnings("unchecked")
    protected void writeEntity(JsonWriter out, Entity entity) throws IOException {
        out.beginObject();
        MetaClass metaClass = metadata.getClass(entity);
        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(metaClass);
        if (primaryKeyProperty == null) {
            primaryKeyProperty = metaClass.getProperty("id");
        }
        Datatype idType = datatypeRegistry.find(primaryKeyProperty.getJavaType());
        Object id = Id.of(entity).getValue();
        if (processedObjects.containsKey(id)) {
            out.name("metaClass");
            out.value(metaClass.getName());
            out.name("id");
            out.value(idType.format(id));
        } else {
            processedObjects.put(id, entity);
            out.name("metaClass");
            out.value(metaClass.getName());
            out.name("id");
            out.value(idType.format(id));
            writeFields(out, entity);
        }

        out.endObject();
    }

    @SuppressWarnings("unchecked")
    protected void writeFields(JsonWriter out, Entity entity) throws IOException {
        MetaClass metaClass = metadata.getClass(entity);
        String idName = metadataTools.getPrimaryKeyName(metaClass);
        if (idName == null) {
            idName = "id";
        }
        for (MetaProperty property : metaClass.getProperties()) {
            if (!idName.equalsIgnoreCase(property.getName())
                    && !property.isReadOnly()
                    && !exclude(entity.getClass(), property.getName())
                    && entityStates.isLoaded(entity, property.getName())) {
                Range propertyRange = property.getRange();
                if (propertyRange.isDatatype()) {
                    writeSimpleProperty(out, entity, property);
                } else if (propertyRange.isClass()) {
                    Object value = EntityValues.getValue(entity, property.getName());
                    if (value instanceof Entity) {
                        out.name(property.getName());
                        writeEntity(out, (Entity) value);
                    } else if (value instanceof Collection) {
                        out.name(property.getName());
                        writeCollection(out, (Collection) value);
                    }
                } else if (propertyRange.isEnum()) {
                    Object value = EntityValues.getValue(entity, property.getName());
                    out.name(property.getName());
                    out.value(propertyRange.asEnumeration().format(value));
                }
            }
        }
    }

    protected void writeCollection(JsonWriter out, Collection value) throws IOException {
        out.beginArray();
        for (Object o : value) {
            if (o instanceof Entity) {
                writeEntity(out, (Entity) o);
            }
        }
        out.endArray();
    }

    protected void writeSimpleProperty(JsonWriter out, Entity entity, MetaProperty property) throws IOException {
        Object value = EntityValues.getValue(entity, property.getName());
        if (value != null) {
            out.name(property.getName());
            Datatype datatype = datatypeRegistry.find(property.getJavaType());
            if (datatype != null) {
                out.value(datatype.format(value));
            } else {
                out.value(String.valueOf(value));
            }
        }
    }

    protected boolean exclude(Class objectClass, String propertyName) {
        return exclusionPolicy != null && exclusionPolicy.exclude(objectClass, propertyName);
    }

    public String convertToString(Object entity) {
        return gsonBuilder.create().toJson(entity);
    }

    public <T> T convertToReport(String json, Class<T> aClass) {
        return gsonBuilder.create().fromJson(json, aClass);
    }
}
