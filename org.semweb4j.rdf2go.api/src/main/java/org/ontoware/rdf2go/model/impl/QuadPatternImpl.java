/**
 * LICENSE INFORMATION
 *
 * Copyright 2005-2008 by FZI (http://www.fzi.de).
 * Licensed under a BSD license (http://www.opensource.org/licenses/bsd-license.php)
 * <OWNER> = Max Völkel
 * <ORGANIZATION> = FZI Forschungszentrum Informatik Karlsruhe, Karlsruhe, Germany
 * <YEAR> = 2010
 *
 * Further project information at http://semanticweb.org/wiki/RDF2Go
 */

package org.ontoware.rdf2go.model.impl;

import org.ontoware.rdf2go.model.QuadPattern;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.NodeOrVariable;
import org.ontoware.rdf2go.model.node.ResourceOrVariable;
import org.ontoware.rdf2go.model.node.UriOrVariable;
import org.ontoware.rdf2go.model.node.Variable;

/**
 * A statement with variables
 * @author voelkel
 *
 */
public class QuadPatternImpl extends TriplePatternImpl implements QuadPattern {

	/**
     * 
     */
    private static final long serialVersionUID = -4492270855601646699L;
	private UriOrVariable context;

	public QuadPatternImpl(UriOrVariable context, ResourceOrVariable subject, UriOrVariable predicate, NodeOrVariable object) {
		super(subject,predicate,object);
		this.context = context;
	}

	@Override
    public UriOrVariable getContext() {
		return this.context;
	}
	
	@Override
	public boolean equals(Object o) {
		return ((o instanceof Statement)
				&& (this.getContext().equals( ((Statement) o ).getContext()))
				&& (this.getSubject().equals(((Statement) o).getSubject()))
				&& (this.getPredicate().equals(((Statement) o).getPredicate())) && (this
				.getObject().equals(((Statement) o).getObject())));
	}
	
	@Override
	public int hashCode() {
		return this.context.hashCode() + super.hashCode();
	}
	
	@Override
	public boolean matches(Statement statement)
	{
		boolean matchesContext = statement.getContext().equals(this.getContext())
			|| this.getContext() instanceof Variable;
		return matchesContext && super.matches(statement);
	}

}
