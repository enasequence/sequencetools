package uk.ac.ebi.embl.api.validation.helper;

import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;

import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.SampleInfo;
import uk.ac.ebi.embl.api.validation.dao.model.SampleEntity;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MasterSourceFeatureUtilsTest {

    @Test
    public void constructSourceFeature() {
        String sampleId = "ERS4477947";
        SourceFeature source = new MasterSourceFeatureUtils().constructSourceFeature(getSampleEntity(),
                new TaxonHelperImpl(), getSampleInfo(sampleId));
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

    //TODO: ask about lat lon unit
    private SampleEntity getSampleEntity() {
        SampleEntity sample = new SampleEntity();
        Map<String, String> attr = new HashMap<>();
        attr.put("sample_description", "hCoV-19/Spain/Valencia27/2020");
        attr.put("collection date", "2020-03-09"); //1
        attr.put("geographic location (latitude)", "39.47");//3
        attr.put("geographic location (longitude)", "0.38 E");//3
        attr.put("geographic location (region and locality)", "Valencia");//2
        attr.put("host common name", "Human");
        attr.put("geographic location (country and/or sea)", "Spain");//2
        attr.put("host subject id", "18218863");
        attr.put("host age", "81");
        attr.put("host health state", "not provided");
        attr.put("host sex", "male");
        attr.put("host scientific name", "Homo sapiens");//4
        attr.put("isolate", "hCoV-19/Spain/Valencia27/2020");//5
        attr.put("GISAID Accession ID","GISAID123");//6
        attr.put("isolation source host-associated", "Nasopharyngeal exudate");
        attr.put("ENA-CHECKLIST", "ERC000033");
        sample.setAttributes(attr);
        return sample;
    }


    private SampleInfo getSampleInfo(String sampleId) {
        SampleInfo sampleInfo = new SampleInfo();
        sampleInfo.setSampleId(sampleId);
        sampleInfo.setUniqueName("hCoV-19/Spain/Valencia27/2020");
        sampleInfo.setScientificName("Severe acute respiratory syndrome coronavirus 2");
        sampleInfo.setTaxId(2697049L);
        return sampleInfo;
    }
}