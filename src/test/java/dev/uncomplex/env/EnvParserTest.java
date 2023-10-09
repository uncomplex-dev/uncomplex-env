package dev.uncomplex.env;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EnvParserTest {

    @Test
    public void debug() throws IOException, ParseException {
        test("key_=\"va\r\nlue\"", "value");
    }

    @Test
    public void testStrings() throws IOException, ParseException {
        test("key_=\"\"", "");
        test("key_=\"test\"", "test");
        test("key_=\"a\\\"b\\\"\"", "a\"b\"");
        test("key_=\"\\\\\\\"\"", "\\\"");
        test("key_=\"\\\r\\\n\\\t\\\f\\\b\"", "\r\n\t\f\b");
        test("key_=\"\u0000\"", "\u0000");
        test("key_=\"\u0001\"", "\u0001");
        test("key_=\"\u0ABC\"", "\u0ABC");
        test("key_=\"\u0abc\"", "\u0abc");
        // quoted strings must be followed by eol or eof
        var e = assertThrows(ParseException.class, () -> testThrows("key_=\"test\" err"));
        assertEquals("java.text.ParseException: Reader Error [line 1]: expected end-of-line but 'e' found", e.toString());
        // missing terminal quote
        e = assertThrows(ParseException.class, () -> testThrows("key_=\"test"));
        assertEquals("java.text.ParseException: Reader Error [line 1]: quoted value missing closing '\"'", e.toString());
        System.out.println(e);
    }

    @Test
    public void testGrammar() throws IOException, ParseException {
        test("", null);
        test("key_=", "");
        test("key_=value", "value");
        test("key_=\"\"", "");
        test("key_=\"value\"", "value");
        // single key multiline value
        test("key_=\"va\r\nlue\"", "value");
        // multiple keys
        test("key_=\"value\"\nkey2_=\"value2\"", "value");
        test("key_=\"value\"\nkey2_=\"value2\"", "key2_", "value2");
    }

    
    
    @Test
    public void testEnvVars() throws IOException, ParseException {
        /* 
        please note that this test requires the environment variable
        UNIT_TEST_ be defined prior to running the test.  This can be
        done in your IDE or the maven pom file (or both).
        */
        var r = new StringReader("");
        var e = new Env(r);
        assertEquals("", e.get("UNIT_TEST_X", ""));
        assertEquals("testvalue", e.get("UNIT_TEST_", ""));
    }

    void testThrows(String input) throws IOException, ParseException {
        var r = new StringReader(input);
        new Env(r);
    }

    void test(String input, String value) throws IOException, ParseException {
        test(input, "key_", value);
    }

    void test(String input, String key, String value) throws IOException, ParseException {
        var r = new StringReader(input);
        var e = new Env(r);
        var v = e.get(key);
        assertEquals(value, v);
    }

}
