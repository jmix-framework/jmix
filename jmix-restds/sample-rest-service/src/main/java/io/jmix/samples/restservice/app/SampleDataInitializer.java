package io.jmix.samples.restservice.app;

import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.security.Authenticated;
import io.jmix.samples.restds.common.entity.Product;
import io.jmix.samples.restds.common.entity.ProductGroup;
import io.jmix.samples.restservice.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
public class SampleDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(SampleDataInitializer.class);
    private final DataManager dataManager;
    private final FileStorage fileStorage;

    public SampleDataInitializer(DataManager dataManager, FileStorage fileStorage) {
        this.dataManager = dataManager;
        this.fileStorage = fileStorage;
    }

    @EventListener(ApplicationStartedEvent.class)
    @Authenticated
    public void init() {
        List<Customer> customers = dataManager.load(Customer.class).all().maxResults(1).list();
        if (!customers.isEmpty()) {
            log.info("Customers already exist, skipping initialization");
        } else {
            List<CustomerRegion> regions = createRegions();
            createCustomers(regions);
            createEmployees();
            List<ProductGroup> productGroups = createProductGroups();
            createProducts(productGroups);
        }
        log.info("Ready for testing");
    }

    private List<CustomerRegion> createRegions() {
        log.info("Creating regions");
        CustomerRegion region = dataManager.create(CustomerRegion.class);
        region.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        region.setName("North America");
        CustomerRegion saved = dataManager.save(region);
        log.info("Regions created");
        return List.of(saved);
    }

    public void createCustomers(List<CustomerRegion> regions) {
        log.info("Creating customers");
        Customer customer = dataManager.create(Customer.class);
        customer.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        customer.setFirstName("Robert");
        customer.setLastName("Taylor");
        customer.setEmail("robert@example.com");
        customer.setRegion(regions.get(0));
        customer.setDocument(uploadDocument("doc.txt" , "Initial content\nLine 1\nLine 2"));

        // create and save two customer contacts
        CustomerContact contact1 = dataManager.create(CustomerContact.class);
        contact1.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        contact1.setCustomer(customer);
        contact1.setContactType(ContactType.PHONE);
        contact1.setContactValue("555-555-5555");
        contact1.setPreferred(true);

        CustomerContact contact2 = dataManager.create(CustomerContact.class);
        contact2.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        contact2.setCustomer(customer);
        contact2.setContactType(ContactType.EMAIL);
        contact2.setContactValue("robert@example.com");

        dataManager.save(customer, contact1, contact2);

        log.info("Customers created");
    }

    private void createEmployees() {
        log.info("Creating employees");
        Employee employee = dataManager.create(Employee.class);
        employee.setId(UUID.fromString("00000000-0000-0000-0000-000000000010"));
        employee.setName("John Doe");
        ((EmployeeExt) employee).setExtInfo("Ext info");
        dataManager.save(employee);
        log.info("Employees created");
    }

    private List<ProductGroup> createProductGroups() {
        log.info("Creating product groups");
        ProductGroup productGroup = dataManager.create(ProductGroup.class);
        productGroup.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        productGroup.setName("Group 1");
        dataManager.save(productGroup);
        log.info("Product groups created");
        return List.of(productGroup);
    }

    private void createProducts(List<ProductGroup> productGroups) {
        log.info("Creating products");
        Product product = dataManager.create(Product.class);
        product.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        product.setName("Product 1");
        product.setGroup(productGroups.get(0));
        dataManager.save(product);
        log.info("Products created");
    }

    private FileRef uploadDocument(String name, String content) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return fileStorage.saveStream(name, inputStream);
    }

}