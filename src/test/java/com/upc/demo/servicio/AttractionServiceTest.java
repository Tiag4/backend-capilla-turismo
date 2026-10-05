package com.upc.demo.servicio;

import com.upc.demo.entidad.Attraction;
import com.upc.demo.entidad.AttractionImage;
import com.upc.demo.repositorio.AttractionImageRepository;
import com.upc.demo.repositorio.AttractionRepository;
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
class AttractionServiceTest {

    @Mock
    private AttractionRepository attractionRepository;

    @Mock
    private AttractionImageRepository attractionImageRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private AttractionService attractionService;

    @Test
    @DisplayName("deleteImage debe purgar la imagen de StorageService antes de eliminarla del repositorio")
    void deleteImage_withPublicId_purgesFromStorage() {
        UUID attractionId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        Attraction attraction = Attraction.builder().id(attractionId).build();
        AttractionImage image = AttractionImage.builder()
                .id(imageId)
                .publicId("attractions/uritorco_top")
                .attraction(attraction)
                .build();

        when(attractionRepository.findById(attractionId)).thenReturn(Optional.of(attraction));
        when(attractionImageRepository.findByIdAndAttractionId(imageId, attractionId)).thenReturn(Optional.of(image));

        attractionService.deleteImage(attractionId, imageId);

        verify(storageService, times(1)).delete("attractions/uritorco_top");
        verify(attractionImageRepository, times(1)).delete(image);
    }

    @Test
    @DisplayName("delete debe eliminar todas las imágenes asociadas de StorageService")
    void delete_attractionWithImages_purgesAllFromStorage() {
        UUID attractionId = UUID.randomUUID();

        AttractionImage img1 = AttractionImage.builder().publicId("attractions/img_1").build();
        AttractionImage img2 = AttractionImage.builder().publicId("attractions/img_2").build();
        List<AttractionImage> images = new ArrayList<>(List.of(img1, img2));

        Attraction attraction = Attraction.builder()
                .id(attractionId)
                .images(images)
                .build();

        when(attractionRepository.findById(attractionId)).thenReturn(Optional.of(attraction));

        attractionService.delete(attractionId);

        verify(storageService, times(1)).delete("attractions/img_1");
        verify(storageService, times(1)).delete("attractions/img_2");
        verify(attractionRepository, times(1)).delete(attraction);
    }
}
