package com.upc.demo.servicio;

import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.AccommodationImage;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.repositorio.AccommodationImageRepository;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.UserRepository;
import com.upc.demo.servicio.storage.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private AccommodationImageRepository accommodationImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private AccommodationService accommodationService;

    @Test
    @DisplayName("deleteImage debe purgar la imagen de StorageService antes de eliminarla del repositorio")
    void deleteImage_withPublicId_purgesFromStorage() {
        UUID accommodationId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();

        User host = User.builder().id(hostId).role(Role.HOST).build();
        Accommodation accommodation = Accommodation.builder().id(accommodationId).host(host).build();

        AccommodationImage image = AccommodationImage.builder()
                .id(imageId)
                .publicId("cabins/cabin_123")
                .accommodation(accommodation)
                .build();

        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));
        when(accommodationImageRepository.findByIdAndAccommodationId(imageId, accommodationId)).thenReturn(Optional.of(image));

        accommodationService.deleteImage(accommodationId, imageId, hostId, false);

        verify(storageService, times(1)).delete("cabins/cabin_123");
        verify(accommodationImageRepository, times(1)).delete(image);
    }

    @Test
    @DisplayName("delete debe eliminar todas las imágenes asociadas de StorageService")
    void delete_accommodationWithImages_purgesAllFromStorage() {
        UUID accommodationId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();

        User host = User.builder().id(hostId).role(Role.HOST).build();

        AccommodationImage img1 = AccommodationImage.builder().publicId("cabins/img_1").build();
        AccommodationImage img2 = AccommodationImage.builder().publicId("cabins/img_2").build();
        List<AccommodationImage> images = new ArrayList<>(List.of(img1, img2));

        Accommodation accommodation = Accommodation.builder()
                .id(accommodationId)
                .host(host)
                .images(images)
                .build();

        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));

        accommodationService.delete(accommodationId, hostId, false);

        verify(storageService, times(1)).delete("cabins/img_1");
        verify(storageService, times(1)).delete("cabins/img_2");
        verify(accommodationRepository, times(1)).delete(accommodation);
    }
}
