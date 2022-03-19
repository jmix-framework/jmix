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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class MetadataFieldsIgnoringGson {

    protected final static String METADATA_STARTS_CHAR = "_";

    public static MetadataFieldsIgnoringGsonBuilder create() {
        return new MetadataFieldsIgnoringGsonBuilder();
    }

    public static class MetadataFieldsIgnoringGsonBuilder {

        protected GsonBuilder gsonBuilder;

        protected ExclusionStrategy strategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }

            @Override
            public boolean shouldSkipField(FieldAttributes field) {
                return field.getName().startsWith(METADATA_STARTS_CHAR);
            }
        };

        public MetadataFieldsIgnoringGsonBuilder() {
            gsonBuilder = new GsonBuilder();
        }

        public MetadataFieldsIgnoringGsonBuilder addIgnoringStrategy() {
            gsonBuilder.addSerializationExclusionStrategy(strategy);
            return this;
        }

        public MetadataFieldsIgnoringGsonBuilder registerTypeAdapter(Type type, Object typeAdapter) {
            gsonBuilder.registerTypeAdapter(type, typeAdapter);
            return this;
        }

        public MetadataFieldsIgnoringGsonBuilder setIgnoringStrategy() {
            gsonBuilder.setExclusionStrategies(strategy);
            return this;
        }

        public Gson build() {
            return gsonBuilder.create();
        }
    }
}
