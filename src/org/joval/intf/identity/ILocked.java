// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt

package org.joval.intf.identity;

/**
 * Something that requires a credential for access.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface ILocked {
    public boolean unlock(ICredential cred);
}
