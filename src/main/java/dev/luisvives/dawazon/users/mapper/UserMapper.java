package dev.luisvives.dawazon.users.mapper;

import dev.luisvives.dawazon.cart.dto.ClientDto;
import dev.luisvives.dawazon.cart.models.Address;
import dev.luisvives.dawazon.cart.models.Client;
import dev.luisvives.dawazon.users.dto.UserAdminRequestDto;
import dev.luisvives.dawazon.users.dto.UserRequestDto;
import dev.luisvives.dawazon.users.models.User;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir DTOs a entidades de usuario.
 */
@Component
public class UserMapper {
    /**
     * Convierte un ClientDto a entidad Client.
     *
     * @param dto DTO con datos del cliente
     * @return Entidad Client con dirección embebida
     */
    public Client toClient(ClientDto dto) {
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
                                .build())
                .build();
    }

    /**
     * Convierte un User a UserAdminRequestDto.
     *
     * @param user Entidad User
     * @return DTO UserAdminRequestDto con datos planos
     */
    public UserAdminRequestDto toUserAdminRequestDto(User user) {
        return UserAdminRequestDto.builder()
                .id(user.getId())
                .nombre(user.getClient() != null && user.getClient().getName() != null
                        ? user.getClient().getName()
                        : user.getUsername())
                .email(user.getEmail())
                .telefono(user.getTelefono() != null ? user.getTelefono() : "")
                .roles(user.getRoles() != null && !user.getRoles().isEmpty()
                        ? user.getRoles().stream().map(role -> role.name()).collect(java.util.stream.Collectors.toSet())
                        : java.util.Set.of("USER"))
                .calle(user.getClient() != null && user.getClient().getAddress() != null
                        ? user.getClient().getAddress().getStreet()
                        : "")
                .ciudad(user.getClient() != null && user.getClient().getAddress() != null
                        ? user.getClient().getAddress().getCity()
                        : "")
                .codigoPostal(user.getClient() != null && user.getClient().getAddress() != null
                        && user.getClient().getAddress().getPostalCode() != null
                                ? user.getClient().getAddress().getPostalCode().toString()
                                : "")
                .provincia(user.getClient() != null && user.getClient().getAddress() != null
                        ? user.getClient().getAddress().getProvince()
                        : "")
                .build();
    }

    /**
     * Convierte un User a UserRequestDto.
     *
     * @param user Entidad User
     * @return DTO UserRequestDto con datos planos
     */
    public UserRequestDto toUserRequestDto(User user) {
        return UserRequestDto.builder()
                .nombre(user.getClient() != null && user.getClient().getName() != null
                        ? user.getClient().getName()
                        : user.getUsername())
                .email(user.getEmail())
                .telefono(user.getTelefono() != null ? user.getTelefono() : "")
                .calle(user.getClient() != null && user.getClient().getAddress() != null
                        ? user.getClient().getAddress().getStreet()
                        : "")
                .ciudad(user.getClient() != null && user.getClient().getAddress() != null
                        ? user.getClient().getAddress().getCity()
                        : "")
                .codigoPostal(user.getClient() != null && user.getClient().getAddress() != null
                        && user.getClient().getAddress().getPostalCode() != null
                                ? user.getClient().getAddress().getPostalCode().toString()
                                : "")
                .provincia(user.getClient() != null && user.getClient().getAddress() != null
                        ? user.getClient().getAddress().getProvince()
                        : "")
                .build();
    }
}
