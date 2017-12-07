package uk.ac.ebi.embl.api.validation.plan;

import java.sql.Connection;
import java.util.HashSet;

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
	public final ValidationPlanProperty<HashSet<String>> contigEntryNames = new ValidationPlanProperty<HashSet<String>>(new HashSet<String>());
}
