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
package view_registry;

import com.vaadin.flow.component.html.Div;
import io.jmix.core.ClassManager;
import io.jmix.core.Resources;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.sys.ViewControllerDefinition;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.template.impl.ViewTemplateDefinitions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Timeout(20)
class ViewRegistryConcurrencyTest {

    @Test
    void loadingAViewWhileConfigurationsAreBeingReadDoesNotModifyTheActiveIterator() throws Exception {
        var configuration = new PausedConfiguration();
        var registry = new TestRegistry(configuration.provider);
        try (var executor = Executors.newFixedThreadPool(2)) {
            try {
                var reader = executor.submit(registry::getViewInfos);
                configuration.awaitRead();
                var loader = executor.submit(() -> registry.loadViewClass(LoadedView.class.getName()));
                awaitMutationAttempt(registry, loader);
                configuration.resume.countDown();

                reader.get(5, TimeUnit.SECONDS);
                loader.get(5, TimeUnit.SECONDS);
                assertThat(registry.getViewInfo("loaded").getControllerClass()).isEqualTo(LoadedView.class);
            } finally {
                configuration.resume.countDown();
                executor.shutdownNow();
            }
        }
    }

    @Test
    void resetDuringInitializationIsNotOverwrittenByThatInitialization() throws Exception {
        var configuration = new PausedConfiguration();
        var registry = new TestRegistry(configuration.provider);
        try (var executor = Executors.newFixedThreadPool(2)) {
            try {
                var reader = executor.submit(registry::getViewInfos);
                configuration.awaitRead();
                configuration.definitions.set(List.of(new ViewControllerDefinition("loaded", LoadedView.class.getName())));
                var reset = executor.submit(registry::reset);
                awaitMutationAttempt(registry, reset);
                configuration.resume.countDown();

                reader.get(5, TimeUnit.SECONDS);
                reset.get(5, TimeUnit.SECONDS);
                assertThat(registry.findViewInfo("loaded")).isPresent();
            } finally {
                configuration.resume.countDown();
                executor.shutdownNow();
            }
        }
    }

    @Test
    void returnedViewInfosRemainStableWhenTheRegistryIsReloaded() {
        var provider = mock(ViewControllersConfiguration.class);
        var registry = new TestRegistry(provider);
        var previous = registry.getViewInfos();

        registry.loadViewClass(LoadedView.class.getName());

        assertThat(registry.getViewInfos()).extracting(ViewInfo::getId).containsExactly("loaded");
        assertThat(previous).isEmpty();
    }

    private void awaitMutationAttempt(TestRegistry registry, Future<?> mutation) {
        // On the old implementation the mutation completes during the paused read;
        // on the fixed implementation it queues behind the registry's write lock.
        await().atMost(Duration.ofSeconds(5)).until(() -> mutation.isDone() || registry.hasQueuedMutation());
    }

    private static class PausedConfiguration {
        final ViewControllersConfiguration provider = mock(ViewControllersConfiguration.class);
        final AtomicReference<List<ViewControllerDefinition>> definitions = new AtomicReference<>(List.of());
        final CountDownLatch reading = new CountDownLatch(1);
        final CountDownLatch resume = new CountDownLatch(1);

        PausedConfiguration() {
            var firstRead = new AtomicBoolean(true);
            when(provider.getViewControllers()).thenAnswer(invocation -> {
                var snapshot = definitions.get();
                if (firstRead.getAndSet(false)) {
                    reading.countDown();
                    if (!resume.await(5, TimeUnit.SECONDS)) {
                        throw new AssertionError("Configuration read was not resumed");
                    }
                }
                return snapshot;
            });
        }

        void awaitRead() throws InterruptedException {
            assertThat(reading.await(5, TimeUnit.SECONDS)).as("registry is iterating configurations").isTrue();
        }
    }

    private static class TestRegistry extends ViewRegistry {
        TestRegistry(ViewControllersConfiguration provider) {
            applicationContext = new StaticApplicationContext();
            metadataReaderFactory = new AnnotationScanMetadataReaderFactory(applicationContext);
            configurations = new ArrayList<>(List.of(provider));
            classManager = mock(ClassManager.class);
            doReturn(LoadedView.class).when(classManager).loadClass(LoadedView.class.getName());
            resources = mock(Resources.class);
            when(resources.getResource(anyString())).thenAnswer(invocation -> new ClassPathResource(invocation.getArgument(0)));
            viewTemplateDefinitions = mock(ViewTemplateDefinitions.class);
        }

        boolean hasQueuedMutation() {
            return ((ReentrantReadWriteLock) lock).hasQueuedThreads();
        }

        @Override
        public void registerRoute(Class<? extends View<?>> viewClass) {
            // Route registration belongs to Vaadin; registry loading remains real in these tests.
        }
    }

    @ViewController("loaded")
    public static class LoadedView extends View<Div> {
    }
}
