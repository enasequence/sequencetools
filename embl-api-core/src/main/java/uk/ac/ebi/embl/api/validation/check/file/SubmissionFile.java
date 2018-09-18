package uk.ac.ebi.embl.api.validation.check.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum SubmissionFile {

	FASTA, FLATFILE;
	List<File> files = new ArrayList<File>();

	public void addFile(File file) {
		files.add(file);
	}

	public List<File> getFiles() {
		return files;
	}

	public void clear() {
		files.clear();

	}

}
