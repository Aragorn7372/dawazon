// Script de inicialización para MongoDB - Colección de Carritos
// Este script debe ejecutarse en la base de datos de MongoDB

// Eliminar la colección si existe
db.cart.drop();

// Crear la colección
db.createCollection("cart");


// IMPORTANTE:
// - ADMIN (userId: 1) y MANAGER (userId: 2) NO tienen carritos
// - Todos los usuarios normales (USER) tienen SIEMPRE un carrito activo (purchased: false)
// - Los usuarios pueden tener carritos históricos (purchased: true)

// Carrito activo de John Doe (userId: 3)
db.cart.insertOne({
    _id: ObjectId(),
    userId: NumberLong(3),
    purchased: false,
    client: {
        name: "John Doe",
        email: "john. doe@email.com",
        phone: "+34666333444",
        address: {
            number: 42,
            street: "Gran Vía",
            city: "Madrid",
            province: "Madrid",
            country: "España",
            postalCode: 28013
        }
    },
    cartLines: [
        {
            quantity: 1,
            productId: "Hx9Lp2Ks4TnB",
            productPrice: 699.99,
            status: "EN_CARRITO",
            totalPrice: 699.99
        },
        {
            quantity: 2,
            productId: "Fp2Jk7Xm4YzT",
            productPrice: 29.99,
            status: "EN_CARRITO",
            totalPrice: 59.98
        }
    ],
    totalItems: 2,
    total: 759.97,
    createdAt: new Date(),
    updatedAt: new Date()
});

// Pedido histórico completado de John Doe (userId: 3)
db.cart.insertOne({
    _id: ObjectId(),
    userId: NumberLong(3),
    purchased: true,
    client: {
        name: "John Doe",
        email: "john.doe@email.com",
        phone: "+34666333444",
        address: {
            number: 42,
            street: "Gran Vía",
            city: "Madrid",
            province: "Madrid",
            country: "España",
            postalCode: 28013
        }
    },
    cartLines: [
        {
            quantity: 2,
            productId: "Dk5Mn8Pj2WcX",
            productPrice: 19.99,
            status: "RECIBIDO",
            totalPrice: 39.98
        },
        {
            quantity: 1,
            productId: "Gn7Qs4Lv8BxZ",
            productPrice: 22.99,
            status: "RECIBIDO",
            totalPrice: 22.99
        }
    ],
    totalItems: 2,
    total: 62.97,
    createdAt: new Date(Date.now() - 45 * 24 * 60 * 60 * 1000),
    updatedAt: new Date(Date.now() - 35 * 24 * 60 * 60 * 1000)
});

// Carrito activo de Jane Smith (userId: 4 - USER)
db.cart.insertOne({
    _id: ObjectId(),
    userId: NumberLong(4),
    purchased: false,
    client: {
        name: "Jane Smith",
        email: "jane.smith@email.com",
        phone: "+34666555666",
        address: {
            number: 15,
            street: "Paseo de Gracia",
            city: "Barcelona",
            province: "Barcelona",
            country: "España",
            postalCode: 8007
        }
    },
    cartLines: [
        {
            quantity: 1,
            productId: "Yw3Zq7Vm1RfG",
            productPrice: 1099.99,
            status: "EN_CARRITO",
            totalPrice: 1099.99
        },
        {
            quantity: 1,
            productId: "Qs1Zw8Ty3NlJ",
            productPrice: 59.99,
            status: "EN_CARRITO",
            totalPrice: 59.99
        }
    ],
    totalItems: 2,
    total: 1159.98,
    createdAt: new Date(),
    updatedAt: new Date()
});

// Pedido histórico con artículo cancelado de Jane Smith (userId: 4)
db.cart.insertOne({
    _id: ObjectId(),
    userId: NumberLong(4),
    purchased: true,
    client: {
        name: "Jane Smith",
        email: "jane.smith@email. com",
        phone: "+34666555666",
        address: {
            number: 15,
            street: "Paseo de Gracia",
            city: "Barcelona",
            province: "Barcelona",
            country: "España",
            postalCode: 8007
        }
    },
    cartLines: [
        {
            quantity: 1,
            productId: "Vb4Gx9Hs6MqK",
            productPrice: 449.99,
            status: "CANCELADO",
            totalPrice: 449.99
        }
    ],
    totalItems: 1,
    total: 449.99,
    createdAt: new Date(Date.now() - 15 * 24 * 60 * 60 * 1000),
    updatedAt: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000)
});

// Carrito activo de Carlos Ruiz (userId: 5 - USER)
db.cart.insertOne({
    _id: ObjectId(),
    userId: NumberLong(5),
    purchased: false,
    client: {
        name: "Carlos Ruiz",
        email: "carlos.ruiz@email.com",
        phone: "+34666777888",
        address: {
            number: 8,
            street: "Calle Larios",
            city: "Málaga",
            province: "Málaga",
            country: "España",
            postalCode: 29015
        }
    },
    cartLines: [
        {
            quantity: 1,
            productId: "Mx7Pk2Vn5RbD",
            productPrice: 599.99,
            status: "EN_CARRITO",
            totalPrice: 599.99
        }
    ],
    totalItems: 1,
    total: 599.99,
    createdAt: new Date(),
    updatedAt: new Date()
});

// Pedido histórico enviado de Carlos Ruiz (userId: 5)
db.cart.insertOne({
    _id: ObjectId(),
    userId: NumberLong(5),
    purchased: true,
    client: {
        name: "Carlos Ruiz",
        email: "carlos. ruiz@email.com",
        phone: "+34666777888",
        address: {
            number: 8,
            street: "Calle Larios",
            city: "Málaga",
            province: "Málaga",
            country: "España",
            postalCode: 29015
        }
    },
    cartLines: [
        {
            quantity: 3,
            productId: "Rt6Bv9Nh3QsL",
            productPrice: 15.99,
            status: "ENVIADO",
            totalPrice: 47.97
        },
        {
            quantity: 2,
            productId: "Fp2Jk7Xm4YzT",
            productPrice: 29.99,
            status: "ENVIADO",
            totalPrice: 59.98
        }
    ],
    totalItems: 2,
    total: 107.95,
    createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
    updatedAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000)
});

// Carrito activo de María García (userId: 6 - USER)
db.cart.insertOne({
    _id: ObjectId(),
    userId: NumberLong(6),
    purchased: false,
    client: {
        name: "María García",
        email: "maria.garcia@email.com",
        phone: "+34666999000",
        address: {
            number: 23,
            street: "Calle Sierpes",
            city: "Sevilla",
            province: "Sevilla",
            country: "España",
            postalCode: 41004
        }
    },
    cartLines: [
        {
            quantity: 1,
            productId: "Ln8Cv5Dt1WpR",
            productPrice: 89.99,
            status: "EN_CARRITO",
            totalPrice: 89.99
        },
        {
            quantity: 2,
            productId: "Jt3Lw6Fh9CmY",
            productPrice: 34.99,
            status: "EN_CARRITO",
            totalPrice: 69.98
        }
    ],
    totalItems: 2,
    total: 159.97,
    createdAt: new Date(),
    updatedAt: new Date()
});

// Pedido histórico recibido de María García (userId:  6)
db.cart.insertOne({
    _id: ObjectId(),
    userId: NumberLong(6),
    purchased: true,
    client: {
        name: "María García",
        email: "maria.garcia@email.com",
        phone: "+34666999000",
        address: {
            number: 23,
            street: "Calle Sierpes",
            city: "Sevilla",
            province: "Sevilla",
            country: "España",
            postalCode: 41004
        }
    },
    cartLines: [
        {
            quantity: 1,
            productId: "Vb4Gx9Hs6MqK",
            productPrice: 449.99,
            status: "RECIBIDO",
            totalPrice: 449.99
        }
    ],
    totalItems: 1,
    total: 449.99,
    createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
    updatedAt: new Date(Date.now() - 20 * 24 * 60 * 60 * 1000)
});

// Carrito activo de Pedro López (userId: 7 - USER) - VACÍO
db.cart.insertOne({
    _id: ObjectId(),
    userId: NumberLong(7),
    purchased: false,
    client: {
        name: "Pedro López",
        email: "pedro.lopez@email.com",
        phone: "+34666000111",
        address: {
            number: 50,
            street: "Calle Mayor",
            city: "Valencia",
            province: "Valencia",
            country: "España",
            postalCode: 46001
        }
    },
    cartLines: [],
    totalItems: 0,
    total: 0.0,
    createdAt: new Date(),
    updatedAt: new Date()
});

// Pedido histórico en preparación de Pedro López (userId: 7)
db.cart.insertOne({
    _id: ObjectId(),
    userId: NumberLong(7),
    purchased: true,
    client: {
        name: "Pedro López",
        email: "pedro.lopez@email.com",
        phone: "+34666000111",
        address: {
            number: 50,
            street: "Calle Mayor",
            city: "Valencia",
            province: "Valencia",
            country: "España",
            postalCode: 46001
        }
    },
    cartLines: [
        {
            quantity: 1,
            productId: "Tx5Wr9Km2NhP",
            productPrice: 1199.99,
            status: "PREPARADO",
            totalPrice: 1199.99
        }
    ],
    totalItems: 1,
    total: 1199.99,
    createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
    updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000)
});

// Crear índices para mejorar el rendimiento
db.cart.createIndex({"userId": 1});
db.cart.createIndex({"purchased": 1});
db.cart.createIndex({"createdAt": -1});
db.cart.createIndex({"userId": 1, "purchased": 1});

// Validación y estadísticas
print("\nColección 'cart' creada e inicializada correctamente");
print("═══════════════════════════════════════════════════════");
print("Total de documentos de carrito: " + db.cart.countDocuments());
print("Carritos activos (purchased:  false): " + db.cart.countDocuments({purchased: false}));
print("Pedidos completados (purchased: true): " + db.cart.countDocuments({purchased: true}));
print("═══════════════════════════════════════════════════════");
print("\nCARRITOS POR USUARIO:");
print("   - John Doe (userId: 3): " + db.cart.countDocuments({userId: NumberLong(3)}) + " carritos (1 activo, " + (db.cart.countDocuments({userId: NumberLong(3)}) - 1) + " históricos)");
print("   - Jane Smith (userId: 4): " + db.cart.countDocuments({userId: NumberLong(4)}) + " carritos (1 activo, " + (db.cart.countDocuments({userId: NumberLong(4)}) - 1) + " históricos)");
print("   - Carlos Ruiz (userId: 5): " + db.cart.countDocuments({userId: NumberLong(5)}) + " carritos (1 activo, " + (db.cart.countDocuments({userId: NumberLong(5)}) - 1) + " históricos)");
print("   - María García (userId: 6): " + db.cart.countDocuments({userId: NumberLong(6)}) + " carritos (1 activo, " + (db.cart.countDocuments({userId: NumberLong(6)}) - 1) + " históricos)");
print("   - Pedro López (userId: 7): " + db.cart.countDocuments({userId: NumberLong(7)}) + " carritos (1 activo, " + (db.cart.countDocuments({userId: NumberLong(7)}) - 1) + " históricos)");
print("   - Admin (userId: 1): " + db.cart.countDocuments({userId: NumberLong(1)}) + " carritos (NO tiene carritos)");
print("   - Manager (userId: 2): " + db.cart.countDocuments({userId: NumberLong(2)}) + " carritos (NO tiene carritos)");
print("═══════════════════════════════════════════════════════\n");