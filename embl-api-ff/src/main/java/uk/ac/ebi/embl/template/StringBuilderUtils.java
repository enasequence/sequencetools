package uk.ac.ebi.embl.template;

import java.util.regex.Pattern;

public class StringBuilderUtils {
    public static final String tokenRegex = "^([\\s\\S]*)(\\{\\S*\\})([\\s\\S]*)$";
    public static final Pattern tokenPattern = Pattern.compile(tokenRegex);

    public static String encloseToken(String token) {
        return new StringBuilder(TemplateProcessorConstants.TOKEN_DELIMITER).append(token).append(TemplateProcessorConstants.TOKEN_CLOSE_DELIMITER).toString();
    }

    static boolean doesBuilderContain(String tokenName,
                                      StringBuilder stringBuilder) {

        return stringBuilder.indexOf(tokenName) != -1;
    }

    static void deleteAllOccurrences(String toDelete,
                                     StringBuilder stringBuilder) {

        while (doesBuilderContain(toDelete, stringBuilder)) {
            deleteString(toDelete, stringBuilder);
        }
    }

    static void deleteString(String tokenName,
                             StringBuilder stringBuilder) {

        int tokenIndex = stringBuilder.indexOf(tokenName);
        if (tokenIndex != -1) {
            stringBuilder.delete(tokenIndex, tokenIndex + tokenName.length());
        }
    }

    public static void removeUnmatchedTokenLines(StringBuilder stringBuilder) {
        StringBuilderLineIterator lineIterator = new StringBuilderLineIterator(stringBuilder);
        while (lineIterator.hasNext()) {
            String currentLine = lineIterator.next();
            boolean tokenMatched = tokenPattern.matcher(currentLine).matches();
            String trimmedLine = currentLine.trim();
            if (tokenMatched || trimmedLine.isEmpty() || trimmedLine.equals("\n"))
                lineIterator.remove();
        }
    }

    public static void deleteBetweenStrings(String fromToken,
                                            String toToken,
                                            StringBuilder currentBuilder) {
        int tokenIndex = currentBuilder.indexOf(fromToken);
        //add on the length of the token as we dont want this
        int tokenCloseIndex = currentBuilder.indexOf(toToken) + toToken.length();

        if (tokenIndex != -1 && tokenCloseIndex != -1 && tokenCloseIndex > tokenIndex) {
            currentBuilder.delete(tokenIndex, tokenCloseIndex);
        }
    }

    public static void doReplace(String stringToFind,
                                 String stringToReplace,
                                 StringBuilder builder) {

        int replaceIndex = builder.indexOf(stringToFind);
        if (replaceIndex != -1) {
            builder.replace(replaceIndex, replaceIndex + stringToFind.length(), stringToReplace);
        }

//        String currentEntryString = builder.toString();
//        builder = new StringBuilder(currentEntryString.replace(stringToFind, stringToReplace));
    }

}
