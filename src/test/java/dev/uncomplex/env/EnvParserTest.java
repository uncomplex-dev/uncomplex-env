package dev.uncomplex.env;


import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EnvParserTest {

    
    @Test
    public void debug() {
        test("key_=\"va\r\nlue\"", "value");
    }

    @Test
    public void testStrings() {
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
    public void testGrammar() {
        test("key_=", "");
        test("key_=value", "value");
        test("key_=\"\"", "");
        test("key_=\"value\"", "value");
        test("key_=\"va\r\nlue\"", "value");
    }
    
    
    void testThrows(String input) throws IOException, ParseException {
            var r = new StringReader(input);
            new Env(r);
    }
    
    
    void test(String input, String result) {
        try {
            var r = new StringReader(input);
            var e = new Env(r);
            var v = e.get("key_");
            assertEquals(result, v);
        } catch (IOException | ParseException e) {
            fail(e.getMessage());
        }
    }

}