package uk.ac.ebi.embl.api.validation.check.file;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentMap;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader.Format;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

public class AnnotationOnlyFlatfileValidationCheck extends FileValidationCheck 
{
	public AnnotationOnlyFlatfileValidationCheck(SubmissionOptions options) 
	{
		super(options);
	}

	@Override
	public boolean check(SubmissionFile submissionFile) throws ValidationEngineException 
	{
		boolean valid =true;
		EmblEntryValidationPlan validationPlan=null;
		try(BufferedReader fileReader= getBufferedReader(submissionFile.getFile());PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile))
		{
			if(!validateFileFormat(submissionFile.getFile(), uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.ANNOTATION_ONLY_FLATFILE))
			{
				ValidationResult result = new ValidationResult();
				valid = false;
				ValidationMessage<Origin> validationMessage = new ValidationMessage<>(Severity.ERROR, "Invalid FLAT File Format:Failed to read entry");
				result.append(validationMessage);
				if(getOptions().reportDir.isPresent())
				getReporter().writeToFile(getReportFile(submissionFile), result);
				addMessagekey(result);
				return valid;
			}
			Format format = options.context.get()==Context.genome?Format.ASSEMBLY_FILE_FORMAT:Format.EMBL_FORMAT;
			EmblEntryReader emblReader = new EmblEntryReader(fileReader,format,submissionFile.getFile().getName());
			ValidationResult parseResult = emblReader.read();
			while(emblReader.isEntry())
			{
				if(!parseResult.isValid())
				{
					valid = false;
					getReporter().writeToFile(getReportFile(submissionFile), parseResult);
					addMessagekey(parseResult);
				}
				parseResult=new ValidationResult();
				Entry entry = emblReader.getEntry();
				entry.setDataClass(getDataclass(entry.getSubmitterAccession()));
				if(entry.getSubmitterAccession()!=null&&getSequenceDB()!=null)
				{
					ConcurrentMap map = getSequenceDB().hashMap("map").createOrOpen();
					if(map.get(entry.getSubmitterAccession().toUpperCase())!=null)
					{
						if(entry.getSequence()==null)
						{
							SequenceFactory sequenceFactory =new SequenceFactory();
							entry.setSequence(sequenceFactory.createSequence());
						}
						entry.getSequence().setSequence(ByteBuffer.wrap(((String)map.get(entry.getSubmitterAccession().toUpperCase())).getBytes()));

					}
				}

    			getOptions().getEntryValidationPlanProperty().validationScope.set(getValidationScope(entry.getSubmitterAccession()));
				getOptions().getEntryValidationPlanProperty().fileType.set(uk.ac.ebi.embl.api.validation.FileType.EMBL);
				validationPlan=new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
				appendHeader(entry);
				ValidationPlanResult planResult=validationPlan.execute(entry);
				if(!planResult.isValid())
				{
					valid = false;
					getReporter().writeToFile(getReportFile(submissionFile), planResult);
					for(ValidationResult result: planResult.getResults())
					{
						addMessagekey(result);
					}
				}
				else
				{
					if(fixedFileWriter!=null)
						new EmblEntryWriter(entry).write(fixedFileWriter);
				}
				emblReader.read();
			}

		}catch(Exception e)
		{
			if(getSequenceDB()!=null)
	               getSequenceDB().close();
			throw new ValidationEngineException(e.getMessage());
		}
		return valid;
	}

	@Override
	public boolean check() throws ValidationEngineException 
	{
		return false;
	}

}
