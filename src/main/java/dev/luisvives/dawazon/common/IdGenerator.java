package dev.luisvives.dawazon.common;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Clase que genera IDs
 */
public class IdGenerator implements IdentifierGenerator {
    @Value("${id.length}")
    private Integer length;
    /**
     * Metodo que genera un ID aleatorio con la longitud parametrizada
     * @return
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

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        return generateId();
    }
}
