package uk.ac.ebi.embl.api.validation.helper;


import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;

public class UtilsTest  {

    @Test
    public void getContigDescription() {
        Assert.assertEquals("masterDescription, contig: submitterAccession",
                Utils.getContigDescription("masterDescription", "submitterAccession"));
    }

    @Test
    public void getScaffoldDescription() {
        Assert.assertEquals("masterDescription, scaffold: submitterAccession",
                Utils.getScaffoldDescription("masterDescription", "submitterAccession"));
    }

    @Test
    public void getChromosomeDescription() {
        SourceFeature feature = new FeatureFactory().createSourceFeature();
        Assert.assertEquals("masterDescription, submitterAccession",
                Utils.getChromosomeDescription(feature, "masterDescription", "submitterAccession"));

        feature.addQualifier(Qualifier.SEGMENT_QUALIFIER_NAME, "segment value");
        Assert.assertEquals("masterDescription, segment: segment value",
                Utils.getChromosomeDescription(feature, "masterDescription", "submitterAccession"));

        feature.addQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME, "organelle value");
        Assert.assertEquals("masterDescription, organelle: organelle value",
                Utils.getChromosomeDescription(feature, "masterDescription", "submitterAccession"));

        feature.addQualifier(Qualifier.CHROMOSOME_QUALIFIER_NAME, "chromosome value");
        Assert.assertEquals("masterDescription, chromosome: chromosome value",
                Utils.getChromosomeDescription(feature, "masterDescription", "submitterAccession"));

        feature.addQualifier(Qualifier.PLASMID_QUALIFIER_NAME, "plasmid value");
        Assert.assertEquals("masterDescription, plasmid: plasmid value",
                Utils.getChromosomeDescription(feature, "masterDescription", "submitterAccession"));
     }
}