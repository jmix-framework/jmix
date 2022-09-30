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
package io.jmix.core.impl.serialization;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jmix.core.CoreProperties;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.UuidProvider;
import io.jmix.core.entity.EntityPreconditions;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.SecurityState;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.rightPad;

@Component("core_EntitySerializationTokenManager")
public class EntitySerializationTokenManager {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected CoreProperties coreProperties;

    protected static final String ENTITY_NAME_KEY = "__entityName";
    protected static final String ENTITY_ID_KEY = "__entityId";

    protected static final Set<String> SYSTEM_ATTRIBUTE_KEYS = new ImmutableSet.Builder<String>()
            .add(ENTITY_NAME_KEY)
            .add(ENTITY_ID_KEY)
            .build();

    private static final Logger logger = LoggerFactory.getLogger(EntitySerializationTokenManager.class);

    /**
     * Encrypt security state and write the result to the security token
     */
    @Nullable
    public String generateSecurityToken(Object entity) {
        EntityPreconditions.checkEntityType(entity);
        MetaClass metaClass = metadata.getClass(entity);

        SecurityState securityState = EntitySystemAccess.getSecurityState(entity);
        JsonObject tokenObject = new JsonObject();
        tokenObject.addProperty(ENTITY_NAME_KEY, metaClass.getName());
        if (!metadataTools.hasCompositePrimaryKey(metaClass) && !EntitySystemAccess.isEmbeddable(entity)) {
            addSingleId(tokenObject, ENTITY_ID_KEY, EntityValues.getId(entity));
        }
        if (securityState.getErasedData() != null) {
            securityState.getErasedData().asMap().forEach((k, v) -> addCollectionId(tokenObject, k, v));
        }
        try {
            return Base64.getEncoder().encodeToString(
                    createCipher(Cipher.ENCRYPT_MODE).doFinal(tokenObject.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("An error occurred while generating security token", e);
        }
    }

    /**
     * Decrypt security token and read filtered data
     */
    public void restoreSecurityToken(Object entity, @Nullable String securityToken) {
        MetaClass metaClass = metadata.getClass(entity);
        SecurityState securityState = EntitySystemAccess.getSecurityState(entity);

        if (securityToken != null) {
            try {
                byte[] decrypted = createCipher(Cipher.DECRYPT_MODE).doFinal(
                        Base64.getDecoder().decode(securityToken));

                JsonObject tokenObject = JsonParser.parseString(new String(decrypted, StandardCharsets.UTF_8)).getAsJsonObject();

                validateToken(tokenObject, entity, metaClass);

                for (String key : tokenObject.keySet()) {
                    if (!SYSTEM_ATTRIBUTE_KEYS.contains(key)) {
                        String propertyName = String.valueOf(key);
                        MetaProperty metaProperty = metaClass.getProperty(propertyName);
                        for (JsonElement id : tokenObject.getAsJsonArray(propertyName)) {
                            securityState.addErasedId(propertyName, parseId(id, metaProperty.getRange().asClass()));
                        }
                    }
                }
                securityState.setRestoreState(SecurityState.RestoreState.RESTORED_FROM_TOKEN);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException("An error occurred while reading security token", e);
            }
        } else {
            securityState.setRestoreState(SecurityState.RestoreState.RESTORED_FROM_NULL_TOKEN);
        }
    }

    protected void validateToken(JsonObject tokenObject, Object entity, MetaClass metaClass) {
        if (!metadataTools.hasCompositePrimaryKey(metaClass) && !metadataTools.isJpaEmbeddable(metaClass)) {

            if (!tokenObject.has(ENTITY_ID_KEY) || !tokenObject.has(ENTITY_NAME_KEY)) {
                throw new EntityTokenException("Invalid format for security token");
            }

            String entityName = tokenObject.get(ENTITY_NAME_KEY).getAsString();
            if (!Objects.equals(entityName, metaClass.getName())) {
                throw new EntityTokenException("Invalid format for security token: incorrect entity type");
            }

            if (!tokenObject.has(ENTITY_ID_KEY)) {
                throw new EntityTokenException("Invalid format for security token: incorrect entity id");
            }

            JsonElement jsonEntityId = tokenObject.get(ENTITY_ID_KEY);
            if (EntityValues.getId(entity) != null && !Objects.equals(EntityValues.getId(entity), parseId(jsonEntityId, metaClass))) {
                throw new EntityTokenException("Invalid format for security token: incorrect entity id");
            }
        }
    }

    protected Cipher createCipher(int mode) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            String encryptionKey = rightPad(
                    StringUtils.substring(coreProperties.getEntitySerializationTokenEncryptionKey(), 0, 16), 16);

            SecretKeySpec sKeySpec = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(mode, sKeySpec);
            return cipher;
        } catch (Exception e) {
            throw new EntityTokenException("An error occurred while initiating encryption/decryption", e);
        }
    }

    protected Object parseId(JsonElement value, MetaClass metaClass) {
        MetaProperty primaryKey = metadataTools.getPrimaryKeyProperty(metaClass);

        if (primaryKey != null) {
            Class type = primaryKey.getJavaType();
            if (UUID.class.equals(type)) {
                return UuidProvider.fromString(value.getAsString());
            } else if (Long.class.equals(type)) {
                return value.getAsLong();
            } else if (Integer.class.equals(type)) {
                return value.getAsInt();
            } else if (String.class.equals(type)) {
                return value.getAsString();
            } else {
                throw new EntityTokenException(
                        String.format("Unsupported primary key type: %s for %s", type.getSimpleName(), metaClass.getName()));
            }
        } else {
            throw new EntityTokenException(
                    String.format("Primary key not found for %s", metaClass.getName()));
        }
    }

    protected void addSingleId(JsonObject jsonObject, String property, @Nullable Object value) {
        if (value instanceof UUID) {
            jsonObject.addProperty(property, value.toString());
        } else if (value instanceof Number) {
            jsonObject.addProperty(property, (Number) value);
        } else if (value instanceof String) {
            jsonObject.addProperty(property, (String) value);
        } else if (value != null) {
            throw new EntityTokenException(
                    String.format("Unsupported primary key type %s", value.getClass().getSimpleName()));
        }
    }

    protected void addCollectionId(JsonObject jsonObject, String property, Collection<Object> values) {
        JsonArray jsonArray = new JsonArray();

        for (Object value : values) {
            if (value instanceof UUID) {
                jsonArray.add(value.toString());
            } else if (value instanceof Number) {
                jsonArray.add((Number) value);
            } else if (value instanceof String) {
                jsonArray.add((String) value);
            } else if (value != null) {
                throw new EntityTokenException(
                        String.format("Unsupported primary key type %s", value.getClass().getSimpleName()));
            }
        }

        jsonObject.add(property, jsonArray);
    }

    @EventListener(ApplicationContextInitializedEvent.class)
    protected void applicationInitialized() {
        if (coreProperties.isEntitySerializationTokenRequired() &&
                "KEY".equals(coreProperties.getEntitySerializationTokenEncryptionKey())) {
            logger.info(
                    "=================================================================\n" +
                            "'jmix.core.entitySerializationTokenEncryptionKey' -\n" +
                            "property is set to default value.\n" +
                            "Use a unique value in production environments.\n" +
                            "=================================================================");
        }
    }
}