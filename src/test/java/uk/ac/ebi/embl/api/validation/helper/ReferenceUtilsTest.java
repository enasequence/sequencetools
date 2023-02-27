package uk.ac.ebi.embl.api.validation.helper;


import org.easymock.EasyMock;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.dao.model.SubmissionAccount;
import uk.ac.ebi.embl.api.validation.dao.model.SubmissionContact;
import uk.ac.ebi.embl.api.validation.dao.model.SubmitterReference;
import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

public class ReferenceUtilsTest  {

    ReferenceUtils refutils = new ReferenceUtils();

    @Test
    public void testConstructReferenceWithoutConsortiumAndBroker() throws ValidationEngineException, UnsupportedEncodingException, ParseException {

        List<SubmissionContact> submissionContactList = new ArrayList<>();
        SubmissionContact submissionContact = new SubmissionContact();
        submissionContact.setConsortium(null);
        submissionContact.setSurname("Vijay");
        submissionContact.setMiddleInitials(null);
        submissionContact.setFirstName("Senthil");
        submissionContactList.add(submissionContact);

        SubmissionAccount submissionAccount = new SubmissionAccount();
        submissionAccount.setBrokerName(null);
        submissionAccount.setCenterName("EBI.(EBL-EBI)");
        submissionAccount.setLaboratoryName("EBI Lab");
        submissionAccount.setAddress("Hinxton, Cambridgeshire");
        submissionAccount.setCountry("United Kingdom");

        SubmitterReference submitterReference = new SubmitterReference(submissionContactList, submissionAccount);
        submitterReference.setSubmissionAccountId("Webin-256");
        submitterReference.setFirstCreated(FlatFileDateUtils.getDay("22-SEP-2020"));

        Reference reference = refutils.constructSubmitterReference(submitterReference);
        assertNotNull(reference);
        //centerName, LaboratoryName, Address,Country
        assertEquals("EBI.(EBL-EBI), EBI Lab, Hinxton, Cambridgeshire, United Kingdom", ((Submission) reference.getPublication()).getSubmitterAddress());
        assertTrue(FlatFileWriter.isBlankString(reference.getPublication().getConsortium()));
        assertEquals(1, reference.getPublication().getAuthors().size());
        Person person = reference.getPublication().getAuthors().get(0);
        assertEquals("Vijay", person.getSurname().trim());
        assertEquals("S.", person.getFirstName());
    }

    @Test
    public void testConstructReferenceMultipleSubmissionContactNonBroker() throws ValidationEngineException, UnsupportedEncodingException, ParseException {

        List<SubmissionContact> submissionContactList = new ArrayList<>();
        SubmissionContact submissionContact = new SubmissionContact();
        submissionContact.setConsortium(null);
        submissionContact.setSurname("Vijay");
        submissionContact.setMiddleInitials(null);
        submissionContact.setFirstName("Nathan");
        submissionContactList.add(submissionContact);

        SubmissionContact submissionContact1 = new SubmissionContact();
        submissionContact1.setConsortium(null);
        submissionContact1.setSurname("Nathan");
        submissionContact1.setMiddleInitials("Vijay");
        submissionContact1.setFirstName("Senthil");
        submissionContactList.add(submissionContact1);


        SubmissionAccount submissionAccount = new SubmissionAccount();
        submissionAccount.setBrokerName(null);
        submissionAccount.setCenterName("EBI.(EBL-EBI)");
        submissionAccount.setLaboratoryName("EBI Lab");
        submissionAccount.setAddress("Hinxton, Cambridgeshire");
        submissionAccount.setCountry("United Kingdom");

        SubmitterReference submitterReference = new SubmitterReference(submissionContactList, submissionAccount);
        submitterReference.setSubmissionAccountId("Webin-256");
        submitterReference.setFirstCreated(FlatFileDateUtils.getDay("22-SEP-2020"));

        Reference reference = refutils.constructSubmitterReference(submitterReference);
        assertNotNull(reference);
        //centerName, LaboratoryName, Address,Country
        assertEquals("EBI.(EBL-EBI), EBI Lab, Hinxton, Cambridgeshire, United Kingdom", ((Submission) reference.getPublication()).getSubmitterAddress());
        assertTrue(FlatFileWriter.isBlankString(reference.getPublication().getConsortium()));
        assertEquals(2, reference.getPublication().getAuthors().size());
        Person person = reference.getPublication().getAuthors().get(0);
        assertEquals("Vijay", person.getSurname().trim());
        assertEquals("N.", person.getFirstName());
        Person person1 = reference.getPublication().getAuthors().get(1);
        assertEquals("Nathan Vijay", person1.getSurname().trim());
        assertEquals("S.", person1.getFirstName());
    }

    @Test
    public void testConstructReferenceWithConsortiumAndBroker() throws UnsupportedEncodingException, ValidationEngineException, ParseException {

        List<SubmissionContact> submissionContactList = new ArrayList<>();
        SubmissionContact submissionContact = new SubmissionContact();
        submissionContact.setConsortium("AB bioinformatics inc");
        submissionContact.setSurname("Vijay");
        submissionContact.setMiddleInitials(null);
        submissionContact.setFirstName("Senthil");
        submissionContactList.add(submissionContact);

        SubmissionAccount submissionAccount = new SubmissionAccount();
        submissionAccount.setBrokerName("AB bioinformatics");
        submissionAccount.setCenterName("R&D unit EBI.(EBL-EBI)");
        submissionAccount.setLaboratoryName("EBI Lab");
        submissionAccount.setAddress("Hinxton, Cambridgeshire");
        submissionAccount.setCountry("United Kingdom");

        SubmitterReference submitterReference = new SubmitterReference(submissionContactList, submissionAccount);
        submitterReference.setSubmissionAccountId("Webin-256");
        submitterReference.setFirstCreated(FlatFileDateUtils.getDay("22-SEP-2020"));

        Reference reference = refutils.constructSubmitterReference(submitterReference);
        assertNotNull(reference);
        //brokername,address,country
        assertEquals("AB bioinformatics, Hinxton, Cambridgeshire, United Kingdom", ((Submission) reference.getPublication()).getSubmitterAddress());
        assertEquals(0, reference.getPublication().getAuthors().size());//No authors
        assertEquals("AB bioinformatics inc",reference.getPublication().getConsortium());
    }

    @Test
    public void testGetSubmitterReferenceFromManifest() throws ValidationEngineException {
        String authors = "Vijay Senthil,Nathan S Vijay";
        String address ="Hinxton,Cambridge,UK";
        Date date = new Date();
        String submissionAccountId = "Webin-256";
        Reference reference = refutils.getSubmitterReferenceFromManifest(authors, address, date, submissionAccountId);
        assertNotNull(reference);
        assertEquals(address, ((Submission) reference.getPublication()).getSubmitterAddress());
        assertTrue(FlatFileWriter.isBlankString(reference.getPublication().getConsortium()));
        assertEquals(2, reference.getPublication().getAuthors().size());
        Person person1 = reference.getPublication().getAuthors().get(0);
        assertEquals("Vijay Senthil", person1.getSurname().trim());
        assertNull( person1.getFirstName());
        Person person2 = reference.getPublication().getAuthors().get(1);
        assertEquals("Nathan S Vijay", person2.getSurname().trim());
        assertNull(person2.getFirstName());

    }

    @Test
    public void testGetSubmitterReferenceFromManifestAddAuthorstoCosortium() throws ValidationEngineException, NoSuchMethodException {
        String authors = "Vijay Senthil,Nathan S Vijay";
        String address ="Hinxton,Cambridge,UK";
        Date date = new Date();
        String submissionAccountId = "Webin-256";

        ReferenceUtils referenceUtilsMock = EasyMock.partialMockBuilder(ReferenceUtils.class)
                .addMockedMethod("doAddAuthorsToConsortium").createMock();

        expect(referenceUtilsMock.doAddAuthorsToConsortium(submissionAccountId)).andReturn(true);
        replay(referenceUtilsMock);

        Reference reference = referenceUtilsMock.getSubmitterReferenceFromManifest(authors, address, date, submissionAccountId);
        assertNotNull(reference);
        assertEquals(address, ((Submission) reference.getPublication()).getSubmitterAddress());
        //authors added to consortium
        assertEquals("Vijay Senthil,Nathan S Vijay",reference.getPublication().getConsortium());
        assertEquals(0, reference.getPublication().getAuthors().size());

    }

}