/*
 * Copyright 2019 Haulmont.
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
package io.jmix.security.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.ServerConfig;
import io.jmix.core.UuidProvider;
import io.jmix.core.entity.*;
import io.jmix.core.event.AppContextInitializedEvent;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.security.SecurityTokenException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.jmix.core.entity.BaseEntityInternalAccess.*;
import static org.apache.commons.lang3.StringUtils.rightPad;
import static org.apache.commons.lang3.StringUtils.substring;

@Component(SecurityTokenManager.NAME)
public class SecurityTokenManager {
    public static final String NAME = "cuba_SecurityTokenManager";

    private static final Logger log = LoggerFactory.getLogger(SecurityTokenManager.class);

    @Inject
    protected ServerConfig config;
    @Inject
    protected Metadata metadata;
    @Inject
    private MetadataTools metadataTools;

    protected static final String ENTITY_NAME_KEY = "__entityName";
    protected static final String ENTITY_ID_KEY = "__entityId";

    protected static final Set<String> SYSTEM_ATTRIBUTE_KEYS = new ImmutableSet.Builder<String>()
            .add(ENTITY_NAME_KEY)
            .add(ENTITY_ID_KEY)
            .build();

    /**
     * Encrypt filtered data and write the result to the security token
     */
    public void writeSecurityToken(Entity entity) {
        SecurityState securityState = getOrCreateSecurityState(entity);
        if (securityState != null) {
            JSONObject jsonObject = new JSONObject();
            Multimap<String, Object> filtered = getFilteredData(securityState);
            if (filtered != null) {
                Set<Map.Entry<String, Collection<Object>>> entries = filtered.asMap().entrySet();
                String[] filteredAttributes = new String[entries.size()];
                int i = 0;
                for (Map.Entry<String, Collection<Object>> entry : entries) {
                    jsonObject.put(entry.getKey(), entry.getValue());
                    filteredAttributes[i++] = entry.getKey();
                }
                setFilteredAttributes(securityState, filteredAttributes);
            }
            MetaClass metaClass = metadata.getClass(entity.getClass());
            jsonObject.put(ENTITY_NAME_KEY, metaClass.getName());
            if (!metadataTools.hasCompositePrimaryKey(metaClass)) {
                jsonObject.put(ENTITY_ID_KEY, getEntityId(entity));
            }

            String json = jsonObject.toString();
            byte[] encrypted;
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            try {
                encrypted = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new RuntimeException("An error occurred while generating security token", e);
            }
            setSecurityToken(securityState, encrypted);
        }
    }

    /**
     * Decrypt security token and read filtered data
     */
    public void readSecurityToken(Entity entity) {
        SecurityState securityState = getSecurityState(entity);
        if (getSecurityToken(entity) == null) {
            return;
        }
        MetaClass metaClass = metadata.getClass(entity);
        Multimap<String, Object> filteredData = ArrayListMultimap.create();
        setFilteredData(securityState, filteredData);
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        try {
            byte[] decrypted = cipher.doFinal(getSecurityToken(securityState));
            String json = new String(decrypted, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(json);
            for (String key : jsonObject.keySet()) {
                if (!SYSTEM_ATTRIBUTE_KEYS.contains(key)) {
                    String elementName = String.valueOf(key);
                    JSONArray jsonArray = jsonObject.getJSONArray(elementName);
                    MetaProperty metaProperty = metaClass.getProperty(elementName);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Object id = jsonArray.get(i);
                        filteredData.put(elementName, convertId(id, metaProperty.getRange().asClass(), true));
                    }
                }
            }
            if (!metadataTools.hasCompositePrimaryKey(metaClass)
                    && !(entity instanceof EmbeddableEntity)) {
                if (!jsonObject.has(ENTITY_ID_KEY) || !jsonObject.has(ENTITY_NAME_KEY)) {
                    throw new SecurityTokenException("Invalid format for security token");
                }
                String entityName = jsonObject.getString(ENTITY_NAME_KEY);
                if (!Objects.equals(entityName, metaClass.getName())) {
                    throw new SecurityTokenException("Invalid format for security token: incorrect entity type");
                }
                Object jsonEntityId = jsonObject.get(ENTITY_ID_KEY);
                if (jsonEntityId == null) {
                    throw new SecurityTokenException("Invalid format for security token: incorrect entity id");
                }
                Object entityId = getEntityId(entity);
                if (entityId != null && !Objects.equals(entityId, convertId(jsonEntityId, metaClass, false))) {
                    throw new SecurityTokenException("Invalid format for security token: incorrect entity id");
                }
            }
        } catch (SecurityTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while reading security token", e);
        }
    }

    protected Cipher getCipher(int mode) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            byte[] encryptionKey = rightPad(substring(config.getKeyForSecurityTokenEncryption(), 0, 16), 16)
                    .getBytes(StandardCharsets.UTF_8);

            SecretKeySpec sKeySpec = new SecretKeySpec(encryptionKey, "AES");
            cipher.init(mode, sKeySpec);
            return cipher;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while initiating encryption/decryption", e);
        }
    }

    protected String[] parseJsonArrayAsStrings(JSONArray array) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            result.add(array.getString(i));
        }
        return result.toArray(new String[0]);
    }

    protected Object getEntityId(Entity entity) {
        Object entityId = entity.getId();
        if (entityId instanceof IdProxy) {
            return ((IdProxy) entityId).get();
        } else {
            return entityId;
        }
    }

    protected Object convertId(Object value, MetaClass metaClass, boolean handleHasUuid) {
        if (handleHasUuid && HasUuid.class.isAssignableFrom(metaClass.getJavaClass())) {
            return UuidProvider.fromString((String) value);
        }
        MetaProperty primaryKey = metadataTools.getPrimaryKeyProperty(metaClass);

        if (primaryKey != null) {
            Class type = primaryKey.getJavaType();
            if (UUID.class.equals(type)) {
                return UuidProvider.fromString((String) value);
            } else if (Long.class.equals(type) || IdProxy.class.equals(type)) {
                return ((Integer) value).longValue();
            } else if (Integer.class.equals(type)) {
                return value;
            } else if (String.class.equals(type)) {
                return value;
            } else {
                throw new IllegalStateException(
                        String.format("Unsupported primary key type: %s for %s", type.getSimpleName(), metaClass.getName()));
            }
        } else {
            throw new IllegalStateException(
                    String.format("Primary key not found for %s", metaClass.getName()));
        }
    }

    /**
     * INTERNAL.
     */
    public void addFiltered(BaseGenericIdEntity<?> entity, String property, Object id) {
        SecurityState securityState = getOrCreateSecurityState(entity);
        if (getFilteredData(securityState) == null) {
            setFilteredData(securityState, ArrayListMultimap.create());
        }
        getFilteredData(securityState).put(property, id);
    }

    /**
     * INTERNAL.
     */
    public void addFiltered(BaseGenericIdEntity<?> entity, String property, Collection ids) {
        SecurityState securityState = getOrCreateSecurityState(entity);
        if (getFilteredData(securityState) == null) {
            setFilteredData(securityState, ArrayListMultimap.create());
        }
        getFilteredData(securityState).putAll(property, ids);
    }

    @EventListener(AppContextInitializedEvent.class)
    protected void applicationInitialized() {
        if ("CUBA.Platform".equals(config.getKeyForSecurityTokenEncryption())) {
            log.warn("\nWARNING:\n" +
                    "=================================================================\n" +
                    "'cuba.keyForSecurityTokenEncryption' app property is set to\n " +
                    "default value. Use a unique value in production environments.\n" +
                    "=================================================================");
        }
    }
}
