# Jmix GraphQL

This repository contains GraphQL integration project of the [Jmix](https://jmix.io) framework.

For more information see:

* Jmix Core project source [repository](https://github.com/Haulmont/jmix-core).
* Jmix [documentation](https://docs.jmix.io).


## Usage

Add to your project's `build.gradle` dependencies:

```groovy
implementation 'io.jmix.graphql:jmix-graphql-starter'
```

## GraphiQL tool
To make GraphiQL work add 

```runtimeOnly 'com.graphql-java-kickstart:graphiql-spring-boot-starter:8.1.1'```

to `build.gradle` `dependencies` section in your app.

GraphiQL will be available at [http://localhost:8080/graphiql]()

## Schema download
Schema could be downloaded using `graphqurl`
```
npm install -g graphqurl
gq http://localhost:8080/graphql --introspect > schema.graphql
```