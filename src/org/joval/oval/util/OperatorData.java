// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt

package org.joval.oval.util;

import oval.schemas.common.OperatorEnumeration;
import oval.schemas.results.core.ResultEnumeration;

import org.joval.oval.OvalException;
import org.joval.util.JOVALSystem;

/**
 * @see http://oval.mitre.org/language/version5.9/ovaldefinition/documentation/oval-common-schema.html#OperatorEnumeration
 */
public class OperatorData {
    int t, f, e, u, ne, na;

    public OperatorData() {
	t = 0;
	f = 0;
	e = 0;
	u = 0;
	ne = 0;
	na = 0;
    }

    public void addResult(ResultEnumeration result) {
	switch(result) {
	  case TRUE:
	    t++;
	    break;
	  case FALSE:
	    f++;
	    break;
	  case UNKNOWN:
	    u++;
	    break;
	  case NOT_APPLICABLE:
	    na++;
	    break;
	  case NOT_EVALUATED:
	    ne++;
	    break;
	  case ERROR:
	    e++;
	    break;
	}
    }

    public ResultEnumeration getResult(OperatorEnumeration op) throws OvalException {
	ResultEnumeration result = ResultEnumeration.UNKNOWN;
	switch(op) {
	  case AND:
	    if        (t > 0	&& f == 0	&& e == 0	&& u == 0	&& ne == 0	&& na >= 0) {
		return ResultEnumeration.TRUE;
	    } else if (t >= 0	&& f > 0	&& e >= 0	&& u >= 0	&& ne >= 0	&& na >= 0) {
		return ResultEnumeration.FALSE;
	    } else if (t >= 0	&& f == 0	&& e > 0	&& u >= 0	&& ne >= 0	&& na >= 0) {
		return ResultEnumeration.ERROR;
	    } else if (t >= 0	&& f == 0	&& e == 0	&& u > 0	&& ne >= 0	&& na >= 0) {
		return ResultEnumeration.UNKNOWN;
	    } else if (t >= 0	&& f == 0	&& e == 0	&& u == 0	&& ne > 0	&& na >= 0) {
		return ResultEnumeration.NOT_EVALUATED;
	    } else if (t == 0	&& f == 0	&& e == 0	&& u == 0	&& ne == 0	&& na > 0) {
		return ResultEnumeration.NOT_APPLICABLE;
	    }
	    break;

	  case ONE:
	    if        (t == 1	&& f >= 0	&& e == 0	&& u == 0	&& ne == 0	&& na >= 0) {
		return ResultEnumeration.TRUE;
	    } else if (t > 1	&& f >= 0	&& e >= 0	&& u >= 0	&& ne >= 0	&& na >= 0) {
		return ResultEnumeration.FALSE;
	    } else if (t == 0	&& f > 0	&& e == 0	&& u == 0	&& ne == 0	&& na >= 0) {
		return ResultEnumeration.FALSE;
	    } else if (t < 2	&& f >= 0	&& e > 0	&& u >= 0	&& ne >= 0	&& na >= 0) {
		return ResultEnumeration.ERROR;
	    } else if (t < 2	&& f >= 0	&& e == 0	&& u > 0	&& ne >= 0	&& na >= 0) {
		return ResultEnumeration.UNKNOWN;
	    } else if (t < 2	&& f >= 0	&& e == 0	&& u == 0	&& ne > 0	&& na >= 0) {
		return ResultEnumeration.NOT_EVALUATED;
	    } else if (t == 0	&& f == 0	&& e == 0	&& u == 0	&& ne == 0	&& na > 0) {
		return ResultEnumeration.NOT_APPLICABLE;
	    }
	    break;

	  case OR:
	    if        (t > 0	&& f >= 0	&& e >= 0	&& u >= 0	&& ne >= 0	&& na >= 0) {
		return ResultEnumeration.TRUE;
	    } else if (t == 0	&& f > 0	&& e == 0	&& u == 0	&& ne == 0	&& na >= 0) {
		return ResultEnumeration.FALSE;
	    } else if (t == 0	&& f >= 0	&& e > 0	&& u >= 0	&& ne >= 0	&& na >= 0) {
		return ResultEnumeration.ERROR;
	    } else if (t == 0	&& f >= 0	&& e == 0	&& u > 0	&& ne >= 0	&& na >= 0) {
		return ResultEnumeration.UNKNOWN;
	    } else if (t == 0	&& f >= 0	&& e == 0	&& u == 0	&& ne > 0	&& na >= 0) {
		return ResultEnumeration.NOT_EVALUATED;
	    } else if (t == 0	&& f == 0	&& e == 0	&& u == 0	&& ne == 0	&& na == 0) {
		return ResultEnumeration.NOT_APPLICABLE;
	    }
	    break;

	  case XOR:
	    if        (t%2 != 0	&& f >= 0	&& e == 0	&& u == 0	&& ne == 0	&& na >= 0) {
		return ResultEnumeration.TRUE;
	    } else if (t%2 == 0	&& f >= 0	&& e == 0	&& u == 0	&& ne == 0	&& na >= 0) {
		return ResultEnumeration.FALSE;
	    } else if (t >= 0	&& f >= 0	&& e > 0	&& u >= 0	&& ne >= 0	&& na >= 0) {
		return ResultEnumeration.ERROR;
	    } else if (t >= 0	&& f >= 0	&& e == 0	&& u > 0	&& ne >= 0	&& na >= 0) {
		return ResultEnumeration.UNKNOWN;
	    } else if (t >= 0	&& f >= 0	&& e == 0	&& u == 0	&& ne > 0	&& na >= 0) {
		return ResultEnumeration.NOT_EVALUATED;
	    } else if (t == 0	&& f == 0	&& e == 0	&& u == 0	&& ne == 0	&& na == 0) {
		return ResultEnumeration.NOT_APPLICABLE;
	    }
	    break;

	  default:
	    throw new OvalException(JOVALSystem.getMessage("ERROR_UNSUPPORTED_OPERATION", op));
	}
	return result;
    }
}

