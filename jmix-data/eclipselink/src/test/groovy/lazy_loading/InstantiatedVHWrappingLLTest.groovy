/*
 * Copyright 2025 Haulmont.
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

package lazy_loading

import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.accesscontext.AccessContext
import io.jmix.core.accesscontext.InMemoryCrudEntityContext
import io.jmix.core.constraint.AccessConstraint
import io.jmix.core.constraint.InMemoryConstraint
import io.jmix.core.metamodel.model.MetaProperty
import io.jmix.eclipselink.impl.lazyloading.AbstractValueHolder
import io.jmix.eclipselink.impl.lazyloading.CollectionValuePropertyHolder
import io.jmix.eclipselink.impl.lazyloading.SingleValueMappedByPropertyHolder
import io.jmix.eclipselink.impl.lazyloading.ValueHoldersSupport
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.apache.commons.lang3.RandomStringUtils
import org.eclipse.persistence.internal.indirection.UnitOfWorkValueHolder
import org.eclipse.persistence.indirection.ValueHolderInterface
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import spock.lang.IgnoreIf
import test_support.DataSpec
import test_support.entity.lazyloading.instantiated_vh_wrapping.ElementCollectionHolder
import test_support.entity.lazyloading.instantiated_vh_wrapping.Second
import test_support.entity.lazyloading.instantiated_vh_wrapping.Third
import test_support.entity.lazyloading.instantiated_vh_wrapping.First
import test_support.entity.lazyloading.recursive_collection.RecursiveCollectionChild
import test_support.entity.lazyloading.recursive_collection.RecursiveCollectionOwner
import test_support.entity.lazyloading.recursive_collection.RecursiveCollectionRoot

import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.BiPredicate


@TestPropertySource(properties = [
        "eclipselink.cache.shared.ivw_First=true",
        "eclipselink.cache.shared.ivw_Second=true",
        "eclipselink.cache.shared.ivw_Third=true",
        "eclipselink.cache.shared.test_RcOwner=true"])
@IgnoreIf({ Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING")) })
class InstantiatedVHWrappingLLTest extends DataSpec {
    @Autowired
    DataManager dataManager

    @Autowired
    EntityStates entityStates

    @PersistenceContext
    EntityManager entityManager


    def "Instantiated ValueHolder wrapping test"() {
        setup:
        def newFirst = dataManager.create(First)
        newFirst.name = RandomStringUtils.insecure().nextAlphabetic(10)

        def newSecond = dataManager.create(Second)
        newSecond.first = newFirst
        newFirst.second = newSecond

        def newThird = dataManager.create(Third)
        newThird.second = newSecond
        newThird.secondForCollection = newSecond
        newSecond.third = newThird
        newSecond.thirds = List.of(newThird)

        dataManager.save(newFirst, newSecond, newThird)

        when: "Lazy-loading nested entities with back references and cache enabled"
        def first = dataManager.load(First).id(newFirst.id).one()

        SingleValueMappedByPropertyHolder testEntityVH = ValueHoldersSupport.getSingleValueHolder(first, "second")
        //forces value holder to reload value which makes eclipselink return value holder already instantiated by cached entity
        testEntityVH.getLoadOptions().getAccessConstraints().add(new TestDummyAccessConstraint())

        def loadedSecond = first.second
        def loadedThird = loadedSecond.third
        def loadedSecondBackReference = loadedThird.second
        def loadedSecondBackReferenceForCollection = loadedThird.secondForCollection

        then: "Already instantiated (from cache) value holders transformed correctly to entity value"
        noExceptionThrown()
        loadedSecondBackReference != null
        loadedSecondBackReferenceForCollection != null
    }

    def "test managed collection loading after intermediate owner list load"() {
        setup:
        cleanupRecursiveCollectionGraph()
        def savedRoot = createRecursiveCollectionGraph()

        when:
        def childNames = loadChildNamesAfterIntermediateOwnerListLoad(savedRoot.id)

        then:
        noExceptionThrown()
        childNames == ["Child-1", "Child-2", "Child-3", "Child-4"]

        cleanup:
        cleanupRecursiveCollectionGraph()
    }

    def "test constrained managed collection loading after intermediate owner list load"() {
        setup:
        cleanupRecursiveCollectionGraph()
        def savedRoot = createRecursiveCollectionGraph()

        when:
        def childNames = loadChildNamesAfterIntermediateOwnerListLoad(savedRoot.id,
                Collections.singleton(new ChildNameConstraint()))

        then:
        noExceptionThrown()
        childNames == ["Child-1", "Child-2", "Child-3"]

        cleanup:
        cleanupRecursiveCollectionGraph()
    }

    def "test detached collection owner lazy loading does not recurse"() {
        setup:
        cleanupRecursiveCollectionGraph()
        def savedRoot = createRecursiveCollectionGraph()

        when:
        RecursiveCollectionRoot detachedRoot = dataManager.load(RecursiveCollectionRoot)
                .id(savedRoot.id)
                .fetchPlan { fp -> fp.add("owner") }
                .one()
        RecursiveCollectionOwner detachedOwner = detachedRoot.owner
        def valueHolder = ValueHoldersSupport.getCollectionValueHolder(detachedOwner, "children")
        def childNames = detachedOwner.children
                .collect { it.name }
                .sort()

        then:
        noExceptionThrown()
        !entityStates.isManaged(detachedOwner)
        valueHolder instanceof CollectionValuePropertyHolder
        childNames == ["Child-1", "Child-2", "Child-3", "Child-4"]

        cleanup:
        cleanupRecursiveCollectionGraph()
    }

    def "test recursive delegation does not instantiate holder after outer load failure"() {
        setup:
        def originalValueHolder = Mock(UnitOfWorkValueHolder)
        originalValueHolder.getValue() >> []
        def metaProperty = Mock(MetaProperty)
        metaProperty.getName() >> "children"
        metaProperty.getJavaType() >> List
        metaProperty.getInverse() >> null
        def valueHolder = new FailingRecursiveValueHolder(originalValueHolder, metaProperty)

        when:
        valueHolder.value

        then:
        thrown(IllegalStateException)
        !valueHolder.instantiated
    }

    def "test recursive delegate value is not visible before outer load completes"() {
        setup:
        def originalValueHolder = Mock(UnitOfWorkValueHolder)
        originalValueHolder.getValue() >> ["delegated"]
        def metaProperty = Mock(MetaProperty)
        metaProperty.getName() >> "children"
        metaProperty.getJavaType() >> List
        metaProperty.getInverse() >> null
        def outerLoadRelease = new CountDownLatch(1)
        def valueHolder = new BlockingRecursiveValueHolder(originalValueHolder, metaProperty, outerLoadRelease)
        def outerExecutor = Executors.newSingleThreadExecutor()
        def concurrentExecutor = Executors.newFixedThreadPool(2)
        def concurrentGetValueStarted = new CountDownLatch(1)
        def concurrentIsInstantiatedStarted = new CountDownLatch(1)

        when:
        def outerFuture = outerExecutor.submit({ valueHolder.value } as Callable<List<String>>)
        assert valueHolder.awaitRecursiveDelegation()
        assert valueHolder.storedValueAfterRecursiveDelegation == null
        assert !valueHolder.rawInstantiated
        assert valueHolder.instantiatedDuringRecursiveDelegation
        def concurrentIsInstantiatedFuture = concurrentExecutor.submit({
            concurrentIsInstantiatedStarted.countDown()
            return valueHolder.instantiated
        } as Callable<Boolean>)
        assert concurrentIsInstantiatedStarted.await(1, TimeUnit.SECONDS)
        concurrentIsInstantiatedFuture.get(200, TimeUnit.MILLISECONDS)

        then:
        thrown(TimeoutException)

        when:
        def concurrentGetValueFuture = concurrentExecutor.submit({
            concurrentGetValueStarted.countDown()
            return valueHolder.value
        } as Callable<List<String>>)
        assert concurrentGetValueStarted.await(1, TimeUnit.SECONDS)
        concurrentGetValueFuture.get(200, TimeUnit.MILLISECONDS)

        then:
        thrown(TimeoutException)

        when:
        outerLoadRelease.countDown()

        then:
        outerFuture.get(1, TimeUnit.SECONDS) == ["final"]
        concurrentIsInstantiatedFuture.get(1, TimeUnit.SECONDS)
        concurrentGetValueFuture.get(1, TimeUnit.SECONDS) == ["final"]

        cleanup:
        outerLoadRelease?.countDown()
        outerExecutor?.shutdownNow()
        concurrentExecutor?.shutdownNow()
    }

    def "test query by datatype element collection does not fail excessive value holder wrapping"() {
        setup:
        cleanupElementCollectionHolders()
        def savedHolder = createElementCollectionHolder("holder-1", ["tag-1", "tag-2"])

        when:
        def loadedHolders = dataManager.load(ElementCollectionHolder)
                .query("select e from ivw_ElementCollectionHolder e where :tag member of e.tags")
                .parameter("tag", "tag-1")
                .list()

        then:
        noExceptionThrown()
        loadedHolders*.id == [savedHolder.id]

        cleanup:
        cleanupElementCollectionHolders()
    }

    def "test loaded datatype element collection is ignored during excessive value holder wrapping"() {
        setup:
        cleanupElementCollectionHolders()
        def savedHolder = createElementCollectionHolder("holder-1", ["tag-1", "tag-2"])

        when:
        def loadedIds = transaction.execute {
            ElementCollectionHolder managedHolder = entityManager.find(ElementCollectionHolder, savedHolder.id)
            managedHolder.tags.size()
            assert entityStates.isLoaded(managedHolder, "tags")

            return dataManager.load(ElementCollectionHolder)
                    .all()
                    .list()
                    .collect { it.id }
        }

        then:
        noExceptionThrown()
        loadedIds == [savedHolder.id]

        cleanup:
        cleanupElementCollectionHolders()
    }

    def "test managed datatype element collection loading after intermediate holder list load"() {
        setup:
        cleanupElementCollectionHolders()
        def savedHolder = createElementCollectionHolder("holder-1", ["tag-1", "tag-2"])

        when:
        def loadedTags = transaction.execute {
            ElementCollectionHolder managedHolder = entityManager.find(ElementCollectionHolder, savedHolder.id)
            assert !entityStates.isLoaded(managedHolder, "tags")

            dataManager.load(ElementCollectionHolder)
                    .all()
                    .list()

            return new ArrayList<>(managedHolder.tags)
        }

        then:
        noExceptionThrown()
        loadedTags.size() == 2
        loadedTags.containsAll(["tag-1", "tag-2"])

        cleanup:
        cleanupElementCollectionHolders()
    }

    private RecursiveCollectionRoot createRecursiveCollectionGraph() {
        def owner = dataManager.create(RecursiveCollectionOwner)
        owner.name = "Owner"

        def child1 = createRecursiveCollectionChild(owner, "Child-1")
        def child2 = createRecursiveCollectionChild(owner, "Child-2")
        def child3 = createRecursiveCollectionChild(owner, "Child-3")
        def child4 = createRecursiveCollectionChild(owner, "Child-4")

        def root = dataManager.create(RecursiveCollectionRoot)
        root.name = "Root"
        root.owner = owner

        dataManager.save(owner, child1, child2, child3, child4, root)
        return root
    }

    private RecursiveCollectionChild createRecursiveCollectionChild(RecursiveCollectionOwner owner, String name) {
        def child = dataManager.create(RecursiveCollectionChild)
        child.name = name
        child.owner = owner
        return child
    }

    private List<String> loadChildNamesAfterIntermediateOwnerListLoad(UUID rootId,
                                                                      Collection<? extends AccessConstraint<? extends AccessContext>> accessConstraints = Collections.emptyList()) {
        return transaction.execute {
            RecursiveCollectionRoot managedRoot = entityManager.find(RecursiveCollectionRoot, rootId)
            RecursiveCollectionOwner managedOwner = managedRoot.owner

            dataManager.load(RecursiveCollectionRoot)
                    .all()
                    .accessConstraints(accessConstraints)
                    .list()

            def valueHolder = ValueHoldersSupport.getCollectionValueHolder(managedOwner, "children")
            assert valueHolder instanceof CollectionValuePropertyHolder

            return managedOwner.children
                    .collect { it.name }
                    .sort()
        }
    }

    private void cleanupRecursiveCollectionGraph() {
        jdbc.update("delete from TEST_LL_RC_CHILD")
        jdbc.update("delete from TEST_LL_RC_ROOT")
        jdbc.update("delete from TEST_LL_RC_OWNER")
    }

    private ElementCollectionHolder createElementCollectionHolder(String name, List<String> tags) {
        def holder = dataManager.create(ElementCollectionHolder)
        holder.name = name
        holder.tags = tags
        return dataManager.save(holder)
    }

    private void cleanupElementCollectionHolders() {
        jdbc.update("delete from IVW_ELEMENT_COLLECTION_HOLDER_TAG")
        jdbc.update("delete from IVW_ELEMENT_COLLECTION_HOLDER")
    }


    static class TestDummyAccessContext implements AccessContext {
    }

    static class ChildNameConstraint implements InMemoryConstraint<InMemoryCrudEntityContext> {

        @Override
        Class<InMemoryCrudEntityContext> getContextType() {
            return InMemoryCrudEntityContext.class
        }

        @Override
        void applyTo(InMemoryCrudEntityContext context) {
            if (context.entityClass.javaClass == RecursiveCollectionChild) {
                context.addReadPredicate({ entity, applicationContext ->
                    ((RecursiveCollectionChild) entity).name != "Child-4"
                } as BiPredicate)
            }
        }
    }

    static class BlockingRecursiveValueHolder extends AbstractValueHolder {

        private final CountDownLatch recursiveDelegationCompleted = new CountDownLatch(1)
        private final CountDownLatch outerLoadRelease
        private Object storedValueAfterRecursiveDelegation
        private boolean instantiatedDuringRecursiveDelegation

        BlockingRecursiveValueHolder(ValueHolderInterface originalValueHolder, MetaProperty metaProperty,
                                     CountDownLatch outerLoadRelease) {
            super(null, originalValueHolder, new Object(), metaProperty)
            this.outerLoadRelease = outerLoadRelease
        }

        @Override
        protected Object loadValue() {
            getValue()
            storedValueAfterRecursiveDelegation = getStoredValue()
            instantiatedDuringRecursiveDelegation = isInstantiated()
            recursiveDelegationCompleted.countDown()
            outerLoadRelease.await()
            return ["final"]
        }

        @Override
        protected void afterLoadValue(Object value) {
        }

        @Override
        protected void registerLoadedProperty(Object entity, String property) {
        }

        @Override
        protected boolean shouldDelegateOnRecursiveLoad() {
            return true
        }

        boolean awaitRecursiveDelegation() {
            return recursiveDelegationCompleted.await(1, TimeUnit.SECONDS)
        }

        Object getStoredValueAfterRecursiveDelegation() {
            return storedValueAfterRecursiveDelegation
        }

        boolean isInstantiatedDuringRecursiveDelegation() {
            return instantiatedDuringRecursiveDelegation
        }

        boolean isRawInstantiated() {
            def isInstantiatedField = AbstractValueHolder.getDeclaredField("isInstantiated")
            isInstantiatedField.accessible = true
            return isInstantiatedField.getBoolean(this)
        }

        private Object getStoredValue() {
            def valueField = AbstractValueHolder.getDeclaredField("value")
            valueField.accessible = true
            return valueField.get(this)
        }

    }

    static class FailingRecursiveValueHolder extends AbstractValueHolder {

        FailingRecursiveValueHolder(ValueHolderInterface originalValueHolder, MetaProperty metaProperty) {
            super(null, originalValueHolder, new Object(), metaProperty)
        }

        @Override
        protected Object loadValue() {
            getValue()
            throw new IllegalStateException("Outer load failed")
        }

        @Override
        protected void afterLoadValue(Object value) {
        }

        @Override
        protected boolean shouldDelegateOnRecursiveLoad() {
            return true
        }
    }

    static class TestDummyAccessConstraint implements AccessConstraint<TestDummyAccessContext> {
        @Override
        Class<TestDummyAccessContext> getContextType() {
            return TestDummyAccessContext.class
        }

        @Override
        void applyTo(TestDummyAccessContext context) {
        }
    }
}
