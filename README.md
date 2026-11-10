# ARI API

API REST do projeto ARI, desenvolvida com Spring Boot, Java 21, Spring MVC,
Bean Validation, Spring Security e Springdoc OpenAPI.

## Índice

- [Executando a aplicação](#executando-a-aplicação)
- [Swagger e OpenAPI](#swagger-e-openapi)
- [Como ler a documentação](#como-ler-a-documentação)
- [Formato das requisições](#formato-das-requisições)
- [Autenticação e sessão](#autenticação-e-sessão)
- [Catálogo de endpoints](#catálogo-de-endpoints)
- [Modelos e validações](#modelos-e-validações)
- [Respostas e erros](#respostas-e-erros)
- [Fluxos recomendados](#fluxos-recomendados)
- [Estrutura do projeto](#estrutura-do-projeto)

## Executando a aplicação

### Pré-requisitos

- Java 21
- Maven (ou o Maven Wrapper incluído no projeto)
- Acesso a um banco Oracle
- As variáveis de ambiente de conexão:

```text
ORACLE_FIAP_URL
ORACLE_FIAP_USER
ORACLE_FIAP_PASSWORD
```

O nome da aplicação é `ari-api`. Sem uma configuração de porta diferente,
o Spring Boot utiliza a porta `8080`.

Na pasta `ari-api`, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

Ou, no Linux/macOS:

```bash
./mvnw spring-boot:run
```

Base URL local:

```text
http://localhost:8080
```

## Swagger e OpenAPI

### O que é cada recurso?

- **OpenAPI** é o contrato em JSON ou YAML que descreve a API: rotas, métodos,
  parâmetros, corpos, respostas e modelos.
- **Swagger UI** é a página visual que lê esse contrato e permite testar as
  requisições pelo navegador.
- **Springdoc** é a biblioteca que gera automaticamente o contrato OpenAPI a
  partir dos controllers, DTOs e validações do Spring.

Neste projeto, a dependência responsável por isso é
`springdoc-openapi-starter-webmvc-ui`.

### URLs disponíveis

Com a aplicação em execução, acesse:

| Recurso | URL |
|---|---|
| Swagger UI principal | http://localhost:8080/swagger-ui/index.html |
| Atalho da Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI em JSON | http://localhost:8080/v3/api-docs |
| OpenAPI em YAML | http://localhost:8080/v3/api-docs.yaml |

O JSON é útil para importar a API no Postman, Insomnia, Stoplight ou gerar
clientes automaticamente. A Swagger UI é a opção mais simples para explorar e
executar as operações manualmente.

### Como testar uma rota na Swagger UI

1. Abra `/swagger-ui/index.html`.
2. Expanda o grupo da operação.
3. Leia os parâmetros marcados como `path`, `query`, `header` ou `body`.
4. Clique em **Try it out**.
5. Preencha os valores sem incluir a máscara de CNPJ ou CEP quando a rota
   exigir somente números.
6. Clique em **Execute**.
7. Confira a URL enviada, o status HTTP, os headers e o corpo da resposta.

O botão **Schemas** mostra os DTOs usados nos corpos de entrada e saída.
Campos com restrições de validação devem ser preenchidos conforme a seção
[Modelos e validações](#modelos-e-validações).

### Importante sobre a documentação gerada

A Swagger UI é gerada a partir do código atual. Os controllers deste projeto
possuem tags e descrições de operação, os DTOs possuem descrições e exemplos
de campos/corpos. O header `X-Session-UUID` é documentado diretamente nas
operações que exigem sessão. Ao abrir uma operação, use **Try it out** para
editar os valores de exemplo antes de executar a requisição.

A seção **Available authorizations** não é exibida no Swagger, pois a sessão
deve ser informada manualmente no header de cada requisição.

## Como ler a documentação

Cada operação possui:

- **Método HTTP**: `GET` consulta, `POST` cria ou autentica, `PUT` substitui
  dados editáveis, `PATCH` altera apenas um estado ou ação específica e
  `DELETE` exclui/desativa.
- **Path**: parte fixa da URL, como `/api/usuarios/{id}`.
- **Path parameter**: valor entre `{}`; deve ser informado na URL.
- **Query parameter**: valor depois de `?`, como
  `/api/estabelecimentos/proximos?latitude=-23.56&longitude=-46.65`.
- **Header**: informação enviada fora do JSON. As rotas de sessão usam
  `X-Session-UUID`.
- **Request body**: JSON enviado em `POST`, `PUT` ou `PATCH`.
- **Response body**: JSON devolvido pela API, quando houver.

Todas as requisições JSON devem usar:

```http
Content-Type: application/json
```

## Autenticação e sessão

### Usuária

1. Crie a usuária com `POST /api/usuarios`.
2. Faça login em `POST /api/auth/usuarios/login`.
3. Guarde o campo `dsUuid` retornado.
4. Envie esse valor em todas as rotas de usuária que exigem sessão:

```http
X-Session-UUID: <dsUuid>
```

5. Encerre a sessão em `POST /api/usuarios/logout`.

Exemplo de login:

```json
{
  "dsEmail": "ana@example.com",
  "dsSenha": "senha-segura"
}
```

Resposta:

```json
{
  "dsUuid": "550e8400-e29b-41d4-a716-446655440000",
  "dtExpiracao": "2026-11-10T18:00:00"
}
```

### Funcionário

O login de funcionário é feito em `POST /api/funcionarios/login`.
As operações administrativas usam o `Principal` do Spring para identificar o
funcionário e o estabelecimento. O corpo retornado pelo login de funcionário
contém o funcionário e um UUID de sessão.

Depois do login, envie o campo `dsUuid` no header
`X-Funcionario-Session-UUID` nas operações administrativas:

```http
X-Funcionario-Session-UUID: <dsUuid>
```

A configuração atual deixa as requisições liberadas no Spring Security
(`permitAll`). Ainda assim, os controllers administrativos verificam a
existência de um `Principal`; sem uma autenticação que o estabeleça, essas
operações podem retornar `409 Conflict` com a mensagem de funcionário não
autenticado.

As sessões de funcionário duram duas horas e ficam em memória. Elas são
invalidadas quando a aplicação é reiniciada.

## Catálogo de endpoints

### Autenticação e usuários

| Método | Rota | Acesso | Resultado |
|---|---|---|---|
| `POST` | `/api/auth/usuarios/login` | Público | `200` e `LoginResponseDTO` |
| `POST` | `/api/usuarios` | Público | `201` |
| `GET` | `/api/usuarios/{id}` | Público | `200` ou `404` |
| `PUT` | `/api/usuarios/{id}` | Público no filtro atual | `204` |
| `PATCH` | `/api/usuarios/{id}/senha` | Público no filtro atual | `204` |
| `DELETE` | `/api/usuarios/{id}` | Público no filtro atual | `204` |
| `POST` | `/api/usuarios/logout` | `X-Session-UUID` | `204` |

`POST /api/usuarios/logout` exige o header `X-Session-UUID`. A sessão precisa
estar ativa e válida.

### Funcionários

| Método | Rota | Acesso lógico | Resultado |
|---|---|---|---|
| `POST` | `/api/funcionarios/login` | Público | `200` e funcionário |
| `POST` | `/api/funcionarios` | Administrador | `201` |
| `GET` | `/api/funcionarios` | Administrador | `200` e lista |
| `GET` | `/api/funcionarios/{id}` | Administrador | `200` |
| `PUT` | `/api/funcionarios/{id}` | Administrador | `204` |
| `PATCH` | `/api/funcionarios/{id}/senha` | Administrador | `204` |
| `DELETE` | `/api/funcionarios/{id}` | Administrador | `204` |

O administrador só manipula funcionários do próprio estabelecimento.

### Estabelecimentos

| Método | Rota | Acesso | Resultado |
|---|---|---|---|
| `POST` | `/api/estabelecimentos` | Público | `201` e estabelecimento |
| `PUT` | `/api/estabelecimentos/{id}` | Público no filtro atual | `204` |
| `DELETE` | `/api/estabelecimentos/{id}` | Público no filtro atual | `204` |
| `GET` | `/api/estabelecimentos/{id}` | Público | `200` ou `404` |
| `GET` | `/api/estabelecimentos/proximos` | Público | `200` e lista |
| `PATCH` | `/api/estabelecimentos/{id}/acolhimento` | Administrador | `204` |
| `PATCH` | `/api/estabelecimentos/{id}/suspensao` | Administrador | `204` |

Consulta de proximidade:

```text
GET /api/estabelecimentos/proximos?latitude=-23.5505&longitude=-46.6333
```

### Condições de apoio

| Método | Rota | Acesso | Resultado |
|---|---|---|---|
| `GET` | `/api/condicoes-apoio/estabelecimento/{id}/publicas` | Público | `200` e condições ativas |
| `GET` | `/api/condicoes-apoio` | Administrador | `200` e ativas/inativas |
| `POST` | `/api/condicoes-apoio` | Administrador | `201` e condição |
| `PUT` | `/api/condicoes-apoio/{id}` | Administrador | `200` e condição |
| `PATCH` | `/api/condicoes-apoio/{id}/status` | Administrador | `204` |

### Disponibilidades

| Método | Rota | Acesso | Resultado |
|---|---|---|---|
| `GET` | `/api/disponibilidades` | Administrador | `200` e grade semanal |
| `POST` | `/api/disponibilidades` | Administrador | `201` e disponibilidade |
| `PUT` | `/api/disponibilidades/{id}` | Administrador | `200` e disponibilidade |
| `PATCH` | `/api/disponibilidades/{id}/status` | Administrador | `204` |

### Avisos de acolhimento

| Método | Rota | Acesso | Resultado |
|---|---|---|---|
| `POST` | `/api/avisos-acolhimento` | Usuária com sessão | `201` e aviso |
| `GET` | `/api/avisos-acolhimento/me/ativo` | Usuária com sessão | `200` ou `204` |
| `PATCH` | `/api/avisos-acolhimento/{id}/cancelar` | Usuária proprietária | `200` e aviso |
| `GET` | `/api/avisos-acolhimento/painel/pendentes` | Funcionário | `200` e lista |
| `PATCH` | `/api/avisos-acolhimento/painel/{id}/confirmar` | Funcionário treinado | `200` e aviso |

### Integrações Brasil API

| Método | Rota | Regra | Resultado |
|---|---|---|---|
| `GET` | `/api/integracoes/cnpj/{cnpj}` | 14 dígitos | `200` |
| `GET` | `/api/integracoes/cep/{cep}` | 8 dígitos | `200` |

Exemplos:

```text
GET /api/integracoes/cnpj/12345678000195
GET /api/integracoes/cep/01310100
```

## Modelos e validações

### Usuário

```json
{
  "nmUsuario": "Ana Souza",
  "nrTelefone": "11999999999",
  "dsEmail": "ana@example.com",
  "dsSenha": "senha-segura"
}
```

- `nmUsuario`: obrigatório, até 120 caracteres.
- `nrTelefone`: opcional, até 20 caracteres.
- `dsEmail`: obrigatório, formato de e-mail, até 150 caracteres.
- `dsSenha`: obrigatória na criação, mínimo de 8 caracteres.
- Alteração de senha: `{ "dsNovaSenha": "nova-senha-segura" }`.

### Funcionário

Criação:

```json
{
  "nmFuncionario": "Carlos Lima",
  "nrTelefone": "11988887777",
  "dsEmailLogin": "carlos@example.com",
  "dsSenha": "senha-segura",
  "tpPerfil": "ATENDENTE",
  "flTreinado": "S"
}
```

- `tpPerfil`: `ADMIN` ou `ATENDENTE`.
- `flTreinado`: `S` ou `N`.
- Nome: até 120; telefone: até 20; e-mail: até 150 caracteres.
- Senhas de criação e alteração exigem pelo menos 8 caracteres.

### Estabelecimento

O CNPJ deve conter exatamente 14 dígitos e o CEP exatamente 8, ambos sem
pontuação. O CNPJ precisa estar ativo na Brasil API e não pode estar
cadastrado anteriormente. Quando informado, o CEP precisa ser encontrado na
Brasil API; os demais dados de endereço não são comparados automaticamente.
`sgUf` usa duas letras maiúsculas. Latitude deve estar entre `-90` e `90`;
longitude entre `-180` e `180`.

```json
{
  "nrCnpj": "12345678000195",
  "nmRazaoSocial": "Instituição ARI",
  "nmFantasia": "ARI",
  "dsEmail": "contato@ari.example",
  "nrTelefone": "1133334444",
  "dsTipoEstabelecimento": "Centro de acolhimento",
  "nrCep": "01310100",
  "dsLogradouro": "Avenida Paulista",
  "nrEndereco": "1000",
  "dsComplemento": "Sala 10",
  "nmBairro": "Bela Vista",
  "nmCidade": "São Paulo",
  "sgUf": "SP",
  "nrLatitude": -23.561684,
  "nrLongitude": -46.655981,
  "responsavel": {
    "nmResponsavel": "Gabriela Souza",
    "nrTelefone": "11999999999",
    "dsEmailLogin": "gabriela@ari.demo",
    "dsSenha": "senhasenha"
  }
}
```

O cadastro do estabelecimento cria automaticamente o primeiro funcionário
`ADMIN` usando os dados de `responsavel`. A senha é armazenada com BCrypt e
não é retornada pela API. O estabelecimento e seu administrador são criados
na mesma transação.

### Condição de apoio

```json
{
  "nmCondicao": "Atendimento em Libras",
  "dsCondicao": "Atendimento com intérprete de Libras."
}
```

`nmCondicao` é obrigatório e aceita até 100 caracteres. A descrição aceita até
255 caracteres. Para alterar o status, envie `{ "flAtivo": "S" }` ou
`{ "flAtivo": "N" }`.

### Disponibilidade

```json
{
  "nrDiaSemana": 1,
  "nrPeriodo": 1,
  "hrInicio": "08:00",
  "hrFim": "17:00"
}
```

- `nrDiaSemana`: de `1` (segunda-feira) a `7` (domingo).
- `nrPeriodo`: de `1` a `99`.
- `hrInicio` e `hrFim`: formato `HH:mm`, por exemplo `08:30`.
- Status: `{ "flAtivo": "S" }` ou `{ "flAtivo": "N" }`.

### Acolhimento e suspensão

Habilitar ou desabilitar acolhimento:

```json
{
  "flAcolhimentoAtivo": "S"
}
```

Configurar suspensão temporária:

```json
{
  "dtSuspensaoAte": "2026-12-31T18:00:00"
}
```

`dtSuspensaoAte` deve estar no futuro. Para remover a suspensão, envie `null`.

Solicitar acolhimento:

```json
{
  "cdEstabelecimento": 1,
  "nrTempoEstimado": 30
}
```

`cdEstabelecimento` é obrigatório e `nrTempoEstimado` aceita no máximo
`99999`.

## Respostas e erros

### Códigos mais comuns

| Código | Significado |
|---|---|
| `200 OK` | Consulta, login ou atualização concluída |
| `201 Created` | Recurso criado |
| `204 No Content` | Operação concluída sem corpo |
| `400 Bad Request` | JSON, parâmetro ou validação inválida |
| `404 Not Found` | Recurso não encontrado |
| `409 Conflict` | Regra de negócio, conflito ou estado inválido |

### Formato de erro

Erros tratados pela aplicação seguem este formato:

```json
{
  "timestamp": "2026-11-10T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos.",
  "path": "/api/usuarios",
  "fields": {
    "dsEmail": "E-mail inválido."
  }
}
```

`fields` relaciona o nome do campo ao motivo da falha. Em erros sem campo
específico, ele pode ser um objeto vazio.

## Fluxos recomendados

### Fluxo da usuária

1. `POST /api/usuarios` para criar a conta.
2. `POST /api/auth/usuarios/login` para obter `dsUuid`.
3. Enviar `X-Session-UUID` para criar, consultar ou cancelar acolhimento.
4. `POST /api/usuarios/logout` para revogar a sessão.

### Fluxo do estabelecimento

1. `POST /api/estabelecimentos`.
2. Criar ou autenticar um funcionário administrador.
3. Configurar acolhimento em `PATCH /{id}/acolhimento`.
4. Configurar horários em `/api/disponibilidades`.
5. Cadastrar condições em `/api/condicoes-apoio`.
6. Criar funcionários atendentes em `/api/funcionarios`.
7. Acompanhar avisos em `/api/avisos-acolhimento/painel/pendentes`.
8. Confirmar o atendimento em `/api/avisos-acolhimento/painel/{id}/confirmar`.

### Exemplo com cURL

```bash
curl -X POST http://localhost:8080/api/auth/usuarios/login \
  -H "Content-Type: application/json" \
  -d "{\"dsEmail\":\"ana@example.com\",\"dsSenha\":\"senha-segura\"}"
```

Depois, use o UUID recebido:

```bash
curl -X GET http://localhost:8080/api/avisos-acolhimento/me/ativo \
  -H "X-Session-UUID: 550e8400-e29b-41d4-a716-446655440000"
```

## Estrutura do projeto

```text
ari-api/
├── src/main/java/br/com/ari/
│   ├── controller/       # Rotas REST descritas pelo OpenAPI
│   ├── dto/              # Corpos de entrada e saída
│   ├── service/          # Regras de negócio
│   ├── dao/              # Acesso ao Oracle
│   ├── model/            # Entidades e modelos persistidos
│   ├── integration/      # Integração com a Brasil API
│   ├── exception/        # Tratamento e formato de erros
│   └── config/           # CORS e Spring Security
└── src/main/resources/
    └── application.properties
```

Ao adicionar ou alterar uma rota, atualize também este README e valide o
contrato em `/v3/api-docs` e na Swagger UI.
