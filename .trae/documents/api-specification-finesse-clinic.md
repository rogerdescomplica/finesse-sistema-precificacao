# Finesse Centro Integrado - Especificação de API

## 1. Visão Geral

A API do Finesse Centro Integrado segue os princípios RESTful e utiliza JSON para comunicação. Todas as requisições autenticadas requerem um token JWT no header Authorization.

**Base URL:** `https://api.finesseclinic.com/v1`

**Headers Padrão:**

```
Content-Type: application/json
Accept: application/json
Authorization: Bearer {jwt_token}
X-Clinic-ID: {clinic_id}
```

## 2. Autenticação

### 2.1 Login

**POST** `/auth/login`

**Requisição:**

```json
{
  "email": "usuario@clinica.com",
  "senha": "senha123",
  "clinicaId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Resposta (200 OK):**

```json
{
  "sucesso": true,
  "dados": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tipoToken": "Bearer",
    "expiraEm": 3600,
    "usuario": {
      "id": "660e8400-e29b-41d4-a716-446655440000",
      "nome": "João Silva",
      "email": "usuario@clinica.com",
      "papel": "PROPRIETARIO",
      "permissoes": ["LEITURA", "ESCRITA", "EXCLUSAO", "ADMIN"],
      "clinica": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "nome": "Clínica Finesse",
        "cnpj": "12.345.678/0001-90"
      }
    }
  }
}
```

**Resposta de Erro (401 Não Autorizado):**

```json
{
  "sucesso": false,
  "erro": {
    "codigo": "CREDENCIAIS_INVALIDAS",
    "mensagem": "Email ou senha inválidos",
    "detalhes": null
  }
}
```

### 2.2 Refresh Token

**POST** `/auth/refresh`

**Request:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

### 2.3 Logout

**POST** `/auth/logout`

**Request:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Logout realizado com sucesso"
}
```

## 3. Gestão de Usuários

### 3.1 Listar Usuários

**GET** `/users`

**Query Parameters:**

* `page` (integer, optional): Página atual (default: 1)

* `limit` (integer, optional): Itens por página (default: 20, max: 100)

* `role` (string, optional): Filtrar por role (OWNER, ADMIN, EMPLOYEE)

* `active` (boolean, optional): Filtrar por status

* `search` (string, optional): Buscar por nome ou email

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "users": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440000",
        "name": "João Silva",
        "email": "joao@clinica.com",
        "role": "OWNER",
        "active": true,
        "lastLogin": "2024-01-15T10:30:00Z",
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 5,
      "pages": 1
    }
  }
}
```

### 3.2 Criar Usuário

**POST** `/users`

**Request:**

```json
{
  "name": "Maria Santos",
  "email": "maria@clinica.com",
  "password": "senha123",
  "role": "ADMIN",
  "active": true
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "data": {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "name": "Maria Santos",
    "email": "maria@clinica.com",
    "role": "ADMIN",
    "active": true,
    "createdAt": "2024-01-15T14:30:00Z"
  }
}
```

### 3.3 Atualizar Usuário

**PUT** `/users/{id}`

**Request:**

```json
{
  "name": "Maria Santos Silva",
  "role": "ADMIN",
  "active": true
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "name": "Maria Santos Silva",
    "email": "maria@clinica.com",
    "role": "ADMIN",
    "active": true,
    "updatedAt": "2024-01-15T15:00:00Z"
  }
}
```

### 3.4 Deletar Usuário

**DELETE** `/users/{id}`

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Usuário deletado com sucesso"
}
```

## 4. Gestão de Serviços

### 4.1 Listar Serviços

**GET** `/servicos`

**Query Parameters:**

* `page` (integer, optional): Página atual

* `limit` (integer, optional): Itens por página

* `category` (string, optional): Filtrar por categoria

* `active` (boolean, optional): Filtrar por status

* `search` (string, optional): Buscar por nome ou descrição

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "services": [
      {
        "id": "880e8400-e29b-41d4-a716-446655440000",
        "name": "Limpeza de Pele",
        "description": "Limpeza profunda da pele com extração de cravos",
        "duration": 60,
        "category": "ESTHETICS",
        "basePrice": 120.00,
        "suggestedPrice": 150.00,
        "cnae": {
          "id": "990e8400-e29b-41d4-a716-446655440000",
          "code": "9609-2/01",
          "description": "Estética e atividades de complementação estética"
        },
        "materials": [
          {
            "id": "aa0e8400-e29b-41d4-a716-446655440000",
            "name": "Ácido Glicólico",
            "quantity": 5.0,
            "unit": "ml",
            "unitCost": 8.50
          }
        ],
        "active": true,
        "createdAt": "2024-01-10T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 15,
      "pages": 1
    }
  }
}
```

### 4.2 Criar Serviço

**POST** `/servicos`

**Request:**

```json
{
  "name": "Peeling Químico",
  "description": "Peeling químico com ácido glicólico",
  "duration": 45,
  "category": "ESTHETICS",
  "cnaeId": "990e8400-e29b-41d4-a716-446655440000",
  "materials": [
    {
      "materialId": "aa0e8400-e29b-41d4-a716-446655440000",
      "quantity": 3.0,
      "unit": "ml",
      "wastePercentage": 5.0
    }
  ]
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "data": {
    "id": "bb0e8400-e29b-41d4-a716-446655440000",
    "name": "Peeling Químico",
    "description": "Peeling químico com ácido glicólico",
    "duration": 45,
    "category": "ESTHETICS",
    "basePrice": null,
    "suggestedPrice": null,
    "cnae": {
      "id": "990e8400-e29b-41d4-a716-446655440000",
      "code": "9609-2/01",
      "description": "Estética e atividades de complementação estética"
    },
    "materials": [
      {
        "id": "aa0e8400-e29b-41d4-a716-446655440000",
        "name": "Ácido Glicólico",
        "quantity": 3.0,
        "unit": "ml",
        "unitCost": 8.50
      }
    ],
    "active": true,
    "createdAt": "2024-01-15T16:00:00Z"
  }
}
```

### 4.3 Calcular Preço de Serviço

**POST** `/servicos/{id}/calcular-preco`

**Request:**

```json
{
  "indirectCostPerHour": 25.00,
  "desiredMargin": 35.0,
  "cnaeId": "990e8400-e29b-41d4-a716-446655440000"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "serviceId": "bb0e8400-e29b-41d4-a716-446655440000",
    "directCost": 26.78,
    "indirectCost": 18.75,
    "totalCost": 45.53,
    "markupPercentage": 35.0,
    "markupValue": 15.94,
    "taxBreakdown": {
      "iss": 2.28,
      "pis": 0.30,
      "cofins": 1.37,
      "irpj": 0.68,
      "csll": 0.46,
      "total": 5.09
    },
    "suggestedPrice": 66.56,
    "profitMargin": 35.0,
    "calculationDate": "2024-01-15T16:30:00Z"
  }
}
```

### 4.4 Atualizar Serviço

**PUT** `/servicos/{id}`

**Request:**

```json
{
  "name": "Peeling Químico Premium",
  "description": "Peeling químico com ácido glicólico 70%",
  "duration": 50,
  "category": "ESTHETICS",
  "basePrice": 120.00,
  "active": true
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "id": "bb0e8400-e29b-41d4-a716-446655440000",
    "name": "Peeling Químico Premium",
    "description": "Peeling químico com ácido glicólico 70%",
    "duration": 50,
    "category": "ESTHETICS",
    "basePrice": 120.00,
    "suggestedPrice": 150.00,
    "active": true,
    "updatedAt": "2024-01-15T17:00:00Z"
  }
}
```

### 4.5 Deletar Serviço

**DELETE** `/servicos/{id}`

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Serviço deletado com sucesso"
}
```

## 5. Gestão de Materiais

### 5.1 Listar Materiais

**GET** `/materiais`

**Query Parameters:**

* `page` (integer, optional): Página atual

* `limit` (integer, optional): Itens por página

* `category` (string, optional): Filtrar por categoria

* `stockStatus` (string, optional): Filtrar por status (NORMAL, LOW\_STOCK, OUT\_OF\_STOCK, OVERSTOCK)

* `supplierId` (uuid, optional): Filtrar por fornecedor

* `search` (string, optional): Buscar por nome ou SKU

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "materials": [
      {
        "id": "aa0e8400-e29b-41d4-a716-446655440000",
        "sku": "ACG-001",
        "name": "Ácido Glicólico",
        "description": "Ácido glicólico 70% para peeling",
        "unit": "ml",
        "unitCost": 8.50,
        "currentStock": 150,
        "minimumStock": 50,
        "maximumStock": 200,
        "stockStatus": "NORMAL",
        "supplier": {
          "id": "cc0e8400-e29b-41d4-a716-446655440000",
          "name": "Laboratório ABC",
          "cnpj": "12.345.678/0001-90"
        },
        "category": "ACIDS",
        "totalValue": 1275.00,
        "active": true,
        "createdAt": "2024-01-05T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 45,
      "pages": 3
    }
  }
}
```

### 5.2 Criar Material

**POST** `/materiais`

**Request:**

```json
{
  "sku": "COL-001",
  "name": "Colágeno Hidrolisado",
  "description": "Colágeno hidrolisado para máscaras faciais",
  "unit": "g",
  "unitCost": 12.50,
  "currentStock": 100,
  "minimumStock": 30,
  "maximumStock": 150,
  "supplierId": "cc0e8400-e29b-41d4-a716-446655440000",
  "category": "PROTEINS",
  "barcode": "7891234567890",
  "location": "A1-P3",
  "expirationDate": "2025-12-31"
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "data": {
    "id": "dd0e8400-e29b-41d4-a716-446655440000",
    "sku": "COL-001",
    "name": "Colágeno Hidrolisado",
    "description": "Colágeno hidrolisado para máscaras faciais",
    "unit": "g",
    "unitCost": 12.50,
    "currentStock": 100,
    "minimumStock": 30,
    "maximumStock": 150,
    "stockStatus": "NORMAL",
    "supplier": {
      "id": "cc0e8400-e29b-41d4-a716-446655440000",
      "name": "Laboratório ABC",
      "cnpj": "12.345.678/0001-90"
    },
    "category": "PROTEINS",
    "barcode": "7891234567890",
    "location": "A1-P3",
    "expirationDate": "2025-12-31",
    "totalValue": 1250.00,
    "active": true,
    "createdAt": "2024-01-15T18:00:00Z"
  }
}
```

### 5.3 Atualizar Estoque

**PUT** `/materiais/{id}/estoque`

**Request:**

```json
{
  "movementType": "IN",
  "quantity": 50,
  "unitCost": 12.50,
  "reason": "Compra de reposição",
  "referenceId": "ee0e8400-e29b-41d4-a716-446655440000",
  "referenceType": "PURCHASE_ORDER"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "id": "dd0e8400-e29b-41d4-a716-446655440000",
    "currentStock": 150,
    "movement": {
      "id": "ff0e8400-e29b-41d4-a716-446655440000",
      "type": "IN",
      "quantity": 50,
      "unitCost": 12.50,
      "totalCost": 625.00,
      "reason": "Compra de reposição",
      "createdAt": "2024-01-15T18:30:00Z"
    }
  }
}
```

### 5.4 Listar Movimentações de Estoque

**GET** `/materiais/{id}/movimentacoes`

**Query Parameters:**

* `page` (integer, optional): Página atual

* `limit` (integer, optional): Itens por página

* `movementType` (string, optional): Filtrar por tipo (IN, OUT, ADJUSTMENT, etc.)

* `startDate` (date, optional): Data inicial

* `endDate` (date, optional): Data final

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "movements": [
      {
        "id": "ff0e8400-e29b-41d4-a716-446655440000",
        "type": "IN",
        "quantity": 50,
        "unitCost": 12.50,
        "totalCost": 625.00,
        "reason": "Compra de reposição",
        "referenceId": "ee0e8400-e29b-41d4-a716-446655440000",
        "referenceType": "PURCHASE_ORDER",
        "createdBy": {
          "id": "660e8400-e29b-41d4-a716-446655440000",
          "name": "João Silva"
        },
        "createdAt": "2024-01-15T18:30:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 25,
      "pages": 2
    }
  }
}
```

## 6. Gestão de Fornecedores

### 6.1 Listar Fornecedores

**GET** `/fornecedores`

**Query Parameters:**

* `page` (integer, optional): Página atual

* `limit` (integer, optional): Itens por página

* `active` (boolean, optional): Filtrar por status

* `search` (string, optional): Buscar por nome, CNPJ ou CPF

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "suppliers": [
      {
        "id": "cc0e8400-e29b-41d4-a716-446655440000",
        "name": "Laboratório ABC",
        "cnpj": "12.345.678/0001-90",
        "email": "contato@lababc.com",
        "phone": "(11) 1234-5678",
        "address": "Rua das Flores, 123",
        "city": "São Paulo",
        "state": "SP",
        "zipCode": "01234-567",
        "paymentTerms": 30,
        "deliveryTime": 7,
        "active": true,
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 10,
      "pages": 1
    }
  }
}
```

### 6.2 Criar Fornecedor

**POST** `/fornecedores`

**Request:**

```json
{
  "name": "Distribuidora XYZ",
  "cnpj": "98.765.432/0001-90",
  "email": "vendas@xyz.com.br",
  "phone": "(11) 9876-5432",
  "address": "Av. Paulista, 1000",
  "city": "São Paulo",
  "state": "SP",
  "zipCode": "01310-100",
  "paymentTerms": 45,
  "deliveryTime": 10
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "data": {
    "id": "gg0e8400-e29b-41d4-a716-446655440000",
    "name": "Distribuidora XYZ",
    "cnpj": "98.765.432/0001-90",
    "email": "vendas@xyz.com.br",
    "phone": "(11) 9876-5432",
    "address": "Av. Paulista, 1000",
    "city": "São Paulo",
    "state": "SP",
    "zipCode": "01310-100",
    "paymentTerms": 45,
    "deliveryTime": 10,
    "active": true,
    "createdAt": "2024-01-15T19:00:00Z"
  }
}
```

## 7. Relatórios Financeiros

### 7.1 Dashboard Financeiro

**GET** `/financeiro/painel`

**Query Parameters:**

* `startDate` (date, optional): Data inicial (default: primeiro dia do mês)

* `endDate` (date, optional): Data final (default: hoje)

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "period": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-31"
    },
    "summary": {
      "totalIncome": 45000.00,
      "totalExpense": 28000.00,
      "netResult": 17000.00,
      "profitMargin": 37.78
    },
    "incomeByCategory": {
      "SERVICE": 42000.00,
      "OTHER": 3000.00
    },
    "expenseByCategory": {
      "MATERIAL": 15000.00,
      "PAYROLL": 8000.00,
      "RENT": 3000.00,
      "UTILITIES": 1200.00,
      "MARKETING": 800.00,
      "OTHER": 0.00
    },
    "topServices": [
      {
        "serviceId": "880e8400-e29b-41d4-a716-446655440000",
        "serviceName": "Limpeza de Pele",
        "revenue": 15000.00,
        "quantity": 100
      }
    ],
    "monthlyTrend": [
      {
        "month": "2024-01",
        "income": 45000.00,
        "expense": 28000.00,
        "profit": 17000.00
      }
    ]
  }
}
```

### 7.2 Demonstração de Resultado (DRE)

**GET** `/financeiro/dre`

**Query Parameters:**

* `startDate` (date, required): Data inicial

* `endDate` (date, required): Data final

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "period": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-31"
    },
    "dre": {
      "grossRevenue": 45000.00,
      "deductions": {
        "taxes": 6750.00,
        "returns": 0.00,
        "discounts": 500.00
      },
      "netRevenue": 37750.00,
      "costOfGoodsSold": 15000.00,
      "grossProfit": 22750.00,
      "operatingExpenses": {
        "personnel": 8000.00,
        "rent": 3000.00,
        "utilities": 1200.00,
        "marketing": 800.00,
        "administrative": 2000.00,
        "total": 15000.00
      },
      "operatingProfit": 7750.00,
      "financialIncome": 0.00,
      "financialExpenses": 500.00,
      "profitBeforeTax": 7250.00,
      "incomeTax": 1812.50,
      "netProfit": 5437.50,
      "profitMargin": 12.08
    }
  }
}
```

### 7.3 Análise de Rentabilidade por Serviço

**GET** `/financeiro/rentabilidade-servicos`

**Query Parameters:**

* `startDate` (date, required): Data inicial

* `endDate` (date, required): Data final

* `serviceId` (uuid, optional): Filtrar por serviço específico

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "services": [
      {
        "serviceId": "880e8400-e29b-41d4-a716-446655440000",
        "serviceName": "Limpeza de Pele",
        "totalRevenue": 15000.00,
        "totalCost": 8500.00,
        "grossProfit": 6500.00,
        "profitMargin": 43.33,
        "quantitySold": 100,
        "averagePrice": 150.00,
        "averageCost": 85.00,
        "contributionMargin": 43.33
      }
    ],
    "summary": {
      "totalRevenue": 45000.00,
      "totalCost": 23500.00,
      "totalProfit": 21500.00,
      "averageProfitMargin": 47.78
    }
  }
}
```

## 8. Configuração Fiscal (CNAE)

### 8.1 Listar CNAE

**GET** `/cnae`

**Query Parameters:**

* `active` (boolean, optional): Filtrar por status ativo

* `search` (string, optional): Buscar por código ou descrição

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "cnae": [
      {
        "id": "990e8400-e29b-41d4-a716-446655440000",
        "code": "9609-2/01",
        "description": "Estética e atividades de complementação estética",
        "taxRates": {
          "iss": 5.0,
          "pis": 0.65,
          "cofins": 3.0,
          "irpj": 1.5,
          "csll": 1.0,
          "total": 11.15
        },
        "active": true
      }
    ]
  }
}
```

### 8.2 Configurar CNAE para Clínica

**POST** `/cnae/configuracao-clinica`

**Request:**

```json
{
  "cnaeId": "990e8400-e29b-41d4-a716-446655440000",
  "customIssRate": 4.5,
  "customPisRate": 0.65,
  "customCofinsRate": 3.0,
  "customIrpjRate": 1.5,
  "customCsllRate": 1.0,
  "effectiveDate": "2024-01-01"
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "data": {
    "id": "hh0e8400-e29b-41d4-a716-446655440000",
    "clinicId": "550e8400-e29b-41d4-a716-446655440000",
    "cnae": {
      "id": "990e8400-e29b-41d4-a716-446655440000",
      "code": "9609-2/01",
      "description": "Estética e atividades de complementação estética"
    },
    "customRates": {
      "iss": 4.5,
      "pis": 0.65,
      "cofins": 3.0,
      "irpj": 1.5,
      "csll": 1.0,
      "total": 10.65
    },
    "effectiveDate": "2024-01-01",
    "active": true,
    "createdAt": "2024-01-15T20:00:00Z"
  }
}
```

## 9. Exportação de Relatórios

### 9.1 Exportar Relatório Financeiro

**POST** `/relatorios/exportar`

**Request:**

```json
{
  "reportType": "FINANCIAL_DRE",
  "format": "PDF",
  "parameters": {
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "includeCharts": true,
    "language": "pt-BR"
  }
}
```

**Response (202 Accepted):**

```json
{
  "success": true,
  "data": {
    "exportId": "ii0e8400-e29b-41d4-a716-446655440000",
    "status": "PROCESSING",
    "estimatedTime": 30,
    "downloadUrl": null
  }
}
```

### 9.2 Verificar Status de Exportação

**GET** `/relatorios/exportar/{exportId}/status`

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "exportId": "ii0e8400-e29b-41d4-a716-446655440000",
    "status": "COMPLETED",
    "progress": 100,
    "downloadUrl": "https://api.finesseclinic.com/v1/reports/export/ii0e8400-e29b-41d4-a716-446655440000/download",
    "expiresAt": "2024-01-16T20:00:00Z"
  }
}
```

## 10. Estrutura de Erros

### 10.1 Formato Padrão de Erro

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Mensagem de erro para o usuário",
    "details": {
      "field": "campo_com_erro",
      "reason": "razão_do_erro"
    }
  }
}
```

### 10.2 Códigos de Erro Comuns

| Código                        | Descrição                      | HTTP Status |
| ----------------------------- | ------------------------------ | ----------- |
| CREDENCIAIS\_INVALIDAS        | Email ou senha inválidos       | 401         |
| NAO\_AUTORIZADO               | Token inválido ou expirado     | 401         |
| ACESSO\_NEGADO                | Acesso não autorizado          | 403         |
| RECURSO\_NAO\_ENCONTRADO      | Recurso não encontrado         | 404         |
| ERRO\_VALIDACAO               | Erro de validação dos dados    | 400         |
| VIOLACAO\_REGRA\_NEGOCIO      | Violação de regra de negócio   | 400         |
| RECURSO\_DUPLICADO            | Recurso duplicado              | 409         |
| ESTOQUE\_INSUFICIENTE         | Estoque insuficiente           | 400         |
| CNAE\_INVALIDO                | CNAE inválido ou inativo       | 400         |
| ERRO\_CALCULO                 | Erro no cálculo                | 400         |
| ERRO\_BANCO\_DADOS            | Erro de banco de dados         | 500         |
| ERRO\_SERVICO\_EXTERNO        | Erro em serviço externo        | 503         |
| LIMITE\_REQUISICOES\_EXCEDIDO | Limite de requisições excedido | 429         |

### 10.3 Exemplos de Erros de Validação

**Erro 400 - Requisição Inválida:**

```json
{
  "sucesso": false,
  "erro": {
    "codigo": "ERRO_VALIDACAO",
    "mensagem": "Erro de validação nos dados enviados",
    "detalhes": [
      {
        "campo": "email",
        "mensagem": "Email inválido",
        "valor": "email_invalido"
      },
      {
        "campo": "senha",
        "mensagem": "A senha deve ter pelo menos 8 caracteres",
        "valor": "123"
      }
    ]
  }
}
```

**Erro 409 - Conflito:**

```json
{
  "sucesso": false,
  "erro": {
    "codigo": "RECURSO_DUPLICADO",
    "mensagem": "Já existe um usuário com este email",
    "detalhes": {
      "campo": "email",
      "valor": "usuario@clinica.com"
    }
  }
}
```

## 11. Paginação

### 11.1 Formato de Resposta Paginada

```json
{
  "success": true,
  "data": {
    "items": [...],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 150,
      "pages": 8,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

### 11.2 Parâmetros de Paginação

* `page` (integer): Número da página (começa em 1)

* `limit` (integer): Itens por página (máximo 100)

* `sort` (string): Campo para ordenação (ex: "name", "-createdAt" para descendente)

* `search` (string): Termo de busca

## 12. Rate Limiting

### 12.1 Limites por Endpoint

* **Autenticação:** 10 requisições por minuto

* **CRUD básico:** 100 requisições por minuto

* **Relatórios:** 5 requisições por minuto

* **Exportações:** 2 requisições por hora

### 12.2 Headers de Rate Limit

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640995200
X-RateLimit-Reset-After: 300
```

## 13. Versionamento

A API utiliza versionamento semântico no URL:

* Versão atual: `/v1`

* Headers de versionamento: `API-Version: 1.0.0`

* Deprecation warnings: `Sunset: Sat, 31 Dec 2024 23:59:59 GMT`

## 14. Webhooks (Futuro)

### 14.1 Eventos Disponíveis

* `service.created` - Novo serviço criado

* `service.updated` - Serviço atualizado

* `material.stock_low` - Estoque baixo

* `financial.transaction.created` - Nova transação financeira

* `report.generated` - Relatório gerado

### 14.2 Formato do Webhook

```json
{
  "event": "service.created",
  "timestamp": "2024-01-15T20:00:00Z",
  "data": {
    "serviceId": "bb0e8400-e29b-41d4-a716-446655440000",
    "clinicId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

