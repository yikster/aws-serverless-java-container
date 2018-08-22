package com.amazonaws.serverless.sample.springboot.controller;


import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.servlet.AwsServletContext;
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringLambdaContainerHandler;
import com.amazonaws.serverless.sample.springboot.Application;
import com.amazonaws.serverless.sample.springboot.model.Pet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ApplicationTest {
    private static final String CUSTOM_HEADER_KEY = "x-custom-header";
    private static final String CUSTOM_HEADER_VALUE = "my-custom-value";
    private static final String AUTHORIZER_PRINCIPAL_ID = "test-principal-" + UUID.randomUUID().toString();

    /*
    @Autowired
    private ObjectMapper objectMapper;
*/
    @Autowired
    private MockLambdaContext lambdaContext;

    private JsonParser jsonParser ;

    private SpringLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    @Before
    public void clearServletContextCache() throws ContainerInitializationException {
        AwsServletContext.clearServletContextCache();
        handler = SpringLambdaContainerHandler.getAwsProxyHandler(Application.class);

        jsonParser = new BasicJsonParser();
    }

    @Test
    public void controllerAdvice_invalidPath_returnAdvice() {

        AwsProxyResponse output = requestAwsProxy("/echo2", "GET");
        assertNotNull(output);
        assertEquals(404, output.getStatusCode());
    }
    @Test
    public void postPets() {
        AwsProxyResponse output = requestAwsProxy("/pets", "POST");
        assertNotNull(output);
        assertEquals(502, output.getStatusCode());

    }


    @Test
    public void getPets() {
        AwsProxyResponse output = requestAwsProxy("/pets", "GET");
        assertNotNull(output);
        assertEquals(200, output.getStatusCode());

        assertNotNull(output.getBody());
        List<Object> result = jsonParser.parseList(output.getBody());
        assertTrue(0 < result.size());

        int TEST_LIMITS = 50;
        output = requestAwsProxy("/pets", "GET", "limit", "" + TEST_LIMITS);
        assertNotNull(output);
        assertEquals(200, output.getStatusCode());

        assertNotNull(output.getBody());
        result = jsonParser.parseList(output.getBody());
        assertTrue(TEST_LIMITS == result.size());


    }
    private AwsProxyResponse requestAwsProxy(String s, String post) {
        return handler.proxy(getAwsProxyRequest(s, post), lambdaContext);
    }

    private AwsProxyResponse requestAwsProxy(String s, String post, String queryKey, String queryValue) {
        return handler.proxy(getAwsProxyRequest(s, post, queryKey, queryValue), lambdaContext);
    }

    private AwsProxyRequest getAwsProxyRequest(String path, String method, String queryKey, String queryValue) {
        AwsProxyRequest request = new AwsProxyRequestBuilder(path, method)
                .json()
                .queryString(queryKey, queryValue)
                .header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
                .build();
        return request;

    }
    private AwsProxyRequest getAwsProxyRequest(String path, String method) {
        AwsProxyRequest request = new AwsProxyRequestBuilder(path, method)
                .json()
                .header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
                .build();
        return request;

    }

}
