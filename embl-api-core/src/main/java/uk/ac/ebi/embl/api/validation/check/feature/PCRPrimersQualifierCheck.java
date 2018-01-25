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
package uk.ac.ebi.embl.api.validation.check.feature;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataRow;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@CheckDataSet(dataSetNames = { FileName.NUCLEOTIDE_CODE })
@Description("Qualifier \"{0}\" has invalid format"
		+ "Qualifier \"{0}\" has illegal modified bases format:\"{1}\""
		+ "Qualifier \"{0}\" has no modified bases within <>"
		+ "Qualifier \"{0}\" has invalid nucleotide:\"{1}\"")
public class PCRPrimersQualifierCheck extends FeatureValidationCheck {

	private final static String PCR_FORMAT_MESSAGE_ID = "PCRPrimersQualifierCheck_1";
	private final static String NUCLEOTIDE_MESSAGE_ID_1 = "PCRPrimersQualifierCheck_2";
	private final static String NUCLEOTIDE_MESSAGE_ID_2 = "PCRPrimersQualifierCheck_3";
	private final static String NUCLEOTIDE_MESSAGE_ID_3 = "PCRPrimersQualifierCheck_4";
	String validLineOstr = null;
	String regex = "^\\s*.*\\s*(fwd_name:|fwd_seq:|rev_name:|rev_seq:)+\\s*$";
	Pattern remove_pattern = Pattern.compile(regex);
	String fwd_name_regex = "fwd_name:\\s*(.+)\\s*";//
	String fwd_seq_regex = "fwd_seq:\\s*([\\sA-Za-z<>]+)\\s*";//
	String rev_name_regex = "rev_name:\\s*(.+)\\s*";//
	String rev_seq_regex = "rev_seq:\\s*([\\sA-Za-z<>]+)\\s*";//
	Pattern fwd_name_pattern = Pattern.compile(fwd_name_regex);
	Pattern fwd_seq_pattern = Pattern.compile(fwd_seq_regex);
	Pattern rev_name_pattern = Pattern.compile(rev_name_regex);
	Pattern rev_seq_pattern = Pattern.compile(rev_seq_regex);

	public PCRPrimersQualifierCheck() {

	}

	public ValidationResult check(Feature feature) {

		result = new ValidationResult();
		if (feature == null) {
			return result;
		}

		for(DataRow dataRow : GlobalDataSets.getDataSet(FileName.NUCLEOTIDE_CODE).getRows()) {

			/*
			 * fwd_seq and rev_seq are both mandatory; fwd_name and rev_name are
			 * both optional. Both sequences should be presented in 5'>3' order. The
			 * sequences should be given in the IUPAC degenerate-base
			 * alphabet.format needs to be checked ( base alphabet in
			 * cv_nucleotide.code )
			 */
			List<Qualifier> pcrQualifiers = feature
					.getQualifiers(Qualifier.PCR_PRIMERS_QUALIFIER_NAME);

			if (pcrQualifiers.size() == 0) {
				continue;
			}
			String fwd_name = "";
			String fwd_seq = "";
			String rev_name = "";
			String rev_seq = "";

			for (Qualifier pcrQualifier : pcrQualifiers) {

				String[] pcrTokens = pcrQualifier.getValue().split("\\s*,\\s*");

				for (String pcrToken : pcrTokens) {

					Matcher fwd_name_matcher = fwd_name_pattern.matcher(pcrToken);
					Matcher fwd_seq_matcher = fwd_seq_pattern.matcher(pcrToken);
					Matcher rev_name_matcher = rev_name_pattern.matcher(pcrToken);
					Matcher rev_seq_matcher = rev_seq_pattern.matcher(pcrToken);

					if (fwd_name_matcher.find()) {
						fwd_name = fwd_name_matcher.group(1);

					}
					if (fwd_seq_matcher.find()) {
						fwd_seq = fwd_seq_matcher.group(1);
						Utils.shrink(fwd_seq);
						fwd_seq = fwd_seq.toLowerCase();
						if (!validateNucleotides(fwd_seq, pcrQualifier, dataRow)) {
							reportError(pcrQualifier.getOrigin(), NUCLEOTIDE_MESSAGE_ID_3,
									pcrQualifier.getName(), fwd_seq);
						}
					}
					if (rev_name_matcher.find()) {
						rev_name = rev_name_matcher.group(1);
					}
					if (rev_seq_matcher.find()) {
						rev_seq = rev_seq_matcher.group(1);
						Utils.shrink(rev_seq);
						rev_seq = rev_seq.toLowerCase();
						if (!validateNucleotides(rev_seq, pcrQualifier, dataRow)) {
							reportError(pcrQualifier.getOrigin(), NUCLEOTIDE_MESSAGE_ID_3,
									pcrQualifier.getName(), rev_seq);
						}

					}

					if (remove_pattern.matcher(pcrToken).find()) {
						reportError(pcrQualifier.getOrigin(), PCR_FORMAT_MESSAGE_ID,
								pcrQualifier.getName());
					}

				}
			}
		}

		return result;
	}

	private boolean validateNucleotides(String sequence, Qualifier qualifier, DataRow dataRow) {
		boolean openAngleBracketFound = false;
		boolean foundModifiedNucleotideWithinBrackets = false;
		if (sequence == null) {
			return true;
		}
		char[] nucleotides = sequence.toCharArray();
		for (char c : nucleotides) {
			boolean isNucleotide = false;
			if (c == '<') {
				if (openAngleBracketFound) {
					reportError(qualifier.getOrigin(), NUCLEOTIDE_MESSAGE_ID_1,
							qualifier.getName(), sequence);
				}
				openAngleBracketFound = true;

				continue;
			}
			if (c == '>') {
				if (!openAngleBracketFound) {
					reportError(qualifier.getOrigin(), NUCLEOTIDE_MESSAGE_ID_1,
							qualifier.getName(), sequence);
				}
				if (!foundModifiedNucleotideWithinBrackets) {
					reportError(qualifier.getOrigin(), NUCLEOTIDE_MESSAGE_ID_2,
							qualifier.getName());
				}
				foundModifiedNucleotideWithinBrackets = false;

				openAngleBracketFound = false;

				continue;

			}
			if (openAngleBracketFound) {
				foundModifiedNucleotideWithinBrackets = true;
				continue;
			}
			String[] validCodes = dataRow.getStringArray(0);
			for (String validCode : validCodes) {
				if (validCode.charAt(0) == c) {
					isNucleotide = true;
					break;
				}
			}
			if (!isNucleotide) {
				return false;
			}

		}
		if (openAngleBracketFound) {
			reportError(qualifier.getOrigin(), NUCLEOTIDE_MESSAGE_ID_1,
					qualifier.getName(), sequence);
		}

		return true;

	}

	

}
