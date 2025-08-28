package hello;

import org.junit.Test;
import static org.junit.Assert.*;

public class HelloWorldTest {
    
    @Test
    public void testGetMessage() {
        HelloWorld hello = new HelloWorld();
        assertEquals("Hello World!", hello.getMessage());
    }
    
    @Test
    public void testNotNull() {
        HelloWorld hello = new HelloWorld();
        assertNotNull(hello.getMessage());
    }
}
