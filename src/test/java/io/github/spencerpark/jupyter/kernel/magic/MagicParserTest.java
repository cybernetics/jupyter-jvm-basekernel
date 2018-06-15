package io.github.spencerpark.jupyter.kernel.magic;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class MagicParserTest {
    private MagicParser inlineParser;
    private MagicParser solParser;

    @Before
    public void setUp() throws Exception {
        this.inlineParser = new MagicParser("//%", "//%%");
        this.solParser = new MagicParser("^\\s*//%", "//%%");
    }

    @Test
    public void transformLineMagics() {
        String cell = Stream.of(
                "//%magicName arg1 arg2",
                "Inline magic = //%magicName2 arg1",
                "//Just a comment",
                "//%magicName3 arg1 \"arg2 arg2\""
        ).collect(Collectors.joining("\n"));

        String transformedCell = this.inlineParser.transformLineMagics(cell, ctx ->
                "**" + ctx.getMagicCall().getName() + "-" + ctx.getMagicCall().getArgs().stream().collect(Collectors.joining(","))
        );

        String expectedTransformedCell = Stream.of(
                "**magicName-arg1,arg2",
                "Inline magic = **magicName2-arg1",
                "//Just a comment",
                "**magicName3-arg1,arg2 arg2"
        ).collect(Collectors.joining("\n"));

        assertEquals(expectedTransformedCell, transformedCell);
    }

    @Test
    public void parseCellMagic() {
        String cell = Stream.of(
                "//%%cellMagicName arg1 \"arg2 arg2\" arg3  ",
                "This is the body"
        ).collect(Collectors.joining("\n"));

        CellMagicParseContext ctx = this.inlineParser.parseCellMagic(cell);

        assertEquals("cellMagicName", ctx.getMagicCall().getName());
        assertEquals(Arrays.asList("arg1", "arg2 arg2", "arg3"), ctx.getMagicCall().getArgs());
        assertEquals("This is the body", ctx.getMagicCall().getBody());
        assertEquals("//%%cellMagicName arg1 \"arg2 arg2\" arg3  ", ctx.getRawArgsLine());
        assertEquals(cell, ctx.getRawCell());
    }

    @Test
    public void transformCellMagic() {
        String cell = Stream.of(
                "//%%cellMagicName arg1 \"arg2 arg2\" arg3  ",
                "This is the body"
        ).collect(Collectors.joining("\n"));

        String transformedCell = this.inlineParser.transformCellMagic(cell, ctx ->
                ctx.getMagicCall().getName() + "(" + ctx.getMagicCall().getArgs().stream().collect(Collectors.joining(",")) + ")" + "\n"
                        + ctx.getMagicCall().getBody()
        );

        String expectedTransformedCell = "cellMagicName(arg1,arg2 arg2,arg3)\nThis is the body";

        assertEquals(expectedTransformedCell, transformedCell);
    }

    @Test
    public void dontTransformNonMagicCell() {
        String cell = Stream.of(
                "//%cellMagicName arg1 \"arg2 arg2\" arg3  ",
                "This is the body"
        ).collect(Collectors.joining("\n"));

        String transformedCell = this.inlineParser.transformCellMagic(cell, ctx -> "transformer applied");

        assertEquals(cell, transformedCell);
    }

    @Test
    public void startOfLineParserSkipsInlineMagics() {
        String cell = "System.out.printf(\"Fmt //%s string\", \"test\");";

        String transformedCell = this.solParser.transformLineMagics(cell, ctx -> "");

        assertEquals(cell, transformedCell);
    }

    @Test
    public void startOfLineParserAllowsWhitespace() {
        String cell = Stream.of(
                "//%sol",
                "  //%sol2",
                "\t//%sol3"
        ).collect(Collectors.joining("\n"));

        String transformedCell = this.solParser.transformLineMagics(cell, ctx -> ctx.getMagicCall().getName());
        String expectedTransformedCell = Stream.of(
                "sol",
                "sol2",
                "sol3"
        ).collect(Collectors.joining("\n"));

        assertEquals(expectedTransformedCell, transformedCell);
    }

    @Test
    public void startOfLineParserSkipsInline() {
        String cell = Stream.of(
                "//%sol",
                "Not //%sol"
        ).collect(Collectors.joining("\n"));

        String transformedCell = this.solParser.transformLineMagics(cell, ctx -> ctx.getMagicCall().getName());
        String expectedTransformedCell = Stream.of(
                "sol",
                "Not //%sol"
        ).collect(Collectors.joining("\n"));

        assertEquals(expectedTransformedCell, transformedCell);
    }
}