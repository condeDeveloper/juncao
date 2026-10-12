package br.com.conde.juncao;

/**
 * ORDENA E MESCLA: poe os dois lados em ordem de chave e passa por eles uma vez.
 *
 * <p>Ele parece o pior dos tres no papel, porque n log n e mais que n. E e o unico que tem
 * tres propriedades que os outros nao tem.
 *
 * <ul>
 *   <li>Nao tem caso ruim por dados. Chave repetida nao atrapalha a mescla, que percorre a
 *       faixa de iguais dos dois lados e escreve o produto. Nao ha balde para inchar.
 *   <li>Sai ORDENADO. Se a pergunta seguinte tambem for por chave, a proxima juncao ja
 *       recebe a entrada pronta.
 *   <li>Se a entrada JA vem ordenada, o custo cai para uma passada so e ele deixa de
 *       guardar qualquer coisa. E entrada ordenada nao e sorte: e o que um indice entrega.
 * </ul>
 *
 * <h2>O que a medida corrigiu no paragrafo acima</h2>
 *
 * <p>Eu tinha escrito que, com entrada ja ordenada, ele ganha de todo mundo. Em COMPARACOES
 * ele perde: com duzentas mil linhas de cada lado faz 480.014 contra 353.324 do
 * espalhamento, 1,4 vezes mais.
 *
 * <p>O que ele ganha e memoria, e ganha de um jeito que nao e uma fracao: a coluna do maior
 * guardado dele e ZERO, e a do espalhamento e as duzentas mil linhas do lado montado. Numa
 * juncao que nao cabe na memoria, 1,4 vezes mais comparacoes e o preco de conseguir rodar.
 *
 * <p>E ele ganha de verdade no outro lugar, que e quando o RESULTADO e grande: ali ele faz
 * 0,12 comparacoes por par escrito contra 1,00 do espalhamento.
 */
public final class OrdenaEMescla implements Juncao {

    private final boolean jaVemOrdenada;

    public OrdenaEMescla() {
        this(false);
    }

    /**
     * @param jaVemOrdenada quando verdadeiro, confia que as duas tabelas chegam em ordem de
     *                      chave e pula a ordenacao. E a situacao de quem le por um indice.
     */
    public OrdenaEMescla(boolean jaVemOrdenada) {
        this.jaVemOrdenada = jaVemOrdenada;
    }

    @Override
    public String nome() {
        return jaVemOrdenada ? "mescla, ja ordenada" : "ordena e mescla";
    }

    @Override
    public Resultado juntar(Tabela r, Tabela s, Contador contador) {
        Tabela aOrdenada;
        Tabela bOrdenada;

        if (jaVemOrdenada) {
            aOrdenada = r;
            bOrdenada = s;
        } else {
            contador.guarda(r.tamanho() + s.tamanho());

            aOrdenada = r.reordenada(ordenarPorChave(r, contador));
            bOrdenada = s.reordenada(ordenarPorChave(s, contador));
        }

        Resultado resultado = new Resultado();

        int i = 0;
        int j = 0;

        while (i < aOrdenada.tamanho() && j < bOrdenada.tamanho()) {
            contador.le(2);
            contador.compara();

            if (aOrdenada.chave(i) < bOrdenada.chave(j)) {
                i++;
                continue;
            }

            if (aOrdenada.chave(i) > bOrdenada.chave(j)) {
                j++;
                continue;
            }

            /*
             * A FAIXA DE IGUAIS DOS DOIS LADOS, e o produto dela.
             *
             * Avancar so um dos lados aqui seria o erro classico: com a chave 7 aparecendo
             * tres vezes em R e duas em S, o resultado tem seis pares, e um laco que avanca
             * os dois juntos acharia dois.
             */
            int chave = aOrdenada.chave(i);
            int fimDeA = i;
            int fimDeB = j;

            while (fimDeA < aOrdenada.tamanho() && aOrdenada.chave(fimDeA) == chave) {
                contador.compara();
                fimDeA++;
            }

            while (fimDeB < bOrdenada.tamanho() && bOrdenada.chave(fimDeB) == chave) {
                contador.compara();
                fimDeB++;
            }

            for (int a = i; a < fimDeA; a++)
                for (int b = j; b < fimDeB; b++) {
                    resultado.escrever(aOrdenada.carga(a), bOrdenada.carga(b));
                    contador.escreve();
                }

            i = fimDeA;
            j = fimDeB;
        }

        return resultado;
    }

    /**
     * Ordenacao por mescla, de baixo para cima, sobre um vetor de indices.
     *
     * <p>De baixo para cima e nao por recursao porque assim nao ha pilha de chamadas
     * nenhuma, e porque as comparacoes ficam num laco so, visiveis de ser contadas.
     *
     * <p>Por MESCLA e nao por particao porque ela e estavel e tem um numero de comparacoes
     * previsivel. Uma ordenacao rapida daria uma coluna que muda com a semente, e a tabela
     * passaria a medir a sorte do pivo.
     */
    static int[] ordenarPorChave(Tabela tabela, Contador contador) {
        int n = tabela.tamanho();
        int[] atual = new int[n];
        int[] outro = new int[n];

        for (int i = 0; i < n; i++) atual[i] = i;

        for (int largura = 1; largura < n; largura *= 2) {
            for (int inicio = 0; inicio < n; inicio += 2 * largura) {
                int meio = Math.min(inicio + largura, n);
                int fim = Math.min(inicio + 2 * largura, n);

                int a = inicio;
                int b = meio;
                int k = inicio;

                while (a < meio && b < fim) {
                    contador.compara();

                    if (tabela.chave(atual[a]) <= tabela.chave(atual[b])) outro[k++] = atual[a++];
                    else outro[k++] = atual[b++];
                }

                while (a < meio) outro[k++] = atual[a++];
                while (b < fim) outro[k++] = atual[b++];
            }

            int[] guarda = atual;

            atual = outro;
            outro = guarda;
        }

        return atual;
    }
}
