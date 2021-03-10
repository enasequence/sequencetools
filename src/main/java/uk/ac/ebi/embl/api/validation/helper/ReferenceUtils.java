package uk.ac.ebi.embl.api.validation.helper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import uk.ac.ebi.embl.api.entry.reference.*;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.dao.entity.ReferenceEntity;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblPersonMatcher;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class ReferenceUtils {

    public Reference getReference(String authors, String address, Date date, String submissionAccountId) throws ValidationEngineException {
        Publication publication = getPublication(address, date);
        if (doAddAuthorsToConsortium(submissionAccountId)) {
            publication.setConsortium(authors);
        } else {
            List<Person> authorList = getAuthors(authors);
            if (authors.isEmpty()) {
                throw new ValidationEngineException("Authors value is invalid:" + authors);
            }
            publication.addAuthors(authorList);
        }
        Reference ref = new ReferenceFactory().createReference();
        ref.setPublication(publication);
        ref.setAuthorExists(true);
        ref.setLocationExists(true);
        ref.setReferenceNumber(1);
        return ref;
    }
    private  Publication getPublication(String address, Date date) {
        Submission submission = (new ReferenceFactory()).createSubmission();
        submission.setDay(date == null ? Calendar.getInstance().getTime(): date);
        if(address != null) {
            submission.setSubmitterAddress(address);
        }
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

    boolean doAddAuthorsToConsortium(String submissionAccountId) {
        return submissionAccountId != null && submissionAccountId.equalsIgnoreCase("Webin-55551");
    }

    public Reference constructReference(List<ReferenceEntity> referenceEntityList) throws UnsupportedEncodingException {
        Publication publication = new Publication();
        ReferenceFactory referenceFactory = new ReferenceFactory();
        Reference reference = referenceFactory.createReference();
        HashSet<String> consortium = new HashSet<>();
        String pubConsortium = "";

        for (ReferenceEntity refEntity : referenceEntityList) {
            String pConsrtium = refEntity.getConsortium();
            consortium.add(pConsrtium);

            if (pConsrtium == null)//ignore first_name,middle_name and last_name ,if consortium is given : WAP-126
            {
                Person person = null;

                person = referenceFactory.createPerson(
                        EntryUtils.concat(" ", WordUtils.capitalizeFully(
                                EntryUtils.convertNonAsciiStringtoAsciiString(refEntity.getSurname()), '-', ' '),
                                EntryUtils.convertNonAsciiStringtoAsciiString(refEntity.getMiddleInitials())),
                        getFirstName(EntryUtils.convertNonAsciiStringtoAsciiString(refEntity.getFirstName())));

                publication.addAuthor(person);
                reference.setAuthorExists(true);
            }

            Submission submission = referenceFactory.createSubmission(publication);
            submission.setSubmitterAddress(getAddress(refEntity));
            Date date = EntryUtils.getDay(refEntity.getFirstCreated());
            submission.setDay(date);
            publication = submission;
            reference.setPublication(publication);
            reference.setLocationExists(true);
            reference.setReferenceNumber(1);
        }

        for (String refCons : consortium) {
            if (refCons != null)
                pubConsortium += refCons + ", ";
        }
        if (pubConsortium.endsWith(", ")) {
            pubConsortium = pubConsortium.substring(0, pubConsortium.length() - 2);
        }
        if (reference.getPublication() == null)
            return null;
        reference.getPublication().setConsortium(pubConsortium);

        return reference;
    }

    public String getAddress(ReferenceEntity refEntity) throws UnsupportedEncodingException {
        //-For brokers, we need to use broker_name instead of center_name
        //-laboratory_name should only be part of address for non-brokers
        if (refEntity.isBroker()) {
            return EntryUtils.concat(", ",
                    EntryUtils.convertNonAsciiStringtoAsciiString(refEntity.getBrokerName()),
                    EntryUtils.convertNonAsciiStringtoAsciiString(refEntity.getLaboratoryName()),
                    EntryUtils.convertNonAsciiStringtoAsciiString(refEntity.getAddress()),
                    EntryUtils.convertNonAsciiStringtoAsciiString(refEntity.getCountry()));
        } else {
            return EntryUtils.convertNonAsciiStringtoAsciiString(refEntity.getLaboratoryName());
        }
    }

    private String getFirstName(String firstName)
    {
        if(firstName==null)
            return null;
        StringBuilder nameBuilder= new StringBuilder();
        if(StringUtils.containsNone(firstName,"@")&&StringUtils.contains(firstName, "-"))
        {
            String[] names=StringUtils.split(firstName,"-");
            List<String> fnames=Arrays.asList(names);
            int i=0;
            for(String n: fnames)
            {
                i++;
                nameBuilder.append(WordUtils.initials(n).toUpperCase());
                if(i==fnames.size())
                    nameBuilder.append(".");
                else
                    nameBuilder.append(".-");
            }
        }
        else
        {
            nameBuilder.append(firstName.toUpperCase().charAt(0));
            nameBuilder.append(".");
        }
        return nameBuilder.toString();
    }
}
