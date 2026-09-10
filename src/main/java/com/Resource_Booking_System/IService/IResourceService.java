package com.Resource_Booking_System.IService;

import com.Resource_Booking_System.Dto.ResourceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface IResourceService {

    public ResourceDto createResource (ResourceDto resourceDto);

    public ResourceDto getResourceById (Long id);

    public Page<ResourceDto> getAllResource(Pageable pageable);

    public ResourceDto updateResource (Long id, ResourceDto resourceDto);

    void deleteResource (Long id);
}
