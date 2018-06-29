package uk.ac.ebi.embl.api.validation;

import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.plan.ValidationUnit;

import java.util.ArrayList;
import java.util.List;

public class ChecksAndFixFilter {

    private static List<Class<? extends EmblEntryValidationCheck<?>>> checks;
    private static List<Class<? extends EmblEntryValidationCheck<?>>> fix;

    public static void main(String args[]) {
        new ChecksAndFixFilter().filterChecksAndFix(ValidationScope.NCBI);
        checks.forEach(System.out::println);
        System.out.println("==================================================================");
        fix.forEach(System.out::println);
    }

    private void filterChecksAndFix(ValidationScope valScope){
        checks = new ArrayList<>();
        fix = new ArrayList<>();

        for(ValidationUnit val : ValidationUnit.values()) {

            for(Class<? extends EmblEntryValidationCheck<?>> checkOrFix: val.getValidationUnit()){

                ExcludeScope excludeScopeAnnotation =  checkOrFix.getAnnotation(ExcludeScope.class);
                boolean isInScope = true;
                if(excludeScopeAnnotation!=null && excludeScopeAnnotation.validationScope().length>0) {

                    for(ValidationScope scope:excludeScopeAnnotation.validationScope()){
                        if(scope.equals(valScope)){
                           isInScope = false;
                        }
                    }
                }

                if(isInScope) {
                    if (checkOrFix.getCanonicalName().endsWith("Fix")) {
                        fix.add(checkOrFix);
                    } else {
                        checks.add(checkOrFix);
                    }
                }

            }
        }

    }

    public List<Class<? extends EmblEntryValidationCheck<?>>> getChecks(ValidationScope scope) {
        if(checks == null)
            filterChecksAndFix(scope);
        return checks;
    }

    public List<Class<? extends EmblEntryValidationCheck<?>>> getFix(ValidationScope scope) {
        if(fix == null)
            filterChecksAndFix(scope);
        return fix;
    }

}
