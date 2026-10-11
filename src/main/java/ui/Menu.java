package ui;

import exception.TarefaNaoEncontradaException;
import model.Estatisticas;
import model.Prioridade;
import model.Tarefa;
import repository.TarefaRepository;
import service.GerenciadorTarefas;

import java.io.Console;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Scanner;

/* Camada de interface: só conversa com o usuário e delega o trabalho ao GerenciadorTarefas */
public class Menu {

    /* STRICT rejeita datas impossíveis como 31/02 (com STRICT o ano precisa ser "uuuu") */
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final GerenciadorTarefas gerenciador;
    private final TarefaRepository repository;
    private final Scanner scanner;

    public Menu(GerenciadorTarefas gerenciador, TarefaRepository repository) {
        this.gerenciador = gerenciador;
        this.repository = repository;
        /* Usa a codificação do terminal para que acentos digitados sejam lidos corretamente */
        Console console = System.console();
        Charset charset = console != null ? console.charset() : Charset.defaultCharset();
        this.scanner = new Scanner(System.in, charset);
    }

    public void iniciar() {
        System.out.println("=== GERENCIADOR DE TAREFAS ===");
        boolean executando = true;

        while (executando) {
            exibirOpcoes();
            int opcao = lerInteiro("Escolha uma opção: ");
            System.out.println();

            try {
                switch (opcao) {
                    case 1 -> adicionarTarefa();
                    case 2 -> listarTarefas();
                    case 3 -> buscarTarefas();
                    case 4 -> filtrarTarefas();
                    case 5 -> editarTarefa();
                    case 6 -> concluirTarefa();
                    case 7 -> reabrirTarefa();
                    case 8 -> removerTarefa();
                    case 9 -> exibirEstatisticas();
                    case 0 -> executando = false;
                    default -> System.out.println("Opção inválida.");
                }
            } catch (TarefaNaoEncontradaException | IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }

        System.out.println("Até logo!");
    }

    private void exibirOpcoes() {
        System.out.println();
        System.out.println("1 - Adicionar tarefa");
        System.out.println("2 - Listar tarefas");
        System.out.println("3 - Buscar por texto");
        System.out.println("4 - Filtrar tarefas");
        System.out.println("5 - Editar tarefa");
        System.out.println("6 - Concluir tarefa");
        System.out.println("7 - Reabrir tarefa");
        System.out.println("8 - Remover tarefa");
        System.out.println("9 - Estatísticas");
        System.out.println("0 - Sair");
    }

    /* Ações do menu */

    private void adicionarTarefa() {
        System.out.println("--- Nova tarefa ---");
        String titulo = lerTextoObrigatorio("Título: ");
        String descricao = lerTexto("Descrição (opcional): ");
        LocalDate prazo = lerData("Prazo (dd/mm/aaaa): ", false);
        Prioridade prioridade = lerPrioridade(false);

        Tarefa tarefa = gerenciador.adicionarTarefa(titulo, descricao, prazo, prioridade);
        salvar();
        System.out.println("Tarefa #" + tarefa.getId() + " criada com sucesso!");
    }

    private void listarTarefas() {
        System.out.println("Ordenar por: 1 - Criação | 2 - Prazo | 3 - Prioridade");
        int opcao = lerInteiro("Opção: ");

        List<Tarefa> tarefas = switch (opcao) {
            case 2 -> gerenciador.listarOrdenadasPorPrazo();
            case 3 -> gerenciador.listarOrdenadasPorPrioridade();
            default -> gerenciador.listarTodas();
        };
        exibirLista(tarefas);
    }

    private void buscarTarefas() {
        String termo = lerTextoObrigatorio("Digite o termo de busca: ");
        exibirLista(gerenciador.buscarPorTexto(termo));
    }

    private void filtrarTarefas() {
        System.out.println("Filtrar: 1 - Pendentes | 2 - Concluídas | 3 - Atrasadas | 4 - Por prioridade");
        int opcao = lerInteiro("Opção: ");

        List<Tarefa> tarefas = switch (opcao) {
            case 1 -> gerenciador.listarPorStatus(false);
            case 2 -> gerenciador.listarPorStatus(true);
            case 3 -> gerenciador.listarAtrasadas();
            case 4 -> gerenciador.listarPorPrioridade(lerPrioridade(false));
            default -> throw new IllegalArgumentException("Filtro inválido.");
        };
        exibirLista(tarefas);
    }

    private void editarTarefa() {
        int id = lerInteiro("ID da tarefa: ");
        Tarefa tarefa = gerenciador.buscarPorId(id);
        System.out.println(formatar(tarefa));
        System.out.println("(Deixe em branco e aperte Enter para manter o valor atual)");

        String titulo = lerTexto("Novo título: ");
        String descricao = lerTexto("Nova descrição: ");
        LocalDate prazo = lerData("Novo prazo (dd/mm/aaaa): ", true);
        Prioridade prioridade = lerPrioridade(true);

        gerenciador.editarTarefa(
                id,
                titulo.isEmpty() ? null : titulo,
                descricao.isEmpty() ? null : descricao,
                prazo,
                prioridade
        );
        salvar();
        System.out.println("Tarefa atualizada!");
    }

    private void concluirTarefa() {
        int id = lerInteiro("ID da tarefa a concluir: ");
        gerenciador.concluirTarefa(id);
        salvar();
        System.out.println("Tarefa #" + id + " concluída!");
    }

    private void reabrirTarefa() {
        int id = lerInteiro("ID da tarefa a reabrir: ");
        gerenciador.reabrirTarefa(id);
        salvar();
        System.out.println("Tarefa #" + id + " reaberta!");
    }

    private void removerTarefa() {
        int id = lerInteiro("ID da tarefa a remover: ");
        Tarefa tarefa = gerenciador.buscarPorId(id);
        System.out.println(formatar(tarefa));

        if (confirmar("Tem certeza que deseja remover? (s/n): ")) {
            gerenciador.removerTarefa(id);
            salvar();
            System.out.println("Tarefa removida.");
        } else {
            System.out.println("Remoção cancelada.");
        }
    }

    private void exibirEstatisticas() {
        Estatisticas estatisticas = gerenciador.gerarEstatisticas();
        System.out.println("--- Estatísticas ---");
        System.out.println("Total:      " + estatisticas.total());
        System.out.println("Concluídas: " + estatisticas.concluidas());
        System.out.println("Pendentes:  " + estatisticas.pendentes());
        System.out.println("Atrasadas:  " + estatisticas.atrasadas());
        System.out.printf("Progresso:  %.1f%%%n", estatisticas.percentualConcluido());
    }

    /* Métodos auxiliares */

    private void exibirLista(List<Tarefa> tarefas) {
        if (tarefas.isEmpty()) {
            System.out.println("Nenhuma tarefa encontrada.");
            return;
        }
        System.out.println("Legenda: [ ] pendente  [X] concluída  [!] atrasada");
        for (Tarefa tarefa : tarefas) {
            System.out.println(formatar(tarefa));
        }
    }

    private String formatar(Tarefa tarefa) {
        String status;
        if (tarefa.isConcluida()) {
            status = "[X]";
        } else if (gerenciador.estaAtrasada(tarefa)) {
            status = "[!]";
        } else {
            status = "[ ]";
        }

        String descricao = tarefa.getDescricao().isEmpty() ? "(sem descrição)" : tarefa.getDescricao();

        return String.format("%s #%d - %s | Prioridade: %s | Prazo: %s%n      %s",
                status, tarefa.getId(), tarefa.getTitulo(), tarefa.getPrioridade().getDescricao(),
                tarefa.getPrazo().format(FORMATO_DATA), descricao);
    }

    private void salvar() {
        try {
            repository.salvar(gerenciador.listarTodas());
        } catch (IOException e) {
            System.out.println("Aviso: não foi possível salvar as tarefas (" + e.getMessage() + ")");
        }
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        if (!scanner.hasNextLine()) {
            /* Entrada encerrada (Ctrl+Z / Ctrl+D). Os dados já estão salvos, então podemos sair com segurança. */
            System.out.println();
            System.out.println("Entrada encerrada. Até logo!");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    private String lerTextoObrigatorio(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem);
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Este campo é obrigatório.");
        }
    }

    private int lerInteiro(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Digite um número válido.");
            }
        }
    }

    /* Retorna null quando o campo é opcional e o usuário deixa em branco */
    private LocalDate lerData(String mensagem, boolean opcional) {
        while (true) {
            String texto = lerTexto(mensagem);
            if (texto.isEmpty() && opcional) {
                return null;
            }
            try {
                LocalDate data = LocalDate.parse(texto, FORMATO_DATA);
                /* O GerenciadorTarefas também valida isso; checar aqui evita que o usuário perca o que já digitou */
                if (data.isBefore(gerenciador.hoje())) {
                    System.out.println("O prazo não pode estar no passado.");
                    continue;
                }
                return data;
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato dd/mm/aaaa (ex: 25/12/2026).");
            }
        }
    }

    private Prioridade lerPrioridade(boolean opcional) {
        Prioridade[] opcoes = Prioridade.values();
        while (true) {
            System.out.print("Prioridade (");
            for (int i = 0; i < opcoes.length; i++) {
                System.out.print((i + 1) + " - " + opcoes[i].getDescricao() + (i < opcoes.length - 1 ? " | " : ""));
            }
            String texto = lerTexto("): ");

            if (texto.isEmpty() && opcional) {
                return null;
            }
            try {
                int indice = Integer.parseInt(texto) - 1;
                if (indice >= 0 && indice < opcoes.length) {
                    return opcoes[indice];
                }
            } catch (NumberFormatException e) {
                /* cai na mensagem abaixo */
            }
            System.out.println("Prioridade inválida.");
        }
    }

    private boolean confirmar(String mensagem) {
        return lerTexto(mensagem).equalsIgnoreCase("s");
    }
}
