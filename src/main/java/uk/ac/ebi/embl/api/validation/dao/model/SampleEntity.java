package uk.ac.ebi.embl.api.validation.dao.model;

import java.util.HashMap;
import java.util.Map;

public class SampleEntity {
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    Map<String,String> attributes = new HashMap<>();

}
