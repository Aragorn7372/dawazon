package dev.luisvives.dawazon.common.email;

import dev.luisvives.dawazon.cart.models.Cart;

/**
 * Servicio para el envío de emails relacionados con pedidos
 */
public interface OrderEmailService {

    /**
     * Envía email de confirmación de pedido en HTML simple
     * @param pedido El pedido para el cual enviar la confirmación
     */
    void enviarConfirmacionPedido(Cart pedido);

    /**
     * Envía email de confirmación de pedido en formato HTML completo
     * @param pedido El pedido para el cual enviar la confirmación
     */
    void enviarConfirmacionPedidoHtml(Cart pedido);
}