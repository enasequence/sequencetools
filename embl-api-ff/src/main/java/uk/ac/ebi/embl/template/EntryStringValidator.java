package uk.ac.ebi.embl.template;

import org.apache.log4j.Logger;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.feature.CdsFeatureTranslationCheck;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.plan.ValidationPlan;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class EntryStringValidator {
    private static final Logger LOGGER = Logger.getLogger(EntryStringValidator.class);
    private ValidationPlan validator;

    public EntryStringValidator() {
        EmblEntryValidationPlanProperty emblEntryValidationProperty=new EmblEntryValidationPlanProperty();
        emblEntryValidationProperty.validationScope.set(ValidationScope.EMBL_TEMPLATE);
        emblEntryValidationProperty.isDevMode.set(false);
        emblEntryValidationProperty.isFixMode.set(true);
        emblEntryValidationProperty.isAssembly.set(false);
        emblEntryValidationProperty.minGapLength.set(0);
        validator = new EmblEntryValidationPlan(emblEntryValidationProperty);
        validator.addMessageBundle(TemplateProcessorConstants.TEMPLATE_MESSAGES_BUNDLE);
        validator.addMessageBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    }

    @SuppressWarnings("unchecked")
    public List<ValidationPlanResult> validateEntryString(String currentEntryString) throws TemplateException {
        BufferedReader stringReader = new BufferedReader(new StringReader(currentEntryString));
        EntryReader reader = new EmblEntryReader(stringReader);
        List<ValidationPlanResult> results = new ArrayList<ValidationPlanResult>();
        int count = 1;
        ValidationResult parseResults = parseNextEntryFromReader(reader);
        while (reader.isEntry()) {
            if (count % 10000 == 0)
                LOGGER.info("count = " + count);
            Entry entry = reader.getEntry();
            ValidationPlanResult validationPlanResult = null;
            try {
                validationPlanResult = validator.execute(entry);
            } catch (ValidationEngineException e) {
                throw new TemplateException(e);
            }
            /**first set any report messages (probably exceptional that the translation report needs to get set outside
             the embl-api-core package due to the need for embl-ff writers**/
            for (ValidationResult result : validationPlanResult.getResults()) {
                if (result instanceof ExtendedResult) {
                    ExtendedResult<?> extendedResult = (ExtendedResult<?>) result;
                    if (extendedResult.getExtension() instanceof CdsFeatureTranslationCheck.TranslationReportInfo)
                        FlatFileValidations.setCDSTranslationReport((ExtendedResult<CdsFeatureTranslationCheck.TranslationReportInfo>) extendedResult);
                }
            }
            validationPlanResult.append(parseResults);//add any parse results that may also have been created
            results.add(validationPlanResult);
            parseResults = parseNextEntryFromReader(reader);
            count++;
        }
        /**
         * if the last iteration of the loop does not occur due to parse errors resulting in a non-valid entry,
         * append the parse results to a new validationPlanResult to these get reported
         */
        if (parseResults.count() > 0) {
            ValidationPlanResult planResult = new ValidationPlanResult();
            planResult.append(parseResults);
            results.add(planResult);
        }
        return results;
    }

    public ValidationResult parseNextEntryFromReader(EntryReader reader) throws TemplateException {
        try {
            ValidationResult parseResult = reader.read();
            return parseResult;
        } catch (IOException e) {
            throw new TemplateException(e);
        }
    }
}