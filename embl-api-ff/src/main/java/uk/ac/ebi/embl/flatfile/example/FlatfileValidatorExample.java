package uk.ac.ebi.embl.flatfile.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;
/*
 * This is a basic example for EMBL flatfile validation, it can be improved
 */
public class FlatfileValidatorExample {

	private static FileWriter infoWriter;
	private static FileWriter errorWriter;
	private static FileWriter reportWriter;
	private static FileWriter fixWriter;

	public static void main(String[] args) throws IOException,
			ValidationEngineException {
		File file = new File(args[0]);//JCommander can be used for command line arguments reading 
		if (!file.exists())
			throw new ValidationEngineException("Invalid File: File doesn't exist");
		BufferedReader fileReader = new BufferedReader(new FileReader(file));
		EmblEntryReader reader = new EmblEntryReader(fileReader,
				EmblEntryReader.Format.EMBL_FORMAT, file.getName());
		//Warnings 
		infoWriter = new FileWriter(file.getParent() + File.separator	+ "VAL_INFO.txt");
		//Errors
		errorWriter = new FileWriter(file.getParent() + File.separator + "VAL_ERROR.txt");
		//Information
		reportWriter = new FileWriter(file.getParent() + File.separator + "VAL_REPORTS.txt");
		//Fixes
		fixWriter = new FileWriter(file.getParent() + File.separator + "VAL_FIXES.txt");
		EmblEntryWriter writer;
		EmblEntryValidationPlan validationPlan = getValidationPlan();
		ValidationResult parseResult = reader.read();
		File fixedFile = new File(file.getPath() + ".fixed");
		if (!fixedFile.exists()) {
			fixedFile.createNewFile();
		}
		FileWriter fixedFileWriter = new FileWriter(fixedFile);
		while (reader.isEntry()) {
			Entry entry = (Entry) reader.getEntry();
			ValidationPlanResult planResult = validationPlan.execute(entry);
			planResult.append(parseResult);
			writeResultsToFile(planResult);
			writer = new EmblEntryWriter(entry);
			writer.write(fixedFileWriter);
			parseResult = reader.read();
		}
		
		errorWriter.close();
		infoWriter.close();
		fixWriter.close();

	}
	
	public static EmblEntryValidationPlan getValidationPlan() {
		EmblEntryValidationPlanProperty emblEntryValidationProperty = new EmblEntryValidationPlanProperty();
		emblEntryValidationProperty.validationScope.set(ValidationScope.EMBL);
		emblEntryValidationProperty.isFixMode.set(true);
		emblEntryValidationProperty.fileType.set(uk.ac.ebi.embl.api.validation.FileType.EMBL);
		EmblEntryValidationPlan validationPlan = new EmblEntryValidationPlan(emblEntryValidationProperty);
		validationPlan.addMessageBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		validationPlan.addMessageBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		return validationPlan;

	}
/*
 * Writes validation results to the reporting files
 */
	public static void writeResultsToFile(ValidationPlanResult planResult)
			throws IOException {

		for (ValidationResult result : planResult.getResults()) {
			result.writeMessages(errorWriter, Severity.ERROR,planResult.getTargetOrigin());
			result.writeMessages(infoWriter, Severity.INFO,planResult.getTargetOrigin());
			result.writeMessages(infoWriter, Severity.WARNING,planResult.getTargetOrigin());
			result.writeMessages(fixWriter, Severity.FIX,planResult.getTargetOrigin());
			errorWriter.flush();
			infoWriter.flush();
			fixWriter.flush();
		}

		for (ValidationResult result : planResult.getResults()) {
			if (result.isHasReportMessage()) {
				result.writeMessageReports(true);
				result.writeMessages(reportWriter);
			}
		}
	}
}
