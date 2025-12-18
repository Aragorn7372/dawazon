package dev.luisvives.dawazon.users.mapper;

import dev.luisvives.dawazon.cart.dto.ClientDto;
import dev.luisvives.dawazon.cart.models.Address;
import dev.luisvives.dawazon.cart.models.Client;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public Client toClient(ClientDto dto){
        return Client.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(
                        Address.builder()
                                .number(dto.getNumber())
                                .street(dto.getStreet())
                                .city(dto.getCity())
                                .province(dto.getProvince())
                                .country(dto.getCountry())
                                .postalCode(dto.getPostalCode())
                                .build()
                ).build();
    }
}
