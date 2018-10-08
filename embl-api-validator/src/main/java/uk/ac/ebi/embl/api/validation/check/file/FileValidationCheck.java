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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import uk.ac.ebi.embl.agp.reader.AGPFileReader;
import uk.ac.ebi.embl.agp.reader.AGPLineReader;
import uk.ac.ebi.embl.api.contant.AnalysisType;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.api.validation.report.DefaultSubmissionReporter;
import uk.ac.ebi.embl.api.validation.report.SubmissionReporter;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

public abstract class FileValidationCheck {

	private SubmissionOptions options =null;
	protected SubmissionReporter reporter=null;
	private static final String REPORT_FILE_SUFFIX = ".report";
	public static HashMap<String,List<Qualifier>> chromosomeNameQualifiers = new HashMap<String,List<Qualifier>>();
	public static List<String> chromosomes =new ArrayList<String>();
	public static List<String> contigs =new ArrayList<String>();
	public static List<String> scaffolds =new ArrayList<String>();
	public static List<String> agpEntryNames =new ArrayList<String>();
	public static HashMap<String,AgpRow> contigRangeMap=new HashMap<String,AgpRow>();
	protected ConcurrentMap<String, AtomicLong> messageStats = null;
	protected static Entry masterEntry =null;
	protected TaxonHelper taxonHelper= null;
	private PrintWriter fixedFileWriter =null;


	public FileValidationCheck(SubmissionOptions options) {
		this.options =options;
		messageStats =  new ConcurrentHashMap<String, AtomicLong>();
		taxonHelper =new TaxonHelperImpl();
		ValidationMessageManager.addBundle(ValidationMessageManager.GENOMEASSEMBLY_VALIDATION_BUNDLE);	
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);		
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
	}
	public abstract boolean check(SubmissionFile file) throws ValidationEngineException;
	public abstract boolean check() throws ValidationEngineException ;

	protected SubmissionOptions getOptions() {
		return options;
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
		HashSet<Severity> severity = new HashSet<Severity>();
		severity.add(Severity.ERROR);
		if(reporter==null)
			return new DefaultSubmissionReporter(severity);
		return reporter;
	}


	public  Path getReportFile(String reportDir, String fileName) throws ValidationEngineException
	{
		return Paths.get(reportDir, fileName + REPORT_FILE_SUFFIX );
	}

	public void readAGPfiles() throws ValidationEngineException
	{
		for( SubmissionFile submissionFile : options.submissionFiles.get().getFiles(FileType.AGP) ) 
		{
			try(BufferedReader fileReader= new BufferedReader(new FileReader(submissionFile.getFile())))
			{
				AGPFileReader reader = new AGPFileReader( new AGPLineReader(fileReader));

				reader.read();
				int i=1;

				while(reader.isEntry() )
				{
					agpEntryNames.add( ( (Entry) reader.getEntry() ).getSubmitterAccession().toUpperCase() );

					for(AgpRow agpRow: ((Entry)reader.getEntry()).getSequence().getSortedAGPRows())
					{
						
						if(!agpRow.isGap())
						{
							contigRangeMap.put(agpRow.getComponent_id().toUpperCase()+"_"+i,agpRow);
						}
						i++;
					}
					reader.read();
				}

			}catch(Exception e)
			{
				throw new ValidationEngineException(e.getMessage());
			}

		}

	}

	protected ValidationScope getValidationScopeandEntrynames(String entryName)
	{
		if(options.context.get()==Context.genome)
		{
			if(chromosomeNameQualifiers.get(entryName.toUpperCase())!=null)
			{
				chromosomes.add(entryName.toUpperCase());
				return ValidationScope.ASSEMBLY_CHROMOSOME;
			}
			if(agpEntryNames.contains(entryName.toUpperCase()))
			{
				scaffolds.add(entryName);
				return ValidationScope.ASSEMBLY_SCAFFOLD;
			}
			else
			{
				contigs.add(entryName.toUpperCase());
				return ValidationScope.ASSEMBLY_CONTIG;
			}
		}
		return options.getEntryValidationPlanProperty().validationScope.get();
	}

	public void validateDuplicateEntryNames() throws ValidationEngineException
	{
		HashSet<String> entryNames = new HashSet<String>();
		List<String> duplicateEntryNames = new ArrayList<String>();
		for(String entryName:contigs)
		{
			if(!entryNames.add(entryName))
				duplicateEntryNames.add(entryName);
		}
		for(String entryName:scaffolds)
		{
			if(!entryNames.add(entryName))
			duplicateEntryNames.add(entryName);
		}
		for(String entryName:chromosomes)
		{
			if(!entryNames.add(entryName))
				duplicateEntryNames.add(entryName);
		}
		if(duplicateEntryNames.size()>0)
		{
			throw new ValidationEngineException("Entry names are duplicated in assembly : "+ String.join(",",duplicateEntryNames));
		}
	}

	public void validateSequencelessChromosomes() throws ValidationEngineException
	{
		List<String> sequencelessChromosomes = new ArrayList<String>();
		if(chromosomeNameQualifiers.size()!=chromosomes.size())
		{
			for(String chromosomeName: chromosomeNameQualifiers.keySet())
			{
				if(!chromosomes.contains(chromosomeName))
				{
					sequencelessChromosomes.add(chromosomeName);
				}
			}
			throw new ValidationEngineException("Sequenceless chromosomes are not allowed in assembly : "+String.join(",",sequencelessChromosomes));
		}
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
				if(agpEntryNames.contains(entryName.toUpperCase()))
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
	protected void addMessagekey(ValidationResult result)
	{
		for(ValidationMessage message: result.getMessages())
		{
			messageStats.putIfAbsent(message.getMessageKey(), new AtomicLong(1));
			messageStats.get(message.getMessageKey()).incrementAndGet();
		}

	}

	protected void appendHeader(Entry entry) throws ValidationEngineException
	{
		if(masterEntry==null)
		{
			throw new ValidationEngineException("Master entry must to validate sequences");
		}
		if(entry==null)
			return ;
		entry.removeReferences();
		entry.removeProjectAccessions();
		entry.addReferences(masterEntry.getReferences());
		entry.addProjectAccessions(masterEntry.getProjectAccessions());
		entry.addXRefs(masterEntry.getXRefs());
		entry.setComment(masterEntry.getComment());
		entry.setDataClass(getDataclass(entry.getSubmitterAccession()));
		addSourceQualifiers(entry);
		//add chromosome qualifiers to entry
		if(entry.getSubmitterAccession()!=null&&options.context.get()==Context.genome)
		{

			Utils.setssemblyLevelDescription(masterEntry.getDescription().getText(), 
					ValidationScope.ASSEMBLY_CONTIG==getOptions().getEntryValidationPlanProperty().validationScope.get() ? 0 
							: ValidationScope.ASSEMBLY_SCAFFOLD==getOptions().getEntryValidationPlanProperty().validationScope.get() ? 1 
									: ValidationScope.ASSEMBLY_CHROMOSOME==getOptions().getEntryValidationPlanProperty().validationScope.get() ? 2 :null,
											entry);
		}

		if(entry.getSubmitterAccession()!=null&&options.context.get()==Context.transcriptome)
			entry.setDescription(new Text("TSA: " + entry.getPrimarySourceFeature().getScientificName() + " " + entry.getSubmitterAccession()));

	}

	protected PrintWriter getFixedFileWriter(SubmissionFile submissionFile) throws FileNotFoundException
	{
		if(submissionFile.createFixedFile()&&fixedFileWriter==null)
			fixedFileWriter= new PrintWriter(submissionFile.getFixedFile().getAbsolutePath());
		return fixedFileWriter;
	}

	protected void collectContigInfo(Entry entry)
	{
		if(!agpEntryNames.isEmpty()&&agpEntryNames.contains(entry.getSubmitterAccession().toUpperCase()))
			return;
		if(entry.getSequence()==null)
			return;
		if(!contigRangeMap.isEmpty())
		{
			List<String> contigKeys=contigRangeMap.entrySet().stream().filter(e -> e.getKey().contains(entry.getSubmitterAccession().toUpperCase())).map(e -> e.getKey()).collect(Collectors.toList());
			for(String contigKey:contigKeys)
			{
				contigRangeMap.get(contigKey).setSequence(entry.getSequence().getSequenceByte(contigRangeMap.get(contigKey).getComponent_beg(),contigRangeMap.get(contigKey).getComponent_end()));
			}
		}
	}

	private void addSourceQualifiers(Entry entry)
	{
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
		}
		SourceFeature source=null;
		if(masterEntry!=null&&masterEntry.getPrimarySourceFeature()!=null)
		{
			source=masterEntry.getPrimarySourceFeature();
		}
		entry.getPrimarySourceFeature().removeAllQualifiers();
		if(options.context.get()==Context.genome)
		{

			List<Qualifier> chromosomeQualifiers = chromosomeNameQualifiers.get(entry.getSubmitterAccession().toUpperCase());

			if(chromosomeQualifiers!=null)
			{
				for (Qualifier chromosomeQualifier : chromosomeQualifiers)
				{
					entry.getPrimarySourceFeature().addQualifier(chromosomeQualifier);

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

	}



}
