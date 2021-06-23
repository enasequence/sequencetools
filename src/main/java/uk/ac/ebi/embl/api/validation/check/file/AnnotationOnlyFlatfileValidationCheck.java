package uk.ac.ebi.embl.api.validation.check.file;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.fixer.entry.EntryNameFix;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.common.CommonUtil;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader.Format;
import uk.ac.ebi.embl.flatfile.reader.genbank.GenbankEntryReader;

import java.io.BufferedReader;
import java.util.concurrent.ConcurrentMap;

public class AnnotationOnlyFlatfileValidationCheck extends FileValidationCheck 
{
	public AnnotationOnlyFlatfileValidationCheck(SubmissionOptions options) 
	{
		super(options);
	}


	@Override
	public ValidationResult check(SubmissionFile submissionFile) throws ValidationEngineException {
		ValidationResult validationResult = new ValidationResult();

		try (BufferedReader fileReader = CommonUtil.bufferedReaderFromFile(submissionFile.getFile())) {
			clearReportFile(getReportFile(submissionFile));
			boolean isGenbankFile = isGenbank(submissionFile.getFile());

			if (!isGenbankFile && !validateFileFormat(submissionFile.getFile(), uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.FLATFILE)) {
				addErrorAndReport(validationResult, submissionFile, "InvalidFileFormat", "flatfile");
				return validationResult;
			}
			Format format = options.context.get() == Context.genome ? Format.ASSEMBLY_FILE_FORMAT : Format.EMBL_FORMAT;
			EntryReader entryReader = isGenbankFile ? new GenbankEntryReader(fileReader) :
					new EmblEntryReader(fileReader, format, submissionFile.getFile().getName());
			ValidationResult parseResult = entryReader.read();
			validationResult.append(parseResult);
			ConcurrentMap<String, Entry> map = (ConcurrentMap<String, Entry>) getAnnotationDB().hashMap("map").createOrOpen();
			while (entryReader.isEntry()) {
				if (!parseResult.isValid()) {
					getReporter().writeToFile(getReportFile(submissionFile), parseResult);
					addMessageStats(parseResult.getMessages());
				}

				Entry entry = entryReader.getEntry();
				if (entry.getSequence() == null || entry.getSequence().getSequenceByte() == null) {
					if (entry.getSubmitterAccession() == null) {
						if (entry.getPrimarySourceFeature() == null || entry.getPrimarySourceFeature().getSingleQualifierValue(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME) == null) {
							entry.setSubmitterAccession(entry.getPrimaryAccession());
						} else {
							entry.setSubmitterAccession(entry.getPrimarySourceFeature().getSingleQualifierValue(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME));
						}
					}
					entry.setSubmitterAccession(EntryNameFix.getFixedEntryName(entry.getSubmitterAccession()));
					entry.setDataClass(getDataclass(entry.getSubmitterAccession()));
					getOptions().getEntryValidationPlanProperty().validationScope.set(getValidationScope(entry.getSubmitterAccession()));
					getOptions().getEntryValidationPlanProperty().fileType.set(uk.ac.ebi.embl.api.validation.FileType.EMBL);
					appendHeader(entry);
					addSubmitterSeqIdQual(entry);
					if (entry.getSubmitterAccession() != null) {
						map.put(entry.getSubmitterAccession().toUpperCase(), entry);
					}
					parseResult = entryReader.read();
					validationResult.append(parseResult);
				} else {
					throw new ValidationEngineException("File has some entries with only annotations and some entries with sequences, If you intend to provide annotations" +
							" separately for some sequences, please submit annotations and sequences in different files", ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
				}
			}

		} catch (ValidationEngineException vee) {
			throw vee;
		} catch (Exception e) {
			throw new ValidationEngineException(e.getMessage(), e);
		} finally {
			if(getAnnotationDB() != null) {
				getAnnotationDB().commit();
			}
			//closeMapDB(getAnnotationDB());
		}
		return validationResult;
	}

	@Override
	public ValidationResult check() throws ValidationEngineException
	{
		throw new UnsupportedOperationException();
	}

}
