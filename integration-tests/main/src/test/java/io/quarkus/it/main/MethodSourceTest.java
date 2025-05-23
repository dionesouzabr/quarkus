package io.quarkus.it.main;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.quarkus.it.arc.UnusedBean;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MethodSourceTest {

    @Inject
    UnusedBean unusedBean;

    @ParameterizedTest
    @MethodSource("provideDummyInput")
    public void testParameterResolver(UnusedBean.DummyInput dummyInput, Matcher<String> matcher) {
        UnusedBean.DummyResult dummyResult = unusedBean.dummy(dummyInput);
        assertThat(dummyResult.getResult(), matcher);

        // Can we get config?
        ConfigProvider.getConfig();
    }

    private static Collection<Arguments> provideDummyInput() {
        return List.of(
                Arguments.of(
                        new UnusedBean.DummyInput("whatever",
                                new UnusedBean.NestedDummyInput(new ArrayList<>(List.of(1, 2, 3)))),
                        CoreMatchers.is("whatever/6")),
                Arguments.of(
                        new UnusedBean.DummyInput("hi", new UnusedBean.NestedDummyInput(new ArrayList<>())),
                        CoreMatchers.is("hi/0")));
    }

}
