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
import oval.schemas.definitions.windows.SidSidObject;
import oval.schemas.definitions.windows.SidSidBehaviors;
import oval.schemas.systemcharacteristics.core.ItemType;
import oval.schemas.systemcharacteristics.core.EntityItemStringType;
import oval.schemas.systemcharacteristics.core.StatusEnumeration;
import oval.schemas.systemcharacteristics.windows.SidSidItem;

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
public class SidSidAdapter extends UserAdapter {
    public SidSidAdapter(LocalDirectory local, ActiveDirectory ad, IWmiProvider wmi) {
	super(local, ad, wmi);
    }

    /**
     * @override
     */
    public Class getObjectClass() {
	return SidSidObject.class;
    }

    /**
     * @override
     */
    public Collection<JAXBElement<? extends ItemType>> getItems(IRequestContext rc) throws OvalException {
	Collection<JAXBElement<? extends ItemType>> items = new Vector<JAXBElement<? extends ItemType>>();
	SidSidObject sObj = (SidSidObject)rc.getObject();
	OperationEnumeration op = sObj.getTrusteeSid().getOperation();
	String sid = (String)sObj.getTrusteeSid().getValue();
	SidSidBehaviors behaviors = sObj.getBehaviors();

	try {
	    switch(op) {
	      case EQUALS:
		try {
		    items.addAll(makeItems(local.queryPrincipalBySid(sid), behaviors));
		} catch (NoSuchElementException e) {
		    items.addAll(makeItems(ad.queryPrincipalBySid(sid), behaviors));
		}
		break;

	      case NOT_EQUAL:
		for (Principal p : local.queryAllPrincipals()) {
		    if (!p.getSid().equals(sid)) {
			items.addAll(makeItems(p, behaviors));
		    }
		}
		break;
    
	      case PATTERN_MATCH:
		try {
		    Pattern p = Pattern.compile(sid);
		    for (Principal principal : local.queryAllPrincipals()) {
			if (p.matcher(principal.getSid()).find()) {
			    items.addAll(makeItems(principal, behaviors));
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

    private List<JAXBElement<? extends ItemType>> makeItems(Principal principal, SidSidBehaviors behaviors) {
	Hashtable<String, Principal> principals = new Hashtable<String, Principal>();
	boolean includeGroups = true;
	boolean resolveGroups = false;
	if (behaviors != null) {
	    includeGroups = behaviors.isIncludeGroup();
	    resolveGroups = behaviors.isResolveGroup();
	}
	getAllPrincipals(principal, resolveGroups, principals);
	List<JAXBElement<? extends ItemType>> items = new Vector<JAXBElement<? extends ItemType>>();
	for (Principal p : principals.values()) {
	    switch(p.getType()) {
	      case GROUP:
		if (includeGroups) {
		    items.add(makeItem(p));
		}
		break;

	      case USER:
		items.add(makeItem(p));
		break;
	    }
	}
	return items;
    }

    /**
     * Recurse members of the principal (if it's a group) and add children if resolveGroups == true.  Won't get stuck in
     * a loop because it adds the groups themselves to the Hashtable as it goes.
     */
    private void getAllPrincipals(Principal principal, boolean resolveGroups, Hashtable<String, Principal> principals) {
	switch(principal.getType()) {
	  case GROUP:
	    if (resolveGroups && !principals.containsKey(principal.getSid())) {
		Group g = (Group)principal;
		for (String netbiosName : g.getMemberUserNetbiosNames()) {
		    try {
			if (local.isMember(netbiosName)) {
			    getAllPrincipals(local.queryUser(netbiosName), resolveGroups, principals);
		        } else if (ad.isMember(netbiosName)) {
			    getAllPrincipals(ad.queryUser(netbiosName), resolveGroups, principals);
		        }
		    } catch (Exception e) {
			JOVALSystem.getLogger().warn(JOVALMsg.ERROR_EXCEPTION, e);
		    }
		}
		for (String netbiosName : g.getMemberGroupNetbiosNames()) {
		    try {
			if (local.isMember(netbiosName)) {
			    getAllPrincipals(local.queryUser(netbiosName), resolveGroups, principals);
		        } else if (ad.isMember(netbiosName)) {
			    getAllPrincipals(ad.queryUser(netbiosName), resolveGroups, principals);
		        }
		    } catch (Exception e) {
			JOVALSystem.getLogger().warn(JOVALMsg.ERROR_EXCEPTION, e);
		    }
		}
	    }
	    break;
	}
	principals.put(principal.getSid(), principal);
    }

    private JAXBElement<? extends ItemType> makeItem(Principal principal) {
	SidSidItem item = JOVALSystem.factories.sc.windows.createSidSidItem();
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

	return JOVALSystem.factories.sc.windows.createSidSidItem(item);
    }
}
