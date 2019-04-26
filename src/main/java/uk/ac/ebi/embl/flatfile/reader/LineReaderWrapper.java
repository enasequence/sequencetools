/*******************************************************************************
 * Copyright 2013 EMBL-EBI, Hinxton outstation
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


/**
 * Wraps a BufferedReader, RandomAccessFile or ILineReader so that readLine()
 * may be called without knowing underlying implementation. Modified so that
 * seek() and getFilePointer() may also be called assuming that the other file
 * reader implements these.
 * 
 * @author simonk
 * 
 */
public class LineReaderWrapper implements ILineReader {

	private BufferedReader bufferedReader;

	private RandomAccessFile raf;

	private ILineReader otherLineReader;

	/**
	 * @param reader
	 */
	public LineReaderWrapper(BufferedReader reader) {
		this.bufferedReader = reader;
	}

	/**
	 * @param raf
	 */
	public LineReaderWrapper(RandomAccessFile raf) {
		this.raf = raf;
	}

	/**
	 * @param reader
	 */
	public LineReaderWrapper(ILineReader reader) {
		this.otherLineReader = reader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ebi.embl.flatfile.reader.ILineReader#getNextLine()
	 */
	@Override
	public String readLine() throws IOException {

		if (bufferedReader != null) {

			return bufferedReader.readLine();
		} else if (raf != null) {
			return raf.readLine();
		} else {
			return otherLineReader.readLine();
		}
	}

	/**
	 * Allows access to the file pointer in the RAF.
	 * 
	 * @return
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	public long getFilePointer() throws UnsupportedOperationException,
			IOException {

		if (raf != null) {
			return raf.getFilePointer();
		} else if (otherLineReader != null) {
			return ((BufferedFileLineReader) otherLineReader).getFilePointer();
		} else {
			throw new UnsupportedOperationException(
					"Cannot get a file pointer without randon access file");
		}
	}

	/**
	 * Allows access to the file pointer in the RAF.
	 * 
	 * @param position
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	public void seek(long position) throws UnsupportedOperationException,
			IOException {

		if (raf != null) {
			raf.seek(position);
		} else if (otherLineReader != null) {
			((BufferedFileLineReader) otherLineReader).seek(position);
		} else {
			throw new UnsupportedOperationException(
					"Cannot seek without randon access file");
		}
	}

	public long getDilePointer() throws  IOException{
		if(raf != null) {
			return raf.getFilePointer();
		}
		return 0L;
	}
}
