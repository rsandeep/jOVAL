// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt

package org.joval.os.windows.registry;

import org.joval.intf.windows.registry.IKey;
import org.joval.intf.windows.registry.IStringValue;
import org.joval.util.JOVALMsg;
import org.joval.util.JOVALSystem;

/**
 * Representation of a Windows registry string value.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class StringValue extends Value implements IStringValue {
    private String data;

    public StringValue(IKey parent, String name, String data) {
	type = REG_SZ;
	this.parent = parent;
	this.name = name;
	this.data = data;
	JOVALSystem.getLogger().trace(JOVALMsg.STATUS_WINREG_VALINSTANCE, toString());
    }

    public String getData() {
	return data;
    }

    public String toString() {
	return "StringValue [Name=\"" + name + "\" Value=\"" + data + "\"]";
    }
}
