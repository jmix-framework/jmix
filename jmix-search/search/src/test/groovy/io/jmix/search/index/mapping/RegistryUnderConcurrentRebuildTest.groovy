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

package io.jmix.search.index.mapping

import io.jmix.core.InstanceNameProvider
import io.jmix.search.index.IndexConfiguration
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Reading the set of index configurations while it is being rebuilt.
 *
 * <p>A rebuild happens when an administrator recomputes the index definitions and when a new metadata generation
 * is published. Reading happens all the time: the queue builds its query every few seconds, the change listener
 * asks on every save, every search asks for the scope. The two meet.
 *
 * <p>This test does not prove the absence of a race - it catches one. It is reliable here because the window is
 * wide: walking a thousand configurations takes long enough to be interrupted.
 */
class RegistryUnderConcurrentRebuildTest extends Specification {

    static final int ENTITY_COUNT = 1000

    @Timeout(60)
    def "a reader walking the configurations is unaffected by a rebuild"() {
        given:
        def configurations = configurations()
        def holder = new RegistryHolder(Stub(InstanceNameProvider))
        holder.rebuild(configurations)

        def failures = new ConcurrentLinkedQueue<Throwable>()
        def stop = new AtomicBoolean(false)
        def started = new CountDownLatch(4)

        def readers = (1..3).collect {
            new Thread({
                started.countDown()
                while (!stop.get()) {
                    try {
                        int seen = 0
                        for (IndexConfiguration ignored : holder.registry.getIndexConfigurations()) {
                            seen++
                        }
                        if (seen != ENTITY_COUNT) {
                            failures.add(new AssertionError("a half-built set of ${seen} configurations" as Object))
                        }
                    } catch (Throwable t) {
                        failures.add(t)
                    }
                }
            } as Runnable)
        }
        def writer = new Thread({
            started.countDown()
            while (!stop.get()) {
                holder.rebuild(configurations)
            }
        } as Runnable)

        when:
        (readers + [writer])*.start()
        started.await(10, TimeUnit.SECONDS)
        Thread.sleep(2000)
        stop.set(true)
        (readers + [writer])*.join(10000)

        then: """a reader must either see the set it was given or a later one, never a set in the middle of being
                 assembled, and never an exception"""
        failures.isEmpty()
    }

    protected List<IndexConfiguration> configurations() {
        def mapping = Stub(IndexMappingConfiguration) {
            getFields() >> [:]
            getDisplayedNameDescriptor() >> Stub(DisplayedNameDescriptor) {
                getInstanceNameRelatedProperties() >> []
            }
        }
        return (1..ENTITY_COUNT).collect { number ->
            new IndexConfiguration("test_Entity${number}", Object, mapping, Set.of(), { true }, null, false, null)
        }
    }

    /**
     * Stands for the way {@code IndexConfigurationManager} keeps its registry: the rebuild assembles one nobody
     * has seen yet and publishes it, instead of refilling the one readers are walking.
     */
    static class RegistryHolder {

        final InstanceNameProvider instanceNameProvider
        volatile IndexConfigurationManager.Registry registry

        RegistryHolder(InstanceNameProvider instanceNameProvider) {
            this.instanceNameProvider = instanceNameProvider
            this.registry = new IndexConfigurationManager.Registry(instanceNameProvider)
        }

        void rebuild(List<IndexConfiguration> configurations) {
            def fresh = new IndexConfigurationManager.Registry(instanceNameProvider)
            configurations.each { fresh.registerIndexConfiguration(it) }
            registry = fresh
        }
    }
}
