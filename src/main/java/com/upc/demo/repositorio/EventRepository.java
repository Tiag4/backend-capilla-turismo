package com.upc.demo.repositorio;

import com.upc.demo.entidad.Event;
import com.upc.demo.entidad.enums.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByIsActiveTrueOrderByStartDateAsc();

    List<Event> findAllByOrderByStartDateAsc();

    List<Event> findByIsActiveTrueAndCategoryOrderByStartDateAsc(EventCategory category);

    List<Event> findByIsActiveTrueAndStartDateBetweenOrderByStartDateAsc(LocalDate from, LocalDate to);
}
