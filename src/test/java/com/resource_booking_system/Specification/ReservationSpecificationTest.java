package com.resource_booking_system.Specification;

import com.resource_booking_system.Entity.Reservation;
import com.resource_booking_system.Entity.ReservationStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationSpecificationTest {

    @Mock
    private Root<Reservation> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> userPath;

    @Mock
    private Path<Object> userIdPath;

    @Mock
    private Path<Object> statusPath;

    @SuppressWarnings("rawtypes")
    @Mock
    private Path pricePath;

    @Mock
    private Predicate predicate;

    @BeforeEach
    void setUp() {

        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(cb.equal(any(), any())).thenReturn(predicate);
        lenient().when(cb.greaterThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(predicate);
        lenient().when(cb.lessThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(predicate);
    }

    @Test
    void filterReservations_WithAllParameters_ShouldBuildAllPredicates() {

        when(root.get("user")).thenReturn(userPath);
        when(userPath.get("id")).thenReturn(userIdPath);
        when(root.get("status")).thenReturn(statusPath);
        when(root.get("price")).thenReturn(pricePath);

        Specification<Reservation> spec = ReservationSpecification.filterReservations(1L, ReservationStatus.CONFIRMED, new BigDecimal("100.00"), new BigDecimal("1000.00"));

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(userIdPath, 1L);
        verify(cb).equal(statusPath, ReservationStatus.CONFIRMED);
        verify(cb).greaterThanOrEqualTo(pricePath, new BigDecimal("100.00"));
        verify(cb).lessThanOrEqualTo(pricePath, new BigDecimal("1000.00"));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void filterReservations_WithNullUserId_ShouldNotAddUserPredicate() {

        when(root.get("status")).thenReturn(statusPath);

        Specification<Reservation> spec = ReservationSpecification.filterReservations(null, ReservationStatus.PENDING, null, null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(statusPath, ReservationStatus.PENDING);
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void filterReservations_WithNullStatus_ShouldNotAddStatusPredicate() {

        when(root.get("user")).thenReturn(userPath);
        when(userPath.get("id")).thenReturn(userIdPath);
        when(root.get("price")).thenReturn(pricePath);

        Specification<Reservation> spec = ReservationSpecification.filterReservations(5L, null, new BigDecimal("50.00"), null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(userIdPath, 5L);
        verify(cb).greaterThanOrEqualTo(pricePath, new BigDecimal("50.00"));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void filterReservations_WithNullMinPrice_ShouldNotAddMinPredicate() {

        when(root.get("user")).thenReturn(userPath);
        when(userPath.get("id")).thenReturn(userIdPath);
        when(root.get("status")).thenReturn(statusPath);
        when(root.get("price")).thenReturn(pricePath);

        Specification<Reservation> spec = ReservationSpecification.filterReservations(2L, ReservationStatus.CANCELLED, null, new BigDecimal("500.00"));

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(userIdPath, 2L);
        verify(cb).equal(statusPath, ReservationStatus.CANCELLED);
        verify(cb).lessThanOrEqualTo(pricePath, new BigDecimal("500.00"));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void filterReservations_WithNullMaxPrice_ShouldNotAddMaxPredicate() {

        when(root.get("user")).thenReturn(userPath);
        when(userPath.get("id")).thenReturn(userIdPath);
        when(root.get("status")).thenReturn(statusPath);
        when(root.get("price")).thenReturn(pricePath);

        Specification<Reservation> spec = ReservationSpecification.filterReservations(3L, ReservationStatus.PENDING, new BigDecimal("200.00"), null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(userIdPath, 3L);
        verify(cb).equal(statusPath, ReservationStatus.PENDING);
        verify(cb).greaterThanOrEqualTo(pricePath, new BigDecimal("200.00"));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void filterReservations_WithAllNull_ShouldReturnEmptyAndPredicate() {

        Specification<Reservation> spec = ReservationSpecification.filterReservations(null, null, null, null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).and(any(Predicate[].class));
    }
}