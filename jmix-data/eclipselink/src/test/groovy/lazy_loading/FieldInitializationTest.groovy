package lazy_loading

import io.jmix.core.Id
import io.jmix.core.UnconstrainedDataManager
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.lazyloading.nullability.AdditionalEntity
import test_support.entity.lazyloading.nullability.ChildEntity
import test_support.entity.lazyloading.nullability.ParentEntity

class FieldInitializationTest extends DataSpec {
    @Autowired
    UnconstrainedDataManager dataManager;

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
    }
}
