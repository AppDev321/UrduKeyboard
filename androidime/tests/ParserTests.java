import com.mobiletin.ime.InputMethod;

import org.junit.Test;

public class ParserTests {

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistantMethod() {
        InputMethod im = InputMethod.fromName("does-not-exist");
    }

}
