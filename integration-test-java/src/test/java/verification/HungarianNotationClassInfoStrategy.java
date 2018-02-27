package verification;

import java.util.regex.Pattern;

import uk.co.jemos.podam.api.AbstractClassInfoStrategy;

/**
 * A class info strategy that allows for hungarian notation in field names.
 * <p>
 * Created by restainoa on 2/27/18.
 */
public class HungarianNotationClassInfoStrategy extends AbstractClassInfoStrategy {

    @Override
    protected String extractFieldNameFromMethod(String methodName, Pattern pattern) {
        final String fieldName = super.extractFieldNameFromMethod(methodName, pattern);
        return "m" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

}
