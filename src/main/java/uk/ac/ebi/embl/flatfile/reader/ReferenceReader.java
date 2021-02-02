package uk.ac.ebi.embl.flatfile.reader;

import uk.ac.ebi.embl.api.entry.reference.*;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblPersonMatcher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReferenceReader {

    public Reference getReference(String authors, String address, Date date) throws ValidationEngineException {
        Publication publication = getPublication(address, date);
        List<Person> authorList = getAuthors(authors);
        if (authors.isEmpty()) {
            throw new ValidationEngineException("Authors value is invalid:" + authors, ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
        }
        Reference ref = new ReferenceFactory().createReference();
        publication.addAuthors(authorList);
        ref.setPublication(publication);
        ref.setAuthorExists(true);
        ref.setLocationExists(true);
        ref.setReferenceNumber(1);
        return ref;
    }
    private  Publication getPublication(String block, Date date) {
        Submission submission = (new ReferenceFactory()).createSubmission();
        submission.setDay(date == null ? Calendar.getInstance().getTime(): date);
        submission.setSubmitterAddress(block);
        return submission;
    }

    private List<Person> getAuthors(String block) {
        List<Person> authors = new ArrayList<>();
        block = FlatFileUtils.remove(block, ';');
        for (String author : FlatFileUtils.split(block, ",")) {
            EmblPersonMatcher personMatcher = new EmblPersonMatcher(null);
            if (!personMatcher.match(author)) {
                authors.add(new ReferenceFactory().createPerson( author, null));
            }
            else {
                authors.add(personMatcher.getPerson());
            }
        }
        return authors;
    }

}
