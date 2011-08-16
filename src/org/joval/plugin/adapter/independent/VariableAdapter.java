// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt

package org.joval.plugin.adapter.independent;

import java.util.List;
import java.util.Vector;
import javax.xml.bind.JAXBElement;

import oval.schemas.definitions.independent.VariableObject;
import oval.schemas.systemcharacteristics.core.EntityItemAnySimpleType;
import oval.schemas.systemcharacteristics.core.FlagEnumeration;
import oval.schemas.systemcharacteristics.core.ItemType;
import oval.schemas.systemcharacteristics.independent.EntityItemVariableRefType;
import oval.schemas.systemcharacteristics.independent.VariableItem;
import oval.schemas.results.core.ResultEnumeration;

import org.joval.intf.plugin.IAdapter;
import org.joval.intf.plugin.IRequestContext;
import org.joval.intf.plugin.IPlugin;
import org.joval.oval.OvalException;
import org.joval.oval.util.CheckData;
import org.joval.util.JOVALSystem;

/**
 * Evaluates VariableTest OVAL tests.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class VariableAdapter implements IAdapter {
    private IPlugin plugin;

    public VariableAdapter() {
    }

    // Implement IAdapter

    public Class getObjectClass() {
	return VariableObject.class;
    }

    public boolean connect() {
	return true;
    }

    public void disconnect() {
    }

    public List<JAXBElement<? extends ItemType>> getItems(IRequestContext rc) throws OvalException {
	VariableObject vObj = (VariableObject)rc.getObject();
	List<JAXBElement<? extends ItemType>> items = new Vector<JAXBElement<? extends ItemType>>();

	List<String> values = rc.resolve((String)vObj.getVarRef().getValue());
	if (values.size() > 0) {
	    VariableItem item = JOVALSystem.factories.sc.independent.createVariableItem();
	    EntityItemVariableRefType ref = JOVALSystem.factories.sc.independent.createEntityItemVariableRefType();
	    ref.setValue(vObj.getVarRef().getValue());
	    item.setVarRef(ref);
	    for (String value : values) {
		EntityItemAnySimpleType valueType = JOVALSystem.factories.sc.core.createEntityItemAnySimpleType();
		valueType.setValue(value);
		item.getValue().add(valueType);
	    }
	    items.add(JOVALSystem.factories.sc.independent.createVariableItem(item));
	}
	return items;
    }
}
