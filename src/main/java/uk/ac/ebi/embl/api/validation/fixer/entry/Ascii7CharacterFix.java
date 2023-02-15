package uk.ac.ebi.embl.api.validation.fixer.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.Ascii7CharacterConverter;

import java.util.function.Consumer;

/**
 * Removes diacritics and replaces non-printable ASCII7 characters with ? from:
 * <ul>
 *     <li>comment</li>
 *     <li>description</li>
 *     <li>reference title</li>
 *     <li>reference author first name</li>
 *     <li>reference author surname</li>
 *     <li>feature qualifiers</li>
 * </ul>
 */
public class Ascii7CharacterFix extends EntryValidationCheck {
    private static final String FIX_ID = "Ascii7CharacterFix_1";

    private final Ascii7CharacterConverter converter = new Ascii7CharacterConverter();

    public ValidationResult check(Entry entry) {
        result = new ValidationResult();

        if (entry == null)
            return result;

        if (entry.getComment() != null) {
            fix(entry.getComment().getText(), entry.getComment().getOrigin(),
                    fixedText -> entry.getComment().setText(fixedText));
        }
        if (entry.getDescription() != null) {
            fix(entry.getDescription().getText(), entry.getDescription().getOrigin(),
                    fixedText -> entry.getDescription().setText(fixedText));
        }

        for (Reference reference : entry.getReferences()) {
            if (reference.getPublication() != null) {
                String pubTitle = reference.getPublication().getTitle();
                if (pubTitle != null) {
                    fix(pubTitle, reference.getOrigin(),
                            fixedPubTitle -> reference.getPublication().setTitle(fixedPubTitle));
                }

                if (reference.getPublication().getAuthors() != null) {
                    for (Person author : reference.getPublication().getAuthors()) {
                        String firstName = author.getFirstName();
                        if (firstName != null) {
                            fix(firstName, reference.getOrigin(),
                                    fixedFirstName -> author.setFirstName(fixedFirstName));
                        }

                        String surname = author.getSurname();
                        if (surname != null) {
                            fix(surname, reference.getOrigin(),
                                    fixedSurname -> author.setSurname(fixedSurname));
                        }
                    }
                }
            }
        }
        for (Feature feature : entry.getFeatures()) {
            for (Qualifier qualifier : feature.getQualifiers()) {
                String qualifierValue = qualifier.getValue();
                if (qualifierValue != null) {
                    fix(qualifierValue, qualifier.getOrigin(),
                            fixedVal -> qualifier.setValue(fixedVal));
                }
            }
        }
        return result;
    }

    private void fix(String str, Origin origin, Consumer<String> replaceStr) {
        if (Ascii7CharacterConverter.doConvert(str)) {
            String fixedStr = converter.convert(str);
            reportMessage(Severity.FIX, origin, FIX_ID, str, fixedStr);
            replaceStr.accept(fixedStr);
        }
    }
}
