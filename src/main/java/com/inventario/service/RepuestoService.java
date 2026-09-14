package com.inventario.service;

import com.inventario.dto.request.RepuestoRequest;
import com.inventario.dto.response.RepuestoResponse;
import com.inventario.entity.Repuesto;
import com.inventario.exception.RepuestoNotFoundException;
import com.inventario.repository.RepuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RepuestoService {

    private final RepuestoRepository repuestoRepository;

    @Transactional(readOnly = true)
    public Page<RepuestoResponse> listar(Pageable pageable) {
        return repuestoRepository.findAll(pageable).map(RepuestoResponse::from);
    }

    @Transactional(readOnly = true)
    public RepuestoResponse obtener(Long id) {
        return RepuestoResponse.from(buscarPorId(id));
    }

    @Transactional
    public RepuestoResponse crear(RepuestoRequest request) {
        Repuesto repuesto = Repuesto.builder()
                .nombre(request.nombre())
                .codigo(blankToNull(request.codigo()))
                .costoUnitario(request.costoUnitario())
                .stockDisponible(request.stockDisponible())
                .build();

        return RepuestoResponse.from(repuestoRepository.save(repuesto));
    }

    @Transactional
    public RepuestoResponse actualizar(Long id, RepuestoRequest request) {
        Repuesto repuesto = buscarPorId(id);
        repuesto.setNombre(request.nombre());
        repuesto.setCodigo(blankToNull(request.codigo()));
        repuesto.setCostoUnitario(request.costoUnitario());
        repuesto.setStockDisponible(request.stockDisponible());

        return RepuestoResponse.from(repuestoRepository.save(repuesto));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repuestoRepository.existsById(id)) {
            throw new RepuestoNotFoundException(id);
        }
        repuestoRepository.deleteById(id);
    }

    private Repuesto buscarPorId(Long id) {
        return repuestoRepository.findById(id).orElseThrow(() -> new RepuestoNotFoundException(id));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
