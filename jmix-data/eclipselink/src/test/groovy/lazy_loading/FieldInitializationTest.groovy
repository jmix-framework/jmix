package lazy_loading

import io.jmix.core.EntityStates
import io.jmix.core.Id
import io.jmix.core.UnconstrainedDataManager
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.IgnoreIf
import test_support.DataSpec
import test_support.entity.lazyloading.nullability.AdditionalEntity
import test_support.entity.lazyloading.nullability.ChildEntity
import test_support.entity.lazyloading.nullability.ParentEntity
import test_support.entity.lazyloading.nullability.SpecificParentEntity

@IgnoreIf({ Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING")) })
class FieldInitializationTest extends DataSpec {
    @Autowired
    UnconstrainedDataManager dataManager
    @Autowired
    EntityStates entityStates

    def "Lazy loading works with NotInstantiated- field initializers"() {
        when:
        ParentEntity parent = dataManager.create(ParentEntity)
        parent.name = "p1"

        ChildEntity child = dataManager.create(ChildEntity)
        child.childname = "c1"
        child.parentEntity = parent

        AdditionalEntity additionalEntity = dataManager.create(AdditionalEntity)
        additionalEntity.name = "a1"

        parent.relatedAdditionalEntities = new LinkedHashSet<>()
        parent.relatedAdditionalEntities.add(additionalEntity)

        dataManager.save(additionalEntity, parent, child);

        // here the collection is loaded eagerly if field initializer instantiated
        ParentEntity loadedParent = dataManager.load(Id.of(parent)).one()
        List<ChildEntity> children = loadedParent.getChildren();

        // here you get UnfetchedAttribute exception in case of field initializer instantiated
        children.get(0).getParentEntity()

        //the same for Set
        Set<AdditionalEntity> additionalEntities = loadedParent.getRelatedAdditionalEntities()
        additionalEntities.iterator().next().parentEntities.get(0)

        then:
        noExceptionThrown()

        cleanup:
        jdbc.update('delete from CHILD_ENTITY')
        jdbc.update('delete from ADDITIONAL_ENTITY')
        jdbc.update('delete from PARENT_ENTITY')
    }

    def "NotInstantiatedList wrapped properly for inheritor properties in ancestor query"() {
        when:
        SpecificParentEntity parent = dataManager.create(SpecificParentEntity)
        parent.name = "p1"

        ChildEntity child = dataManager.create(ChildEntity)
        child.childname = "c1"
        child.parentEntity = parent

        ChildEntity additionalChild = dataManager.create(ChildEntity)
        additionalChild.childname = "c2"
        additionalChild = dataManager.save(additionalChild)

        AdditionalEntity additionalEntity = dataManager.create(AdditionalEntity)
        additionalEntity.name = "a1"

        parent.relatedAdditionalEntities = new LinkedHashSet<>()
        parent.relatedAdditionalEntities.add(additionalEntity)
        parent.additionalChildren = [additionalChild]
        additionalChild.additionalParentEntity = parent

        dataManager.save(additionalChild, additionalEntity, parent, child);

        // here the collection is loaded eagerly if field initializer instantiated
        ParentEntity loadedByParentClass = dataManager.load(Id.of(parent.getId(), ParentEntity.class)).one()
        List<ChildEntity> children = loadedByParentClass.getChildren();

        // here you get UnfetchedAttribute exception in case of field initializer instantiated
        children.get(0).getParentEntity()

        //the same for Set
        Set<AdditionalEntity> additionalEntities = loadedByParentClass.getRelatedAdditionalEntities()
        additionalEntities.iterator().next().parentEntities.get(0)

        then:
        noExceptionThrown()
        !entityStates.isLoaded(loadedByParentClass, "additionalChildren")
        loadedByParentClass.children==[child]

        when:
        def additionalChildren = ((SpecificParentEntity) loadedByParentClass).additionalChildren

        then:
        thrown(IllegalStateException) //see https://github.com/jmix-framework/jmix/issues/4346


        cleanup:
        jdbc.update('delete from CHILD_ENTITY')
        jdbc.update('delete from ADDITIONAL_ENTITY')
        jdbc.update('delete from PARENT_ENTITY')

    }
}
