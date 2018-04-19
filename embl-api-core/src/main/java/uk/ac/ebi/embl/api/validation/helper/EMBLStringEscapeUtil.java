package uk.ac.ebi.embl.api.validation.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EMBLStringEscapeUtil {

    private static final Pattern entityNumberPattern = Pattern.compile("^&#([0-9]{2,3});$");
    private static final Map<String, Character> entityNameToChar = new HashMap<String, Character>() {{
        put("&amp;", '&');
        put("&lt;", '<');
        put("&gt;", '>');
    }};

    public static StringBuilder escapeASCIIHtmlEntities(final CharSequence input) {
        if(input == null)
            return null;

        StringBuilder replaced = new StringBuilder();
        StringBuilder toBeReplaced = new StringBuilder();
        boolean isEntity = false;

        for(int i =0; i<input.length();i++){
            char c = input.charAt(i);
            if (isEntity) {
                if (c == '&') {
                    replaced.append(toBeReplaced);
                    toBeReplaced = new StringBuilder();
                    toBeReplaced.append(c);
                } else {
                    toBeReplaced.append(c);
                    if (c == ';') {
                        if (entityNameToChar.containsKey(toBeReplaced.toString())) {
                            replaced.append(entityNameToChar.get(toBeReplaced.toString()));
                        } else {
                            Matcher m = entityNumberPattern.matcher(toBeReplaced);
                            if (m.matches()) {
                                int ascii = Integer.parseInt(m.group(1));
                                replaced.append(ascii >= 32 && ascii <= 126 ? Character.toString((char) ascii) : toBeReplaced);
                            } else {
                                replaced.append(toBeReplaced);
                            }
                        }
                        isEntity = false;
                        toBeReplaced = new StringBuilder();
                    }
                }
            } else if (c == '&') {
                isEntity = true;
                toBeReplaced.append(c);
            } else {
                replaced.append(c);
            }
        }

        if (isEntity) {
            replaced.append(toBeReplaced);
        }

        return replaced;
    }

}
