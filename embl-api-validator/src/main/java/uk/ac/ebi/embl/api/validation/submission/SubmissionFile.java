package uk.ac.ebi.embl.api.validation.submission;

import java.io.File;

public class SubmissionFile {

	public enum FileType {
		ASSEMBLY_INFO,
		FASTA,
		FLATFILE,
		AGP,
		TSV,
		CHROMOSOME_LIST,
		UNLOCALISED_LIST,
		MASTER,
		ANNOTATION_ONLY_FLATFILE
	};

	private final FileType fileType;
	private final File file;
	private final File fixedFile;
	private final File reportFile;

	public SubmissionFile (FileType fileType, File file) {
		this.fileType = fileType;
		this.file = file;
		this.fixedFile = null;
		this.reportFile =null;
	}

	public SubmissionFile (FileType fileType, File file, File fixedFile) {
		this.fileType = fileType;
		this.file = file;
		this.fixedFile = fixedFile;
		this.reportFile =null;
	}
	
	public SubmissionFile (FileType fileType, File file, File fixedFile,File reportFile) {
		this.fileType = fileType;
		this.file = file;
		this.fixedFile = fixedFile;
		this.reportFile= reportFile;
	}


	public FileType getFileType() {
		return fileType;
	}

	public File getFile() {
		return file;
	}

	public File getFixedFile() {
		return fixedFile;
	}

	public boolean createFixedFile() {
		return fixedFile != null;
	}
	
	public File getReportFile() {
		return reportFile;
	};

	public boolean createReportFile() {
		return reportFile != null;
	}
}
