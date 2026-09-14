package com.inventario.service;

import com.inventario.dto.response.DashboardResponse;
import com.inventario.dto.response.NotificacionesRecordatorioResponse;
import com.inventario.dto.response.RecordatorioResponse;
import com.inventario.dto.response.ResultadoAlertaResponse;
import com.inventario.entity.Rol;
import com.inventario.entity.Usuario;
import com.inventario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Resumen diario de alertas (mantenimientos vencidos/próximos + equipos con
 * salida prolongada) por correo, para no depender de que alguien abra la
 * campanita. Se omite en silencio si no hay servidor SMTP configurado
 * (spring.mail.host vacío) — nunca falla el arranque ni una petición por esto.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaEmailService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Optional<JavaMailSender> mailSender;
    private final RecordatorioMantenimientoService recordatorioMantenimientoService;
    private final DashboardService dashboardService;
    private final UsuarioRepository usuarioRepository;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.alertas.remitente}")
    private String remitente;

    @Scheduled(cron = "${app.alertas.cron}")
    public void enviarResumenDiarioProgramado() {
        ResultadoAlertaResponse resultado = enviarResumenDiario();
        log.info("Resumen diario de alertas: enviado={}, destinatarios={}, mensaje={}",
                resultado.enviado(), resultado.destinatarios(), resultado.mensaje());
    }

    public ResultadoAlertaResponse enviarResumenDiario() {
        if (mailHost == null || mailHost.isBlank() || mailSender.isEmpty()) {
            return new ResultadoAlertaResponse(false,
                    "El correo no está configurado (spring.mail.host vacío); no se envió nada.", 0);
        }

        NotificacionesRecordatorioResponse notificaciones = recordatorioMantenimientoService.notificaciones();
        List<DashboardResponse.EquipoEnCalle> equiposEnAlerta = dashboardService.listarEquiposEnAlerta();

        if (notificaciones.vencidos().isEmpty() && notificaciones.proximos().isEmpty() && equiposEnAlerta.isEmpty()) {
            return new ResultadoAlertaResponse(false, "No hay alertas pendientes hoy.", 0);
        }

        List<String> destinatarios = usuarioRepository.findByActivoTrueAndRolOrderByNombreAsc(Rol.ADMIN).stream()
                .map(Usuario::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .toList();

        if (destinatarios.isEmpty()) {
            return new ResultadoAlertaResponse(false,
                    "Ningún administrador tiene un correo configurado en su perfil.", 0);
        }

        String cuerpo = componerCuerpo(notificaciones, equiposEnAlerta);

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setBcc(destinatarios.toArray(String[]::new));
        mensaje.setSubject("FUNSAT — Resumen diario de alertas");
        mensaje.setText(cuerpo);

        try {
            mailSender.get().send(mensaje);
        } catch (MailException e) {
            log.warn("No se pudo enviar el resumen de alertas por correo", e);
            return new ResultadoAlertaResponse(false, "Error al enviar el correo: " + e.getMessage(), 0);
        }

        return new ResultadoAlertaResponse(true, "Resumen enviado correctamente.", destinatarios.size());
    }

    private String componerCuerpo(NotificacionesRecordatorioResponse notificaciones,
                                   List<DashboardResponse.EquipoEnCalle> equiposEnAlerta) {
        StringBuilder sb = new StringBuilder();
        sb.append("Resumen diario de FUNSAT\n\n");

        agregarSeccionRecordatorios(sb, "MANTENIMIENTOS VENCIDOS", notificaciones.vencidos());
        agregarSeccionRecordatorios(sb, "MANTENIMIENTOS PRÓXIMOS (7 días)", notificaciones.proximos());

        sb.append(String.format("EQUIPOS CON SALIDA PROLONGADA (%d)%n", equiposEnAlerta.size()));
        if (equiposEnAlerta.isEmpty()) {
            sb.append("  (ninguno)\n");
        } else {
            for (DashboardResponse.EquipoEnCalle e : equiposEnAlerta) {
                sb.append(String.format("  - %s (%s) — con %s desde %s, %d días fuera%n",
                        e.equipoDescripcion(), e.equipoCodigo() != null ? e.equipoCodigo() : "s/código",
                        e.clienteNombre(), e.fechaSalida().format(FECHA), e.diasFuera()));
            }
        }
        sb.append("\n— Sistema Inventario FUNSAT");

        return sb.toString();
    }

    private void agregarSeccionRecordatorios(StringBuilder sb, String titulo, List<RecordatorioResponse> items) {
        sb.append(String.format("%s (%d)%n", titulo, items.size()));
        if (items.isEmpty()) {
            sb.append("  (ninguno)\n\n");
            return;
        }
        for (RecordatorioResponse r : items) {
            sb.append(String.format("  - %s (%s): %s — programado %s%n",
                    r.equipoDescripcion(), r.equipoCodigo() != null ? r.equipoCodigo() : "s/código",
                    r.titulo(), r.fechaProgramada().format(FECHA)));
        }
        sb.append('\n');
    }
}
