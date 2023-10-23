package com.kskb.se.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static com.kskb.se.http.HttpMethod.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestHttpServer implements HttpErrorHandler {
   private static final long TIMEOUT = 3000L;

   private final ServerSocket socket = Mockito.mock();
   private final Socket clientSocket = Mockito.mock();
   private final OutputStream clientOutputStream = Mockito.mock();
   private final InputStream clientInputStream = Mockito.mock();
   private final byte[] clientResponseBuffer = new byte[4096];
   private int endpointCounter = 0;

   private HttpServerImpl server;

   private Throwable exception;
   private int expectedEndpointCount = 1;
   private int clientResponseBufferSize = 0;

   @Before
   public void setup() throws IOException {
      when(socket.accept())
         .thenReturn(clientSocket);
      when(clientSocket.getOutputStream())
         .thenReturn(clientOutputStream);
      when(clientSocket.getInputStream())
         .thenReturn(clientInputStream);
      doAnswer(invocation -> {
         final byte[] buf = invocation.getArgument(0);
         final int offset = invocation.getArgument(1);
         final int length = invocation.getArgument(2);
         final int size = Math.min(clientResponseBuffer.length, offset + length);
         if (size - offset >= 0)
            System.arraycopy(buf, offset, clientResponseBuffer, offset, size - offset);
         clientResponseBufferSize = size;
         return null;
      }).when(clientOutputStream)
         .write(any(), anyInt(), anyInt());
   }

   @After
   public void tearDown() {
      assertNotNull(server);
      verify(server, atLeastOnce())
         .isRunning();
      if (exception != null) {
         exception.printStackTrace(System.err);
         throw new AssertionError(exception.getMessage());
      }
   }

   @Test(timeout = TIMEOUT)
   public void testSimple() throws Exception {
      server = createHttpServer();
      server.start();
      server.stop();
   }

   @Test(timeout = TIMEOUT)
   public void testGetRequest() throws Exception {
      sendClientRequest("""
         GET / HTTP/1.1
         
         """);
      server = createHttpServer();
      server.add(GET, "/", (context) -> assertEndpointContext(context, GET, "/"));
      server.start();
      server.stop();
      assertResponse();
   }

   @Test(timeout = TIMEOUT)
   public void testPostRequest() throws Exception {
      sendClientRequest("""
         POST / HTTP/1.1
         
         """);
      server = createHttpServer();
      server.add(POST, "/", (context) -> assertEndpointContext(context, POST, "/"));
      server.start();
      server.stop();
      assertResponse();
   }

   @Test(timeout = TIMEOUT)
   public void testPutRequest() throws Exception {
      sendClientRequest("""
         PUT / HTTP/1.1
         
         """);
      server = createHttpServer();
      server.add(PUT, "/", (context) -> assertEndpointContext(context, PUT, "/"));
      server.start();
      server.stop();
      assertResponse();
   }

   @Test(timeout = TIMEOUT)
   public void testDeleteRequest() throws Exception {
      sendClientRequest("""
         DELETE / HTTP/1.1
         
         """);
      server = createHttpServer();
      server.add(DELETE, "/", (context) -> assertEndpointContext(context, DELETE, "/"));
      server.start();
      server.stop();
      assertResponse();
   }

   @Override
   public void onException(HttpErrorHandlerContext context, Throwable t) {
      exception = t;
   }

   private void assertResponse() {
      assertEquals(endpointCounter, expectedEndpointCount);
      final String response = new String(clientResponseBuffer, 0, clientResponseBufferSize, StandardCharsets.US_ASCII);
      assertEquals(response.substring(0, 17), "HTTP/1.1 200 OK\r\n");
      assertTrue(response.contains("Server: Demo Server\r\n"));
      assertFalse(findResponseHeader(response, "Date").isEmpty());
      assertTrue(response.contains("Content-Length: 0\r\n"));
   }

   private void sendClientRequest(String clientRequest) throws IOException {
      final String clientRequestTransformed = clientRequest
         .replaceAll("\n", "\r\n");
      final byte[] request = clientRequestTransformed
         .getBytes(StandardCharsets.US_ASCII);
      when(clientInputStream.read(any(), anyInt(), anyInt()))
         .thenAnswer(invocationOnMock -> {
            final byte[] buf = invocationOnMock.getArgument(0);
            final int offset = invocationOnMock.getArgument(1);
            final int length = invocationOnMock.getArgument(2);
            final int size = Math.min(request.length, offset + length);
            if (size - offset >= 0)
               System.arraycopy(request, offset, buf, offset, size - offset);
            return size - offset;
         });
   }

   private String findResponseHeader(String response, String header) {
      final int size = (header + ": ").length();
      final int index = response.indexOf(header + ": ");
      assertTrue(index >= 0);
      final int offset = response.substring(index).indexOf("\r\n");
      assertTrue(offset >= 0);
      final String headerValue = response.substring(index + size, index + offset - size);
      return headerValue;
   }

   private HttpServerImpl createHttpServer() {
      HttpServerContext context = HttpServerContext.builder()
         .addErrorHandler(this)
         .build();
      return createHttpServer(context);
   }

   private void assertEndpointContext(HttpEndPointContext context, HttpMethod method, String path) {
      assertEquals(context.method(), method);
      assertEquals(context.path(), path);
      endpointCounter++;
   }

   private HttpServerImpl createHttpServer(HttpServerContext context) {
      final HttpServerImpl server = context != null ?
         Mockito.spy((HttpServerImpl) HttpFactory.createServer(context)) :
         Mockito.spy((HttpServerImpl) HttpFactory.createServer());
      try {
         final Field field = HttpServerImpl.class.getDeclaredField("serverSocket");
         field.setAccessible(true);
         field.set(server, socket);

         final Method method = HttpServerImpl.class.getDeclaredMethod("run");
         method.setAccessible(true);
         doAnswer(invocationOnMock -> {
            method.invoke(server);
            return null;
         }).when(server).start();

         when(server.isRunning())
            .thenReturn(true, false);
      }
      catch (Exception e) {
         throw new AssertionError(e);
      }

      return server;
   }
}
