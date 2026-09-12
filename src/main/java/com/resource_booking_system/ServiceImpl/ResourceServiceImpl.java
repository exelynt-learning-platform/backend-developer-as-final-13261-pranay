package com.resource_booking_system.ServiceImpl;

import com.resource_booking_system.Dto.ResourceDto;
import com.resource_booking_system.Entity.Resource;
import com.resource_booking_system.Exception.ResourceNotFoundException;
import com.resource_booking_system.IService.IResourceService;
import com.resource_booking_system.Repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ResourceServiceImpl implements IResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public ResourceDto createResource(ResourceDto resourceDto) {

        Resource resource = new Resource();

        resource.setName(resourceDto.getName());
        resource.setDescription(resourceDto.getDescription());
        resource.setType(resourceDto.getType());
        resource.setAvailable(resourceDto.isAvailable());

        Resource createdResource = resourceRepository.save(resource);

        return mapToDto(createdResource);
    }

    @Override
    public ResourceDto getResourceById(Long id) {

        Resource resource = resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource Not Found"));

        return mapToDto(resource);
    }

    @Override
    public Page<ResourceDto> getAllResource(Pageable pageable) {

        Page<Resource> resources = resourceRepository.findAll(pageable);

        return resources.map(this::mapToDto);
    }

    @Override
    public ResourceDto updateResource(Long id, ResourceDto resourceDto) {

        Resource resource = resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource Not Found"));

        resource.setName(resourceDto.getName());
        resource.setDescription(resourceDto.getDescription());
        resource.setType(resourceDto.getType());
        resource.setAvailable(resourceDto.isAvailable());

        Resource updatedResource = resourceRepository.save(resource);

        return mapToDto(updatedResource);
    }

    @Override
    public void deleteResource(Long id) {

        Resource resource = resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        resourceRepository.delete(resource);
    }

    private ResourceDto mapToDto(Resource resource) {

        ResourceDto resourceDto = new ResourceDto();

        resourceDto.setId(resource.getId());
        resourceDto.setName(resource.getName());
        resourceDto.setDescription(resource.getDescription());
        resourceDto.setType(resource.getType());
        resourceDto.setAvailable(resource.getAvailable());

        return resourceDto;
    }
}