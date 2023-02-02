package uk.ac.ebi.embl.template;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.service.SampleRetrievalService;
import uk.ac.ebi.embl.api.service.SequenceToolsServices;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.model.SampleEntity;
import uk.ac.ebi.embl.api.validation.fixer.entry.SubmitterAccessionFix;
import uk.ac.ebi.embl.api.validation.helper.SourceFeatureUtils;
import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.plan.ValidationPlan;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.embl.flatfile.writer.embl.CCWriter;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.webin.cli.validator.reference.Attribute;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TemplateEntryProcessor {
    
    private StringBuilder template;
    private TemplateInfo templateInfo;
    private ValidationPlan validationPlan;
    private Connection connEra;
    private String molType;
    private HashMap<String, Sample> sampleCache = new HashMap<String,Sample>();

    public TemplateEntryProcessor(Connection connEra) {
        this(ValidationScope.EMBL_TEMPLATE);
        this.connEra = connEra;
    }

    public TemplateEntryProcessor(ValidationScope validationScope) {
        EmblEntryValidationPlanProperty emblEntryValidationProperty = new EmblEntryValidationPlanProperty();
        emblEntryValidationProperty.validationScope.set(validationScope);
        emblEntryValidationProperty.isDevMode.set(false);
        emblEntryValidationProperty.isFixMode.set(true);
        emblEntryValidationProperty.minGapLength.set(0);
        validationPlan = new EmblEntryValidationPlan(emblEntryValidationProperty);
        validationPlan.addMessageBundle(TemplateProcessorConstants.TEMPLATE_MESSAGES_BUNDLE);
        validationPlan.addMessageBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
        validationPlan.addMessageBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    }

    public ValidationResult validateSequenceUploadEntry(Entry entry) throws Exception {
        return validationPlan.execute(entry);
    }

    protected TemplateProcessorResultSet processEntry(TemplateInfo templateInfo, String molType, TemplateVariables templateVariables, String projectId) throws Exception {
        this.templateInfo = templateInfo;
        this.molType = molType;
        TemplateProcessorResultSet templateProcessorResultSet = new TemplateProcessorResultSet();
        if (!checkMandatoryFieldsArePresent(templateInfo, templateVariables, templateProcessorResultSet))
            return templateProcessorResultSet;
        if (!checkSelectedHeadersHaveValuesAndAreSupported(templateVariables, templateProcessorResultSet))
            return templateProcessorResultSet;
        template = new StringBuilder(this.templateInfo.getTemplateString());
        replacePPOrganelleToken(templateVariables);
        replacePPNotes(templateVariables);
        replacePPGene(templateVariables);
        addSequenceLengthToken(templateVariables);
        replaceOrganismToken(templateVariables,templateProcessorResultSet);
        replaceTokens(templateVariables);
        new SectionExtractor().removeSections(template, this.templateInfo.getSections(), templateVariables);
        StringBuilderUtils.removeUnmatchedTokenLines(template);
        validateSediment(templateProcessorResultSet, templateVariables);
        validateMarker(templateProcessorResultSet, templateVariables);
        if(!templateProcessorResultSet.getValidationResult().isValid()) {
            return templateProcessorResultSet;
        }
        
        BufferedReader stringReader = new BufferedReader(new StringReader(template.toString().trim().concat("\n//")));
        EntryReader entryReader = new EmblEntryReader(stringReader);
        ValidationResult validationResult = entryReader.read();
        if(!validationResult.isValid()) {
            templateProcessorResultSet.getValidationResult().append(validationResult);
            return templateProcessorResultSet;
        }
        Entry entry = entryReader.getEntry();
        if(StringUtils.isNotEmpty(projectId)) {
            entry.addProjectAccession(new Text(projectId));
        }
        entry.setSubmitterAccession(SubmitterAccessionFix.fix(templateVariables.getSequenceName()));
        addDataToEntry(entry, templateVariables);
        
        // Update SourceFeature using sample values.
        updateSourceFeatureUsingOrganismFieldValue(entry,templateVariables);
        
        List<Text> kewordsL = entry.getKeywords();
        if (kewordsL != null && !kewordsL.isEmpty()) {
            switch (kewordsL.get(0).getText()) {
                case Entry.WGS_DATACLASS:
                    entry.setDataClass(Entry.WGS_DATACLASS);
                    break;
                case Entry.GSS_DATACLASS:
                    entry.setDataClass(Entry.GSS_DATACLASS);
                    break;
                case Entry.CON_DATACLASS:
                    entry.setDataClass(Entry.CON_DATACLASS);
                    break;
                case Entry.TPA_DATACLASS:
                    entry.setDataClass(Entry.TPA_DATACLASS);
                    break;
                case Entry.PRT_DATACLASS:
                    entry.setDataClass(Entry.PRT_DATACLASS);
                    break;
                case Entry.PAT_DATACLASS:
                    entry.setDataClass(Entry.PAT_DATACLASS);
                    break;
                case Entry.HTG_DATACLASS:
                    entry.setDataClass(Entry.HTG_DATACLASS);
                    break;
                case Entry.TSA_DATACLASS:
                    entry.setDataClass(Entry.TSA_DATACLASS);
                    break;
                case Entry.HTC_DATACLASS:
                    entry.setDataClass(Entry.HTC_DATACLASS);
                    break;
                case Entry.TPX_DATACLASS:
                    entry.setDataClass(Entry.TPX_DATACLASS);
                    break;
                case Entry.STS_DATACLASS:
                    entry.setDataClass(Entry.STS_DATACLASS);
                    break;
                case Entry.EST_DATACLASS:
                    entry.setDataClass(Entry.EST_DATACLASS);
                    break;
                case Entry.TLS_DATACLASS:
                    entry.setDataClass(Entry.TLS_DATACLASS);
                    break;
                default:
                    entry.setDataClass(Entry.STD_DATACLASS);
            }
        } else
            entry.setDataClass(Entry.STD_DATACLASS);
       /* if (this.templateInfo.getAnalysisId() != null && !this.templateInfo.getAnalysisId().isEmpty()) {
            Reference reference = getReferences();
            if (reference != null)
                entry.getReferences().add(reference);
        }*/
        templateProcessorResultSet.getValidationResult().append((validationPlan.execute(entry)));
        templateProcessorResultSet.setEntry(entry);
        return templateProcessorResultSet;
    }

    private boolean checkMandatoryFieldsArePresent(TemplateInfo templateInfo, TemplateVariables templateVariables, TemplateProcessorResultSet templateProcessorResultSet) throws Exception {
        List<String> mandatoryFieldsList =  templateInfo.getMandatoryFields();
        Map<String, String> fieldsMap = templateVariables.getVariables();
        String missingfields = "";
        for (String field: mandatoryFieldsList) {
            if (!fieldsMap.containsKey(field))
                missingfields += field + ",";
        }
        if (!missingfields.isEmpty()) {
            ValidationMessage<Origin> message = new ValidationMessage<Origin>(Severity.ERROR, "MandatoryFieldsCheck", missingfields.substring(0, missingfields.length() - 1));
            templateProcessorResultSet.getValidationResult().append(new ValidationResult().append(message));
            return false;
        }
        return true;
    }

    private boolean checkSelectedHeadersHaveValuesAndAreSupported(TemplateVariables templateVariables, TemplateProcessorResultSet templateProcessorResultSet) throws Exception {
        Map<String, String> fieldsMap = templateVariables.getVariables();
        String missingValue = "";
        String unsupportedHeaders = "";
        Map <String, TemplateTokenInfo>  templateTokenMap = templateInfo.getTokensAsMap();
        for (String header: fieldsMap.keySet()) {
            String value = fieldsMap.get(header);
            if(!templateTokenMap.containsKey(header))
                unsupportedHeaders += header + ",";
            else if (value == null || value.isEmpty())
                missingValue += "Header " + header + " has no value.\n";
        }
        if (!unsupportedHeaders.isEmpty()) {
            ValidationMessage<Origin> message = new ValidationMessage<Origin>(Severity.ERROR, "HeadersSupportedCheck", unsupportedHeaders.substring(0, unsupportedHeaders.length() - 1));
            templateProcessorResultSet.getValidationResult().append(new ValidationResult().append(message));
            return false;
        }
        if (!missingValue.isEmpty()) {
            ValidationMessage<Origin> message = new ValidationMessage<Origin>(Severity.ERROR, "HeadersValuesCheck", missingValue.substring(0, missingValue.length() - 1));
            templateProcessorResultSet.getValidationResult().append(new ValidationResult().append(message));
            return false;
        }
        return true;
    }
    
    private void replacePPOrganelleToken(TemplateVariables templateVariables) throws Exception {
        if (!template.toString().contains(TemplateProcessorConstants.PP_ORGANELLE_TOKEN))
            return;
        for (String tokenName: templateVariables.getTokenNames()) {
            String tokenValue = templateVariables.getTokenValue(tokenName);
            if (tokenValue == null || tokenValue.isEmpty())
                continue;
            if (tokenName.toUpperCase().equals(TemplateProcessorConstants.ORGANELLE_TOKEN)) {
                template = new StringBuilder(template.toString().replace(TemplateProcessorConstants.PP_ORGANELLE_TOKEN , tokenValue));
                return;
            }
        }
        template = new StringBuilder(template.toString().replace(TemplateProcessorConstants.PP_ORGANELLE_TOKEN, ""));
    }

    /**
     * This method replaces {ORGANISM_NAME} with valid scientificName.
     */
    private void replaceOrganismToken(TemplateVariables templateVariables, TemplateProcessorResultSet templateProcessorResultSet) throws Exception {
        if (!templateContainsOrganismToken())
            return;
        TaxonomyClient taxonomyClient=new TaxonomyClient();
        String scientificName="";
        
        for (String fieldName: templateVariables.getTokenNames()) {
            if (isOrganismField(fieldName)) {
                String fieldValue = templateVariables.getTokenValue(fieldName);
                
                if (Utils.isValidTaxId(fieldValue)) {
                    // Check if the passed value is a valid taxId.
                    Taxon taxon = taxonomyClient.getTaxonByTaxid(Long.valueOf(fieldValue));
                    scientificName = taxon!=null ? taxon.getScientificName() : "";
                }else {
                    // Check if the passed value is a valid organismName.
                    List<Taxon> taxonList = taxonomyClient.getTaxonsByAnyName(fieldValue);
                    List<Taxon> submittableTaxonList=taxonList.stream().filter(taxon -> taxon.isSubmittable()).collect(Collectors.toList());
                    scientificName = !submittableTaxonList.isEmpty() ? submittableTaxonList.get(0).getScientificName() : "";
                }
                
                if(StringUtils.isEmpty(scientificName)){
                    Sample sample;
                    if((sample = getSampleById(fieldValue)) !=null) {
                        // Get scientificName from sample.
                        scientificName = sample.getOrganism();
                    }
                }
                
                if(StringUtils.isEmpty(scientificName)){
                    ValidationMessage<Origin> message = new ValidationMessage<Origin>(Severity.ERROR, "MasterEntrySourceCheck_2", fieldValue);
                    templateProcessorResultSet.getValidationResult().append(new ValidationResult().append(message));
                }else {
                    replaceOrganismTokenWithScientificname(TemplateProcessorConstants.ORGANISM_TOKEN,scientificName);
                    replaceOrganismTokenWithScientificname(TemplateProcessorConstants.ORGANISM_NAME_TOKEN,scientificName);
                }
            }
        }
    }
    
    private void replacePPNotes(TemplateVariables templateVariables) throws Exception {
        if (!template.toString().contains(TemplateProcessorConstants.PP_NOTES_TOKEN))
            return;
        ValidationResult validationResult = new ValidationResult();
        String token18s = templateVariables.getTokenValue("18S");
        String tokenITS1 = templateVariables.getTokenValue("ITS1");
        String token5point8S = templateVariables.getTokenValue("5.8S");
        String tokenITS2 = templateVariables.getTokenValue("ITS2");
        String token28S = templateVariables.getTokenValue("28S");
        boolean TOKEN_FOUND = false;
        StringBuilder builder = new StringBuilder();
        if (token18s != null && !token18s.isEmpty() && token18s.equalsIgnoreCase(TemplateTokenInfo.YES_VALUE)) {
            builder.append("18S rRNA gene");
            TOKEN_FOUND = true;
        }
        if (tokenITS1 != null && !tokenITS1.isEmpty() && tokenITS1.equalsIgnoreCase(TemplateTokenInfo.YES_VALUE)) {
            if (TOKEN_FOUND)
                builder.append(", ");
            builder.append("ITS1");
            TOKEN_FOUND = true;
        }
        if (token5point8S != null && !token5point8S.isEmpty() && token5point8S.equalsIgnoreCase(TemplateTokenInfo.YES_VALUE)) {
            if (TOKEN_FOUND)
                builder.append(", ");
            builder.append("5.8S rRNA gene");
            TOKEN_FOUND = true;
        }
        if (tokenITS2 != null && !tokenITS2.isEmpty() && tokenITS2.equalsIgnoreCase(TemplateTokenInfo.YES_VALUE)) {
            if (TOKEN_FOUND)
                builder.append(", ");
            builder.append("ITS2");
            TOKEN_FOUND = true;
        }
        if (token28S != null && !token28S.isEmpty() && token28S.equalsIgnoreCase(TemplateTokenInfo.YES_VALUE)) {
            if (TOKEN_FOUND)
                builder.append(", ");
            builder.append("28S rRNA gene");
            TOKEN_FOUND = true;
        }
        if (!builder.toString().isEmpty())
            templateVariables.addToken("PP_NOTES", "sequence contains " + builder.toString());
    }

    public void replacePPGene(TemplateVariables templateVariables) {
        if (!template.toString().contains(TemplateProcessorConstants.PP_GENE_TOKEN))
            return;
        String marker = templateVariables.getTokenValue(TemplateProcessorConstants.MARKER_TOKEN);
        if (marker == null || marker.isEmpty())
            return;
        for (TemplateProcessorConstants.MarkerE markerE : TemplateProcessorConstants.MarkerE.values()) {
            if (markerE.getMarker().equals(marker)) {
                template = new StringBuilder(template.toString().replace(TemplateProcessorConstants.PP_GENE_TOKEN, markerE.name()));
                return;
            }
        }
        for (TemplateProcessorConstants.MarkerE markerE : TemplateProcessorConstants.MarkerE.values()) {
            if (markerE.name().equals(marker)) {
                template = new StringBuilder(template.toString().replace(TemplateProcessorConstants.PP_GENE_TOKEN, markerE.name()));
                templateVariables.setTokenValue(TemplateProcessorConstants.MARKER_TOKEN, markerE.getMarker());
                return;
            }
        }
    }

    private void addDataToEntry(Entry entry, TemplateVariables templateVariables) throws TemplateException {
        try {
            if (templateVariables == null)
                return;
            for (String tokenName: templateVariables.getTokenNames()) {
                String tokenValue = templateVariables.getTokenValue(tokenName);
                if (tokenValue == null || tokenValue.isEmpty())
                    continue;
                if (tokenName.toUpperCase().equals(TemplateProcessorConstants.moloculeTypeE.NCRMOLTYPE.name()) || tokenName.toUpperCase().equals(TemplateProcessorConstants.moloculeTypeE.VMOLTYPE.name())) {
                    molType = tokenValue;
                    break;
                }
            }
            Sequence.Topology topology = Sequence.Topology.LINEAR;
            for (String tokenName: templateVariables.getTokenNames()) {
                String tokenValue = templateVariables.getTokenValue(tokenName);
                if (tokenValue == null || tokenValue.isEmpty())
                    continue;
                if(tokenName.equals(TemplateProcessorConstants.TOPOLOGY_TOKEN)) {
                    topology = SequenceEntryUtils.getTopology(tokenValue.trim());
                }
                if (tokenName.equals(TemplateProcessorConstants.SEQUENCE_TOKEN)) {
                    Sequence sequence = new SequenceFactory().createSequence();
                    sequence.setSequence(ByteBuffer.wrap(tokenValue.toLowerCase().getBytes()));
                    sequence.setVersion(1);
                    sequence.setTopology(topology == null ? Sequence.Topology.LINEAR: topology);
                    sequence.setMoleculeType(molType);
                    entry.setSequence(sequence);
                } else if (tokenName.equals(TemplateProcessorConstants.COMMENTS_TOKEN)) {
                    StringWriter writer = new StringWriter();
                    entry.setComment(new Text(tokenValue));
                    FlatFileWriter ccwriter= new CCWriter(entry);
                    ccwriter.setWrapType(WrapType.EMBL_WRAP);
                    ccwriter.setWrapChar(WrapChar.WRAP_CHAR_SPACE);
                    ccwriter.write(writer);
                    entry.setComment(new Text(writer.toString().replace("CC   ", "")));
                }
            }
        } catch (Exception e) {
            throw new TemplateException(e);
        }

    }

    private void addSequenceLengthToken(TemplateVariables variables) {
        if (variables.containsToken(TemplateProcessorConstants.SEQUENCE_TOKEN)) {
            int sequenceLength = variables.getTokenValue(TemplateProcessorConstants.SEQUENCE_TOKEN).length();
            variables.addToken(TemplateProcessorConstants.SEQUENCE_LENGTH_TOKEN, Integer.toString(sequenceLength));
        }
    }

    private void replaceTokens(TemplateVariables tokens) throws TemplateException {
        try {
            if (tokens == null)
                return;
            for (String tokenName : tokens.getTokenNames()) {
                String token = tokens.getTokenValue(tokenName);
                if (token == null || token.isEmpty())
                    continue;//leave empty tokens as we will strip the unmatched token lines
                String delimitedKey = StringBuilderUtils.encloseToken(tokenName);
                if (tokenName.equals(TemplateProcessorConstants.SEQUENCE_TOKEN) || tokenName.equals(TemplateProcessorConstants.COMMENTS_TOKEN))
                    continue;
                doReplace(delimitedKey, token);
            }
        } catch (Exception e) {
            throw new TemplateException(e);
        }
    }

    private void doReplace(String stringToFind, String stringToReplace) {
        template = new StringBuilder(template.toString().replace(stringToFind, stringToReplace));
    }

    private void validateSediment(TemplateProcessorResultSet templateProcessorResultSet, TemplateVariables templateVariables) {
        if (templateVariables.getVariables().containsKey(TemplateProcessorConstants.SEDIMENT_TOKEN)) {
            String sediment = templateVariables.getVariables().get(TemplateProcessorConstants.SEDIMENT_TOKEN);
            for (TemplateProcessorConstants.SedimentE sedimentE: TemplateProcessorConstants.SedimentE.values()) {
                if (sedimentE.getSediment().equals(sediment))
                    return;
            }
            String values = "";
            for (TemplateProcessorConstants.SedimentE sedimentE: TemplateProcessorConstants.SedimentE.values())
                values += sedimentE.getSediment() + ",";
            ValidationMessage<Origin> message = new ValidationMessage<Origin>(Severity.ERROR, "SedimentCheck", sediment, StringUtils.chompLast(values, ","));
            templateProcessorResultSet.getValidationResult().append(new ValidationResult().append(message));
        }
    }

    private void validateMarker(TemplateProcessorResultSet templateProcessorResultSet, TemplateVariables templateVariables) {
        if (templateVariables.getVariables().containsKey(TemplateProcessorConstants.MARKER_TOKEN)) {
            String marker = templateVariables.getVariables().get(TemplateProcessorConstants.MARKER_TOKEN);
            for (TemplateProcessorConstants.MarkerE markerE : TemplateProcessorConstants.MarkerE.values()) {
                if (markerE.getMarker().equals(marker))
                    return;
            }
            String values = "";
            for (TemplateProcessorConstants.MarkerE markerE : TemplateProcessorConstants.MarkerE.values())
                values += markerE.getMarker() + ",";
            ValidationMessage<Origin> message = new ValidationMessage<Origin>(Severity.ERROR, "MarkerCheck", marker, values);
            templateProcessorResultSet.getValidationResult().append(new ValidationResult().append(message));
        }
    }

    /**
     * This method check if the ORGANISM_NAME field value is related to a sample and 
     * updates the entry's sourceFeature using the sample values.
     *
     */
    private void updateSourceFeatureUsingOrganismFieldValue(Entry entry, TemplateVariables templateVariables) throws Exception {

        // Validate and get samples
        Sample sample = validateAndGetSample(templateVariables);
        
        if (sample != null && entry.getPrimarySourceFeature() != null) {
            SourceFeature sourceFeature = entry.getPrimarySourceFeature();
            SampleInfo sampleInfo = getSampleInfo(sample);
            SampleEntity sampleEntity = getSampleEntity(sample);
            updateSourceFeature(sourceFeature, sampleEntity, sampleInfo);
            entry.addXRef(new XRef("BioSample", sample.getBioSampleId()));
        }
    }

    /**
     * Returns sample if one could be found using the ORGANISM_NAME field of the TSV 
     * file or NULL if the ORGANISM_NAME was a scientific name or tax id.
     */
    public Sample validateAndGetSample(TemplateVariables templateVariables) throws Exception {

        Map<String, String> tsvFieldMap = templateVariables.getVariables();
        Sample sample = null;
        
        // Iterate TSV header fields.
        for (String tsvHeader : tsvFieldMap.keySet()) {
            if (isOrganismField(tsvHeader)) {
                // When TSV header cell value is ORGANISM_NAME or ORGANISM

                if (Utils.isValidTaxId(tsvFieldMap.get(tsvHeader))) {
                    // When field value is taxId pattern DO NOTHING
                    break;
                }

                /** When tsv value do NOT match taxId pattern then retrieve sample assuming that the passed 
                 *  value is sampleId, bioSampleId, or sample alias.
                 *  If no sample is returned then the value is organism name.
                 */
                String sampleValue = tsvFieldMap.get(tsvHeader);
                // Get sample from cache if exists. 
                sample = getSampleById(sampleValue);
                break;
            }
        }
        return sample;
    }
    
    public Sample getSampleById(String sampleId){
        // Get sample from cache if exists. 
        Sample sample = sampleCache.get(sampleId);
        if (sample == null) {
            try {
                // Get sample using server sample retrieval service.
                SampleRetrievalService sampleRetrievalService = SequenceToolsServices.sampleRetrievalService();
                sample = sampleRetrievalService.getSample(sampleId);
                sampleCache.put(sampleId, sample);
            }catch (Exception serviceiException){
                // DO NOTHING when there is no sample
            }
        }
        return sample;
    }

    private SampleEntity getSampleEntity(Sample sample){
        SampleEntity sampleEntity = new SampleEntity();
        Map<String,String> attributeMap=new HashMap();
        for(Attribute attribute: sample.getAttributes()){
            attributeMap.put(attribute.getName(),attribute.getValue());
        }
        sampleEntity.setAttributes(attributeMap);
        return sampleEntity;
    }

    private SourceFeature updateSourceFeature(SourceFeature sourceFeature,SampleEntity sampleEntity,SampleInfo sampleInfo) throws Exception {
        return new SourceFeatureUtils().updateSourceFeature(sourceFeature, sampleEntity, new TaxonomyClient(), sampleInfo);
    }

    public SourceFeature createSourceFeature(SampleEntity sampleEntity,SampleInfo sampleInfo) throws Exception {
        return new SourceFeatureUtils().constructSourceFeature(sampleEntity, new TaxonomyClient(), sampleInfo);
    }

    private SampleInfo getSampleInfo(Sample sample){
        SampleInfo sampleInfo=new SampleInfo();
        sampleInfo.setScientificName(sample.getOrganism());
        sampleInfo.setUniqueName(sample.getName());
        if(sample.getTaxId()!=null) {
            sampleInfo.setTaxId(Long.valueOf(sample.getTaxId()));
        }
        return sampleInfo;
    }
    
    private boolean templateContainsOrganismToken(){
        return template.toString().contains(TemplateProcessorConstants.ORGANISM_TOKEN) ||
                template.toString().contains(TemplateProcessorConstants.ORGANISM_NAME_TOKEN);
    }
    
    private boolean isOrganismField(String fieldName){
        return StringUtils.equalsIgnoreCase(fieldName,TemplateProcessorConstants.ORGANISM_TOKEN) ||
                StringUtils.equalsIgnoreCase(fieldName,TemplateProcessorConstants.ORGANISM_NAME_TOKEN);
    }
    
    private void replaceOrganismTokenWithScientificname(String organismToken, String scientificName){
        String delimitedKey = StringBuilderUtils.encloseToken(organismToken);
        template = new StringBuilder(template.toString().replace(delimitedKey, scientificName));
    }
}
