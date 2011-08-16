// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt

package org.joval.plugin.adapter.independent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.xml.bind.JAXBElement;

import oval.schemas.definitions.independent.TextfilecontentObject;
import oval.schemas.systemcharacteristics.core.EntityItemAnySimpleType;
import oval.schemas.systemcharacteristics.core.EntityItemStringType;
import oval.schemas.systemcharacteristics.core.ItemType;
import oval.schemas.systemcharacteristics.core.StatusEnumeration;
import oval.schemas.systemcharacteristics.independent.TextfilecontentItem;
import oval.schemas.results.core.ResultEnumeration;

import org.joval.intf.io.IFile;
import org.joval.intf.io.IFilesystem;
import org.joval.intf.plugin.IAdapter;
import org.joval.intf.plugin.IRequestContext;
import org.joval.oval.OvalException;
import org.joval.util.BaseFileAdapter;
import org.joval.util.JOVALSystem;

/**
 * Evaluates TextfilecontentTest OVAL tests.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class TextfilecontentAdapter extends BaseFileAdapter {
    public TextfilecontentAdapter(IFilesystem fs) {
	super(fs);
    }

    // Implement IAdapter

    public Class getObjectClass() {
	return TextfilecontentObject.class;
    }

    // Protected

    protected Object convertFilename(EntityItemStringType filename) {
	return filename;
    }

    protected ItemType createFileItem() {
	return JOVALSystem.factories.sc.independent.createTextfilecontentItem();
    }

    /**
     * Parse the file as specified by the Object, and decorate the Item.
     */
    protected List<JAXBElement<? extends ItemType>> getItems(ItemType base, IFile f, IRequestContext rc)
		throws IOException, OvalException {

	List<JAXBElement<? extends ItemType>> items = new Vector<JAXBElement<? extends ItemType>>();

	TextfilecontentItem baseItem = null;
	if (base instanceof TextfilecontentItem) {
	    baseItem = (TextfilecontentItem)base;
	}
	TextfilecontentObject tfcObj = null;
	if (rc.getObject() instanceof TextfilecontentObject) {
	    tfcObj = (TextfilecontentObject)rc.getObject();
	}

	if (baseItem != null && tfcObj != null) {
	    InputStream in = null;
	    try {
		Pattern p = Pattern.compile((String)tfcObj.getLine().getValue());
    
		//
		// Read the file line-by-line and search for the pattern.  Add matching lines as subexpression elements.
		//
		in = f.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = br.readLine()) != null) {
		    Matcher m = p.matcher(line);
		    if (m.find()) {
			TextfilecontentItem item = JOVALSystem.factories.sc.independent.createTextfilecontentItem();
			item.setPath(baseItem.getPath());
			item.setFilename(baseItem.getFilename());
			EntityItemStringType lineType = JOVALSystem.factories.sc.core.createEntityItemStringType();
			lineType.setValue(line);
			item.setLine(lineType);
			items.add(JOVALSystem.factories.sc.independent.createTextfilecontentItem(item));
		    }
		}
	    } catch (PatternSyntaxException e) {
		JOVALSystem.getLogger().log(Level.WARNING, JOVALSystem.getMessage("ERROR_PATTERN", e.getMessage()), e);
		throw new IOException(e);
	    } finally {
		if (in != null) {
		    try {
			in.close();
		    } catch (IOException e) {
			JOVALSystem.getLogger().log(Level.WARNING, JOVALSystem.getMessage("ERROR_FILE_STREAM_CLOSE", f.toString()), e);
		    }
		}
	    }
	}
	return items;
    }
}
