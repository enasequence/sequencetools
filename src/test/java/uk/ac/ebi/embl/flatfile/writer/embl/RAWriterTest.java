/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblLineReader;
import uk.ac.ebi.embl.flatfile.reader.embl.RAReader;
import uk.ac.ebi.embl.flatfile.writer.embl.RAWriter;

public class RAWriterTest extends EmblWriterTest {

    public void testWrite_OneAuthors() throws IOException {
    	entry.removeReferences();    	
    	EmblLineReader lineReader = new EmblLineReader(new BufferedReader(new StringReader(
				"RA   Antonellis A.")));
		lineReader.readLine();
		Publication publication = lineReader.getCache().getPublication();
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new RAReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
    	entry.addReference(reference);
        StringWriter writer = new StringWriter();
        assertTrue(new RAWriter(entry, publication, wrapType).write(writer));
        assertEquals(
        		"RA   Antonellis A.;\n",
                writer.toString());
    }
		
    public void testWrite_ManyAuthors() throws IOException {
    	entry.removeReferences();
    	EmblLineReader lineReader = new EmblLineReader(new BufferedReader(new StringReader(
				"RA   Antonellis A., Ayele K., Benjamin B., Blakesley R.W., Boakye A., Bouffard G.G., Brinkley C., Brooks S., Chu G., Coleman H., Engle J., Gestole M., Greene A., Guan X., Gupta J., Haghighi P., Han J., Hansen N., Ho S.-L., Hu P., Hunter G., Hurle B., Idol J.R., Kwong P., Laric P., Larson S., Lee-Lin S.-Q., Legaspi R., Madden M., Maduro Q.L., Maduro V.B., Margulies E.H., Masiello C., Maskeri B., McDowell J., Mojidi H.A., Mullikin J.C., Oestreicher J.S., Park M., Portnoy M.E., Prasad A., Puri O., Reddix-Dugue N., Schandler K., Schueler M.G., Sison C., Stantripop S., Stephen E., Taye A., Thomas J.W., Thomas P.J., Tsipouri V., Ung L., Vogt J.L., Wetherby K.D., Young A., Green E.D.")));
		lineReader.readLine();
		Publication publication = lineReader.getCache().getPublication();
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new RAReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
    	entry.addReference(reference);
        StringWriter writer = new StringWriter();
        assertTrue(new RAWriter(entry, publication, wrapType).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"RA   Antonellis A., Ayele K., Benjamin B., Blakesley R.W., Boakye A.,\n" +
                "RA   Bouffard G.G., Brinkley C., Brooks S., Chu G., Coleman H., Engle J.,\n" +
                "RA   Gestole M., Greene A., Guan X., Gupta J., Haghighi P., Han J., Hansen N.,\n" +
                "RA   Ho S.-L., Hu P., Hunter G., Hurle B., Idol J.R., Kwong P., Laric P.,\n" +
                "RA   Larson S., Lee-Lin S.-Q., Legaspi R., Madden M., Maduro Q.L., Maduro V.B.,\n" +
                "RA   Margulies E.H., Masiello C., Maskeri B., McDowell J., Mojidi H.A.,\n" +
                "RA   Mullikin J.C., Oestreicher J.S., Park M., Portnoy M.E., Prasad A., Puri O.,\n" +
                "RA   Reddix-Dugue N., Schandler K., Schueler M.G., Sison C., Stantripop S.,\n" +
                "RA   Stephen E., Taye A., Thomas J.W., Thomas P.J., Tsipouri V., Ung L.,\n" +
                "RA   Vogt J.L., Wetherby K.D., Young A., Green E.D.;\n",
                writer.toString());
    }

    public void testWrite_NoAuthors() throws IOException {
    	entry.removeReferences();
    	ReferenceFactory referenceFactory = new ReferenceFactory();
		Publication publication = (new ReferenceFactory()).createPublication();
    	Reference reference = referenceFactory.createReference(publication, 1);
    	entry.addReference(reference);
        StringWriter writer = new StringWriter();
        assertTrue(new RAWriter(entry, publication, wrapType).write(writer));
        assertEquals( 
        		"RA   ;\n",
        		writer.toString());                        
    }
}
