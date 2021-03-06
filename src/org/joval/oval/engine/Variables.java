// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt

package org.joval.oval.engine;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.w3c.dom.Node;

import oval.schemas.variables.core.OvalVariables;
import oval.schemas.variables.core.VariablesType;
import oval.schemas.variables.core.VariableType;

import org.joval.oval.OvalException;
import org.joval.util.JOVALMsg;
import org.joval.util.JOVALSystem;

/**
 * Index to an OvalVariables object, for fast look-up of variable definitions.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
class Variables {
    /**
     * Unmarshal an XML file and return the OvalVariables root object.
     */
    static final OvalVariables getOvalVariables(File f) throws OvalException {
	try {
	    JAXBContext ctx = JAXBContext.newInstance(JOVALSystem.getOvalProperty(JOVALSystem.OVAL_PROP_VARIABLES));
	    Unmarshaller unmarshaller = ctx.createUnmarshaller();
	    Object rootObj = unmarshaller.unmarshal(f);
	    if (rootObj instanceof OvalVariables) {
		return (OvalVariables)rootObj;
	    } else {
		throw new OvalException(JOVALSystem.getMessage(JOVALMsg.ERROR_VARIABLES_BAD_FILE, f.toString()));
	    }
	} catch (JAXBException e) {
	    throw new OvalException(e);
	}
    }

    private OvalVariables vars;
    private Hashtable <String, VariableType>variables;

    Variables(OvalVariables vars) {
	this.vars = vars;

	variables = new Hashtable <String, VariableType>();
	List <VariableType> varList = vars.getVariables().getVariable();
	int len = varList.size();
	for (int i=0; i < len; i++) {
	    VariableType vt = varList.get(i);
	    variables.put(vt.getId(), vt);
	}
    }

    /**
     * For whatever reason, JAXB failed to generate an appropriate container type, so we do this DOM hack.
     */
    List<String> getValue(String id) throws OvalException {
	VariableType var = variables.get(id);
	if (var == null) {
	    throw new OvalException(JOVALSystem.getMessage(JOVALMsg.ERROR_REF_VARIABLE, id));
	}
	List<String> list = new Vector<String>();
	for (Object obj : var.getValue()) {
	    if (obj instanceof Node) {
		list.add(((Node)obj).getTextContent());
	    } else {
		throw new OvalException(JOVALSystem.getMessage(JOVALMsg.ERROR_UNEXPECTED_NODE, obj.getClass().getName()));
	    }
	}
	return list;
    }
}
