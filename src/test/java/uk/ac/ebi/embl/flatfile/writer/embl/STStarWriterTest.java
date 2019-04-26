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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.writer.embl.STStarWriter;

public class STStarWriterTest extends EmblWriterTest {

	public void testWrite_Public() throws IOException {
        entry.setStatus(Entry.Status.PUBLIC);
        StringWriter writer = new StringWriter();
        assertTrue(new STStarWriter(entry).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"ST * public\n",
        		writer.toString());
    }

	public void testWrite_DraftWithoutHoldDate() throws IOException {
        entry.setStatus(Entry.Status.DRAFT);
        StringWriter writer = new StringWriter();
        assertTrue(new STStarWriter(entry).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"ST * draft\n",
        		writer.toString());
    }

    public void testWrite_DraftWithHoldDate1() throws IOException {
        entry.setStatus(Entry.Status.DRAFT);
        entry.setHoldDate(FlatFileUtils.getDay("10-SEP-1998"));
        StringWriter writer = new StringWriter();
        assertTrue(new STStarWriter(entry).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"ST * draft 10-SEP-1998\n",
        		writer.toString());
    }

    public void testWrite_DraftWithHoldDate2() throws IOException {
        entry.setStatus(Entry.Status.DRAFT);
        entry.setHoldDate(FlatFileUtils.getDay("1-SEP-1998"));
        StringWriter writer = new StringWriter();
        assertTrue(new STStarWriter(entry).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"ST * draft 01-SEP-1998\n",
        		writer.toString());
    }

	public void testWrite_Cancelled() throws IOException {
        entry.setStatus(Entry.Status.CANCELLED);
        StringWriter writer = new StringWriter();
        assertTrue(new STStarWriter(entry).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"ST * cancelled\n",
        		writer.toString());
    }	
	
	public void testWrite_Suppressed() throws IOException {
        entry.setStatus(Entry.Status.SUPPRESSED);
        StringWriter writer = new StringWriter();
        assertTrue(new STStarWriter(entry).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"ST * suppressed\n",
        		writer.toString());
    }	
	
	public void testWrite_Killed() throws IOException {
        entry.setStatus(Entry.Status.KILLED);
        StringWriter writer = new StringWriter();
        assertTrue(new STStarWriter(entry).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"ST * killed\n",
        		writer.toString());
    }	
	
	public void testWrite_PrivateWithoutHoldDate() throws IOException {
        entry.setStatus(Entry.Status.PRIVATE);
        StringWriter writer = new StringWriter();
        assertTrue(new STStarWriter(entry).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"ST * private\n",
        		writer.toString());
    }	
	
	public void testWrite_PrivateWithHoldDate() throws IOException {
        entry.setStatus(Entry.Status.PRIVATE);
        entry.setHoldDate(FlatFileUtils.getDay("10-SEP-1998"));
        StringWriter writer = new StringWriter();
        assertTrue(new STStarWriter(entry).write(writer));
        // System.out.print(writer.toString());
        assertEquals(
        		"ST * private 10-SEP-1998\n",
        		writer.toString());
    }		

	public void testWrite_PrivateWithHoldDate2() throws IOException {
        entry.setStatus(Entry.Status.PRIVATE);
        entry.setHoldDate(FlatFileUtils.getDay("1-SEP-1998"));
        StringWriter writer = new StringWriter();
        assertTrue(new STStarWriter(entry).write(writer));
        // System.out.print(writer.toString());
        assertEquals(
        		"ST * private 01-SEP-1998\n",
        		writer.toString());
    }
}
