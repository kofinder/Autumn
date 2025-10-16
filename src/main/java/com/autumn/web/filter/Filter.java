package com.autumn.web.filter;

import java.net.http.HttpRequest;

public interface Filter {
    boolean doFilter(HttpRequest request);
}
