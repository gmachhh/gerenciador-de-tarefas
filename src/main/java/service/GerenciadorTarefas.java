package service;

import exception.TarefaNaoEncontradaException;
import model.Estatisticas;
import model.Prioridade;
import model.Tarefa;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/* Camada de serviço: concentra as regras de negócio. Não sabe nada de console nem de arquivo. */
public class GerenciadorTarefas {

    private final List<Tarefa> tarefas;
    private final Clock clock; /* "relógio" do sistema: nos testes, passamos um relógio parado num dia fixo */
    private int proximoId;

    public GerenciadorTarefas() {
        this(new ArrayList<>());
    }

    public GerenciadorTarefas(List<Tarefa> tarefasIniciais) {
        this(tarefasIniciais, Clock.systemDefaultZone());
    }

    public GerenciadorTarefas(List<Tarefa> tarefasIniciais, Clock clock) {
        this.tarefas = new ArrayList<>(tarefasIniciais);
        this.clock = clock;
        /* Continua a contagem a partir do maior ID já existente */
        this.proximoId = tarefas.stream()
                .mapToInt(Tarefa::getId)
                .max()
                .orElse(0) + 1;
    }

    /* CRUD */

    public Tarefa adicionarTarefa(String titulo, String descricao, LocalDate prazo, Prioridade prioridade) {
        validarPrazoFuturo(prazo);
        Tarefa tarefa = new Tarefa(proximoId, titulo, descricao, prazo, prioridade);
        tarefas.add(tarefa);
        proximoId++;
        return tarefa;
    }

    public Tarefa buscarPorId(int id) {
        return tarefas.stream()
                .filter(tarefa -> tarefa.getId() == id)
                .findFirst()
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    /* Campos recebidos como null são mantidos como estão */
    public Tarefa editarTarefa(int id, String novoTitulo, String novaDescricao, LocalDate novoPrazo, Prioridade novaPrioridade) {
        Tarefa tarefa = buscarPorId(id);

        if (novoTitulo != null) {
            tarefa.setTitulo(novoTitulo);
        }
        if (novaDescricao != null) {
            tarefa.setDescricao(novaDescricao);
        }
        if (novoPrazo != null) {
            validarPrazoFuturo(novoPrazo);
            tarefa.setPrazo(novoPrazo);
        }
        if (novaPrioridade != null) {
            tarefa.setPrioridade(novaPrioridade);
        }
        return tarefa;
    }

    public void removerTarefa(int id) {
        Tarefa tarefa = buscarPorId(id);
        tarefas.remove(tarefa);
    }

    public void concluirTarefa(int id) {
        buscarPorId(id).concluir();
    }

    public void reabrirTarefa(int id) {
        buscarPorId(id).reabrir();
    }

    /* Consultas */

    public List<Tarefa> listarTodas() {
        return List.copyOf(tarefas); /* cópia imutável: quem chama não consegue bagunçar a lista interna */
    }

    public List<Tarefa> listarOrdenadasPorPrazo() {
        return tarefas.stream()
                .sorted(Comparator.comparing(Tarefa::getPrazo))
                .toList();
    }

    public List<Tarefa> listarOrdenadasPorPrioridade() {
        return tarefas.stream()
                .sorted(Comparator.comparing(Tarefa::getPrioridade).reversed()
                        .thenComparing(Tarefa::getPrazo))
                .toList();
    }

    public List<Tarefa> listarPorStatus(boolean concluida) {
        return tarefas.stream()
                .filter(tarefa -> tarefa.isConcluida() == concluida)
                .toList();
    }

    public List<Tarefa> listarPorPrioridade(Prioridade prioridade) {
        return tarefas.stream()
                .filter(tarefa -> tarefa.getPrioridade() == prioridade)
                .toList();
    }

    public List<Tarefa> listarAtrasadas() {
        LocalDate hoje = hoje();
        return tarefas.stream()
                .filter(tarefa -> tarefa.isAtrasada(hoje))
                .toList();
    }

    public List<Tarefa> buscarPorTexto(String termo) {
        String termoMinusculo = termo.toLowerCase();
        return tarefas.stream()
                .filter(tarefa -> tarefa.getTitulo().toLowerCase().contains(termoMinusculo)
                        || tarefa.getDescricao().toLowerCase().contains(termoMinusculo))
                .toList();
    }

    public Estatisticas gerarEstatisticas() {
        int total = tarefas.size();
        int concluidas = listarPorStatus(true).size();
        int atrasadas = listarAtrasadas().size();
        return new Estatisticas(total, concluidas, total - concluidas, atrasadas);
    }

    /* Datas */

    public LocalDate hoje() {
        return LocalDate.now(clock);
    }

    public boolean estaAtrasada(Tarefa tarefa) {
        return tarefa.isAtrasada(hoje());
    }

    private void validarPrazoFuturo(LocalDate prazo) {
        if (prazo != null && prazo.isBefore(hoje())) {
            throw new IllegalArgumentException("O prazo não pode estar no passado.");
        }
    }
}
