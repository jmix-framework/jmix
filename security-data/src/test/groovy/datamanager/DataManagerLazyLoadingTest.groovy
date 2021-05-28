package datamanager

import io.jmix.core.AccessConstraintsRegistry
import io.jmix.core.Metadata
import io.jmix.core.UnconstrainedDataManager
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.security.authentication.RoleGrantedAuthority
import io.jmix.security.role.ResourceRoleRepository
import io.jmix.security.role.RowLevelRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.SecurityDataSpecification
import test_support.entity.ManyToManyFirstEntity
import test_support.entity.ManyToManySecondEntity
import test_support.entity.ManyToOneEntity
import test_support.entity.OneToManyEntity
import test_support.role.TestLazyLoadingRole

import javax.sql.DataSource

class DataManagerLazyLoadingTest extends SecurityDataSpecification {
    @Autowired
    UnconstrainedDataManager dataManager

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    ResourceRoleRepository resourceRoleRepository

    @Autowired
    RowLevelRoleRepository rowLevelRoleRepository

    @Autowired
    Metadata metadata

    @Autowired
    AccessConstraintsRegistry accessConstraintsRegistry

    @Autowired
    DataSource dataSource

    UserDetails user1

    UUID manyToOneId, oneToManyId

    Authentication systemAuthentication

    public static final String PASSWORD = "123"

    def setup() {
        user1 = User.builder()
                .username("user1")
                .password("{noop}$PASSWORD")
                .authorities(RoleGrantedAuthority.
                        withRowLevelRoleProvider({ rowLevelRoleRepository.getRoleByCode(it) })
                        .withResourceRoleProvider({ resourceRoleRepository.getRoleByCode(it) })
                        .withRowLevelRoles(TestLazyLoadingRole.NAME)
                        .withResourceRoles(TestLazyLoadingRole.NAME)
                        .build())
                .build()
        userRepository.addUser(user1)

        prepareManyToOne()
        prepareManyToMany()

        systemAuthentication = SecurityContextHelper.getAuthentication()
    }

    def cleanup() {
        SecurityContextHelper.setAuthentication(systemAuthentication)

        userRepository.removeUser(user1)

        new JdbcTemplate(dataSource).execute('delete from TEST_MANY_TO_MANY_FIRST_ENTITY_MANY_TO_MANY_SECOND_ENTITY_LINK;' +
                ' delete from TEST_MANY_TO_MANY_FIRST_ENTITY;' +
                ' delete from TEST_MANY_TO_MANY_SECOND_ENTITY;' +
                ' delete from TEST_MANY_TO_ONE_ENTITY;' +
                ' delete from TEST_ONE_TO_MANY_ENTITY;')
    }

    def "lazy load manyToOne with constraints"() {
        setup:

        authenticate('user1')

        when:

        def manyToOneEntity = dataManager.load(ManyToOneEntity.class)
                .id(manyToOneId)
                .accessConstraints(accessConstraintsRegistry.getConstraints())
                .one()
        manyToOneEntity.getOneToManyEntity()
        dataManager.save(manyToOneEntity)

        def oneToManyEntity = dataManager.load(OneToManyEntity.class)
                .id(oneToManyId)
                .one()

        manyToOneEntity = dataManager.load(ManyToOneEntity.class)
                .id(manyToOneId)
                .one()

        then:

        oneToManyEntity.manyToOneEntities.size() == 2
        manyToOneEntity.oneToManyEntity == oneToManyEntity
    }

    def "lazy load manyToMany with constraints"() {
        setup:

        authenticate('user1')

        when:

        def manyToManySecondEntity = dataManager.load(ManyToManySecondEntity.class)
                .all()
                .accessConstraints(accessConstraintsRegistry.getConstraints())
                .list().iterator().next()
        def manyToManySecondEntityWithoutConstraints = dataManager.load(ManyToManySecondEntity.class)
                .all().list().iterator().next()

        then:

        manyToManySecondEntity.getManyToManyFirstEntities().size() == 2
        manyToManySecondEntityWithoutConstraints.getManyToManyFirstEntities().size() == 5

        when:

        dataManager.save(manyToManySecondEntity)
        def id = manyToManySecondEntity.__getEntityEntry().getEntityId()
        def resultWithConstraints = dataManager.load(ManyToManySecondEntity.class)
                .id(id)
                .accessConstraints(accessConstraintsRegistry.getConstraints())
                .one()
        def resultWithoutConstraints = dataManager.load(ManyToManySecondEntity.class)
                .id(id)
                .one()

        then:

        resultWithConstraints.getManyToManyFirstEntities().size() == 2
        resultWithoutConstraints.getManyToManyFirstEntities().size() == 5
    }

    protected void authenticate(String username) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }

    def prepareManyToMany() {
        ManyToManyFirstEntity manyToManyEntity1 = metadata.create(ManyToManyFirstEntity.class)
        manyToManyEntity1.setName("1")
        dataManager.save(manyToManyEntity1)

        ManyToManyFirstEntity manyToManyEntity2 = metadata.create(ManyToManyFirstEntity.class)
        manyToManyEntity2.setName("2")
        dataManager.save(manyToManyEntity2)

        ManyToManyFirstEntity manyToManyEntity3 = metadata.create(ManyToManyFirstEntity.class)
        manyToManyEntity3.setName("3")
        dataManager.save(manyToManyEntity3)

        ManyToManyFirstEntity manyToManyEntity4 = metadata.create(ManyToManyFirstEntity.class)
        manyToManyEntity4.setName("allowed_4")
        dataManager.save(manyToManyEntity4)

        ManyToManyFirstEntity manyToManyEntity5 = metadata.create(ManyToManyFirstEntity.class)
        manyToManyEntity5.setName("allowed_5")
        dataManager.save(manyToManyEntity5)

        List<ManyToManyFirstEntity> manyToManyEntities = new ArrayList<>()
        manyToManyEntities.add(manyToManyEntity1)
        manyToManyEntities.add(manyToManyEntity2)
        manyToManyEntities.add(manyToManyEntity3)
        manyToManyEntities.add(manyToManyEntity4)
        manyToManyEntities.add(manyToManyEntity5)

        ManyToManySecondEntity manyToManyTwoEntity1 = metadata.create(ManyToManySecondEntity.class)
        manyToManyTwoEntity1.setName("1")
        manyToManyTwoEntity1.setManyToManyFirstEntities(manyToManyEntities)
        dataManager.save(manyToManyTwoEntity1)

        ManyToManySecondEntity manyToManyTwoEntity2 = metadata.create(ManyToManySecondEntity.class)
        manyToManyTwoEntity2.setName("allowed_2")
        manyToManyTwoEntity2.setManyToManyFirstEntities(manyToManyEntities)
        dataManager.save(manyToManyTwoEntity2)
    }

    def prepareManyToOne() {
        OneToManyEntity oneToManyEntity = metadata.create(OneToManyEntity.class)
        oneToManyEntity.setName("1")
        dataManager.save(oneToManyEntity)
        oneToManyId = oneToManyEntity.__getEntityEntry().getEntityId()

        ManyToOneEntity manyToOneEntity = metadata.create(ManyToOneEntity.class)
        manyToOneEntity.setName("1")
        manyToOneEntity.setOneToManyEntity(oneToManyEntity)
        dataManager.save(manyToOneEntity)

        manyToOneEntity = metadata.create(ManyToOneEntity.class)
        manyToOneEntity.setName("allowed_1")
        manyToOneEntity.setOneToManyEntity(oneToManyEntity)
        dataManager.save(manyToOneEntity)
        manyToOneId = manyToOneEntity.__getEntityEntry().getEntityId()

        oneToManyEntity = metadata.create(OneToManyEntity.class)
        oneToManyEntity.setName("allowed_2")
        dataManager.save(oneToManyEntity)

        manyToOneEntity = metadata.create(ManyToOneEntity.class)
        manyToOneEntity.setName("2")
        manyToOneEntity.setOneToManyEntity(oneToManyEntity)
        dataManager.save(manyToOneEntity)

        manyToOneEntity = metadata.create(ManyToOneEntity.class)
        manyToOneEntity.setName("allowed_2")
        manyToOneEntity.setOneToManyEntity(oneToManyEntity)
        dataManager.save(manyToOneEntity)
    }
}
