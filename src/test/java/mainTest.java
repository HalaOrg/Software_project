import edu.library.Main;
import edu.library.service.AuthService;
import edu.library.model.Roles;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class mainTest {
    @BeforeAll
    static void setUpBeforeClass() {
    }

    @AfterAll
    static void tearDownAfterClass() {
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }
    @Test
    void testMain(@TempDir Path tempDir) {
        // Redirect System.in and System.out
        String input = "exit\n"; // Simulate user input to exit immediately
        ByteArrayInputStream inContent = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;
        System.setIn(inContent);
        System.setOut(new PrintStream(outContent));

        // Run the main method
        Main.main(new String[]{});

        // Restore original System.in and System.out
        System.setOut(originalOut);
        System.setIn(originalIn);

        // Check output contains welcome message
        String outputString = outContent.toString();
        assertTrue(outputString.contains("Welcome to the Library Management System"));
    }




}
