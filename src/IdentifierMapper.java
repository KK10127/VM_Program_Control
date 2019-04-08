
import java.util.ArrayList;

/**
 * Maps and identifies any unique labels that may need to be generate
 */
public class IdentifierMapper extends ArrayList<String> {

    private int labelCounter;

    public IdentifierMapper() {
        super();
    }

    public String getLabelFromValue(String value) {
        if (contains(value)) {
            return "(" + value + ")";
        } else {
            return null;
        }
    }

    public String getACommandFromValue(String value) {
        if (contains(value)) {
            return "@" + value;
        } else {
            return null;
        }
    }

}
