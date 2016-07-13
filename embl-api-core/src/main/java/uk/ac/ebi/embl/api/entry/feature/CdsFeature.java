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
package uk.ac.ebi.embl.api.entry.feature;

import java.io.Serializable;

import uk.ac.ebi.embl.api.entry.qualifier.CodonStartQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.ProteinIdQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.TranslTableQualifier;
import uk.ac.ebi.embl.api.validation.ValidationException;

public class CdsFeature extends Feature implements Serializable {

	private static final long serialVersionUID = 4631877375297918571L;
	
	protected CdsFeature() {
		super(CDS_FEATURE_NAME, true);
	}

    protected CdsFeature(String featureName, boolean join) {
        super(featureName, join);
    }

    public String getProteinAccession() throws ValidationException {
		ProteinIdQualifier qualifier = (ProteinIdQualifier)getSingleQualifier(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME);
		if (qualifier == null) {
			return null;
		}
		return qualifier.getProteinAccession();	
	}

	public Integer getProteinVersion() throws ValidationException {
		ProteinIdQualifier qualifier = (ProteinIdQualifier)getSingleQualifier(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME);
		if (qualifier == null) {
			return null;
		}
		return qualifier.getProteinVersion();	
	}
		
	public Integer getTranslationTable() throws ValidationException {
		TranslTableQualifier qualifier = (TranslTableQualifier)getSingleQualifier(
				Qualifier.TRANSL_TABLE_QUALIFIER_NAME);
		if (qualifier == null) {
			return null;
		}
		return qualifier.getTranslationTable();
	}
	
	public void setTranslationTable(Integer value) {
		if (value != null) {
			setSingleQualifierValue(Qualifier.TRANSL_TABLE_QUALIFIER_NAME, String.valueOf(value));
		}
		else {
			removeSingleQualifier(Qualifier.TRANSL_TABLE_QUALIFIER_NAME);
		}
	}
	
	public Integer getStartCodon() throws ValidationException {
		CodonStartQualifier qualifier = (CodonStartQualifier)getSingleQualifier(
				Qualifier.CODON_START_QUALIFIER_NAME);
		if (qualifier == null) {
			return null;
		}
		return qualifier.getStartCodon();	
	}

	public void setStartCodon(Integer value) {
		if (value != null) {
			setSingleQualifierValue(Qualifier.CODON_START_QUALIFIER_NAME, String.valueOf(value));
		}
		else {
			removeSingleQualifier(Qualifier.CODON_START_QUALIFIER_NAME);			
		}
	}	
	
	public String getTranslation() {
		return getSingleQualifierValue(Qualifier.TRANSLATION_QUALIFIER_NAME);
	}
	
	public void setTranslation(String value) {
		if (value != null&&!value.isEmpty()) {
            setSingleQualifierValue(Qualifier.TRANSLATION_QUALIFIER_NAME, value);
		}
		else {
			removeSingleQualifier(Qualifier.TRANSLATION_QUALIFIER_NAME);			
		}
	}

    public void setSingleQualifierValue(String name, String value) {
        if(name.equals(Qualifier.TRANSLATION_QUALIFIER_NAME) && value != null){
            value = value.replace("\n", "");//get rid of any line breaks
            value = value.replace(" ", "");//get rid of any spaces
        }
        super.setSingleQualifierValue(name, value);
    }

    public String getSingleQualifierValue(String name) {
        String returnVal = super.getSingleQualifierValue(name);
        if(returnVal != null && name.equals(Qualifier.TRANSLATION_QUALIFIER_NAME)){
            returnVal = returnVal.replace("\n", "");//get rid of any line breaks
            returnVal = returnVal.replace(" ", "");//get rid of any spaces
        }

        return returnVal;
    }

    public boolean isPseudo() {
		return (getSingleQualifier(Qualifier.PSEUDO_QUALIFIER_NAME) != null||getSingleQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME)!=null);
	}

	public void setPseudo(boolean pseudo) {
		if (pseudo) {
			setSingleQualifier(Qualifier.PSEUDO_QUALIFIER_NAME);
		} else {
			removeSingleQualifier(Qualifier.PSEUDO_QUALIFIER_NAME);
		}
	}

	public boolean isException() {
		return (getSingleQualifier(Qualifier.EXCEPTION_QUALIFIER_NAME) != null);
	}

	public String getException() {
		return getSingleQualifierValue(Qualifier.EXCEPTION_QUALIFIER_NAME);
	}
	
	public void setException(String value) {
		if (value != null) {
			setSingleQualifierValue(Qualifier.EXCEPTION_QUALIFIER_NAME, value);
		}
		else {
			removeSingleQualifier(Qualifier.EXCEPTION_QUALIFIER_NAME);			
		}
	}	

//	public boolean isPartial() {
//		return (getSingleQualifier(Qualifier.PARTIAL_QUALIFIER_NAME) != null);
//	}

//	public void setPartial(boolean exception) {
//		if (exception) {
//			setSingleQualifier(Qualifier.PARTIAL_QUALIFIER_NAME);
//		}
//		else {
//			removeSingleQualifier(Qualifier.PARTIAL_QUALIFIER_NAME);
//		}
//	}

}
