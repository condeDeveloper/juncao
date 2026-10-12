package br.com.conde.juncao;

/**
 * Uma TABELA: chaves e cargas em vetores paralelos.
 *
 * <p>Vetores paralelos e nao uma lista de objetos. A juncao toca so a chave na hora de
 * decidir e so a carga na hora de escrever, e separando as duas coisas fica visivel que o
 * custo do metodo nao depende do tamanho da linha. Numa lista de objetos isso ficaria
 * escondido atras de um campo.
 */
public final class Tabela {

    private final String nome;
    private final int[] chaves;
    private final int[] cargas;

    public Tabela(String nome, int[] chaves, int[] cargas) {
        if (chaves.length != cargas.length)
            throw new IllegalArgumentException("chaves e cargas de tamanhos diferentes");

        this.nome = nome;
        this.chaves = chaves;
        this.cargas = cargas;
    }

    public static Tabela de(String nome, int[] chaves) {
        int[] cargas = new int[chaves.length];

        for (int i = 0; i < chaves.length; i++) cargas[i] = i;

        return new Tabela(nome, chaves, cargas);
    }

    public String nome() {
        return nome;
    }

    public int tamanho() {
        return chaves.length;
    }

    public int chave(int i) {
        return chaves[i];
    }

    public int carga(int i) {
        return cargas[i];
    }

    public int[] chaves() {
        return chaves;
    }

    /** Uma copia com as linhas em outra ordem. Os metodos que ordenam nao mexem na original. */
    public Tabela reordenada(int[] ordem) {
        int[] novasChaves = new int[ordem.length];
        int[] novasCargas = new int[ordem.length];

        for (int i = 0; i < ordem.length; i++) {
            novasChaves[i] = chaves[ordem[i]];
            novasCargas[i] = cargas[ordem[i]];
        }

        return new Tabela(nome, novasChaves, novasCargas);
    }
}
