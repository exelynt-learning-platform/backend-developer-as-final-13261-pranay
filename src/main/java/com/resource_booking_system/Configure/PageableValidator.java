package com.resource_booking_system.Configure;

import com.resource_booking_system.Exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PageableValidator {

    private final int maxPageSize;

    public PageableValidator(@Value("${app.pagination.max-page-size}") int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public Pageable validateAndSanitize(Pageable pageable, Set<String> allowedSorts) {

        validateSort(pageable, allowedSorts);
        validatePageSize(pageable);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    }

    private void validateSort(Pageable pageable, Set<String> allowedSorts) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedSorts.contains(order.getProperty())) {
                throw new BadRequestException("Invalid sort field: " + order.getProperty());
            }
        }
    }

    private void validatePageSize(Pageable pageable) {
        if (pageable.getPageSize() > maxPageSize) {
            throw new BadRequestException("Page size must not exceed " + maxPageSize);
        }
    }
}