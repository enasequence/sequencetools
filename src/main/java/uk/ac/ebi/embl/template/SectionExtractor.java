package uk.ac.ebi.embl.template;

import java.util.List;

public class SectionExtractor {
    private StringBuilder currentBuilder;

    protected void removeSections(StringBuilder currentEntryBuilder,
                                  List<TemplateSectionInfo> sectionInfos,
                                  TemplateVariables variables) {

        this.currentBuilder = currentEntryBuilder;

        for (TemplateSectionInfo currentSectionInfo : sectionInfos) {
            List<TemplateTokenInfo> dependsList = currentSectionInfo.getDepends();

            /**
             * look to see if all of the dependent tokens of the current section are present - if so, do not strip this section.
             */
            boolean stripCurrentSection = false;
            for (TemplateTokenInfo dependentToken : dependsList) {

                boolean variablesDoesNotContain = false;

                if (variablesDoesNotContainToken(variables, dependentToken)) {
                    variablesDoesNotContain = true;
                }

                if (variablesDoesNotContain) {
                    stripCurrentSection = true;
                    break;
                }
            }

            if (stripCurrentSection) {
                deleteSectionFromBuilder(currentSectionInfo);
            } else {//just remove the currentSectionInfo tokens
                deleteSectionTokensFromBuilder(currentSectionInfo, currentBuilder);
            }
        }
    }

    private boolean variablesDoesNotContainToken(TemplateVariables variables,
                                                 TemplateTokenInfo tokenInfo) {

        String tokenName = tokenInfo.getName();

        /**
         * in order - is there a variables map?,
         * does it contain the key?,
         * is the value empty?,
         * is it a yes/no type and if so is the value no?
         */
        return variables == null ||
                !variables.containsToken(tokenName) ||
                variables.getTokenValue(tokenName) == null ||
                variables.getTokenValue(tokenName).isEmpty() ||
                (tokenInfo.getType() == TemplateTokenType.BOOLEAN_FIELD && variables.getTokenValue(tokenName).equalsIgnoreCase(TemplateTokenInfo.NO_VALUE));
    }

    private void deleteSectionFromBuilder(TemplateSectionInfo currentSectionInfo) {
        String tokenStartName = encloseSectionStartToken(currentSectionInfo);
        String tokenCloseName = encloseSectionEndToken(currentSectionInfo);

        while (StringBuilderUtils.doesBuilderContain(tokenStartName, currentBuilder) &&
                StringBuilderUtils.doesBuilderContain(tokenCloseName, currentBuilder)) {
            StringBuilderUtils.deleteBetweenStrings(tokenStartName, tokenCloseName, currentBuilder);
        }
    }

    private static void deleteSectionTokensFromBuilder(TemplateSectionInfo currentSectionInfo,
                                                       StringBuilder stringBuilder) {

        String sectionOpenName = encloseSectionStartToken(currentSectionInfo);
        String sectionCloseName = encloseSectionEndToken(currentSectionInfo);

        StringBuilderUtils.deleteAllOccurrences(sectionOpenName, stringBuilder);
        StringBuilderUtils.deleteAllOccurrences(sectionCloseName, stringBuilder);
    }

    static String encloseSectionStartToken(TemplateSectionInfo currentSectionInfo) {
        StringBuilder builder = new StringBuilder(TemplateProcessorConstants.SECTION_DELIMITER);
        builder.append(currentSectionInfo.getName());
        builder.append(TemplateProcessorConstants.SECTION_CLOSE_DELIMITER);
        return builder.toString();
    }

    static String encloseSectionEndToken(TemplateSectionInfo currentSectionInfo) {
        StringBuilder builder = new StringBuilder(TemplateProcessorConstants.SECTION_DELIMITER);
        builder.append("/");
        builder.append(currentSectionInfo.getName());
        builder.append(TemplateProcessorConstants.SECTION_CLOSE_DELIMITER);

        return builder.toString();
    }

}
