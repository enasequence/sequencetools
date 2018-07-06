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
package uk.ac.ebi.embl.api.translation;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.PeptideFeature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.qualifier.CodonQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.TranslExceptQualifier;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.sequence.SequenceBasesCheck;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

import java.util.*;

/**
 * Translates a bases to an amino acid sequences. The bases are encoded using
 * lower case single letter JCBN abbreviations and the amino acids are encoded
 * using upper case single letter JCBN abbreviations.
 */
public class Translator extends AbstractTranslator
{

	private boolean nonTranslating = false;
	private boolean exception = false;
	// private boolean partial = false;
	private boolean rightPartial = false;
	private boolean leftPartial = false;
	private boolean peptideFeature = false;
	private boolean fixDegenerateStartCodon = false;
	private boolean fixMissingStartCodon = false;
	private boolean fixRightPartialStopCodon = false;
	private boolean fixRightPartialCodon = false;

	private int codonStart = 1;

	public void setCodonStart(int startCodon)
	{
		this.codonStart = startCodon;
	}

	public void setLeftPartial(boolean leftPartial)
	{
		this.leftPartial = leftPartial;
	}

	public void setRightPartial(boolean rightPartial)
	{
		this.rightPartial = rightPartial;
	}

	public boolean isRightPartial()
	{
		return rightPartial;
	}

	public boolean isLeftPartial()
	{
		return leftPartial;
	}

	public void setNonTranslating(boolean nonTranslating)
	{
		this.nonTranslating = nonTranslating;
	}

	public void setException(boolean exception)
	{
		this.exception = exception;
	}

	public void setPeptideFeature(boolean peptideFeature)
	{
		this.peptideFeature = peptideFeature;
	}

	private class TranslationException
	{
		Character aminoAcid;
		Integer beginPosition;
		Integer endPosition;
	}

	private Map<Integer, TranslationException> translationExceptionMap = new HashMap<Integer, TranslationException>();

	public void addTranslationException(Integer beginPosition,
			Integer endPosition, Character aminoAcid)
	{
		TranslationException translationException = new TranslationException();
		translationException.beginPosition = beginPosition;
		translationException.endPosition = endPosition;
		translationException.aminoAcid = aminoAcid;
		translationExceptionMap.put(beginPosition, translationException);
	}

	public void addCodonException(String codon, Character aminoAcid)
	{
		codonTranslator.addCodonException(codon, aminoAcid);
	}

	/**
	 * If true then a degenerate start codon is translated to M using a
	 * translation exception.
	 */
	public void setFixDegenarateStartCodon(boolean fixDegenerateStartCodon)
	{
		this.fixDegenerateStartCodon = fixDegenerateStartCodon;
	}

	/** If true then a feature with no start codon is made 5' partial. */
	public void setFixMissingStartCodon(boolean fixMissingStartCodon)
	{
		this.fixMissingStartCodon = fixMissingStartCodon;
	}

	/**
	 * If true then 3' partiality is removed when a stop codon is found at the
	 * 3' end.
	 */
	public void setFixRightPartialStopCodon(boolean fixRightPartialStopCodon)
	{
		this.fixRightPartialStopCodon = fixRightPartialStopCodon;
	}

	/**
	 * If true then a partial codon is removed after a stop codon at the 3' end.
	 */
	public void setFixRightPartialCodon(boolean fixRightPartialCodon)
	{
		this.fixRightPartialCodon = fixRightPartialCodon;
	}

	private String extendCodon(String codon)
	{
		int bases = codon.length();
		for (int i = 0; i < 3 - bases; ++i)
		{
			codon = codon + "n";
		}
		return codon;
	}

	private boolean applyTranslationException(Codon codon)
	{
		TranslationException translationException = translationExceptionMap
				.get(codon.getPos());
		Character aminoAcid = null;
		if (translationException != null)
		{
			aminoAcid = translationException.aminoAcid;
		}
		codon.setTranslationException(aminoAcid != null);
		if (codon.isTranslationException())
		{
			codon.setAminoAcid(aminoAcid);
		}
		return codon.isTranslationException();
	}

	private void translateStartCodon(Codon codon,
			TranslationResult translationResult) throws ValidationException
	{
		codonTranslator.translateStartCodon(codon);
		applyTranslationException(codon);
		if (!codon.isTranslationException() && fixDegenerateStartCodon
				&& !leftPartial && !codon.getAminoAcid().equals('M')
				&& codonTranslator.isDegenerateStartCodon(codon))
		{
			codon.setAminoAcid('M');
			codon.setTranslationException(true);
			translationResult.setFixedDegerateStartCodon(true);
		}
	}

	private void translateOtherCodon(Codon codon) throws ValidationException
	{
		codonTranslator.translateOtherCodon(codon);
		applyTranslationException(codon);
	}

	public void translateCodons(byte[] sequence,
			TranslationResult translationResult) throws ValidationException
	{
		int countX = 0;
		int bases = sequence.length;
		Vector<Codon> codons = new Vector<Codon>(bases / 3);
		// Complete codons.
		int i = codonStart - 1;
		for (; i + 3 <= bases; i += 3)
		{
			Codon codon = new Codon();
			codon.setCodon(new String(Arrays.copyOfRange(sequence, i, i + 3)));
			codon.setPos(i + 1);
			if ((i == codonStart - 1) && !leftPartial)
			{
				translateStartCodon(codon, translationResult);
			} else
			{
				translateOtherCodon(codon);
			}
			codons.add(codon);
			// Added code to check CDS translations have more than 50% of X
			if (codon.getAminoAcid().equals('X'))
			{
				countX++;
			}
		}
		if (countX > (codons.size() / 2))
		{
			ValidationException.throwError("Translator-20");
		}
		int trailingBases = bases - i;
		if (trailingBases > 0)
		{
			Codon codon = new Codon();
			codon.setCodon(extendCodon(new String(Arrays.copyOfRange(sequence,
					i, sequence.length))));
			codon.setPos(i + 1);
			if ((i == codonStart - 1) && !leftPartial)
			{
				translateStartCodon(codon, translationResult);
			} else
			{
				translateOtherCodon(codon);
			}

			// Discard partial codon translations X.
			if (!codon.getAminoAcid().equals('X'))
			{
				trailingBases = 0;
				codons.add(codon);
			}

		}
		translationResult.setCodons(codons);
		if (trailingBases > 0)
		{
			translationResult
					.setTrailingBases(new String(Arrays.copyOfRange(sequence,
							sequence.length - trailingBases, sequence.length)));
		} else
		{
			translationResult.setTrailingBases(new String());
		}
	}

	public ValidationResult configureFromFeature(CdsFeature feature,
			TaxonHelper taxHelper, Entry entry) throws ValidationException
	{

		ValidationResult validationResult = new ValidationResult();
		Integer featureTranslationTable = null;
		if (feature != null)
			featureTranslationTable = feature.getTranslationTable();

		Integer translationTable = null;
		ValidationMessage<Origin> infoMessage = null;

		SourceFeature sourceFeature = entry.getPrimarySourceFeature();
		if (sourceFeature != null)
		{
			Taxon taxon = null;
			if (sourceFeature.getTaxon().getTaxId() != null)
			{
				taxon = taxHelper.getTaxonById(sourceFeature.getTaxon()
						.getTaxId());
			} else if (sourceFeature.getTaxon().getScientificName() != null)
			{
				taxon = taxHelper.getTaxonByScientificName(sourceFeature
						.getTaxon().getScientificName());
			}

			// Classified organism

			if (taxon != null)
			{
				String organelle = sourceFeature
						.getSingleQualifierValue("organelle");
				if (StringUtils.equals(organelle, "mitochondrion")
						|| StringUtils.equals(organelle,
								"mitochondrion:kinetoplast"))
				{
					translationTable = taxon.getMitochondrialGeneticCode();
					infoMessage = EntryValidations.createMessage(
							entry.getOrigin(), Severity.INFO,
							"CDSTranslator-12", organelle, translationTable);
				} else if (StringUtils.contains(organelle, "plastid"))
				{
					translationTable = TranslationTable.PLASTID_TRANSLATION_TABLE;
					infoMessage = EntryValidations.createMessage(
							entry.getOrigin(), Severity.INFO,
							"CDSTranslator-12", organelle, translationTable);
				} else
				{
					translationTable = taxon.getGeneticCode();
					infoMessage = EntryValidations.createMessage(
							entry.getOrigin(), Severity.INFO,
							"CDSTranslator-11", translationTable);
				}
			}

			else
			{
				if (featureTranslationTable != null)
				{
					translationTable = featureTranslationTable;
					infoMessage = EntryValidations.createMessage(
							entry.getOrigin(), Severity.INFO,
							"CDSTranslator-13", translationTable);
				} else
				{
					String organelle = sourceFeature
							.getSingleQualifierValue("organelle");
					if (StringUtils.equals(organelle, "mitochondrion"))
					{
						translationTable = 2;
						infoMessage = EntryValidations
								.createMessage(entry.getOrigin(),
										Severity.INFO, "CDSTranslator-14",
										organelle, translationTable);
					} else if (StringUtils.equals(organelle,
							"mitochondrion:kinetoplast"))
					{
						translationTable = 4;
						infoMessage = EntryValidations
								.createMessage(entry.getOrigin(),
										Severity.INFO, "CDSTranslator-14",
										organelle, translationTable);
					} else if (StringUtils.contains(organelle, "plastid"))
					{
						translationTable = TranslationTable.PLASTID_TRANSLATION_TABLE;
						infoMessage = EntryValidations
								.createMessage(entry.getOrigin(),
										Severity.INFO, "CDSTranslator-14",
										organelle, translationTable);
					} else
					{
						translationTable = TranslationTable.DEFAULT_TRANSLATION_TABLE;
						infoMessage = EntryValidations.createMessage(
								entry.getOrigin(), Severity.INFO,
								"CDSTranslator-15", translationTable);
					}

				}
			}
		}

		if (featureTranslationTable != null
				&& !featureTranslationTable.equals(translationTable))
		{
			validationResult.append(EntryValidations.createMessage(
					entry.getOrigin(), Severity.ERROR, "CDSTranslator-10",
					featureTranslationTable, translationTable));
			translationTable = featureTranslationTable;
		} else if (infoMessage != null)
		{
			validationResult.append(infoMessage);

		}
		if (translationTable == null)
		{
			if (feature != null)// CDSfeature
				validationResult.append(EntryValidations.createMessage(
						feature.getOrigin(), Severity.INFO, "CDSTranslator-8",
						TranslationTable.DEFAULT_TRANSLATION_TABLE));
			else
				// non-cds feature
				validationResult.append(EntryValidations.createMessage(
						entry.getOrigin(), Severity.INFO, "CDSTranslator-8"));
			// Get the default translation table.
			translationTable = TranslationTable.DEFAULT_TRANSLATION_TABLE;
		}
		setTranslationTable(translationTable);
		if (feature == null) // if it is not CDSfeature ex:tRNA
			return validationResult;
		if (!translationTable
				.equals(TranslationTable.DEFAULT_TRANSLATION_TABLE))
		{
			// Set feature translation table.
			if (!(feature instanceof PeptideFeature))
				feature.setTranslationTable(translationTable);
		}

		// Set the start codon.
		Integer startCodon = feature.getStartCodon();
		if (startCodon != null)
		{
			setCodonStart(startCodon);
		}

		/**
		 * set the left and right partiality - if the compound location is
		 * global complement, and one end is partial only, we need to reverse
		 * the left and right partiality, as the translator assumes sequence to
		 * be on the primary strand. (if both ends are partial or both are
		 * unpartial, no need to reverse)
		 */
		CompoundLocation compoundLocation = feature.getLocations();
		if (compoundLocation.isComplement()
				&& compoundLocation.isLeftPartial() != compoundLocation
						.isRightPartial())
		{

			setLeftPartial(!compoundLocation.isLeftPartial());// reverse it
			setRightPartial(!compoundLocation.isRightPartial());// reverse it
		} else
		{
			setLeftPartial(compoundLocation.isLeftPartial());
			setRightPartial(compoundLocation.isRightPartial());
		}

		// Set a pseudo translation.
		setNonTranslating(feature.isPseudo());
		// Set an exceptional translation.
		setException(feature.isException());

		// Set translation exceptions.
		List<TranslExceptQualifier> translExceptQualifiers = new ArrayList<TranslExceptQualifier>();
		feature.getComplexQualifiers(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME,
				translExceptQualifiers);

		for (TranslExceptQualifier qualifier : translExceptQualifiers)
		{
			Long beginPosition = qualifier.getLocations().getMinPosition();
			Long endPosition = qualifier.getLocations().getMaxPosition();

			Integer relativeBeginPos = feature.getLocations()
					.getRelativeIntPosition(beginPosition);
			Integer relativeEndPos = feature.getLocations()
					.getRelativeIntPosition(endPosition);
			if (relativeBeginPos != null && relativeEndPos != null)// EMD-5594
			{
				if (beginPosition < endPosition
						&& relativeBeginPos > relativeEndPos)
				{
					Integer temp = relativeBeginPos;
					relativeBeginPos = relativeEndPos;
					relativeEndPos = temp;
				}
			}
			if (relativeBeginPos == null)
			{
				validationResult.append(EntryValidations.createMessage(
						feature.getOrigin(), Severity.ERROR, "CDSTranslator-6",
						beginPosition.toString()));
			} else if (relativeEndPos == null)
			{
				validationResult.append(EntryValidations.createMessage(
						feature.getOrigin(), Severity.ERROR, "CDSTranslator-7",
						endPosition.toString()));
			} else
			{
				addTranslationException(relativeBeginPos, relativeEndPos,
						qualifier.getAminoAcid().getLetter());
			}
		}

		// Set codon exceptions.
		List<CodonQualifier> codonQualifiers = new ArrayList<CodonQualifier>();
		feature.getComplexQualifiers(Qualifier.CODON_QUALIFIER_NAME,
				codonQualifiers);
		for (CodonQualifier qualifier : codonQualifiers)
		{
			addCodonException(qualifier.getCodon(), qualifier.getAminoAcid()
					.getLetter());
		}

		// set whether is a peptide feature
		if (feature instanceof PeptideFeature)
		{
			this.peptideFeature = true;
		}

		return validationResult;
	}

	private Integer getEntryTranslationTable(Integer featureTranslationTable,
			TaxonHelper taxHelper, Entry entry,
			ValidationResult validationResult) throws ValidationException
	{
		Integer translationTable = null;

		SourceFeature sourceFeature = entry.getPrimarySourceFeature();
		if (sourceFeature != null)
		{
			Taxon taxon = null;
			if (sourceFeature.getTaxon().getTaxId() != null)
			{
				taxon = taxHelper.getTaxonById(sourceFeature.getTaxon()
						.getTaxId());
			} else if (sourceFeature.getTaxon().getScientificName() != null)
			{
				taxon = taxHelper.getTaxonByScientificName(sourceFeature
						.getTaxon().getScientificName());
			}

			// Classified organism
			if (taxon != null)
			{
				String organelle = sourceFeature
						.getSingleQualifierValue("organelle");
				if (StringUtils.equals(organelle, "mitochondrion")
						|| StringUtils.equals(organelle,
								"mitochondrion:kinetoplast"))
				{
					return taxon.getMitochondrialGeneticCode();
				} else if (StringUtils.contains(organelle, "plastid"))
				{
					return TranslationTable.PLASTID_TRANSLATION_TABLE;
				} else
				{
					return taxon.getGeneticCode();
				}
			} else
			{
				if (featureTranslationTable != null)
					return featureTranslationTable;
				String organelle = sourceFeature
						.getSingleQualifierValue("organelle");
				if (StringUtils.equals(organelle, "mitochondrion"))
				{
					return 2;
				}
				if (StringUtils.equals(organelle, "mitochondrion:kinetoplast"))
				{
					return 4;
				} else if (StringUtils.contains(organelle, "plastid"))
				{
					return TranslationTable.PLASTID_TRANSLATION_TABLE;
				} else
				{
					return TranslationTable.DEFAULT_TRANSLATION_TABLE;
				}

			}
		}

		return translationTable;

	}

	public ExtendedResult<TranslationResult> translate(byte[] sequence,
			Origin origin)
	{

		TranslationResult translationResult = new TranslationResult();
		ExtendedResult<TranslationResult> extendedResult = new ExtendedResult<TranslationResult>(
				translationResult);

		if (sequence == null)
		{
			try
			{
				ValidationException.throwError("Translator-19");
			} catch (ValidationException ex)
			{
				extendedResult.append(ex.getValidationMessage());
			}
			return extendedResult;
		}
		SequenceFactory sequenceFactory = new SequenceFactory();
		SequenceBasesCheck basesCheck = new SequenceBasesCheck();
		if (origin != null)
		{
			basesCheck.setOrigin(origin);
		}
		extendedResult.append(basesCheck.check(sequenceFactory
				.createSequenceByte(sequence)));
		if (extendedResult.count(Severity.ERROR) > 0)
		{
			return extendedResult;
		}
		try
		{
			validateCodonStart(sequence.length);
			sequence = validateTranslationExceptions(sequence);
			validateCodons(sequence.length, translationResult);
			translateCodons(sequence, translationResult);
			if (translationResult.getCodons().size() == 0)
			{
				if (exception || nonTranslating)
				{
					// no conceptual translation
					translationResult.setConceptualTranslationCodons(0);
					return extendedResult;
				} else
				{
					// no translation
					ValidationException.throwError("Translator-1");
				}
			}
			validateTranslation(translationResult);
		} catch (ValidationException ex)
		{
			extendedResult.append(ex.getValidationMessage());
		}
		return extendedResult;
	}

	private void validateCodonStart(int bases) throws ValidationException
	{
		if (codonStart < 1 || codonStart > 3)
		{
			ValidationException.throwError("Translator-2", codonStart);
		}
		if (codonStart != 1)
		{
			if (!leftPartial && !nonTranslating)
			{
				ValidationException.throwError("Translator-3", codonStart);
			}
		}
		if (bases < 3)
		{
			if (codonStart != 1)
			{
				// Currently, all cds features with less than 3 bases have codon
				// start 1. These entries are BN000810, AY950706, AY950707,
				// DQ539670, DQ519928, DQ785858,DQ785863, EF466144, EF466157,
				// EF466202.
				ValidationException.throwError("Translator-4");
			}
		}
	}

	private byte[] validateTranslationExceptions(byte[] sequence)
			throws ValidationException
	{
		int bases = sequence.length;
		Iterator<Integer> itr = translationExceptionMap.keySet().iterator();
		while (itr.hasNext())
		{
			TranslationException translationException = translationExceptionMap
					.get(itr.next());
			int beginPos = translationException.beginPosition;
			int endPos;
			if (translationException.endPosition == null)
			{
				endPos = beginPos;
			} else
			{
				endPos = translationException.endPosition;
			}

			Character aminoAcid = translationException.aminoAcid;
			if (beginPos < codonStart)
			{
				// Translation exception outside frame on the 5' end.
				ValidationException.throwError("Translator-4");
			}
			if (beginPos > bases)
			{
				// Translation exception outside frame on the 3' end.
				ValidationException.throwError("Translator-6");
			}
			if (endPos < beginPos)
			{
				// Invalid translation exception range.
				ValidationException.throwError("Translator-7");
			}
			if (!(endPos == beginPos + 2)
					&& !(endPos == beginPos + 1 && endPos == bases && aminoAcid
							.equals('*'))
					&& !(endPos == beginPos && endPos == bases && aminoAcid
							.equals('*')))
			{
				// Translation exception must span 3 bases or be a partial stop
				// codon at 3' end.
				ValidationException.throwError("Translator-8");
			}
			if (endPos > bases)
			{
				// Translation exception outside frame on the 3' end.
				ValidationException.throwError("Translator-6");
			}
			int translationExceptionCodonStart = beginPos % 3;
			if (translationExceptionCodonStart == 0)
			{
				translationExceptionCodonStart = 3;
			}
			if (translationExceptionCodonStart != codonStart)
			{
				// Translation exception is in different frame.
				ValidationException.throwError("Translator-9", beginPos,
						translationExceptionCodonStart, codonStart);
			}
			// Extend 3' partial stop codon.
			if (endPos == beginPos + 1 && aminoAcid.equals('*'))
			{
				sequence = Arrays.copyOf(sequence, bases + 1);
				sequence[bases] = 'n';
			} else if (endPos == beginPos && aminoAcid.equals('*'))
			{
				sequence = Arrays.copyOf(sequence, bases + 2);
				sequence[bases] = 'n';
				sequence[bases + 1] = 'n';
			}
		}
		return sequence;
	}

	private void validateCodons(int bases, TranslationResult translationResult)
			throws ValidationException
	{
		if (bases < 3)
		{
			// All cds features with 1 base are 3' partial and nonTranslating.
			// There is only one such entry: BN000810.
			// All cds features with 2 bases are 3' partial and
			// translate to 'M'. These entries are: AY950706, AY950707,
			// DQ539670, DQ519928, DQ785858,DQ785863, EF466144, EF466157,
			// EF466202.
			translationResult.setTranslationBaseCount(bases);
			if (!leftPartial && !rightPartial)
			{
				// CDS features with less than 3 bases must be 3' or 5' partial.
				ValidationException.throwError("Translator-10");
			}
		} else if ((bases - codonStart + 1) % 3 != 0)
		{
			int length = bases - codonStart + 1;
			translationResult.setTranslationLength(length);

			// The current implementation allows 3' partial non-
			// translated codons after the stop codon if the
			// feature is 5'partial. An example CDS entry is AAA67861.
			// non mod 3 peptide features other than Cds dont throw errors as
			// there are more complex checks in
			// PeptideFeatureCheck
			if (!peptideFeature && !leftPartial && !rightPartial
					&& !nonTranslating && !exception)
			{
				// CDS feature length must be a multiple of 3. Consider 5' or 3'
				// partial location.
				ValidationException.throwError("Translator-11");
			}
		}
	}

	private void validateTranslation(TranslationResult translationResult)
			throws ValidationException
	{
		int trailingStopCodons = 0;
		int internalStopCodons = 0;
		Vector<Codon> codons = translationResult.getCodons();
		int i = codons.size();
		// Count the number of trailing stop codons.
		while (i > 0 && codons.get(i - 1).getAminoAcid().equals('*'))
		{
			--i;
			++trailingStopCodons;
		}
		int conceptualTranslationCodons = codons.size() - trailingStopCodons;
		translationResult
				.setConceptualTranslationCodons(conceptualTranslationCodons);
		if (conceptualTranslationCodons == 0)
		{
			validateStopCodonOnly(translationResult);
			validateTrailingStopCodons(trailingStopCodons, translationResult);
		} else
		{
			// Count the number of internal stop codons.
			while (i > 0)
			{
				if (codons.get(i - 1).getAminoAcid().equals('*'))
				{
					++internalStopCodons;
				}
				--i;
			}
			boolean conceptualTranslation = true;
			if (!validateInternalStopCodons(internalStopCodons))
			{
				conceptualTranslation = false;
			}
			if (!validateStartCodon(translationResult))
			{
				conceptualTranslation = false;
			}
			if (!validateTrailingStopCodons(trailingStopCodons,
					translationResult))
			{
				conceptualTranslation = false;
			}
			if (!conceptualTranslation)
			{
				translationResult.setConceptualTranslationCodons(0);
			}
		}
	}

	private void validateStopCodonOnly(TranslationResult translationResult)
			throws ValidationException
	{
		if (exception || nonTranslating)
		{
			return; // no conceptual translation
		}
		if (translationResult.getCodons().size() == 1
				&& translationResult.getTrailingBases().length() == 0
				&& leftPartial)
		{
			return; // no conceptual translation
		} else
		{
			// CDS feature can have a single stop codon only
			// if it has 3 bases and is 5' partial.
			ValidationException.throwError("Translator-12");
		}
	}

	private boolean validateTrailingStopCodons(int trailingStopCodons,
			TranslationResult translationResult) throws ValidationException
	{
		if (!exception)
		{
			if (trailingStopCodons > 1)
			{
				if (nonTranslating)
				{
					return false; // no conceptual translation
				} else
				{
					// More than one stop codon at the 3' end.
					ValidationException.throwError("Translator-13");
				}
			}
			if (trailingStopCodons == 1 && rightPartial)
			{
				if (nonTranslating)
				{
					return false; // no conceptual translation
				} else
				{
					if (fixRightPartialStopCodon)
					{
						translationResult.setFixedRightPartialStopCodon(true);
						translationResult.setFixedRightPartial(true);
						rightPartial = false;
					} else
					{
						// Stop codon found at 3' partial end.
						ValidationException.throwError("Translator-14");
					}
				}
			}
			if (trailingStopCodons == 0 && !rightPartial)
			{
				if (nonTranslating)
				{
					return false; // no conceptual translation
				} else if (!peptideFeature)
				{// peptide features are allowed to not have stop codons
					// No stop codon at the 3' end.
					ValidationException.throwError("Translator-15");
				}
			}
			if (trailingStopCodons == 1
					&& translationResult.getTrailingBases().length() > 0)
			{
				if (nonTranslating)
				{
					return false; // no conceptual translation
				} else
				{
					if (fixRightPartialCodon)
					{
						translationResult.setFixedRightPartialCodon(true);
					} else
					{
						// A partial codon appears after the stop codon.
						ValidationException.throwError("Translator-16");
					}
				}
			}
		}
		return true;
	}

	private boolean validateInternalStopCodons(int internalStopCodons)
			throws ValidationException
	{
		if (internalStopCodons > 0)
		{
			if (exception || nonTranslating)
			{
				return false; // no conceptual translation
			} else
			{
				// The protein translation contains internal stop condons.
				ValidationException.throwError("Translator-17");
			}
		}
		return true;
	}

	private boolean validateStartCodon(TranslationResult translationResult)
			throws ValidationException
	{
		if (!leftPartial && !exception && !peptideFeature)
		{
			if (!translationResult.getCodons().get(0).getAminoAcid()
					.equals('M')
					&& !leftPartial)
			{
				if (nonTranslating)
				{
					return false; // no conceptual translation
				} else
				{
					if (fixMissingStartCodon)
					{
						translationResult.setFixedMissingStartCodon(true);
						translationResult.setFixedLeftPartial(true);
						leftPartial = true;
					} else
					{
						// The protein translation does not start with a
						// methionine.
						ValidationException.throwError("Translator-18");
					}
				}
			}
		}
		return true;
	}

	public boolean equalsTranslation(String expectedTranslation,
			String conceptualTranslation)
	{
		if (expectedTranslation.length() < conceptualTranslation.length())
		{
			return false;
		}
		for (int i = 0; i < conceptualTranslation.length(); i++)
		{
			if (expectedTranslation.charAt(i) != conceptualTranslation
					.charAt(i) && expectedTranslation.charAt(i) != 'X')
			{
				return false;
			}
		}
		// Ignore trailing X.
		if (expectedTranslation.length() > conceptualTranslation.length())
		{
			for (int i = conceptualTranslation.length(); i < expectedTranslation
					.length(); i++)
			{
				if (expectedTranslation.charAt(i) != 'X')
				{
					return false;
				}
			}
		}
		return true;
	}

}
