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

package org.ontoware.rdf2go.model.node;


/**
 * RDF Literal representation of a datatype (usually xml schema datatype)
 * 
 * Implementations are expected to have valid implementations of equals( Object )
 * and hashCode()
 * 
 * @author mvo
 * 
 */
public interface DatatypeLiteral extends Literal {

	/**
	 * the URI normally is an URI for a xml schema datatype (xsd)
	 * 
	 * @return the URI of the datatype
	 */
	public URI getDatatype();

}