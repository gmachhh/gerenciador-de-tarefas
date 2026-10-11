# Gerenciador de Tarefas

Aplicação de linha de comando em **Java** para organizar tarefas do dia a dia, com prioridades, prazos, filtros, estatísticas e salvamento automático em arquivo.

Projeto desenvolvido para praticar **Programação Orientada a Objetos**, **arquitetura em camadas**, **testes automatizados** e recursos modernos do Java (Streams, Records, `switch` expressions, `java.time` e `java.nio`).

**Tecnologias:** Java 17+ · Maven · JUnit 6

## Funcionalidades

- Criar, editar, concluir, reabrir e remover tarefas
- Listar tarefas ordenadas por data de criação, prazo ou prioridade
- Buscar tarefas por texto (título ou descrição)
- Filtrar por pendentes, concluídas, atrasadas ou prioridade
- Detectar automaticamente tarefas atrasadas
- Painel de estatísticas com percentual de progresso
- Salvamento automático em `tarefas.csv`: os dados continuam lá quando o programa é aberto de novo
- Escrita segura do arquivo (arquivo temporário + troca atômica): uma queda no meio do salvamento não corrompe os dados
- Validação de entrada: datas impossíveis (ex: 31/02), prazos no passado, campos obrigatórios e IDs inexistentes

## Exemplo de uso

```
Legenda: [ ] pendente  [X] concluída  [!] atrasada
[X] #1 - Estudar Java | Prioridade: Alta | Prazo: 20/12/2026
      POO, streams e records
[ ] #2 - Comprar pão | Prioridade: Média | Prazo: 15/11/2026
      (sem descrição)

--- Estatísticas ---
Total:      2
Concluídas: 1
Pendentes:  1
Atrasadas:  0
Progresso:  50,0%
```

## Arquitetura

O código é dividido em camadas, cada uma com uma única responsabilidade:

```
src/main/java/
├── Main.java                         # Ponto de entrada: monta as dependências e inicia o menu
├── model/                            # Entidades e regras do domínio
│   ├── Tarefa.java
│   ├── Prioridade.java               # enum
│   └── Estatisticas.java             # record
├── service/
│   └── GerenciadorTarefas.java       # Regras de negócio (CRUD, filtros, ordenação)
├── repository/
│   └── TarefaRepository.java         # Persistência em arquivo
├── ui/
│   └── Menu.java                     # Interação com o usuário pelo console
└── exception/
    └── TarefaNaoEncontradaException.java

src/test/java/                        # Testes automatizados (JUnit)
├── model/TarefaTest.java
├── service/GerenciadorTarefasTest.java
└── repository/TarefaRepositoryTest.java
```

- **model** não depende de nenhuma outra camada.
- **service** não sabe nada sobre console nem arquivo, então poderia ser reaproveitado em uma API REST ou interface gráfica.
- **ui** só conversa com o usuário e delega o trabalho ao service.

## Conceitos aplicados

| Conceito | Onde |
|---|---|
| Encapsulamento e validação | `Tarefa`: atributos privados, setters que validam os dados |
| Enum com atributos | `Prioridade` |
| Records | `Estatisticas` |
| Streams e lambdas | Filtros e ordenações em `GerenciadorTarefas` |
| `Comparator` encadeado | Ordenação por prioridade e depois por prazo |
| Exceção personalizada | `TarefaNaoEncontradaException` |
| `switch` expressions | `Menu` |
| API `java.time` | Prazos, detecção de atraso, validação estrita de datas |
| API `java.nio.file` | Leitura e escrita do arquivo |
| `equals` / `hashCode` | `Tarefa` (identidade pelo ID) |
| Injeção de dependência | `Clock` injetado no `GerenciadorTarefas`, o que deixa as regras de data testáveis |
| Testes unitários | 33 testes com JUnit, incluindo relógio fixo e `@TempDir` |

## Testes

Os testes cobrem as validações da `Tarefa`, todas as regras do `GerenciadorTarefas` e o salvamento/carregamento do `TarefaRepository`.

Como as regras de atraso dependem da data atual, o `GerenciadorTarefas` recebe um `java.time.Clock`. Nos testes, ele é um relógio parado num dia fixo, então os resultados são sempre os mesmos, em qualquer dia em que os testes rodarem.

```bash
./mvnw test        # Linux/macOS
mvnw.cmd test      # Windows
```

## Como executar

Requisitos: **Java 17 ou superior**. Não é preciso instalar o Maven: o *Maven Wrapper* (`mvnw`) baixa a versão certa automaticamente.

```bash
mvnw.cmd package                       # compila, roda os testes e gera o .jar
java -jar target/gerenciador-tarefas.jar
```

> No Linux/macOS, use `./mvnw` no lugar de `mvnw.cmd`.

Ou abra a pasta no VS Code (com o *Extension Pack for Java*) ou no IntelliJ e execute a classe `Main`.

## Próximos passos

- [x] Testes unitários com JUnit
- [x] Build com Maven
- [ ] Interface gráfica (JavaFX) ou API REST (Spring Boot)
- [ ] Persistência em banco de dados (SQLite / PostgreSQL)
