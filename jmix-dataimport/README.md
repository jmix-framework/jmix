# Jmix Data Import

Data import add-on allows importing entities from XLSX, CSV, JSON, and XML formats using API.

**CAUTION: This add-on is now in the incubating state and its API and behavior may be modified in the future minor and patch releases.**

## Installation

To include the `jmix-dataimport` add-on to the application, add the following dependency to `build.gradle`:

```
implementation 'io.jmix.dataimport:jmix-dataimport-starter'
```

## DataImporter

`DataImporter` interface - the main API to import data.

Available methods:

### Import data from byte array

```java
ImportResult importData(ImportConfiguration configuration, byte[] content);
```

Parameters:

* Input data (XLSX, CSV, JSON, or XML) as a byte array
* ImportConfiguration applicable for the format of input data

### Import data from input stream

```java
ImportResult importData(ImportConfiguration configuration, InputStream inputStream);
```

Parameters:

* Input data (XLSX, CSV, JSON, or XML) as an input stream
* ImportConfiguration applicable for the format of input data

Both methods return an instance of ImportResult.

**ImportResult**

An object that contains the following information about the import execution result:

1. `success` - a boolean field that specifies whether entities import is executed successfully.
2. `importedEntityIds` - list of entity ids that are successfully imported.
3. `failedEntities` - list of `EntityImportError` objects created for not imported entities.
4. `errorMessage` - an error message if an exception is thrown during parsing the input data/entities extraction/entities import.

**EntityImportError**

An object that contains the following details if an entity import fails:

1. `ImportedDataItem` - a source of raw values for entity properties.
2. `errorMessage`  - an error description.
3. `entity`  - an extracted entity ready to import.
4. `errorType`  - one of the following `EntityImportErrorType` :
    * UNIQUE_VIOLATION - occurs if a duplicate entity is found and the SKIP policy is used to process the duplicates;
    * VALIDATION - occurs if the pre-import predicate returns false for an entity or during entity import `EntityValidationException`  is thrown.
    * PERSISTENCE - occurs if a `PersistenceException`  is thrown during entity import.
    * PRE_IMPORT_PREDICATE - occurs if an exception is thrown during pre-import predicate check.
    * DATA_BINDING - occurs if an exception is thrown during extracting  the entity from input data.
    * NOT_IMPORTED_BATCH - occurs if an exception is thrown during importing a batch of entities.

## Custom data extractor

There is an ability to add a data extractor to parse input data in the format not supported out-of-box.
To add a custom data extractor: create a Spring component implementing the `ImportedDataExtractor` interface and implement methods to parse input data and get the supported format of input data.

## ImportConfiguration

ImportConfiguration allows configuring the import process by specifying the following options:

1. Entity class: class of the entity that will be imported.
2. Input data format: xlsx, csv, json or xml
3. Property mappings: one property mapping specifies which field(s) from input data maps to which entity property.
4. Format options: date format, formats of boolean values.
5. Unique entity configurations
6. Transaction strategy
7. Import batch size: actual if the "Transaction per batch" strategy is used
8. Pre-import predicate
9. Entity initializer

### **Property mappings**

Four types of property mappings are implemented:

1. SimplePropertyMapping
2. ReferencePropertyMapping
3. ReferenceMultiFieldPropertyMapping
4. CustomPropertyMapping

**SimplePropertyMapping** is a mapping for a simple property of an entity.

| **Field** | **Description** | **Required** |
| --- | --- | --- |
| dataFieldName | Field name/tag name/column from the input data <br/>that has a raw value of entity property | Yes |
| entityPropertyName | Name of the entity property which value will be set | Yes |

*Supported types of simple properties*: String, Integer, Double, Long, BigDecimal, Boolean, Date, LocalDate, Enum.

A raw value is taken from a data field, is parsed, and set in the entity property. The date format from the ImportConfiguration is used to parse Date and LocalDate field values. The boolean true/value formats from the ImportConfiguration are used to parse boolean values.

**Reference property mappings** are mappings for a reference property of an entity.

There are two types of mappings for the reference property: **ReferencePropertyMapping** and **ReferenceMultiFieldPropertyMapping.**

These mappings are supported for the following reference properties:

1. Embedded
2. Many-to-one association
3. One-to-one association

Additionally, **ReferenceMultiFieldPropertyMapping** supports one-to-many association (actual for JSON and XML formats).

A reference entity for both mappings is processed according to **ReferenceImportPolicy**:

1. **Create** - always create a reference entity without searching existing one. The new entity is set as a reference property value.
2. **Create if missing** - create a reference entity if the existing one is not found. Otherwise, the existing entity is set as a reference property value.
3. **Ignore if missing** - not create a reference entity if the existing one is not found. If there is no existing entity, the reference property has a null value.
4. **Fail if missing** - fail entity import if there is no existing reference entity.

**Note:** For embedded and one-to-many associations only the Create policy is supported.

**ReferencePropertyMapping** is a mapping for the reference property mapped by one field from input data.

| **Field** | **Description** | **Required** |
| --- | --- | --- |
| entityPropertyName | Name of the reference property which value will be set | Yes |
| lookupPropertyName | Property of the reference entity that is used to search existing one | Yes |
| dataFieldName | Field name/tag name/column from the input data <br/>that has a raw value of lookupPropertyName | Yes |
| referenceImportPolicy | ReferenceImportPolicy | Yes |

**ReferenceMultiFieldPropertyMapping** is a mapping for the reference property mapped by multiple fields from input data.

| **Field** | **Description** | **Required** |
| --- | --- | --- |
| entityPropertyName | Name of the reference property which value will be set | Yes |
| dataFieldName | Field/tag name from the input data that has raw values of the reference entity properties (**Note:** actual for JSON/XML formats) | No |
| propertyMappings | Mappings for the reference entity properties | Yes |
| lookupPropertyNames | Names of the properties by which an existing entity will be search | Yes (except the CREATE <br/>import policy) |
| referenceImportPolicy | ReferenceImportPolicy | Yes |

**CustomPropertyMapping** is a mapping for entity property that has a custom value.

| **Field** | **Description** | **Required** |
| --- | --- | --- |
| entityPropertyName | Name of the entity reference property which value will be set | Yes |
| customValueFunction | A function to get a custom value of the property. Input parameter - CustomMappingContext that contains raw values from the input data and ImportConfiguration. Output parameter - property value. | Yes |

### UniqueEntityConfiguration

UniqueEntityConfiguration allows configuring how to process a case if the entity with the same values of the particular properties already exists.

| **Field** | **Description** | **Required** |
| --- | --- | --- |
| entityPropertyNames | Names of the properties by which values  the duplicate entity will be search | Yes |
| duplicateEntityPolicy | Policy to process a found duplicate | Yes |

For each extracted entity from input data, it is checked whether a duplicate entity exists or not. If the duplicate is found, it is processed according to the specified policy.

Implemented policies:

1. Update - an extracted entity not imported, existing one is updated. Properties of an existing entity are populated by values from input data as for extracted entity.
2. Skip - an extracted entity not imported, existing one is not changed.
3. Abort - the import process is aborted immediately. **Note:** depending on the ImportTransactionStrategy the result of the import process differs: no one entity imported (single transaction) or entities, before the entity for which the duplicate found, are imported (transaction per entity).

### ImportTransactionStrategy

ImportTransactionStrategy defines how to import the entities extracted from input data.

Implemented strategies:

1. ***Single transaction:*** all entities are imported in one transaction. If an error occurs during any entity import, the import process fails and no one entity is imported.
2. ***Transaction per entity:*** each entity is imported in a separate transaction. If an error occurs during any entity import,  the import process continues and the remaining entities are imported.
3. ***Transaction per batch:*** the entities are imported by batches. If any entity import in batch fails, the import process continues. All entities in the not imported batch are marked as failed. The batch size can be speicified in import configuration (default value = 100).

### Pre-import predicate

It is possible to add validation of extracted entity before import using a pre-import predicate.

As an input parameter, a pre-import predicate has an EntityExtractionResult that contains:

1. An entity extracted from input data
2. ImportedDataItem: source of raw values for the entity properties

### Entity initializer

It is possible to make additional changes with extracted entity before import using the "entityInitializer" consumer in import configuration.

## Examples

Examples of import configuration creation for each support format are presented below.

### Data model

Let's consider the following entities: Order, Customer, OrderItem, Product.

The Order entity has the following properties:

| Property name | Type |
| --- | --- |
| number | String |
| date | java.util.Date |
| amount | BigDecimal |
| customer | Customer |
| orderItems | List\<OrderItem\> |

The Customer entity has the following properties:

| Property name | Type |
| --- | --- |
| firstName | String |
| lastName | String |
| name | String |
| birthdate | java.util.Date |
| email | String |
| grade | CustomerGrade |

The OrderItem entity has the following properties:

| Property name | Type |
| --- | --- |
| quantity | Integer |
| product | Product |
| order | order |

The Product entity has the following properties:

| Property name | Type |
| --- | --- |
| name | String |
| price | BigDecimal |

### **Excel (xlsx)**

**Example 1**

ImportConfiguration to import orders from Excel file in the following format:

| Customer Name | Order Number | Order Date | Order Amount |
| --- | --- | --- | --- |
| James Smith | 1234-0001 | 12/06/2021 12:00 | 25.5 |
| William Johnson | 1235-0001 | 13/06/2021 14:00 | 50 |

**Import configuration:**

Values for the "number", "date", "amount" properties are taken from the "Order Number", "Order Date", "Order Amount" columns respectively.

The "customer" property is a reference property represented in the input data by one property - "name". The value for the "name" property contains in the "Customer Name" column. The "name" property is a lookup property by which an existing customer will be searched. If an existing customer is not found by name, a new one is NOT created (because of `ReferenceImportPolicy.IGNORE_IF_MISSING`).

Before import, it is checked whether there is an order with the same number, date, and customer name. If such an order exists, the number, date, amount, and customer name will be updated by values from input data.

```java
  ImportConfiguration importConfiguration = ImportConfiguration.builder(Order.class, InputDataFormat.XLSX)
                .addSimplePropertyMapping("number", "Order Number")
                .addSimplePropertyMapping("date", "Order Date")
                .addSimplePropertyMapping("amount", "Order Amount")
                .addReferencePropertyMapping("customer", "Customer Name", "name", ReferenceImportPolicy.IGNORE_IF_MISSING)
                .addUniqueEntityConfiguration(DuplicateEntityPolicy.UPDATE, "number", "date", "customer.name")
                .withDateFormat("dd/MM/yyyy HH:mm")
                .withTransactionStrategy(ImportTransactionStrategy.TRANSACTION_PER_ENTITY)
                .build();
```

**Example 2**

ImportConfiguration to import orders from Excel file in the following format:

| Customer First Name | Customer Last Name | Customer Email | Order Number | Order Amount |
| --- | --- | --- | --- | --- |
| James | Smith | [j.smith@mail.com](mailto:j.smith@mail.com) | 1234-0001 | 25.5 |
| William | Johnson | [w.johnson@mail.com](mailto:w.johnson@mail.com) | 1235-0001 | 50 |

**Import configuration:**

Values for the "number", "amount" properties are taken from the "Order Number", "Order Amount" columns respectively.

The "customer" reference property is mapped by three properties:

1. firstName - the value is taken from the "Customer First Name" column;
2. lastName - the value is taken from the "Customer Last Name" column;
3. email - the value is taken from the "Customer Email" column.

Existing customer is searched by "firstName", "lastName" properties (lookupPropertiyNames). If the customer is not found in the database, it will be created (`ReferenceImportPolicy.CREATE_IF_MISSING` policy).

Before import, it is checked whether a created order has a number. If order number is null, then an order is not imported. Otherwise, the order is imported and today is set as order date.

```java
  ImportConfiguration importConfiguration = ImportConfiguration.builder(Order.class, InputDataFormat.XLSX)
                .addSimplePropertyMapping("number", "Order Number")
                .addSimplePropertyMapping("amount", "Order Amount")
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("customer", ReferenceImportPolicy.CREATE_IF_MISSING)
                        .addSimplePropertyMapping("firstName", "Customer First Name")
                        .addSimplePropertyMapping("lastName", "Customer Last Name")
                        .addSimplePropertyMapping("email", "Customer Email")
                        .withLookupPropertyNames("firstName", "lastName")
                        .build())
                .withPreImportPredicate(extractionResult -> {
                    Order order = (Order) extractionResult.getEntity();
                    if (order.getNumber() == null) {
                        return false;
                    } else {
                        order.setDate(currentDate);
                        return true;
                    }
                })
                .withTransactionStrategy(ImportTransactionStrategy.TRANSACTION_PER_ENTITY)
                .build();
```

### CSV

ImportConfiguration to import customers from CSV file in the following format:

```text
Customer Name,Customer Email
James Smith,j.smith@mail.com
William Johnson,w.johnson@mail.com
```

**Import configuration:**

Value for the "email" property is taken from the "Customer Email" column.

Let's suppose that the Customer entity has two separate attributes for the "firstName" and for the "lastName". The values of these attributes should be taken from a single "Customer Name" column of the CSV file. We'll use custom property mapping for that:

```java
 ImportConfiguration importConfiguration = ImportConfiguration.builder(Customer.class, InputDataFormat.CSV)
                .addSimplePropertyMapping("email", "Customer Email")
                .addCustomPropertyMapping("firstName", customMappingContext -> {
                    String fullName = (String) customMappingContext.getRawValues().get("Customer Name");
                    String[] parsedName = fullName.split("\\s+");
                    return parsedName[0];
                })
                .addCustomPropertyMapping("lastName", customMappingContext -> {
                    String fullName = (String) customMappingContext.getRawValues().get("Customer Name");
                    String[] parsedName = fullName.split("\\s+");
                    return parsedName[1];
                })
                .withTransactionStrategy(ImportTransactionStrategy.TRANSACTION_PER_ENTITY)
                .build();
```

### JSON

ImportConfiguration to import orders from JSON file in the following format:

```json
[
   {
      "customer":{
         "firstName":"James",
         "lastName":"Smith"
      },
      "orderNumber":"1234-0001",
      "orderAmount":25,
      "orderDate":"12/06/2021 12:00",
      "items":[
         {
            "productName":"Outback Power Nano-Carbon Battery 12V",
            "quantity":2
         }
      ]
   },
   {
      "customer":{
         "firstName":"William",
         "lastName":"Johnson"
      },
      "orderNumber":"1235-0001",
      "orderAmount":57,
      "orderDate":"13/06/2021 14:00",
      "items":[
         {
            "productName":"Fullriver Sealed Battery 6V",
            "quantity":1
         },
         {
            "productName":"Outback Power Nano-Carbon Battery 12V",
            "quantity":4
         }
      ]
   }
]
```

**Import configuration:**

Values for the "number", " date", "amount"  properties are taken from the "orderNumber", "orderDate", "orderAmount" fields respectively.

The "customer" reference property is mapped by two properties raw values of which are taken the "customer" field:

1. firstName - the value is taken from the "firstName" field.
2. lastName -the value is taken from the "lastName" field.

An existing customer is searched by "firstName" and "lastName" properties.

**Note:** For JSON/XML formats, the "dataFieldName" in `ReferenceMultiFieldPropertyMapping`  is actual because property values for the reference entity can be placed in the separate field/tag. In the example above, the "customer" field in JSON is the "dataFieldName" in `ReferenceMultiFieldPropertyMapping`  for the "customer" property. For XLSX and CSV format the "dataFieldName" is always null in `ReferenceMultiFieldPropertyMapping`.

The "orderItems" is a collection of references raw value of which is taken from the "items" field. Each reference in the collection is mapped by two properties:

1. product - the reference property that is mapped by the "productName" field. The "productName" field contains the value of the "name" property from the Product entity.

2. quantity -  the value is taken from the "quantity" field.



```java
   ImportConfiguration importConfiguration = ImportConfiguration.builder(Order.class, InputDataFormat.JSON)
                .addSimplePropertyMapping("number", "orderNumber")
                .addSimplePropertyMapping("date", "orderDate")
                .addSimplePropertyMapping("amount", "orderAmount")
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("customer", ReferenceImportPolicy.CREATE_IF_MISSING)
                        .withDataFieldName("customer")
                        .addSimplePropertyMapping("firstName", "firstName")
                        .addSimplePropertyMapping("lastName", "lastName")
                        .withLookupPropertyNames("firstName", "lastName")
                        .build())
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("orderItems", ReferenceImportPolicy.CREATE)
                        .withDataFieldName("items")
                        .addSimplePropertyMapping("quantity", "quantity")
                        .addReferencePropertyMapping("product", "productName", "name", ReferenceImportPolicy.FAIL_IF_MISSING)
                        .build())
                .withDateFormat("dd/MM/yyyy HH:mm")
                .withTransactionStrategy(ImportTransactionStrategy.SINGLE_TRANSACTION)
                .build();
```

### XML

ImportConfiguration to import orders from XML file in the following format:

```xml
<orders>
    <order>
        <customer>
            <firstName>James</firstName>
            <lastName>Smith</lastName>
        </customer>
        <number>1234-0002</number>
        <date>13/06/2021 15:00</date>
        <amount>26</amount>
        <items>
            <item>
                <productName>Fullriver Sealed Battery 6V</productName>
                <quantity>3</quantity>
            </item>
             <item>
                <productName>Outback Power Nano-Carbon Battery 12V</productName>
                <quantity>1</quantity>
            </item>
        </items>
    </order>
    <order>
        <customer>
            <firstName>William</firstName>
            <lastName>Johnson</lastName>
        </customer>
        <number>1235-0002</number>
        <date>13/07/2021 10:00</date>
        <amount>10</amount>
        <items>
            <item>
                <productName>Fullriver Sealed Battery 6V</productName>
                <quantity>2</quantity>
            </item>
        </items>
    </order>
</orders>
```

**Import configuration:**

Values for the "number", " date", "amount"  properties are taken from the "number", "date", "amount" tags respectively.

The "customer" reference property is mapped by two properties raw values of which are taken the "customer" tag:

1. firstName - the value is taken from the "firstName" tag value.
2. lastName - the value is taken from the "lastName" tag value.

An existing customer is searched by "firstName" and "lastName" properties.

The "orderItems" is a collection of references raw value of which is taken from the "items" tag. Each reference in the collection is mapped by two properties:

1. product - the reference property that is mapped by the "productName" tag. The "productName" tag contains the value of the "name" property from the Product entity.
2. quantity -  the value is taken from the "quantity" tag.

```java
        ImportConfiguration importConfiguration = ImportConfiguration.builder(Order.class, InputDataFormat.XML)
                .addSimplePropertyMapping("number", "number")
                .addSimplePropertyMapping("date", "date")
                .addSimplePropertyMapping("amount", "amount")
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("customer", ReferenceImportPolicy.CREATE_IF_MISSING)
                        .withDataFieldName("customer")
                        .addSimplePropertyMapping("firstName", "firstName")
                        .addSimplePropertyMapping("lastName", "lastName")
                        .withLookupPropertyNames("firstName", "lastName")
                        .build())
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("orderItems", ReferenceImportPolicy.CREATE)
                        .withDataFieldName("items")
                        .addSimplePropertyMapping("quantity", "quantity")
                        .addReferencePropertyMapping("product", "productName", "name", ReferenceImportPolicy.FAIL_IF_MISSING)
                        .build())
                .withDateFormat("dd/MM/yyyy HH:mm")
                .withTransactionStrategy(ImportTransactionStrategy.TRANSACTION_PER_BATCH)
                .withImportBatchSize(50)
                .build();
```
