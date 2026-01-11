package dev.luisvives.dawazon.users.mapper;

import dev.luisvives.dawazon.cart.dto.ClientDto;
import dev.luisvives.dawazon.cart.models.Address;
import dev.luisvives.dawazon.cart.models.Client;
import dev.luisvives.dawazon.users.dto.UserAdminRequestDto;
import dev.luisvives.dawazon.users.dto.UserRequestDto;
import dev.luisvives.dawazon.users.models.Role;
import dev.luisvives.dawazon.users.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para UserMapper siguiendo principios FIRST:
 * - Fast: Tests rápidos sin dependencias externas
 * - Independent: Cada test es independiente
 * - Repeatable: Resultados consistentes en cualquier entorno
 * - Self-validating: Assertions claros y automáticos
 * - Timely: Tests escritos junto al código
 */
@DisplayName("UserMapper Test - FIRST Principles")
class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        // Inicialización del mapper antes de cada test (Independent)
        userMapper = new UserMapper();
    }

    // ========== Tests para toClient(ClientDto) ==========

    @Test
    @DisplayName("toClient debe convertir ClientDto con todos los datos a Client")
    void toClient_ConDatosCompletos_RetornaClientConAddress() {
        // Given - Preparamos un ClientDto con todos los datos
        ClientDto dto = ClientDto.builder()
                .name("Juan Pérez")
                .email("juan@example.com")
                .phone("123456789")
                .number((short) 42)
                .street("Calle Mayor")
                .city("Madrid")
                .province("Madrid")
                .country("España")
                .postalCode(28001)
                .build();

        // When - Ejecutamos el método a testear
        Client result = userMapper.toClient(dto);

        // Then - Verificamos todos los campos (Self-validating)
        assertNotNull(result);
        assertEquals("Juan Pérez", result.getName());
        assertEquals("juan@example.com", result.getEmail());
        assertEquals("123456789", result.getPhone());

        // Verificamos el Address embebido
        assertNotNull(result.getAddress());
        assertEquals(Short.valueOf((short) 42), result.getAddress().getNumber());
        assertEquals("Calle Mayor", result.getAddress().getStreet());
        assertEquals("Madrid", result.getAddress().getCity());
        assertEquals("Madrid", result.getAddress().getProvince());
        assertEquals("España", result.getAddress().getCountry());
        assertEquals(28001, result.getAddress().getPostalCode());
    }

    @Test
    @DisplayName("toClient debe manejar ClientDto con valores nulos")
    void toClient_ConValoresNulos_RetornaClientConNulos() {
        // Given - ClientDto con valores null
        ClientDto dto = ClientDto.builder()
                .name(null)
                .email(null)
                .phone(null)
                .number(null)
                .street(null)
                .city(null)
                .province(null)
                .country(null)
                .postalCode(null)
                .build();

        // When
        Client result = userMapper.toClient(dto);

        // Then - El mapper debe crear el objeto aunque tenga nulls
        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getEmail());
        assertNull(result.getPhone());
        assertNotNull(result.getAddress());
    }

    // ========== Tests para toUserAdminRequestDto(User) ==========

    @Test
    @DisplayName("toUserAdminRequestDto debe convertir User completo a DTO")
    void toUserAdminRequestDto_ConDatosCompletos_RetornaDtoCompleto() {
        // Given - User con Client y Address completos
        Address address = Address.builder()
                .street("Calle Principal")
                .city("Barcelona")
                .province("Barcelona")
                .postalCode(8001)
                .build();

        Client client = Client.builder()
                .name("María García")
                .build();
        client.setAddress(address);

        User user = User.builder()
                .id(1L)
                .userName("maria.garcia")
                .email("maria@example.com")
                .telefono("987654321")
                .roles(List.of(Role.ADMIN, Role.USER))
                .client(client)
                .build();

        // When
        UserAdminRequestDto result = userMapper.toUserAdminRequestDto(user);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("María García", result.getNombre());
        assertEquals("maria@example.com", result.getEmail());
        assertEquals("987654321", result.getTelefono());
        assertEquals(2, result.getRoles().size());
        assertTrue(result.getRoles().contains("ADMIN"));
        assertTrue(result.getRoles().contains("USER"));
        assertEquals("Calle Principal", result.getCalle());
        assertEquals("Barcelona", result.getCiudad());
        assertEquals("8001", result.getCodigoPostal());
        assertEquals("Barcelona", result.getProvincia());
    }

    @Test
    @DisplayName("toUserAdminRequestDto debe usar username si client.name es null")
    void toUserAdminRequestDto_SinClientName_UsaUsername() {
        // Given - User sin nombre de cliente
        Client client = Client.builder()
                .name(null)
                .build();

        User user = User.builder()
                .id(2L)
                .userName("johndoe")
                .email("john@example.com")
                .client(client)
                .build();

        // When
        UserAdminRequestDto result = userMapper.toUserAdminRequestDto(user);

        // Then - Debe usar el username como nombre
        assertEquals("johndoe", result.getNombre());
    }

    @Test
    @DisplayName("toUserAdminRequestDto debe manejar User sin Client")
    void toUserAdminRequestDto_SinClient_RetornaDtoConValoresVacios() {
        // Given - User sin Client
        User user = User.builder()
                .id(3L)
                .userName("testuser")
                .email("test@example.com")
                .telefono(null)
                .roles(null)
                .client(null)
                .build();

        // When
        UserAdminRequestDto result = userMapper.toUserAdminRequestDto(user);

        // Then - Debe usar valores por defecto
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("testuser", result.getNombre());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("", result.getTelefono());
        assertEquals(Set.of("USER"), result.getRoles());
        assertEquals("", result.getCalle());
        assertEquals("", result.getCiudad());
        assertEquals("", result.getCodigoPostal());
        assertEquals("", result.getProvincia());
    }

    @Test
    @DisplayName("toUserAdminRequestDto debe manejar User con Client sin Address")
    void toUserAdminRequestDto_ClientSinAddress_RetornaDtoConDireccionVacia() {
        // Given - User con Client pero sin Address
        Client client = Client.builder()
                .name("Pedro López")
                .build();

        User user = User.builder()
                .id(4L)
                .userName("pedro")
                .email("pedro@example.com")
                .telefono("555123456")
                .client(client)
                .build();

        // When
        UserAdminRequestDto result = userMapper.toUserAdminRequestDto(user);

        // Then - Campos de dirección deben estar vacíos
        assertEquals("Pedro López", result.getNombre());
        assertEquals("", result.getCalle());
        assertEquals("", result.getCiudad());
        assertEquals("", result.getCodigoPostal());
        assertEquals("", result.getProvincia());
    }

    @Test
    @DisplayName("toUserAdminRequestDto debe manejar roles vacíos")
    void toUserAdminRequestDto_RolesVacios_RetornaRolUserPorDefecto() {
        // Given - User con set de roles vacío
        User user = User.builder()
                .id(5L)
                .userName("newuser")
                .email("new@example.com")
                .roles(List.of())
                .build();

        // When
        UserAdminRequestDto result = userMapper.toUserAdminRequestDto(user);

        // Then - Debe tener rol USER por defecto
        assertEquals(Set.of("USER"), result.getRoles());
    }

    @Test
    @DisplayName("toUserAdminRequestDto debe manejar PostalCode null en Address")
    void toUserAdminRequestDto_PostalCodeNull_RetornaStringVacio() {
        // Given - Address con postalCode null
        Address address = Address.builder()
                .street("Avenida Test")
                .city("Valencia")
                .province("Valencia")
                .postalCode(null)
                .build();

        Client client = Client.builder()
                .name("Ana Ruiz")
                .build();
        client.setAddress(address);

        User user = User.builder()
                .id(6L)
                .userName("ana")
                .email("ana@example.com")
                .client(client)
                .build();

        // When
        UserAdminRequestDto result = userMapper.toUserAdminRequestDto(user);

        // Then - CodigoPostal debe ser string vacío
        assertEquals("", result.getCodigoPostal());
    }

    // ========== Tests para toUserRequestDto(User) ==========

    @Test
    @DisplayName("toUserRequestDto debe convertir User completo a DTO")
    void toUserRequestDto_ConDatosCompletos_RetornaDtoCompleto() {
        // Given - User completo
        Address address = Address.builder()
                .street("Plaza Mayor")
                .city("Sevilla")
                .province("Sevilla")
                .postalCode(41001)
                .build();

        Client client = Client.builder()
                .name("Carlos Martínez")
                .build();
        client.setAddress(address);

        User user = User.builder()
                .userName("carlos")
                .email("carlos@example.com")
                .telefono("666777888")
                .client(client)
                .build();

        // When
        UserRequestDto result = userMapper.toUserRequestDto(user);

        // Then
        assertNotNull(result);
        assertEquals("Carlos Martínez", result.getNombre());
        assertEquals("carlos@example.com", result.getEmail());
        assertEquals("666777888", result.getTelefono());
        assertEquals("Plaza Mayor", result.getCalle());
        assertEquals("Sevilla", result.getCiudad());
        assertEquals("41001", result.getCodigoPostal());
        assertEquals("Sevilla", result.getProvincia());
    }

    @Test
    @DisplayName("toUserRequestDto debe usar username si client.name es null")
    void toUserRequestDto_SinClientName_UsaUsername() {
        // Given - User sin nombre de cliente
        Client client = Client.builder()
                .name(null)
                .build();

        User user = User.builder()
                .userName("testuser2")
                .email("test2@example.com")
                .client(client)
                .build();

        // When
        UserRequestDto result = userMapper.toUserRequestDto(user);

        // Then - Debe usar username
        assertEquals("testuser2", result.getNombre());
    }

    @Test
    @DisplayName("toUserRequestDto debe manejar User sin Client")
    void toUserRequestDto_SinClient_RetornaDtoConValoresVacios() {
        // Given - User sin Client
        User user = User.builder()
                .userName("alone")
                .email("alone@example.com")
                .telefono(null)
                .client(null)
                .build();

        // When
        UserRequestDto result = userMapper.toUserRequestDto(user);

        // Then
        assertNotNull(result);
        assertEquals("alone", result.getNombre());
        assertEquals("alone@example.com", result.getEmail());
        assertEquals("", result.getTelefono());
        assertEquals("", result.getCalle());
        assertEquals("", result.getCiudad());
        assertEquals("", result.getCodigoPostal());
        assertEquals("", result.getProvincia());
    }

    @Test
    @DisplayName("toUserRequestDto debe manejar PostalCode null")
    void toUserRequestDto_PostalCodeNull_RetornaStringVacio() {
        // Given - Address con postalCode null
        Address address = Address.builder()
                .street("Calle Sin CP")
                .city("Zaragoza")
                .province("Zaragoza")
                .postalCode(null)
                .build();

        Client client = Client.builder()
                .name("Laura Sánchez")
                .build();
        client.setAddress(address);

        User user = User.builder()
                .userName("laura")
                .email("laura@example.com")
                .client(client)
                .build();

        // When
        UserRequestDto result = userMapper.toUserRequestDto(user);

        // Then
        assertEquals("", result.getCodigoPostal());
    }

    @Test
    @DisplayName("toUserRequestDto debe manejar telefono null")
    void toUserRequestDto_TelefonoNull_RetornaStringVacio() {
        // Given - User con telefono null
        User user = User.builder()
                .userName("notel")
                .email("notel@example.com")
                .telefono(null)
                .build();

        // When
        UserRequestDto result = userMapper.toUserRequestDto(user);

        // Then
        assertEquals("", result.getTelefono());
    }
}
