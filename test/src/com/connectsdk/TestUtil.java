package com.connectsdk;

import com.connectsdk.core.Util;

import org.apache.tools.ant.filters.StringInputStream;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by oleksii.frolov on 1/30/2015.
 */
public final class TestUtil {

    public static URL getMockUrl(final String content, String applicationUrl) throws IOException {
        final URLConnection mockConnection = Mockito.mock(URLConnection.class);
        Mockito.when(mockConnection.getInputStream()).thenReturn(new StringInputStream(content));
        Mockito.when(mockConnection.getHeaderField("Application-URL")).thenReturn(applicationUrl);

        final URLStreamHandler handler = new URLStreamHandler() {

            @Override
            protected URLConnection openConnection(final URL arg0)
                    throws IOException {
                return mockConnection;
            }
        };
        final URL url = new URL("http", "hostname", 80, "", handler);
        return url;
    }

    public static void runUtilBackgroundTasks() {
        ExecutorService executor = (ExecutorService) Util.getExecutor();
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
