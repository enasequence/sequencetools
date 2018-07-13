package uk.ac.ebi.embl.api.validation.plan;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import uk.ac.ebi.embl.api.entry.ContigSequenceInfo;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;

public class EmblEntryValidationPlanProperty
{
	public final ValidationPlanProperty<ValidationScope> validationScope = new ValidationPlanProperty<ValidationScope>(ValidationScope.EMBL);
	public final ValidationPlanProperty<Connection> enproConnection = new ValidationPlanProperty<Connection>(null);
	public final ValidationPlanProperty<Connection> eraproConnection = new ValidationPlanProperty<Connection>(null);
	public final ValidationPlanProperty<Boolean> isDevMode = new ValidationPlanProperty<Boolean>(false);
	public final ValidationPlanProperty<Boolean> isFixMode = new ValidationPlanProperty<Boolean>(false);
	public final ValidationPlanProperty<Integer> minGapLength = new ValidationPlanProperty<Integer>(0);
	public final ValidationPlanProperty<Boolean> isAssembly = new ValidationPlanProperty<Boolean>(false);
	public final ValidationPlanProperty<TaxonHelper> taxonHelper = new ValidationPlanProperty<TaxonHelper>(null);
	public final ValidationPlanProperty<Boolean> isRemote=new ValidationPlanProperty<Boolean>(false);
	public final ValidationPlanProperty<FileType> fileType=new ValidationPlanProperty<FileType>(FileType.EMBL);
	public final ValidationPlanProperty<String> analysis_id=new ValidationPlanProperty<String>(null);
	public final ValidationPlanProperty<List<String>> locus_tag_prefixes = new ValidationPlanProperty<List<String>>(new ArrayList<String>());
	public final ValidationPlanProperty<HashMap<String, Long>> contigEntryNames= new ValidationPlanProperty<HashMap<String, Long>>(new HashMap<String,Long>());
	public final ValidationPlanProperty<Boolean> isFixCds = new ValidationPlanProperty<Boolean>(false);
}
