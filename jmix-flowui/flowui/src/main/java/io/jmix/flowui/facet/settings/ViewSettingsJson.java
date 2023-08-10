/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.facet.settings;

import com.google.common.base.Strings;
import com.google.gson.*;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.view.View;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * {@link View} settings that uses JSON structure.
 */
@Internal
public class ViewSettingsJson extends AbstractViewSettings {

    protected JsonArray root;

    protected Gson gson;

    public ViewSettingsJson(String viewId) {
        super(viewId);

        initGson();
    }

    protected void initGson() {
        gson = new GsonBuilder()
                .create();
    }

    @Override
    public ViewSettings put(String id, String key, @Nullable String value) {
        checkNotNullPutConditions(id, key);

        JsonObject object = getObjectOrCreate(id);

        object.addProperty(key, value);

        put(object, id);

        return this;
    }

    @Override
    public ViewSettings put(String id, String key, @Nullable Integer value) {
        checkNotNullPutConditions(id, key);

        JsonObject object = getObjectOrCreate(id);

        object.addProperty(key, value);

        put(object, id);

        return this;
    }

    @Override
    public ViewSettings put(String id, String key, @Nullable Long value) {
        checkNotNullPutConditions(id, key);

        JsonObject object = getObjectOrCreate(id);

        object.addProperty(key, value);

        put(object, id);

        return this;
    }

    @Override
    public ViewSettings put(String id, String key, @Nullable Double value) {
        checkNotNullPutConditions(id, key);

        JsonObject object = getObjectOrCreate(id);

        object.addProperty(key, value);

        put(object, id);

        return this;
    }

    @Override
    public ViewSettings put(String id, String key, @Nullable Boolean value) {
        checkNotNullPutConditions(id, key);

        JsonObject object = getObjectOrCreate(id);

        object.addProperty(key, value);

        put(object, id);

        return this;
    }

    @Override
    public ViewSettings put(Settings settings) {
        Preconditions.checkNotNullArgument(settings);

        if (settings.getId() == null) {
            throw new IllegalArgumentException("Cannot put settings with null id");
        }

        put(gson.toJsonTree(settings), settings.getId());

        return this;
    }

    /**
     * @param json json object that represents settings
     * @return current instance of {@link ViewSettings}
     */
    public ViewSettingsJson put(JsonObject json) {
        Preconditions.checkNotNullArgument(json);

        if (isValueNull(json, "id")) {
            throw new IllegalArgumentException("Cannot put settings, json must have not null id");
        }

        String id = json.getAsJsonPrimitive("id").getAsString();
        put(json, id);

        return this;
    }

    @Override
    public ViewSettings delete(String id) {
        Preconditions.checkNotNullArgument(id);

        JsonObject component = getObject(id);

        if (component != null) {
            root.remove(component);

            setModified(true);
        }

        return this;
    }

    @Override
    public ViewSettings delete(String id, String key) {
        Preconditions.checkNotNullArgument(id);
        Preconditions.checkNotNullArgument(key);

        JsonObject component = getObject(id);

        if (component != null) {
            component.remove(key);

            setModified(true);
        }

        return this;
    }

    /**
     * @param id e.g. component id
     * @return json object that represents settings
     */
    public Optional<JsonObject> getJsonSettings(String id) {
        Preconditions.checkNotNullArgument(id);

        return Optional.ofNullable(getObject(id));
    }

    @Override
    public Optional<String> getString(String id, String key) {
        checkNotNullGetConditions(id, key);

        JsonObject object = getObject(id);

        if (object == null || isValueNull(object, key))
            return Optional.empty();

        return object.keySet().contains(key) ?
                Optional.of(object.getAsJsonPrimitive(key).getAsString()) : Optional.empty();
    }

    @Override
    public Optional<Integer> getInteger(String id, String key) {
        checkNotNullGetConditions(id, key);

        JsonObject object = getObject(id);

        if (object == null || isValueNull(object, key))
            return Optional.empty();

        return object.keySet().contains(key) ?
                Optional.of(object.getAsJsonPrimitive(key).getAsInt()) : Optional.empty();
    }

    @Override
    public Optional<Long> getLong(String id, String key) {
        checkNotNullGetConditions(id, key);

        JsonObject object = getObject(id);

        if (object == null || isValueNull(object, key))
            return Optional.empty();

        return object.keySet().contains(key) ?
                Optional.of(object.getAsJsonPrimitive(key).getAsLong()) : Optional.empty();
    }

    @Override
    public Optional<Double> getDouble(String id, String key) {
        checkNotNullGetConditions(id, key);

        JsonObject object = getObject(id);

        if (object == null || isValueNull(object, key))
            return Optional.empty();

        return object.keySet().contains(key) ?
                Optional.of(object.getAsJsonPrimitive(key).getAsDouble()) : Optional.empty();
    }

    @Override
    public Optional<Boolean> getBoolean(String id, String key) {
        checkNotNullGetConditions(id, key);

        JsonObject object = getObject(id);

        if (object == null || isValueNull(object, key))
            return Optional.empty();

        return object.keySet().contains(key) ?
                Optional.of(object.getAsJsonPrimitive(key).getAsBoolean()) : Optional.empty();
    }

    @Override
    public <T extends Settings> Optional<T> getSettings(String id, Class<T> settingsClass) {
        Preconditions.checkNotNullArgument(id);
        Preconditions.checkNotNullArgument(settingsClass);

        JsonObject json = getObject(id);
        if (json == null)
            return Optional.empty();

        return Optional.ofNullable(gson.fromJson(json, settingsClass));
    }

    @Override
    public <T extends Settings> T getSettingsOrCreate(String id, Class<T> settingsClass) {
        Preconditions.checkNotNullArgument(id);
        Preconditions.checkNotNullArgument(settingsClass);

        return getSettings(id, settingsClass).orElseGet(() -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", id);
            return settingsClass.cast(gson.fromJson(json, settingsClass));
        });
    }

    @Override
    public void initialize(@Nullable String raw) {
        root = Strings.isNullOrEmpty(raw)
                ? new JsonArray()
                : gson.fromJson(raw, JsonArray.class);
    }

    @Override
    public String serialize() {
        return gson.toJson(root);
    }

    protected void put(JsonElement json, String id) {
        initRoot();

        delete(id);

        root.add(json);

        setModified(true);
    }

    protected JsonObject getObjectOrCreate(String id) {
        JsonObject object = getObject(id);
        if (object == null) {
            object = new JsonObject();
            object.addProperty("id", id);
        }

        return object;
    }

    @Nullable
    protected JsonObject getObject(String objectId) {
        initRoot();

        for (JsonElement jsonElement : root) {
            JsonObject object = (JsonObject) jsonElement;

            boolean keyExist = object.keySet().contains("id");
            if (keyExist) {
                String id = object.getAsJsonPrimitive("id").getAsString();
                if (id.equals(objectId)) {
                    return object;
                }
            }
        }
        return null;
    }

    protected void checkNotNullPutConditions(String id, String key) {
        Preconditions.checkNotNullArgument(id, "Cannot put settings when object id is null");
        Preconditions.checkNotNullArgument(key, "Cannot put settings when key is null");
    }

    protected void checkNotNullGetConditions(String id, String property) {
        Preconditions.checkNotNullArgument(id, "Cannot get settings when object id is null");
        Preconditions.checkNotNullArgument(property, "Cannot get settings when key is null");
    }

    protected void initRoot() {
        if (root == null) {
            root = new JsonArray();
        }
    }

    protected boolean isValueNull(JsonObject json, String key) {
        if (json.keySet().contains(key))
            return json.get(key).isJsonNull();

        return true;
    }
}
