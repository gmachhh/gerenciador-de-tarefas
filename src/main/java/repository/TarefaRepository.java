package repository;

import model.Prioridade;
import model.Tarefa;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/* Camada de persistência: salva e carrega as tarefas em um arquivo de texto.
   Formato de cada linha: id;titulo;descricao;prazo;prioridade;concluida */
public class TarefaRepository {

    private static final char SEPARADOR = ';';
    private static final char ESCAPE = '\\';
    private static final int QUANTIDADE_CAMPOS = 6;

    private final Path arquivo;

    public TarefaRepository(String caminhoArquivo) {
        this.arquivo = Path.of(caminhoArquivo);
    }

    /* Escreve primeiro num arquivo temporário e só depois substitui o original.
       Assim, se o programa travar no meio da escrita, o arquivo antigo continua intacto. */
    public void salvar(List<Tarefa> tarefas) throws IOException {
        List<String> linhas = new ArrayList<>();
        for (Tarefa tarefa : tarefas) {
            linhas.add(converterParaLinha(tarefa));
        }

        Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
        Files.write(temporario, linhas, StandardCharsets.UTF_8);

        try {
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            /* Alguns sistemas de arquivos não suportam troca atômica; nesse caso faz a troca normal */
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public List<Tarefa> carregar() throws IOException {
        List<Tarefa> tarefas = new ArrayList<>();
        if (Files.notExists(arquivo)) {
            return tarefas; /* primeira execução: ainda não há arquivo */
        }

        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha.isBlank()) {
                continue;
            }
            try {
                tarefas.add(converterParaTarefa(linha));
            } catch (RuntimeException e) {
                throw new IOException("Linha " + (i + 1) + " do arquivo está inválida: " + e.getMessage(), e);
            }
        }
        return tarefas;
    }

    private String converterParaLinha(Tarefa tarefa) {
        return tarefa.getId() + String.valueOf(SEPARADOR)
                + escapar(tarefa.getTitulo()) + SEPARADOR
                + escapar(tarefa.getDescricao()) + SEPARADOR
                + tarefa.getPrazo() + SEPARADOR          /* LocalDate vira texto no formato 2026-10-11 */
                + tarefa.getPrioridade().name() + SEPARADOR
                + tarefa.isConcluida();
    }

    private Tarefa converterParaTarefa(String linha) {
        List<String> campos = separarCampos(linha);
        if (campos.size() != QUANTIDADE_CAMPOS) {
            throw new IllegalArgumentException("esperava " + QUANTIDADE_CAMPOS + " campos, encontrou " + campos.size());
        }

        int id = Integer.parseInt(campos.get(0));
        String titulo = campos.get(1);
        String descricao = campos.get(2);
        LocalDate prazo = LocalDate.parse(campos.get(3));
        Prioridade prioridade = Prioridade.valueOf(campos.get(4));
        boolean concluida = Boolean.parseBoolean(campos.get(5));

        return new Tarefa(id, titulo, descricao, prazo, prioridade, concluida);
    }

    /* Se o usuário digitar ";" no título, ele vira "\;" para não ser confundido com o separador */
    private String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace(";", "\\;");
    }

    /* Faz o caminho inverso do escapar: percorre a linha caractere por caractere */
    private List<String> separarCampos(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder campoAtual = new StringBuilder();

        for (int i = 0; i < linha.length(); i++) {
            char caractere = linha.charAt(i);

            if (caractere == ESCAPE && i + 1 < linha.length()) {
                i++;
                campoAtual.append(linha.charAt(i)); /* adiciona o próximo caractere literalmente */
            } else if (caractere == SEPARADOR) {
                campos.add(campoAtual.toString());
                campoAtual.setLength(0);
            } else {
                campoAtual.append(caractere);
            }
        }
        campos.add(campoAtual.toString());
        return campos;
    }
}
