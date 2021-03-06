// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt

package org.joval.plugin.adapter.windows;

import java.util.Hashtable;
import java.util.Collection;
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
import oval.schemas.definitions.windows.UserSid55Object;
import oval.schemas.systemcharacteristics.core.ItemType;
import oval.schemas.systemcharacteristics.core.EntityItemBoolType;
import oval.schemas.systemcharacteristics.core.EntityItemStringType;
import oval.schemas.systemcharacteristics.core.StatusEnumeration;
import oval.schemas.systemcharacteristics.windows.UserSidItem;

import org.joval.intf.plugin.IRequestContext;
import org.joval.intf.windows.wmi.IWmiProvider;
import org.joval.os.windows.identity.ActiveDirectory;
import org.joval.os.windows.identity.LocalDirectory;
import org.joval.os.windows.identity.Group;
import org.joval.os.windows.identity.User;
import org.joval.os.windows.wmi.WmiException;
import org.joval.oval.OvalException;
import org.joval.util.JOVALMsg;
import org.joval.util.JOVALSystem;

/**
 * Collects items for the user_sid55_object.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class UserSid55Adapter extends UserAdapter {
    public UserSid55Adapter(LocalDirectory local, ActiveDirectory ad, IWmiProvider wmi) {
	super(local, ad, wmi);
    }

    /**
     * @override
     */
    public Class getObjectClass() {
	return UserSid55Object.class;
    }

    /**
     * @override
     */
    public Collection<JAXBElement<? extends ItemType>> getItems(IRequestContext rc) throws OvalException {
	Collection<JAXBElement<? extends ItemType>> items = new Vector<JAXBElement<? extends ItemType>>();
	UserSid55Object uObj = (UserSid55Object)rc.getObject();
	String sid = (String)uObj.getUserSid().getValue();
	OperationEnumeration op = uObj.getUserSid().getOperation();

	try {
	    switch(op) {
	      case EQUALS:
		try {
		    items.add(makeItem(local.queryUserBySid(sid)));
		} catch (NoSuchElementException e) {
		    items.add(makeItem(ad.queryUserBySid(sid)));
		}
		break;
    
	      case NOT_EQUAL:
		for (User u : local.queryAllUsers()) {
		    if (!u.getSid().equals(sid)) {
			items.add(makeItem(u));
		    }
		}
		break;
    
	      case PATTERN_MATCH:
		try {
		    Pattern p = Pattern.compile(sid);
		    for (User u : local.queryAllUsers()) {
			if (p.matcher(u.getSid()).find()) {
			    items.add(makeItem(u));
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

    private JAXBElement<? extends ItemType> makeItem(User user) {
	UserSidItem item = JOVALSystem.factories.sc.windows.createUserSidItem();
	EntityItemStringType userSidType = JOVALSystem.factories.sc.core.createEntityItemStringType();
	userSidType.setValue(user.getSid());
	item.setUserSid(userSidType);
	EntityItemBoolType enabledType = JOVALSystem.factories.sc.core.createEntityItemBoolType();
	enabledType.setValue(user.isEnabled() ? "true" : "false");
	enabledType.setDatatype(SimpleDatatypeEnumeration.BOOLEAN.value());
	item.setEnabled(enabledType);
	for (String groupNetbiosName : user.getGroupNetbiosNames()) {
	    Group group = null;
	    try {
		if (local.isMember(groupNetbiosName)) {
		    group = local.queryGroup(groupNetbiosName);
		} else if (ad.isMember(groupNetbiosName)) {
		    group = ad.queryGroup(groupNetbiosName);
		}
		if (group != null) {
		    EntityItemStringType groupSidType = JOVALSystem.factories.sc.core.createEntityItemStringType();
		    groupSidType.setValue(group.getSid());
		    item.getGroupSid().add(groupSidType);
		}
	    } catch (NoSuchElementException e) {
	    } catch (WmiException e) {
	    }
	}
	if (item.getGroupSid().size() == 0) {
	    EntityItemStringType groupSidType = JOVALSystem.factories.sc.core.createEntityItemStringType();
	    groupSidType.setStatus(StatusEnumeration.DOES_NOT_EXIST);
	    item.getGroupSid().add(groupSidType);
	}
	return JOVALSystem.factories.sc.windows.createUserSidItem(item);
    }
}
