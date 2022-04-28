# Jmix GraphQL

The GraphQL add-on provides GraphQL API for Jmix applications.

**CAUTION: This add-on is now in the incubating state and its API and behavior may be modified in the future minor and patch releases.**

## Usage

Add the following dependency to your project's `build.gradle`:

```groovy
implementation 'io.jmix.graphql:jmix-graphql-starter'
```

## API

### Schema for Persistent Jmix Entities
For each persistent Jmix entity the add-on automatically generates:
* input and output GraphQL type
* list, count and byId query
* mutations that allow to create, update and delete entity

By default, schema also contains all needed primitive types (data types), enums and filter types 
which are used in list queries.

For example for [Car](graphql/src/test/java/test_support/entity/Car.java) entity from test project 
will be generated schema fragment
```graphql
input inp_scr_Car {
  carType: CarType
  createdBy: String
  createdDate: Date
  ecoRank: EcoRank
  garage: inp_scr_Garage
  id: UUID
  lastModifiedBy: String
  lastModifiedDate: Date
  manufactureDate: DateTime
  manufacturer: String
  maxPassengers: Int
  mileage: Float
  model: String
  price: BigDecimal
  purchaseDate: Date
  regNumber: String
  technicalCertificate: inp_scr_TechnicalCertificate
  version: Int
  wheelOnRight: Boolean
}

type scr_Car {
  _instanceName: String
  carType: CarType
  createdBy: String
  createdDate: Date
  ecoRank: EcoRank
  garage: scr_Garage
  id: UUID
  lastModifiedBy: String
  lastModifiedDate: Date
  manufactureDate: DateTime
  manufacturer: String
  maxPassengers: Int
  mileage: Float
  model: String
  price: BigDecimal
  purchaseDate: Date
  regNumber: String
  technicalCertificate: scr_TechnicalCertificate
  version: Int
  wheelOnRight: Boolean
}

type Query {
  scr_CarById(id: String!): scr_Car

   scr_CarCount(
      "expressions to compare scr_Car objects, all items are combined with logical 'AND'"
      filter: [inp_scr_CarFilterCondition]
   ): Long
  
  scr_CarList(
    "expressions to compare scr_Car objects, all items are combined with logical 'AND'"
    filter: [inp_scr_CarFilterCondition],
    "limit the number of items returned"
    limit: Int,
    "skip the first n items"
    offset: Int,
    "sort the items by one or more fields"
    orderBy: inp_scr_CarOrderBy
  ): [scr_Car]
  
}

type Mutation {
  upsert_scr_Car(car: inp_scr_Car!): scr_Car
   delete_scr_Car(id: String!): Void
}
```
More schema details and additional types such as filter types and scalars could be found in 
[test project schema](graphql/src/test/resources/graphql/io/jmix/graphql/schema.graphql).

### Extend Schema with Custom Query\Mutation
Schema could be supplemented with types and queries defined manually.
For schema extension used [GraphQL SPQR](https://github.com/leangen/graphql-spqr) library. <br>
As an example of extension could be seen [UserInfoGraphQLService](graphql/src/main/java/io/jmix/graphql/service/UserInfoGraphQLService.java),
which provides custom resolver for additional user info.

Creating `UserInfoGraphQLService` annotated with `@GraphQLApi` and contains method marked with `@GraphQLQuery`
```java

@GraphQLApi
@Service("gql_UserInfoGraphQLService")
public class UserInfoGraphQLService {

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @GraphQLQuery
    public UserInfo getUserInfo() {
        UserDetails user = currentAuthentication.getUser();
        UserInfo userInfo = new UserInfo(user);
        userInfo.setLocale(currentAuthentication.getLocale().toString());
        return userInfo;
    }
}
```
leads to schema additional type and query generation 
```graphql

type UserInfo {
  locale: String
  username: String
}

type Query {
   userInfo: UserInfo
}
```

#### Choosing which methods get exposed through the API
To deduce which methods of each operation source class should be exposed as GraphQL queries/mutations/subscriptions, 
SPQR uses the concept of a `ResolverBuilder` (since each exposed method acts as a resolver function for a 
GraphQL operation). To cover the basic approaches `AutoConfiguration` registers a bean for each 
of the three built-in `ResolverBuilder` implementations:
- `AnnotatedResolverBuilder` - exposes only the methods annotated by `@GraphQLQuery`, `@GraphQLMutation` or `@GraphQLSubscription`
- `PublicResolverBuilder` - exposes all `public` methods from the operations source class (methods returning `void` are considered mutations)
- `BeanResolverBuilder` - exposes all getters as queries and setters as mutations (getters returning `Publisher<T>` are considered subscriptions)

**It is also possible to implement custom resolver builders by implementing the `ResolverBuilder` interface.**

Resolver builders can be declared both globally and on the operation source level.
If not sticking to the defaults, it is generally safer to explicitly customize on
the operation source level, unless the rules are absolutely uniform across all 
operation sources. Customizing on both levels simultaneously will work but could 
prove tricky to control as your API grows.

At the moment SPQR's (v0.11.2) default resolver builder is `AnnotatedResolverBuilder`. 
This starter follows that convention and will continue to do so if at some point SPQR's default changes.

####Customizing resolver builders globally
To change the default resolver builders globally, implement and register a bean of type 
`ExtensionProvider<ResolverBuilder>`. A simplified example of this could be:
```java
   @Bean
   public ExtensionProvider<GeneratorConfiguration, ResolverBuilder> resolverBuilderExtensionProvider() {
      return (config, current) -> {
         List<ResolverBuilder> resolverBuilders = new ArrayList<>();
   
         //add a custom subtype of PublicResolverBuilder that only exposes a method if it's called "greeting"
         resolverBuilders.add(new PublicResolverBuilder() {
            @Override
            protected boolean isQuery(Method method) {
               return super.isQuery(method) && method.getName().equals("greeting");
            }
         });
         //add the default builder
         resolverBuilders.add(new AnnotatedResolverBuilder());
   
         return resolverBuilders;
      };
   }
```

This would add two resolver builders that apply to *all* operation sources.
The First one exposes all public methods named *greeting*. The second is 
the inbuilt `AnnotatedResolverBuilder` (that exposes only the explicitly annotated 
methods). A quicker way to achieve the same would be:

```java
    @Bean
    public ExtensionProvider<GeneratorConfiguration, ResolverBuilder> resolverBuilderExtensionProvider() {
        //prepend the custom builder to the provided list of defaults
        return (config, current) -> current.prepend(new PublicResolverBuilder() {
                @Override
                protected boolean isQuery(Method method) {
                    return super.isQuery(method) && method.getName().equals("greeting");
                }
            });
    };
```

####Customizing the resolver builders for a specific operation source
To attach a resolver builder to a specific source (bean), use 
the `@WithResolverBuilder` annotation on it. This annotation also works both 
on the beans registered by `@Component/@Service/@Repository` or `@Bean` annotations.

As an example, we can expose the `greeting` query by using:
```java
    @Component
    @GraphQLApi
    @WithResolverBuilder(BeanResolverBuilder.class) //exposes all getters
    private class MyOperationSource {
        public String getGreeting(){
            return "Hello world !";
        }
    }
```

or:
```java
    @Bean
    @GraphQLApi
    //No explicit resolver builders declared, so AnnotatedResolverBuilder is used
    public MyOperationSource() {
        @GraphQLQuery(name = "greeting")
        public String getGreeting() {
            return "Hello world !";
        }
    }
```

It is also entirely possible to use more than one resolver builder on the same
operation source e.g.

```java
    @Component
    @GraphQLApi
    @WithResolverBuilder(BeanResolverBuilder.class)
    @WithResolverBuilder(AnnotatedResolverBuilder.class)
    private class MyOperationSource {
        //Exposed by BeanResolverBuilder because it's a getter
        public String getGreeting(){
            return "Hello world !";
        }

        //Exposed by AnnotatedResolverBuilder because it's annotated
        @GraphQLQuery
        public String personalGreeting(String name){
            return "Hello " + name + " !"; 
        }
    }
```
This way, both queries are exposed but in different ways. 
The same would work on a bean registered using the `@Bean` annotation.

#### Input Types Generation in Custom Resolver 
By default, SPQR creates input types with `Input` suffix:
```graphql
input CarInput
```
In `jmix-graphql` schema will be generated this type with `inp_` prefix, as in generic types:
```graphql
input inp_Car
```

#### Work with SPQR
More about how to work with SPQR could be found [in this article](https://www.howtographql.com/graphql-java/11-alternative-approaches/).

### Create custom query/mutation data fetchers for entities
In default behavior query/mutation data fetchers are working with default database of project 
You can change default logic of query/mutation for entities using follow API:
1) Set annotation GraphQLCustomQueryDataFetcher/GraphQLCustomMutationDataFetcher on yours Component
2) Implement one or several interfaces for query data fetcher:
	* GraphQLEntityCountDataFetcher
	* GraphQLEntityListDataFetcher
	* GraphQLEntityDataFetcher
3) Implement one or several interfaces for mutation data fetcher:
	* GraphQLRemoveEntityDataFetcher
	* GraphQLUpsertEntityDataFetcher

Example for query data fetcher:
```java
@Component("Test_CarLoader")
public class CarEntityDataFetcher implements GraphQLEntityCountDataFetcher<Car>,
		GraphQLEntityListDataFetcher<Car>, GraphQLEntityDataFetcher<Car> {

	@Autowired
	Metadata metadata;

	@Override
	public Long loadCount(GraphQLEntityCountDataFetcherContext<Car> context) {
		return 999L;
	}

	@Override
	public List<Car> loadEntityList(GraphQLEntityListDataFetcherContext<Car> context) {
		Car car1 = metadata.create(Car.class);
		car1.setManufacturer("BMW");
		car1.setModel("M3");
		car1.setPrice(BigDecimal.valueOf(10));
		Car car2 = metadata.create(Car.class);
		car2.setManufacturer("Lada");
		car2.setModel("Vesta");
		car2.setPrice(BigDecimal.valueOf(20));
		return new ArrayList<>(Arrays.asList(car1, car2));
	}

	@Override
	public Car loadEntity(GraphQLEntityDataFetcherContext<Car> context) {
		Car car = metadata.create(Car.class);
		car.setManufacturer("Lada");
		car.setModel("Vesta");
		car.setPrice(BigDecimal.valueOf(10));
		return car;
	}
}
```
Example for query data fetcher:
```java
@Component("Test_CarModifier")
public class CarModifier implements GraphQLRemoveEntityDataFetcher<Car>, GraphQLUpsertEntityDataFetcher<Car> {

    @Autowired
    Metadata metadata;

    private static Logger log = LoggerFactory.getLogger(CarModifier.class);

    @Override
    public Car importEntities(GraphQLUpsertEntityDataFetcherContext<Car> context) {
        Car car = context.getEntities().get(0);
        car.setPrice(BigDecimal.valueOf(10));
        return car;
    }

    @Override
    public void deleteEntity(GraphQLRemoveEntityDataFetcherContext<Car> graphQLRemoveEntityDataFetcherContext) {
        log.warn("Delete entity with id " + graphQLRemoveEntityDataFetcherContext.getId());
    }
}
```

### Permission Query

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
    specifics {
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
      "specifics": [
        {
          "target": "graphql.fileDownload.enabled",
          "value": 0
        },
        {
          "target": "graphql.fileUpload.enabled",
          "value": 0
        }
      ]
    }
  }
}
```

### Messages Query 
The query for getting messages for all entities:

```
{
    entityMessages{
        key
        value
    }
}
```

The part of the result will look like this:
```
{
  "data": {
    "entityMessages": [
        ...
        {
            "key": "scr$Car",
            "value": "Car"
        },
        {
            "key": "scr$Car.purchaseDate",
            "value": "Purchase Date"
        },
        {
            "key": "scr$Car.lastModifiedDate",
            "value": "Last modified date"
        },
        {
            "key": "scr$Car.maxPassengers",
            "value": "Max Passengers"
        },
        {
            "key": "scr$Car.lastModifiedBy",
            "value": "Last modified by"
        },
        {
            "key": "scr$Car.garage",
            "value": "Garage"
        },
        ...
    ]
  }
}
```

Messages for enums can get by the query:
```
{
    enumMessages{
        key
        value
    }
}
```
And the part of the result:
```
{
  "data": {
    "enumMessages": [
        ...
        {
            "key": "com.company.scr.entity.CarType",
            "value": "CarType"
        },
        {
            "key": "com.company.scr.entity.CarType.SEDAN",
            "value": "Sedan"
        },
        {
            "key": "com.company.scr.entity.CarType.HATCHBACK",
            "value": "Hatchback"
        },
        ...
    ]
  }
}
```

Also available getting messages for only one entity with `String` parameter `className` :
```
{
    entityMessages(className: "scr$Car") {
        key
        value
    }
}
```
Or enum:
```
{
    enumMessages(className: "com.company.scr.entity.CarType"){
        key
        value
    }
}
```
Localized messages can be loaded by `String` parameter `locale`. 
If the parameter doesn't define, so the locale will be received from the current user:
```
{
    entityMessages(className: "scr$Car" locale:"ru") {
        key
        value
    }
    enumMessages(className: "com.company.scr.entity.CarType" locale: "en"){
        key
        value
    }
}
```

### User Info Query
Information about current user can be reached with `userInfo` query
```
{
  userInfo {
    locale
    username
  }
}
``` 
The response will look like
```
{
  "data": {
    "userInfo": {
      "locale": "en",
      "username": "admin"
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

### Filter Conditions

Every field has a different set of available operations which depends on the type.
The table below shows the dependency of types.

| Field type    | Available operations |
| :---          |    :---- |
| uuid          | _eq, _neq, _in, _notIn, _isNull |
| numbers       | _eq, _neq, _gt, _gte, _lt, _lte, _in, _notIn, _isNull |
| string/char   | _eq, _neq, _in, _notIn, _contains, _notContains, _startsWith, _endsWith, _isNull |
| date, dateTime| _eq, _neq, _gt, _gte, _lt, _lte, _in, _notIn, _isNull |
| time          | _eq, _neq, _gt, _gte, _lt, _lte, _isNull |
| boolean       | _eq, _neq, _isNull |


**Please pay attention** that filter conditions `_eq`, `_in`, `_notIn` have strict case matching and `_contains`, `_endsWith`, `_startsWith` have ignored case matching for strings.

### Condition Types

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



### Filter Example in \*List Query

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



### Condition Composition

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

## Supported Date Datatypes
The table below describes which scalar used for every date type and shows its string format:

| Java type                                     | GraphQL type            | Date format           | Example|
| :---                                          | :---                  | :---                  | :---|
| ```@Temporal(TemporalType.DATE) Date```       | Date           | [ISO_LOCAL_DATE](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html#ISO_LOCAL_DATE) | '2011-12-03'|
| ```@Temporal(TemporalType.TIME) Date```       | Time           | [ISO_LOCAL_TIME](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html#ISO_LOCAL_TIME) | '10:15', '10:15:30'|
| ```@Temporal(TemporalType.TIMESTAMP) Date```  | DateTime       | [ISO_LOCAL_DATE_TIME](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html#ISO_LOCAL_DATE_TIME) | '2011-12-03T10:15:30'|
| ```LocalDate```                               | LocalDate      | [ISO_LOCAL_DATE](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html#ISO_LOCAL_DATE) | '2011-12-03'|
| ```LocalTime```                               | LocalTime      | [ISO_LOCAL_TIME](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html#ISO_LOCAL_TIME) | '10:15', '10:15:30'|
| ```LocalDateTime```                           | LocalDateTime  | [ISO_LOCAL_DATE_TIME](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html#ISO_LOCAL_DATE_TIME) | '2011-12-03T10:15:30'|
| ```OffsetTime```                              | OffsetTime     | [ISO_OFFSET_TIME](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html#ISO_OFFSET_TIME) | '10:15+01:00' or '10:15:30+01:00'|
| ```OffsetDateTime```                          | OffsetDateTime | [ISO_OFFSET_DATE_TIME](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html#ISO_OFFSET_DATE_TIME) | '2011-12-03T10:15:30+01:00'|

## Query Limits

### Rate Limit
Set `jmix.graphql.operationRateLimitPerMinute` property with integer value in `application.properties`
to control how many times it's possible to send queries per minute from one IP address.

### Max Query Depth
Query depth could be configured via `jmix.graphql.maxQueryDepth` property.
By default, query depth has no limit (`maxQueryDepth` is set to `0`).

## Work with OAuth Protected Queries and Mutations

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

### OAuth Protected Query Flow Example

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

### Specific Permission
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
You can set authorization header for GraphiQL using application property:
```
graphiql.headers.Authorization=Bearer
```

## GraphiQL Tool
To make GraphiQL work add

```runtimeOnly 'com.graphql-java-kickstart:graphiql-spring-boot-starter:8.1.1'```

to `build.gradle` `dependencies` section in your app.

GraphiQL will be available at [http://localhost:8080/graphiql]()

### Configure GraphiQL for Using with Auth Header
To add **request headers** tab configure application property:

```graphiql.props.variables.headerEditorEnabled=true```

## Schema Download
Schema could be downloaded using [JS GraphQL IDEA Plugin](https://plugins.jetbrains.com/plugin/8097-js-graphql).
