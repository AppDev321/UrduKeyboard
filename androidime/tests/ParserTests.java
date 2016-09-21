import java.lang.IllegalArgumentException;
import org.junit.Test;
import com.mobiletin.ime.InputMethod;

public class ParserTests {

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistantMethod() {
        InputMethod im = InputMethod.fromName("does-not-exist");
    }

}
