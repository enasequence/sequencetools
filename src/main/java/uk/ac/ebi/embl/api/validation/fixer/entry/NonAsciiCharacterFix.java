package uk.ac.ebi.embl.api.validation.fixer.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.Utils;

/**
 * Fix works for certain non-ascii characters only. Check Utils.removeAccents limitations.
 * If it is not possible to transliterate certain chars, it will be caught in and rejected
 * by AsciiCharacterCheck.
 */
@Description("Non-ascii characters fixed from \"{0}\" to \"{1}\".")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class NonAsciiCharacterFix extends EntryValidationCheck {
    private static final String ASCII_CHARACTER_FIX = "AsciiCharacterFix_1";

    public ValidationResult check(Entry entry) {
        result = new ValidationResult();

        if (entry == null)
            return result;

        attemptFix(entry.getComment());
        attemptFix(entry.getDescription());

        for (Reference reference : entry.getReferences()) {
            if (reference.getPublication() != null) {
                String pubTitle = reference.getPublication().getTitle();
                if (pubTitle != null) {
                    String fixedPubTitle = fixedStr(pubTitle);
                    if (!fixedPubTitle.equals(pubTitle)) {
                        reference.getPublication().setTitle(fixedPubTitle);
                        reportMessage(Severity.FIX, reference.getOrigin(), ASCII_CHARACTER_FIX, pubTitle, fixedPubTitle);
                    }
                }

                if (reference.getPublication().getAuthors() != null) {
                    for (Person author : reference.getPublication().getAuthors()) {
                        String firstName = author.getFirstName();
                        if (firstName != null) {
                            String fixedFirstName = fixedStr(firstName);
                            if (!fixedFirstName.equals(firstName)) {
                                author.setFirstName(fixedFirstName);
                                reportMessage(Severity.FIX, reference.getOrigin(), ASCII_CHARACTER_FIX, firstName, fixedFirstName);
                            }
                        }

                        String surname = author.getSurname();
                        if (surname != null) {
                            String fixedSurname = fixedStr(surname);
                            if (!fixedSurname.equals(surname)) {
                                author.setSurname(fixedSurname);
                                reportMessage(Severity.FIX, reference.getOrigin(), ASCII_CHARACTER_FIX, surname, fixedSurname);
                            }
                        }
                    }
                }
            }
        }
        for (Feature feature : entry.getFeatures()) {
            for (Qualifier qualifier : feature.getQualifiers()) {
                if (qualifier.getName().equals(Qualifier.COUNTRY_QUALIFIER_NAME)
                        || qualifier.getName().equals(Qualifier.ISOLATE_QUALIFIER_NAME) ) {

                    String qualifierValue = qualifier.getValue();
                    if (qualifierValue != null) {
                        String fixedVal = fixedStr(qualifierValue);
                        if (!fixedVal.equals(qualifierValue)) {
                            qualifier.setValue(fixedVal);
                            reportMessage(Severity.FIX, qualifier.getOrigin(), ASCII_CHARACTER_FIX, qualifierValue, fixedVal);
                        }
                    }
                }
            }
        }
        return result;
    }

    private void attemptFix(Text text) {
        if (text != null && text.getText() != null) {
            if (Utils.hasNonAscii(text.getText())) {
                String fixed = Utils.removeAccents(text.getText());
                if (!fixed.equals(text.getText())) {
                    text.setText(fixed);
                    reportMessage(Severity.FIX, text.getOrigin(), ASCII_CHARACTER_FIX, text.getText(), fixed);
                }
            }
        }
    }

    private String fixedStr(String str) {
        if (Utils.hasNonAscii(str)) {
            return Utils.removeAccents(str);
        }
        return str;
    }
}
