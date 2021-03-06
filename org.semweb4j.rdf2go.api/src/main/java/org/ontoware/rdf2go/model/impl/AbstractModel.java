/**
 * LICENSE INFORMATION
 * 
 * Copyright 2005-2008 by FZI (http://www.fzi.de). Licensed under a BSD license
 * (http://www.opensource.org/licenses/bsd-license.php) <OWNER> = Max Völkel
 * <ORGANIZATION> = FZI Forschungszentrum Informatik Karlsruhe, Karlsruhe,
 * Germany <YEAR> = 2010
 * 
 * Further project information at http://semanticweb.org/wiki/RDF2Go
 */

package org.ontoware.rdf2go.model.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.exception.MalformedQueryException;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.exception.QueryLanguageNotSupportedException;
import org.ontoware.rdf2go.exception.SyntaxNotSupportedException;
import org.ontoware.rdf2go.model.Diff;
import org.ontoware.rdf2go.model.DiffReader;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.TriplePattern;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.DatatypeLiteral;
import org.ontoware.rdf2go.model.node.LanguageTagLiteral;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.NodeOrVariable;
import org.ontoware.rdf2go.model.node.PlainLiteral;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.ResourceOrVariable;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.UriOrVariable;
import org.ontoware.rdf2go.model.node.Variable;
import org.ontoware.rdf2go.model.node.impl.DatatypeLiteralImpl;
import org.ontoware.rdf2go.model.node.impl.LanguageTagLiteralImpl;
import org.ontoware.rdf2go.model.node.impl.PlainLiteralImpl;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * adapter that maps the rdf2go model functions to a smaller subset of methods
 * 
 * @author mvo
 * 
 */
public abstract class AbstractModel extends AbstractModelRemovePatterns implements Model {
	
	private static final long serialVersionUID = -8779401783869682830L;
	
	private static Logger log = LoggerFactory.getLogger(AbstractModel.class);
	
	/**
	 * The underlying implementation.
	 */
	protected Object model;
	
	/**
	 * Uses to store runtime-properties - no related to RDF at all. You could
	 * for example store a user session object here.
	 */
	private Map<URI,Object> runtimeProperties = new HashMap<URI,Object>();
	
	private boolean open = false;
	
	/** subclasses should overwrite this for performance reasons */
	@Override
	public void addModel(Model model) {
		ClosableIterator<Statement> it = model.iterator();
		Set<Statement> statements = new HashSet<Statement>();
		while(it.hasNext()) {
			Statement stmt = it.next();
			statements.add(stmt);
		}
		it.close();
		for(Statement stmt : statements) {
			this.addStatement(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
		}
	}
	
	@Override
	public void addAll(Iterator<? extends Statement> other) throws ModelRuntimeException {
		assertModel();
		super.addAll(other);
	}
	
	@Override
	public void addStatement(Resource subject, URI predicate, String literal)
	        throws ModelRuntimeException {
		assertModel();
		super.addStatement(subject, predicate, literal);
	}
	
	@Override
	public void addStatement(Resource subject, URI predicate, String literal, String languageTag)
	        throws ModelRuntimeException {
		assertModel();
		super.addStatement(subject, predicate, literal, languageTag);
	}
	
	@Override
	public void addStatement(Resource subject, URI predicate, String literal, URI datatypeURI)
	        throws ModelRuntimeException {
		assertModel();
		super.addStatement(subject, predicate, literal, datatypeURI);
	}
	
	@Override
	public void addStatement(Statement statement) throws ModelRuntimeException {
		assertModel();
		super.addStatement(statement);
	}
	
	// essential methods
	
	// core rdf2go model methods
	// /////////////////////////
	
	@Override
	public void addStatement(String subjectURIString, URI predicate, String literal)
	        throws ModelRuntimeException {
		assertModel();
		super.addStatement(subjectURIString, predicate, literal);
	}
	
	@Override
	public void addStatement(String subjectURIString, URI predicate, String literal,
	        String languageTag) throws ModelRuntimeException {
		assertModel();
		super.addStatement(subjectURIString, predicate, literal, languageTag);
	}
	
	@Override
	public void addStatement(String subjectURIString, URI predicate, String literal, URI datatypeURI)
	        throws ModelRuntimeException {
		assertModel();
		super.addStatement(subjectURIString, predicate, literal, datatypeURI);
	}
	
	/**
	 * This method checks if the model is properly initialized and i.e. not
	 * closed.
	 */
	protected void assertModel() {
		if(this.getUnderlyingModelImplementation() == null) {
			throw new ModelRuntimeException("Underlying model is null.");
		}
		if(!isOpen())
			throw new ModelRuntimeException("Model is not open");
	}
	
	/**
	 * Close connection to defined, underlying implementation
	 */
	@Override
	public void close() {
		if(isOpen()) {
			this.open = false;
		} else {
			log.trace("Model was closed already, ignored.");
		}
	}
	
	/**
	 * OVERWRITE ME
	 */
	@Deprecated
	@Override
	public void commit() {
		// do nothing
	}
	
	/**
	 * Convenience method. Might have faster implementations. Overwrite me!
	 */
	@Override
	public boolean contains(ResourceOrVariable subject, UriOrVariable predicate,
	        NodeOrVariable object) throws ModelRuntimeException {
		assertModel();
		ClosableIterator<? extends Statement> cit = findStatements(subject, predicate, object);
		boolean result = cit.hasNext();
		cit.close();
		return result;
	}
	
	/**
	 * Convenience method.
	 */
	@Override
	public boolean contains(ResourceOrVariable subject, UriOrVariable predicate, String plainLiteral)
	        throws ModelRuntimeException {
		assertModel();
		return contains(subject, predicate, new PlainLiteralImpl(plainLiteral));
	}
	
	/**
	 * Convenience method.
	 */
	@Override
	public boolean contains(Statement s) throws ModelRuntimeException {
		assertModel();
		return contains(s.getSubject(), s.getPredicate(), s.getObject());
	}
	
	/**
	 * Very inefficient. Please override.
	 */
	@Override
	public long countStatements(TriplePattern pattern) throws ModelRuntimeException {
		assertModel();
		ClosableIterator<?> it = findStatements(pattern);
		int count = 0;
		while(it.hasNext()) {
			count++;
			it.next();
		}
		it.close();
		return count;
	}
	
	@Override
	public DatatypeLiteral createDatatypeLiteral(String literal, URI datatypeURI)
	        throws ModelRuntimeException {
		return new DatatypeLiteralImpl(literal, datatypeURI);
	}
	
	@Override
	public LanguageTagLiteral createLanguageTagLiteral(String literal, String languageTag)
	        throws ModelRuntimeException {
		return new LanguageTagLiteralImpl(literal, languageTag);
	}
	
	@Override
	public PlainLiteral createPlainLiteral(String literal) throws ModelRuntimeException {
		return new PlainLiteralImpl(literal);
	}
	
	// ///////////
	// stubs
	
	@Override
	public Statement createStatement(Resource subject, URI predicate, Node object) {
		return new StatementImpl(getContextURI(), subject, predicate, object);
	}
	
	@Override
	public TriplePattern createTriplePattern(ResourceOrVariable subject, UriOrVariable predicate,
	        NodeOrVariable object) {
		return new TriplePatternImpl(subject, predicate, object);
	}
	
	@Override
	public URI createURI(String uriString) throws ModelRuntimeException {
		return new URIImpl(uriString);
	}
	
	/**
	 * Convenience method.
	 */
	@Override
	public ClosableIterator<Statement> findStatements(TriplePattern triplepattern)
	        throws ModelRuntimeException {
		assertModel();
		return findStatements(triplepattern.getSubject(), triplepattern.getPredicate(),
		        triplepattern.getObject());
	}
	
	/**
	 * Computes a Diff by using HashSets.
	 */
	@Override
	public Diff getDiff(Iterator<? extends Statement> other) throws ModelRuntimeException {
		assertModel();
		
		Set<Statement> otherSet = new HashSet<Statement>();
		while(other.hasNext())
			otherSet.add(other.next());
		log.trace("this has " + size() + " triples, other has " + otherSet.size() + " triples");
		
		// added Statements = present in other, but not this
		Set<Statement> added = new HashSet<Statement>();
		for(Statement s : otherSet) {
			if(!this.contains(s))
				added.add(s);
		}
		
		// removed = present here, but no longer in other
		Set<Statement> removed = new HashSet<Statement>();
		for(Statement s : this) {
			if(!otherSet.contains(s)) {
				log.trace("otherSet does not contain " + s);
				removed.add(s);
			}
		}
		
		log.trace(added.size() + " triples added, " + removed.size() + " removed.");
		
		// These iterators are not closable, so we don't have to close them
		return new DiffImpl(added.iterator(), removed.iterator());
	}
	
	/**
	 * Note: This is a property of the model, not an RDF property
	 * 
	 * @param propertyURI used as a property name to get the model property
	 * @return stored property value for this model or null
	 */
	@Override
	public Object getProperty(URI propertyURI) {
		return this.runtimeProperties.get(propertyURI);
	}
	
	@Override
	public Object getUnderlyingModelImplementation() {
		if(!isOpen())
			throw new ModelRuntimeException("Model is not open");
		return this.model;
	}
	
	/** sublcasses should override this method for performance */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}
	
	@Override
	public boolean isOpen() {
		return this.open;
	}
	
	@Override
	public URI newRandomUniqueURI() {
		return URIGenerator.createNewRandomUniqueURI();
	}
	
	/**
	 * Open connection to defined, unterlying implementation.
	 */
	@Override
	public Model open() {
		if(isOpen()) {
			log.warn("Model is already open. Ignored.");
		} else {
			this.open = true;
		}
		return this;
	}
	
	/**
	 * Throws an exception if the syntax is not SPARQL
	 */
	@Override
	public ClosableIterable<Statement> queryConstruct(String query, String querylanguage)
	        throws QueryLanguageNotSupportedException, ModelRuntimeException {
		assertModel();
		if(querylanguage.equalsIgnoreCase("SPARQL"))
			return sparqlConstruct(query);
		// else
		throw new QueryLanguageNotSupportedException("Unsupported query language: " + querylanguage);
	}
	
	/**
	 * Throws an exception if the syntax is not SPARQL
	 */
	@Override
	public QueryResultTable querySelect(String query, String querylanguage)
	        throws QueryLanguageNotSupportedException, ModelRuntimeException {
		assertModel();
		if(querylanguage.equalsIgnoreCase("SPARQL"))
			return sparqlSelect(query);
		// else
		throw new QueryLanguageNotSupportedException("Unsupported query language: " + querylanguage);
	}
	
	/**
	 * Throws an exception if the syntax is not RDF/XML. Subclasses are
	 * encouraged to overwrite this.
	 */
	@Override
	public void readFrom(InputStream in, Syntax syntax) throws IOException, ModelRuntimeException {
		assertModel();
		if(syntax == Syntax.RdfXml) {
			readFrom(in);
		} else {
			throw new ModelRuntimeException("Unsupported syntax: " + syntax);
		}
	}
	
	/**
	 * Note: <em>Subclasses are encouraged to overwrite this.</em>
	 * 
	 * Throws an exception if the syntax is not RDF/XML. Sets baseURI to the
	 * empty string.
	 */
	@Override
	public void readFrom(InputStream in, Syntax syntax, String baseURI) throws IOException,
	        ModelRuntimeException {
		assertModel();
		if(syntax == Syntax.RdfXml) {
			readFrom(in);
		} else {
			throw new ModelRuntimeException("Unsupported syntax: " + syntax);
		}
	}
	
	/**
	 * Note: <em>Subclasses are encouraged to overwrite this.</em>
	 * 
	 * Throws an exception if the syntax is not RDF/XML. Sets base URI to the
	 * empty string (default).
	 * 
	 * @throws IOException from reader
	 * @throws ModelRuntimeException from model
	 */
	@Override
	public void readFrom(Reader reader, Syntax syntax, String baseURI)
	        throws ModelRuntimeException, IOException {
		assertModel();
		if(syntax == Syntax.RdfXml) {
			readFrom(reader);
		} else {
			throw new ModelRuntimeException("Unsupported syntax: " + syntax);
		}
	}
	
	@Override
	public void removeAll() throws ModelRuntimeException {
		assertModel();
		super.removeAll();
	}
	
	@Override
	public void removeAll(Iterator<? extends Statement> statements) {
		assertModel();
		super.removeAll(statements);
	}
	
	@Override
	public void removeStatement(Resource subject, URI predicate, String literal)
	        throws ModelRuntimeException {
		assertModel();
		super.removeStatement(subject, predicate, literal);
	}
	
	@Override
	public void removeStatement(Resource subject, URI predicate, String literal, String languageTag)
	        throws ModelRuntimeException {
		assertModel();
		super.removeStatement(subject, predicate, literal, languageTag);
	}
	
	@Override
	public void removeStatement(Resource subject, URI predicate, String literal, URI datatypeURI)
	        throws ModelRuntimeException {
		assertModel();
		super.removeStatement(subject, predicate, literal, datatypeURI);
	}
	
	@Override
	public void removeStatement(Statement statement) throws ModelRuntimeException {
		assertModel();
		super.removeStatement(statement);
	}
	
	@Override
	public void removeStatement(String subjectURIString, URI predicate, String literal)
	        throws ModelRuntimeException {
		assertModel();
		super.removeStatement(subjectURIString, predicate, literal);
	}
	
	@Override
	public void removeStatement(String subjectURIString, URI predicate, String literal,
	        String languageTag) throws ModelRuntimeException {
		assertModel();
		super.removeStatement(subjectURIString, predicate, literal, languageTag);
	}
	
	@Override
	public void removeStatement(String subjectURIString, URI predicate, String literal,
	        URI datatypeURI) throws ModelRuntimeException {
		assertModel();
		super.removeStatement(subjectURIString, predicate, literal, datatypeURI);
	}
	
	@Override
	public void removeStatements(ResourceOrVariable subject, UriOrVariable predicate,
	        NodeOrVariable object) throws ModelRuntimeException {
		assertModel();
		super.removeStatements(subject, predicate, object);
	}
	
	@Override
	public void removeStatements(TriplePattern triplePattern) throws ModelRuntimeException {
		assertModel();
		super.removeStatements(triplePattern);
	}
	
	/**
	 * Convenience method.
	 */
	@Override
	public String serialize(Syntax syntax) throws SyntaxNotSupportedException {
		StringWriter sw = new StringWriter();
		try {
			this.writeTo(sw, syntax);
		} catch(IOException e) {
			throw new ModelRuntimeException(e);
		}
		return sw.getBuffer().toString();
	}
	
	/**
	 * Note: <em>Subclasses SHOULD overwrite this.</em>
	 * 
	 * This implementation simply ignores the request.
	 */
	@Deprecated
	@Override
	public void setAutocommit(boolean autocommit) {
		// do nothing
	}
	
	/**
	 * Add an arbitrary property, this will not be persisted and is only
	 * available at runtime. This allows Model to serve as a central data model
	 * in larger applications (like SemVersion.ontoware.org)
	 * 
	 * @param propertyURI used as a parameter name for storing a model property
	 * @param value any java object. Only stored in memory, not in RDF.
	 */
	@Override
	public void setProperty(URI propertyURI, Object value) {
		this.runtimeProperties.put(propertyURI, value);
	}
	
	/**
	 * This is a really slow implementation, please override.
	 */
	@Override
	public long size() throws ModelRuntimeException {
		assertModel();
		ClosableIterator<Statement> it = iterator();
		int count = 0;
		while(it.hasNext()) {
			count++;
			it.next();
		}
		it.close();
		return count;
	}
	
	// work around Sesame not having this yet
	@Override
	public boolean sparqlAsk(String query) throws ModelRuntimeException, MalformedQueryException {
		QueryResultTable table = sparqlSelect(query);
		ClosableIterator<QueryRow> it = table.iterator();
		boolean result = it.hasNext();
		it.close();
		return result;
	}
	
	/**
	 * Implementations with support for transactions should use them instead of
	 * this implementation.
	 */
	@Override
	public synchronized void update(DiffReader diff) throws ModelRuntimeException {
		assertModel();
		for(Statement r : diff.getRemoved()) {
			removeStatement(r);
		}
		
		for(Statement a : diff.getAdded()) {
			addStatement(a);
		}
	}
	
	/**
	 * Throws an exception if the syntax is not known
	 */
	@Override
	public void writeTo(OutputStream out, Syntax syntax) throws IOException, ModelRuntimeException {
		assertModel();
		if(syntax == Syntax.RdfXml) {
			writeTo(out);
		} else {
			throw new ModelRuntimeException("Unsupported syntax: " + syntax);
		}
	}
	
	/* fast, no need to override */
	@Override
	public BlankNode addReificationOf(Statement statement) {
		BlankNode bnode = createBlankNode();
		return (BlankNode)addReificationOf(statement, bnode);
	}
	
	@Override
	public Resource addReificationOf(Statement statement, Resource resource) {
		Diff diff = new DiffImpl();
		diff.addStatement(resource, RDF.type, RDF.Statement);
		diff.addStatement(resource, RDF.subject, statement.getSubject());
		diff.addStatement(resource, RDF.predicate, statement.getPredicate());
		diff.addStatement(resource, RDF.object, statement.getObject());
		update(diff);
		return resource;
	}
	
	@Override
	public boolean hasReifications(Statement statement) {
		return this.sparqlAsk("ASK WHERE { " + " ?res " + RDF.type.toSPARQL() + " "
		        + RDF.Statement.toSPARQL() + " ." + " ?res " + RDF.subject.toSPARQL() + " "
		        + statement.getSubject().toSPARQL() + " ." + " ?res " + RDF.predicate.toSPARQL()
		        + " " + statement.getPredicate().toSPARQL() + " ." + " ?res "
		        + RDF.object.toSPARQL() + " " + statement.getObject().toSPARQL() + " ." + " }");
	}
	
	/*
	 * inefficient, loads all in memory. should be OK for almost all practical
	 * cases (when each statement has a small number of refications)
	 */
	@Override
	public Collection<Resource> getAllReificationsOf(Statement statement) {
		QueryResultTable table = this.sparqlSelect("SELECT ?res WHERE { " + " ?res "
		        + RDF.type.toSPARQL() + " " + RDF.Statement.toSPARQL() + " ." + " ?res "
		        + RDF.subject.toSPARQL() + " " + statement.getSubject().toSPARQL() + " ."
		        + " ?res " + RDF.predicate.toSPARQL() + " " + statement.getPredicate().toSPARQL()
		        + " ." + " ?res " + RDF.object.toSPARQL() + " " + statement.getObject().toSPARQL()
		        + " ." + " }");
		LinkedList<Resource> result = new LinkedList<Resource>();
		ClosableIterator<QueryRow> it = table.iterator();
		while(it.hasNext()) {
			Resource res = it.next().getValue("res").asResource();
			result.add(res);
		}
		it.close();
		return result;
	}
	
	@Override
	public void deleteReification(Resource reificationResource) {
		Diff diff = new DiffImpl();
		diff.removeStatement(reificationResource, RDF.type, RDF.Statement);
		ClosableIterator<Statement> it = findStatements(reificationResource, RDF.subject,
		        Variable.ANY);
		while(it.hasNext()) {
			diff.removeStatement(it.next());
		}
		it.close();
		it = findStatements(reificationResource, RDF.predicate, Variable.ANY);
		while(it.hasNext()) {
			diff.removeStatement(it.next());
		}
		it.close();
		it = findStatements(reificationResource, RDF.object, Variable.ANY);
		while(it.hasNext()) {
			diff.removeStatement(it.next());
		}
		it.close();
		update(diff);
	}
}
