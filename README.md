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

### Configure GraphiQL for using with auth header
To add **request headers** tab configure application property:

```graphiql.props.variables.headerEditorEnabled=true``` 

## Work with OAuth protected queries and mutations

To fetch and modify entities protected by jmix security, 
GraphQL request should contain authorisation header. 

Example of auth header:
```
{ "Authorization": "Bearer gLGo5vRTMBWQtleaBbMl2oTkAZM=" }
```
Where `gLGo5vRTMBWQtleaBbMl2oTkAZM=` - auth token obtained from jmix OAuth service. 

This header could be added for example in GraphIQL **request headers** tab (described in GraphIQL section) 
for proper queries and mutation execution. 

Token obtain instruction described in [https://doc.cuba-platform.com/restapi-7.2/#rest_api_v2_ex_get_token]()
the only one difference that at this moment OAuth token url is changed from [/app/rest/v2/oauth/token]()
to [/oauth/token]() 

### OAuth protected query flow example

Example based on [scr-jmix](https://github.com/jmix-projects/scr-jmix) backend app. 
[HTTPie](https://httpie.io/) tool used to send requests. 


1. Send auth request for user admin\admin, where `client` and `secret` - properties from `scr-jmix` 
`client.id` and `client.secret`.
```
http -a client:secret --form POST localhost:8080/oauth/token \
grant_type=password username=admin password=admin
```

Result will be json with `access_token`, which we need to send next request:
```
{
    "OAuth2.SESSION_ID": "1697AFAFD8F76DE88C6C7AA092E3AAFD",
    "access_token": "jMfH2tGBhioE2ugBa4jojeO/Wi8=",
    "expires_in": 43199,
    "refresh_token": "GdMwjtjSulf7NMuhlwqt/TtK3B8=",
    "scope": "api",
    "token_type": "bearer"
}
```

2. Get list of cars
```
http POST http://localhost:8080/graphql query="{carList {_instanceName}}" "Authorization: Bearer jMfH2tGBhioE2ugBa4jojeO/Wi8="
``` 

## Jmix Security Development Mode
OAuth header could be partially skipped in development mode. To switch on dev mode configure properties in app:
```
jmix.security.oauth2.devMode=true
jmix.security.oauth2.devUsername=admin
graphiql.props.variables.headerEditorEnabled=true
```
In this mode no generated token required for authorization. Request automatically got permissions 
of user which username set in `devUsername`. Due to issues in jmix OAuth service, header still need to be sent 
but with empty token.
```
{ "Authorization": "bearer" }
```


## Schema download
Schema could be downloaded using `graphqurl`
```
npm install -g graphqurl
gq http://localhost:8080/graphql --introspect > schema.graphql
```

## Api

### Permission query

```
{
  permissions {
    entities {
      target
      value
    }
    entityAttributes {
      target
      value
    }
  }
}
```

will return result as follows:
```
{
  "data": {
    "permissions": {
      "entities": [
        {
          "target": "scr$Car:create",
          "value": 1
        }

        # more items
        # ...
      ],
      "entityAttributes": [
        {
          "target": "scr$Car:purchaseDate",
          "value": 2
        }

        # more items
        # ...
      ]
    }
  }
}
```

### Bean Validation

Bean validation errors available in GraphQL response property `errors.extensions.constraintViolations`

Error structure contains validated property path, message and property value that is invalid. 
For example, if `regNumber` has validation constraint:

```
    @Size(min = 0, max = 5)
    @Pattern(regexp = "[a-zA-Z]{2}\\d{3}")
    @Column(name = "REG_NUMBER", length = 5)
    protected String regNumber;
```

And we try to save it with incorrect value:

```
mutation {
  upsert_scr_Car (car: {
	manufacturer: "TESS"
    carType: SEDAN
    regNumber: "aa12"
  }) {
    _instanceName
  }
}
```

Error response will look like:

```
{
  "errors": [
    {
      "message": "Exception while fetching data (/upsert_scr_Car) : Entity validation failed",
      "locations": [
        {
          "line": 2,
          "column": 3
        }
      ],
      "path": [
        "upsert_scr_Car"
      ],
      "extensions": {
        "constraintViolations": [
          {
            "path": "regNumber",
            "message": "must match \"[a-zA-Z]{2}\\d{3}\"",
            "messageTemplate": "{javax.validation.constraints.Pattern.message}",
            "invalidValue": "aa12"
          }
        ],
        "classification": "DataFetchingException"
      }
    }
  ],
  "data": {
    "upsert_scr_Car": null
  }
}
```


