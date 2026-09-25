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

import io.jmix.aitools.dataload.execution.JpqlExecutionParameter;
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionService;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import test_support.entity.sales.Customer;
import test_support.entity.sales.Order;
import test_support.entity.sales.OrderApproval;
import test_support.entity.sales.OrderLine;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Data fixture and authentication for the data-load access tests: three customers ({@code VisibleCo},
 * {@code HiddenCo}, {@code OtherCo}), one order with one line per customer, and a login as a user holding
 * the given resource and row-level roles.
 */
@Component
public class SecuredDataLoadTestSupport {

    @Autowired
    protected JpqlExecutionService executionService;
    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected SystemAuthenticator systemAuthenticator;
    @Autowired
    protected RoleGrantedAuthorityUtils authorityUtils;
    @Autowired
    protected DataSource dataSource;

    /**
     * Creates the fixture as the system user.
     */
    public void createData() {
        systemAuthenticator.runWithSystem(() -> {
            long id = 1;
            for (String name : List.of("VisibleCo", "HiddenCo", "OtherCo")) {
                Customer customer = metadata.create(Customer.class);
                customer.setId(id++);
                customer.setName(name);
                customer.setSystemNote("note-" + name);
                customer.setSecretToken("token-" + name);
                customer.setPiiPhone("+1-" + name);
                OrderApproval approval = metadata.create(OrderApproval.class);
                approval.setApprovedBy("boss-" + name);
                Order order = metadata.create(Order.class);
                order.setNumber("ORD-" + name);
                order.setCustomer(customer);
                order.setApproval(approval);
                OrderLine line = metadata.create(OrderLine.class);
                line.setQuantity(2);
                line.setOrder(order);
                dataManager.saveWithoutReload(customer, approval, order, line);
            }
        });
    }

    /**
     * Removes the fixture and the authentication.
     */
    public void deleteData() {
        SecurityContextHelper.setAuthentication(null);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("delete from AITLS_ORDER_LINE");
        jdbc.update("delete from AITLS_ORDER_SHIPMENT");
        jdbc.update("delete from AITLS_ORDER");
        jdbc.update("delete from AITLS_ORDER_APPROVAL");
        jdbc.update("delete from AITLS_CUSTOMER");
    }

    /**
     * Authenticates a user holding the given roles. Role codes ending with {@code -row-level} are granted as
     * row-level roles, the rest as resource roles.
     *
     * @param roleCodes role codes to grant
     */
    public void loginAs(String... roleCodes) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String code : roleCodes) {
            authorities.add(isRowLevelRole(code)
                    ? authorityUtils.createRowLevelRoleGrantedAuthority(code)
                    : authorityUtils.createResourceRoleGrantedAuthority(code));
        }
        User user = (User) User.builder().username("user").password("{noop}x").authorities(authorities).build();
        SecurityContextHelper.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, authorities));
    }

    /**
     * Runs the query through the real execution service, as a tool call would.
     *
     * @param jpql             query to execute
     * @param resultProperties result aliases in select-clause order
     * @return the execution result
     */
    public JpqlExecutionResult execute(String jpql, String... resultProperties) {
        return execute(jpql, List.of(), resultProperties);
    }

    /**
     * Runs the query with parameters through the real execution service, as a tool call would.
     *
     * @param jpql             query to execute
     * @param parameters       parameters passed with the query
     * @param resultProperties result aliases in select-clause order
     * @return the execution result
     */
    public JpqlExecutionResult execute(String jpql, List<JpqlExecutionParameter> parameters,
                                       String... resultProperties) {
        return executionService.execute(
                new JpqlExecutionRequest("test", jpql, parameters, List.of(resultProperties), null, null));
    }

    protected boolean isRowLevelRole(String code) {
        return code.endsWith("-row-level");
    }
}
