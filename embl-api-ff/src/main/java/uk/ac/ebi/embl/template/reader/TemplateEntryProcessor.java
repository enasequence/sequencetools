package uk.ac.ebi.embl.template.reader;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.plan.ValidationPlan;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.writer.embl.CCWriter;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblSequenceWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;

import static uk.ac.ebi.embl.template.reader.TemplateProcessorConstants.SEQUENCE_LENGTH_TOKEN;
import static uk.ac.ebi.embl.template.reader.TemplateProcessorConstants.SEQUENCE_TOKEN;

public class TemplateEntryProcessor {
    private StringBuilder currentEntryBuilder;
    private TemplateInfo templateInfo;
    private ValidationPlan validationPlan;
    private Connection connEra;

    public TemplateEntryProcessor(Connection connEra) {
        this.connEra = connEra;
        EmblEntryValidationPlanProperty emblEntryValidationProperty=new EmblEntryValidationPlanProperty();
        emblEntryValidationProperty.validationScope.set(ValidationScope.EMBL_TEMPLATE);
        emblEntryValidationProperty.isDevMode.set(false);
        emblEntryValidationProperty.isFixMode.set(true);
        emblEntryValidationProperty.isAssembly.set(false);
        emblEntryValidationProperty.minGapLength.set(0);
        validationPlan = new EmblEntryValidationPlan(emblEntryValidationProperty);
        validationPlan.addMessageBundle(TemplateProcessorConstants.TEMPLATE_MESSAGES_BUNDLE);
        validationPlan.addMessageBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    }

    protected TemplateProcessorResultSet processEntry(TemplateInfo templateInfo, TemplateVariables variables, Integer entryNumber) throws Exception {
        String templateString = templateInfo.getTemplateString();
        if (templateString.contains(StringBuilderUtils.encloseToken(TemplateProcessorConstants.CITATION_TOKEN)))
            templateString = templateString.replace(StringBuilderUtils.encloseToken(TemplateProcessorConstants.CITATION_TOKEN), "");
        if (templateString.contains(StringBuilderUtils.encloseToken(TemplateProcessorConstants.STATUS_DATE_TOKEN)))
            templateString = templateString.replace(StringBuilderUtils.encloseToken(TemplateProcessorConstants.STATUS_DATE_TOKEN), "");
        this.currentEntryBuilder = new StringBuilder(templateString);
        this.templateInfo = templateInfo;
        addSequenceLengthToken(variables);
        replaceTokens(variables);
        new SectionExtractor().removeSections(currentEntryBuilder, templateInfo.getSections(), variables);
        StringBuilderUtils.removeUnmatchedTokenLines(currentEntryBuilder);
        TemplateProcessorResultSet templateProcessorResultSet = new TemplateProcessorResultSet();
        templateProcessorResultSet.setEntryNumber(entryNumber);
        String processedEntryString = currentEntryBuilder.toString().trim();
        BufferedReader stringReader = new BufferedReader(new StringReader(currentEntryBuilder.toString().trim()));
        EntryReader entryReader = new EmblEntryReader(stringReader);
        try {
            entryReader.read();
        } catch (Exception e) {
            throw e;
        }
        if (entryReader.isEntry()) {
            Entry entry = entryReader.getEntry();
            entry.setStatus(Entry.Status.PRIVATE);
            if (templateInfo.getAnalysisId() != null && !templateInfo.getAnalysisId().isEmpty()) {
                Reference reference = getReferences();
                if (reference != null)
                    entry.getReferences().add(reference);
            }
            templateProcessorResultSet.setValidationPlanResult(validationPlan.execute(entry));
            templateProcessorResultSet.setEntry(entry);
        }
        templateProcessorResultSet.setEntryString(processedEntryString);
        return templateProcessorResultSet;
    }

    private Reference getReferences() throws Exception {
        try {
            return new EraproDAOUtilsImpl(connEra).getSubmitterReference(templateInfo.getAnalysisId());
        } catch (Exception e) {
            throw e;
        }
    }


    private void addSequenceLengthToken(TemplateVariables variables) {
        if (variables.containsToken(SEQUENCE_TOKEN)) {
            int sequenceLength = variables.getTokenValue(SEQUENCE_TOKEN).length();
            variables.addToken(SEQUENCE_LENGTH_TOKEN, Integer.toString(sequenceLength));
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
                if (tokenName.equals(SEQUENCE_TOKEN))
                    writeSequenceBlock(token, delimitedKey);
                else if (tokenName.equals(TemplateProcessorConstants.COMMENTS_TOKEN))
                    writeCommentBlock(token, delimitedKey);
                else
                    doReplace(delimitedKey, token);
            }
        } catch (IOException e) {
            throw new TemplateException(e);
        }
    }

    private void writeSequenceBlock(String token, String delimitedKey) throws IOException {
        Sequence sequence = new SequenceFactory().createSequenceByte(token.getBytes());
        StringWriter writer = new StringWriter();
        Entry entry = new EntryFactory().createEntry();
        new EmblSequenceWriter(entry, sequence).write(writer);
        String sequenceString = writer.toString();
        doReplace(delimitedKey, sequenceString);
    }

    private void writeCommentBlock(String token, String delimitedKey) throws IOException {
        StringWriter writer = new StringWriter();
        Entry entry = new EntryFactory().createEntry();
        entry.setComment(new Text(token));
        new CCWriter(entry).write(writer);
        String commentBlock = writer.toString();
        doReplace(delimitedKey, commentBlock);
    }

    private void doReplace(String stringToFind, String stringToReplace) {
        String currentEntryString = currentEntryBuilder.toString();
        currentEntryBuilder = new StringBuilder(currentEntryString.replace(stringToFind, stringToReplace));
    }
}
