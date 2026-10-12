package br.com.conde.juncao;

import java.util.Arrays;

/**
 * O RESULTADO de uma juncao: os pares de linhas que casaram.
 *
 * <p>Cada par e guardado num long so, com a carga de R nos 32 bits de cima e a de S nos de
 * baixo. Isso nao e economia de memoria: e o que torna a comparacao entre dois resultados
 * uma ordenacao de vetor de long e nada mais.
 *
 * <p>Um resultado de juncao e um MULTICONJUNTO e nao uma lista: dois metodos corretos podem
 * produzir os mesmos pares em ordens diferentes, e nenhum dos dois esta errado por isso. Se
 * o juiz comparasse a ordem, ele reprovaria o espalhamento por produzir na ordem dos baldes
 * e o ordena-e-mescla por produzir na ordem das chaves.
 */
public final class Resultado {

    private long[] pares = new long[64];
    private int quantos;

    public void escrever(int deR, int deS) {
        if (quantos == pares.length) pares = Arrays.copyOf(pares, pares.length * 2);

        pares[quantos++] = ((long) deR << 32) | (deS & 0xFFFFFFFFL);
    }

    public int quantos() {
        return quantos;
    }

    /** Os pares em ordem, que e a forma em que dois resultados podem ser comparados. */
    public long[] ordenado() {
        long[] copia = Arrays.copyOf(pares, quantos);

        Arrays.sort(copia);

        return copia;
    }

    public int deR(int i) {
        return (int) (pares[i] >>> 32);
    }

    public int deS(int i) {
        return (int) pares[i];
    }
}
