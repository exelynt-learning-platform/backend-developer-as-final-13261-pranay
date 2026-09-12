package com.resource_booking_system.Configure;

import com.resource_booking_system.Exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageableValidatorTest {

    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> ALLOWED_FIELDS = Set.of("id", "name", "createdAt");

    private PageableValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PageableValidator(MAX_PAGE_SIZE);
    }

    @Test
    void validateAndSanitize_whenValidInput_returnsPageable() {

        Pageable input = PageRequest.of(0, 10, Sort.by("name").ascending());

        Pageable result = validator.validateAndSanitize(input, ALLOWED_FIELDS);

        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals("name", result.getSort().iterator().next().getProperty());
    }

    @Test
    void validateAndSanitize_whenInvalidSortField_throwsBadRequest() {

        Pageable input = PageRequest.of(0, 10, Sort.by("user.password").ascending());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> validator.validateAndSanitize(input, ALLOWED_FIELDS));

        assertEquals("Invalid sort field: user.password", ex.getMessage());
    }

    @Test
    void validateAndSanitize_whenPageSizeExceedsMax_throwsBadRequest() {

        Pageable input = PageRequest.of(0, 999);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> validator.validateAndSanitize(input, ALLOWED_FIELDS));

        assertEquals("Page size must not exceed " + MAX_PAGE_SIZE, ex.getMessage());
    }

    @Test
    void validateAndSanitize_whenUnsorted_passes() {

        Pageable input = PageRequest.of(2, 20);

        Pageable result = validator.validateAndSanitize(input, ALLOWED_FIELDS);

        assertNotNull(result);
        assertEquals(2, result.getPageNumber());
        assertEquals(20, result.getPageSize());
        assertEquals(Sort.unsorted(), result.getSort());
    }

    @Test
    void validateAndSanitize_whenEmptySortWhitelistAndNoSort_passes() {

        Pageable input = PageRequest.of(0, 5);

        Pageable result = validator.validateAndSanitize(input, Set.of());

        assertNotNull(result);
        assertEquals(5, result.getPageSize());
    }
}