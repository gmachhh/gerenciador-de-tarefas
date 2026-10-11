import model.Tarefa;
import repository.TarefaRepository;
import service.GerenciadorTarefas;
import ui.Menu;

import java.io.IOException;
import java.util.List;

public class Main {

    private static final String ARQUIVO_DADOS = "tarefas.csv";

    public static void main(String[] args) {
        TarefaRepository repository = new TarefaRepository(ARQUIVO_DADOS);

        List<Tarefa> tarefasSalvas;
        try {
            tarefasSalvas = repository.carregar();
        } catch (IOException e) {
            System.out.println("Erro ao carregar " + ARQUIVO_DADOS + ": " + e.getMessage());
            System.out.println("Corrija ou remova o arquivo e tente novamente.");
            return;
        }

        GerenciadorTarefas gerenciador = new GerenciadorTarefas(tarefasSalvas);
        Menu menu = new Menu(gerenciador, repository);
        menu.iniciar();
    }
}
