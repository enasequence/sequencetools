/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.validation;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

public class ValidationMessageManager {

    public static final String STANDARD_VALIDATION_BUNDLE = "uk.ac.ebi.embl.api.validation.ValidationMessages";
    public static final String STANDARD_FIXER_BUNDLE = "uk.ac.ebi.embl.api.validation.FixerMessages";

    public static final String GFF3_VALIDATION_BUNDLE = "uk.ac.ebi.embl.api.validation.GFF3ValidationMessages";
    public static final String GENOMEASSEMBLY_VALIDATION_BUNDLE = "uk.ac.ebi.embl.api.validation.GenomeAssemblyValidationMessages";
    public static final String GENOMEASSEMBLY_FIXER_BUNDLE = "uk.ac.ebi.embl.api.validation.GenomeAssemblyFixerMessages";

	private static final String NO_MESSAGE = "Missing message: ";

	private final static List<ResourceBundle> bundles = new Vector<ResourceBundle>();
	private final static Set<String> bundleNames = Collections.synchronizedSet(new HashSet<String>());

    public static void addBundle(String bundleName) {
//        System.out.println("bundleName = " + bundleName);
		if (!bundleNames.contains(bundleName)) {
			bundles.add(ResourceBundle.getBundle(bundleName));
            bundleNames.add(bundleName);
        }
	}

	/**
	 * Applies the message parameters and returns the message 
	 * from one of the message bundles.
	 * 
	 * @param key property key
	 * @param params message parameters to be used in message's place holders 
	 * @return Resource value or place-holder error String
	 */
	public static String getString(String key, Object... params) {
		String message = getStringSafely(key);
		if (params != null && params.length > 0) {
			return MessageFormat.format(message, params);
		} else {
			return message;
		}
	}

	/** Returns the message from one of the message bundles. */
	private static String getStringSafely(String key) {
		String resource = null;
		for (ResourceBundle bundle: bundles) {
			try {
				resource = bundle.getString(key);
			} catch (MissingResourceException mrex) {
				continue;
			}
			return resource;
		}
		return NO_MESSAGE + key;
	}	
}
