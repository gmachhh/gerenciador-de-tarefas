package service;

import exception.TarefaNaoEncontradaException;
import model.Estatisticas;
import model.Prioridade;
import model.Tarefa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GerenciadorTarefasTest {

    /* Relógio parado: para os testes, "hoje" é sempre 15/06/2026 */
    private static final LocalDate HOJE = LocalDate.of(2026, 6, 15);
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final Clock RELOGIO_FIXO = Clock.fixed(HOJE.atStartOfDay(FUSO).toInstant(), FUSO);

    private GerenciadorTarefas gerenciador;

    @BeforeEach
    void prepararCenario() {
        /* Roda antes de cada teste: cada teste começa com um gerenciador novo e vazio */
        gerenciador = new GerenciadorTarefas(new ArrayList<>(), RELOGIO_FIXO);
    }

    @Test
    @DisplayName("Usa o relógio injetado para saber a data de hoje")
    void usaRelogioInjetado() {
        assertEquals(HOJE, gerenciador.hoje());
    }

    @Test
    @DisplayName("Gera IDs sequenciais começando em 1")
    void geraIdsSequenciais() {
        Tarefa primeira = gerenciador.adicionarTarefa("A", "", HOJE, Prioridade.BAIXA);
        Tarefa segunda = gerenciador.adicionarTarefa("B", "", HOJE, Prioridade.BAIXA);

        assertEquals(1, primeira.getId());
        assertEquals(2, segunda.getId());
    }

    @Test
    @DisplayName("Continua a contagem de IDs a partir das tarefas carregadas")
    void continuaIdsDasTarefasCarregadas() {
        List<Tarefa> salvas = List.of(new Tarefa(5, "Antiga", "", HOJE, Prioridade.BAIXA));
        GerenciadorTarefas comDados = new GerenciadorTarefas(salvas, RELOGIO_FIXO);

        Tarefa nova = comDados.adicionarTarefa("Nova", "", HOJE, Prioridade.BAIXA);

        assertEquals(6, nova.getId());
    }

    @Test
    @DisplayName("Não aceita prazo no passado ao adicionar")
    void naoAceitaPrazoNoPassado() {
        assertThrows(IllegalArgumentException.class,
                () -> gerenciador.adicionarTarefa("A", "", HOJE.minusDays(1), Prioridade.BAIXA));
        assertTrue(gerenciador.listarTodas().isEmpty());
    }

    @Test
    @DisplayName("Tarefa inválida não consome um ID")
    void tarefaInvalidaNaoConsomeId() {
        assertThrows(IllegalArgumentException.class,
                () -> gerenciador.adicionarTarefa("", "", HOJE, Prioridade.BAIXA));

        Tarefa valida = gerenciador.adicionarTarefa("Válida", "", HOJE, Prioridade.BAIXA);

        assertEquals(1, valida.getId());
    }

    @Test
    @DisplayName("Lança exceção ao buscar ID inexistente")
    void buscarIdInexistente() {
        TarefaNaoEncontradaException erro = assertThrows(TarefaNaoEncontradaException.class,
                () -> gerenciador.buscarPorId(99));

        assertEquals("Tarefa com ID 99 não encontrada.", erro.getMessage());
    }

    @Test
    @DisplayName("Editar altera só os campos informados")
    void editarAlteraSoCamposInformados() {
        Tarefa tarefa = gerenciador.adicionarTarefa("Título", "Descrição", HOJE, Prioridade.BAIXA);

        gerenciador.editarTarefa(tarefa.getId(), "Novo título", null, null, Prioridade.ALTA);

        assertEquals("Novo título", tarefa.getTitulo());
        assertEquals("Descrição", tarefa.getDescricao());
        assertEquals(HOJE, tarefa.getPrazo());
        assertEquals(Prioridade.ALTA, tarefa.getPrioridade());
    }

    @Test
    @DisplayName("Remove a tarefa pelo ID")
    void removerTarefa() {
        Tarefa tarefa = gerenciador.adicionarTarefa("A", "", HOJE, Prioridade.BAIXA);

        gerenciador.removerTarefa(tarefa.getId());

        assertTrue(gerenciador.listarTodas().isEmpty());
        assertThrows(TarefaNaoEncontradaException.class, () -> gerenciador.removerTarefa(tarefa.getId()));
    }

    @Test
    @DisplayName("Concluir e reabrir pelo ID")
    void concluirEReabrir() {
        Tarefa tarefa = gerenciador.adicionarTarefa("A", "", HOJE, Prioridade.BAIXA);

        gerenciador.concluirTarefa(tarefa.getId());
        assertTrue(tarefa.isConcluida());

        gerenciador.reabrirTarefa(tarefa.getId());
        assertFalse(tarefa.isConcluida());
    }

    @Test
    @DisplayName("A lista retornada não pode ser alterada por fora")
    void listaRetornadaEImutavel() {
        gerenciador.adicionarTarefa("A", "", HOJE, Prioridade.BAIXA);

        List<Tarefa> lista = gerenciador.listarTodas();

        assertThrows(UnsupportedOperationException.class, lista::clear);
    }

    @Test
    @DisplayName("Ordena por prioridade (maior primeiro) e desempata pelo prazo")
    void ordenaPorPrioridade() {
        Tarefa baixa = gerenciador.adicionarTarefa("Baixa", "", HOJE, Prioridade.BAIXA);
        Tarefa altaDepois = gerenciador.adicionarTarefa("Alta depois", "", HOJE.plusDays(5), Prioridade.ALTA);
        Tarefa altaAntes = gerenciador.adicionarTarefa("Alta antes", "", HOJE.plusDays(1), Prioridade.ALTA);
        Tarefa media = gerenciador.adicionarTarefa("Média", "", HOJE, Prioridade.MEDIA);

        List<Tarefa> ordenadas = gerenciador.listarOrdenadasPorPrioridade();

        assertEquals(List.of(altaAntes, altaDepois, media, baixa), ordenadas);
    }

    @Test
    @DisplayName("Ordena por prazo (mais próximo primeiro)")
    void ordenaPorPrazo() {
        Tarefa depois = gerenciador.adicionarTarefa("Depois", "", HOJE.plusDays(10), Prioridade.BAIXA);
        Tarefa antes = gerenciador.adicionarTarefa("Antes", "", HOJE.plusDays(1), Prioridade.BAIXA);

        assertEquals(List.of(antes, depois), gerenciador.listarOrdenadasPorPrazo());
    }

    @Test
    @DisplayName("Lista atrasadas ignorando as concluídas")
    void listaAtrasadas() {
        Tarefa atrasada = new Tarefa(1, "Atrasada", "", HOJE.minusDays(2), Prioridade.ALTA);
        Tarefa concluida = new Tarefa(2, "Feita", "", HOJE.minusDays(2), Prioridade.ALTA, true);
        Tarefa emDia = new Tarefa(3, "Em dia", "", HOJE.plusDays(2), Prioridade.ALTA);
        GerenciadorTarefas comDados = new GerenciadorTarefas(List.of(atrasada, concluida, emDia), RELOGIO_FIXO);

        assertEquals(List.of(atrasada), comDados.listarAtrasadas());
        assertTrue(comDados.estaAtrasada(atrasada));
        assertFalse(comDados.estaAtrasada(emDia));
    }

    @Test
    @DisplayName("Filtra por status e por prioridade")
    void filtros() {
        Tarefa pendente = gerenciador.adicionarTarefa("Pendente", "", HOJE, Prioridade.ALTA);
        Tarefa concluida = gerenciador.adicionarTarefa("Concluída", "", HOJE, Prioridade.BAIXA);
        gerenciador.concluirTarefa(concluida.getId());

        assertEquals(List.of(pendente), gerenciador.listarPorStatus(false));
        assertEquals(List.of(concluida), gerenciador.listarPorStatus(true));
        assertEquals(List.of(pendente), gerenciador.listarPorPrioridade(Prioridade.ALTA));
    }

    @Test
    @DisplayName("Busca por texto no título e na descrição, sem diferenciar maiúsculas")
    void buscaPorTexto() {
        Tarefa noTitulo = gerenciador.adicionarTarefa("Estudar JAVA", "", HOJE, Prioridade.BAIXA);
        Tarefa naDescricao = gerenciador.adicionarTarefa("Ler", "livro de java", HOJE, Prioridade.BAIXA);
        gerenciador.adicionarTarefa("Academia", "treino", HOJE, Prioridade.BAIXA);

        assertEquals(List.of(noTitulo, naDescricao), gerenciador.buscarPorTexto("Java"));
    }

    @Test
    @DisplayName("Calcula as estatísticas corretamente")
    void estatisticas() {
        Tarefa atrasada = new Tarefa(1, "Atrasada", "", HOJE.minusDays(1), Prioridade.ALTA);
        Tarefa concluida = new Tarefa(2, "Feita", "", HOJE, Prioridade.ALTA, true);
        Tarefa pendente = new Tarefa(3, "Pendente", "", HOJE.plusDays(1), Prioridade.ALTA);
        Tarefa outraConcluida = new Tarefa(4, "Feita 2", "", HOJE, Prioridade.ALTA, true);
        GerenciadorTarefas comDados = new GerenciadorTarefas(
                List.of(atrasada, concluida, pendente, outraConcluida), RELOGIO_FIXO);

        Estatisticas estatisticas = comDados.gerarEstatisticas();

        assertEquals(new Estatisticas(4, 2, 2, 1), estatisticas);
        assertEquals(50.0, estatisticas.percentualConcluido());
    }

    @Test
    @DisplayName("Percentual é zero quando não há tarefas")
    void percentualSemTarefas() {
        assertEquals(0.0, gerenciador.gerarEstatisticas().percentualConcluido());
    }
}
