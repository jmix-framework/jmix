package declarative_screen_load

import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.UI
import declarative_screen_load.screen.CustomerView
import io.jmix.core.DataManager
import io.jmix.flowui.ScreenNavigators
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Customer
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DeclarativeScreenLoadTest extends FlowuiTestSpecification {

    @Autowired
    ScreenNavigators screenNavigators

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate;

    void setup() {
        registerScreenBasePackages("declarative_screen_load.screen")

        def customer = dataManager.create(Customer)
        dataManager.save(customer)
    }

    void cleanup() {
        jdbcTemplate.execute("delete from TEST_CUSTOMER")
    }

    def "navigateToTestProjectView"() {
        when: "Open the CustomerView"
        screenNavigators.screen(CustomerView.class)
                .navigate()

        List<HasElement> activeRouterTargetsChain = UI.getCurrent().getInternals().getActiveRouterTargetsChain()

        then: "Data container should be injected"
        activeRouterTargetsChain.get(0) instanceof CustomerView

        CustomerView projectView = (CustomerView) activeRouterTargetsChain.get(0)

        projectView.customersDc != null
    }

    def "clickButtonInTestProjectView"() {
        when:
        screenNavigators.screen(CustomerView.class)
                .navigate()

        List<HasElement> activeRouterTargetsChain = UI.getCurrent().getInternals().getActiveRouterTargetsChain()

        then:
        activeRouterTargetsChain.get(0) instanceof CustomerView

        CustomerView projectView = (CustomerView) activeRouterTargetsChain.get(0)

        projectView.doBtn.click()
    }
}
