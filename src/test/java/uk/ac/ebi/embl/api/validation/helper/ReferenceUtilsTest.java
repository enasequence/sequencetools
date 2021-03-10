package uk.ac.ebi.embl.api.validation.helper;


import org.easymock.EasyMock;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.dao.entity.ReferenceEntity;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

public class ReferenceUtilsTest  {

    ReferenceUtils refutils = new ReferenceUtils();

    @Test
    public void testConstructReferenceWithoutConsortiumAndBroker() throws UnsupportedEncodingException {

        List<ReferenceEntity> refEntityList = new ArrayList<>();
        ReferenceEntity refEntity = new ReferenceEntity();
        refEntity.setBrokerName(null);
        refEntity.setSubmissionAccountId("Webin-256");
        refEntity.setConsortium(null);
        refEntity.setSurname("Vijay");
        refEntity.setMiddleInitials(null);
        refEntity.setFirstName("Senthil");
        refEntity.setCenterName("EBI.(EBL-EBI)");
        refEntity.setFirstCreated("22-SEP-2020");
        refEntity.setLaboratoryName("EBI -EMBL");
        refEntity.setAddress("Hinxton, Cambridgeshire");
        refEntity.setCountry("United Kingdom");
        refEntityList.add(refEntity);

        Reference reference = refutils.constructReference(refEntityList);
        assertNotNull(reference);
        assertEquals("EBI -EMBL", ((Submission) reference.getPublication()).getSubmitterAddress());//just labarotary name for non-brokers
        assertTrue(FlatFileWriter.isBlankString(reference.getPublication().getConsortium()));
        assertEquals(1, reference.getPublication().getAuthors().size());
        Person person = reference.getPublication().getAuthors().get(0);
        assertEquals("Vijay", person.getSurname().trim());
        assertEquals("S.", person.getFirstName());
    }

    @Test
    public void testConstructReferenceWithConsortiumAndBroker() throws UnsupportedEncodingException {

        List<ReferenceEntity> refEntityList = new ArrayList<>();
        ReferenceEntity refEntity = new ReferenceEntity();
        refEntity.setBrokerName("AB bioinformatics");
        refEntity.setSubmissionAccountId("Webin-256");
        refEntity.setConsortium("AB bioinformatics inc");
        refEntity.setSurname("Vijay");
        refEntity.setMiddleInitials(null);
        refEntity.setFirstName("Senthil");
        refEntity.setCenterName("R&D unit EBI.(EBL-EBI)");
        refEntity.setFirstCreated("22-SEP-2020");
        refEntity.setLaboratoryName("EBI Lab");
        refEntity.setAddress("Hinxton, Cambridgeshire");
        refEntity.setCountry("United Kingdom");
        refEntityList.add(refEntity);

        Reference reference = refutils.constructReference(refEntityList);
        assertNotNull(reference);
        //brokername,labaratory,address,country
        assertEquals("AB bioinformatics, EBI Lab, Hinxton, Cambridgeshire, United Kingdom", ((Submission) reference.getPublication()).getSubmitterAddress());
        assertEquals(0, reference.getPublication().getAuthors().size());
        assertEquals("AB bioinformatics inc",reference.getPublication().getConsortium());
    }

    @Test
    public void getReference() throws ValidationEngineException {
        String authors = "Vijay Senthil,Nathan S Vijay";
        String address ="Hinxton,Cambridge,UK";
        Date date = new Date();
        String submissionAccountId = "Webin-256";
        Reference reference = refutils.getReference(authors, address, date, submissionAccountId);
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
    public void getReferenceAddAuthorToConsortium() throws ValidationEngineException, NoSuchMethodException {
        String authors = "Vijay Senthil,Nathan S Vijay";
        String address ="Hinxton,Cambridge,UK";
        Date date = new Date();
        String submissionAccountId = "Webin-256";

        ReferenceUtils referenceUtilsMock = EasyMock.partialMockBuilder(ReferenceUtils.class)
                .addMockedMethod("doAddAuthorsToConsortium").createMock();

        expect(referenceUtilsMock.doAddAuthorsToConsortium(submissionAccountId)).andReturn(true);
        replay(referenceUtilsMock);

        Reference reference = referenceUtilsMock.getReference(authors, address, date, submissionAccountId);
        assertNotNull(reference);
        assertEquals(address, ((Submission) reference.getPublication()).getSubmitterAddress());
        //authors added to consortium
        assertEquals("Vijay Senthil,Nathan S Vijay",reference.getPublication().getConsortium());
        assertEquals(0, reference.getPublication().getAuthors().size());

    }

}