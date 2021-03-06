// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt

package org.joval.plugin.adapter.windows;

import java.util.Collection;
import java.util.Hashtable;
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
import oval.schemas.definitions.windows.GroupObject;
import oval.schemas.systemcharacteristics.core.ItemType;
import oval.schemas.systemcharacteristics.core.EntityItemBoolType;
import oval.schemas.systemcharacteristics.core.EntityItemStringType;
import oval.schemas.systemcharacteristics.core.StatusEnumeration;
import oval.schemas.systemcharacteristics.windows.GroupItem;
import oval.schemas.results.core.ResultEnumeration;

import org.joval.intf.plugin.IAdapter;
import org.joval.intf.plugin.IRequestContext;
import org.joval.intf.windows.wmi.IWmiProvider;
import org.joval.os.windows.identity.ActiveDirectory;
import org.joval.os.windows.identity.Group;
import org.joval.os.windows.identity.LocalDirectory;
import org.joval.os.windows.wmi.WmiException;
import org.joval.oval.OvalException;
import org.joval.util.JOVALMsg;
import org.joval.util.JOVALSystem;

/**
 * Evaluates Group OVAL tests.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class GroupAdapter extends UserAdapter {
    public GroupAdapter(LocalDirectory local, ActiveDirectory ad, IWmiProvider wmi) {
	super(local, ad, wmi);
    }

    // Implement IAdapter

    /**
     * @override
     */
    public Class getObjectClass() {
	return GroupObject.class;
    }

    /**
     * @override
     */
    public Collection<JAXBElement<? extends ItemType>> getItems(IRequestContext rc) throws OvalException {
	Collection<JAXBElement<? extends ItemType>> items = new Vector<JAXBElement<? extends ItemType>>();
	OperationEnumeration op = ((GroupObject)rc.getObject()).getGroup().getOperation();
	String group = (String)((GroupObject)rc.getObject()).getGroup().getValue();

	try {
	    switch(op) {
	      case EQUALS:
		if (local.isMember(group)) {
		    items.add(makeItem(local.queryGroup(group)));
		} else if (ad.isMember(group)) {
		    items.add(makeItem(ad.queryGroup(group)));
		} else {
		    MessageType msg = JOVALSystem.factories.common.createMessageType();
		    msg.setLevel(MessageLevelEnumeration.WARNING);
		    String s = JOVALSystem.getMessage(JOVALMsg.ERROR_AD_DOMAIN_UNKNOWN, group);
		    JOVALSystem.getLogger().warn(s);
		    msg.setValue(s);
		    rc.addMessage(msg);
		}
		break;
    
	      case NOT_EQUAL:
		for (Group g : local.queryAllGroups()) {
		    if (!local.getQualifiedNetbiosName(group).equals(g.getNetbiosName())) {
			items.add(makeItem(g));
		    }
		}
		break;
    
	      case PATTERN_MATCH:
		try {
		    Pattern p = Pattern.compile(group);
		    for (Group g : local.queryAllGroups()) {
			Matcher m = null;
			if (local.isMember(g.getNetbiosName())) {
			    m = p.matcher(g.getName());
			} else {
			    m = p.matcher(g.getNetbiosName());
			}
			if (m.find()) {
			    items.add(makeItem(g));
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

    private JAXBElement<? extends ItemType> makeItem(Group group) {
	GroupItem item = JOVALSystem.factories.sc.windows.createGroupItem();
	EntityItemStringType groupType = JOVALSystem.factories.sc.core.createEntityItemStringType();
	if (local.isBuiltinGroup(group.getNetbiosName())) {
	    groupType.setValue(group.getName());
	} else {
	    groupType.setValue(group.getNetbiosName());
	}
	item.setGroup(groupType);
	Collection<String> userNetbiosNames = group.getMemberUserNetbiosNames();
	if (userNetbiosNames.size() == 0) {
	    EntityItemStringType userType = JOVALSystem.factories.sc.core.createEntityItemStringType();
	    userType.setStatus(StatusEnumeration.DOES_NOT_EXIST);
	    item.getUser().add(userType);
	} else {
	    for (String userNetbiosName : userNetbiosNames) {
		EntityItemStringType userType = JOVALSystem.factories.sc.core.createEntityItemStringType();
		if (local.isBuiltinUser(userNetbiosName)) {
		    userType.setValue(local.getName(userNetbiosName));
		} else {
		    userType.setValue(userNetbiosName);
		}
		item.getUser().add(userType);
	    }
	}
	Collection<String> groupNetbiosNames = group.getMemberGroupNetbiosNames();
	if (groupNetbiosNames.size() == 0) {
	    EntityItemStringType subgroupType = JOVALSystem.factories.sc.core.createEntityItemStringType();
	    subgroupType.setStatus(StatusEnumeration.DOES_NOT_EXIST);
	    item.getSubgroup().add(subgroupType);
	} else {
	    for (String groupNetbiosName : groupNetbiosNames) {
		EntityItemStringType subgroupType = JOVALSystem.factories.sc.core.createEntityItemStringType();
		if (local.isBuiltinGroup(groupNetbiosName)) {
		    subgroupType.setValue(local.getName(groupNetbiosName));
		} else {
		    subgroupType.setValue(groupNetbiosName);
		}
		item.getSubgroup().add(subgroupType);
	    }
	}
	return JOVALSystem.factories.sc.windows.createGroupItem(item);
    }
}
