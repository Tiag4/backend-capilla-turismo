package com.upc.demo.controlador;



import com.upc.demo.dto.attraction.AttractionImageDto;

import com.upc.demo.dto.attraction.AttractionResponseDto;

import com.upc.demo.dto.attraction.CreateAttractionDto;

import com.upc.demo.dto.attraction.UpdateAttractionDto;

import com.upc.demo.entidad.enums.AttractionCategory;

import com.upc.demo.servicio.AttractionService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;



import java.util.List;

import java.util.UUID;



@RestController

@RequestMapping("/api/v1/attractions")

@RequiredArgsConstructor

public class AttractionController {



    private final AttractionService attractionService;



    @GetMapping

    public ResponseEntity<List<AttractionResponseDto>> getAllAttractions(

            @RequestParam(required = false) AttractionCategory category,

            @RequestParam(required = false) String difficulty,

            @RequestParam(required = false) Boolean requiresGuide,

            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(attractionService.getAll(category, difficulty, requiresGuide, search));

    }



    @GetMapping("/{idOrSlug}")
    public ResponseEntity<AttractionResponseDto> getAttractionByIdOrSlug(@PathVariable String idOrSlug) {
        return ResponseEntity.ok(attractionService.getByIdOrSlug(idOrSlug));
    }



    @PostMapping

    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<AttractionResponseDto> createAttraction(

            @Valid @RequestBody CreateAttractionDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(attractionService.create(dto));

    }



    @PutMapping("/{id}")

    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<AttractionResponseDto> updateAttraction(

            @PathVariable UUID id,

            @Valid @RequestBody UpdateAttractionDto dto) {

        return ResponseEntity.ok(attractionService.update(id, dto));

    }



        @PatchMapping("/{id}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttractionResponseDto> setFeatured(
            @PathVariable UUID id,
            @RequestBody(required = false) UpdateAttractionDto dto) {
        if (dto != null && dto.getIsFeatured() != null) {
            return ResponseEntity.ok(attractionService.setFeatured(id, dto.getIsFeatured(), dto.getFeaturedBadgeTheme()));
        }
        return ResponseEntity.ok(attractionService.toggleFeatured(id));
    }



    @PatchMapping("/{id}/featured-theme")

    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<AttractionResponseDto> toggleFeaturedTheme(@PathVariable UUID id) {

        return ResponseEntity.ok(attractionService.toggleFeaturedTheme(id));

    }



    @DeleteMapping("/{id}")

    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<Void> deleteAttraction(@PathVariable UUID id) {

        attractionService.delete(id);

        return ResponseEntity.noContent().build();

    }



    @PostMapping("/{id}/images")

    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<AttractionImageDto> addImage(

            @PathVariable UUID id,

            @Valid @RequestBody AttractionImageDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(attractionService.addImage(id, dto));

    }



    @DeleteMapping("/{id}/images/{imageId}")

    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<Void> deleteImage(

            @PathVariable UUID id,

            @PathVariable UUID imageId) {

        attractionService.deleteImage(id, imageId);

        return ResponseEntity.noContent().build();

    }

}