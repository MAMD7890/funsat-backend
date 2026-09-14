package com.inventario.service;

import com.inventario.dto.request.TipoMotorCatalogoRequest;
import com.inventario.dto.response.TipoMotorCatalogoResponse;
import com.inventario.entity.AccionAuditoria;
import com.inventario.entity.TipoMotorCatalogo;
import com.inventario.exception.TipoMotorCatalogoNotFoundException;
import com.inventario.exception.TipoMotorCatalogoYaExisteException;
import com.inventario.repository.TipoMotorCatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoMotorCatalogoService {

    private final TipoMotorCatalogoRepository tipoMotorCatalogoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<TipoMotorCatalogoResponse> listar(boolean soloActivos) {
        List<TipoMotorCatalogo> motores = soloActivos
                ? tipoMotorCatalogoRepository.findByActivoTrueOrderByOrdenAscNombreAsc()
                : tipoMotorCatalogoRepository.findAllByOrderByOrdenAscNombreAsc();
        return motores.stream().map(TipoMotorCatalogoResponse::from).toList();
    }

    @Transactional
    public TipoMotorCatalogoResponse crear(TipoMotorCatalogoRequest request) {
        if (tipoMotorCatalogoRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new TipoMotorCatalogoYaExisteException(request.nombre());
        }

        TipoMotorCatalogo motor = TipoMotorCatalogo.builder()
                .nombre(request.nombre())
                .orden(request.orden())
                .activo(request.activo() == null || request.activo())
                .build();

        TipoMotorCatalogo guardado = tipoMotorCatalogoRepository.save(motor);
        auditoriaService.registrar(AccionAuditoria.CREAR, "TipoMotorCatalogo", guardado.getId(), guardado.getNombre());
        return TipoMotorCatalogoResponse.from(guardado);
    }

    @Transactional
    public TipoMotorCatalogoResponse actualizar(Long id, TipoMotorCatalogoRequest request) {
        TipoMotorCatalogo motor = buscarPorId(id);

        if (tipoMotorCatalogoRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre(), id)) {
            throw new TipoMotorCatalogoYaExisteException(request.nombre());
        }

        motor.setNombre(request.nombre());
        motor.setOrden(request.orden());
        if (request.activo() != null) {
            motor.setActivo(request.activo());
        }

        TipoMotorCatalogo guardado = tipoMotorCatalogoRepository.save(motor);
        auditoriaService.registrar(AccionAuditoria.EDITAR, "TipoMotorCatalogo", guardado.getId(), guardado.getNombre());
        return TipoMotorCatalogoResponse.from(guardado);
    }

    private TipoMotorCatalogo buscarPorId(Long id) {
        return tipoMotorCatalogoRepository.findById(id)
                .orElseThrow(() -> new TipoMotorCatalogoNotFoundException(id));
    }
}
