package com.kaitusoft.ratel;

import org.junit.Test;

public class UtilTest {

    @Test
    public void replace(){
        String regxp = "\\$1";
        String source = "/xy/z/$1";
        System.out.println(source.replaceAll(regxp, "1213"));
    }
}