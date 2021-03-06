// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt

package org.joval.oval;

/**
 * An exception class for OVAL parsing.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class OvalException extends Exception {
    public OvalException(String message) {
	super(message);
    }

    public OvalException(Exception e) {
	super(e);
    }
}
