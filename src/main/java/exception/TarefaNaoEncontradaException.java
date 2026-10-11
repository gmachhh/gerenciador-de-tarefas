package exception;

/* Exceção personalizada: deixa claro qual foi o erro em vez de um genérico "null" */
public class TarefaNaoEncontradaException extends RuntimeException {

    public TarefaNaoEncontradaException(int id) {
        super("Tarefa com ID " + id + " não encontrada.");
    }
}
