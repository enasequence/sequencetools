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

import uk.ac.ebi.embl.api.entry.reference.Article;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

public class XmlArticleWriterTest extends XmlWriterTest {

    public void testWrite() throws IOException {
    	ReferenceFactory referenceFactory = new ReferenceFactory();
    	Article article = referenceFactory.createArticle(
			"Cloning and characterization of a cDNA encoding a novel subtype of rat thyrotropin-releasing hormone receptor",
			"J. Biol. Chem.");
    	article.setVolume("273");
    	article.setIssue("48");
    	article.setFirstPage("32281");
    	article.setLastPage("32287");
    	article.setYear(FlatFileUtils.getDay("10-SEP-1998"));
        StringWriter writer = new StringWriter();
        assertTrue(new XmlReferenceLocationWriter(entry, article).write(new SimpleXmlWriter(writer)));
        //System.out.print(writer.toString());
        assertEquals(
            "<referenceLocation>\n" +        		
    		"J. Biol. Chem. 273(48):32281-32287(1998).\n" +
            "</referenceLocation>\n",
    		writer.toString());
    }
}
