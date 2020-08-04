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
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.SecurityState;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.security.SecurityProperties;
import io.jmix.security.SecurityTokenException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.rightPad;
import static org.apache.commons.lang3.StringUtils.substring;

@Component(SecurityTokenManager.NAME)
public class SecurityTokenManager {
    public static final String NAME = "jmix_SecurityTokenManager";

    private static final Logger log = LoggerFactory.getLogger(SecurityTokenManager.class);

    @Autowired
    protected SecurityProperties properties;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;

    protected static final String ENTITY_NAME_KEY = "__entityName";
    protected static final String ENTITY_ID_KEY = "__entityId";

    protected static final Set<String> SYSTEM_ATTRIBUTE_KEYS = new ImmutableSet.Builder<String>()
            .add(ENTITY_NAME_KEY)
            .add(ENTITY_ID_KEY)
            .build();

    /**
     * Encrypt filtered data and write the result to the security token
     */
    public void writeSecurityToken(JmixEntity entity) {
        SecurityState securityState = entity.__getEntityEntry().getSecurityState();
        if (securityState != null) {
            JSONObject jsonObject = new JSONObject();
            Multimap<String, Object> filtered = securityState.getErasedData();
            if (filtered != null) {
                Set<Map.Entry<String, Collection<Object>>> entries = filtered.asMap().entrySet();
                String[] filteredAttributes = new String[entries.size()];
                int i = 0;
                for (Map.Entry<String, Collection<Object>> entry : entries) {
                    jsonObject.put(entry.getKey(), entry.getValue());
                    filteredAttributes[i++] = entry.getKey();
                }
                securityState.setFilteredAttributes(filteredAttributes);
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
            securityState.setSecurityToken(encrypted);
        }
    }

    /**
     * Decrypt security token and read filtered data
     */
    public void readSecurityToken(JmixEntity entity) {
        SecurityState securityState = entity.__getEntityEntry().getSecurityState();
        if (securityState.getSecurityToken() == null) {
            return;
        }
        MetaClass metaClass = metadata.getClass(entity);
        Multimap<String, Object> filteredData = ArrayListMultimap.create();
        securityState.setFilteredData(filteredData);
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        try {
            byte[] decrypted = cipher.doFinal(securityState.getSecurityToken());
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
                    && !entity.__getEntityEntry().isEmbeddable()) {
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
            byte[] encryptionKey = rightPad(substring(properties.getKeyForSecurityTokenEncryption(), 0, 16), 16)
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

    protected Object getEntityId(JmixEntity entity) {
        return EntityValues.getId(entity);
    }

    protected Object convertId(Object value, MetaClass metaClass, boolean handleHasUuid) {
        if (handleHasUuid && metadataTools.hasUuid(metaClass)) {
            return UuidProvider.fromString((String) value);
        }
        MetaProperty primaryKey = metadataTools.getPrimaryKeyProperty(metaClass);

        if (primaryKey != null) {
            Class type = primaryKey.getJavaType();
            if (UUID.class.equals(type)) {
                return UuidProvider.fromString((String) value);
            } else if (Long.class.equals(type)) {
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
    public void addFiltered(JmixEntity entity, String property, Object id) {
        EntityEntry entityEntry = entity.__getEntityEntry();
        Multimap<String, Object> filteredData = entityEntry.getSecurityState().getErasedData();
        if (filteredData == null) {
            filteredData = ArrayListMultimap.create();
            entityEntry.getSecurityState().setFilteredData(filteredData);
        }
        filteredData.put(property, id);
    }

    /**
     * INTERNAL.
     */
    public void addFiltered(JmixEntity entity, String property, Collection ids) {
        EntityEntry entityEntry = entity.__getEntityEntry();
        Multimap<String, Object> filteredData = entityEntry.getSecurityState().getErasedData();
        if (filteredData == null) {
            filteredData = ArrayListMultimap.create();
            entityEntry.getSecurityState().setFilteredData(filteredData);
        }
        filteredData.putAll(property, ids);
    }

    @EventListener(ContextRefreshedEvent.class)
    protected void applicationInitialized() {
        if ("CUBA.Platform".equals(properties.getKeyForSecurityTokenEncryption())) {
            log.warn("\nWARNING:\n" +
                    "=================================================================\n" +
                    "'cuba.keyForSecurityTokenEncryption' app property is set to\n " +
                    "default value. Use a unique value in production environments.\n" +
                    "=================================================================");
        }
    }
}
