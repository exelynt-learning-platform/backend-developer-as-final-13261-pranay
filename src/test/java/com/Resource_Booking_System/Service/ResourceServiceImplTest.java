package com.Resource_Booking_System.Service;

import com.Resource_Booking_System.Dto.ResourceDto;
import com.Resource_Booking_System.Entity.Resource;
import com.Resource_Booking_System.Exception.ResourceNotFoundException;
import com.Resource_Booking_System.Repository.ResourceRepository;
import com.Resource_Booking_System.ServiceImpl.ResourceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceServiceImpl resourceService;



    @Test
    void createResource_ShouldCreateSuccessfully() {

        ResourceDto request = new ResourceDto();

        request.setName("Meeting Room");
        request.setDescription("Room for meetings");
        request.setType("ROOM");
        request.setAvailable(true);


        Resource savedResource = new Resource();

        savedResource.setId(1L);
        savedResource.setName("Meeting Room");
        savedResource.setDescription("Room for meetings");
        savedResource.setType("ROOM");
        savedResource.setAvailable(true);


        when(resourceRepository.save(any(Resource.class))).thenReturn(savedResource);


        ResourceDto response = resourceService.createResource(request);


        assertNotNull(response);

        assertEquals(1L, response.getId());
        assertEquals("Meeting Room", response.getName());
        assertEquals("Room for meetings", response.getDescription());
        assertEquals("ROOM", response.getType());
        assertTrue(response.isAvailable());


        verify(resourceRepository).save(any(Resource.class));
    }



    @Test
    void getResourceById_ShouldReturnResource() {

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setDescription("Room for meetings");
        resource.setType("ROOM");
        resource.setAvailable(true);


        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));


        ResourceDto response = resourceService.getResourceById(1L);


        assertNotNull(response);

        assertEquals(1L, response.getId());
        assertEquals("Meeting Room", response.getName());
        assertEquals("Room for meetings", response.getDescription());
        assertEquals("ROOM", response.getType());
        assertTrue(response.isAvailable());


        verify(resourceRepository).findById(1L);
    }


    @Test
    void getResourceById_WhenNotFound_ShouldThrowException() {

        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class, () -> resourceService.getResourceById(99L));


        verify(resourceRepository).findById(99L);
    }


    @Test
    void getAllResource_ShouldReturnResources() {

        Resource resource1 = new Resource();

        resource1.setId(1L);
        resource1.setName("Meeting Room");
        resource1.setDescription("Room 1");
        resource1.setType("ROOM");
        resource1.setAvailable(true);


        Resource resource2 = new Resource();

        resource2.setId(2L);
        resource2.setName("Projector");
        resource2.setDescription("Office Projector");
        resource2.setType("EQUIPMENT");
        resource2.setAvailable(true);


        List<Resource> resourceList = List.of(resource1, resource2);


        Page<Resource> resourcePage = new PageImpl<>(resourceList);


        Pageable pageable = PageRequest.of(0, 10);


        when(resourceRepository.findAll(pageable)).thenReturn(resourcePage);


        Page<ResourceDto> response = resourceService.getAllResource(pageable);


        assertNotNull(response);

        assertEquals(2, response.getTotalElements());

        assertEquals("Meeting Room", response.getContent().get(0).getName());

        assertEquals("Projector", response.getContent().get(1).getName());


        verify(resourceRepository).findAll(pageable);
    }


    @Test
    void updateResource_ShouldUpdateSuccessfully() {

        ResourceDto request = new ResourceDto();

        request.setName("Updated Room");
        request.setDescription("Updated Description");
        request.setType("ROOM");
        request.setAvailable(false);


        Resource existingResource = new Resource();

        existingResource.setId(1L);
        existingResource.setName("Old Room");
        existingResource.setDescription("Old Description");
        existingResource.setType("ROOM");
        existingResource.setAvailable(true);


        Resource updatedResource = new Resource();

        updatedResource.setId(1L);
        updatedResource.setName("Updated Room");
        updatedResource.setDescription("Updated Description");
        updatedResource.setType("ROOM");
        updatedResource.setAvailable(false);


        when(resourceRepository.findById(1L)).thenReturn(Optional.of(existingResource));

        when(resourceRepository.save(any(Resource.class))).thenReturn(updatedResource);


        ResourceDto response = resourceService.updateResource(1L, request);


        assertNotNull(response);

        assertEquals(1L, response.getId());
        assertEquals("Updated Room", response.getName());
        assertEquals("Updated Description", response.getDescription());
        assertEquals("ROOM", response.getType());
        assertFalse(response.isAvailable());


        verify(resourceRepository).findById(1L);

        verify(resourceRepository).save(existingResource);
    }



    @Test
    void updateResource_WhenNotFound_ShouldThrowException() {

        ResourceDto request = new ResourceDto();

        request.setName("Updated Room");
        request.setDescription("Updated Description");
        request.setType("ROOM");
        request.setAvailable(true);


        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class, () -> resourceService.updateResource(99L, request));


        verify(resourceRepository).findById(99L);

        verify(resourceRepository, never()).save(any(Resource.class));
    }


    @Test
    void deleteResource_ShouldDeleteSuccessfully() {

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setDescription("Room");
        resource.setType("ROOM");
        resource.setAvailable(true);


        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));


        resourceService.deleteResource(1L);


        verify(resourceRepository).findById(1L);

        verify(resourceRepository).delete(resource);
    }


    @Test
    void deleteResource_WhenNotFound_ShouldThrowException() {

        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class, () -> resourceService.deleteResource(99L));


        verify(resourceRepository).findById(99L);

        verify(resourceRepository, never()).delete(any(Resource.class));
    }
}