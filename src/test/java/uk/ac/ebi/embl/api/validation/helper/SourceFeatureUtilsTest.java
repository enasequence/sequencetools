package uk.ac.ebi.embl.api.validation.helper;

import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;

import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.webin.cli.validator.reference.Attribute;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SourceFeatureUtilsTest {

    @Test
    public void constructSourceFeature() {
        String sampleId = "ERS4477947";
        SourceFeature source = new SourceFeatureUtils().constructSourceFeature(getSample(sampleId), new TaxonomyClient());
        assertEquals(7, source.getQualifiers().size());//6 from sample + organism qualifier
        assertEquals("2020-03-09", source.getSingleQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME).getValue());
        assertEquals("Spain:Valencia", source.getSingleQualifier(Qualifier.COUNTRY_QUALIFIER_NAME).getValue());
        // expects direction (N) added to latitude, and longitude with existing direction returned as it is - both cases checked
        assertEquals("39.47 N 0.38 E", source.getSingleQualifier(Qualifier.LAT_LON_QUALIFIER_NAME).getValue());
        assertEquals("Homo sapiens", source.getSingleQualifier(Qualifier.HOST_QUALIFIER_NAME).getValue());
        assertEquals("hCoV-19/Spain/Valencia27/2020", source.getSingleQualifier(Qualifier.ISOLATE_QUALIFIER_NAME).getValue());
        assertEquals("Severe acute respiratory syndrome coronavirus 2", source.getSingleQualifier(Qualifier.ORGANISM_QUALIFIER_NAME).getValue());
        assertEquals("GISAID123", source.getSingleQualifier(Qualifier.NOTE_QUALIFIER_NAME).getValue());
        Taxon taxon = source.getTaxon();
        assertEquals(new Long(2697049), taxon.getTaxId());
        assertEquals("Severe acute respiratory syndrome coronavirus 2", taxon.getScientificName());
    }

    // This to do comment was moved here from its previous spot before the refactor.
    //TODO: ask about lat lon unit
    private Sample getSample(String sampleId) {
        Sample sample = new Sample();
        sample.setSraSampleId(sampleId);
        sample.setName("hCoV-19/Spain/Valencia27/2020");
        sample.setOrganism("Severe acute respiratory syndrome coronavirus 2");
        sample.setTaxId(2697049);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("sample_description", "hCoV-19/Spain/Valencia27/2020"));
        attributes.add(new Attribute("collection date", "2020-03-09")); //1
        attributes.add(new Attribute("geographic location (latitude)", "39.47"));//3
        attributes.add(new Attribute("geographic location (longitude)", "0.38 E"));//3
        attributes.add(new Attribute("geographic location (region and locality)", "Valencia"));//2
        attributes.add(new Attribute("host common name", "Human"));
        attributes.add(new Attribute("geographic location (country and/or sea)", "Spain"));//2
        attributes.add(new Attribute("host subject id", "18218863"));
        attributes.add(new Attribute("host age", "81"));
        attributes.add(new Attribute("host health state", "not provided"));
        attributes.add(new Attribute("host sex", "male"));
        attributes.add(new Attribute("host scientific name", "Homo sapiens"));//4
        attributes.add(new Attribute("isolate", "hCoV-19/Spain/Valencia27/2020"));//5
        attributes.add(new Attribute("GISAID Accession ID","GISAID123"));//6
        attributes.add(new Attribute("isolation source host-associated", "Nasopharyngeal exudate"));
        attributes.add(new Attribute("ENA-CHECKLIST", "ERC000033"));

        sample.setAttributes(attributes);

        return sample;
    }
}