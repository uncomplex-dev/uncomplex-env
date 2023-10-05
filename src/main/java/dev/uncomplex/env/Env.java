/*
 * @author James Thorpe <james@uncomplex.dev>
 */
package dev.uncomplex.env;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/***
 * Env variable
 * 
 * var              = name WSP '=' WSP (quoted_string / string) WSP (eol / eof)
 * name             = 1*(ALPHA / "_") *(ALPHA / DIGIT / "_")
 * string           = *%x20-10FFFF
 * quoted_string    = DQUOTE *(quoted_char / escape / expansion) DQUOTE
 * quoted_char      = %x20-21 / %x23-5B / %x5D-7A / %x7C-10FFFF                 ; unicode >= SP - ["\{]
 * escape           = %x22 / ; "
 *                    "\" /
 *                    "/" /
 *                    "{" /
 *                    "b" /
 *                    "f" /
 *                    "n" /
 *                    "r" /
 *                    "t" /
 *                    "u" 4HEXDIG
 * expansion        = "{" name "}"
 * eof              = 0xFFFFFFFF
 * eol              = *CR.LF
 * 
 */
public class Env {

    private final Map<String,String> values = new HashMap<>();
    private int c;
    private int pos;
    private Reader r;

    public String get(String key, String defaultValue) {
        var val = values.get(key);
        return (val != null) ? val : defaultValue;
    }

    public String get(String key) {
        return values.get(key);
    }
    
    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        var val = get(key, null);
        return val == null ? defaultValue : new BigDecimal(val);
    }


    public int getInt(String key, int defaultValue) {
        var val = get(key, null);
        return val == null ? defaultValue : Integer.parseInt(val);
    }


    public long getLong(String key, long defaultValue) {
        var val = get(key, null);
        return val == null ? defaultValue : Long.parseLong(val);
    }


    public boolean setIfUndefined(String key, String value) {
        if (!values.containsKey(key)) {
            set(key, value);
            return true;
        } else {
            return false;
        }
    }

    public void set(String key, String value) {
        values.put(key, value);
    }



    private String parseVarName() throws IOException, ParseException {
        var sb = new StringBuilder();
        c = r.read();
        while (c != -1 && c != '}') {
            sb.appendCodePoint(c);
        }
        checkEof();
        return r.toString().trim().toLowerCase();
    }

    private boolean isWs(int ch) {
        return ch == 0x20 || ch == 0x09 || ch == 0x0a || ch == 0x0d;
    }
    
    private String readString() throws IOException, ParseException {
        consumeOrError('"');
        StringBuilder sb = new StringBuilder();
        while (!consume('"')) {
            if (consume('\\')) {
                switch (c) {
                    case '\\':
                    case '"':
                        break;
                    case '{':
                        // do var expansion
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 'r':
                        c = '\r';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    case 'f':
                        c = '\f';
                        break;
                    case 'b':
                        c = '\b';
                        break;
                    case 'u':
                        readChar();
                        int ch = (readHexDigit() << 12)
                                + (readHexDigit() << 8)
                                + (readHexDigit() << 4)
                                + (readHexDigit());
                        sb.appendCodePoint(ch);
                        continue; // while()
                }
            }
            sb.appendCodePoint(c);
            readChar();
        }

        return sb.toString();
    }
    
    private void skipWs() throws IOException {
        while (isWs(c)) {
            readChar();
        }
    }
    
    private boolean consume(int ch) throws IOException {
        if (ch == c) {
            readChar();
            return true;
        }
        return false;
    }

        private int readHexDigit() throws IOException, ParseException {
        int ch = c;
        if (ch >= '0' && ch <= '9') {
            readChar();
            return ch - '0';
        }
        if (ch >= 'A' && ch <= 'F') {
            readChar();
            return ch + 10 - 'A';
        }
        if (ch >= 'a' && ch <= 'f') {
            readChar();
            return ch + 10 - 'a';
        }
        throw error("invalid hex digit '%c'", ch);
    }
    
    private void consumeOrError(int ch) throws IOException, ParseException {
        if (ch == c) {
            readChar();
        } else {
            throw error("'%c' expected but '%c' found", ch, c);
        }
    }

    private ParseException error(String msg, Object... params) {
        String m = String.format("Reader Error [%d]: %s", pos, msg);
        return new ParseException(String.format(m, params), pos);
    }
    
    /*
    Read input byte stream and record position for error handling
     */
    private void readChar() throws IOException {
        c = r.read();
        ++pos;
    }

    private void checkEof() throws ParseException {
        if (c == -1) {
            throw error("Unexpected end of input");
        }
    }

}
