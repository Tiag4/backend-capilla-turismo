package com.upc.demo.servicio;

import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.event.CreateOrUpdateEventDto;
import com.upc.demo.dto.event.EventResponseDto;
import com.upc.demo.entidad.Event;
import com.upc.demo.entidad.enums.EventCategory;
import com.upc.demo.repositorio.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<EventResponseDto> getAllEvents(Boolean activeOnly, EventCategory category, LocalDate from, LocalDate to) {
        List<Event> events;

        if (Boolean.TRUE.equals(activeOnly)) {
            if (from != null && to != null) {
                events = eventRepository.findByIsActiveTrueAndStartDateBetweenOrderByStartDateAsc(from, to);
            } else if (category != null) {
                events = eventRepository.findByIsActiveTrueAndCategoryOrderByStartDateAsc(category);
            } else {
                events = eventRepository.findByIsActiveTrueOrderByStartDateAsc();
            }
        } else {
            events = eventRepository.findAllByOrderByStartDateAsc();
        }

        return events.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponseDto getEventById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento cultural no encontrado con ID: " + id));
        return toDto(event);
    }

    @Transactional
    public EventResponseDto createEvent(CreateOrUpdateEventDto dto) {
        Event event = Event.builder()
                .title(dto.getTitle().trim())
                .description(dto.getDescription().trim())
                .category(dto.getCategory())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .timeString(dto.getTimeString() != null ? dto.getTimeString().trim() : null)
                .location(dto.getLocation().trim())
                .isFree(dto.isFree())
                .price(dto.isFree() ? null : dto.getPrice())
                .imageUrl(dto.getImageUrl() != null ? dto.getImageUrl().trim() : null)
                .isActive(dto.getIsActive() == null || dto.getIsActive())
                .isFeatured(dto.getIsFeatured() != null && dto.getIsFeatured())
                .build();

        Event saved = eventRepository.save(event);
        log.info("Evento cultural creado con ID: {}, título: '{}'", saved.getId(), saved.getTitle());
        return toDto(saved);
    }

    @Transactional
    public EventResponseDto updateEvent(UUID id, CreateOrUpdateEventDto dto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento cultural no encontrado con ID: " + id));

        event.setTitle(dto.getTitle().trim());
        event.setDescription(dto.getDescription().trim());
        event.setCategory(dto.getCategory());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setTimeString(dto.getTimeString() != null ? dto.getTimeString().trim() : null);
        event.setLocation(dto.getLocation().trim());
        event.setFree(dto.isFree());
        event.setPrice(dto.isFree() ? null : dto.getPrice());
        event.setImageUrl(dto.getImageUrl() != null ? dto.getImageUrl().trim() : null);

        if (dto.getIsActive() != null) {
            event.setActive(dto.getIsActive());
        }
        if (dto.getIsFeatured() != null) {
            event.setFeatured(dto.getIsFeatured());
        }

        Event updated = eventRepository.save(event);
        log.info("Evento cultural actualizado con ID: {}", updated.getId());
        return toDto(updated);
    }

    @Transactional
    public void deleteEvent(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento cultural no encontrado con ID: " + id));
        eventRepository.delete(event);
        log.info("Evento cultural eliminado con ID: {}", id);
    }

    private EventResponseDto toDto(Event entity) {
        return EventResponseDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .timeString(entity.getTimeString())
                .location(entity.getLocation())
                .isFree(entity.isFree())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .isActive(entity.isActive())
                .isFeatured(entity.isFeatured())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
