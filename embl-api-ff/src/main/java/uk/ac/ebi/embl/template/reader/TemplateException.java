package uk.ac.ebi.embl.template.reader;

public class TemplateException extends Exception {
    private static final long serialVersionUID = 1L;

    public TemplateException() {
        super();
    }

    public TemplateException(final String message) {
        super(message);
    }

    public TemplateException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TemplateException(Throwable cause) {
        super(cause);
    }
}
