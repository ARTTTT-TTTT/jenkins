package hello;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
    
    @Test
    public void testMain() {
        // Capture System.out output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        try {
            // Test main method
            HelloWorld.main(new String[]{});
            assertEquals("Hello World!\n", outContent.toString());
        } finally {
            // Restore original System.out
            System.setOut(originalOut);
        }
    }
}
