package com.upc.demo.servicio;

import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.event.CreateOrUpdateEventDto;
import com.upc.demo.dto.event.EventResponseDto;
import com.upc.demo.entidad.Event;
import com.upc.demo.entidad.enums.EventCategory;
import com.upc.demo.repositorio.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    @DisplayName("createEvent debe persistir el evento y devolver el DTO correctamente")
    void createEvent_persistsAndReturnsDto() {
        CreateOrUpdateEventDto dto = CreateOrUpdateEventDto.builder()
                .title("Festival del Alienígena")
                .description("Desfile y conferencias")
                .category(EventCategory.FESTIVAL)
                .startDate(LocalDate.now().plusDays(5))
                .location("Calle Techada")
                .isFree(true)
                .build();

        UUID generatedId = UUID.randomUUID();
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event entity = invocation.getArgument(0);
            entity.setId(generatedId);
            return entity;
        });

        EventResponseDto result = eventService.createEvent(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(generatedId);
        assertThat(result.getTitle()).isEqualTo("Festival del Alienígena");
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("getEventById debe lanzar ResourceNotFoundException si el evento no existe")
    void getEventById_notFound_throwsException() {
        UUID randomId = UUID.randomUUID();
        when(eventRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(randomId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Evento cultural no encontrado con ID");
    }

    @Test
    @DisplayName("deleteEvent debe invocar la eliminación en el repositorio")
    void deleteEvent_callsRepositoryDelete() {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder().id(eventId).title("Feria").build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        eventService.deleteEvent(eventId);

        verify(eventRepository, times(1)).delete(event);
    }
}
