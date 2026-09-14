# Análise de Estrutura — portal-atendimento

Documento de referência produzido a partir da leitura completa do código-fonte de `portal-atendimento-main`, para servir de base a um novo projeto derivado deste.

## 1. Visão geral

`portal-atendimento` (grupo Maven `com.joao-v-marques`, artefato `portal-atendimento`) é um backend Java/Spring Boot para um "Portal de Atendimento": cadastro de usuários com perfis de acesso, e um fluxo central de **solicitações de autorização** vinculadas a um beneficiário, com upload de documentos comprobatórios (PDF/JPEG/PNG) anexados a cada solicitação. Tem login via formulário/JWT em cookie, controle de acesso por papel (ADMIN/EMPLOYEE) e trilha de auditoria simples (quem inseriu, quando).

Tamanho: 63 arquivos `.java`, ~2.656 linhas de código em `src/main`, mais um pacote de testes com 3 classes.

## 2. Stack tecnológica

- **Java 17**, **Spring Boot 4.1.0** (via `spring-boot-starter-parent`)
- **Spring Data JPA** + **Hibernate** (`ddl-auto: validate` — o schema é sempre gerado pelas migrations, nunca pelo Hibernate)
- **Flyway** (`flyway-database-postgresql`) para versionamento de schema
- **PostgreSQL** (driver `org.postgresql:postgresql`), rodando em Docker (`postgres:18-alpine`)
- **Spring Security** com autenticação **stateless via JWT** (`io.jsonwebtoken:jjwt` 0.12.6), token entregue em cookie httpOnly (`access_token`) e também aceito via header `Authorization: Bearer`
- **Thymeleaf** para as (poucas) páginas server-side (`/home`, `/login`) — o restante da aplicação é uma API REST (`/api/**`)
- **Bean Validation** (`spring-boot-starter-validation`) nos DTOs de entrada
- **Lombok** (`@Getter/@Setter/@NoArgsConstructor`) nas entidades
- **Maven** com wrapper (`mvnw`/`mvnw.cmd`)
- Testes: JUnit 5, com starters de teste próprios do Spring Boot 4 (`-test` para jpa, security, flyway, thymeleaf, validation, webmvc)

Observação: `spring-boot-starter-webmvc`, `-thymeleaf`, `-flyway` como *starters* dedicados (em vez de vir embutido no `spring-boot-starter-web`/`spring-boot-starter`) é o formato novo do Spring Boot 4 — vale manter essa mesma convenção no projeto novo para não misturar versões.

## 3. Estrutura de pastas

```
portal-atendimento-main/
├── docker-compose.yml          # só o Postgres (app roda fora do compose)
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/
└── src/
    ├── main/
    │   ├── java/com/joao_v_marques/portal_atendimento/
    │   │   ├── PortalAtendimentoApplication.java
    │   │   ├── config/            # SecurityConfig, ViewConfig
    │   │   ├── exception/         # ApiError, ApiExceptionHandler, StorageException
    │   │   ├── security/          # JWT, UserDetails, entry points
    │   │   ├── storage/           # abstração de armazenamento de arquivos
    │   │   ├── users/
    │   │   │   ├── auth/          # login/logout
    │   │   │   ├── user/          # CRUD de usuário
    │   │   │   └── user_roles/    # CRUD de perfil de acesso
    │   │   └── authorization/
    │   │       ├── authorization_request/           # a "solicitação" em si
    │   │       ├── authorization_request_document/   # documentos anexados
    │   │       ├── authorization_status/             # catálogo (ex.: Em análise, Finalizado)
    │   │       └── authorization_type/               # catálogo (ex.: Não especificado)
    │   └── resources/
    │       ├── application.yaml
    │       ├── db/migration/      # V1..V5, Flyway
    │       └── templates/         # home.html, login.html (Thymeleaf, hoje só placeholders)
    └── test/java/.../             # espelha o pacote de main
```

Cada módulo de negócio é organizado por **feature package** (não por camada): a pasta `authorization_request/`, por exemplo, contém a entidade, o controller, o service, o repository e uma subpasta `dto/` com os records de request/response — tudo junto. Esse é o padrão a repetir em qualquer feature nova.

## 4. Arquitetura e padrões de código

O projeto segue um padrão muito consistente em todos os módulos, o que facilita replicar em um projeto novo:

**Entidade JPA** — classe simples anotada com `@Entity`, `@Table(name = "...")`, `@Getter @Setter @NoArgsConstructor` do Lombok (sem `@AllArgsConstructor`, sem builder). Chave primária sempre `Integer id` com `@GeneratedValue(strategy = GenerationType.IDENTITY)`. Relacionamentos sempre `@ManyToOne(fetch = FetchType.LAZY)`.

**DTOs** — sempre `record`, nunca classe. Um `XxxRequest` com anotações de Bean Validation e mensagens de erro em português, e um `XxxResponse` "achatado" (sem aninhar entidades — por exemplo `AuthorizationRequestResponse` expõe `authorizationTypeName: String` em vez do objeto `AuthorizationType` inteiro).

**Repository** — interface `JpaRepository<Entidade, Integer>`, com métodos derivados (`existsByNameIgnoreCase`, `existsByTransactionNumber`) e, quando necessário, `@Query` customizada com `@Lock(LockModeType.PESSIMISTIC_WRITE)` para seções críticas (usado em `AuthorizationRequestRepository.findByIdForUpdate`, para serializar uploads concorrentes na mesma solicitação).

**Service** — injeção de dependência por construtor (sem `@Autowired`, sem Lombok `@RequiredArgsConstructor` — todos os construtores são escritos à mão). Regra de negócio nunca lança exceção de framework diretamente: usa `IllegalArgumentException` com mensagem amigável em português, que o `ApiExceptionHandler` global traduz para HTTP 400. Método privado `toResponse(entidade)` no fim da classe converte entidade → DTO. `@Transactional` explícito em cada método (readOnly nos GETs).

**Controller** — `@RestController` fino, sem lógica de negócio, delega tudo ao service. Padrão de rotas: `GET /api/recurso`, `POST /api/recurso`, `PUT /api/recurso/{id}`, `PATCH /api/recurso/{id}/deactivate` e `.../reactivate` para os catálogos (em vez de DELETE — nada é hard-deletado, tudo tem `is_active`). `POST` retorna `201 Created` com header `Location`.

**Catálogos com soft delete** — `authorization_type`, `authorization_status` e `user_roles` seguem o mesmo mini-padrão: coluna `is_active boolean default true`, e os services bloqueiam edição de registro inativo e impedem desativar/reativar duas vezes seguidas (checagem explícita antes de mudar o estado).

**Tratamento de erros centralizado** — um único `@RestControllerAdvice` (`ApiExceptionHandler`) mapeia `BindException` (validação), `IllegalArgumentException` (regra de negócio), `HttpMessageNotReadableException` (JSON inválido), `StorageException`, `DataIntegrityViolationException` (choque de unique constraint → 409) e `MaxUploadSizeExceededException`, todos devolvendo o mesmo formato `ApiError { message, fields }`.

## 5. Segurança

Stateless, sem sessão HTTP (`SessionCreationPolicy.STATELESS`), CSRF desabilitado (coerente com API sem cookies de sessão tradicionais). Login é feito em `POST /api/auth/login`, que autentica via `AuthenticationManager`/`DaoAuthenticationProvider` (senha com `BCryptPasswordEncoder`) e devolve o JWT tanto no corpo (`AuthResponse`) quanto em um cookie `access_token` (`httpOnly`, `SameSite=Lax`, `secure` configurável por variável de ambiente `COOKIE_SECURE`).

O `JwtAuthenticationFilter` (um `OncePerRequestFilter`) lê o token do header `Authorization: Bearer` ou do cookie, valida e popula o `SecurityContextHolder`. `UserPrincipal` implementa `UserDetails` envolvendo a entidade `User`, expõe uma única authority `ROLE_<nome do papel>` (hoje `ADMIN` ou `EMPLOYEE`).

Autorização é declarada centralmente em `SecurityConfig`: rotas públicas (`/login`, `/api/auth/**`, `/error`, estáticos), `POST /api/users` restrito a `ROLE_ADMIN`, e tudo mais exige autenticação — não há ainda `@PreAuthorize` espalhado pelos controllers, a regra fica concentrada na filter chain.

Tratamento de falha de autenticação/autorização é sensível ao tipo de rota: para `/api/**` devolve JSON (401/403) via `RestAuthenticationEntryPoint`/`RestAccessDeniedHandler`; para as demais rotas, redireciona para `/login?erro=...` (distinguindo `login-necessario` de `sessao-expirada` via um atributo de request setado pelo filtro).

## 6. Armazenamento de documentos

Módulo `storage/` é uma abstração própria, desacoplada de qualquer provedor: interface `DocumentStorage` (`store/load/delete/exists`) com uma única implementação `LocalDocumentStorage` (filesystem local, raiz configurável por `app.storage.documents-root`). Pontos notáveis, úteis para replicar num projeto novo:

- `PathSanitizer` normaliza nome de arquivo livre em segmento de path seguro — preserva acentos/espaços de propósito (legibilidade humana da árvore de pastas), mas neutraliza barras, caracteres inválidos no Windows, nomes reservados (`CON`, `NUL`...) e trailing dot/espaço, com bateria de testes dedicada.
- `FileTypeValidator` não confia na extensão nem no `Content-Type` enviado pelo cliente: detecta o tipo real pelos primeiros bytes (magic numbers) comparando contra `AllowedFileType` (PDF/JPEG/PNG apenas).
- `LocalDocumentStorage.resolve()` bloqueia path traversal verificando que o caminho resolvido continua dentro da raiz configurada (coberto por teste específico, inclusive para path absoluto).
- Caminho final do arquivo é `<número da transação>/<nome original sanitizado>.<extensão detectada>`, com resolução de colisão incremental (`arquivo (2).pdf`, `(3)`...) checando tanto o banco (`existsByStoredPath`) quanto o disco.
- Upload é transacional: se o registro em banco falhar depois do arquivo gravado, um `TransactionSynchronization` de rollback apaga o arquivo órfão do disco (o filesystem não participa da transação do banco).
- Existem dois fluxos de upload: multipart atômico junto com a criação da solicitação (`POST /api/authorization-requests`, `multipart/form-data`) e upload avulso posterior (`POST /api/authorization-requests/{id}/documents`), ambos passando pelo mesmo método `attach()` central. Um lock pessimista na solicitação (`findByIdForUpdate`) serializa uploads concorrentes para não estourar o limite de arquivos por solicitação.

## 7. Modelo de dados

Migrations Flyway (`V1` a `V5`), todas incrementais e sem `down`:

- `user_roles (id, name unique, is_active)` — seed: ADMIN, EMPLOYEE
- `users (id, username unique, name, password_hash, email, role_id → user_roles, created_at, is_active)`
- `authorization_type (id, name unique, is_active)` — seed: "Não especificado"
- `authorization_status (id, name unique, is_active)` — seed: "Em análise", "Finalizado"
- `authorization_requests (id, transaction_number unique varchar(12), request_date, authorization_type_id →, authorization_status_id →, beneficiary_name, beneficiary_phone, created_at, inserted_by → users)`
- `authorization_request_documents (id, original_filename, stored_path unique, content_type, size_bytes, uploaded_at, uploaded_by → users, authorization_request_id → authorization_requests ON DELETE CASCADE)`

Índices explícitos em todas as FKs (comentário no SQL lembrando que o Postgres não cria isso automaticamente). Usuário admin seed já vem com senha hash bcrypt fixa no `V3` — trocar isso é o primeiro passo de segurança antes de qualquer deploy real.

## 8. Configuração

`application.yaml` usa `context-path: /portal-atendimento`, e praticamente todo valor sensível tem variável de ambiente com default de desenvolvimento (`JWT_SECRET`, `JWT_EXPIRATION`, `COOKIE_SECURE`, `DOCUMENTS_ROOT`). Datasource e credenciais do Postgres, porém, estão hardcoded no `application.yaml` (não como variável de ambiente) — outro ponto a endurecer num projeto novo. `docker-compose.yml` sobe só o banco (porta local `5433→5432`), a aplicação roda fora do compose (via `mvnw spring-boot:run` ou artefato).

## 9. Testes

Cobertura de teste hoje está concentrada no módulo `storage` (`PathSanitizerTest`, `LocalDocumentStorageTest`, `FileTypeValidatorTest`) mais o teste de contexto padrão (`PortalAtendimentoApplicationTests`). Nenhum teste de controller/service dos módulos de negócio (`authorization_request`, `users`) ainda existe — é uma lacuna a considerar preencher no projeto novo, já que o `storage` mostra que o padrão de teste do time é bom (nomes de teste descritivos com `@DisplayName` em português, casos de borda bem pensados).

## 10. Frente ainda não desenvolvida

Os templates Thymeleaf (`home.html`, `login.html`) são placeholders (`<h1>Esta é a página home</h1>`) — o frontend real (provavelmente as telas de login e a tela principal do portal) ainda não foi construído; hoje a "interface" é puramente a API REST consumida por algo externo, ou está por vir.

## 11. Checklist para o novo projeto baseado neste

Ao clonar esse modelo, os pontos que valem replicar deliberadamente são: a organização por *feature package* (entidade+controller+service+repository+dto juntos por módulo, não por camada global); DTOs sempre como `record`; regra de negócio via `IllegalArgumentException` + handler central; catálogos com `is_active` em vez de hard delete; JWT em cookie httpOnly com fallback de header; e a abstração `DocumentStorage` caso a nova aplicação também lide com upload de arquivo (trocar `LocalDocumentStorage` por uma implementação em nuvem é troca de uma classe só, a interface já está pronta para isso).

Pontos que merecem revisão antes de reaproveitar tal e qual: credenciais do Postgres hardcoded no `application.yaml`; segredo JWT com default fraco versionado no repositório; ausência de testes para os módulos de negócio; e ausência de `@PreAuthorize` por endpoint (hoje só há uma regra de papel, para `POST /api/users`) — se o novo projeto tiver mais de dois papéis com regras distintas por rota, vale granularizar isso desde o início.
