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

package test_support;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.restds.extension.RestDataStoreExtension;
import org.jspecify.annotations.NullMarked;
import test_support.entity.Customer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Extension for tests: records the contexts it is called with and runs actions set by a test.
 * Supports one entity class of the {@code restService1} data store, {@link Customer} by default.
 */
@NullMarked
public class TestRestDataStoreExtension implements RestDataStoreExtension {

    private final int order;

    private Class<?> supportedClass = Customer.class;

    private Consumer<BeforeLoadContext> beforeLoadAction = context -> {};
    private Consumer<BeforeCountContext> beforeCountAction = context -> {};
    private Consumer<BeforeSaveContext> beforeSaveAction = context -> {};

    private final List<BeforeLoadContext> beforeLoadContexts = new CopyOnWriteArrayList<>();
    private final List<AfterLoadContext> afterLoadContexts = new CopyOnWriteArrayList<>();
    private final List<BeforeCountContext> beforeCountContexts = new CopyOnWriteArrayList<>();
    private final List<BeforeSaveContext> beforeSaveContexts = new CopyOnWriteArrayList<>();
    private final List<AfterSaveContext> afterSaveContexts = new CopyOnWriteArrayList<>();

    public TestRestDataStoreExtension(int order) {
        this.order = order;
    }

    @Override
    public boolean supports(String dataStoreName, MetaClass metaClass) {
        return "restService1".equals(dataStoreName) && metaClass.getJavaClass() == supportedClass;
    }

    @Override
    public void beforeLoad(BeforeLoadContext context) {
        beforeLoadContexts.add(context);
        beforeLoadAction.accept(context);
    }

    @Override
    public void afterLoad(AfterLoadContext context) {
        afterLoadContexts.add(context);
    }

    @Override
    public void beforeCount(BeforeCountContext context) {
        beforeCountContexts.add(context);
        beforeCountAction.accept(context);
    }

    @Override
    public void beforeSave(BeforeSaveContext context) {
        beforeSaveContexts.add(context);
        beforeSaveAction.accept(context);
    }

    @Override
    public void afterSave(AfterSaveContext context) {
        afterSaveContexts.add(context);
    }

    public void onBeforeSave(Consumer<BeforeSaveContext> action) {
        this.beforeSaveAction = action;
    }

    public List<BeforeSaveContext> getBeforeSaveContexts() {
        return beforeSaveContexts;
    }

    public List<AfterSaveContext> getAfterSaveContexts() {
        return afterSaveContexts;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setSupportedClass(Class<?> supportedClass) {
        this.supportedClass = supportedClass;
    }

    public void onBeforeLoad(Consumer<BeforeLoadContext> action) {
        this.beforeLoadAction = action;
    }

    public void onBeforeCount(Consumer<BeforeCountContext> action) {
        this.beforeCountAction = action;
    }

    public List<BeforeLoadContext> getBeforeLoadContexts() {
        return beforeLoadContexts;
    }

    public List<AfterLoadContext> getAfterLoadContexts() {
        return afterLoadContexts;
    }

    public List<BeforeCountContext> getBeforeCountContexts() {
        return beforeCountContexts;
    }

    /**
     * Clears the recorded contexts and restores the default supported class and actions.
     */
    public void reset() {
        supportedClass = Customer.class;
        beforeLoadAction = context -> {};
        beforeCountAction = context -> {};
        beforeLoadContexts.clear();
        afterLoadContexts.clear();
        beforeCountContexts.clear();
        beforeSaveAction = context -> {};
        beforeSaveContexts.clear();
        afterSaveContexts.clear();
    }
}
