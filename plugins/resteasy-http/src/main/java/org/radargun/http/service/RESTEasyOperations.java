package org.radargun.http.service;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.radargun.logging.Log;
import org.radargun.logging.LogFactory;
import org.radargun.traits.RESTOperations;

/**
 *
 * @author Martin Gencur
 */
public class RESTEasyOperations implements RESTOperations {
   private static final Log log = LogFactory.getLog(RESTEasyOperations.class);
   private final RESTEasyService service;

   public RESTEasyOperations(RESTEasyService service) {
      this.service = service;
   }

   public RESTOperationInvoker getRESTInvoker(String contextPath) {
      if (service.isRunning()) {
         return new RESTOperationInvokerImpl(contextPath);
      }
      return null;
   }

   protected class RESTOperationInvokerImpl implements RESTOperationInvoker {

      private String uri;

      public RESTOperationInvokerImpl(String contextPath) {
         this.uri = buildApplicationUrl(contextPath);
      }

      private String buildApplicationUrl(String contextPath) {
         InetSocketAddress node = pickServer();
         StringBuilder s = new StringBuilder("http://");
         if (service.getUsername() != null) {
            try {
               s.append(URLEncoder.encode(service.getUsername(), "UTF-8")).append(":")
                  .append(URLEncoder.encode(service.getPassword(), "UTF-8")).append("@");
            } catch (UnsupportedEncodingException e) {
               throw new RuntimeException("Could not encode the supplied username and password", e);
            }
         }
         s.append(node.getHostName()).append(":").append(node.getPort()).append("/");
         s.append(contextPath);
         log.info("buildApplicationUrl = " + s.toString());
         return s.toString();
      }

      /* There's one server picked for each thread at the beginning. Subsequent requests from this
      thread go to the same server. */
      private InetSocketAddress pickServer() {
         return service.getServers().get(service.getServersLoadBalance().next(new Random()));
      }

      @Override
      public Response get(List<Cookie> cookiesToPass, MultivaluedMap<String, Object> headersToPass) {
         Response response = null;
         if (service.isRunning()) {

            try {
               Invocation.Builder requestBuilder = service.getHttpClient().target(uri).request();
               for (Cookie cookie : cookiesToPass) {
                  requestBuilder.cookie(cookie);
               }
               Invocation get = requestBuilder.accept(service.getContentType())
                  .buildGet();
               response = get.invoke();
               if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
                  log.warn("The requested URI does not exist");
               }
            } catch (Exception e) {
               throw new RuntimeException("RESTEasyOperations::get request threw exception: " + uri, e);
            } finally {
               if (response != null) {
                  response.close();
               }
            }
         }
         return response;
      }
   }
}
