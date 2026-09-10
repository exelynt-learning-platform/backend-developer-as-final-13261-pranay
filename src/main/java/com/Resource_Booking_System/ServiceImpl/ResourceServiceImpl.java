package com.Resource_Booking_System.ServiceImpl;

import com.Resource_Booking_System.Dto.ResourceDto;
import com.Resource_Booking_System.Entity.Resource;
import com.Resource_Booking_System.Exception.ResourceNotFoundException;
import com.Resource_Booking_System.IService.IResourceService;
import com.Resource_Booking_System.Repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ResourceServiceImpl implements IResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository)
    {
        this.resourceRepository = resourceRepository;
    }


    @Override
    public ResourceDto createResource(ResourceDto resourceDto) {

        Resource resource = new Resource();
        resource.setName(resourceDto.getName());
        resource.setDescription(resourceDto.getDescription());
        resource.setType(resourceDto.getType());
        resource.setAvailable(resourceDto.isAvailable());

        Resource create = resourceRepository.save(resource);

        ResourceDto resourceDto1 = new ResourceDto();

        resourceDto1.setId(create.getId());
        resourceDto1.setName(create.getName());
        resourceDto1.setDescription(create.getDescription());
        resourceDto1.setType(create.getType());
        resourceDto1.setAvailable(create.getAvailable());

        return resourceDto1;
    }

    @Override
    public ResourceDto getResourceById(Long id) {

         Resource resource = resourceRepository.findById(id)
                 .orElseThrow(() -> new ResourceNotFoundException("Resource Not F0und"));

         ResourceDto resourceDto = new ResourceDto();
         resourceDto.setId(resource.getId());
         resourceDto.setName(resource.getName());
         resourceDto.setDescription(resource.getDescription());
         resourceDto.setType(resource.getType());
         resourceDto.setAvailable(resource.getAvailable());

        return resourceDto;
    }

    @Override
    public Page<ResourceDto> getAllResource(Pageable pageable) {

         Page<Resource> resources = resourceRepository.findAll(pageable);

         return resources.map( resource -> {

             ResourceDto resourceDto = new ResourceDto();

             resourceDto.setId(resource.getId());

             resourceDto.setName(resource.getName());

             resourceDto.setDescription(resource.getDescription());

             resourceDto.setType(resource.getType());

             resourceDto.setAvailable(resource.getAvailable());

             return resourceDto;

         });
    }

    @Override
    public ResourceDto updateResource(Long id, ResourceDto resourceDto) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("Resource Not Found"));

        resource.setName(resourceDto.getName());
        resource.setDescription(resourceDto.getDescription());
        resource.setType(resourceDto.getType());
        resource.setAvailable(resourceDto.isAvailable());

        Resource update = resourceRepository.save(resource);

        ResourceDto resourceDto1 = new ResourceDto();

        resourceDto1.setId(update.getId());
        resourceDto1.setName(update.getName());
        resourceDto1.setDescription(update.getDescription());
        resourceDto1.setType(update.getType());
        resourceDto1.setAvailable(update.getAvailable());

        return resourceDto1;
    }

    @Override
    public void deleteResource(Long id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        resourceRepository.delete(resource);

    }
}
