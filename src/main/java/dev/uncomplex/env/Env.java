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
import java.util.HashMap;
import java.util.Map;

/*
    Environment variable processing

    var             = WSP name WSP '=' WSP (quoted_value / value) WSP (eol / eof)
    name            = 1*(ALPHA / "_") *(ALPHA / DIGIT / "_")
    value           = *(%x20-10FFFFF)
    quoted_value    = DQUOTE *(char / eol) DQUOTE
    char            = unescaped /
                        escape (
                           %x22 / ; "
                           "\" /
                           "/" /
                           "{" /
                           "b" /
                           "f" /
                           "n" /
                           "r" /
                           "t" /
                           "u" 4HEXDIG )
    unescaped       = %x20-21 / %x23-5B / %x5D-7A / %x7C-10FFFF   ; UNICODE >= SP - ["\{]
    escape          = "\"
    eof             = 0xFFFFFFFF
    eol             = *CR.LF
 */
public class Env {

    private int c;
    private int pos;
    private final Reader r;
    private final Map<String, String> values = new HashMap<>();

    public Env(Reader r) throws IOException, ParseException {
        this.r = r;
        readFile();
    }
     
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

    public void set(String key, String value) {
        values.put(key, value);
    }

    public boolean setIfUndefined(String key, String value) {
        if (!values.containsKey(key)) {
            set(key, value);
            return true;
        } else {
            return false;
        }
    }

    private void checkEof() throws ParseException {
        if (c == -1) {
            throw error("Unexpected end of input");
        }
    }

    private boolean consume(int ch) throws IOException {
        if (ch == c) {
            readChar();
            return true;
        }
        return false;
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

    private boolean isEol() {
        return c == 0x0a || c == 0x0d;
    }

    private boolean isWs() {
        return c == 0x20 || c == 0x09;
    }
    
    private boolean isNameChar() {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private String readName() throws IOException, ParseException {
        var sb = new StringBuilder();
        while (isNameChar()) {
            sb.appendCodePoint(c);
            readChar();
        }
        checkEof();
        return sb.toString();
    }

    /*
    Read input byte stream and record position for error handling
     */
    private void readChar() throws IOException {
        c = r.read();
        ++pos;
    }

    private void readFile() throws IOException, ParseException {
        readChar();
        skipEol();
        while (c != -1) {
            readLine();
            skipEol();
        }
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

    private void readLine() throws IOException, ParseException {
        skipWs();
        var name = readName();
        skipWs();
        consumeOrError('=');
        skipWs();
        var value = readValue();
        skipWs();
        values.put(name, value);
    }

    private String readValue() throws IOException, ParseException {
        if (consume('"')) {
            return readQuotedString();
        } else {
            var sb = new StringBuilder();
            while(!isEol() && c != -1) {
                sb.appendCodePoint(c);
                readChar();
            }
            return sb.toString();
        }
    }
    
    private String readQuotedString() throws IOException, ParseException {
        StringBuilder sb = new StringBuilder();
        while (!consume('"')) {
            checkEof();
            if (consume('\\')) {
                int ch;
                switch (c) {
                    case 'n' ->
                        ch = '\n';
                    case 'r' ->
                        ch = '\r';
                    case 't' ->
                        ch = '\t';
                    case 'f' ->
                        ch = '\f';
                    case 'b' ->
                        ch = '\b';
                    case 'u' -> {
                        readChar();
                        ch = (readHexDigit() << 12)
                                + (readHexDigit() << 8)
                                + (readHexDigit() << 4)
                                + (readHexDigit());
                    }
                    default ->
                        ch = c;
                }
                sb.appendCodePoint(ch);
            } else if (!isEol()) {
                sb.appendCodePoint(c);
            }
            readChar();
        }
        return sb.toString();
    }

    private void skipEol() throws IOException {
        while (c == '\r' || c == '\n') {
            readChar();
        }
    }

    private void skipWs() throws IOException {
        while (isWs()) {
            readChar();
        }
    }

}
