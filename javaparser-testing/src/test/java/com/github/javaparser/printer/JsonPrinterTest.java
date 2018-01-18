package com.github.javaparser.printer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonPrinterTest {
    @Test
    public void testWithType() {
        JsonPrinter jsonPrinter = new JsonPrinter(true);
        Expression expression = JavaParser.parseExpression("x(1,1)");

        String output = jsonPrinter.output(expression);

        assertEquals("{\"type\":\"MethodCallExpr\",\"name\":{\"type\":\"SimpleName\",\"identifier\":\"x\"},\"arguments\":[{\"type\":\"IntegerLiteralExpr\",\"value\":\"1\"},{\"type\":\"IntegerLiteralExpr\",\"value\":\"1\"}]}", output);
    }

    @Test
    public void testWithoutType() {
        JsonPrinter jsonPrinter = new JsonPrinter(false);
        Expression expression = JavaParser.parseExpression("1+1");

        String output = jsonPrinter.output(expression);

        assertEquals("{\"operator\":\"PLUS\",\"left\":{\"value\":\"1\"},\"right\":{\"value\":\"1\"}}", output);
    }
    
    @Test
    public void issue1338() {
        String code = "class Test {" +
                "  public void method() {" +
                "    String.format(\"I'm using %s\", \"JavaParser\");" +
                "  }" +
                "}";
        CompilationUnit unit = JavaParser.parse(code);
        JsonPrinter printer = new JsonPrinter(true);
        printer.output(unit);
    }
}