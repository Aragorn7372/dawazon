package dev.luisvives.dawazon.common.storage.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * Excepción personalizada para indicar que un recurso de almacenamiento
 * no fue encontrado.
 * <p>
 * Se lanza cuando se intenta acceder, eliminar o manipular un fichero
 * que no existe en el sistema de almacenamiento.
 * </p>
 *
 * <p>
 * La anotación {@link ResponseStatus} asegura que, al lanzarse esta excepción,
 * Spring devuelva automáticamente un estado HTTP 404 (Not Found).
 * </p>
 *
 * @see StorageException
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class StorageNotFound extends StorageException {

    /** Identificador de versión para serialización */
    @Serial
    private static final long serialVersionUID = 43876691117560211L;

    /**
     * Constructor con mensaje de error.
     *
     * @param mensaje Mensaje descriptivo de la excepción
     */
    public StorageNotFound(String mensaje) {
        super(mensaje);
    }
}
