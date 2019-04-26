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

import uk.ac.ebi.embl.agp.reader.AGPFileReader;
import uk.ac.ebi.embl.agp.reader.AGPLineReader;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.check.feature.CdsFeatureTranslationCheck;
import uk.ac.ebi.embl.api.validation.helper.FileUtils;
import uk.ac.ebi.embl.api.validation.helper.FlattenedMessageResult;
import uk.ac.ebi.embl.api.validation.helper.FlattenedValidationPlanResult;
import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.api.validation.helper.ValidationMessageComparator;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.plan.GFF3ValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.GenomeAssemblyValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.ValidationPlan;
import uk.ac.ebi.embl.fasta.reader.FastaFileReader;
import uk.ac.ebi.embl.fasta.reader.FastaLineReader;
import uk.ac.ebi.embl.flatfile.reader.FlatFileReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.reader.genbank.GenbankEntryReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;
import uk.ac.ebi.embl.flatfile.writer.EntryWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.embl.flatfile.writer.degenerator.DEGenerator;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;
import uk.ac.ebi.embl.flatfile.writer.genbank.GenbankEntryWriter;
import uk.ac.ebi.embl.gff3.reader.GFF3FlatFileEntryReader;

import org.apache.commons.dbutils.DbUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
/**
 * Created by IntelliJ IDEA. User: Lawrence Date: 08-Dec-2008 Time: 09:58:38 To
 * change this template use File | Settings | File Templates.
 */
public class EnaValidator
{

	public static final String help_arg = "-help";
	public static final String log_level_arg = "-l";
	public static final String remote_arg = "-r";
	public static final String fixer_arg = "-fix";
	public static final String filter_arg = "-filter";
	public static final String fix_diagnose_arg = "-fix_diagnose";
	public static final String skip_arg = "-skip";
	public static final String low_memory_arg = "-lowmemory";
	public static final String write_de_arg = "-de";
	public static final String wrap_arg = "-wrap";
	public static FileType fileType = FileType.EMBL;
	public static final String file_format = "-f";
	public static final String prefix_token = "-prefix";
	public static final String min_gap_length_token = "-min_gap_length";
	public static final String assembly_token = "-assembly";
	public static final String transTable_token = "-table";
	public static final String version_token="-version";
	private static final String fileformatString = "File format(optional) Values:'embl','genbank','gff3','assembly'";
	private static final String log_levelString = "Log level(optional) Values : 0(Quiet), 1(Summary), 2(Verbose)";
	private static final String remoteString = "Remote, is this being run outside the EBI(optional)";
	private static final String fixString = "Fixes entries in input files. Stores input files in 'original_files' folder. (optional)";
	private static final String filterString = "-filter <prefix> Store entries in <prefix>_good.txt and <prefix>_bad.txt files in the working directory. Entries with errors are stored in the bad file and entries without errors are stored in the good file. (optional)(default :false)";
	private static final String fix_diagnoseString = "Creates 'diagnose' folder in the current directory with original entries in <filename>_origin file and the fixed entries in <filename>_fixed file. Only fixed entries will be stored in these files.(optional) ";
	private static final String skipString = "-skip <errorcode1>,<errorcode2>,... Ignore specified errors.(optional)(default:false) ";
	private static final String lowmemoryString = "Runs in low memory usage mode. Writes error logs but does not show message summary(optional)";
	private static final String wrapString = "Turns on line wrapping in flat file writing (optional) ";
	private static final String helpString = "Displays available options";
	private static final String prefix_string = "Adds prefix to report files";
	private static final String min_gap_length_string = "minimum gap length to generate assembly_gap/gap features, use assembly flag to add assembly_gap features";
	private static final String assembly_string = "genome assembly entries";
	private static final String version_string ="Displays implementation version of Jar";
	protected static final String EMBL_FORMAT = "embl";
	protected static final String GENBANK_FORMAT = "genbank";
	protected static final String GFF3_FORMAT = "gff3";
	protected static final String ASSEMBLY_FORMAT = "assembly";
	protected static final String FASTA_FORMAT = "fasta";
	public static final int LOG_LEVEL_ALL = 2;
	public static final int LOG_LEVEL_QUIET = 0;
	public static final int LOG_LEVEL_SUMMARY = 1;
	private static final int MESSAGE_FLATTEN_THRESHOLD = 5;
	/**
	 * the number of validation messages stored before we start worrying about
	 * memory and go into low memory mode
	 */
	private static final int LOW_MEMORY_THRESHOLD = 1000000;

	protected FlatFileReader reader = null;
	protected static String prefix;
	protected static int log_level = LOG_LEVEL_SUMMARY;// default
	protected static boolean remote = false;// default
	protected static boolean testMode = false;// default
	protected static boolean fixMode = false;// default
	protected static boolean fixDiagnoseMode = false;// default
	protected static boolean filterMode = false;// default
	protected static String filterPrefix = null;// default
	protected static boolean lowMemoryMode = false;// default
	protected static boolean writeDeMode = false;// default
	public static WrapType wrapType = WrapType.NO_WRAP;// default
	public static boolean lineCount = true;// default
	protected static int min_gap_length = 0;// default
	protected static boolean assembly = false;
	protected List<File> entryFiles;
	private ValidationPlan emblValidator;
	private ValidationPlan gff3Validator;
	private ValidationPlan gaValidator;
	protected boolean parseError;
	private int totalEntryCount = 0;
	private int fixCount = 0;
	private int failCount = 0;
	private int unchangedCount = 0;
	protected List<String> suppressedErrorCodes = new ArrayList<String>();
	protected List<ValidationResult> parseResults = new ArrayList<ValidationResult>();
	/**
	 * writers for separating good files and bad files - use needs to be
	 * specified in the arguments
	 */
	Writer goodFilesWriter;
	Writer badFilesWriter;

	/**
	 * writers for logging all errors, warnings etc
	 */
	Writer summaryWriter;
	Writer infoWriter;
	Writer errorWriter;
	Writer reportWriter;
	Writer fixWriter;
	/*
	 * database connection
	 */
	protected static Connection con = null;
/*
 * 
 */
	public static void main(String[] args) 
	{
		try
		{
			EnaValidator enaValidator = new EnaValidator();
			enaValidator.init(args,null);
			enaValidator.initValidator();
			int failedCount =enaValidator.validateFiles();
			
			if(failedCount==0)
				System.exit(0);
			if(failedCount>0)
				System.exit(3);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
			
		}
		finally
		{
			DbUtils.closeQuietly(con);

		}
	}

	/**
	 * Inits the validator.
	 * 
	 * @throws SQLException
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected void initValidator() throws SQLException, IOException
	{

		EmblEntryValidationPlanProperty emblEntryValidationPlanProperty = new EmblEntryValidationPlanProperty();
		emblEntryValidationPlanProperty.validationScope.set(ValidationScope.getScope(fileType));
		emblEntryValidationPlanProperty.isDevMode.set(testMode);
		emblEntryValidationPlanProperty.isFixMode.set(fixMode || fixDiagnoseMode);
		emblEntryValidationPlanProperty.minGapLength.set(min_gap_length);
		emblEntryValidationPlanProperty.isRemote.set(remote);
		emblEntryValidationPlanProperty.fileType.set(fileType);
		emblEntryValidationPlanProperty.enproConnection.set(con);
		emblValidator = new EmblEntryValidationPlan(emblEntryValidationPlanProperty);
		emblValidator.addMessageBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		emblValidator.addMessageBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		gff3Validator = new GFF3ValidationPlan(emblEntryValidationPlanProperty);
		gff3Validator.addMessageBundle(ValidationMessageManager.GFF3_VALIDATION_BUNDLE);
		gaValidator = new GenomeAssemblyValidationPlan(emblEntryValidationPlanProperty);
		gaValidator.addMessageBundle(ValidationMessageManager.GENOMEASSEMBLY_VALIDATION_BUNDLE);
		gaValidator.addMessageBundle(ValidationMessageManager.GENOMEASSEMBLY_FIXER_BUNDLE);
		initWriters();

	}

	/**
	 * Inits the arguments.
	 * @param args the args
	 * @param message the message
	 * @throws SQLException
	 * @throws IOException 
	 */
	protected void init(String[] args, String message) throws SQLException, IOException
	{
		Params params = new Params();
		JCommander jc = new JCommander(params);
		jc.setProgramName("ena_validator <files>");
		try
		{
		jc.parse(args);
		}catch(Exception e)
		{
			System.err.println("Invalid options");
			if (message == null)
			{  
				jc.usage();
				writeReturnCodes();
			}
			else
				System.out.println(message);
			    System.exit(2);
		}
		if (args.length == 0 || (args.length == 1 && params.help))
		{
			if (message == null)
			{
				jc.usage();
				writeReturnCodes();
				
			}
			else
				System.out.println(message);
			    System.exit(2);
		}
		if(params.version)
		{
			System.out.println(this.getClass().getPackage().getImplementationVersion());
			System.exit(0);
		}
		if (params.filenames.isEmpty())
		{
			System.err.println("Please give the filenames (or) directory with files to validate");
			jc.usage();
		}
		fileType = FileType.get(params.fileFormat);
		prefix = params.prefixString;
		log_level = params.log_level;
		remote = params.remote;
		lowMemoryMode = params.lowmemory;
		min_gap_length = params.min_gap_length;
		assembly = params.assembly;
		if (params.wrap)
		{
			wrapType = WrapType.EMBL_WRAP;
		}
		
		if (params.skip != null)
		{
			String suppressString = params.skip;
			suppressString = suppressString.replaceAll(	"\\(","");
			suppressString = suppressString.replaceAll(	"\\)","");
			String[] suppressArray = suppressString.split(",");
			suppressedErrorCodes = new ArrayList<String>(Arrays.asList(suppressArray));
		}
		fixMode = params.fix;
		writeDeMode = params.fixDe;
		fixDiagnoseMode = params.fix_diagnose;
		if (params.filter != null)
		{
			filterMode = true;
			filterPrefix = params.filter;

		}
		List<String> fileStrings = params.filenames;
		entryFiles = new ArrayList<File>();
		for (String fileString : fileStrings)
		{
			File fileHandle = new File(fileString);

			if (!fileHandle.exists())
			{
				printMessageLine(	"File " + fileHandle.getPath() + " does not exist - exiting",LOG_LEVEL_QUIET);
				return;
			}

			if (fileHandle.isDirectory())
			{
				printMessageLine(	"Directory found : " + fileHandle.getPath(),LOG_LEVEL_ALL);
				entryFiles.addAll(Arrays.asList(fileHandle.listFiles()));
			}
			else
			{
				printMessageLine(	"File found : " + fileHandle.getPath(),LOG_LEVEL_ALL);
				entryFiles.add(fileHandle);
			}

		}
		File formatFile=entryFiles.get(0);
		FileType fileFormat=FileUtils.getFileType(formatFile);
		if(fileFormat!=null)
			fileType=fileFormat;
	}

	/**
	 * Validate files.
	 * @throws ValidationEngineException
	 */
	private int validateFiles()
	{
		List<ValidationPlanResult> planResults = new ArrayList<ValidationPlanResult>();
		int parseErrorCount = 0;
		try
		{
			long timeIn = System.currentTimeMillis();

			if (filterMode && filterPrefix != null)
			{
				goodFilesWriter = new PrintWriter(filterPrefix + "_good.txt","UTF-8");
				badFilesWriter = new PrintWriter(filterPrefix + "_bad.txt","UTF-8");
			}

			for (File file : entryFiles)
			{
				List<ValidationPlanResult> results = validateFile(	file,errorWriter);
				planResults.addAll(results);
			}

			infoWriter.flush();
			errorWriter.flush();
			reportWriter.flush();
			fixWriter.flush();

			infoWriter.close();
			errorWriter.close();
			reportWriter.close();
			fixWriter.close();

			if (filterMode && filterPrefix != null)
			{
				badFilesWriter.flush();
				badFilesWriter.close();
				goodFilesWriter.flush();
				goodFilesWriter.close();
			}

			List<ValidationMessage<Origin>> messages = new ArrayList<ValidationMessage<Origin>>();

			for (ValidationPlanResult planResult : planResults)
			{
				messages.addAll(planResult.getMessages());
			}
			for (ValidationResult parseResult : parseResults)
			{
				messages.addAll(parseResult.getMessages());
				for (ValidationMessage message : parseResult.getMessages())
				{
					parseErrorCount++;
				}
			}

			/**
			 * will be built up to form the summary for the whole run
			 */
			String summaryLine = "";

			if (!planResults.isEmpty())
			{
				FlattenedMessageResult results = Utils.flattenMessages(messages,MESSAGE_FLATTEN_THRESHOLD);
				List<ValidationMessage> flattenedMessages = results.getFlattenedMessages();
				List<ValidationMessage> unFlattenedMessages = results.getUnFlattenedMessages();

				Collections.sort(	flattenedMessages,new ValidationMessageComparator());
				Collections.sort(	unFlattenedMessages,new ValidationMessageComparator());

				if (!flattenedMessages.isEmpty())
				{
					summaryLine = summaryLine.concat("\n\n***MESSAGES SUMMARY***");
					summaryLine = summaryLine.concat("\nCompressed messages (occurring more than "+ MESSAGE_FLATTEN_THRESHOLD + " times)");
					for (ValidationMessage message : flattenedMessages)
					{
						summaryLine = summaryLine.concat("\n"+ message.getSeverity());
						summaryLine = summaryLine.concat(": ");
						summaryLine = summaryLine.concat(message.getMessage());
						summaryLine = summaryLine.concat(" ("+ message.getMessageKey() + ") ");
					}
				}

				if (!unFlattenedMessages.isEmpty())
				{
					summaryLine = summaryLine.concat("\n\nMessages");
					for (ValidationMessage message : unFlattenedMessages)
					{

						summaryLine = summaryLine.concat("\n"+ message.getSeverity());
						summaryLine = summaryLine.concat(": ");
						summaryLine = summaryLine.concat(message.getMessage());
						summaryLine = summaryLine.concat(" ("+ message.getMessageKey() + ") ");
						for (Object origin : message.getOrigins())
						{
							StringWriter writer = new StringWriter();
							String text = ((Origin) origin).getOriginText();
							writer.write(text);
							summaryLine = summaryLine.concat(writer.toString());
							writer.close();
						}
					}
				}

				summaryLine = summaryLine.concat("\n\n***FILE SUMMARY***\n");
				List<FlattenedValidationPlanResult> flattenedPlanResults = Utils.flattenValidationPlans(planResults);
				for (FlattenedValidationPlanResult flattenedResult : flattenedPlanResults)
				{
					summaryLine = summaryLine.concat(flattenedResult.getFileName() + " - ");
					summaryLine = summaryLine.concat(flattenedResult.getEntryCount() + " entries, ");
					summaryLine = summaryLine.concat(flattenedResult.getFailedEntryCount() + " failed entries, ");
					summaryLine = summaryLine.concat((flattenedResult.getErrorCount() + parseErrorCount) + " errors, ");
					summaryLine = summaryLine.concat(flattenedResult.getFixCount() + " fixes, ");
					summaryLine = summaryLine.concat(flattenedResult.getWarningInfoCount() + " warnings & info");
					summaryLine = summaryLine.concat("\n");
				}
			}

			summaryLine = summaryLine.concat("\n*** SUMMARY***\n");

			summaryLine = summaryLine.concat("Parsing error:" + parseErrorCount+ "\n");
			summaryLine = summaryLine.concat("Fixed Entries:" + fixCount + "\n");
			summaryLine = summaryLine.concat("Failed Entries:" + failCount+ "\n");
			summaryLine = summaryLine.concat("Checked Entries:"+ totalEntryCount + "\n");
			summaryLine = summaryLine.concat("Unchanged Entries:"+ unchangedCount + "\n");
			long timeOut = System.currentTimeMillis();

			long timeToRun = (timeOut - timeIn) / 1000;

			summaryLine = summaryLine.concat("\n\nProcessed " + totalEntryCount+ " entries in " + timeToRun + " seconds.\n\n");
			printMessage(	summaryLine,LOG_LEVEL_SUMMARY);

			summaryWriter.write(summaryLine);
			summaryWriter.flush();
			summaryWriter.close();
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return failCount;
	}

	/**
	 * separate method to instantiate so unit tests can call this
	 * 
	 * @throws IOException
	 */
	protected void initWriters() throws IOException
	{
		String summarywriter = prefix == null ? "VAL_SUMMARY.txt" : prefix + "_" + "VAL_SUMMARY.txt";
		String infowriter = prefix == null ? "VAL_INFO.txt" : prefix + "_" + "VAL_INFO.txt";
		String errorwriter = prefix == null ? "VAL_ERROR.txt" : prefix + "_" + "VAL_ERROR.txt";
		String reportswriter = prefix == null ? "VAL_REPORTS.txt" : prefix + "_" + "VAL_REPORTS.txt";
		String fixwriter = prefix == null ? "VAL_FIXES.txt" : prefix + "_" + "VAL_FIXES.txt";
		summaryWriter = new PrintWriter(summarywriter,"UTF-8");
		infoWriter = new PrintWriter(infowriter,"UTF-8");
		errorWriter = new PrintWriter(errorwriter,"UTF-8");
		reportWriter = new PrintWriter(reportswriter,"UTF-8");
		fixWriter = new PrintWriter(fixwriter,"UTF-8");
	}

	/**
	 * Validate file.
	 * 
	 * @param file
	 *            the file
	 * @param writer
	 *            the writer
	 * @return the list of ValidationPlanResult
	 * @throws IOException 
	 * @throws ValidationEngineException
	 */
	private List<ValidationPlanResult> validateFile(File file, Writer writer) throws IOException
	{

		List<ValidationPlanResult> messages = new ArrayList<ValidationPlanResult>();
		ArrayList<Object> entryList = new ArrayList<Object>();
		BufferedReader fileReader = null; 

		try
		{
			fileReader= new BufferedReader(new FileReader(file));
			prepareReader(	fileReader,file.getName());
			List<ValidationPlanResult> results = validateEntriesInReader(writer,file,entryList);
			ValidationPlanResult entrySetResult = validateEntry(entryList);
			if (file != null)
			{
				entrySetResult.setTargetOrigin(file.getName());
			}
			for (ValidationPlanResult planResult : results)
			{
				messages.add(planResult);
			}
			if (!entrySetResult.isValid())
			{
				writeResultsToFile(entrySetResult);
				messages.add(entrySetResult);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally{
			if(fileReader!=null)
			fileReader.close();
		}

		return messages;
	}

	/**
	 * Prepare reader.
	 * 
	 * @param fileReader
	 *            the file reader
	 * @param fileId
	 *            the file name
	 */
	private void prepareReader(BufferedReader fileReader, String fileId)
	{

		switch (fileType)
		{
		case EMBL:
			EmblEntryReader emblReader = new EmblEntryReader(	fileReader,	EmblEntryReader.Format.EMBL_FORMAT,fileId);
			emblReader.setCheckBlockCounts(lineCount);
			reader = emblReader;
			break;
		case GENBANK:
			reader = new GenbankEntryReader(fileReader,fileId);
			break;
		case GFF3:
			reader = new GFF3FlatFileEntryReader(fileReader);
			break;
		case FASTA:
			reader = new FastaFileReader(new FastaLineReader(fileReader));
			break;
		case AGP:
			reader= new AGPFileReader(new AGPLineReader(fileReader));
			break;
		default:
			System.exit(0);
			break;
		}
	}

	/**
	 * Validate entries in reader.
	 * 
	 * @param writer
	 *            the writer
	 * @param targetFile
	 *            the target file
	 * @return the list of ValidationPlanResult
	 */
	protected List<ValidationPlanResult> validateEntriesInReader(Writer writer,File targetFile,ArrayList<Object> entryList)
	{

		List<ValidationPlanResult> validationResults = new ArrayList<ValidationPlanResult>();

		try
		{
			int currentEntryCount = 0;
			Object entry = getNextEntryFromReader(writer);
			if (entryList != null)
				entryList.add(entry);
			PrintWriter origFileWriter = null;
			PrintWriter fixedFileWriter = null;
			/**
			 * we write fixed files (if we are fixing) to a temp file, then
			 * replace the original file once we have finished reading the
			 * original file
			 */
			File fixedTempFile = new File("./fixedFile_temp.del");

			String diagnosisDir = "diagnosis";

			/**
			 * for files with errors, we split the original file rather than
			 * write it using the writer, this ensures we have an exact copy of
			 * the original.
			 */
			String[] splitEntries = null;
			String fileString = null;
			if (filterMode && filterPrefix != null && !FileType.GFF3.equals(fileType))
			{
				File originalFile = new File(targetFile.getPath());
				fileString = FileUtils.readFile(new FileInputStream(originalFile));
				fileString = fileString.trim();
				splitEntries = fileString.split("//");
			}

			if (fixDiagnoseMode)
			{
				File diagnoseDir = new File("./" + diagnosisDir);
				if (!diagnoseDir.exists())
				{
					diagnoseDir.mkdirs();
				}
				origFileWriter = new PrintWriter(new File(diagnosisDir + File.separator + targetFile.getName() + "_orig.txt"),"UTF-8");
				fixedFileWriter = new PrintWriter(new File(diagnosisDir+ File.separator + targetFile.getName() + "_fixed.txt"),"UTF-8");
			}
			else
				if (fixMode)
				{
					fixedFileWriter = new PrintWriter(fixedTempFile,"UTF-8");
					String originalDirString = prefix == null ? "./original_files" : prefix + "_" + "original_files";
					File originalsDir = new File(originalDirString);
					System.out.println(originalsDir.getAbsolutePath());

					/**
					 * make the original file dir if does not exist
					 */
					if (!originalsDir.exists())
					{
						originalsDir.mkdirs();
					}

					/**
					 * make copies of the original files only if copies have not
					 * been made already
					 */
					File copyOfOriginal = new File(originalDirString + File.separator + targetFile.getName());
					if (!copyOfOriginal.exists())
					{
						FileUtils.copyFile(	targetFile,copyOfOriginal);

					}

				}

			/**
			 * total number of validation messages created
			 */
			int totalCount = 0;
			while (entry != null)
			{

				String originalFileString = null;

				/**
				 * if in diagnosis mode - need to make a copy of the entry
				 * before it gets fixed. Write the entry out to a string and
				 * keep it for writing if any fixes take place. We use the file
				 * writer rather than make a copy of the original file because
				 * we want the file writer to sort the feature table - making
				 * diffs easier to see when comparing with the fixed files.
				 */
				if (fixDiagnoseMode)

				{
					StringWriter stringWriter = new StringWriter();
					if (FileType.EMBL.equals(fileType) || FileType.GENBANK.equals(fileType))
						{

							Entry originalEntry = (Entry) reader.getEntry();
							EntryWriter entryWriter;
							if (FileType.EMBL.equals(fileType)||FileType.FASTA.equals(fileType))
							{
								entryWriter = new EmblEntryWriter(originalEntry);
							}
							else
							{
								entryWriter = new GenbankEntryWriter(originalEntry);
							}

							entryWriter.setWrapType(wrapType);
							entryWriter.write(stringWriter);

						}
					stringWriter.close();
					originalFileString = stringWriter.toString();
				}
				/**
				 * then validate and fix the entry
				 */
				ValidationPlanResult planResult = validateEntry(entry);
				if (targetFile != null)
				{
					planResult.setTargetOrigin(targetFile.getName());// set the validation target to be the file name
				}

				if (!suppressedErrorCodes.isEmpty())
				{
					for (String warningCode : suppressedErrorCodes)
					{
						planResult.removeMessages(warningCode);
					}
				}

				/**
				 * if we are rewriting de lines - do so here
				 */
				if (writeDeMode && entry instanceof Entry)
				{
					ValidationResult deValidationResult = DEGenerator.writeDE((Entry) entry);
					planResult.append(deValidationResult);
				}
				/**
				 * if we are sorting the files into good and bad - do so here
				 */
				if (filterMode && filterPrefix != null && !(FileType.GFF3.equals(fileType)))
				{

					if (!planResult.getMessages(Severity.ERROR).isEmpty())
					{
						if (splitEntries.length == 1)
						{
							badFilesWriter.write(fileString);
							badFilesWriter.flush();
						}
						else
						{
							String entryString = splitEntries[currentEntryCount];
							entryString = entryString.concat("\n//\n");// as we will have lost the original when splitting
							badFilesWriter.write(entryString.trim());
							badFilesWriter.flush();
						}

					}
					else
					{
						EntryWriter entryWriter = null;
						switch(fileType)
						{
						case EMBL:
						case FASTA:
						case AGP:
							entryWriter = new EmblEntryWriter((Entry) entry);
							break;
						case GENBANK:
							entryWriter = new GenbankEntryWriter((Entry) entry);
							break;
						default:
							break;
						}
						if(entryWriter!=null)
						entryWriter.write(goodFilesWriter);
					}
				}

				/**
				 * if we are fixing in diagnosis mode, see if there were fixes.
				 * if so, write the original and fixed files into the diagnosis
				 * directory.
				 */
				if (fixDiagnoseMode)
				{
					if (!planResult.getMessages(Severity.FIX).isEmpty())
					{

						File diagnoseDir = new File("./" + diagnosisDir);
						if (!diagnoseDir.exists())
						{
							diagnoseDir.mkdirs();
						}

						if (originalFileString != null)
						{
							origFileWriter.write(originalFileString);
						}
							EntryWriter entryWriter;
							if (FileType.EMBL.equals(fileType)||FileType.FASTA.equals(fileType))
							{
								entryWriter = new EmblEntryWriter((Entry) entry);
							}
							else
							{
								entryWriter = new GenbankEntryWriter((Entry) entry);
							}
							entryWriter.setWrapType(wrapType);
							Entry Egentry = (Entry) entry;
							if (!Egentry.isDelete())
							{
								entryWriter.write(fixedFileWriter);
							}
						}
					
				}
				else
					if (fixMode)
					{
						EntryWriter entryWriter = null;
						Entry Egentry = (Entry) entry;
						switch(fileType)
						{
						case EMBL:
						case FASTA:
						case AGP:
							if (!Egentry.isDelete())
								entryWriter = new EmblEntryWriter((Entry) entry);
							break;
							
						case GENBANK:
								entryWriter = new GenbankEntryWriter((Entry) entry);
								break;
						default:
							break;
								
								
							}
						if(entryWriter!=null)
						{
						entryWriter.setWrapType(wrapType);
						entryWriter.write(fixedFileWriter);
						}
							
					}
					

				if (planResult.getMessages(Severity.FIX).size() > 0)
				{
					fixCount++;
				}
				if (planResult.getMessages(Severity.ERROR).size() > 0)
				{
					failCount++;
				}
				if (planResult.getMessages(Severity.FIX).size() == 0)
				{
					unchangedCount++;
				}

				/**
				 * writes to the report files
				 */
				writeResultsToFile(planResult);

				if (!lowMemoryMode)
				{
					validationResults.add(planResult);
					totalCount += planResult.count();
				}

				/**
				 * we go into low memory mode automatically past a certain
				 * threshold
				 */
				if (totalCount > LOW_MEMORY_THRESHOLD && !lowMemoryMode)
				{
					System.out.println("\n\nEntering low memory mode\n\n");
					lowMemoryMode = true;
					validationResults.clear();
					System.gc();
				}

				entry = getNextEntryFromReader(writer);
				if (entryList != null && entry != null)
					entryList.add((Entry) entry);
				totalEntryCount++;
				currentEntryCount++;
			}

			if (origFileWriter != null)
			{
				origFileWriter.close();
			}

			if (fixedFileWriter != null)
			{
				fixedFileWriter.close();

				if (fixMode)
				{
	    			FileUtils.copyFile(	fixedTempFile,targetFile);
					fixedTempFile.delete();
				}
			}

		}
		catch (Throwable e)
		{
			e.printStackTrace();
			printMessageLine(	"VALIDATOR FAILING",LOG_LEVEL_QUIET);
			System.exit(1);
		}

		return validationResults;
	}

	/**
	 * Write results to file.
	 * 
	 * @param planResult  the plan result
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeResultsToFile(ValidationPlanResult planResult) throws IOException
	{

		/**
		 * first set any report messages (probably exceptional that the
		 * translation report needs to get set outside the embl-api-core package
		 * due to the need for embl-ff writers
		 **/
		for (ValidationResult result : planResult.getResults())
		{
			if (result instanceof ExtendedResult)
			{
				ExtendedResult extendedResult = (ExtendedResult) result;
				if (extendedResult.getExtension() instanceof CdsFeatureTranslationCheck.TranslationReportInfo)
				{
				FlatFileValidations.setCDSTranslationReport((ExtendedResult<CdsFeatureTranslationCheck.TranslationReportInfo>) extendedResult);
				}
			}
		}

		// then look at writing the files

		for (ValidationResult result : planResult.getResults())
		{
			result.writeMessages(errorWriter,Severity.ERROR,planResult.getTargetOrigin());
			result.writeMessages(infoWriter,Severity.INFO,planResult.getTargetOrigin());
			result.writeMessages(infoWriter,Severity.WARNING,planResult.getTargetOrigin());
			result.writeMessages(fixWriter,Severity.FIX,planResult.getTargetOrigin());
			errorWriter.flush();
			infoWriter.flush();
			fixWriter.flush();
		}

		for (ValidationResult result : planResult.getResults())
		{
			if (result.isHasReportMessage())
			{
				result.setWriteReportMessage(true);// turn report writing on for
													// this writer
				result.writeMessages(reportWriter);
			}
		}
	}

	private void printMessageLine(Object message, int threshold)
	{
		if (log_level >= threshold)
		{
			System.err.println(message);
		}
	}

	private void printMessage(Object message, int threshold)
	{
		if (log_level >= threshold)
		{
			System.err.print(message);
		}
	}

	// *******THE METHODS ARE EXPOSED TO SUBCLASSES - ALLOW RETRIEVAL OF ENTRIES
	// FROM CURRENT READER
	// AND VALIDATION OF ENTRIES***///

	protected ValidationPlanResult validateEntry(Object entry) throws ValidationEngineException
	{

		ValidationPlanResult planResult = new ValidationPlanResult();

		String id = entry.toString();
		if (id == null)
		{
			id = "*";
		}

		printMessage(	id + ", ",LOG_LEVEL_ALL);
		switch(fileType)
		{
		case EMBL:
		case GENBANK:
		case FASTA:
			planResult = emblValidator.execute(entry); 
			break;
		case GFF3:
			planResult = gff3Validator.execute(entry);
			break;
		default:
			break;
						
		}
		return planResult;
	}

	/**
	 * Gets the next entry from reader.
	 * 
	 * @param writer
	 *            the writer
	 * @return the next entry from reader
	 */
	protected Object getNextEntryFromReader(Writer writer)
	{
		try
		{
			parseError = false;
			ValidationResult parseResult = reader.read();
			if (parseResult.getMessages("FT.10").size() >= 1 && (fixMode || fixDiagnoseMode))
			{
				parseResult.removeMessage("FT.10"); // writer fixes automatically if quotes are not given for qualifier value in fix/fix_diagnose mode.
			}

			if (parseResult.count() != 0)
			{
				parseResults.add(parseResult);
				writer.write("\n");
				for (ValidationMessage<Origin> validationMessage : parseResult.getMessages())
				{
					validationMessage.writeMessage(writer);
				}
				parseError = true;
			}

			if (!reader.isEntry())
			{
				return null;
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return reader.getEntry();
	}

	/**
	 * The Class Params.
	 */
	static class Params
	{
		@Parameter(names = help_arg, description = helpString)
		private boolean help = false;
		@Parameter(names = log_level_arg, description = log_levelString)
		private int log_level = 1;
		@Parameter(names = remote_arg, description = remoteString)
		private boolean remote = false;
		@Parameter(names = fixer_arg, description = fixString)
		private boolean fix = false;
		@Parameter(	names = write_de_arg, description = "Additional Fix :Adds/Fixes DE line(optional)")
		private boolean fixDe = false;
		@Parameter(names = filter_arg, description = filterString)
		private String filter;
		@Parameter(names = fix_diagnose_arg, description = fix_diagnoseString)
		private boolean fix_diagnose = false;
		@Parameter(names = skip_arg, description = skipString)
		private String skip;
		@Parameter(names = low_memory_arg, description = lowmemoryString)
		private boolean lowmemory = false;
		@Parameter(names = wrap_arg, description = wrapString)
		private boolean wrap = false;
		@Parameter(names = file_format, description = fileformatString)
		private String fileFormat = EMBL_FORMAT;
		@Parameter(names = prefix_token, description = prefix_string)
		private String prefixString;
		@Parameter(	names = min_gap_length_token,description = min_gap_length_string)
		private int min_gap_length;
		@Parameter(names = assembly_token, description = assembly_string)
		private boolean assembly = false;
		@Parameter(names = version_token, description = version_string)
		private boolean version;
		@Parameter()
		List<String> filenames = new ArrayList<String>();
	}

	private void writeReturnCodes() {

		HashMap<Integer,String> returnCodeMap= new HashMap<Integer, String>();
		returnCodeMap.put(0,"SUCCESS");
		returnCodeMap.put(1,"INTERNAL ERROR");
		returnCodeMap.put(2, "INVALID INPUT");
		returnCodeMap.put(3, "VALIDATION ERROR");
		
	    System.out.println("Return Codes: "+returnCodeMap.toString());
	   }
}
