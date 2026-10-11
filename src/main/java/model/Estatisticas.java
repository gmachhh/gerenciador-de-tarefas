package model;

/* record: uma classe imutável feita só para guardar dados.
   O Java gera automaticamente construtor, getters, equals, hashCode e toString. */
public record Estatisticas(int total, int concluidas, int pendentes, int atrasadas) {

    public double percentualConcluido() {
        if (total == 0) {
            return 0;
        }
        return concluidas * 100.0 / total;
    }
}
