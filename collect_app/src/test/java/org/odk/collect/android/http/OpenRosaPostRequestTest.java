package org.odk.collect.android.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertThat;

public abstract class OpenRosaPostRequestTest {

    protected abstract OpenRosaHttpInterface buildSubject();

    private final MockWebServer mockWebServer = new MockWebServer();
    private OpenRosaHttpInterface subject;

    @Before
    public void setup() throws Exception {
        mockWebServer.start();
        subject = buildSubject();
    }

    @After
    public void teardown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void makesAPostRequestToUri() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        URI uri = mockWebServer.url("/blah").uri();
        subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);

        assertThat(mockWebServer.getRequestCount(), equalTo(1));

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod(), equalTo("POST"));
        assertThat(request.getRequestUrl().uri(), equalTo(uri));
    }

    @Test
    public void withCredentials_whenHttp_doesNotRetryWithCredentials() throws Exception  {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
                .setBody("Please authenticate."));
        mockWebServer.enqueue(new MockResponse());

        URI uri = mockWebServer.url("").uri();
        subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, new HttpCredentials("user", "pass"), 0);

        assertThat(mockWebServer.getRequestCount(), equalTo(1));
    }

    @Test
    public void whenLastRequestSetCookies_nextRequestDoesNotSendThem() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Set-Cookie", "blah=blah"));
        mockWebServer.enqueue(new MockResponse());

        URI uri = mockWebServer.url("").uri();
        subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);
        subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);

        mockWebServer.takeRequest();
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Cookie"), isEmptyOrNullString());
    }
}
