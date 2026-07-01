/*
 * Copyright 2026 Haulmont.
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

package data_components;

import io.jmix.core.Sort;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.model.BaseCollectionLoader;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionContainerSortContext;
import io.jmix.flowui.model.CollectionContainerSortProvider;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.Sorter;
import io.jmix.flowui.model.SorterFactory;
import io.jmix.flowui.model.impl.CollectionContainerSorter;
import io.jmix.flowui.model.impl.CollectionPropertyContainerSorter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import test_support.entity.sales.Order;
import test_support.entity.sales.OrderLine;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Configuration
@NullMarked
public class CollectionContainerSortProviderTestConfiguration {

    @Bean("test_SorterFactory")
    @Primary
    SorterFactory sorterFactory() {
        return new SorterFactory() {

            @Override
            public Sorter createCollectionContainerSorter(CollectionContainer<?> container,
                                                          @Nullable BaseCollectionLoader loader) {
                return sort -> {
                    throw new AssertionError("SorterFactory must not be used when sort providers are available");
                };
            }

            @Override
            public Sorter createCollectionPropertyContainerSorter(CollectionPropertyContainer<?> container) {
                return sort -> {
                    throw new AssertionError("SorterFactory must not be used when sort providers are available");
                };
            }
        };
    }

    @Bean("test_FirstOrderSortProvider")
    CollectionContainerSortProvider firstOrderSortProvider(BeanFactory beanFactory) {
        return new FirstOrderSortProvider(beanFactory);
    }

    @Bean("test_SecondOrderSortProvider")
    CollectionContainerSortProvider secondOrderSortProvider(BeanFactory beanFactory) {
        return new SecondOrderSortProvider(beanFactory);
    }

    @Bean("test_OrderLineSortProvider")
    CollectionContainerSortProvider orderLineSortProvider(BeanFactory beanFactory) {
        return new OrderLineSortProvider(beanFactory);
    }

    protected abstract static class TestSortProvider implements CollectionContainerSortProvider, Ordered {

        protected final BeanFactory beanFactory;
        private final int order;

        protected TestSortProvider(BeanFactory beanFactory, int order) {
            this.beanFactory = beanFactory;
            this.order = order;
        }

        @Override
        public int getOrder() {
            return order;
        }

        protected boolean isContainer(CollectionContainerSortContext context, Class<?> entityClass) {
            return entityClass.equals(context.container().getEntityMetaClass().getJavaClass());
        }
    }

    protected static class FirstOrderSortProvider extends TestSortProvider {

        protected FirstOrderSortProvider(BeanFactory beanFactory) {
            super(beanFactory, 100);
        }

        @Nullable
        @Override
        public Sorter getSorter(CollectionContainerSortContext context) {
            return !(context.container() instanceof CollectionPropertyContainer<?>) && isContainer(context, Order.class)
                    ? new TestOrderCollectionContainerSorter(context.container(), context.loader(), beanFactory)
                    : null;
        }
    }

    protected static class SecondOrderSortProvider extends TestSortProvider {

        protected SecondOrderSortProvider(BeanFactory beanFactory) {
            super(beanFactory, 200);
        }

        @Nullable
        @Override
        public Sorter getSorter(CollectionContainerSortContext context) {
            return !(context.container() instanceof CollectionPropertyContainer<?>) && isContainer(context, Order.class)
                    ? new NaturalOrderCollectionContainerSorter(context.container(), context.loader(), beanFactory)
                    : null;
        }
    }

    protected static class OrderLineSortProvider extends TestSortProvider {

        protected OrderLineSortProvider(BeanFactory beanFactory) {
            super(beanFactory, 100);
        }

        @Nullable
        @Override
        public Sorter getSorter(CollectionContainerSortContext context) {
            if (context.container() instanceof CollectionPropertyContainer<?> propertyContainer
                    && isContainer(context, OrderLine.class)) {
                return new TestOrderLineCollectionPropertyContainerSorter(propertyContainer, beanFactory);
            }

            return null;
        }
    }

    protected static class TestOrderCollectionContainerSorter extends CollectionContainerSorter {

        @Nullable
        private Map<String, Comparator<?>> propertyComparators;

        protected TestOrderCollectionContainerSorter(CollectionContainer<?> container,
                                                     @Nullable BaseCollectionLoader loader,
                                                     BeanFactory beanFactory) {
            super(container, loader, beanFactory);
        }

        @Override
        public void setPropertyComparators(@Nullable Map<String, Comparator<?>> propertyComparators) {
            this.propertyComparators = propertyComparators != null
                    ? new HashMap<>(propertyComparators)
                    : null;

            super.setPropertyComparators(propertyComparators);
        }

        @Override
        protected Comparator<?> createComparator(Sort.Order sortOrder, MetaClass metaClass) {
            Comparator<?> propertyComparator = getPropertyComparator(propertyComparators, sortOrder);
            if (propertyComparator != null) {
                return applySortDirection(sortOrder, propertyComparator);
            }

            if ("number".equals(sortOrder.getProperty())) {
                return applySortDirection(sortOrder,
                        Comparator.comparing(Order::getTotal, Comparator.nullsFirst(Double::compareTo)));
            }

            if ("total".equals(sortOrder.getProperty())) {
                return applySortDirection(sortOrder,
                        Comparator.comparing(Order::getNumber, Comparator.nullsFirst(String::compareTo)));
            }

            return super.createComparator(sortOrder, metaClass);
        }
    }

    protected static class NaturalOrderCollectionContainerSorter extends CollectionContainerSorter {

        protected NaturalOrderCollectionContainerSorter(CollectionContainer<?> container,
                                                        @Nullable BaseCollectionLoader loader,
                                                        BeanFactory beanFactory) {
            super(container, loader, beanFactory);
        }
    }

    protected static class TestOrderLineCollectionPropertyContainerSorter extends CollectionPropertyContainerSorter {

        @Nullable
        private Map<String, Comparator<?>> propertyComparators;

        protected TestOrderLineCollectionPropertyContainerSorter(CollectionPropertyContainer<?> container,
                                                                 BeanFactory beanFactory) {
            super(container, beanFactory);
        }

        @Override
        public void setPropertyComparators(@Nullable Map<String, Comparator<?>> propertyComparators) {
            this.propertyComparators = propertyComparators != null
                    ? new HashMap<>(propertyComparators)
                    : null;

            super.setPropertyComparators(propertyComparators);
        }

        @Override
        protected Comparator<?> createComparator(Sort.Order sortOrder, MetaClass metaClass) {
            Comparator<?> propertyComparator = getPropertyComparator(propertyComparators, sortOrder);
            if (propertyComparator != null) {
                return applySortDirection(sortOrder, propertyComparator);
            }

            if ("quantity".equals(sortOrder.getProperty())) {
                return applySortDirection(sortOrder,
                        Comparator.comparing(OrderLine::getDescription, Comparator.nullsFirst(String::compareTo)));
            }

            return super.createComparator(sortOrder, metaClass);
        }
    }

    @Nullable
    protected static Comparator<?> getPropertyComparator(@Nullable Map<String, Comparator<?>> propertyComparators,
                                                         Sort.Order sortOrder) {
        return propertyComparators != null
                ? propertyComparators.get(sortOrder.getProperty())
                : null;
    }

    protected static Comparator<?> applySortDirection(Sort.Order sortOrder, Comparator<?> comparator) {
        return sortOrder.getDirection() == Sort.Direction.ASC ? comparator : comparator.reversed();
    }
}
