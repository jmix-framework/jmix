package io.jmix.graphql.servlet;

import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.servlet.GraphQLHttpServlet;
import io.jmix.graphql.schema.SchemaBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "JmixGraphQLServlet", loadOnStartup = 1, urlPatterns = "/graphql")
public class JmixGraphQLServlet extends GraphQLHttpServlet {

    @Autowired
    SchemaBuilder schemaBuilder;

    @Override
    protected GraphQLConfiguration getConfiguration() {
        return GraphQLConfiguration.with(schemaBuilder.createSchema()).build();
    }

}
