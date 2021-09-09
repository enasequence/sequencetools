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

import org.apache.commons.lang3.StringUtils;
import org.mapdb.DB;
import uk.ac.ebi.embl.api.contant.AnalysisType;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
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
import uk.ac.ebi.embl.api.validation.helper.ReferenceUtils;
import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.api.validation.report.DefaultSubmissionReporter;
import uk.ac.ebi.embl.api.validation.report.SubmissionReporter;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.common.CommonUtil;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.reader.genbank.GenbankEntryReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblReducedFlatFileWriter;

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


public abstract class FileValidationCheck {
	final static int MAX_SEQUENCE_COUNT_FOR_TEMPLATE = 30000;

	public static final String REPORT_FILE_SUFFIX = ".report";

	public static final String masterFileName = "master.dat";

	protected SubmissionOptions options =null;
	protected SubmissionReporter reporter=null;

	protected ConcurrentMap<String, AtomicLong> messageStats = null;

	protected TaxonHelper taxonHelper= null;
	protected PrintWriter fixedFileWriter =null;

	protected SharedInfo sharedInfo;

	private  DB annotationDB = null;
	private DB contigDB =null;
	public static final String contigFileName = "contigs.reduced.tmp";
	public static final String scaffoldFileName = "scaffolds.reduced.tmp";
	public static final String chromosomeFileName = "chromosome.flatfile.tmp";

	public FileValidationCheck(SubmissionOptions options, SharedInfo sharedInfo) {
		this.options =options;

		this.sharedInfo = sharedInfo;

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

	public abstract ValidationResult check(SubmissionFile file) throws ValidationEngineException;
	public abstract ValidationResult check() throws ValidationEngineException ;

	protected SubmissionOptions getOptions() {
		return options;
	}

	public SubmissionReporter getReporter()
	{
		HashSet<Severity> severity = new HashSet<>();
		severity.add(Severity.ERROR);
		if(reporter==null)
			return new DefaultSubmissionReporter(severity);
		return reporter;
	}


	public Path getReportFile(SubmissionFile submissionFile) {
		Path reportfilePath = null;

		if (submissionFile.getReportFile() != null)
			reportfilePath = submissionFile.getReportFile().toPath();
		else if (getOptions().reportDir.isPresent())
			reportfilePath = Paths.get(getOptions().reportDir.get(), submissionFile.getFile().getName() + REPORT_FILE_SUFFIX);

		return reportfilePath;
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
				if (sharedInfo.chromosomeNameQualifiers.keySet().stream().anyMatch(s -> s.equalsIgnoreCase(entryNameUpper))) {
					if(sharedInfo.unlocalisedEntryNames.contains(entryNameUpper) ) {
						throw new ValidationEngineException("Sequence can not exist in both chromosome and unlocalised list");
					}
					return ValidationScope.ASSEMBLY_CHROMOSOME;
				}

				if (sharedInfo.agpEntryNames.contains(entryNameUpper)) {
					if (!sharedInfo.unlocalisedEntryNames.contains(entryNameUpper)) {

						sharedInfo.unplacedEntryNames.add(entryNameUpper);
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
		if(!sharedInfo.entryNames.add(entryName.toUpperCase()))
		{
			sharedInfo.duplicateEntryNames.add(entryName);
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

	// TODO: Genome length check should be generic given the tax id for all species with known expected genome length.
	public void validateCovid19GenomeSize() throws ValidationEngineException {
		if (sharedInfo.assemblyType != null && sharedInfo.assemblyType.equalsIgnoreCase(AssemblyType.COVID_19_OUTBREAK.getValue())) {

			long genomeSize = GenomeUtils.calculateGenomeSize(sharedInfo.sequenceInfo, sharedInfo.agpPlacedComponents);

			if (genomeSize > GenomeUtils.COVID_19_OUTBREAK_GENOME_MAX_SIZE) {
				throw new ValidationEngineException(String.format("%s maximum genome size is %d bp.", AssemblyType.COVID_19_OUTBREAK.getValue(), GenomeUtils.COVID_19_OUTBREAK_GENOME_MAX_SIZE), ReportErrorType.VALIDATION_ERROR);
			}
		}
	}

	public void validateDuplicateEntryNames() throws ValidationEngineException
	{
		if(sharedInfo.duplicateEntryNames.size()>0)
		{
			throw new ValidationEngineException("Entry names are duplicated in assembly : "+ String.join(",", sharedInfo.duplicateEntryNames),ReportErrorType.VALIDATION_ERROR);
		}
	}

	public void validateSequencelessChromosomes() throws ValidationEngineException
	{
		List<String> sequencelessChromosomes =new ArrayList<String>();
		for(String chromosomeName: sharedInfo.chromosomeNameQualifiers.keySet())
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
				if(entryName!=null&& sharedInfo.agpEntryNames.contains(entryName.toUpperCase()))
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

	void addErrorAndReport(ValidationResult validationResult, SubmissionFile submissionFile,String messageKey,  String... params) {
		ValidationMessage<Origin> message = FlatFileValidations.message(Severity.ERROR, messageKey, (Object) params);
		validationResult.append(message);
		addMessageStat(message);
		if(getOptions().reportDir.isPresent())
			getReporter().writeToFile(getReportFile(submissionFile), validationResult);
	}

	void addMessageStat(ValidationMessage message) {
		if (messageStats.putIfAbsent(message.getMessageKey(), new AtomicLong(1)) != null)
			messageStats.get(message.getMessageKey()).incrementAndGet();

	}

	void addMessageStats(Collection<ValidationMessage<Origin>> result)
	{
		if(result == null)
			return;

		for(ValidationMessage message: result)
		{
			addMessageStat(message);
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
		if(sharedInfo.masterEntry == null)
		{
			throw new ValidationEngineException("Master entry must to validate sequences");
		}
		if(entry==null)
			return ;
		entry.removeReferences();
		entry.removeProjectAccessions();
		entry.addReferences(sharedInfo.masterEntry.getReferences());
		entry.addProjectAccessions(sharedInfo.masterEntry.getProjectAccessions());
		entry.addXRefs(sharedInfo.masterEntry.getXRefs());
		entry.setComment(sharedInfo.masterEntry.getComment());
		entry.setDivision(sharedInfo.masterEntry.getDivision());
		//add chromosome qualifiers to entry

        if(Context.sequence == options.context.get()) {
            if(entry.getDataClass() == null || entry.getDataClass().isEmpty())
                entry.setDataClass(Entry.STD_DATACLASS);
        } else {
            entry.setDataClass(getDataclass(entry.getSubmitterAccession()));
        }
		addSourceQualifiers(entry);
		entry.getSequence().setMoleculeType(sharedInfo.masterEntry.getSequence().getMoleculeType());
   		if (entry.getSequence().getTopology() == null) {
      		entry.getSequence().setTopology(sharedInfo.masterEntry.getSequence().getTopology());
		}
		if(entry.getSubmitterAccession()!=null&&options.context.get()==Context.genome)
		{

			Utils.setAssemblyLevelDescription(sharedInfo.masterEntry.getDescription().getText(),
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
			&& sharedInfo.chromosomeNameQualifiers != null) {
			return sharedInfo.chromosomeNameQualifiers.get(submitterAccn.toUpperCase()).getTopology();
		}
		return null;
  	}

	protected PrintWriter getFixedFileWriter(SubmissionFile submissionFile) throws IOException
	{
		if(submissionFile.createFixedFile() && fixedFileWriter==null)
			fixedFileWriter= new PrintWriter(Files.newBufferedWriter(Paths.get(submissionFile.getFixedFile().getAbsolutePath()), StandardCharsets.UTF_8));
		return fixedFileWriter;
	}

	protected  PrintWriter getContigsReducedFileWriter(SubmissionFile submissionFile) throws IOException {
		if (sharedInfo.contigsReducedFileWriter == null) {
			sharedInfo.contigsReducedFileWriter = new PrintWriter(Files.newBufferedWriter(Paths.get(submissionFile.getFile().getParent() + File.separator + "reduced" + File.separator +  contigFileName), StandardCharsets.UTF_8));
		}
		return sharedInfo.contigsReducedFileWriter;
	}

	protected  PrintWriter getScaffoldsReducedFileWriter(SubmissionFile submissionFile) throws IOException {
		if ( sharedInfo.scaffoldsReducedFileWriter == null) {
			sharedInfo.scaffoldsReducedFileWriter = new PrintWriter(Files.newBufferedWriter(Paths.get(submissionFile.getFile().getParent() + File.separator + "reduced" + File.separator +  scaffoldFileName), StandardCharsets.UTF_8));
		}
		return sharedInfo.scaffoldsReducedFileWriter;
	}

	protected  PrintWriter getChromosomeFileWriter(SubmissionFile submissionFile) throws IOException {
		if (sharedInfo.chromosomesFileWriter == null) {
			sharedInfo.chromosomesFileWriter = new PrintWriter(Files.newBufferedWriter(Paths.get(submissionFile.getFile().getParent() +  File.separator + chromosomeFileName), StandardCharsets.UTF_8));
		}
		return sharedInfo.chromosomesFileWriter;
	}

	public  void flushAndCloseFileWriters() {
		if(!getOptions().forceReducedFlatfileCreation && getOptions().isWebinCLI) {
			return;
		}
		try {
			if (sharedInfo.contigsReducedFileWriter != null) {
				sharedInfo.contigsReducedFileWriter.flush();
				sharedInfo.contigsReducedFileWriter.close();
			}
			if (sharedInfo.scaffoldsReducedFileWriter != null) {
				sharedInfo.scaffoldsReducedFileWriter.flush();
				sharedInfo.scaffoldsReducedFileWriter.close();
			}
			if(sharedInfo.chromosomesFileWriter != null) {
				sharedInfo.chromosomesFileWriter.flush();
				sharedInfo.chromosomesFileWriter.close();
			}
		} catch (Exception ignored) {
		}
	}

	protected void collectContigInfo(Entry entry) throws Exception {
		try {
			if (entry.getSubmitterAccession() == null)
				entry.setSubmitterAccession(entry.getPrimaryAccession());
			if (entry.getSubmitterAccession() == null)
				throw new ValidationEngineException("Submitter accession missing for an entry");
			if (!sharedInfo.agpEntryNames.isEmpty() && sharedInfo.agpEntryNames.contains(entry.getSubmitterAccession().toUpperCase()))
			if (entry.getSubmitterAccession() == null) {
				if(entry.getPrimaryAccession() == null) {
					throw new ValidationEngineException("Both submitter accession and primary accession missing for an entry");
				} else {
					entry.setSubmitterAccession(entry.getPrimaryAccession());
				}
			}

			if (!sharedInfo.agpEntryNames.isEmpty() && sharedInfo.agpEntryNames.contains(entry.getSubmitterAccession().toUpperCase())) {
				return;
			}
			if (entry.getSequence() == null) {
				return;
			}
			// scaff1 contig1 -ok first we set seq for this , no issues
			//scaff2 scaff1 - currently we are not setting seq to it bcoz it is not part of ff or fasta
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

		} catch (Exception e) {
			if (getContigDB() != null)
				getContigDB().close();
			throw e;
		}
	}

	protected void addAgpEntryName(String entryName) throws ValidationEngineException {
			 if(!sharedInfo.agpEntryNames.add(entryName)) {
			 	throw new ValidationEngineException( " Object name should be unique in AGP files."+ entryName);
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
				entry.getSequence().setMoleculeType(sharedInfo.masterEntry.getSequence().getMoleculeType());
			}
			SourceFeature sourceFeature=featureFactory.createSourceFeature();
			sourceFeature.setLocations(featureLocation);
			entry.addFeature(sourceFeature);
		} else {
			subSeqIdQual = entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME);
		}

		SourceFeature source=null;
		if(sharedInfo.masterEntry!=null&&sharedInfo.masterEntry.getPrimarySourceFeature()!=null)
		{
			source=sharedInfo.masterEntry.getPrimarySourceFeature();
		}

		entry.getPrimarySourceFeature().removeAllQualifiers();
		if(options.context.get()==Context.genome)
		{

			if(entry.getSubmitterAccession()!=null)
			{
				Optional<java.util.Map.Entry<String, ChromosomeEntry>> chromosomeQualifierMap =
						sharedInfo.chromosomeNameQualifiers.entrySet().stream()
						.filter(x -> x.getKey().equalsIgnoreCase(entry.getSubmitterAccession()))
						.findFirst();
				if(chromosomeQualifierMap.isPresent())
				{	
					List<Qualifier> chromosomeQualifiers = chromosomeQualifierMap.get().getValue().setAndGetQualifiers(taxonHelper.isChildOf(
							sharedInfo.masterEntry
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
			Reference reference = new ReferenceUtils().getSubmitterReferenceFromManifest(options.assemblyInfoEntry.get().getAuthors(),
					options.assemblyInfoEntry.get().getAddress(), options.assemblyInfoEntry.get().getDate(), options.assemblyInfoEntry.get().getSubmissionAccountId());
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

	public  boolean isGenbank(File f) throws ValidationEngineException {
		try(BufferedReader br = CommonUtil.bufferedReaderFromFile(f)) {
			String line;
			if((line = br.readLine()) != null){
				return line.trim().startsWith("LOCUS");
			}
		} catch (Exception e) {
			throw new ValidationEngineException("Could not read the file :"+f, e);
		}
		return false;
	}

	public void setAnnotationDB(DB annotationDB)
	{
		this.annotationDB=annotationDB;
	}

	public DB getAnnotationDB()
	{
		return this.annotationDB;
	}
	public static void closeMapDB(DB ... dbs) {
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
	
	boolean validateFileFormat(File file,uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType fileType) throws IOException
	{  
		String flat_file_token= "ID";
		String fasta_file_token=">";
		String agp_file_comment_token="#";
		String spaceregex = "\\s+";
		int agp_number_of_columns = 9;
		int chromosome_min_number_of_columns = 3;
		int chromosome_max_number_of_columns = 4;
		String line=null;
		
		try(BufferedReader  fileReader=CommonUtil.bufferedReaderFromFile(file))
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
		return sharedInfo.sequenceInfo.entrySet().stream().anyMatch(x->x.getKey().equalsIgnoreCase(entryName));

	}

	boolean validateSequenceCountForTemplate(ValidationResult validationResult, SubmissionFile submissionFile) {
		if (!options.ignoreErrors && sharedInfo.sequenceCount > MAX_SEQUENCE_COUNT_FOR_TEMPLATE) {
			validationResult.append(new ValidationMessage<>(Severity.ERROR, "MaxSequenceCountExceededError", MAX_SEQUENCE_COUNT_FOR_TEMPLATE ));
			if (getOptions().reportDir.isPresent())
				getReporter().writeToFile(getReportFile(submissionFile), validationResult);
			return false;
		}
		return true;
	}

	public static boolean excludeDistribution(String assemblyType) {
		return AssemblyType.BINNEDMETAGENOME.getValue().equalsIgnoreCase(assemblyType) ||
				AssemblyType.PRIMARYMETAGENOME.getValue().equalsIgnoreCase(assemblyType) ||
				AssemblyType.CLINICALISOLATEASSEMBLY.getValue().equalsIgnoreCase(assemblyType);
	}

	void writeEntryToFile(Entry entry, SubmissionFile submissionFile) throws IOException {
		if(!getOptions().forceReducedFlatfileCreation && (getOptions().isWebinCLI || excludeDistribution(sharedInfo.assemblyType))) {
			return;
		}
		if (getOptions().getEntryValidationPlanProperty().validationScope.get() == ValidationScope.ASSEMBLY_CONTIG
		|| getOptions().context.orElse(null) == Context.transcriptome) {
			new EmblReducedFlatFileWriter(entry).write(getContigsReducedFileWriter(submissionFile));
		} else if (getOptions().getEntryValidationPlanProperty().validationScope.get() == ValidationScope.ASSEMBLY_SCAFFOLD) {
			new EmblReducedFlatFileWriter(entry).write(getScaffoldsReducedFileWriter(submissionFile));
		} else if (getOptions().getEntryValidationPlanProperty().validationScope.get() == ValidationScope.ASSEMBLY_CHROMOSOME) {
			new EmblEntryWriter(entry).write(getChromosomeFileWriter(submissionFile));
		}
	}

	void addSubmitterSeqIdQual(Entry entry) {
		if (getOptions().getEntryValidationPlanProperty().validationScope.get() == ValidationScope.ASSEMBLY_CONTIG
				|| getOptions().getEntryValidationPlanProperty().validationScope.get() == ValidationScope.ASSEMBLY_SCAFFOLD
				|| getOptions().getEntryValidationPlanProperty().validationScope.get() == ValidationScope.ASSEMBLY_CHROMOSOME) {
			if (entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME) == null) {
				entry.getPrimarySourceFeature().addQualifier(new QualifierFactory().createQualifier(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME, entry.getSubmitterAccession()));
			}
		}
	}

	void checkChromosomeTopology(Entry entry) throws ValidationEngineException {
		Sequence.Topology chrListToplogy = getTopology(entry.getSubmitterAccession());
		if (chrListToplogy != null) {
			if (entry.getSequence().getTopology() != null
					&& entry.getSequence().getTopology() != chrListToplogy) {
				throw new ValidationEngineException("The topology in the ID line " + entry.getSequence().getTopology() + " conflicts with the topology specified in the chromosome list file " + chrListToplogy, ReportErrorType.VALIDATION_ERROR);
			}
			entry.getSequence().setTopology(chrListToplogy);
		}
	}

	public boolean hasAnnotationFlatfile() throws ValidationEngineException {
		for (SubmissionFile submissionFile : options.submissionFiles.get().getFiles(SubmissionFile.FileType.FLATFILE)) {
			boolean isGenbankFile = isGenbank(submissionFile.getFile());
			EmblEntryReader.Format format = options.context.get() == Context.genome ? EmblEntryReader.Format.ASSEMBLY_FILE_FORMAT : EmblEntryReader.Format.EMBL_FORMAT;

			try (BufferedReader fileReader = CommonUtil.bufferedReaderFromFile(submissionFile.getFile())) {
				EntryReader entryReader = isGenbankFile ? new GenbankEntryReader(fileReader) : new EmblEntryReader(fileReader, format, submissionFile.getFile().getName());

				entryReader.read();
				if (entryReader.isEntry()) {
					Entry entry = entryReader.getEntry();
					return entry.getSequence() == null || entry.getSequence().getSequenceByte() == null;
				} else {
					throw new ValidationEngineException("Could not read flatfile, please check the flatfile formatted correctly.", ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
				}
			} catch (IOException e) {
				throw new ValidationEngineException(e.getMessage(), e);
			}
		}
		return false;
	}

	public SharedInfo getSharedInfo() {
		return sharedInfo;
	}

	public static class SharedInfo {
		public Entry masterEntry =null;

		public int sequenceCount = 0;

		public boolean hasAnnotationOnlyFlatfile = false;
		public boolean hasAgp = false;

		public HashMap<String, ChromosomeEntry> chromosomeNameQualifiers = new HashMap<>();
		public List<String> chromosomeNames =new ArrayList<>();
		public Map<String, AssemblySequenceInfo> sequenceInfo = new LinkedHashMap<>();
		public Map<String,AssemblySequenceInfo> fastaInfo = new LinkedHashMap<>();
		public Map<String,AssemblySequenceInfo> flatfileInfo = new LinkedHashMap<>();
		public Map<String,AssemblySequenceInfo> agpInfo = new LinkedHashMap<>();
		public List<String> duplicateEntryNames = new ArrayList<>();
		public HashSet<String> entryNames = new HashSet<>();
		public Set<String> agpEntryNames =new HashSet<>();
		public Set<String> agpPlacedComponents =new HashSet<>();
		public Set<String> unplacedEntryNames =new HashSet<>();
		public Set<String> unlocalisedEntryNames = new HashSet<>();
		public PrintWriter contigsReducedFileWriter =null;
		public PrintWriter scaffoldsReducedFileWriter =null;
		public PrintWriter chromosomesFileWriter =null;
		public String assemblyType =null;
	}
}
