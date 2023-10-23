/*
 * @author James Thorpe <james@uncomplex.dev>
 */
package dev.uncomplex.env;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class Env {

    private static final List<String> TRUE = List.of("Y", "YES", "T", "TRUE", "1");
    private Map<String, String> values;

    public Env() throws IOException, ParseException {
        this(new File(".env"));
    }
    
    public Env(File f) throws IOException, ParseException {
        f = f.getAbsoluteFile();
        try( var r = new FileReader(f)) {
            var reader = new BufferedReader(r);
            this.values = EnvParser.parse(reader);
        }
    }
    
    public Env(Reader r) throws IOException, ParseException {
        this.values = EnvParser.parse(r);
    }
     
    public String get(String key, String defaultValue) {
        var val = lookup(key);
        return (val != null) ? val : defaultValue;
    }

    public String get(String key) {
        return lookup(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        var val = lookup(key);
        return val == null ? defaultValue : TRUE.contains(val.toUpperCase());
    }


    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        var val = lookup(key);
        return val == null ? defaultValue : new BigDecimal(val);
    }

    public int getInt(String key, int defaultValue) {
        var val = lookup(key);
        return val == null ? defaultValue : Integer.parseInt(val);
    }

    public long getLong(String key, long defaultValue) {
        var val = lookup(key);
        return val == null ? defaultValue : Long.parseLong(val);
    }
    
    private String lookup(String key) {
        var v = System.getenv(key);
        return v != null ? v : values.get(key);
    }

}
