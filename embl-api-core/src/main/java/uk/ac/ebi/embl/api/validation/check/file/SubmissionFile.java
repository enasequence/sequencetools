package uk.ac.ebi.embl.api.validation.check.file;

import java.io.File;

public class SubmissionFile {

	public enum FileType {
		FASTA,
		FLATFILE,
		AGP,
		CHROMOSOME_LIST,
		UNLOCALISED_LIST
	};

	private final FileType fileType;
	private final File file;
	private final File fixedFile;

	public SubmissionFile (FileType fileType, File file) {
		this.fileType = fileType;
		this.file = file;
		this.fixedFile = null;
	}

	public SubmissionFile (FileType fileType, File file, File fixedFile) {
		this.fileType = fileType;
		this.file = file;
		this.fixedFile = fixedFile;
	}

	FileType getFileType() {
		return fileType;
	};

	File getFile() {
		return file;
	};

	File getFixedFile() {
		return fixedFile;
	};

	boolean createFixedFile() {
		return fixedFile != null;
	}
}
