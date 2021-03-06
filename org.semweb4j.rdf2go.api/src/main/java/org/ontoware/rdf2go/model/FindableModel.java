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

package org.ontoware.rdf2go.model;

import java.io.IOException;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.node.NodeOrVariable;
import org.ontoware.rdf2go.model.node.PlainLiteral;
import org.ontoware.rdf2go.model.node.ResourceOrVariable;
import org.ontoware.rdf2go.model.node.UriOrVariable;
import org.ontoware.rdf2go.model.node.Variable;


/**
 * A model where you can list all statements, find statements and check if a
 * statement is contained.
 * 
 * @author voelkel
 * 
 */
public interface FindableModel {
	
	/**
	 * get all statements in the model with this subject, predicate and object.
	 * Each of those (s,p,o) can be Variable.ANY
	 * 
	 * Iterator must be auto-close, i.e. when last element is fetched, the
	 * implementation must call close().
	 * 
	 * @param subject URI or Object (= blankNode) or Variable
	 * @param predicate URI or Variable
	 * @param object URI or String (=plainLiteral) or BlankNode (=blankNode) or
	 *            TypedLiteral or LanguageTagLiteral or Variable
	 * @return a statement iterator
	 * @throws ModelRuntimeException most often caused by underlying
	 *             {@link IOException}, if any
	 */
	public ClosableIterator<Statement> findStatements(ResourceOrVariable subject,
	        UriOrVariable predicate, NodeOrVariable object) throws ModelRuntimeException;
	
	/**
	 * Iterator must be auto-close, i.e. when last element is fetched, the
	 * implementation must call close().
	 * 
	 * @param pattern to be matched
	 * @return statement iterator returning all triples matching the given
	 *         pattern
	 * @throws ModelRuntimeException most often caused by underlying
	 *             {@link IOException}, if any
	 */
	public ClosableIterator<Statement> findStatements(TriplePattern pattern)
	        throws ModelRuntimeException;
	
	// ////////////////////////////
	// counting
	
	/**
	 * @return the number of statements in the model matching the query
	 */
	public long countStatements(TriplePattern pattern) throws ModelRuntimeException;
	
	/**
	 * @param subject defined or a {@link Variable}, e.g. {@link Variable#ANY}
	 * @param predicate defined or a {@link Variable}, e.g. {@link Variable#ANY}
	 * @param object defined or a {@link Variable}, e.g. {@link Variable#ANY}
	 * @return true if the statement (subject, predicate, object) is in the
	 *         model
	 * @throws ModelRuntimeException most often caused by underlying
	 *             {@link IOException}, if any
	 */
	public boolean contains(ResourceOrVariable subject, UriOrVariable predicate,
	        NodeOrVariable object) throws ModelRuntimeException;
	
	/**
	 * Convenience function.
	 * 
	 * @param subject defined or a {@link Variable}, e.g. {@link Variable#ANY}
	 * @param predicate defined or a {@link Variable}, e.g. {@link Variable#ANY}
	 * @param plainLiteral a string interpreted as adefined {@link PlainLiteral}
	 * @return true if model contains statement (s,p,'o')
	 * @throws ModelRuntimeException most often caused by underlying
	 *             {@link IOException}, if any
	 */
	public boolean contains(ResourceOrVariable subject, UriOrVariable predicate, String plainLiteral)
	        throws ModelRuntimeException;
	
	/**
	 * @param s a Statement
	 * @return true if the model contains a statement s
	 * @throws ModelRuntimeException most often caused by underlying
	 *             {@link IOException}, if any
	 */
	public boolean contains(Statement s) throws ModelRuntimeException;
	
	/**
	 * @param subject defined or a {@link Variable}, e.g. {@link Variable#ANY}
	 * @param predicate defined or a {@link Variable}, e.g. {@link Variable#ANY}
	 * @param object defined or a {@link Variable}, e.g. {@link Variable#ANY}
	 * @return a triple pattern
	 */
	public TriplePattern createTriplePattern(ResourceOrVariable subject, UriOrVariable predicate,
	        NodeOrVariable object);
	
}
