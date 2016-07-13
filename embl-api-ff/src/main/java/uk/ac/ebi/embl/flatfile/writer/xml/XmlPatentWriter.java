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
package uk.ac.ebi.embl.flatfile.writer.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import uk.ac.ebi.embl.api.entry.reference.Patent;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

public class XmlPatentWriter {
	
    private Patent patent;
    
    private static final DateFormat DAY_FORMAT = 
    		new SimpleDateFormat("dd-MMM-yyyy");

	public XmlPatentWriter(Patent patent) {
		this.patent = patent;
	}

    public boolean write(SimpleXmlWriter writer) throws IOException {
    	StringWriter stringWriter = new StringWriter();
		stringWriter.write("Patent number ");
		if (!FlatFileUtils.isBlankString(patent.getPatentOffice())) {
			stringWriter.write(patent.getPatentOffice());
		}
		if (!FlatFileUtils.isBlankString(patent.getPatentNumber())) {
			stringWriter.write(patent.getPatentNumber());
		}
		stringWriter.write("-");
		if (!FlatFileUtils.isBlankString(patent.getPatentType())) {
			stringWriter.write(patent.getPatentType());
		}
		stringWriter.write("/");
		if (patent.getSequenceNumber()!= null) {
			stringWriter.write(patent.getSequenceNumber().toString());
		}
		stringWriter.write(", ");
		if (patent.getDay() != null) {
			stringWriter.write(DAY_FORMAT.format(patent.getDay()).toUpperCase());
		}
		stringWriter.write(".");    	
    	writer.writeElementText(stringWriter.toString());
    	return true;    
    }	
}
