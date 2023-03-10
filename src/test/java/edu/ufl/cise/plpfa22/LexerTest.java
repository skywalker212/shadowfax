/**
 * This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized.
 */

package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.IToken.Kind;
import org.junit.jupiter.api.Test;

import static edu.ufl.cise.plpfa22.IToken.Kind.*;
import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    /*** Useful functions ***/
    ILexer getLexer(String input) {
        return CompilerComponentFactory.getLexer(input);
    }

    //makes it easy to turn output on and off (and less typing than System.out.println)
    static final boolean VERBOSE = true;

    void show(Object obj) {
        if (VERBOSE) {
            System.out.println(obj);
        }
    }

    //check that this token has the expected kind
    void checkToken(IToken t, Kind expectedKind) {
        assertEquals(expectedKind, t.getKind());
    }

    //check that the token has the expected kind and position
    void checkToken(IToken t, Kind expectedKind, int expectedLine, int expectedColumn) {
        assertEquals(expectedKind, t.getKind());
        assertEquals(new IToken.SourceLocation(expectedLine, expectedColumn), t.getSourceLocation());
    }

    //check that this token is an IDENT and has the expected name
    void checkIdent(IToken t, String expectedName) {
        assertEquals(Kind.IDENT, t.getKind());
        assertEquals(expectedName, String.valueOf(t.getText()));
    }

    //check that this token is an IDENT, has the expected name, and has the expected position
    void checkIdent(IToken t, String expectedName, int expectedLine, int expectedColumn) {
        checkIdent(t, expectedName);
        assertEquals(new IToken.SourceLocation(expectedLine, expectedColumn), t.getSourceLocation());
    }


    //check that this token is an NUM_LIT with expected int value
    void checkInt(IToken t, int expectedValue) {
        assertEquals(Kind.NUM_LIT, t.getKind());
        assertEquals(expectedValue, t.getIntValue());
    }

    //check that this token  is an NUM_LIT with expected int value and position
    void checkInt(IToken t, int expectedValue, int expectedLine, int expectedColumn) {
        checkInt(t, expectedValue);
        assertEquals(new IToken.SourceLocation(expectedLine, expectedColumn), t.getSourceLocation());
    }

    // check that this token is a STRING_LIT with expected string value and position
    void checkString(IToken t, String expectedValue, int expectedLine, int expectedColumn) {
        assertEquals(Kind.STRING_LIT, t.getKind());
        assertEquals(expectedValue, t.getStringValue());
        assertEquals(new IToken.SourceLocation(expectedLine, expectedColumn), t.getSourceLocation());
    }


    //check that this token is the EOF token
    void checkEOF(IToken t) {
        checkToken(t, Kind.EOF);
    }

    /***Tests****/

    //The lexer should add an EOF token to the end.
    @Test
    void testEmpty() throws LexicalException {
        ILexer lexer = getLexer("");
        checkEOF(lexer.next());

        lexer = getLexer(" \n\t");
        checkEOF(lexer.next());
        checkEOF(lexer.next());
        checkEOF(lexer.next());
    }

    //A couple of single character tokens
    @Test
    void testSingleChar0() throws LexicalException {
        String input = """
                + 
                - 	 
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.PLUS, 1, 1);
        checkToken(lexer.next(), Kind.MINUS, 2, 1);
        checkEOF(lexer.next());
    }

    //comments should be skipped
    @Test
    void testComment0() throws LexicalException {
        //Note that the quotes around "This is a string" are passed to the lexer.
        String input = """
                "This is a string"
                // this is a comment
                *
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.STRING_LIT, 1, 1);
        checkToken(lexer.next(), Kind.TIMES, 3, 1);
        checkEOF(lexer.next());
    }

    //Example for testing input with an illegal character
    @Test
    void testError0() throws LexicalException {
        String input = """
                abc
                @
                """;
        show(input);
        ILexer lexer = getLexer(input);
        //this check should succeed
        checkIdent(lexer.next(), "abc", 1, 1);
        //this is expected to throw an exception since @ is not a legal
        //character unless it is part of a string or comment
        assertThrows(LexicalException.class, () -> {
            @SuppressWarnings("unused")
            IToken token = lexer.next();
        });
    }

    //Several identifiers to test positions
    @Test
    public void testIdent0() throws LexicalException {
        String input = """
                abc
                  def
                     ghi

                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "abc", 1, 1);
        checkIdent(lexer.next(), "def", 2, 3);
        checkIdent(lexer.next(), "ghi", 3, 6);
        checkEOF(lexer.next());
    }


    @Test
    public void testIdenInt() throws LexicalException {
        String input = """
                a123 456b
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "a123", 1, 1);
        checkInt(lexer.next(), 456, 1, 6);
        checkIdent(lexer.next(), "b", 1, 9);
        checkEOF(lexer.next());
    }


    //Example showing how to handle number that are too big.
    @Test
    public void testIntTooBig() throws LexicalException {
        String input = """
                42
                99999999999999999999999999999999999999999999999999999999999999999999999
                """;
        show(input);
        final ILexer lexer = getLexer(input);
        checkInt(lexer.next(), 42, 1, 1);
        assertThrows(LexicalException.class, lexer::next);
    }

    @Test
    public void testInt() throws LexicalException {
        String input = """
                42
                0
                10
                135
                200
                257
                3000
                379
                40000
                426
                500000
                538
                60
                649
                700
                757
                800
                88
                90
                913
                00
                01
                """;
        show(input);
        final ILexer lexer = getLexer(input);
        checkInt(lexer.next(), 42, 1, 1);
        checkInt(lexer.next(), 0, 2, 1);
        checkInt(lexer.next(), 10, 3, 1);
        checkInt(lexer.next(), 135, 4, 1);
        checkInt(lexer.next(), 200, 5, 1);
        checkInt(lexer.next(), 257, 6, 1);
        checkInt(lexer.next(), 3000, 7, 1);
        checkInt(lexer.next(), 379, 8, 1);
        checkInt(lexer.next(), 40000, 9, 1);
        checkInt(lexer.next(), 426, 10, 1);
        checkInt(lexer.next(), 500000, 11, 1);
        checkInt(lexer.next(), 538, 12, 1);
        checkInt(lexer.next(), 60, 13, 1);
        checkInt(lexer.next(), 649, 14, 1);
        checkInt(lexer.next(), 700, 15, 1);
        checkInt(lexer.next(), 757, 16, 1);
        checkInt(lexer.next(), 800, 17, 1);
        checkInt(lexer.next(), 88, 18, 1);
        checkInt(lexer.next(), 90, 19, 1);
        checkInt(lexer.next(), 913, 20, 1);
        checkInt(lexer.next(), 0, 21, 1);
        checkInt(lexer.next(), 0, 21, 2);
        checkInt(lexer.next(), 0, 22, 1);
        checkInt(lexer.next(), 1, 22, 2);
        checkEOF(lexer.next());
    }

    @Test
    public void testWhiteSpace() throws LexicalException {
        String input = "123 \n\r \t\t\r456";
        show(input);
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), NUM_LIT, 1, 1);
        checkToken(lexer.next(), NUM_LIT, 2, 6);
        checkEOF(lexer.next());
    }

    @Test
    public void testNewLine() throws LexicalException {
        String input = "123 \n\r \t\t\r\r\n\n\n 456";
        show(input);
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), NUM_LIT, 1, 1);
        checkToken(lexer.next(), NUM_LIT, 5, 2);
        checkEOF(lexer.next());
    }

    @Test
    public void testEscapeSequences0() throws LexicalException {
        String input = "\"\\b \\t \\n \\f \\r \"";
        show(input);
        ILexer lexer = getLexer(input);
        IToken t = lexer.next();
        checkString(t, "\b \t \n \f \r ", 1, 1);
        assertEquals("\"\\b \\t \\n \\f \\r \"", String.valueOf(t.getText()));

        lexer = getLexer("\"\\b \\z\"");
        assertThrows(LexicalException.class, lexer::next);
    }

    @Test
    public void testEscapeSequences1() throws LexicalException {
        String input = "   \" ...  \\\"  \\\'  \\\\  \"";
        show(input);
        ILexer lexer = getLexer(input);
        IToken t = lexer.next();
        String expectedStringValue = " ...  \"  \'  \\  ";
        checkString(t, expectedStringValue, 1, 4);
        String text = String.valueOf(t.getText());
        String expectedText = "\" ...  \\\"  \\\'  \\\\  \""; //almost the same as input, but white space is omitted
        assertEquals(expectedText, text);
    }

    //A couple of boolean tokens
    @Test
    void testBooleans() throws LexicalException {
        String input = """
                TRUE
                FALSE
                true
                false
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.BOOLEAN_LIT, 1, 1);
        checkToken(lexer.next(), Kind.BOOLEAN_LIT, 2, 1);
        checkToken(lexer.next(), IDENT, 3, 1);
        checkToken(lexer.next(), IDENT, 4, 1);
        checkEOF(lexer.next());
    }

    // Mix of Identifiers, Number, Comment, Keyword and String literal
    @Test
    public void testIDNNUM() throws LexicalException {
        String input = """
                df123 345 g546 IF
                //next is string

                 "Hello, World"
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "df123", 1, 1);
        checkInt(lexer.next(), 345, 1, 7);
        checkIdent(lexer.next(), "g546", 1, 11);
        checkToken(lexer.next(), Kind.KW_IF, 1, 16);
        checkToken(lexer.next(), Kind.STRING_LIT, 4, 2);
        checkEOF(lexer.next());
    }

    // All symbols
    @Test
    public void testAllSymbols() throws LexicalException {
        String input = """
                . , ; ( ) + - * / %
                //next is line 3
                ? ! := = # < <= > >=
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.DOT, 1, 1);
        checkToken(lexer.next(), Kind.COMMA, 1, 3);
        checkToken(lexer.next(), Kind.SEMI, 1, 5);
        checkToken(lexer.next(), Kind.LPAREN, 1, 7);
        checkToken(lexer.next(), Kind.RPAREN, 1, 9);
        checkToken(lexer.next(), Kind.PLUS, 1, 11);
        checkToken(lexer.next(), Kind.MINUS, 1, 13);
        checkToken(lexer.next(), Kind.TIMES, 1, 15);
        checkToken(lexer.next(), Kind.DIV, 1, 17);
        checkToken(lexer.next(), Kind.MOD, 1, 19);
        checkToken(lexer.next(), Kind.QUESTION, 3, 1);
        checkToken(lexer.next(), Kind.BANG, 3, 3);
        checkToken(lexer.next(), Kind.ASSIGN, 3, 5);
        checkToken(lexer.next(), Kind.EQ, 3, 8);
        checkToken(lexer.next(), Kind.NEQ, 3, 10);
        checkToken(lexer.next(), Kind.LT, 3, 12);
        checkToken(lexer.next(), Kind.LE, 3, 14);
        checkToken(lexer.next(), Kind.GT, 3, 17);
        checkToken(lexer.next(), Kind.GE, 3, 19);
        checkEOF(lexer.next());
    }

    // All reserved words
    @Test
    public void testAllReserved() throws LexicalException {
        String input = """
                CONST VAR PROCEDURE
                     CALL BEGIN END
                        //next is line 3
                        IF THEN WHILE DO
                       
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.KW_CONST, 1, 1);
        checkToken(lexer.next(), Kind.KW_VAR, 1, 7);
        checkToken(lexer.next(), Kind.KW_PROCEDURE, 1, 11);
        checkToken(lexer.next(), Kind.KW_CALL, 2, 6);
        checkToken(lexer.next(), Kind.KW_BEGIN, 2, 11);
        checkToken(lexer.next(), Kind.KW_END, 2, 17);
        checkToken(lexer.next(), Kind.KW_IF, 4, 9);
        checkToken(lexer.next(), Kind.KW_THEN, 4, 12);
        checkToken(lexer.next(), Kind.KW_WHILE, 4, 17);
        checkToken(lexer.next(), Kind.KW_DO, 4, 23);
        checkEOF(lexer.next());
    }

    @Test
    public void testExpression() throws LexicalException {
        String input = """
                12+3
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkInt(lexer.next(), 12, 1, 1);
        checkToken(lexer.next(), Kind.PLUS, 1, 3);
        checkInt(lexer.next(), 3, 1, 4);
        checkEOF(lexer.next());
    }

    @Test
    public void testInvalidIdentifier() throws LexicalException {
        String input = """
                $valid_123
                valid_and_symbol+
                invalid^
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "$valid_123", 1, 1);
        checkIdent(lexer.next(), "valid_and_symbol", 2, 1);
        checkToken(lexer.next(), Kind.PLUS, 2, 17);
        checkIdent(lexer.next(), "invalid", 3, 1);
        assertThrows(LexicalException.class, lexer::next);
    }

    @Test
    public void testUnterminatedString() throws LexicalException {
        String input = "\"unterminated";
        show(input);
        ILexer lexer = getLexer(input);
        assertThrows(LexicalException.class, lexer::next);
    }

    @Test
    public void testInvalidEscapeSequence() throws LexicalException {
        String input = "\"esc\\\"";
        show(input);
        ILexer lexer = getLexer(input);
        assertThrows(LexicalException.class, lexer::next);
    }

    @Test
    public void testStringLineNum() throws LexicalException {
        String input = """
                "He\nllo Wo\\nr
                ld" ident
                "Hello\\tAgain"
                """;
        show(input);
        ILexer lexer = getLexer(input);
        // escape char within string affects line number and column number
        checkString(lexer.next(), "He\nllo Wo\nr\nld", 1, 1);
        checkIdent(lexer.next(), "ident", 4, 5);
        checkString(lexer.next(), "Hello\tAgain", 5, 1);
        checkEOF(lexer.next());
    }

    //Test 8
    @Test
    void testAllChars() throws LexicalException {
        String input = """
                .,; ()+-*/%?!:==#<<=>>=
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.DOT, 1, 1);
        checkToken(lexer.next(), Kind.COMMA, 1, 2);
        checkToken(lexer.next(), Kind.SEMI, 1, 3);
        checkToken(lexer.next(), Kind.LPAREN, 1, 5);
        checkToken(lexer.next(), Kind.RPAREN, 1, 6);
        checkToken(lexer.next(), Kind.PLUS, 1, 7);
        checkToken(lexer.next(), Kind.MINUS, 1, 8);
        checkToken(lexer.next(), Kind.TIMES, 1, 9);
        checkToken(lexer.next(), Kind.DIV, 1, 10);
        checkToken(lexer.next(), Kind.MOD, 1, 11);
        checkToken(lexer.next(), Kind.QUESTION, 1, 12);
        checkToken(lexer.next(), Kind.BANG, 1, 13);
        checkToken(lexer.next(), Kind.ASSIGN, 1, 14);
        checkToken(lexer.next(), Kind.EQ, 1, 16);
        checkToken(lexer.next(), Kind.NEQ, 1, 17);
        checkToken(lexer.next(), Kind.LT, 1, 18);
        checkToken(lexer.next(), Kind.LE, 1, 19);
        checkToken(lexer.next(), Kind.GT, 1, 21);
        checkToken(lexer.next(), Kind.GE, 1, 22);
        checkEOF(lexer.next());
    }

    @Test
        // make sure your program does not confuse comments with divide
    void testCommentWithDiv() throws LexicalException {
        String input = """
                ///
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkEOF(lexer.next());
    }

    @Test
    public void testKeywordBacktoBack() throws LexicalException {
        String input = """
                DOWHILE
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "DOWHILE", 1, 1);
        checkEOF(lexer.next());
    }

    @Test
    public void testLongInput0() throws LexicalException {
        String input = """
                VAR x = 0;
                VAR y = TRUE;
                VAR z = "a";
                DO
                    x = x + 1;
                    y = !y;
                    z = z + "a";
                WHILE (x < 10)
                """;
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.KW_VAR, 1, 1);
        checkIdent(lexer.next(), "x", 1, 5);
        checkToken(lexer.next(), Kind.EQ, 1, 7);
        checkInt(lexer.next(), 0, 1, 9);
        checkToken(lexer.next(), Kind.SEMI, 1, 10);

        checkToken(lexer.next(), Kind.KW_VAR, 2, 1);
        checkIdent(lexer.next(), "y", 2, 5);
        checkToken(lexer.next(), Kind.EQ, 2, 7);
        final IToken next = lexer.next();
        assertTrue(next.getBooleanValue());
        checkToken(next, BOOLEAN_LIT, 2, 9);
        checkToken(lexer.next(), Kind.SEMI, 2, 13);

        checkToken(lexer.next(), Kind.KW_VAR, 3, 1);
        checkIdent(lexer.next(), "z", 3, 5);
        checkToken(lexer.next(), Kind.EQ, 3, 7);
        checkString(lexer.next(), "a", 3, 9);
        checkToken(lexer.next(), Kind.SEMI, 3, 12);

        checkToken(lexer.next(), Kind.KW_DO, 4, 1);

        checkIdent(lexer.next(), "x", 5, 5);
        checkToken(lexer.next(), Kind.EQ, 5, 7);
        checkIdent(lexer.next(), "x", 5, 9);
        checkToken(lexer.next(), Kind.PLUS, 5, 11);
        checkInt(lexer.next(), 1, 5, 13);
        checkToken(lexer.next(), Kind.SEMI, 5, 14);

        checkIdent(lexer.next(), "y", 6, 5);
        checkToken(lexer.next(), Kind.EQ, 6, 7);
        checkToken(lexer.next(), Kind.BANG, 6, 9);
        checkIdent(lexer.next(), "y", 6, 10);
        checkToken(lexer.next(), Kind.SEMI, 6, 11);

        checkIdent(lexer.next(), "z", 7, 5);
        checkToken(lexer.next(), Kind.EQ, 7, 7);
        checkIdent(lexer.next(), "z", 7, 9);
        checkToken(lexer.next(), Kind.PLUS, 7, 11);
        checkString(lexer.next(), "a", 7, 13);
        checkToken(lexer.next(), Kind.SEMI, 7, 16);

        checkToken(lexer.next(), Kind.KW_WHILE, 8, 1);
        checkToken(lexer.next(), Kind.LPAREN, 8, 7);
        checkIdent(lexer.next(), "x", 8, 8);
        checkToken(lexer.next(), Kind.LT, 8, 10);
        checkInt(lexer.next(), 10, 8, 12);
        checkToken(lexer.next(), Kind.RPAREN, 8, 14);

        checkEOF(lexer.next());
    }

    public void testSingleEscapeSequence() throws LexicalException {
        String input ="""
        "\\b"
        "\\t"
        "\\n"
        "\\f"
        "\\r"
        "\\""
        "\\'"
        "\\\\"
        ""
        """;
        show(input);
        ILexer lexer = getLexer(input);
        checkString(lexer.next(), "\b", 1, 1);
        checkString(lexer.next(), "\t", 2, 1);
        checkString(lexer.next(), "\n", 3, 1);
        checkString(lexer.next(), "\f", 4, 1);
        checkString(lexer.next(), "\r", 5, 1);
        checkString(lexer.next(), "\"", 6, 1);
        checkString(lexer.next(), "\'", 7, 1);
        checkString(lexer.next(), "\\", 8, 1);
        checkString(lexer.next(), "", 9, 1);
    }

    @Test
    public void testStringPeekAndNext() throws LexicalException {
        String input = """
				"This is a string"
				123peek
				""";
        show(input);
        ILexer lexer = getLexer(input);
        checkString(lexer.peek(), "This is a string", 1, 1);
        checkString(lexer.peek(), "This is a string", 1, 1);
        checkString(lexer.peek(), "This is a string", 1, 1);
        checkString(lexer.next(), "This is a string", 1, 1);
        checkInt(lexer.next(), 123, 2, 1);
        checkIdent(lexer.next(), "peek", 2, 4);
    }

    @Test
    public void testStringPeekAndNext2() throws LexicalException {
        String input = """
                BEGIN
                CALL
                """;
        show(input);
        ILexer lexer = getLexer(input);
        checkToken(lexer.peek(), KW_BEGIN, 1, 1);
        checkToken(lexer.next(), KW_BEGIN, 1, 1);
        checkToken(lexer.peek(), KW_CALL, 2, 1);
        checkToken(lexer.next(), KW_CALL, 2, 1);
    }
}
