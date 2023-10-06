package dev.uncomplex.env;


import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class EnvTest {


    @org.junit.jupiter.api.Test
    public void testDebug() {
    }

    
    @org.junit.jupiter.api.Test
    public void testGrammar() {
        test("key=", "");
        test("key=value", "value");
        test("key=\"\"", "");
        test("key=\"value\"", "value");
        test("key=\"va\r\nlue\"", "value");
                
/*                
        test(" key = value ", "key", "value");
        test("key=", "key", "");
        test("key = ", "key", "");
        test("=value", "", "value");
        test(" = value ", "", "value");
        test("key=va$lue", "key", "va$lue");
        test("key=va$$lue", "key", "va$$lue");
        test("key=value$", "key", "value$");
        test("key=${TEST_PARAM}", "key", "test-value");
        test(" key = ${ TEST_PARAM } ", "key", "test-value");
        test("key=a${TEST_PARAM}z", "key", "atest-valuez");
        test("key=a${TEST_PARAM:alt}z", "key", "atest-valuez");
        test("key=a${ TEST_PARAM : alt }z", "key", "atest-valuez");
        test("test=a${XOX:alt}z", "test", "aaltz");

*/
    }
    
    void test(String input, String result) {
        try {
            var r = new StringReader(input);
            var e = new Env(r);
            assertEquals(result, e.get("key"));
        } catch (IOException | ParseException e) {
            fail(e.getMessage());
        }
    }

}