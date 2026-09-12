package com.resource_booking_system.Controller;

import com.resource_booking_system.Dto.ResourceDto;
import com.resource_booking_system.Exception.BadRequestException;
import com.resource_booking_system.IService.IResourceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/resources")
@Validated
public class ResourceController {


    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "type", "available", "createdAt", "updatedAt");

    private final IResourceService resourceService;

    private final int maxPageSize;

    public ResourceController(
            IResourceService resourceService,
            @Value("${app.pagination.max-page-size:50}")
            int maxPageSize)
    {
        this.resourceService = resourceService;
        this.maxPageSize = maxPageSize;
    }

    @PostMapping
    public ResponseEntity<ResourceDto> createResource(@Valid @RequestBody ResourceDto resourceDto) {

        ResourceDto response = resourceService.createResource(resourceDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDto> getResourceById(@PathVariable @Positive Long id) {

        ResourceDto response = resourceService.getResourceById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ResourceDto>> getAllResource(Pageable pageable) {

        validateSort(pageable);

        Pageable safePageable = createSafePageable(pageable);

        Page<ResourceDto> response = resourceService.getAllResource(safePageable);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceDto> updateResource(@PathVariable @Positive Long id, @Valid @RequestBody ResourceDto resourceDto) {

        ResourceDto response = resourceService.updateResource(id, resourceDto);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable @Positive Long id) {

        resourceService.deleteResource(id);

        return ResponseEntity.noContent().build();
    }

    private void validateSort(Pageable pageable) {

        for (Sort.Order order : pageable.getSort()) {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                throw new BadRequestException("Invalid sort field: " + order.getProperty());
            }
        }
    }

    private Pageable createSafePageable(Pageable pageable) {

        if (pageable.getPageSize() > maxPageSize) {
            throw new BadRequestException("Page size must not exceed " + maxPageSize);
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    }
}