# Roteiro de arguição — CandidFlow

## Explicação em 30 segundos

O CandidFlow é uma API REST em camadas para controlar candidatos, vagas, candidaturas e feedbacks. Os controllers recebem JSON por HTTP, os DTOs validam os dados, os services aplicam as regras de negócio, os repositories usam JPA e Hibernate para acessar o Oracle, e os mappers transformam entidades em respostas. O Flyway controla a estrutura do banco, enquanto o Docker padroniza a execução local e no Render.

## Responsabilidade de cada camada

| Camada | Responsabilidade | O que ela não deve fazer |
|---|---|---|
| Controller | Receber HTTP, encaminhar dados e escolher o status da resposta | Consultar diretamente o banco |
| DTO Request | Representar e validar o JSON recebido | Ser persistido como tabela |
| Service | Aplicar regras e controlar transações | Conhecer detalhes de HTTP |
| Mapper | Converter DTOs e entidades | Consultar o banco |
| Repository | Oferecer operações de persistência | Conter regras de negócio |
| Entity | Mapear objetos Java para tabelas | Ser exposta diretamente na API |
| DTO Response | Definir o JSON devolvido | Carregar comportamentos de persistência |
| Exception Handler | Converter erros em respostas HTTP padronizadas | Implementar o fluxo normal do recurso |

## Como uma requisição chega ao banco e volta

Exemplo: `POST /api/applications`.

1. O cliente envia um JSON com `candidateId`, `jobId` e `coverLetter`.
2. O Spring MVC recebe a chamada e o Jackson converte o JSON em `ApplicationCreateRequestDTO`.
3. `@Valid` executa as validações do DTO antes de entrar na regra de negócio.
4. `ApplicationController` chama `ApplicationService`.
5. O service abre uma transação por meio de `@Transactional`.
6. Os repositories consultam candidato e vaga no banco.
7. O service verifica se a vaga está aberta e se a candidatura ainda não existe.
8. O mapper cria a entidade `JobApplication` com status inicial `SUBMITTED`.
9. `ApplicationRepository.saveAndFlush` entrega a entidade ao Hibernate.
10. O Hibernate gera o `INSERT`; o HikariCP fornece uma conexão do pool e o driver JDBC envia o SQL por TLS ao Oracle.
11. O Oracle grava os dados e devolve o ID gerado.
12. O mapper converte a entidade persistida em `ApplicationResponseDTO`.
13. O controller devolve HTTP `201 Created`, e o Jackson transforma o DTO em JSON.

Fluxo resumido:

```text
JSON
 ↓ Jackson + Bean Validation
Controller
 ↓
Service + @Transactional
 ↓
Repository
 ↓ JPA/Hibernate
HikariCP + Oracle JDBC
 ↓ TLS
Oracle Autonomous Database
 ↑
Entity → Mapper → Response DTO → JSON
```

## O que o Lombok faz

Lombok gera código repetitivo durante a compilação. Ele não muda as regras de negócio nem cria tabelas.

| Anotação | Função no projeto |
|---|---|
| `@Getter` | Gera getters nas entidades |
| `@Setter` | Gera setters nas entidades |
| `@Data` | Gera getters, setters e métodos utilitários nos DTOs |
| `@Builder` | Permite construir objetos de maneira legível |
| `@NoArgsConstructor` | Gera construtor vazio, necessário pelo JPA e Jackson |
| `@AllArgsConstructor` | Gera construtor com todos os atributos |
| `@RequiredArgsConstructor` | Gera injeção por construtor para campos `final` |
| `@Slf4j` | Disponibiliza o logger no tratador global de erros |

Não foi usado `@Data` nas entidades porque ele também gera `equals`, `hashCode` e `toString`. Em entidades relacionadas, isso pode provocar consultas involuntárias, problemas com proxies do Hibernate ou recursão. Por isso as entidades usam apenas `@Getter` e `@Setter`.

## JPA e Hibernate

- JPA é a especificação que define como mapear objetos para um banco relacional.
- Hibernate é a implementação usada pelo Spring Data JPA.
- `@Entity` marca uma classe persistente.
- `@Table` informa a tabela correspondente.
- `@Id` define a chave primária.
- `@GeneratedValue` delega a geração do ID ao Oracle.
- `@ManyToOne` representa as relações de candidatura com candidato/vaga e feedback com candidatura.
- `@Enumerated(EnumType.STRING)` salva enums de forma legível, como `OPEN` e `INTERVIEW`.
- `@Lob` mapeia textos extensos para `CLOB`.

## Repository

Os repositories estendem `JpaRepository`. O Spring cria suas implementações em tempo de execução. Métodos como:

```java
existsByCandidateIdAndJobId(candidateId, jobId)
```

são interpretados pelo Spring Data, que gera a consulta correspondente. Isso evita SQL manual para operações comuns.

Nas consultas de candidaturas, `@EntityGraph` carrega candidato e vaga na mesma operação. Isso evita o problema N+1, no qual uma listagem poderia disparar consultas adicionais para cada item retornado.

## DTOs e mappers

As entidades representam o banco; os DTOs representam o contrato HTTP. Separá-los permite alterar uma tabela sem necessariamente alterar a resposta pública e evita expor relacionamentos internos do Hibernate.

O mapper concentra as conversões. Assim, controller e service não ficam cheios de atribuições repetitivas.

## Flyway

O Flyway executa scripts versionados quando a aplicação inicia. A primeira migração cria as quatro tabelas, chaves estrangeiras, restrições e índices.

```text
V1__create_initial_schema.sql
```

O projeto usa:

```yaml
ddl-auto: validate
```

Portanto:

- Flyway modifica o banco.
- Hibernate apenas confirma que entidades e tabelas são compatíveis.
- Mudanças futuras devem gerar `V2__...sql`, `V3__...sql` e assim por diante.

## Regras de negócio principais

1. O e-mail do candidato é normalizado em minúsculas e deve ser único.
2. A vaga nasce aberta quando o status não é informado.
3. Uma candidatura só pode ser criada para uma vaga aberta.
4. O mesmo candidato não pode se candidatar duas vezes à mesma vaga.
5. Vagas ou candidatos com candidaturas não podem ser excluídos.
6. Feedbacks dependem de uma candidatura existente.

As regras 3 e 4 estão no service e também são reforçadas por restrições do banco. Essa duplicação é intencional: o service devolve uma mensagem clara, enquanto o banco protege a integridade mesmo diante de concorrência.

## Códigos HTTP

| Código | Significado no CandidFlow |
|---|---|
| `200 OK` | Consulta ou atualização realizada |
| `201 Created` | Recurso criado |
| `204 No Content` | Recurso excluído |
| `400 Bad Request` | JSON ou campo inválido |
| `404 Not Found` | ID inexistente |
| `409 Conflict` | Regra de negócio ou integridade violada |
| `500 Internal Server Error` | Falha inesperada |

## SOLID no projeto

- **Responsabilidade única:** cada camada possui uma função específica.
- **Aberto/fechado:** novos recursos podem ser adicionados com novas classes sem reescrever os CRUDs existentes.
- **Substituição de Liskov:** as abstrações do Spring Data podem ser utilizadas conforme seus contratos.
- **Segregação de interfaces:** os repositories expõem apenas os contratos de persistência necessários.
- **Inversão de dependência:** o Spring injeta dependências por construtor; repositories são interfaces e suas implementações são fornecidas pelo framework.

SOLID não é determinado pelos nomes das pastas. A organização em camadas torna as responsabilidades visíveis, enquanto o princípio é aplicado nas dependências e no conteúdo das classes.

## Escalabilidade sem microserviços prematuros

- API sem estado local: qualquer instância pode atender uma requisição.
- Listagens paginadas para não carregar tabelas inteiras.
- Índices nos campos usados por relacionamentos e status.
- Pool HikariCP para reutilizar conexões.
- Tamanho do pool configurável por ambiente.
- Banco gerenciado pelo Oracle Autonomous Database.
- Imagem Docker reproduzível.
- Variáveis de ambiente para configuração local e em nuvem.
- Camadas desacopladas, permitindo extração futura de módulos se houver necessidade real.

## Docker e Render

O Dockerfile possui dois estágios:

1. O estágio de build usa Maven e gera o JAR.
2. O estágio final contém apenas o Java Runtime e o JAR.

Isso reduz a imagem final e torna o ambiente reproduzível. O Render constrói essa imagem, injeta as variáveis do banco e monitora `/actuator/health`.

## Perguntas prováveis

### Por que não usar a entidade diretamente no controller?

Porque isso acoplaria o contrato HTTP ao modelo do banco e poderia expor campos ou relacionamentos internos.

### Por que usar service se o repository já possui CRUD?

Porque o repository apenas persiste. Regras como impedir candidatura duplicada e candidatura em vaga fechada pertencem ao service.

### Por que existe validação no Java e restrição no banco?

A validação fornece respostas rápidas e claras. A restrição do banco é a última barreira contra inconsistências e condições de corrida.

### Por que `PUT` e `PATCH`?

`PUT` atualiza a representação editável completa. `PATCH /status` altera apenas o status da candidatura.

### Por que começar como monólito?

Porque o domínio ainda é pequeno. Um monólito em camadas é mais simples de desenvolver, testar e implantar; microserviços só seriam justificados por escala organizacional ou operacional comprovada.

### Onde entraria segurança depois?

Spring Security e JWT seriam adicionados antes dos controllers. O token identificaria o usuário e permitiria restringir ações por função, sem mudar a responsabilidade das demais camadas.
