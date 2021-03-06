// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt

package org.joval.plugin.adapter.windows;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.xml.bind.JAXBElement;

import oval.schemas.common.MessageType;
import oval.schemas.common.MessageLevelEnumeration;
import oval.schemas.common.OperationEnumeration;
import oval.schemas.common.SimpleDatatypeEnumeration;
import oval.schemas.definitions.core.ObjectType;
import oval.schemas.definitions.windows.SidObject;
import oval.schemas.systemcharacteristics.core.ItemType;
import oval.schemas.systemcharacteristics.core.EntityItemStringType;
import oval.schemas.systemcharacteristics.core.StatusEnumeration;
import oval.schemas.systemcharacteristics.windows.SidItem;

import org.joval.intf.plugin.IRequestContext;
import org.joval.intf.windows.wmi.IWmiProvider;
import org.joval.os.windows.identity.ActiveDirectory;
import org.joval.os.windows.identity.LocalDirectory;
import org.joval.os.windows.identity.Group;
import org.joval.os.windows.identity.Principal;
import org.joval.os.windows.wmi.WmiException;
import org.joval.oval.OvalException;
import org.joval.util.JOVALMsg;
import org.joval.util.JOVALSystem;

/**
 * Collects items for the sid_sid_object.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class SidAdapter extends UserAdapter {
    public SidAdapter(LocalDirectory local, ActiveDirectory ad, IWmiProvider wmi) {
	super(local, ad, wmi);
    }

    /**
     * @override
     */
    public Class getObjectClass() {
	return SidObject.class;
    }

    /**
     * @override
     */
    public Collection<JAXBElement<? extends ItemType>> getItems(IRequestContext rc) throws OvalException {
	Collection<JAXBElement<? extends ItemType>> items = new Vector<JAXBElement<? extends ItemType>>();
	SidObject sObj = (SidObject)rc.getObject();
	OperationEnumeration op = sObj.getTrusteeName().getOperation();

	try {
	    switch(op) {
	      case EQUALS: {
		String netbiosName = local.getQualifiedNetbiosName((String)sObj.getTrusteeName().getValue());
		try {
		    items.add(makeItem(local.queryPrincipal(netbiosName)));
		} catch (NoSuchElementException e) {
		    items.add(makeItem(ad.queryPrincipal(netbiosName)));
		}
		break;
	      }

	      case NOT_EQUAL: {
		String netbiosName = local.getQualifiedNetbiosName((String)sObj.getTrusteeName().getValue());
		for (Principal p : local.queryAllPrincipals()) {
		    if (!p.getNetbiosName().equals(netbiosName)) {
			items.add(makeItem(p));
		    }
		}
		break;
	      }

	      case PATTERN_MATCH:
		try {
		    Pattern p = Pattern.compile((String)sObj.getTrusteeName().getValue());
		    for (Principal principal : local.queryAllPrincipals()) {
			if (p.matcher(principal.getNetbiosName()).find()) {
			    items.add(makeItem(principal));
			}
		    }
		} catch (PatternSyntaxException e) {
		    MessageType msg = JOVALSystem.factories.common.createMessageType();
		    msg.setLevel(MessageLevelEnumeration.ERROR);
		    msg.setValue(JOVALSystem.getMessage(JOVALMsg.ERROR_PATTERN, e.getMessage()));
		    rc.addMessage(msg);
		    JOVALSystem.getLogger().warn(JOVALSystem.getMessage(JOVALMsg.ERROR_EXCEPTION), e);
		}
		break;
    
	      default:
		throw new OvalException(JOVALSystem.getMessage(JOVALMsg.ERROR_UNSUPPORTED_OPERATION, op));
	    }
	} catch (NoSuchElementException e) {
	    // No match.
	} catch (WmiException e) {
	    MessageType msg = JOVALSystem.factories.common.createMessageType();
	    msg.setLevel(MessageLevelEnumeration.ERROR);
	    msg.setValue(JOVALSystem.getMessage(JOVALMsg.ERROR_WINWMI_GENERAL, e.getMessage()));
	    rc.addMessage(msg);
	}
	return items;
    }

    // Private

    private JAXBElement<? extends ItemType> makeItem(Principal principal) {
	SidItem item = JOVALSystem.factories.sc.windows.createSidItem();
	EntityItemStringType trusteeSidType = JOVALSystem.factories.sc.core.createEntityItemStringType();
	trusteeSidType.setValue(principal.getSid());
	trusteeSidType.setDatatype(SimpleDatatypeEnumeration.STRING.value());
	item.setTrusteeSid(trusteeSidType);

	boolean builtin = false;
	switch(principal.getType()) {
	  case USER:
	    if (local.isBuiltinUser(principal.getNetbiosName())) {
		builtin = true;
 	    }
	    break;
	  case GROUP:
	    if (local.isBuiltinGroup(principal.getNetbiosName())) {
		builtin = true;
 	    }
	    break;
	}
	EntityItemStringType trusteeNameType = JOVALSystem.factories.sc.core.createEntityItemStringType();
	if (builtin) {
	    trusteeNameType.setValue(principal.getName());
	} else {
	    trusteeNameType.setValue(principal.getNetbiosName());
	}
	trusteeNameType.setDatatype(SimpleDatatypeEnumeration.STRING.value());
	item.setTrusteeName(trusteeNameType);

	EntityItemStringType trusteeDomainType = JOVALSystem.factories.sc.core.createEntityItemStringType();
	trusteeDomainType.setValue(principal.getDomain());
	trusteeDomainType.setDatatype(SimpleDatatypeEnumeration.STRING.value());
	item.setTrusteeDomain(trusteeDomainType);

	return JOVALSystem.factories.sc.windows.createSidItem(item);
    }
}
