package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TarefaTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 6, 15);

    private Tarefa criarTarefa(String titulo) {
        return new Tarefa(1, titulo, "descrição", HOJE, Prioridade.MEDIA);
    }

    @Test
    @DisplayName("Nova tarefa começa pendente")
    void novaTarefaComecaPendente() {
        Tarefa tarefa = criarTarefa("Estudar");

        assertFalse(tarefa.isConcluida());
    }

    @Test
    @DisplayName("Remove espaços extras do título")
    void removeEspacosDoTitulo() {
        Tarefa tarefa = criarTarefa("   Estudar Java   ");

        assertEquals("Estudar Java", tarefa.getTitulo());
    }

    @Test
    @DisplayName("Não aceita título vazio, só com espaços ou nulo")
    void naoAceitaTituloVazio() {
        assertThrows(IllegalArgumentException.class, () -> criarTarefa(""));
        assertThrows(IllegalArgumentException.class, () -> criarTarefa("   "));
        assertThrows(IllegalArgumentException.class, () -> criarTarefa(null));
    }

    @Test
    @DisplayName("Não aceita título com mais de 100 caracteres")
    void naoAceitaTituloMuitoLongo() {
        String tituloLongo = "a".repeat(101);

        assertThrows(IllegalArgumentException.class, () -> criarTarefa(tituloLongo));
    }

    @Test
    @DisplayName("Setter também valida o título")
    void setterValidaTitulo() {
        Tarefa tarefa = criarTarefa("Estudar");

        assertThrows(IllegalArgumentException.class, () -> tarefa.setTitulo(""));
        assertEquals("Estudar", tarefa.getTitulo()); /* o valor antigo continua lá */
    }

    @Test
    @DisplayName("Descrição nula vira texto vazio")
    void descricaoNulaViraVazia() {
        Tarefa tarefa = new Tarefa(1, "Estudar", null, HOJE, Prioridade.BAIXA);

        assertEquals("", tarefa.getDescricao());
    }

    @Test
    @DisplayName("Prazo, prioridade e ID são obrigatórios")
    void camposObrigatorios() {
        assertThrows(IllegalArgumentException.class, () -> new Tarefa(1, "Estudar", "", null, Prioridade.BAIXA));
        assertThrows(IllegalArgumentException.class, () -> new Tarefa(1, "Estudar", "", HOJE, null));
        assertThrows(IllegalArgumentException.class, () -> new Tarefa(0, "Estudar", "", HOJE, Prioridade.BAIXA));
    }

    @Test
    @DisplayName("Concluir e reabrir alteram o status")
    void concluirEReabrir() {
        Tarefa tarefa = criarTarefa("Estudar");

        tarefa.concluir();
        assertTrue(tarefa.isConcluida());

        tarefa.reabrir();
        assertFalse(tarefa.isConcluida());
    }

    @Test
    @DisplayName("Está atrasada só se o prazo passou e não foi concluída")
    void regrasDeAtraso() {
        Tarefa vencida = new Tarefa(1, "Vencida", "", HOJE.minusDays(1), Prioridade.ALTA);
        Tarefa venceHoje = new Tarefa(2, "Vence hoje", "", HOJE, Prioridade.ALTA);
        Tarefa vencidaMasConcluida = new Tarefa(3, "Feita", "", HOJE.minusDays(1), Prioridade.ALTA, true);

        assertTrue(vencida.isAtrasada(HOJE));
        assertFalse(venceHoje.isAtrasada(HOJE));
        assertFalse(vencidaMasConcluida.isAtrasada(HOJE));
    }

    @Test
    @DisplayName("Tarefas com o mesmo ID são iguais e têm o mesmo hashCode")
    void igualdadePorId() {
        Tarefa tarefa = new Tarefa(7, "Uma", "", HOJE, Prioridade.BAIXA);
        Tarefa mesmoId = new Tarefa(7, "Outra", "", HOJE.plusDays(3), Prioridade.ALTA);
        Tarefa outroId = new Tarefa(8, "Uma", "", HOJE, Prioridade.BAIXA);

        assertEquals(tarefa, mesmoId);
        assertEquals(tarefa.hashCode(), mesmoId.hashCode());
        assertNotEquals(tarefa, outroId);
    }
}
