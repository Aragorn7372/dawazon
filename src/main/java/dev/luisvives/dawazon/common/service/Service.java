package dev.luisvives.dawazon.common.service;

/**
 * Interfaz genérica que define los métodos comunes de un servicio CRUD.
 *
 * <p>
 * Los parámetros genéricos son:
 * <ul>
 *   <li><b>R</b>: Tipo de respuesta de las operaciones de lectura, creación o actualización.</li>
 *   <li><b>D</b>: Tipo de respuesta de la operación de eliminación.</li>
 *   <li><b>ID</b>: Tipo del identificador de la entidad (por ejemplo, Long, UUID).</li>
 *   <li><b>P</b>: Tipo de objeto que representa la entidad completa para creación o actualización.</li>
 *   <li><b>PA</b>: Tipo de objeto que representa la entidad parcial para operaciones PATCH.</li>
 * </ul>
 * </p>
 *
 * @param <R> Tipo de respuesta para get, save, update y patch.
 * @param <ID> Tipo del identificador de la entidad.
 * @param <P> Tipo de objeto completo para save y update.
 */
public interface Service<R, ID, P > {

    /**
     * Obtiene un recurso por su identificador.
     *
     * @param id Identificador del recurso.
     * @return El recurso correspondiente al ID.
     */
    R getById(ID id);

    /**
     * Crea un nuevo recurso.
     *
     * @param entity Objeto completo que representa el recurso a crear.
     * @return El recurso creado.
     */
    R save(P entity);

    /**
     * Elimina un recurso por su identificador.
     *
     * @param id Identificador del recurso a eliminar.
     * @return Objeto de respuesta que indica el resultado de la eliminación.
     */
    void deleteById(ID id);
}