# JPQL Parsing

This directory contains the lexical and syntactic analyzer for parsing JPQL queries.
If you need to modify the grammar (for example, add a new macro), you can do this in the grammar file: [JPA2.g](JPA2.g)

Use the following algorithm to generate new analyzers:
1. Download the ANTLR 3.5.2 generator https://www.antlr3.org/download/antlr-3.5.2-complete.jar to a local directory
2. Modify the grammar in the [JPA2.g](JPA2.g) file
3. Run the following command:
   ```bash
   java -cp /some_path/antlr-3.5.2-complete.jar org.antlr.Tool JPA2.g
   ```
4. Add the following code manually to the [JPA2Parser.java](JPA2Parser.java):
   ```java
    // CAUTION: inserted manually, when regenerating the lexer, do not forget to insert
    @Override
    public void emitErrorMessage(String msg) {
        //do nothing
    }

    @Override
    protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
        throw new MismatchedTokenException(ttype, input);
    }
   ```
5. Add the following code manually to the [JPA2Lexer.java](JPA2Lexer.java):
   ```java
    // CAUTION: inserted manually, when regenerating the lexer, do not forget to insert
    @Override
    public void emitErrorMessage(String msg) {
        throw new JPA2RecognitionException(msg);
    }
   ```
6. Run the tests:
   ```bash
   ./gradlew test
   ```
