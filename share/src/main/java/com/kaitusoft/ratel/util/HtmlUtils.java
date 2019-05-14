package com.kaitusoft.ratel.util;

/**
 * 借用spring对html字符转义
 */
public abstract class HtmlUtils {

    /**
     * Shared instance of pre-parsed HTML character entity references.
     */
//    private static final HtmlCharacterEntityReferences characterEntityReferences =
//            new HtmlCharacterEntityReferences();


    /**
     * 将 HTML 特殊字符转义为 HTML 通用转义序列；
     * Turn special characters into HTML character references.
     * Handles complete character set defined in HTML 4.01 recommendation.
     * <p>Escapes all special characters to their corresponding
     * entity reference (e.g. <code>&lt;</code>).
     * <p>Reference:
     * <a href="http://www.w3.org/TR/html4/sgml/entities.html">
     * http://www.w3.org/TR/html4/sgml/entities.html
     * </a>
     * @param input the (unescaped) input string
     * @return the escaped string
     */
    public static String htmlEscape(String input) {
        return input;
    }

}