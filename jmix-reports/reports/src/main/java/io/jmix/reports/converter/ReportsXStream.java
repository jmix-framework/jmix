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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.reflection.ExternalizableConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.ClassLoaderReference;
import com.thoughtworks.xstream.core.DefaultConverterLookup;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.Mapper;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ReportsXStream extends XStream {
    public ReportsXStream() {
        this(null);
    }

    public ReportsXStream(@Nullable List<Class> excluded) {
        this(null,
                new XppDriver(),
                new ClassLoaderReference(Thread.currentThread().getContextClassLoader()),
                null,
                new ReportsXStreamConverterLookup(excluded));
    }

    protected ReportsXStream(
            @Nullable ReflectionProvider reflectionProvider, HierarchicalStreamDriver driver, ClassLoaderReference classLoader,
            @Nullable Mapper mapper, final ReportsXStreamConverterLookup converterLookup) {
        super(reflectionProvider, driver, classLoader, mapper,
                converterLookup::lookupConverterForType,
                converterLookup::registerConverter);
    }

    public static class ReportsXStreamConverterLookup extends DefaultConverterLookup {
        protected List<Class> excluded;

        public ReportsXStreamConverterLookup(@Nullable List<Class> excluded) {
            super();
            this.excluded = new ArrayList<>();
            this.excluded.add(ExternalizableConverter.class);
            if (excluded != null) {
                this.excluded.addAll(excluded);
            }
        }

        @Override
        public void registerConverter(Converter converter, int priority) {
            if (converter != null && excluded.contains(converter.getClass())) {
                return;
            }
            super.registerConverter(converter, priority);
        }
    }
}