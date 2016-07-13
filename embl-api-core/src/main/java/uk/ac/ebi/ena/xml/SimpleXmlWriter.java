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
package uk.ac.ebi.ena.xml;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/** A simple XML writer.
 */
public class SimpleXmlWriter {
	
	private Writer writer;
	private boolean indent;
	private boolean noTextElement;
	public SimpleXmlWriter(Writer writer) {
		this.writer = writer;
		indent = true;
		noTextElement = false;
	}

	private static final DateFormat DATE_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd");
	
	
	
	public boolean isNoTextElement() {
		return noTextElement;
	}

	public void setNoTextElement(boolean noTextElement) {
		this.noTextElement = noTextElement;
	}

	public boolean isIndent() {
		return indent;
	}

	public void setIndent(boolean indent) {
		this.indent = indent;
	}

	private void indent() throws IOException {
		for (int i = 0 ; i < elementNames.size() - 1; ++i) {
			writer.write("\t");
		}
	}

	private Vector<String> elementNames = new Vector<String>();	

	private void addElementName(String elementName) {
		elementNames.add(elementName);
	}
	
	private void removeElementName(String elementName) throws IOException {
		assert(elementNames.size() > 0);
		assert(elementNames.get(elementNames.size() - 1).equals(elementName));
		elementNames.remove(elementNames.size() - 1);
	}
	
	private boolean escapeXml = true;

	public boolean isEscapeXml() {
		return escapeXml;
	}

	public void setEscapeXml(boolean escapeXml) {
		this.escapeXml = escapeXml;
	}

	private String escapeXml(String xml) throws IOException {
		if (xml == null) {
			return null;
		}
		if (!escapeXml) {
			return xml;
		}
		String escapedXml = xml;
		escapedXml = escapedXml.replace("&","&amp;");
		escapedXml = escapedXml.replace("<","&lt;");
		escapedXml = escapedXml.replace(">","&gt;");
		escapedXml = escapedXml.replace("\"","&quot;");
		escapedXml = escapedXml.replace("'","&apos;");
		return escapedXml;
	}
		
	public void writeDeclaration() throws IOException {
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");	
	}

	public void writeNamespaceAttribute() throws IOException {
		writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");		
	}

	public void writeNamespaceAttributeForDarwin() throws IOException {
		writeAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");		
		writeAttribute("xmlns:dcterms", "http://purl.org/dc/terms/");
		writeAttribute("xmlns:dwr", "http://rs.tdwg.org/dwc/xsd/tdwg_dwc_simple.xsd");
		writeAttribute("xmlns:dwc", "http://rs.tdwg.org/dwc/terms/");
		writeAttribute("version", "1.1");
		writeAttribute("targetNamespace", "http://rs.tdwg.org/dwc/xsd/tdwg_dwc_simple.xsd");
	}
	
	public void writeAttribute(String attribute, String value) throws IOException {
		if (attribute == null || attribute.length() == 0) {
			return;
		}
		if (value == null || value.length() == 0) {
			return;
		}
		writer.write(" ");
		writer.write(attribute);
		writer.write("=\"");
		writer.write(escapeXml(value));
		writer.write("\"");
	}

	public void writeAttribute(String attribute, Boolean value) throws IOException {
		if (attribute == null || attribute.length() == 0) {
			return;
		}
		if (value == null || !value) { // Do not show the attribute when value is false.
			return;
		}
		writer.write(" ");
		writer.write(attribute);
		writer.write("=\"");
		if (value) {
			writer.write("true");
		}
		else {
			writer.write("false");
		}
		writer.write("\"");
	}

	public void writeAttribute(String attribute, Integer value) throws IOException {
		if (attribute == null || attribute.length() == 0) {
			return;
		}		
		if (value == null) {
			return;
		}
		writer.write(" ");
		writer.write(attribute);
		writer.write("=\"");
		writer.write(value.toString());
		writer.write("\"");
	}

	public void writeAttribute(String attribute, Date value) throws IOException {
		if (attribute == null || attribute.length() == 0) {
			return;
		}
		if (value == null) {
			return;
		}
		writer.write(" ");
		writer.write(attribute);
		writer.write("=\"");
		writer.write(DATE_FORMAT.format(value));
		writer.write("\"");
	}

	public void writeAttribute(String attribute, Long value) throws IOException {
		if (attribute == null || attribute.length() == 0) {
			return;
		}
		if (value == null) {
			return;
		}
		writer.write(" ");
		writer.write(attribute);
		writer.write("=\"");
		writer.write(value.toString());
		writer.write("\"");
	}
	
	/**
	 * Writes '<elementName'.
	 */      
	public void beginElement(String elementName) throws IOException {		
		addElementName(elementName);
		indent();
		writer.write("<");
		writer.write(elementName);
	}

	/**
	 * Writes '>\n'.
	 */
	public void openElement(String elementName) throws IOException {
		assert(elementNames.size() > 0);
		assert(elementNames.get(elementNames.size() - 1).equals(elementName));		
		writer.write(">");
		if(indent || noTextElement) {
			writer.write("\n");
		}
	}

	/**
	 * Writes '/>\n'.
	 */
	public void openCloseElement(String elementName) throws IOException {
		assert(elementNames.size() > 0);
		assert(elementNames.get(elementNames.size() - 1).equals(elementName));
		writer.write("/>\n");		
		removeElementName(elementName);
	}

	/**
	 * Writes </elementName>\n'.
	 */   
	public void closeElement(String elementName) throws IOException {
		assert(elementNames.size() > 0);
		assert(elementNames.get(elementNames.size() - 1).equals(elementName));
		if(indent || noTextElement)
		indent();		
		writer.write("</");		
		writer.write(elementName);
		writer.write(">\n");		
		removeElementName(elementName);
	}
	
	public void writeElementText(String text) throws IOException {
		writer.write(escapeXml(text));
	}
	public void writeElementTextForDarwin(String text) throws IOException {
		writer.write(text);
	}
	
	/**
	 * Writes '<elementName><text></elementName>\n'.
	 */   
	public void writeSingleLineTextElement(String elementName, String text) throws IOException {
		if (elementName == null || elementName.length() == 0) {
			return;
		}
		if (text == null || text.length() == 0) {
			return;
		}
		addElementName(elementName);		
		indent();		
		writer.write("<");
		writer.write(elementName);
		writer.write(">");
		writer.write(escapeXml(text));
		writer.write("</");
		writer.write(elementName);
		writer.write(">\n");						
		removeElementName(elementName);
	}

	/**
	 * Writes '<elementName><text></elementName>\n'.
	 */   
	public void writeSingleLineDateElement(String elementName, Date value) throws IOException {
		if (elementName == null || elementName.length() == 0) {
			return;
		}
		if (value == null) {
			return;
		}
		addElementName(elementName);		
		indent();		
		writer.write("<");
		writer.write(elementName);
		writer.write(">");
		writer.write(DATE_FORMAT.format(value));
		writer.write("</");
		writer.write(elementName);
		writer.write(">\n");						
		removeElementName(elementName);
	}
	
	/**
	 * Writes '<elementName>\n<text>\n</elementName>\n'.
	 */   
	public void writeMultiLineTextElement(String elementName, String text) throws IOException {
		if (elementName == null || elementName.length() == 0) {
			return;
		}
		if (text == null || text.length() == 0) {
			return;
		}		
		addElementName(elementName);		
		indent();		
		writer.write("<");
		writer.write(elementName);
		writer.write(">\n");
		writer.write(escapeXml(text));
		writer.write("\n");		
		indent();		
		writer.write("</");
		writer.write(elementName);
		writer.write(">\n");		
		removeElementName(elementName);
	}		   
}
