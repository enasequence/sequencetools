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
package uk.ac.ebi.embl.flatfile.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;

/**
 * Reader for flat file lines.
 */
public abstract class LineReader {

	/**
	 * an identifier of the thing being read - a file path or an accession
	 */
	private String fileId = null;

	private String currentLine = null;

	private String nextLine = null;

	private int currentLineNumber = 0;

	private int nextLineNumber = 0;
	
	// TODO take into consideration the Charset
	private long bytesOffset = 0;

	private String activeTag;
	
	private String firstLine;
	
	private boolean ignoreParseError=false;

	public boolean isIgnoreParseError()
	{
		return ignoreParseError;
	}
	
	/**
	 * Charset encoding used by the underlying BufferedReader
	 */
	private Charset charset;

	private CharsetEncoder ce;

	public LineReader setIgnoreParseError(boolean ignoreParseError)
	{
		this.ignoreParseError = ignoreParseError;
		return this;
	}


	private LineReaderCache cache = new LineReaderCache();

	private LineReaderWrapper reader;

	private static final Pattern REPLACE = Pattern.compile("[\t\n\r]");

	private static final int LINE_LENGTH = 80 + System.getProperty(
			"line.separator").length();

	public LineReader() {
	}

	public LineReader(BufferedReader reader) {
	   this(reader, Charset.defaultCharset());
	}
	
	public LineReader(BufferedReader reader, Charset charset) {
		this.reader = new LineReaderWrapper(reader);
		this.charset = charset;
		initCharsetEncoder();
	}

	public LineReader(BufferedReader reader, String fileId) {
		this.reader = new LineReaderWrapper(reader);
		initCharsetEncoder();
		this.fileId = fileId;
	}
	
	public LineReader(BufferedReader reader, Charset charset, String fileId) {
		this.reader = new LineReaderWrapper(reader);
		this.fileId = fileId;
		this.charset = charset;
		initCharsetEncoder();
	}

	public LineReader(RandomAccessFile raf, String fileId) {
		this.fileId = fileId;
		this.reader = new LineReaderWrapper(raf);
	}
	
	public LineReader(BufferedFileLineReader raf, String fileId) {
		this.fileId = fileId;
		this.reader = new LineReaderWrapper(raf);
	}

	public void setReader(BufferedReader reader) {
		setReader(reader, Charset.defaultCharset());
	}

	public void setReader(BufferedReader reader, Charset charset) {
		this.reader = new LineReaderWrapper(reader);
		this.charset = charset;
		initCharsetEncoder();
	}
	
	private void initCharsetEncoder() {
		if (charset != null) {
			ce = charset.newEncoder();
			ce.onMalformedInput(CodingErrorAction.REPLACE);
			ce.onUnmappableCharacter(CodingErrorAction.REPLACE);
		}
	}

	/**
	 * Returns true if there is an active tag.
	 */
	public boolean isActiveTag() {
		return this.activeTag != null;
	}

	/**
	 * Returns the active tag.
	 */
	public String getActiveTag() {
		if (activeTag == null) {
			return "";
		}
		return activeTag;
	}

	/**
	 * Return the tag width which is used to separate the tag from content.
	 */
	abstract protected int getTagWidth(String line);

	abstract protected boolean isTag(String line);

	protected boolean isSkipLine(String line) {
		return false;
	}

	/**
	 * Return true if the current line has a tag.
	 */
	public boolean isCurrentTag() {
		if (currentLine == null) {
			return false;
		}
		return isTag(currentLine);
	}

	/**
	 * Return true if the next line has a tag.
	 */
	public boolean isNextTag() {
		if (nextLine == null) {
			return false;
		}
		return isTag(nextLine);
	}

	protected String getTag(String line) {
		if (!isLine(line)) {
			return "";
		}
		if (!isTag(line)) {
			return "";
		}
		return FlatFileUtils.trim(
				line.substring(0, Math.min(getTagWidth(line), line.length())),
				0);
	}

	public String getFileId() {
		return fileId;
	}

	/**
	 * Return the current line tag.
	 */
	public String getCurrentTag() {
		return getTag(currentLine);
	}

	/**
	 * Return the next line tag.
	 */
	public String getNextTag() {
		return getTag(nextLine);
	}

	private boolean isLine(String line) {
		return line != null;
	}

	/**
	 * Return true if the current line exists.
	 */
	public boolean isCurrentLine() {
		return isLine(currentLine);
	}

	/**
	 * Return true if the next line exists.
	 */
	public boolean isNextLine() {
		return isLine(nextLine);
	}

	/**
	 * Returns true if the next and current lines can be joined together either
	 * because they have the same tag or because the next line does not have a
	 * tag.
	 */
	public boolean joinLine() {
		if (!isCurrentLine()) {
			return false;
		}
		if (!isNextLine()) {
			return false;
		}
		if (!isNextTag()) {
			return true; // no next tag -> continue block
		}
		if (!isCurrentTag()) {
			return false; // no current tag -> new block
		}
		// compare current and next tag
		return getCurrentTag().equals(getNextTag());
	}

	/**
	 * Return current line without tag.
	 */
	public String getCurrentLine() {
		if (!isCurrentLine()) {
			return null;
		}
		if (isTag(currentLine))
			return FlatFileUtils.trimRight(currentLine,
					getTagWidth(currentLine));
		else
			return currentLine.trim();
	}

	/**
	 * Return next line without tag.
	 */
	public String getNextLine() {
		if (!isNextLine()) {
			return null;
		}
		if (isTag(nextLine))
			return FlatFileUtils.trimRight(nextLine,
					getTagWidth(nextLine));
		else
			return nextLine.trim();
	}

	/**
	 * Return current line with tag masked with whitespace.
	 */
	public String getCurrentMaskedLine() {
		if (!isCurrentLine()) {
			return null;
		}
		StringBuilder str = new StringBuilder();
		int tagWidth = getTagWidth(currentLine);
		for (int i = 0; i < tagWidth; ++i) {
			str.append(" ");
		}
		if (currentLine.length() > tagWidth) {
			str.append(currentLine.substring(tagWidth));
		}
		return str.toString();
	}

	/**
	 * Return next line with tag masked with whitespace.
	 */
	public String getNextMaskedLine() {
		if (!isNextLine()) {
			return null;
		}
		StringBuilder str = new StringBuilder();
		int tagWidth = getTagWidth(nextLine);
		for (int i = 0; i < tagWidth; ++i) {
			str.append(" ");
		}
		if (nextLine.length() > tagWidth) {
			str.append(nextLine.substring(tagWidth));
		}
		return str.toString();
	}

	/**
	 * Trim and return the current line without tag.
	 */
	public String getCurrentTrimmedLine() {
		if (!isCurrentLine()) {
			return null;
		}
		return FlatFileUtils.trim(currentLine, getTagWidth(currentLine));
	}

	/**
	 * Shrink and return the current line without tag.
	 */
	public String getCurrentShrinkedLine() {
		if (!isCurrentLine()) {
			return null;
		}
		String string = FlatFileUtils.trim(currentLine,
				getTagWidth(currentLine));
		if (string.equals("")) {
			return null;
		}
		return FlatFileUtils.shrink(string);
	}

	/**
	 * Shrink and return the current line without any modifications.
	 */
	// TODO all should return strings and all should be unit tested
	public String getCurrentRawLine() {
		if (!isCurrentLine()) {
			return null;
		}
		return currentLine;
	}
	/*
	 * get the first line of the file to check the file format
	 */
	public String getFirstLine()
	{
		return firstLine;
	}
	
	/*
	 * set the first line of the file to check the file format
	 */
	public void setFirstLine(String firstLine)
	{
		this.firstLine = firstLine;
	}

	/**
	 * Return current line number.
	 */
	public int getCurrentLineNumber() {
		return currentLineNumber;
	}

	public boolean readLine() throws IOException {

		if (currentLineNumber == 0) {
			while (true) {
				currentLine = reader.readLine();
				++currentLineNumber;
				++nextLineNumber;
				if( currentLine != null ) 
				{
					if (currentLine.length() == 0 ) {
						countStringBytes("\n");
						continue;
					}
					if (isSkipLine(currentLine)) {
						countLineBytes(currentLine);
						continue;
					}
					
					currentLine = replaceWithSpace( currentLine );
					setFirstLine(currentLine);
				}
				break;
			}
		} else {
			currentLine = nextLine;
			currentLineNumber = nextLineNumber;
		}
		if (currentLine == null) {
			return false;
		} else {
			countLineBytes(currentLine);
		}

		while (true) {
			nextLine = reader.readLine();
			++nextLineNumber;
			if (nextLine != null) {
				if (nextLine.length() == 0) {
					countStringBytes("\n");
					continue;
				}
				if (isSkipLine(nextLine)) {
					countLineBytes(nextLine);
					continue;
				}

				nextLine = replaceWithSpace( nextLine );
			}
			break;
		}

		// Save the last valid tag. This is required
		// to assign tags to rows without tags which
		// in turn makes it simpler to implement block
		// readers.
		if (isTag(currentLine)) {
			activeTag = getTag(currentLine);
			cache.countTag(activeTag);
		}

		return true;
	}

	private void countLineBytes(String line) {
		// count line terminator
		countStringBytes(line + "\n");
	}

	private void countStringBytes(String line) {
		try {
			bytesOffset += ce.encode(CharBuffer.wrap(line)).array().length;
		} catch (CharacterCodingException e) {
			// should not happen, we asked to replace chars in these situations
			e.printStackTrace();
		}
	}
	
	String
	replaceWithSpace( CharSequence line )
	{
	    StringBuilder result = new StringBuilder( line );
	    for( int index = 0; index < result.length(); ++index )
	        switch( result.charAt( index ) )
	        {
	            default:
	                continue;
	            case '\t':
	            case '\n':
	            case '\r':
	                result.setCharAt( index, ' ' );
	                bytesOffset++;
	            
	        }
	    return result.toString();
	}


	public boolean 
	skipToTerminatingFlag() throws IOException 
	{
        getCache().resetReferenceCache();
        getCache().resetOrganismCache();

	    while (true) 
		{
			nextLine = reader.readLine();
			currentLineNumber++;
			nextLineNumber++;

			if (nextLine != null) {
				if (nextLine.startsWith(EmblTag.TERMINATOR_TAG)) {

					currentLine = nextLine;
					nextLine = reader.readLine();
					return true;
				}
			} else 
			    return false;
			}
		}

	public LineReaderCache getCache() {
		return cache;
	}
	
	/**
	 * Return read bytes offset from the beginning of the stream
	 * 
	 * @return
	 */
	public long getOffset() {
	   return bytesOffset;
	}
}
