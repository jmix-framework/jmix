package cascade_operations

import io.jmix.core.*
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.core.metamodel.model.MetaProperty
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.cascade_operations.JpaCascadeBar
import test_support.entity.cascade_operations.JpaCascadeFoo

import javax.persistence.CascadeType
import java.util.stream.Collectors

class CommonCascadeTest extends DataSpec {


    @Autowired
    private MetadataTools metadataTools

    @Autowired
    private Metadata metadata

    @Autowired
    private DataManager dataManager

    @Autowired
    private FetchPlans fetchPlans


    def "Check metadata for reference properties"() {

        when:
        MetaClass metaClass = metadata.getClass(JpaCascadeFoo)

        def anyCascadeProperties = metadataTools.getCascadeProperties(metaClass, null)
                .stream()
                .map(MetaProperty::getName)
                .collect(Collectors.toList())

        def persistProperties = metadataTools.getCascadeProperties(metaClass, CascadeType.PERSIST)
                .stream()
                .map(MetaProperty::getName)
                .collect(Collectors.toList())

        def mergeProperties = metadataTools.getCascadeProperties(metaClass, CascadeType.MERGE)
                .stream()
                .map(MetaProperty::getName)
                .collect(Collectors.toList())

        def detachProperties = metadataTools.getCascadeProperties(metaClass, CascadeType.DETACH)
                .stream()
                .map(MetaProperty::getName)
                .collect(Collectors.toList())

        def refreshProperties = metadataTools.getCascadeProperties(metaClass, CascadeType.REFRESH)
                .stream()
                .map(MetaProperty::getName)
                .collect(Collectors.toList())

        def removeProperties = metadataTools.getCascadeProperties(metaClass, CascadeType.REMOVE)
                .stream()
                .map(MetaProperty::getName)
                .collect(Collectors.toList())

        MetaProperty barProperty = metaClass.getProperty("bar")
        MetaProperty barPProperty = metaClass.getProperty("barP")
        MetaProperty barDRProperty = metaClass.getProperty("barDR")
        MetaProperty barNonCascadeProperty = metaClass.getProperty("nonCascadeBar")

        then:
        anyCascadeProperties.size() == 6
        anyCascadeProperties.containsAll(["bar", "barP", "barM", "barDR", "barR", "items"])

        persistProperties.size() == 3
        persistProperties.containsAll(["bar", "barP", "items"])

        mergeProperties.size() == 3
        mergeProperties.containsAll(["bar", "barM", "items"])

        detachProperties.size() == 3
        detachProperties.containsAll(["bar", "barDR", "items"])

        refreshProperties.size() == 3
        refreshProperties.containsAll(["bar", "barDR", "items"])

        removeProperties.size() == 3
        removeProperties.containsAll(["bar", "barR", "items"])

        metadataTools.getCascadeTypes(barProperty) == [CascadeType.ALL]
        metadataTools.getCascadeTypes(barPProperty) == [CascadeType.PERSIST]
        metadataTools.getCascadeTypes(barDRProperty).size() == 2
        metadataTools.getCascadeTypes(barDRProperty).containsAll([CascadeType.DETACH, CascadeType.REFRESH])
        metadataTools.getCascadeTypes(barNonCascadeProperty).isEmpty()
    }

    def "Check metadata for embedded properties"() {
        when:
        MetaClass metaClass = metadata.getClass(JpaCascadeFoo)
        List<String> embeddedProperties = metadataTools.getEmbeddedProperties(metaClass)

        then: "List of embedded properties is correct"
        embeddedProperties == ["embeddable"]

        when:
        MetaClass embeddedPropertyClass = metaClass.getProperty(embeddedProperties[0]).getRange().asClass()

        then: "Cascade properties inside embedded entity registered correctly"
        metadataTools.getCascadeProperties(embeddedPropertyClass, CascadeType.ALL).size() == 1
        metadataTools.getCascadeProperties(embeddedPropertyClass, CascadeType.ALL)[0].name == "barInside"
    }

    def "Cascade type considered correctly"() {
        when: "entity persisted"
        def foo = dataManager.create(JpaCascadeFoo)
        foo.name = "testFoo"

        def bar1 = dataManager.create(JpaCascadeBar)
        bar1.name = "bar1"

        def bar2 = dataManager.create(JpaCascadeBar)
        bar2.name = "bar2"

        def bar3 = dataManager.create(JpaCascadeBar)
        bar3.name = "bar3"

        def bar4 = dataManager.create(JpaCascadeBar)
        bar4.name = "bar4"

        def bar5 = dataManager.create(JpaCascadeBar)
        bar5.name = "bar5"

        foo.setNonCascadeBar(bar2)

        dataManager.save(foo)

        then: "property without appropriate cascade type cannot be persisted"
        thrown(IllegalStateException)

        when: "entity merged"

        foo.setBarP(bar1)
        foo.setNonCascadeBar(null)

        dataManager.save(bar1, bar2, bar3, bar4, bar5, foo)

        def loaded = dataManager.load(JpaCascadeFoo)
                .id(foo.id)
                .fetchPlan(fetchPlans.builder(JpaCascadeFoo)
                        .add("barP", FetchPlan.LOCAL)
                        .add("nonCascadeBar", FetchPlan.LOCAL)
                        .add("barDR", FetchPlan.LOCAL)
                        .build()
                ).one()

        loaded.barP.name = "bar1_upadted"

        loaded.barM = bar2
        loaded.barM.name = "bar2_updated"

        loaded.nonCascadeBar = bar3
        loaded.nonCascadeBar.name = "bar3_updated"
        loaded.barDR = bar4
        loaded.barDR.name = "bar4_updated"
        loaded.barR = [bar5]
        bar3.name = "bar5_updated"

        dataManager.save(loaded)

        def loaded2 = dataManager.load(JpaCascadeFoo)
                .id(foo.id)
                .fetchPlan(fetchPlans.builder(JpaCascadeFoo)
                        .add("barP", FetchPlan.LOCAL)
                        .add("barM", FetchPlan.LOCAL)
                        .add("nonCascadeBar", FetchPlan.LOCAL)
                        .add("barDR", FetchPlan.LOCAL)
                        .add("barR", FetchPlan.LOCAL)
                        .build()
                ).one()


        then: "cascaded property with appropriate type only"
        loaded2.barP.name == "bar1"
        loaded2.barM.name == "bar2_updated"
        loaded2.nonCascadeBar.name == "bar3"
        loaded2.barDR.name == "bar4"
        loaded2.barR[0].name == "bar5"

        cleanup:
        bar2 = loaded2.barM
        dataManager.remove(loaded2, bar1, bar2, bar3, bar4, bar5)
    }
}
