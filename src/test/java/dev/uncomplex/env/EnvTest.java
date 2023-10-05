package dev.uncomplex.env;


import dev.uncomplex.env.Env;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvTest {

    static Map<String,String> params = new HashMap<>();

    static {
        params.put("TEST_PARAM", "test-value");
    }

    @org.junit.jupiter.api.Test
    void testGrammer() {
        test("key=value", "key", "value");
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
    }
    
    @org.junit.jupiter.api.Test
    void testSystemProperties() {
        test("key=${user.dir}", "key", System.getProperty("user.dir"));
    }    
    
    @org.junit.jupiter.api.Test
    void testEnvironmentProperties() {
        test("key=${path}", "key", System.getenv("PATH"));
    }  

    void test(String input, String resultKey, String resultValue) {
        try {
            Env p = new Env();
            p.load(new StringReader(input), params);
            assertTrue(p.stringPropertyNames().contains(resultKey));
            assertEquals(resultValue, p.getProperty(resultKey));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    void testEx(String input, String msg) {
        try {
            Env p = new Env();
            p.load(new StringReader(input), params);
            fail();
        } catch (RuntimeException e) {
            assertEquals(msg, e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}