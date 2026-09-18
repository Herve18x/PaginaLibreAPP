package org.paginalibre.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static Conexion instancia;

    // Configuración de la conexión a MySQL
    private static final String URL =
            "jdbc:mysql://localhost:3306/libreriadb_in4cm"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC";

    private static final String USER = "IN4CM";
    private static final String PASSWORD = "#NdimAM4";

    // Constructor privado
    private Conexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL cargado correctamente.");
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el Driver de MySQL: "
                    + e.getMessage());
        }
    }

    // Singleton
    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    // Crear una conexión nueva
    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Probar conexión
    public boolean probarConexion() {
        try (Connection conexion = conectar()) {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            System.err.println("Error de conexión a MySQL: "
                    + e.getMessage());
            return false;
        }
    }
}