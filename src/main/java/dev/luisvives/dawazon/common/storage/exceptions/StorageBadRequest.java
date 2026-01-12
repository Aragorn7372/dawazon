package dev.luisvives.dawazon.common.storage.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * Excepción personalizada para errores de tipo "Bad Request" en el sistema de almacenamiento.
 * <p>
 * Se lanza cuando se detecta una solicitud inválida relacionada con operaciones
 * de archivos, como subir un fichero vacío o con formato incorrecto.
 * </p>
 *
 * <p>
 * La anotación {@link ResponseStatus} asegura que cuando se lance esta excepción,
 * Spring devuelva automáticamente un estado HTTP 400 (Bad Request).
 * </p>
 * @see StorageException
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StorageBadRequest extends StorageException {

    /** Identificador de versión para serialización */
    @Serial
    private static final long serialVersionUID = 43876691117560211L;

    /**
     * Constructor con mensaje de error.
     *
     * @param mensaje Mensaje descriptivo de la excepción.
     */
    public StorageBadRequest(String mensaje) {
        super(mensaje);
    }
}
