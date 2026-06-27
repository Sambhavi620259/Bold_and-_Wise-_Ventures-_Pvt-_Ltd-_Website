package in.bawvpl.Authify.filter;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 30;
    private static final long WINDOW = 60000;

    private final Map<String,Integer> requests = new ConcurrentHashMap<>();
    private final Map<String,Long> timestamps = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){

        String path=request.getRequestURI();

        return !(path.startsWith("/api/v1.0/login")
                || path.startsWith("/api/v1.0/forgot-password")
                || path.startsWith("/api/v1.0/reset-password"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String ip=request.getRemoteAddr();

        long now=System.currentTimeMillis();

        timestamps.putIfAbsent(ip,now);
        requests.putIfAbsent(ip,0);

        if(now-timestamps.get(ip)>WINDOW){

            timestamps.put(ip,now);
            requests.put(ip,0);
        }

        int count=requests.get(ip)+1;

        requests.put(ip,count);

        if(count>MAX_REQUESTS){

            response.setStatus(429);

            response.setContentType("application/json");

            response.getWriter().write("""
            {
              "success":false,
              "message":"Too many requests"
            }
            """);

            return;
        }

        chain.doFilter(request,response);
    }
}
