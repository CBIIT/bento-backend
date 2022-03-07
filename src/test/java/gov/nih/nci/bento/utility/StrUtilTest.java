package gov.nih.nci.bento.utility;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
public class StrUtilTest {

    @Test
    public void getBoolText_Test() {
        assertThat(StrUtil.getBoolFromText(null), is(""));
        assertThat(StrUtil.getBoolFromText("TESTTESTTEST true TESTTESTTEST"), is("true"));
        assertThat(StrUtil.getBoolFromText("TEST_FALSE_TEST"), is(""));
        assertThat(StrUtil.getBoolFromText("TRUEFALSETESTTEST"), is(""));
        assertThat(StrUtil.getBoolFromText("TESTTESTTESTTESTTESTTEST true"), is("true"));
        assertThat(StrUtil.getBoolFromText("true"), is("true"));
        assertThat(StrUtil.getBoolFromText("false"), is("false"));
        assertThat(StrUtil.getBoolFromText(" false "), is("false"));
        assertThat(StrUtil.getBoolFromText(" FALse "), is("false"));
        assertThat(StrUtil.getBoolFromText(" tRue "), is("true"));
    }

}