package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Tarefa {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int TAMANHO_MAXIMO_TITULO = 100;

    private final int id; /* final: o ID nunca muda depois que a tarefa é criada */
    private String titulo;
    private String descricao;
    private LocalDate prazo;
    private Prioridade prioridade;
    private boolean concluida;

    /* Construtor usado ao criar uma tarefa nova (sempre começa pendente) */
    public Tarefa(int id, String titulo, String descricao, LocalDate prazo, Prioridade prioridade) {
        this(id, titulo, descricao, prazo, prioridade, false);
    }

    /* Construtor completo, usado ao carregar tarefas salvas no arquivo */
    public Tarefa(int id, String titulo, String descricao, LocalDate prazo, Prioridade prioridade, boolean concluida) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID deve ser maior que zero.");
        }
        this.id = id;
        this.titulo = validarTitulo(titulo);
        this.descricao = validarDescricao(descricao);
        this.prazo = validarPrazo(prazo);
        this.prioridade = validarPrioridade(prioridade);
        this.concluida = concluida;
    }

    /* Getters: retornam o valor dos atributos */

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public boolean isConcluida() {
        return concluida;
    }

    /* Setters: alteram os atributos, sempre passando pela validação */

    public void setTitulo(String titulo) {
        this.titulo = validarTitulo(titulo);
    }

    public void setDescricao(String descricao) {
        this.descricao = validarDescricao(descricao);
    }

    public void setPrazo(LocalDate prazo) {
        this.prazo = validarPrazo(prazo);
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = validarPrioridade(prioridade);
    }

    /* Regras de negócio */

    public void concluir() {
        this.concluida = true;
    }

    public void reabrir() {
        this.concluida = false;
    }

    /* Recebe a data de hoje em vez de usar LocalDate.now(): assim os testes conseguem simular qualquer dia */
    public boolean isAtrasada(LocalDate hoje) {
        return !concluida && prazo.isBefore(hoje);
    }

    /* Validações */

    private static String validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título não pode ser vazio.");
        }
        String tituloLimpo = titulo.trim();
        if (tituloLimpo.length() > TAMANHO_MAXIMO_TITULO) {
            throw new IllegalArgumentException("O título pode ter no máximo " + TAMANHO_MAXIMO_TITULO + " caracteres.");
        }
        return tituloLimpo;
    }

    private static String validarDescricao(String descricao) {
        return descricao == null ? "" : descricao.trim();
    }

    private static LocalDate validarPrazo(LocalDate prazo) {
        if (prazo == null) {
            throw new IllegalArgumentException("O prazo é obrigatório.");
        }
        return prazo;
    }

    private static Prioridade validarPrioridade(Prioridade prioridade) {
        if (prioridade == null) {
            throw new IllegalArgumentException("A prioridade é obrigatória.");
        }
        return prioridade;
    }

    /* Duas tarefas são iguais se tiverem o mesmo ID */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Tarefa tarefa)) {
            return false;
        }
        return id == tarefa.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /* Representação para depuração (logs, debugger). A exibição bonita para o usuário fica no Menu. */
    @Override
    public String toString() {
        return String.format("Tarefa{id=%d, titulo='%s', prazo=%s, prioridade=%s, concluida=%s}",
                id, titulo, prazo.format(FORMATO_DATA), prioridade, concluida);
    }
}
