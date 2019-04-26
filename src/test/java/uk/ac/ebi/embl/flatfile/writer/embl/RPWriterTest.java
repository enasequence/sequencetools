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

import java.io.IOException;
import java.io.StringWriter;

import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.writer.embl.RPWriter;

public class RPWriterTest extends EmblWriterTest {

    public void testWrite_OneLocation() throws IOException {
    	ReferenceFactory referenceFactory = new ReferenceFactory();
    	Reference reference = referenceFactory.createReference();
    	LocationFactory locationFactory = new LocationFactory();
    	reference.getLocations().addLocation(
    		locationFactory.createLocalRange(1l, 262417l));
    	StringWriter writer = new StringWriter();
    	assertTrue(new RPWriter(entry, reference, wrapType).write(writer));
        assertEquals(
        		"RP   1-262417\n",
                writer.toString());
    }

    public void testWrite_TwoLocations() throws IOException {
    	ReferenceFactory referenceFactory = new ReferenceFactory();
    	Reference reference = referenceFactory.createReference();
    	LocationFactory locationFactory = new LocationFactory();
    	reference.getLocations().addLocation(
    		locationFactory.createLocalRange(1l, 262417l));    
    	reference.getLocations().addLocation(
    		locationFactory.createLocalRange(1l, 262417l));    
    	StringWriter writer = new StringWriter();
    	assertTrue(new RPWriter(entry, reference, wrapType).write(writer));
        assertEquals(
        		"RP   1-262417, 1-262417\n",
                writer.toString());
    }
    
    public void testWrite_ManyLocations() throws IOException {
    	ReferenceFactory referenceFactory = new ReferenceFactory();
    	Reference reference = referenceFactory.createReference();
    	LocationFactory locationFactory = new LocationFactory();
    	reference.getLocations().addLocation(
    		locationFactory.createLocalRange(1l, 262417l));    
    	reference.getLocations().addLocation(
    		locationFactory.createLocalRange(1l, 262417l));    
    	reference.getLocations().addLocation(
        		locationFactory.createLocalRange(1l, 262417l));    
    	reference.getLocations().addLocation(
        		locationFactory.createLocalRange(1l, 262417l));    
    	reference.getLocations().addLocation(
        		locationFactory.createLocalRange(1l, 262417l));    
    	reference.getLocations().addLocation(
        		locationFactory.createLocalRange(1l, 262417l));    
    	reference.getLocations().addLocation(
        		locationFactory.createLocalRange(1l, 262417l));    
    	reference.getLocations().addLocation(
        		locationFactory.createLocalRange(1l, 262417l));    
    	reference.getLocations().addLocation(
        		locationFactory.createLocalRange(1l, 262417l));    
    	reference.getLocations().addLocation(
        		locationFactory.createLocalRange(1l, 262417l));    
    	StringWriter writer = new StringWriter();
    	assertTrue(new RPWriter(entry, reference, wrapType).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"RP   1-262417, 1-262417, 1-262417, 1-262417, 1-262417, 1-262417, 1-262417,\n" +
        		"RP   1-262417, 1-262417, 1-262417\n",        		
                writer.toString());
    }    
}
