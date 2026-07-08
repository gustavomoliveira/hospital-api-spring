# Hospital API

*[Read in English](README.md)*

Uma API REST para gerenciar os registros centrais de um hospital — pacientes, médicos, consultas e internações — construída com Spring Boot e PostgreSQL. Diferente de uma configuração H2 em memória, o escopo deste enunciado pedia especificamente a prática de um banco relacional em estilo produção, e é por isso que o Postgres é uma dependência real rodando aqui, em vez de algo embutido.

Stack: **Java 21, Spring Boot 4.1.0, Spring Data JPA, Bean Validation, PostgreSQL (runtime), H2 (testes), Lombok, JUnit 5 + Mockito, Maven**.

## Sumário

- [O que o projeto faz](#o-que-o-projeto-faz)
- [Decisões técnicas](#decisões-técnicas)
- [Rodando localmente](#rodando-localmente)
- [Estrutura do projeto](#estrutura-do-projeto)

## O que o projeto faz

A API expõe endpoints estilo CRUD sobre quatro entidades de domínio:

| Entidade | Prefixo do endpoint | Exposta via API? |
|---|---|---|
| `Paciente` | `/pacientes` | Sim — criar, buscar por id, listar, deletar |
| `Medico` | `/medicos` | Sim — criar, listar, mais um endpoint de ranking |
| `Consulta` | `/consultas` | Sim — apenas criar |
| `Internacao` | — | Não — modelada como entidade JPA, ainda não exposta por nenhum controller/service/repository |

`Internacao` ter ficado só como entidade não é descuido — segue exatamente as diretrizes do enunciado, que delimitou a exposição completa de CRUD apenas para `Paciente`, `Medico` e `Consulta`.

Além do CRUD simples, `GET /medicos/consultas/ranking` retorna os médicos ordenados pelo total de consultas, calculado com uma única query agregada em vez de carregado e contado em Java.

Na inicialização, um `DataLoader` (`CommandLineRunner`) popula dois médicos e dois pacientes caso as tabelas estejam vazias — útil pra já ter dados pra testar a API assim que o serviço sobe.

## Decisões técnicas

### 1. Package-by-feature, não package-by-layer

Diferente de um layout que agrupa todos os controllers juntos, todos os services juntos, etc., este projeto agrupa tudo que pertence a um mesmo conceito em um único pacote:

```
domain/paciente/    → Paciente, PacienteController, PacienteService, PacienteRepository,
                       PacienteRequestDTO, PacienteResponseDTO, PacienteMapper
domain/medico/      → mesma estrutura, para Medico
domain/consulta/    → mesma estrutura, para Consulta
domain/internacao/  → só a entidade, por enquanto
```

Preocupações transversais que não pertencem a uma única feature — as duas classes de exceção genéricas, o exception handler global, o `DataLoader` — ficam fora de `domain/`, em `exception/` e `infrastructure/`, respectivamente. Isso é um trade-off em relação a um estilo em camadas (agrupar todos os controllers juntos, todos os services juntos, e assim por diante): aqui, tudo sobre "pacientes" está num só lugar, ao custo de não ter uma única pasta `controller/` pra escanear todos os endpoints do sistema.

### 2. DTOs e mapeamento mantidos explícitos e manuais

Cada entidade tem um `RequestDTO` (entrada, como `record`) e um `ResponseDTO` (saída, também `record`), convertidos através de uma pequena classe `Mapper` estática (`PacienteMapper.toEntity(...)`, `PacienteMapper.toDTO(...)`). Nenhuma biblioteca de mapeamento (MapStruct, ModelMapper) é usada — a conversão é feita com algumas linhas de atribuição manual de campo por entidade. Isso mantém o mapeamento visível e fácil de debugar, ao custo de alguma repetição entre os três DTOs/mappers do projeto.

As entidades em si seguem a mesma estrutura nas quatro: um construtor público recebendo os campos de negócio (sem `id`), um construtor sem argumentos protegido/implícito para o JPA (`@NoArgsConstructor`), apenas `@Getter` — sem setters, então a única forma de construir uma é através do construtor.

### 3. Bean Validation nos DTOs de request, aplicada de forma consistente em todos os controllers

`PacienteRequestDTO` e `MedicoRequestDTO` declaram constraints do Jakarta Validation (`@NotBlank`, `@NotNull`, e `@CPF` do `hibernate-validator`, específico pro Brasil, que valida os dígitos verificadores do CPF, não só o formato). `ConsultaRequestDTO` faz o mesmo com `@NotNull` nos campos obrigatórios. Os três controllers (`PacienteController`, `MedicoController`, `ConsultaController`) aplicam `@Valid` no parâmetro `@RequestBody`, então um nome em branco ou um CPF inválido é rejeitado com `400 Bad Request` antes de chegar na camada de service.

### 4. Query agregada via constructor expression em JPQL

O endpoint de ranking de médicos é sustentado por uma única query JPQL usando uma constructor expression, em vez de buscar todos os médicos com suas consultas e contar em Java:

```java
@Query("SELECT new dev.gustavo.at.domain.medico.MedicoConsultasDTO(m.nome, COUNT(c)) " +
        "FROM Medico m LEFT JOIN m.consultas c " +
        "GROUP BY m.nome " +
        "ORDER BY COUNT(c) DESC")
List<MedicoConsultasDTO> findMedicosByTotalConsultas();
```

O `LEFT JOIN` garante que médicos com zero consultas ainda apareçam no ranking com contagem zero, em vez de serem silenciosamente excluídos como um `INNER JOIN` faria. A query retorna instâncias de `MedicoConsultasDTO` diretamente — o Hibernate constrói o DTO a partir do resultado da query, então nenhuma lista intermediária de entidades é materializada só pra ser descartada depois da contagem.

### 5. Tratamento de exceção centralizado com um formato de erro consistente

Um único `@RestControllerAdvice` (`GlobalExceptionHandler`) mapeia as quatro exceções de domínio para respostas HTTP, todas encapsuladas no mesmo `ErrorResponseDTO` (`status`, `message`, `timeStamp`). Os dois casos de "já existe" (`PacienteJaExisteException`, `MedicoJaExisteException`) mapeiam para `409 Conflict`, e os dois casos de "não existe" (`PacienteNaoExisteException`, `MedicoNaoExisteException`) mapeiam para `404 Not Found` — cada exceção carregando o código de status que de fato corresponde à sua semântica.

### 6. Configuração baseada em variáveis de ambiente para o banco, H2 para os testes

`src/main/resources/application.properties` lê o datasource inteiramente de variáveis de ambiente (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`), sem valores padrão e sem nada commitado — a aplicação não sobe sem elas definidas. Localmente, esses valores são fornecidos através de um arquivo `.env` (ignorado pelo Git, nunca commitado), com um `.env.example` versionado no repositório como modelo, mostrando quais chaves são esperadas, sem valores reais. `src/test/resources/application.properties` sobrescreve isso com um banco H2 em memória autocontido (`ddl-auto=create-drop`), então a suíte de testes nunca toca o Postgres nem exige nenhuma variável de ambiente pra rodar.

### 7. Dois estilos de teste, em dois níveis diferentes

- `HospitalServiceTest` — testes unitários contra `PacienteService` e `MedicoService`, com `PacienteRepository`/`MedicoRepository` mockados via Mockito (`@Mock`, `@InjectMocks`). Verificam a lógica de negócio (checagem de CPF/CRM duplicado, tratamento de "não encontrado") sem tocar em nenhum banco de dados.
- `PacienteIntegracaoTest` — testes de ponta a ponta usando `@SpringBootTest` + `@AutoConfigureMockMvc`, chamando os endpoints reais de `/pacientes` através do `MockMvc` e verificando a resposta JSON de fato, respaldados pelo banco H2 de teste descrito acima.

Isso dá cobertura rápida e isolada das regras de negócio, e cobertura mais lenta e completa do contrato HTTP, em vez de depender de um único estilo.

### 8. Actuator com superfície exposta mínima

`spring-boot-starter-actuator` está no classpath, mas `management.endpoints.web.exposure.include=health` expõe apenas o endpoint de health — não `/actuator/env`, `/actuator/beans`, nem qualquer outro endpoint do actuator que possa vazar configuração ou detalhes internos se deixado aberto por padrão.

## Rodando localmente

> As instruções assumem **macOS** com terminal.

**O Docker é a forma oficial de rodar este projeto.** Ele sobe o Postgres e a aplicação juntos com um único comando, sem precisar instalar Java ou Postgres diretamente na sua máquina (além do próprio Docker). Um passo a passo manual, sem Docker, também está incluído mais abaixo, pra desenvolvimento local dentro de uma IDE.

### Pré-requisitos

- **Docker Desktop for Mac** instalado e rodando.
- Não é necessário ter Maven, Java ou Postgres instalados localmente pro caminho com Docker — tudo roda dentro dos containers.

### 1. Clone o repositório

```bash
git clone https://github.com/gustavomoliveira/hospital-api-spring.git
cd hospital-api-spring
```

### 2. Configure o arquivo `.env`

O `docker-compose.yml` lê a senha do Postgres a partir de uma variável `POSTGRES_PASSWORD` em um arquivo `.env` local (ignorado pelo Git, nunca commitado). Copie o modelo e preencha com uma senha real:

```bash
cp .env.example .env
```

Depois abra o `.env` e defina um valor real para `POSTGRES_PASSWORD`.

### 3. Compile o jar da aplicação

O `Dockerfile` copia um jar já compilado em vez de compilar dentro do container, então compile primeiro:

```bash
./mvnw clean package -DskipTests
```

### 4. Suba o Postgres e a aplicação juntos

```bash
docker compose up --build
```

Isso sobe um container do Postgres e o container da aplicação Spring Boot, conectados na mesma rede do Docker. Na primeira execução, o `DataLoader` popula dois médicos e dois pacientes automaticamente.

### 5. Verifique se está rodando

```bash
curl -i http://localhost:8080/pacientes
```

Isso deve retornar `200 OK` com um array JSON contendo os dois pacientes semeados (João Silva e Maria Oliveira).

### 6. Teste o endpoint de ranking de médicos

```bash
curl -i http://localhost:8080/medicos/consultas/ranking
```

Ambos os médicos semeados devem aparecer com `totalConsultas: 0`, já que nenhuma consulta foi criada ainda.

### 7. Rode a suíte de testes

Os testes rodam inteiramente contra um banco H2 em memória e não precisam do Postgres nem do Docker rodando:

```bash
./mvnw test
```

### Rodando sem Docker (para desenvolvimento local via IDE)

Se você está desenvolvendo ativamente e quer rodar a aplicação direto pelo IntelliJ ou terminal sem containers, isso exige uma instância real do PostgreSQL instalada localmente, em vez da que o Docker fornece.

**Pré-requisitos:**

- **JDK 21** instalado e selecionado (`java -version` deve imprimir 21).
- **PostgreSQL** instalado localmente via Homebrew:
  ```bash
  brew install postgresql@16
  brew services start postgresql@16
  ```

**Passos:**

1. Crie o banco de dados:
   ```bash
   createdb hospital
   ```
2. Configure as variáveis de ambiente que o `application.properties` espera (não existe valor padrão, então a aplicação não sobe sem elas):
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/hospital
   export DB_USERNAME=$(whoami)
   export DB_PASSWORD=
   ```
   Se o seu usuário local do Postgres precisar de senha, defina `DB_PASSWORD` de acordo; uma instalação recente via Homebrew geralmente não tem senha no superusuário padrão.
3. Rode a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```

### Abrindo no IntelliJ

Abra o `pom.xml` na raiz do repositório como um projeto no IntelliJ. Se estiver rodando sem Docker, defina as três variáveis de ambiente acima na configuração de execução (**Run → Edit Configurations → Environment variables**) antes de rodar `AtApplication`, senão a aplicação falha ao subir com um erro de datasource.

## Estrutura do projeto

```
hospital-api-spring/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── mvnw / mvnw.cmd
└── src/
    ├── main/
    │   ├── java/dev/gustavo/at/
    │   │   ├── AtApplication.java
    │   │   ├── domain/
    │   │   │   ├── paciente/     # entidade, controller, service, repository, DTOs, mapper de Paciente
    │   │   │   ├── medico/       # entidade, controller, service, repository, DTOs, mapper de Medico
    │   │   │   ├── consulta/     # entidade, controller, service, repository, DTOs, mapper de Consulta
    │   │   │   └── internacao/   # só a entidade Internacao — ainda sem controller/service/repository
    │   │   ├── exception/        # exceções específicas de domínio (compartilhadas entre features)
    │   │   └── infrastructure/
    │   │       ├── config/       # DataLoader
    │   │       └── exception/    # GlobalExceptionHandler, ErrorResponseDTO
    │   └── resources/
    │       └── application.properties   # datasource via env vars, dialeto Postgres
    └── test/
        ├── java/dev/gustavo/at/
        │   ├── AtApplicationTests.java
        │   └── domain/paciente/
        │       ├── HospitalServiceTest.java      # testes unitários, Mockito
        │       └── PacienteIntegracaoTest.java    # testes de integração, MockMvc + H2
        └── resources/
            └── application.properties   # datasource H2, sobrescreve o profile principal para os testes
```
