package uk.ac.ebi.embl.template.reader;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import java.sql.Connection;

public class TemplateProcessor {
    private static final Logger LOGGER = Logger.getLogger(TemplateProcessor.class);
    private int maxProcessingSize = -1;//default - process all
    private TemplateInfo templateInfo;
    private TemplateEntryProcessor entryProcessor;

    public TemplateProcessor(TemplateInfo templateInfo, Connection connEra) {
        this.templateInfo = templateInfo;
        this.entryProcessor = new TemplateEntryProcessor(connEra);
    }

    public List<TemplateProcessorResultSet> process(TemplateVariablesSet variables) throws Exception {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
        ArrayList<Integer> entryNumbers = new ArrayList<Integer>(variables.getEntryNumbers());
        List<TemplateProcessorResultSet> templateProcessorResultSetsL = new ArrayList<TemplateProcessorResultSet>();
        incrementAndDecrementTokans(variables, templateInfo);
        if (maxProcessingSize < 0) //not set
            maxProcessingSize = variables.getEntryCount();
        if (entryNumbers.size() > 0) {
            int count = 0;
            for (Integer entrynumber: entryNumbers) {
                reportEntryCount(entrynumber);
                TemplateVariables entryValues = variables.getEntryValues(entrynumber);
                TemplateProcessorResultSet templateProcessorResultSet = entryProcessor.processEntry(templateInfo, entryValues, entrynumber);
                templateProcessorResultSetsL.add(templateProcessorResultSet);
                count++;
                if (count >= maxProcessingSize)
                    break;
            }
        }
        /**
         * check no identical records
         */
        new FieldUniquenessChecker().check(variables, templateProcessorResultSetsL);
        return templateProcessorResultSetsL;
    }

    private void incrementAndDecrementTokans(TemplateVariablesSet variables, TemplateInfo templateInfo) throws TemplateException {
        new TokenIncrementor().processIncrementAndDecrementTokens(templateInfo, variables);
    }

    private void reportEntryCount(Integer entrynumber) {
        if (entrynumber % 10000 == 0)
            LOGGER.info("variableKey = " + entrynumber);
    }
}