package io.jmix.saml;

import io.jmix.saml.entity.Foo;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SamlTest {

	@Autowired
	DataManager dataManager;

	@Test
	void contextLoads() {
	}

	@Test
	void testFoo() {
		Foo foo = dataManager.create(Foo.class);
		foo.setName("abc");

		Foo foo1 = dataManager.save(foo);
		assertEquals(foo, foo1);

		Foo foo2 = dataManager.load(Id.of(foo)).one();
		assertEquals(foo, foo2);
	}
}
