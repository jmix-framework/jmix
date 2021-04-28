package io.jmix.graphql.servlet;

import graphql.kickstart.execution.GraphQLQueryInvoker;
import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.servlet.GraphQLHttpServlet;
import io.jmix.graphql.limitation.OperationRateLimitInstrumentation;
import io.jmix.graphql.limitation.OperationRateLimitService;
import io.jmix.graphql.schema.SchemaBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import java.util.Collections;

@WebServlet(name = "JmixGraphQLServlet", loadOnStartup = 1, urlPatterns = "/graphql")
public class JmixGraphQLServlet extends GraphQLHttpServlet {

    @Autowired
    SchemaBuilder schemaBuilder;
    @Autowired
    OperationRateLimitService operationRateLimitService;

    @Override
    protected GraphQLConfiguration getConfiguration() {
        GraphQLQueryInvoker invoker = GraphQLQueryInvoker.newBuilder()
                .with(Collections.singletonList(new OperationRateLimitInstrumentation(operationRateLimitService)))
                .build();

        return GraphQLConfiguration
                .with(schemaBuilder.createSchema())
                .with(invoker)
                .build();
    }
}
