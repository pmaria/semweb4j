/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.eclipse.rdf4j.rdf2go;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Statement;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;

import org.eclipse.rdf4j.RDF4JException;

/**
 * Iterable over RDF4J Statements that converts them to R2Go Statements on
 * demand.
 */
public class StatementIterable implements ClosableIterable<Statement> {

	/**
     * 
     */
    private static final long serialVersionUID = -8172286299886107501L;

	private final CloseableIteration<? extends org.eclipse.rdf4j.model.Statement, ? extends RDF4JException> cit;

	private RepositoryModel model;

	public StatementIterable(
			CloseableIteration<? extends org.eclipse.rdf4j.model.Statement, ? extends RDF4JException> cit,
			RepositoryModel model)
	{
		this.cit = cit;
		this.model = model;
	}

	public ClosableIterator<Statement> iterator() {
		return new StatementIterator(this.cit, this.model);
	}
}
