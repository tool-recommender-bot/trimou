package org.trimou.tests.servlet.locator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.trimou.tests.IntegrationTestUtils.getResolver;

import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.TemplateLocator;
import org.trimou.servlet.locator.ServletContextTemplateLocator;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class ServletContextTemplateLocatorTest {

	@Deployment
	public static WebArchive createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class)
				// WEB-INF/templates
				.addAsWebInfResource(new StringAsset("<html/>"),
						"templates/foo.html")
				.addAsWebInfResource(new StringAsset("<html/>"),
						"templates/qux.html")
				.addAsWebInfResource(new StringAsset("<xml/>"),
						"templates/alpha.xml")
				.addAsWebInfResource(new StringAsset("<html/>"),
						"templates/cool/charlie.html")
				// templates
				.addAsWebResource(new StringAsset("<html/>"),
						"templates/bar.html")
				.addAsLibraries(
						getResolver().artifact(
								"org.trimou:trimou-extension-servlet")
								.resolveAsFiles());
	}

	@Test
	public void testAllIdentifiers() {

		TemplateLocator locator1 = new ServletContextTemplateLocator(
				10, "/WEB-INF/templates", "html");
		TemplateLocator locator2 = new ServletContextTemplateLocator(
			9, "/templates", "html");
		TemplateLocator locator3 = new ServletContextTemplateLocator(
				8, "/WEB-INF/templates");

		Set<String> locator1Names = locator1.getAllIdentifiers();
		assertEquals(3, locator1Names.size());
		assertTrue(locator1Names.contains("foo"));
		assertTrue(locator1Names.contains("qux"));
		assertTrue(locator1Names.contains("cool/charlie"));

		Set<String> locator2Names = locator2.getAllIdentifiers();
		assertEquals(1, locator2Names.size());
		assertTrue(locator2Names.contains("bar"));

		Set<String> locator3Names = locator3.getAllIdentifiers();
		assertEquals(4, locator3Names.size());
		assertTrue(locator3Names.contains("foo.html"));
		assertTrue(locator3Names.contains("qux.html"));
		assertTrue(locator3Names.contains("alpha.xml"));
		assertTrue(locator3Names.contains("cool/charlie.html"));
	}

	@Test
	public void testLocate() {

		TemplateLocator locator1 = new ServletContextTemplateLocator(
				10, "/WEB-INF/templates", "html");
		TemplateLocator locator2 = new ServletContextTemplateLocator(
			9, "/templates", "html");
		TemplateLocator locator3 = new ServletContextTemplateLocator(
				8, "/WEB-INF/templates");

		MustacheEngine factory = MustacheEngineBuilder.newBuilder()
				.addTemplateLocator(locator1).addTemplateLocator(locator2)
				.addTemplateLocator(locator3).build();

		Mustache foo = factory.getMustache("foo");
		assertNotNull(foo);
		assertEquals("<html/>", foo.render(null));

		Mustache bar = factory.getMustache("bar");
		assertNotNull(bar);
		assertEquals("<html/>", bar.render(null));

		Mustache alpha = factory.getMustache("alpha.xml");
		assertNotNull(alpha);
		assertEquals("<xml/>", alpha.render(null));

		Mustache charlie = factory.getMustache("cool/charlie");
		assertNotNull(charlie);
		assertEquals("<html/>", charlie.render(null));
	}
}
