package com.kskb.se.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.kskb.se.http.HttpMethod.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class TestHttpServer implements HttpErrorHandler {
   private static final long TIMEOUT = 3000L;

   record ClientResponse(byte[] buf, int size) {};

   private final ServerSocket socket = Mockito.mock();
   private final Socket clientSocket = Mockito.mock();
   private final OutputStream clientOutputStream = Mockito.mock();
   private final InputStream clientInputStream = Mockito.mock();
   private final byte[] clientResponseBuffer = new byte[4096];
   private int endpointCounter = 0;
   private List<ClientResponse> clientResponseBuffers = new ArrayList<>();

   private HttpServerImpl server;
   private final SessionManagerMock sessionManager = new SessionManagerMock();

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
      when(clientInputStream.available())
         .thenReturn(1);
      doAnswer(invocation -> {
         final byte[] buf = invocation.getArgument(0);
         final int offset = invocation.getArgument(1);
         final int length = invocation.getArgument(2);
         final int size = Math.min(clientResponseBuffer.length, offset + length);
         if (size - offset >= 0)
            System.arraycopy(buf, offset, clientResponseBuffer, clientResponseBufferSize + offset, size - offset);
         clientResponseBufferSize += size;
         return null;
      }).when(clientOutputStream)
         .write(any(), anyInt(), anyInt());
      doAnswer(invocation -> {
         clientResponseBuffers.add(new ClientResponse(clientResponseBuffer.clone(), clientResponseBufferSize));
         clientResponseBufferSize = 0;
         return null;
      }).when(clientOutputStream)
         .close();
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
      server = createHttpServer();
      sendClientRequests("""
         GET / HTTP/1.1
         
         """);
      server.add(GET, "/", (context) -> assertEndpointContext(context, GET, "/"));
      server.start();
      server.stop();
      assertResponse();
   }

   @Test(timeout = TIMEOUT)
   public void testPostRequest() throws Exception {
      server = createHttpServer();
      sendClientRequests("""
         POST / HTTP/1.1
         
         """);
      server.add(POST, "/", (context) -> assertEndpointContext(context, POST, "/"));
      server.start();
      server.stop();
      assertResponse();
   }

   @Test(timeout = TIMEOUT)
   public void testPutRequest() throws Exception {
      server = createHttpServer();
      sendClientRequests("""
         PUT / HTTP/1.1
         
         """);
      server.add(PUT, "/", (context) -> assertEndpointContext(context, PUT, "/"));
      server.start();
      server.stop();
      assertResponse();
   }

   @Test(timeout = TIMEOUT)
   public void testDeleteRequest() throws Exception {
      server = createHttpServer();
      sendClientRequests("""
         DELETE / HTTP/1.1
         
         """);
      server.add(DELETE, "/", (context) -> assertEndpointContext(context, DELETE, "/"));
      server.start();
      server.stop();
      assertResponse();
   }

   @Test(timeout = TIMEOUT)
   public void testSessionRequest() throws Exception {
      server = createHttpServer();
      sendClientRequests(
         // First request
         """
         GET /login HTTP/1.1
         
         """,
         // Second request
         """
         GET / HTTP/1.1
         Cookie: session={{ session_id }}
         
         """.replace("{{ session_id }}", SessionManagerMock.FIRST_SESSION_ID.toString())
      );
      server.add(GET, "/login", (context) -> {
         assertEndpointContext(context, GET, "/login");
         context.session().dataset().put("key", "value");
      });
      server.add(GET, "/", (context) -> {
         assertEndpointContext(context, GET, "/");
         assertEquals(context.session().dataset().get("key"), "value");
      });
      server.start();
      server.stop();
      assertResponse();
   }

   @Test(timeout = TIMEOUT)
   public void testExpiredSessionRequest() throws Exception {
      sessionManager.setCreationTime(0);
      server = createHttpServer();
      sendClientRequests(
         // First request
         """
         GET /login HTTP/1.1
         
         """,
         // Second request
         """
         GET / HTTP/1.1
         Cookie: session={{ session_id }}
         
         """.replace("{{ session_id }}", SessionManagerMock.FIRST_SESSION_ID.toString())
      );
      server.add(GET, "/login", (context) -> {
         assertEndpointContext(context, GET, "/login");
         context.session().dataset().put("key", "value");
      });
      server.add(GET, "/", (context) -> {
         assertEndpointContext(context, GET, "/");
         assertNull(context.session().dataset().get("key"));
      });
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
      for (final var rawResponse: clientResponseBuffers) {
         final String response = new String(rawResponse.buf, 0, rawResponse.size, StandardCharsets.US_ASCII);
         System.out.println("Server Response:");
         for (final var line: response.split("\r\n")) {
            System.out.println("   " + line);
         }
         assertEquals(response.substring(0, 17), "HTTP/1.1 200 OK\r\n");
         assertTrue(response.contains("Server: Demo Server\r\n"));
         assertFalse(findResponseHeader(response, "Date").isEmpty());
         assertTrue(response.contains("Content-Length: 0\r\n"));
      }
   }

   private void sendClientRequests(String ... clientRequests) throws IOException {
      final List<byte[]> requests = new ArrayList<>();
      for (String clientRequest: clientRequests) {
         final String clientRequestTransformed = clientRequest
            .replaceAll("\n", "\r\n");
         System.out.println("Sending request:");
         for (final var line: clientRequestTransformed.split("\r\n"))
            System.out.printf("   %.4096s%n", line);
         final byte[] request = clientRequestTransformed
            .getBytes(StandardCharsets.US_ASCII);
         requests.add(request);
      }
      expectedEndpointCount = clientRequests.length;

      final AtomicInteger counter = new AtomicInteger(0);
      when(server.isRunning())
         .thenAnswer((Answer<Boolean>) i -> counter.getAndIncrement() < clientRequests.length);

      final Iterator<byte[]> it = requests.iterator();
      final AtomicReference<byte[]> requestRef = new AtomicReference<>(it.next());
      when(clientInputStream.read(any(), anyInt(), anyInt()))
         .thenAnswer(invocationOnMock -> {
            byte[] request = requestRef.get();
            if (request != null) {
               final byte[] buf = invocationOnMock.getArgument(0);
               final int offset = invocationOnMock.getArgument(1);
               final int length = invocationOnMock.getArgument(2);
               final int size = Math.min(request.length, offset + length);
               if (size - offset >= 0)
                  System.arraycopy(request, offset, buf, offset, size - offset);
               if (size == request.length) {
                  requestRef.set(it.hasNext() ? it.next() : null);
               }
               return size - offset;
            }
            else {
               return -1;
            }
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
         .withSessionManager(sessionManager)
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
