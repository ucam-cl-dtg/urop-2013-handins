package uk.ac.cam.sup.helpers;

import com.google.common.collect.ImmutableMap;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class Util {
    public static HttpServletRequest getRequest() {
        return ResteasyProviderFactory.getContextData(HttpServletRequest.class);
    }

    public static HttpServletResponse getResponse() {
        return ResteasyProviderFactory.getContextData(HttpServletResponse.class);
    }

    public static Object forward(HttpServletRequest request, String path) {
        try {
            request.getRequestDispatcher(path).forward(request, getResponse());
        } catch (ServletException e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(ImmutableMap.of("message", "There was an error with the server"))
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(ImmutableMap.of("message", "There was an error with the server"))
                    .build();
        }
        return null;
    }
}
