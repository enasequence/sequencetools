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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * @author simonk
 * 
 */
public class BufferedFileLineReader implements ILineReader {

	// Set the buffer size to 32 M
	private final static int BUF_SIZE = /*32 */ 1024 * 1024;

	// For efficiency reasons, we define a byte buffer instead of char buffer.
	// The variables buf_end, buf_pos, and real_pos are used to record the
	// effective positions on the buffer:
	private byte buffer[];
	private int buf_end = 0;
	private int buf_pos = 0;
	private long real_pos = 0;

	private RandomAccessFile raf;

	private String name;

	/**
	 * Constructor creates a new RandomAccessFile and buffer.
	 * 
	 * @param name
	 * @param mode
	 * @throws FileNotFoundException
	 */
	public BufferedFileLineReader(final String name, final String mode)
			throws FileNotFoundException {

		this.raf = new RandomAccessFile(name, mode);
		this.name = name;
		this.buffer = new byte[BUF_SIZE];
	}

	/**
	 * Constructor creates a new RandomAccessFile and buffer.
	 * 
	 * @param file
	 * @param mode
	 * @throws FileNotFoundException
	 */
	public BufferedFileLineReader(final File file, final String mode)
			throws FileNotFoundException {

		this.raf = new RandomAccessFile(file, mode);
		this.name = file.getName();
		this.buffer = new byte[BUF_SIZE];
	}
	
	/**
	 * Constructor taking a RandomAccessFile.
	 * 
	 * @param raf
	 * @throws IOException
	 */
	public BufferedFileLineReader(final RandomAccessFile raf) throws IOException {
		
		this.raf = raf;
		this.name = raf.getFD().toString();
		this.buffer = new byte[BUF_SIZE];
	}

	/**
	 * Returns the relative position in the file to the position in the buffer.
	 * 
	 * @return
	 * @throws IOException
	 */
	public long getFilePointer() throws IOException {

		return (real_pos - buf_end + buf_pos);
	}

	/**
	 * If the sought position is within the buffer - simply sets the current
	 * buffer position so the next read will be from the buffer. Otherwise seeks
	 * in the RAF and reset the buffer end and current positions.
	 * 
	 * 
	 * @param pos
	 * @throws IOException
	 */
	public void seek(long pos) throws IOException {

		int n = (int) (real_pos - pos);

		if (n >= 0 && n <= buf_end) {

			buf_pos = buf_end - n;

		} else {

			raf.seek(pos);

			buf_end = 0;
			buf_pos = 0;
			real_pos = raf.getFilePointer();
		}
	}

	/**
	 * Returns the file name.
	 * 
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Closes the file.
	 * 
	 * @throws IOException
	 */
	public final void close() throws IOException {
		raf.close();
	}

	/**
	 * This method first decides if the buffer still contains unread contents.
	 * If it doesn't, the buffer needs to be filled up. If the new line
	 * delimiter can be found in the buffer, then a new line is read from the
	 * buffer and converted into String. Otherwise, it will simply call the read
	 * method to read byte by byte. Although the code of the latter portion is
	 * similar to the original readLine, performance is better here because the
	 * read method is buffered in the new class.
	 * 
	 * @see uk.ac.ebi.embl.flatfile.reader.ILineReader#readline()
	 */
	@Override
	public final String readLine() throws IOException {

		String str = null;

		if (buf_end - buf_pos <= 0) {
			if (fillBuffer() < 0) {
				// return null if we are at the end and there is nothing to read
				return null;
			}
		}

		int lineend = -1;

		for (int i = buf_pos; i < buf_end; i++) {
			if (buffer[i] == '\n') {
				lineend = i;
				break;
			}
		}

		if (lineend < 0) {
			StringBuffer input = new StringBuffer(256);
			int c;
			while (((c = read()) != -1) && (c != '\n')) {
				input.append((char) c);
			}
			if ((c == -1) && (input.length() == 0)) {
				return null;
			}
			return input.toString();
		}

		if (lineend > 0 && buffer[lineend - 1] == '\r') {
			str = new String(buffer, 0, buf_pos, lineend - buf_pos - 1);
		} else {
			str = new String(buffer, 0, buf_pos, lineend - buf_pos);
		}

		buf_pos = lineend + 1;

		return str;
	}

	/**
	 * If the buffer position is past the end, calls fillBuffer(). In all cases
	 * increments the buffer position and returns the contents at that position.
	 * This presents the most significant performance gain because the read
	 * method is heavily used in the readLine method.
	 * 
	 * @throws IOException
	 */
	private final int read() throws IOException {

		if (buf_pos >= buf_end) {
			if (fillBuffer() < 0) {
				return -1;
			}
		}

		if (buf_end == 0) {
			return -1;
		} else {
			return buffer[buf_pos++];
		}
	}

	/**
	 * Used to fill the buffer by calling the RAF read() method using the
	 * standard buffer size. If the read was successful, the real position is
	 * incremented by the number of bytes read and the buffer end is set to the
	 * number of bytes read.
	 * 
	 * @return the number of bytes read
	 * @throws IOException
	 */
	private int fillBuffer() throws IOException {

		int bytesRead = raf.read(buffer, 0, BUF_SIZE);

		if (bytesRead >= 0) {
			real_pos += bytesRead;
			buf_end = bytesRead;
			buf_pos = 0;
		}

		return bytesRead;
	}
}
