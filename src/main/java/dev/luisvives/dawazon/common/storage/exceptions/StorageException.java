package dev.luisvives.dawazon.common.storage.exceptions;

import java.io.Serial;

/**
 * Excepción base abstracta para el sistema de almacenamiento.
 * <p>
 * Todas las excepciones personalizadas relacionadas con operaciones de archivos
 * deben extender esta clase. Permite encapsular errores generales de almacenamiento
 * y facilita la gestión de excepciones específicas (como {@link StorageBadRequest}).
 * </p>
 *
 * <p>
 * Al extender {@link RuntimeException}, no es necesario declararla en el método
 * con `throws`. Esto permite lanzar la excepción en cualquier punto del código
 * sin obligar al manejo explícito.
 * </p>
 */
public abstract class StorageException extends RuntimeException {

    /**
     * Identificador de versión para serialización
     */
    @Serial
    private static final long serialVersionUID = 43876691117560211L;

    /**
     * Constructor con mensaje de error.
     *
     * @param mensaje Mensaje descriptivo de la excepción
     */
    public StorageException(String mensaje) {
        super(mensaje);
    }
}
