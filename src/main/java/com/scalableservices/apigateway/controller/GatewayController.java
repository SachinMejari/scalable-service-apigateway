package com.scalableservices.apigateway.controller;

import com.scalableservices.apigateway.service.JwtService;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Enumeration;

@Controller
public class GatewayController {
    private final JwtService jwtService;
    private final WebClient webClient;

    public GatewayController(JwtService jwtService, WebClient.Builder webClientBuilder) {
        this.jwtService = jwtService;
        this.webClient = webClientBuilder.build();
    }

    @RequestMapping(value = "/api/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> routeRequest(HttpServletRequest request,
                                          @RequestBody(required = false) String body) {
        try {
            // Extract original request details
            String path = request.getRequestURI();
            String method = request.getMethod();
            HttpMethod httpMethod = HttpMethod.valueOf(method);

            // Extract token and retrieve role
            String authHeader = request.getHeader("Authorization");
            String role; // Default role
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                role = jwtService.extractClaims(jwt).get("role", String.class);
            } else {
                role = "ANONYMOUS";
            }

            // Forward the request to the target service
            String targetServiceUrl = getTargetServiceUrl(path); // Define your service routing logic
            if (targetServiceUrl == null) {
                return ResponseEntity.notFound().build();
            }

            if(targetServiceUrl.contains("restaurant-gateway") && role.equalsIgnoreCase("customer")) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            // Build the request
            WebClient.RequestBodySpec requestSpec = webClient.method(httpMethod)
                    .uri(URI.create(targetServiceUrl))
                    .headers(headers -> {
                        // Copy all headers from the original request
                        Enumeration<String> headerNames = request.getHeaderNames();
                        while (headerNames.hasMoreElements()) {
                            String headerName = headerNames.nextElement();
                            headers.add(headerName, request.getHeader(headerName));
                        }
                        // Add X-UserType header
                        headers.add("X-UserType", role);
                    });

            if (body != null) {
                requestSpec.bodyValue(body);
            }

            return requestSpec
                    .retrieve()
                    .toEntity(String.class)
                    .block();
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Error occurred: " + ex.getMessage());
        }
    }

    private String getTargetServiceUrl(String path) {
        // Define routing logic based on path
        if (path.startsWith("/api/order-gateway")) {
            return "http://15.206.124.77:8080" + path.replace("/api/order-gateway", "");
        } else if (path.startsWith("/api/restaurant-gateway")) {
            return "http://35.154.176.161:8080" + path.replace("/api/restaurant-gateway", "");
        } else if (path.startsWith("/api/delivery-gateway")) {
            return "http://65.2.129.84:8080" + path.replace("/api/delivery-gateway", "");
        }
        return null;
    }
}
