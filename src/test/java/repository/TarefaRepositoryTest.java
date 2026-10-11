package repository;

import model.Prioridade;
import model.Tarefa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TarefaRepositoryTest {

    /* O JUnit cria uma pasta temporária para cada teste e apaga depois: não sujamos o projeto */
    @TempDir
    Path pastaTemporaria;

    private Path arquivo;
    private TarefaRepository repository;

    @BeforeEach
    void prepararCenario() {
        arquivo = pastaTemporaria.resolve("tarefas.csv");
        repository = new TarefaRepository(arquivo.toString());
    }

    @Test
    @DisplayName("Retorna lista vazia quando o arquivo ainda não existe")
    void arquivoInexistente() throws IOException {
        assertTrue(repository.carregar().isEmpty());
    }

    @Test
    @DisplayName("Salva e carrega mantendo todos os dados")
    void salvarECarregar() throws IOException {
        Tarefa pendente = new Tarefa(1, "Estudar Java", "POO e streams", LocalDate.of(2026, 12, 20), Prioridade.ALTA);
        Tarefa concluida = new Tarefa(2, "Comprar pão", "", LocalDate.of(2026, 11, 15), Prioridade.BAIXA, true);

        repository.salvar(List.of(pendente, concluida));
        List<Tarefa> carregadas = repository.carregar();

        assertEquals(2, carregadas.size());
        assertMesmosDados(pendente, carregadas.get(0));
        assertMesmosDados(concluida, carregadas.get(1));
    }

    @Test
    @DisplayName("Textos com ponto e vírgula e barra invertida sobrevivem ao salvamento")
    void caracteresEspeciais() throws IOException {
        Tarefa tarefa = new Tarefa(1, "Título; com separador", "C:\\pasta\\arquivo; fim", LocalDate.of(2026, 1, 1), Prioridade.MEDIA);

        repository.salvar(List.of(tarefa));
        Tarefa carregada = repository.carregar().get(0);

        assertEquals("Título; com separador", carregada.getTitulo());
        assertEquals("C:\\pasta\\arquivo; fim", carregada.getDescricao());
    }

    @Test
    @DisplayName("Não deixa o arquivo temporário para trás")
    void naoDeixaArquivoTemporario() throws IOException {
        repository.salvar(List.of(new Tarefa(1, "A", "", LocalDate.of(2026, 1, 1), Prioridade.BAIXA)));

        assertTrue(Files.exists(arquivo));
        assertFalse(Files.exists(pastaTemporaria.resolve("tarefas.csv.tmp")));
    }

    @Test
    @DisplayName("Avisa qual linha está inválida")
    void linhaInvalida() throws IOException {
        Files.write(arquivo, List.of(
                "1;Válida;;2026-01-01;ALTA;false",
                "2;Prioridade inexistente;;2026-01-01;SUPER;false"
        ), StandardCharsets.UTF_8);

        IOException erro = assertThrows(IOException.class, () -> repository.carregar());

        assertTrue(erro.getMessage().startsWith("Linha 2"));
    }

    @Test
    @DisplayName("Ignora linhas em branco")
    void ignoraLinhasEmBranco() throws IOException {
        Files.write(arquivo, List.of("", "1;Tarefa;;2026-01-01;ALTA;false", "   "), StandardCharsets.UTF_8);

        assertEquals(1, repository.carregar().size());
    }

    private void assertMesmosDados(Tarefa esperada, Tarefa atual) {
        assertEquals(esperada.getId(), atual.getId());
        assertEquals(esperada.getTitulo(), atual.getTitulo());
        assertEquals(esperada.getDescricao(), atual.getDescricao());
        assertEquals(esperada.getPrazo(), atual.getPrazo());
        assertEquals(esperada.getPrioridade(), atual.getPrioridade());
        assertEquals(esperada.isConcluida(), atual.isConcluida());
    }
}
