package com.inventario.service;

import com.inventario.dto.request.CrearRecordatorioRequest;
import com.inventario.dto.request.EditarRecordatorioRequest;
import com.inventario.dto.response.NotificacionesRecordatorioResponse;
import com.inventario.dto.response.RecordatorioResponse;
import com.inventario.entity.Equipo;
import com.inventario.entity.EstadoRecordatorio;
import com.inventario.entity.RecordatorioMantenimiento;
import com.inventario.entity.Usuario;
import com.inventario.exception.EquipoNotFoundException;
import com.inventario.exception.RecordatorioNotFoundException;
import com.inventario.repository.EquipoRepository;
import com.inventario.repository.RecordatorioMantenimientoRepository;
import com.inventario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordatorioMantenimientoService {

    private static final int DIAS_PROXIMOS = 7;

    private final RecordatorioMantenimientoRepository recordatorioRepository;
    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<RecordatorioResponse> listarPorRango(LocalDate desde, LocalDate hasta) {
        return recordatorioRepository.findByFechaProgramadaBetweenOrderByFechaProgramadaAsc(desde, hasta).stream()
                .map(RecordatorioResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificacionesRecordatorioResponse notificaciones() {
        LocalDate hoy = LocalDate.now();

        List<RecordatorioResponse> vencidos = recordatorioRepository
                .findByEstadoAndFechaProgramadaBeforeOrderByFechaProgramadaAsc(EstadoRecordatorio.PENDIENTE, hoy)
                .stream()
                .map(RecordatorioResponse::from)
                .toList();

        List<RecordatorioResponse> proximos = recordatorioRepository
                .findByEstadoAndFechaProgramadaBetweenOrderByFechaProgramadaAsc(
                        EstadoRecordatorio.PENDIENTE, hoy, hoy.plusDays(DIAS_PROXIMOS))
                .stream()
                .map(RecordatorioResponse::from)
                .toList();

        return new NotificacionesRecordatorioResponse(vencidos, proximos);
    }

    @Transactional
    public RecordatorioResponse crear(CrearRecordatorioRequest request, String username) {
        Equipo equipo = equipoRepository.findById(request.equipoId())
                .orElseThrow(() -> new EquipoNotFoundException(request.equipoId()));
        Usuario creadoPor = usuarioRepository.findByUsername(username).orElse(null);

        RecordatorioMantenimiento recordatorio = RecordatorioMantenimiento.builder()
                .equipo(equipo)
                .titulo(request.titulo())
                .descripcion(request.descripcion())
                .fechaProgramada(request.fechaProgramada())
                .intervaloRecurrenciaDias(request.intervaloRecurrenciaDias())
                .creadoPor(creadoPor)
                .build();

        return RecordatorioResponse.from(recordatorioRepository.save(recordatorio));
    }

    @Transactional
    public RecordatorioResponse editar(Long id, EditarRecordatorioRequest request) {
        RecordatorioMantenimiento recordatorio = buscarPorId(id);
        recordatorio.setTitulo(request.titulo());
        recordatorio.setDescripcion(request.descripcion());
        recordatorio.setFechaProgramada(request.fechaProgramada());
        recordatorio.setIntervaloRecurrenciaDias(request.intervaloRecurrenciaDias());
        return RecordatorioResponse.from(recordatorioRepository.save(recordatorio));
    }

    /**
     * Completar un recordatorio nunca crea ni exige un Servicio (a proposito,
     * son flujos independientes). Si tenia intervalo de recurrencia, se
     * agenda automaticamente el siguiente a partir de la MISMA fecha
     * programada (no de "hoy"), para mantener un cronograma fijo aunque se
     * marque como cumplido tarde.
     */
    @Transactional
    public RecordatorioResponse completar(Long id) {
        RecordatorioMantenimiento recordatorio = buscarPorId(id);
        recordatorio.setEstado(EstadoRecordatorio.COMPLETADO);
        recordatorioRepository.save(recordatorio);

        if (recordatorio.getIntervaloRecurrenciaDias() != null) {
            RecordatorioMantenimiento siguiente = RecordatorioMantenimiento.builder()
                    .equipo(recordatorio.getEquipo())
                    .titulo(recordatorio.getTitulo())
                    .descripcion(recordatorio.getDescripcion())
                    .fechaProgramada(recordatorio.getFechaProgramada().plusDays(recordatorio.getIntervaloRecurrenciaDias()))
                    .intervaloRecurrenciaDias(recordatorio.getIntervaloRecurrenciaDias())
                    .creadoPor(recordatorio.getCreadoPor())
                    .build();
            recordatorioRepository.save(siguiente);
        }

        return RecordatorioResponse.from(recordatorio);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!recordatorioRepository.existsById(id)) {
            throw new RecordatorioNotFoundException(id);
        }
        recordatorioRepository.deleteById(id);
    }

    private RecordatorioMantenimiento buscarPorId(Long id) {
        return recordatorioRepository.findById(id).orElseThrow(() -> new RecordatorioNotFoundException(id));
    }
}
