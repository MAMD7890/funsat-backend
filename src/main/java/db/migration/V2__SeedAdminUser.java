package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.PreparedStatement;

/**
 * Migracion Java (no SQL) para poder generar el hash BCrypt real de la
 * contrasena semilla con el mismo algoritmo que usa la app en runtime,
 * en vez de pegar un hash calculado a mano en un script SQL.
 *
 * Usuario semilla: admin / ChangeMe#2026 (debe_cambiar_password = true,
 * forzando el cambio en el primer login via POST /auth/change-password).
 */
public class V2__SeedAdminUser extends BaseJavaMigration {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "ChangeMe#2026";

    @Override
    public void migrate(Context context) throws Exception {
        String hash = new BCryptPasswordEncoder().encode(DEFAULT_ADMIN_PASSWORD);

        String sql = """
                INSERT INTO usuario (nombre, username, password_hash, rol, activo, debe_cambiar_password)
                VALUES (?, ?, ?, 'ADMIN', 1, 1)
                """;

        try (PreparedStatement ps = context.getConnection().prepareStatement(sql)) {
            ps.setString(1, "Administrador");
            ps.setString(2, DEFAULT_ADMIN_USERNAME);
            ps.setString(3, hash);
            ps.executeUpdate();
        }
    }
}
