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

package io.jmix.core.impl;

import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ProxyClassLoader extends ClassLoader {
    Map<String, TimestampClass> loaded;
    ThreadLocal<Map<String, TimestampClass>> removedFromLoading = new ThreadLocal<>();

    ProxyClassLoader(ClassLoader parent, Map<String, TimestampClass> loaded) {
        super(parent);
        this.loaded = loaded;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        TimestampClass tsClass = loaded.get(name);
        if (tsClass != null) {
            return tsClass.clazz;
        } else {
            return super.loadClass(name, resolve);
        }
    }

    @Nullable
    public TimestampClass removeFromCache(String className) {
        Map<String, TimestampClass> removedFromCompilationMap = removedFromLoading.get();
        if (removedFromCompilationMap == null) {
            removedFromCompilationMap = new HashMap<>();
            removedFromLoading.set(removedFromCompilationMap);
        }

        TimestampClass timestampClass = loaded.get(className);
        if (timestampClass != null) {
            removedFromCompilationMap.put(className, timestampClass);
            loaded.remove(className);
            return timestampClass;
        }

        return null;
    }

    public void restoreRemoved() {
        Map<String, TimestampClass> map = removedFromLoading.get();
        if (map != null) {
            loaded.putAll(map);
        }
        removedFromLoading.remove();
    }

    public void cleanupRemoved() {
        removedFromLoading.remove();
    }

    public boolean contains(String className) {
        return loaded.containsKey(className);
    }
}
