package uk.ac.ebi.embl.api.validation.submission;

public class SubmissionProperty<T> {

	private T value;

	public SubmissionProperty(T value) {
		this.value = value;
	}

	public void set(T value) {
		this.value = value;
	}

	public T get() {
		return value;
	}

}
