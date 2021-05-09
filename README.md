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

### Specific permission
The user must have specific permission to work with GraphQL API. 
To make it available, have to set access for `graphql.enabled` resource in the user's resource role, 
or assign `graphql-minimal` role to user.  

## Jmix Security Development Mode
OAuth header could be partially skipped in development mode. To switch on dev mode configure properties in app:
```
jmix.security.oauth2.devMode=true
jmix.security.oauth2.devUsername=admin
graphiql.props.variables.headerEditorEnabled=true
```
In this mode no generated token required for authorization. Request automatically got permissions 
of user which username set in `devUsername`. Authorization header should be set to `bearer`:
```
{ "Authorization": "bearer" }
```
You can set authorization header for GraphIQL using application property:
```
graphiql.headers.Authorization=Bearer
```


## Rate limit
Set `jmix.graphql.operationRateLimitPerMinute` property with integer value in `application.property` to control how many times 
is possible send queries per minute from one IP address

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

Then we try to save it with incorrect value:

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

## Entity Filters

### Filter conditions

Every field has a different set of available operations which depends on the type.
The table below shows the dependency of types.

| Field type    | Available operations |
| :---          |    :---- |
| uuid          | _eq, _neq, _in, _notIn, _isNull |
| numbers       | _eq, _neq, _gt, _gte, _lt, _lte, _in, _notIn, _isNull |
| string/char   | _eq, _neq, _in, _notIn, _contains, _notContains, _startsWith, _endsWith, _isNull |
| date          | _eq, _neq, _gt, _gte, _lt, _lte, _in, _notIn, _isNull |
| boolean       | _eq, _neq, _isNull |


**Please pay attention** that filter conditions `_eq`, `_in`, `_notIn` have strict case matching and `_contains`, `_endsWith`, `_startsWith` have ignored case matching for strings.

### Condition types

GraphQL backend used strictly typed entity and datatype conditions.

Filter for each entity has it own type in schema, each datatype condition also represent as separate type.

Condition operators start with `_` (prefix used for all system names) and have names in camel case notation.



Filter condition for `Car` class:

```
input inp_scr_CarFilterCondition {
  
  # nested entity conditions
  garage: [inp_scr_GarageFilterCondition]
 
  # datatype conditions
  wheelOnRight: [inp_booleanFilterCondition]
  manufactureDate: [inp_dateFilterCondition]
  model: [inp_stringFilterCondition]
 
  # conditon compostion operators
  AND: [inp_scr_CarFilterCondition]
  OR: [inp_scr_CarFilterCondition]
  
  #other property condtions (class and datatype)
  # ....
}
```



Condition for String datatype which is used for `Car.model` :

```graphql
# expression to compare columns of type String. All fields are combined with logical 'AND'
input inp_stringFilterCondition {
  
  # contains substring
  _contains: String
  # not contains substring
  _notContains: String
  # starts with substring
  _startsWith: String
  # ends with substring
  _endsWith: String

  # other operators
  # ....
}
```



### Filter example in \*List query

To apply filter condition we need to pass `filter` parameter in `*List` query:

```
{
  carList(filter: {model: {_contains: "M"}}) {
    model
  }
}
```

this return all cars which model name contains "M" letter



Also, there could be more complicated conditions with nested entities:

```
{
  carList(filter: {
    garage: {name: {_contains: "P"}}
  }) {
    manufacturer
  }
}
```

return all cars which garage name contains "P"



### Condition composition

Each query could contain a set of condition. If conditions are required to composed by **AND** they could be passed to filter as array:

```
{
  carList(filter: [{model: {_contains: "M"}}, {model: {_contains: "0"}}]) {
    model
  }
}
```

return all cars which model name contains both "M" and "0"



This also could be rewritten by using special **AND** and **OR** operators which exist in each class condition:

```
{
  carList(filter: {AND: [{model: {_contains: "M"}}, {model: {_contains: "0"}}]}) {
    model
  }
}
```



The same way we can create condition that search for models with "M" **or** "0" symbols:

```
{
  carList(filter: {OR: [{model: {_contains: "M"}}, {model: {_contains: "0"}}]}) {
    model
  }
}
```



Similar it works for nested entities:

```
{
  carList(filter: {garage: {OR: [{name: {_contains: "1"}}, {name: {_contains: "A"}}]}}) {
    model
  }
}
```

search cars which garage name contains "1" or "A"



Finally, we can combine all this approaches in one query:

```
{
  carList(filter: [
    {model: {_contains: "M"}}, 
    {garage: {OR: [{name: {_contains: "1"}}, {name: {_contains: "A"}}]}}]) 
  {
    model
  }
}
```


