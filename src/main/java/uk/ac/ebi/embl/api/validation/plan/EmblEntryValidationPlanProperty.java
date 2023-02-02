package uk.ac.ebi.embl.api.validation.plan;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class EmblEntryValidationPlanProperty
{
	public final ValidationPlanProperty<ValidationScope> validationScope = new ValidationPlanProperty<>(ValidationScope.EMBL);
	public final ValidationPlanProperty<Connection> enproConnection = new ValidationPlanProperty<>(null);
	public final ValidationPlanProperty<Connection> eraproConnection = new ValidationPlanProperty<>(null);
	public final ValidationPlanProperty<Boolean> isDevMode = new ValidationPlanProperty<>(false);
	public final ValidationPlanProperty<Boolean> isFixMode = new ValidationPlanProperty<>(false);
	public final ValidationPlanProperty<Integer> minGapLength = new ValidationPlanProperty<>(0);
	public final ValidationPlanProperty<TaxonomyClient> taxonClient = new ValidationPlanProperty<>(null);
	public final ValidationPlanProperty<Boolean> isRemote=new ValidationPlanProperty<>(false);
	public final ValidationPlanProperty<FileType> fileType=new ValidationPlanProperty<>(FileType.EMBL);
	public final ValidationPlanProperty<String> analysis_id=new ValidationPlanProperty<>(null);
	public final ValidationPlanProperty<String> organism=new ValidationPlanProperty<>(null);
	public final ValidationPlanProperty<List<String>> locus_tag_prefixes = new ValidationPlanProperty<>(new ArrayList<>());
	public final ValidationPlanProperty<HashMap<String, AssemblySequenceInfo>> assemblySequenceInfo= new ValidationPlanProperty<>(new HashMap<>());
	public final ValidationPlanProperty<Boolean> isFixCds = new ValidationPlanProperty<>(false);
	public final ValidationPlanProperty<Boolean> ignore_errors = new ValidationPlanProperty<>(false);
	public final ValidationPlanProperty<Integer> sequenceNumber = new ValidationPlanProperty<>(0);
	public final ValidationPlanProperty<Boolean> ncbiCon = new ValidationPlanProperty<>(false);
	public final ValidationPlanProperty<Boolean> isSourceUpdate = new ValidationPlanProperty<>(false);
}
