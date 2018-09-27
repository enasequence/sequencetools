package uk.ac.ebi.embl.api.validation.template;

import org.apache.log4j.Logger;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;

public class TemplateProcessor {
    private static final Logger LOGGER = Logger.getLogger(TemplateProcessor.class);
    private int maxProcessingSize = -1;//default - process all
    private TemplateInfo templateInfo;
    private TemplateEntryProcessor entryProcessor;
    private final static String MOL_TYPE = "/mol_type";

    public TemplateProcessor() {
    }

    public TemplateProcessor(TemplateInfo templateInfo, Connection connEra) {
        this.templateInfo = templateInfo;
        this.entryProcessor = new TemplateEntryProcessor(connEra);
    }

    private String getMolTypeFromTemplate() throws Exception {
        String template = templateInfo.getTemplateString();
        String molType = "";
        if (template.toString().contains(MOL_TYPE)) {
            String start = template.toString().substring(template.toString().indexOf(MOL_TYPE) + MOL_TYPE.length());
            if (start.contains("\"")) {
                start = start.substring(start.indexOf("\"") + 1);
                if (start.contains("\""))
                    molType = start.substring(0, start.indexOf("\""));
                else
                    throw new Exception("Found MOL TYPE in template but there is no ending '\"' (double quotes).");
            } else
                throw new Exception("Found MOL TYPE in template but there is no starting '\"' (double quotes).");
        } else
            throw new Exception("Template is missing MOL TYPE.");
        return molType;
    }

    public TemplateProcessorResultSet process(TemplateVariables templateVariables) throws Exception {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
        incrementAndDecrementTokans(templateVariables, templateInfo);
        String molType = getMolTypeFromTemplate();
        reportEntryCount(templateVariables.getSequenceNumber());
        TemplateProcessorResultSet templateProcessorResultSet = entryProcessor.processEntry(templateInfo, molType, templateVariables);
        /**
         * check no identical records
         */
//        new FieldUniquenessChecker().check(templateVariables, templateProcessorResultSetsL);
        return templateProcessorResultSet;
    }

    private void incrementAndDecrementTokans(TemplateVariables variables, TemplateInfo templateInfo) throws TemplateException {
        new TokenIncrementor().processIncrementAndDecrementTokens(templateInfo, variables);
    }

    private void reportEntryCount(Integer entrynumber) {
        if (entrynumber % 10000 == 0)
            LOGGER.info("variableKey = " + entrynumber);
    }

    public String getTemplate(String templateId) throws Exception {
        StringBuilder template = new StringBuilder();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("templates/" + templateId + ".xml")));
        buffer.lines().forEach(p -> template.append(p.trim() + "\n"));

//        String template  = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("templates/" + templateId + ".xml")).trim();
        return template.toString();
    }
}