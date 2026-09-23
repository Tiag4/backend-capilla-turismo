package com.upc.demo.controlador;

import com.upc.demo.dto.report.TourismReportResponseDto;
import com.upc.demo.servicio.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/occupancy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TourismReportResponseDto> getOccupancyReport(
            @RequestParam(required = false, defaultValue = "CURRENT_FORTNIGHT") String period) {
        return ResponseEntity.ok(reportService.getOccupancyReport(period));
    }
}
