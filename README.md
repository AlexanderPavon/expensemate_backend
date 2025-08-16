# 💸 ExpenseMate

**ExpenseMate** es una aplicación de gestión financiera personal que permite a los usuarios registrar, consultar y organizar sus movimientos económicos como ingresos, egresos, tarjetas de crédito, cuentas bancarias y categorías de gasto.

---

## 🚀 Tecnologías utilizadas

- Kotlin + Spring Boot
- PostgreSQL (usando Docker)
- JPA / Hibernate
- Postman para pruebas de API
- Gradle (gestión de dependencias)

---

## 📁 Estructura del proyecto

```
├── controllers         # Lógica de control de endpoints
├── services            # Lógica de negocio
├── repositories        # Acceso a datos (JPA)
├── models
│   ├── entities        # Entidades JPA
│   ├── requests        # DTOs de entrada
│   └── responses       # DTOs de salida
├── mappers             # Conversión entre entidad y DTO
├── routes              # Rutas centralizadas
├── exceptions          # Manejo de errores personalizados
└── application.yml     # Configuración del proyecto
```

---

## 🧪 Endpoints principales

| Recurso         | Método | Ruta                                                                   | Descripción                                 |
|-----------------|--------|------------------------------------------------------------------------|---------------------------------------------|
| **Usuarios**    | GET    | `/api/expensemate/users`                                               | Obtener todos los usuarios                  |
|                 | GET    | `/api/expensemate/users/{id}`                                          | Obtener un usuario por ID                   |
|                 | GET    | `/api/expensemate/users/email/{email}`                                 | Obtener un usuario por EMAIL                |
|                 | GET    | `/api/expensemate/users/{id}}/summary`                                 | Obtener el resumen de un usuario por ID     |
|                 | POST   | `/api/expensemate/users`                                               | Crear un nuevo usuario                      |
|                 | PUT    | `/api/expensemate/users/{id}`                                          | Actualizar usuario por ID                   |
|                 | DELETE | `/api/expensemate/users/{id}`                                          | Eliminar usuario por ID                     |
| **Movimientos** | GET    | `/api/expensemate/movements`                                           | Obtener todos los movimientos               |
|                 | GET    | `/api/expensemate/movements/{id}`                                      | Obtener un movimiento por ID                |
|                 | GET    | `/api/expensemate/movements/by-user/{userId}`                          | Obtener movimientos por USERID              |
|                 | GET    | `/api/expensemate/movements/by-user/{userId}/by-category/{categoryId}` | Obtener movimientos por USERID y CATEGORYID |
|                 | POST   | `/api/expensemate/movements`                                           | Registrar un nuevo movimiento               |
|                 | PUT    | `/api/expensemate/movements/{id}`                                      | Actualizar movimiento por ID                |
|                 | DELETE | `/api/expensemate/movements/{id}`                                      | Eliminar movimiento por ID                  |
| **Categorías**  | GET    | `/api/expensemate/categories`                                          | Obtener todas las categorías                |
|                 | GET    | `/api/expensemate/categories/{id}`                                     | Obtener una categoría por ID                |
|                 | POST   | `/api/expensemate/categories`                                          | Crear una nueva categoría                   |
|                 | PUT    | `/api/expensemate/categories/{id}`                                     | Actualizar categoría por ID                 |
|                 | DELETE | `/api/expensemate/categories/{id}`                                     | Eliminar categoría por ID                   |
| **Tarjetas**    | GET    | `/api/expensemate/credit-cards`                                        | Obtener todas las tarjetas de crédito       |
| de Crédito      | GET    | `/api/expensemate/credit-cards/{id}`                                   | Obtener una tarjeta de crédito por ID       |
|                 | GET    | `/api/expensemate/credit-cards/by-user/{userId}`                       | Obtener tarjetas de crédito por USERID      |
|                 | POST   | `/api/expensemate/credit-cards`                                        | Registrar nueva tarjeta de crédito          |
|                 | PUT    | `/api/expensemate/credit-cards/{id}`                                   | Actualizar tarjeta de crédito por ID        |
|                 | DELETE | `/api/expensemate/credit-cards/{id}`                                   | Eliminar tarjeta de crédito por ID          |
| **Cuentas**     | GET    | `/api/expensemate/accounts`                                            | Obtener todas las cuentas bancarias         |
| Bancarias       | GET    | `/api/expensemate/accounts/{id}`                                       | Obtener una cuenta bancaria por ID          |
|                 | GET    | `/api/expensemate/accounts/by-user/{userId}`                           | Obtener cuentas bancarias por USERID        |
|                 | POST   | `/api/expensemate/accounts`                                            | Crear una nueva cuenta bancaria             |
|                 | PUT    | `/api/expensemate/accounts/{id}`                                       | Actualizar cuenta bancaria por ID           |
|                 | DELETE | `/api/expensemate/accounts/{id}`                                       | Eliminar cuenta bancaria por ID             |

---

## Paso 1: Clonar el repositorio

Clona este repositorio en tu máquina local con el siguiente comando:

```bash
git clone https://github.com/AlexanderPavon/expensemate_backend.git
```

Luego navega a la carpeta del proyecto:

```bash
cd expensemate_backend
```

---

## Paso 2: Configuración de la base de datos

El proyecto usa PostgreSQL dentro de un contenedor Docker. Puedes levantar la base de datos con:

```bash
docker-compose up -d
```
o para visualizar los logs

```bash
docker-compose up
```

---

## Paso 3: Cómo ejecutar el proyecto

```bash
./gradlew bootRun
```

---

## Paso 4: Pruebas con Postman

Dentro del proyecto se incluye el archivo `Expensemate test.postman_collection.json`, el cual contiene las peticiones necesarias para probar los principales endpoints del sistema.

Puedes importarlo en Postman para facilitar el proceso de pruebas.
