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

import java.util.ArrayList;
import java.util.List;

public class ReportsXStream extends XStream {
    public ReportsXStream() {
        this(null);
    }

    public ReportsXStream(List<Class> excluded) {
        this(null,
                new XppDriver(),
                new ClassLoaderReference(Thread.currentThread().getContextClassLoader()),
                null,
                new ReportsXStreamConverterLookup(excluded));
    }

    protected ReportsXStream(
            ReflectionProvider reflectionProvider, HierarchicalStreamDriver driver, ClassLoaderReference classLoader,
            Mapper mapper, final ReportsXStreamConverterLookup converterLookup) {
        super(reflectionProvider, driver, classLoader, mapper,
                converterLookup::lookupConverterForType,
                converterLookup::registerConverter);
    }

    public static class ReportsXStreamConverterLookup extends DefaultConverterLookup {
        protected List<Class> excluded;

        public ReportsXStreamConverterLookup(List<Class> excluded) {
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