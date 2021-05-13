package io.jmix.graphql.servlet;

import graphql.kickstart.execution.GraphQLQueryInvoker;
import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.servlet.GraphQLHttpServlet;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.graphql.limitation.JmixMaxQueryDepthInstrumentation;
import io.jmix.graphql.limitation.LimitationProperties;
import io.jmix.graphql.limitation.OperationRateLimitInstrumentation;
import io.jmix.graphql.limitation.OperationRateLimitService;
import io.jmix.graphql.schema.SchemaBuilder;
import io.jmix.graphql.security.SpecificPermissionInstrumentation;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import java.util.Arrays;

@WebServlet(name = "JmixGraphQLServlet", loadOnStartup = 1, urlPatterns = "/graphql")
public class JmixGraphQLServlet extends GraphQLHttpServlet {

    @Autowired
    SchemaBuilder schemaBuilder;
    @Autowired
    OperationRateLimitService operationRateLimitService;
    @Autowired
    AccessManager accessManager;
    @Autowired
    Messages messages;
    @Autowired
    private LimitationProperties limitationProperties;

    @Override
    protected GraphQLConfiguration getConfiguration() {

        GraphQLQueryInvoker invoker = GraphQLQueryInvoker.newBuilder()
                // add instrumentation
                .with(Arrays.asList(
                        new OperationRateLimitInstrumentation(operationRateLimitService),
                        new SpecificPermissionInstrumentation(accessManager, messages),
                        new JmixMaxQueryDepthInstrumentation(limitationProperties.getMaxQueryDepth())
                ))
                .build();

        return GraphQLConfiguration
                .with(schemaBuilder.createSchema())
                .with(invoker)
                .build();
    }
}
