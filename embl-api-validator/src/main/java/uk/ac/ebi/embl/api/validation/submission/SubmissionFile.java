package uk.ac.ebi.embl.api.validation.submission;

import java.io.File;

public class SubmissionFile {

	public enum FileType {
		FASTA,
		FLATFILE,
		AGP,
		TSV,
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

	public FileType getFileType() {
		return fileType;
	};

	public File getFile() {
		return file;
	};

	public File getFixedFile() {
		return fixedFile;
	};

	public boolean createFixedFile() {
		return fixedFile != null;
	}
}
