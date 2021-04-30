package io.jmix.graphql.servlet;

import graphql.execution.instrumentation.Instrumentation;
import graphql.kickstart.execution.GraphQLQueryInvoker;
import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.servlet.GraphQLHttpServlet;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.graphql.limitation.OperationRateLimitInstrumentation;
import io.jmix.graphql.limitation.OperationRateLimitService;
import io.jmix.graphql.schema.SchemaBuilder;
import io.jmix.graphql.security.SpecificPermissionInstrumentation;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected GraphQLConfiguration getConfiguration() {
        List<Instrumentation> instrumentations = new ArrayList<>();
        instrumentations.add(new OperationRateLimitInstrumentation(operationRateLimitService));
        instrumentations.add(new SpecificPermissionInstrumentation(accessManager, messages));

        GraphQLQueryInvoker invoker = GraphQLQueryInvoker.newBuilder()
                .with(instrumentations)
                .build();

        return GraphQLConfiguration
                .with(schemaBuilder.createSchema())
                .with(invoker)
                .build();
    }
}
