package com.autumn.web.filter;

import com.autumn.web.HttpRequest;

public interface Filter {
    boolean doFilter(HttpRequest request);
}
