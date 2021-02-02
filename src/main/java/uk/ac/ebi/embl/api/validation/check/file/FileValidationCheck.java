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
package uk.ac.ebi.embl.api.validation.check.file;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.lang3.StringUtils;
import org.mapdb.DB;
import uk.ac.ebi.embl.api.contant.AnalysisType;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.ChromosomeEntry;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.reference.*;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.ValidationEngineException.ReportErrorType;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.api.validation.report.DefaultSubmissionReporter;
import uk.ac.ebi.embl.api.validation.report.SubmissionReporter;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.ReferenceReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

 
public abstract class FileValidationCheck {

	protected SubmissionOptions options =null;
	protected SubmissionReporter reporter=null;
	public static final String REPORT_FILE_SUFFIX = ".report";
	public static HashMap<String, ChromosomeEntry> chromosomeNameQualifiers = new HashMap<>();
	public static List<String> chromosomeNames =new ArrayList<String>();
	public static Map<String,AssemblySequenceInfo> sequenceInfo = new LinkedHashMap<>();
	public static Map<String,AssemblySequenceInfo> fastaInfo = new LinkedHashMap<>();
	public static Map<String,AssemblySequenceInfo> flatfileInfo = new LinkedHashMap<>();
	public static Map<String,AssemblySequenceInfo> agpInfo = new LinkedHashMap<>();
	public static List<String> duplicateEntryNames = new ArrayList<String>();
	public static HashSet<String> entryNames = new HashSet<String>();
	public static Set<String> agpEntryNames =new HashSet<>();
	public static Set<String> unplacedEntryNames =new HashSet<>();
	public static Set<String> unlocalisedEntryNames = new HashSet<>();
	protected ConcurrentMap<String, AtomicLong> messageStats = null;
	protected static Entry masterEntry =null;
	protected TaxonHelper taxonHelper= null;
	protected PrintWriter fixedFileWriter =null;
	private static boolean hasAnnotationOnlyFlatfile = false;
	private static boolean hasAgp = false;
	public static final String masterFileName = "master.dat";
	private  DB sequenceDB = null;
	private DB contigDB =null;
	protected static int sequenceCount = 0;

	public FileValidationCheck(SubmissionOptions options) {
		this.options =options;
		messageStats =  new ConcurrentHashMap<String, AtomicLong>();
		taxonHelper =new TaxonHelperImpl();
		ValidationMessageManager.addBundle(ValidationMessageManager.GENOMEASSEMBLY_VALIDATION_BUNDLE);	
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);		
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		if(!EntryReader.getBlockCounter().isEmpty())
            EntryReader.getBlockCounter().clear();
         if(!EntryReader.getSkipTagCounter().isEmpty())
             EntryReader.getSkipTagCounter().clear();
	}
	public abstract ValidationPlanResult check(SubmissionFile file) throws ValidationEngineException;
	public abstract ValidationPlanResult check() throws ValidationEngineException ;

	protected SubmissionOptions getOptions() {
		return options;
	}

	public static void setSequenceCount(int seqCount) {
		sequenceCount = seqCount;
	}

	public static int getSequenceCount() {
		return sequenceCount;
	}


	protected AnalysisType getAnalysisType()
	{
		switch(getOptions().context.get())
		{
		case transcriptome:
			return AnalysisType.TRANSCRIPTOME_ASSEMBLY;
		case genome:
			return AnalysisType.SEQUENCE_ASSEMBLY;
		default :
			return null;
		}
	}

	public SubmissionReporter getReporter()
	{
		HashSet<Severity> severity = new HashSet<>();
		severity.add(Severity.ERROR);
		if(reporter==null)
			return new DefaultSubmissionReporter(severity);
		return reporter;
	}


	public Path getReportFile(SubmissionFile submissionFile)  {
		Path reportFilePath = null;

		if (submissionFile.getReportFile() != null)
			reportFilePath = submissionFile.getReportFile().toPath();
		else if (getOptions().reportDir.isPresent()) {
			reportFilePath = Paths.get(getOptions().reportDir.get(), submissionFile.getFile().getName() + REPORT_FILE_SUFFIX);
		}

		return reportFilePath;
	}

	protected void clearReportFile(Path reportfilePath) throws IOException
	{
		if(reportfilePath!=null)
			Files.deleteIfExists(reportfilePath);
	}
	protected ValidationScope getValidationScope(String entryName1) throws ValidationEngineException
	{
		switch (options.context.get()) {
			case genome:
				final String entryNameUpper = entryName1.toUpperCase();
				if (chromosomeNameQualifiers.keySet().stream().anyMatch(s -> s.equalsIgnoreCase(entryNameUpper))) {
					if(unlocalisedEntryNames.contains(entryNameUpper) ) {
						throw new ValidationEngineException("Sequence can not exist in both chromosome and unlocalised list", ReportErrorType.VALIDATION_ERROR);
					}
					return ValidationScope.ASSEMBLY_CHROMOSOME;
				}

				if (agpEntryNames.contains(entryNameUpper)) {
					if (!unlocalisedEntryNames.contains(entryNameUpper)) {

						unplacedEntryNames.add(entryNameUpper);
					}
					return ValidationScope.ASSEMBLY_SCAFFOLD;
				} else {
					return ValidationScope.ASSEMBLY_CONTIG;
				}
			case transcriptome:
				return ValidationScope.ASSEMBLY_TRANSCRIPTOME;
			case sequence:
				return ValidationScope.EMBL_TEMPLATE;
			default:
				return null;
		}
	}
	
	protected void addEntryName(String entryName)
	{
		if(!entryNames.add(entryName.toUpperCase()))
		{
			duplicateEntryNames.add(entryName);
		}
	}

	protected int getAssemblyLevel(ValidationScope scope)
	{
		int assemblyLevel=-1;

		switch(scope)
		{
			case ASSEMBLY_CHROMOSOME:
				assemblyLevel=2;
				break;
			case ASSEMBLY_SCAFFOLD:
				assemblyLevel=1;
				break;
			case ASSEMBLY_CONTIG:
				assemblyLevel=0;
				break;
			default:
				break;
		}
		return assemblyLevel;
	}

	public void validateDuplicateEntryNames() throws ValidationEngineException
	{
		if(duplicateEntryNames.size()>0)
		{
			throw new ValidationEngineException("Entry names are duplicated in assembly : "+ String.join(",",duplicateEntryNames),ReportErrorType.VALIDATION_ERROR);
		}
	}

	public void validateSequencelessChromosomes() throws ValidationEngineException
	{
		List<String> sequencelessChromosomes =new ArrayList<String>();
		for(String chromosomeName: chromosomeNameQualifiers.keySet())
			{
				if(!hasSequenceInfo(chromosomeName))
				{
					sequencelessChromosomes.add(chromosomeName);
				}
			}
		if(sequencelessChromosomes.size()>0)
		throw new ValidationEngineException("Sequenceless chromosomes are not allowed in assembly : "+String.join(",",sequencelessChromosomes),ReportErrorType.VALIDATION_ERROR);

	}
	
	
	public String getDataclass(String entryName)
	{
		String dataclass=null;
		switch(getOptions().context.get())
		{
		case genome :
			switch(getOptions().getEntryValidationPlanProperty().fileType.get())
			{
			case FASTA:
				switch(getOptions().getEntryValidationPlanProperty().validationScope.get())
				{
				case ASSEMBLY_CONTIG :
					dataclass= Entry.WGS_DATACLASS;
					break;
				case ASSEMBLY_CHROMOSOME :
					dataclass= Entry.STD_DATACLASS;
					break;
				default:
					break;
				}
				break;
			case AGP:
				dataclass= Entry.CON_DATACLASS;
				break;
			case EMBL:
				if(entryName!=null&&agpEntryNames.contains(entryName.toUpperCase()))
					dataclass= Entry.CON_DATACLASS;
				switch(getOptions().getEntryValidationPlanProperty().validationScope.get())
				{
				case ASSEMBLY_CONTIG :
					dataclass= Entry.WGS_DATACLASS;
					break;
				case ASSEMBLY_CHROMOSOME :
					dataclass= Entry.STD_DATACLASS;
					break;
				default:
					break;
				}
				break;
			case MASTER :
				dataclass = Entry.SET_DATACLASS;
				break;

			default:
				break;
			}
			break;
		case transcriptome:
			dataclass= Entry.TSA_DATACLASS;
			break;
		default:
			break;

		}
		return dataclass;
	}

	public ConcurrentMap<String,AtomicLong> getMessageStats()
	{
		return messageStats;
	}

	void addMessageKey(ValidationMessage<Origin> message) {
		if (messageStats.putIfAbsent(message.getMessageKey(), new AtomicLong(1)) != null)
			messageStats.get(message.getMessageKey()).incrementAndGet();
	}

	void addMessageKeys(Collection<ValidationMessage<Origin>> result) {
		for(ValidationMessage message: result)
		{
			if(messageStats.putIfAbsent(message.getMessageKey(), new AtomicLong(1))!=null)
				messageStats.get(message.getMessageKey()).incrementAndGet();
		}
	}

	protected void appendHeader(Entry entry) throws ValidationEngineException
	{

		if(Context.sequence == getOptions().context.get())
		{
			try
			{
				addTemplateHeader(entry);
				return;
			}catch(Exception e)
			{
				throw new ValidationEngineException(e.getMessage(), e);
			}
		}
		if(masterEntry == null)
		{
			throw new ValidationEngineException("Master entry must to validate sequences", ReportErrorType.VALIDATION_ERROR);
		}
		if(entry==null)
			return ;
		entry.removeReferences();
		entry.removeProjectAccessions();
		entry.addReferences(masterEntry.getReferences());
		entry.addProjectAccessions(masterEntry.getProjectAccessions());
		entry.addXRefs(masterEntry.getXRefs());
		entry.setComment(masterEntry.getComment());
		//add chromosome qualifiers to entry

        if(Context.sequence == options.context.get()) {
            if(entry.getDataClass() == null || entry.getDataClass().isEmpty())
                entry.setDataClass(Entry.STD_DATACLASS);
        } else {
            entry.setDataClass(getDataclass(entry.getSubmitterAccession()));
        }
		addSourceQualifiers(entry);
		entry.getSequence().setMoleculeType(masterEntry.getSequence().getMoleculeType());
   		if (entry.getSequence().getTopology() == null) {
      		entry.getSequence().setTopology(masterEntry.getSequence().getTopology());
		}
		if(entry.getSubmitterAccession()!=null&&options.context.get()==Context.genome)
		{

			Utils.setssemblyLevelDescription(masterEntry.getDescription().getText(), 
					ValidationScope.ASSEMBLY_CONTIG==getOptions().getEntryValidationPlanProperty().validationScope.get() ? 0 
							: ValidationScope.ASSEMBLY_SCAFFOLD==getOptions().getEntryValidationPlanProperty().validationScope.get() ? 1 
									: ValidationScope.ASSEMBLY_CHROMOSOME==getOptions().getEntryValidationPlanProperty().validationScope.get() ? 2 :null,
											entry);
		}
		if(Context.transcriptome == options.context.get()) {
			entry.getSequence().setVersion(1);
			Order<Location> order = new Order<Location>();
			order.addLocation(new LocationFactory().createLocalRange(1l, entry.getSequence().getLength()));
			entry.getPrimarySourceFeature().setLocations(order);
			if(entry.getSubmitterAccession() != null) {
				entry.setDescription(new Text("TSA: " + entry.getPrimarySourceFeature().getScientificName() + " " + entry.getSubmitterAccession()));
			}

		}

	}

  	protected Sequence.Topology getTopology(String submitterAccn)  {
		if (submitterAccn != null && getOptions().getEntryValidationPlanProperty().validationScope.get()
				== ValidationScope.ASSEMBLY_CHROMOSOME
			&& chromosomeNameQualifiers != null) {
			return chromosomeNameQualifiers.get(submitterAccn.toUpperCase()).getTopology();
		}
		return null;
  	}

	protected PrintWriter getFixedFileWriter(SubmissionFile submissionFile) throws IOException
	{
		if(submissionFile.createFixedFile()&&fixedFileWriter==null)
			fixedFileWriter= new PrintWriter(Files.newBufferedWriter(Paths.get(submissionFile.getFixedFile().getAbsolutePath()), StandardCharsets.UTF_8));
		return fixedFileWriter;
	}

	protected void collectContigInfo(Entry entry) throws ValidationEngineException {
		try {
			if (entry.getSubmitterAccession() == null)
				entry.setSubmitterAccession(entry.getPrimaryAccession());
			if (entry.getSubmitterAccession() == null)
				throw new ValidationEngineException("Submitter accession missing for an entry", ReportErrorType.VALIDATION_ERROR);
			if (!agpEntryNames.isEmpty() && agpEntryNames.contains(entry.getSubmitterAccession().toUpperCase()))
				return;
			if (entry.getSequence() == null)
				return;
			if (getContigDB() != null && entry.getSubmitterAccession() != null) {
				ConcurrentMap<String, List<AgpRow>> contigMap = (ConcurrentMap<String, List<AgpRow>>) getContigDB().hashMap("map").createOrOpen();
				List<AgpRow> agpRows = contigMap.get(entry.getSubmitterAccession().toLowerCase());
				if (agpRows != null) {
					for (AgpRow agpRow : agpRows) {
						agpRow.setSequence(entry.getSequence().getSequenceByte(agpRow.getComponent_beg(), agpRow.getComponent_end()));
					}
					contigMap.put(entry.getSubmitterAccession().toLowerCase(), agpRows);
				}
			}

		} catch (ValidationEngineException e) {
			if (getContigDB() != null)
				getContigDB().close();
			throw e;
		}
	}

	protected void addAgpEntryName(String entryName) throws ValidationEngineException {
			 if(!agpEntryNames.add(entryName)) {
			 	throw new ValidationEngineException( " Object name should be unique in AGP files."+ entryName,ReportErrorType.VALIDATION_ERROR);
			 }
	}
	private void addSourceQualifiers(Entry entry)
	{
		Qualifier subSeqIdQual = null;
		if(entry.getPrimarySourceFeature() == null)
		{
			FeatureFactory featureFactory=new FeatureFactory();
			Order<Location>featureLocation = new Order<Location>();
			LocationFactory locationFactory=new LocationFactory();
			if(entry.getSequence()!=null)
			{
				featureLocation.addLocation(locationFactory.createLocalRange(1l, entry.getSequence().getLength()));
				entry.getSequence().setMoleculeType(masterEntry.getSequence().getMoleculeType());
			}
			SourceFeature sourceFeature=featureFactory.createSourceFeature();
			sourceFeature.setLocations(featureLocation);
			entry.addFeature(sourceFeature);
		} else {
			subSeqIdQual = entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME);
		}

		SourceFeature source=null;
		if(masterEntry!=null&&masterEntry.getPrimarySourceFeature()!=null)
		{
			source=masterEntry.getPrimarySourceFeature();
		}

		entry.getPrimarySourceFeature().removeAllQualifiers();
		if(options.context.get()==Context.genome)
		{

			if(entry.getSubmitterAccession()!=null)
			{
				Optional<java.util.Map.Entry<String, ChromosomeEntry>> chromosomeQualifierMap =
					chromosomeNameQualifiers.entrySet().stream()
						.filter(x -> x.getKey().equalsIgnoreCase(entry.getSubmitterAccession()))
						.findFirst();
				if(chromosomeQualifierMap.isPresent())
				{	
					List<Qualifier> chromosomeQualifiers = chromosomeQualifierMap.get().getValue().setAndGetQualifiers(taxonHelper.isChildOf(
							masterEntry
									.getPrimarySourceFeature()
									.getSingleQualifierValue(Qualifier.ORGANISM_QUALIFIER_NAME),
							"Viruses"));

					if(chromosomeQualifiers!=null)
					{
						for (Qualifier chromosomeQualifier : chromosomeQualifiers)
						{
							entry.getPrimarySourceFeature().addQualifier(chromosomeQualifier);

						}
					}
				}
			}
			if(Entry.WGS_DATACLASS.equals(entry.getDataClass()))
			{
				Qualifier noteQualifier =(new QualifierFactory()).createQualifier(Qualifier.NOTE_QUALIFIER_NAME,"contig: "+entry.getSubmitterAccession());
				entry.getPrimarySourceFeature().addQualifier(noteQualifier);
			}
		}
		if(source!=null)
		{
			for (Qualifier sourceQualifier : source.getQualifiers())
			{
				entry.getPrimarySourceFeature().addQualifier(sourceQualifier);
			}
		}
		if( subSeqIdQual != null ) {
			entry.getPrimarySourceFeature().addQualifier(subSeqIdQual);
		}

	}

	protected void addTemplateHeader(Entry entry) throws UnsupportedEncodingException, SQLException, ValidationEngineException {
		entry.removeReferences();
		entry.removeProjectAccessions();
		entry.addProjectAccession(new Text(options.getProjectId()));
		entry.getSequence().setVersion(1);
		entry.setStatus(Entry.Status.PRIVATE);
   		if (entry.getSecondaryAccessions() != null && !entry.getSecondaryAccessions().isEmpty())
      		entry.getSecondaryAccessions().clear();

		if (options.assemblyInfoEntry.isPresent()
				&& StringUtils.isNotBlank(options.assemblyInfoEntry.get().getAddress())
				&& StringUtils.isNotBlank(options.assemblyInfoEntry.get().getAuthors())) {
			entry.removeReferences();
			Reference reference = new ReferenceReader().getReference(options.assemblyInfoEntry.get().getAuthors(),
					options.assemblyInfoEntry.get().getAddress(), options.assemblyInfoEntry.get().getDate());
			entry.addReference(reference);
		} else {

			if (!getOptions().isWebinCLI) {
				EraproDAOUtils eraProDao = new EraproDAOUtilsImpl(options.eraproConnection.get());
				Reference reference =  eraProDao.getReference(entry, options.analysisId.get(), AnalysisType.SEQUENCE_FLATFILE);
				if(reference == null) {
					reference = eraProDao.getSubmitterReference(options.analysisId.get());
				}
				entry.addReference(reference);
				return;
			}
			ReferenceFactory referenceFactory = new ReferenceFactory();
			Reference reference = referenceFactory.createReference();
			Publication publication = new Publication();
			Person person = referenceFactory.createPerson("CLELAND");
			publication.addAuthor(person);
			reference.setAuthorExists(true);
			Submission submission = referenceFactory.createSubmission(publication);
			submission.setSubmitterAddress(", The European Bioinformatics Institute (EMBL-EBI), Wellcome Genome Campus, CB10 1SD, United Kingdom");
			submission.setDay(Calendar.getInstance().getTime());
			publication = submission;
			reference.setPublication(publication);
			reference.setLocationExists(true);
			reference.setReferenceNumber(1);
			entry.addReference(reference);
		}
	}

	protected BufferedReader 
	getBufferedReader( File file ) throws FileNotFoundException, IOException 
	{
		if( file.getName().matches( "^.+\\.gz$" ) || file.getName().matches( "^.+\\.gzip$" ) ) 
		{
			GZIPInputStream gzip = new GZIPInputStream( new FileInputStream( file ) );
			return new BufferedReader( new InputStreamReader( gzip ) );

		} else if( file.getName().matches( "^.+\\.bz2$" ) || file.getName().matches( "^.+\\.bzip2$" ) ) 
		{
			BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream( new FileInputStream( file ) );
			return new BufferedReader( new InputStreamReader( bzIn ) );

		} else 
		{
			return new BufferedReader( new FileReader(file ) );
		}
	}


	public  boolean isGenbank(File f) throws ValidationEngineException {
		try(BufferedReader br = getBufferedReader(f)) {
			String line;
			if((line = br.readLine()) != null){
				return line.trim().startsWith("LOCUS");
			}
		} catch (Exception e) {
			throw new ValidationEngineException("Could not read the file :"+f, e);
		}
		return false;
	}

	public static boolean isHasAnnotationOnlyFlatfile() {
		return hasAnnotationOnlyFlatfile;
	}
	public static void setHasAnnotationOnlyFlatfile(boolean annotationOnlyFile) {
		hasAnnotationOnlyFlatfile = annotationOnlyFile;
	}

	public void setSequenceDB(DB sequenceDB)
	{
		this.sequenceDB=sequenceDB;
	}

	public DB getSequenceDB()
	{
		return this.sequenceDB;
	}
	public void closeDB(DB ... dbs) {
		for(DB db: dbs)
		{
			if(db != null)
				db.close();
		}
	}
	public DB getContigDB() {
		return contigDB;
	}
	public void setContigDB(DB contigDB) {
		this.contigDB = contigDB;
	}

	ValidationResult reportError(Path reportFile, String fileType) {
		ValidationResult result = new ValidationResult();
		ValidationMessage<Origin> message = FlatFileValidations.message(Severity.ERROR, "InvalidFileFormat", fileType);
		addMessageKey(message);
		result.append(message);
		if (getOptions().reportDir.isPresent())
			getReporter().writeToFile(reportFile, result);
		return result;
	}


	protected boolean validateFileFormat(File file,uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType fileType) throws IOException
	{  
		String flat_file_token= "ID";
		String fasta_file_token=">";
		String agp_file_comment_token="#";
		String spaceregex = "\\s+";
		int agp_number_of_columns = 9;
		int chromosome_min_number_of_columns = 3;
		int chromosome_max_number_of_columns = 4;
		String line=null;
		
		try(BufferedReader  fileReader=getBufferedReader(file))
		{
			int count=1;
		while(line==null||line.isEmpty())
   	   {
			count++;
   		 line=fileReader.readLine();
   		 if(count>100)
   			 return false;
       }
        
		switch(fileType)
         {
         case FLATFILE:
         case ANNOTATION_ONLY_FLATFILE:
         case MASTER:
        	 if(!line.startsWith(flat_file_token))
        	 { 
        	   return false;
        	 }
             break;
         case FASTA:
        	 if(!line.startsWith(fasta_file_token))
        	 { 
        		 return false;
        	 }
             break;
         case AGP:
        	 if(!line.startsWith(agp_file_comment_token)&&line.split(spaceregex).length!=agp_number_of_columns)
        	 { 
        	   return false;
        	 }
             break;
         case CHROMOSOME_LIST:
        	 if(line.split(spaceregex).length<chromosome_min_number_of_columns||line.split(spaceregex).length>chromosome_max_number_of_columns)
        	 { 
        		 return false;
        	 }
             break;
           default:
             return false;
         }
		}
	
		return true;
		}
	

	public boolean hasSequenceInfo(String entryName)
	{
		return sequenceInfo.entrySet().stream().anyMatch(x->x.getKey().equalsIgnoreCase(entryName));

	}

	public static boolean isHasAgp() {
		return hasAgp;
	}

	public static void setHasAgp(boolean hasAgpFiles) {
		hasAgp = hasAgpFiles;
	}
}
