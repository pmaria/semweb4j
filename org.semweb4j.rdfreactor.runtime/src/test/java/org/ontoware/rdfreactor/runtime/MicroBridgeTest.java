package org.ontoware.rdfreactor.runtime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.Variable;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.ontoware.rdfreactor.AllTests;
import org.ontoware.rdfreactor.runtime.example.Person;
import org.ontoware.rdfreactor.test.tag.File;
import org.ontoware.rdfreactor.test.tag.Tag;
import org.ontoware.rdfreactor.test.tag.TagAssignment;

@SuppressWarnings("unused")
public class MicroBridgeTest extends TestCase {

	private static Log log = LogFactory.getLog(MicroBridgeTest.class);

	Model m;

	public void setUp() {
		m = AllTests.m;
		assert m != null;

	}

	public void testAdd() throws Exception {
		log.debug("----------------------");
		URI resource = URIImpl.create("data://r1");
		URI prop = URIImpl.create("data://p1");
		Bridge.addValue(m, resource, prop, "Jim");
		m.dump();

		Iterator<? extends Statement> it = m.findStatements(resource, prop, Variable.ANY);
		assertTrue(it.hasNext());
	}

	public void testEqual() throws Exception {

		Person p = new Person(m, URIImpl.create("data://p1"), true);
		Person q = new Person(m, URIImpl.create("data://p1"), true);
		URI u = URIImpl.create("data://p1");

		assertTrue(p.equals(q));
		assertTrue(p.equals(u));
	}

	public void testSet() throws Exception {
		URI resource = URIImpl.create("data://r1");
		URI prop1 = URIImpl.create("data://p1");
		URI prop2 = URIImpl.create("data://p1");
		Bridge.addValue(m, resource, prop1, "Jon");
		Bridge.setValue(m, resource, prop2, "Jim");
		m.dump();

		assertTrue( m.contains(resource, prop1, Variable.ANY));
	}

	public void testRemove() throws Exception {
		URI resource = URIImpl.create("data://r1");
		URI prop = URIImpl.create("data://p1");

		m.addStatement(resource, prop, "Jim");
		assertTrue(m.iterator().hasNext());

		Iterator<? extends Statement> it = m.findStatements(resource, prop, Variable.ANY);
		assertTrue(it.hasNext());
		assertEquals(1, Bridge.getAllValues_asSet(m, resource, prop, String.class).size());
		assertEquals(1, Bridge.getAllValues(m, resource, prop, String.class).length);

		Iterator<? extends Statement> it2 = m.findStatements(resource, prop, Variable.ANY);
		assertTrue(it2.hasNext());
		while (it2.hasNext()) {
			Statement s = it2.next();
			// compare rdfnode to value
			assertTrue(s.getObject().equals("Jim"));
		}

		assertTrue(Bridge.removeValue(m, resource, prop, "Jim"));

		assertEquals(0, Bridge.getAllValues(m, resource, prop, String.class).length);
	}

	public void testSparqlSelect() throws Exception {

		File fileA = new File(m, URIImpl.create("file://a"));
		File fileB = new File(m, URIImpl.create("file://b"));

		Tag tagSemweb = new Tag(m, URIImpl.create("tag://semweb"));
		Tag tagPaper = new Tag(m, URIImpl.create("tag://paper"));

		TagAssignment ass1 = new TagAssignment(m, URIImpl.create("ass://1"));
		TagAssignment ass2 = new TagAssignment(m, URIImpl.create("ass://2"));
		TagAssignment ass3 = new TagAssignment(m, URIImpl.create("ass://3"));

		// a = 'paper'
		ass1.setTag(tagPaper);
		ass1.setFile(fileA);

		// a = 'semweb'
		ass2.setTag(tagSemweb);
		ass2.setFile(fileA);
		// b = 'semweb'
		ass3.setTag(tagSemweb);
		ass3.setFile(fileB);

		m.dump();

		Map<String, Class> returnTypes = new HashMap<String,Class>();
		returnTypes.put("file",File.class);
		OOQueryResultTable result = Bridge.getSparqlSelect(m, returnTypes,
				"SELECT ?file WHERE " + "{ ?a <" + TagAssignment.FILE + "> ?file. " + "?a <"
						+ TagAssignment.TAG + "> <" + tagSemweb + "> . }");

		for(OOQueryRow row : result) {
			assertEquals(result.getVariables().size(), 1);
			assertEquals(File.class, row.getValue("file").getClass());
		}
		
	}

}
