package cc.sportsdb.common.test;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractMockController<T> {

    protected MockMvc mvc;

    @Autowired
    protected T controller;

    @Value("${local.server.port}")
    protected String serverPort;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    protected final String getHost() {
        return "http://localhost:" + serverPort;
    }

    public final MvcResult mockPost(String uri) throws Exception {
        return mockPost(uri, null);
    }

    public final MvcResult mockPost(String uri, Map<String, String> params) throws Exception {
        return mock(uri, HttpMethod.POST, params);
    }

    public final MvcResult mockGet(String uri) throws Exception {
        return mockGet(uri, null);
    }

    public final MvcResult mockGet(String uri, Map<String, String> params) throws Exception {
        return mock(uri, HttpMethod.GET, params);
    }

    public final MvcResult mockBodyGet(String uri, String content) throws Exception {
        return mockBody(uri, HttpMethod.GET, content);
    }

    public final MvcResult mockBodyPost(String uri, String content) throws Exception {
        return mockBody(uri, HttpMethod.POST, content);
    }

    public final MvcResult mockBody(String uri, HttpMethod method, String content) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(method, getHost() + uri);

        if (content != null && !content.isEmpty()) {
            builder.contentType(MediaType.APPLICATION_JSON).content(content);
        }

        builder.accept(MediaType.APPLICATION_JSON);
        return mvc.perform(builder).andReturn();
    }

    public final MvcResult mock(String uri, HttpMethod method, Map<String, String> params) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(method, getHost() + uri);

        if (params != null && params.size() > 0) {
            params.forEach((k, v) -> {
                builder.param(k, v);
            });
        }

        builder.accept(MediaType.APPLICATION_JSON);
        return mvc.perform(builder).andReturn();
    }
}
