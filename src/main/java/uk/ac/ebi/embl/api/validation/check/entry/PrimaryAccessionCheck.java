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
package uk.ac.ebi.embl.api.validation.check.entry;

import uk.ac.ebi.embl.api.AccessionMatcher;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

import java.sql.SQLException;

@Description("invalid accession \"{0}\" for dataclass \"{1}\""
		+ "invalid accession prefix : \"{0}\". Accession prefix is not registered in cv/prefix tables")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI_MASTER})
public class PrimaryAccessionCheck extends EntryValidationCheck {

	private final static String INVALID_ACCESSION_FORMAT_ID = "PrimaryAccessionCheck1";
	private final static String INVALID_ACCESSION_PREFIX_ID = "PrimaryAccessionCheck2";

	public PrimaryAccessionCheck() {

	}
	
		public ValidationResult check(Entry entry) throws ValidationEngineException {
	
		
		if(entry==null)
			return result;	
		if (null == entry.getPrimaryAccession()|| entry.getPrimaryAccession().isEmpty())
		{
			return result;
		}

		String accessionPrefix = AccessionMatcher.getAccessionPrefix(entry.getPrimaryAccession(), entry.getDataClass());

		if (accessionPrefix == null) {
			reportError(entry.getOrigin(), INVALID_ACCESSION_FORMAT_ID, entry.getPrimaryAccession(), entry.getDataClass());
		} else if (getEntryDAOUtils() != null) {
			//check accession prefix is registered in cv_database_prefix table
			try {
				if (getEntryDAOUtils().getDbcode(accessionPrefix) == null) {
					reportError(entry.getOrigin(), INVALID_ACCESSION_PREFIX_ID, accessionPrefix);
				}
			} catch (SQLException e) {
				throw new ValidationEngineException(e.getMessage(), e);
			}
		}
		return result;
	}

}
