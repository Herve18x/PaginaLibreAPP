package org.paginalibre.controller;

import org.paginalibre.model.Usuario;

public interface BaseDashboardController {
    /**
     * Permite transferir el usuario autenticado al dashboard correspondiente.
     */
    void iniciarUsuario(Usuario usuario);
}