package dev.luisvives.dawazon.common.utils;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.beans.factory.annotation.Value;

import java.util.Random;

/**
 * Generador personalizado de IDs alfanuméricos para entidades JPA.
 * <p>
 * Genera identificadores aleatorios usando caracteres alfanuméricos y guiones.
 * La longitud del ID es configurable mediante la propiedad {@code id.length}.
 * </p>
 */
public class IdGenerator implements IdentifierGenerator {
    /**
     * Longitud del ID generado (configurable desde properties).
     */
    @Value("${id.length}")
    private Integer length;

    /**
     * Genera un ID aleatorio con la longitud parametrizada.
     *
     * @return ID alfanumérico generado
     */
    private String generateId() {
        String charArray = "QWRTYPSDFGHJKLZXCVBNMqwrtypsdfghjklzxcvbnm1234567890-_";
        StringBuilder id = new StringBuilder();
        if (length == null) length = 12;
        for  (int i = 0; i < length ; i++) {
            id.append(charArray.charAt(new Random().nextInt(charArray.length())));
        }
        return id.toString();
    }

    /**
     * Método llamado por Hibernate para generar el ID de la entidad.
     *
     * @param session Sesión de Hibernate
     * @param object  Entidad para la que se genera el ID
     * @return ID generado
     */
    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        return generateId();
    }
}
