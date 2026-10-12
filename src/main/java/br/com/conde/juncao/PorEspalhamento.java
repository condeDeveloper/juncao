package br.com.conde.juncao;

/**
 * A JUNCAO POR ESPALHAMENTO: monta uma tabela com um lado e sonda com o outro.
 *
 * <p>E o metodo que todo mundo escolhe por padrao, e com razao: numa entrada comum ele e
 * linear nos dois lados, e os outros dois sao quadratico e n log n.
 *
 * <p>Ele tem, porem, as duas unicas fraquezas de verdade deste repositorio, e as duas sao
 * invisiveis num teste de correcao porque ele responde CERTO em qualquer uma delas. Ele so
 * fica lento.
 *
 * <ul>
 *   <li>CHAVE REPETIDA. O balde de uma chave que aparece mil vezes tem mil linhas, e cada
 *       sondagem dessa chave percorre as mil. Isso nao tem conserto por funcao de
 *       espalhamento nenhuma: as mil linhas TEM que ficar juntas, porque todas casam.
 *   <li>ESPALHAMENTO RUIM. Chaves com estrutura nos bits baixos caem no mesmo balde mesmo
 *       sendo diferentes. Esse tem conserto, e o conserto e uma linha.
 * </ul>
 *
 * <h2>Qual lado monta</h2>
 *
 * <p>Monta sempre o MENOR, porque o que fica na memoria e o lado montado. Essa escolha nao
 * muda o numero de comparacoes de forma apreciavel e muda a memoria pelo fator entre os dois
 * tamanhos.
 */
public final class PorEspalhamento implements Juncao {

    private final Espalhamento espalhamento;

    public PorEspalhamento(Espalhamento espalhamento) {
        this.espalhamento = espalhamento;
    }

    @Override
    public String nome() {
        return "espalhamento " + (espalhamento == Espalhamento.BOA ? "bom" : "cru");
    }

    @Override
    public Resultado juntar(Tabela r, Tabela s, Contador contador) {
        boolean montaS = s.tamanho() <= r.tamanho();

        Tabela montada = montaS ? s : r;
        Tabela sonda = montaS ? r : s;

        int baldes = Integer.highestOneBit(Math.max(4, montada.tamanho())) * 2;
        int[] cabeca = new int[baldes];
        int[] proximo = new int[montada.tamanho()];

        java.util.Arrays.fill(cabeca, -1);

        contador.guarda(montada.tamanho());

        for (int i = 0; i < montada.tamanho(); i++) {
            contador.le();

            int balde = espalhamento.balde(montada.chave(i), baldes);

            proximo[i] = cabeca[balde];
            cabeca[balde] = i;
        }

        Resultado resultado = new Resultado();

        for (int j = 0; j < sonda.tamanho(); j++) {
            contador.le();
            contador.sonda();

            int balde = espalhamento.balde(sonda.chave(j), baldes);

            for (int i = cabeca[balde]; i >= 0; i = proximo[i]) {
                contador.compara();

                if (montada.chave(i) != sonda.chave(j)) continue;

                /*
                 * O PAR SAI SEMPRE COM R NA FRENTE, qualquer que tenha sido o lado montado.
                 * Sem isto, montar o menor lado trocaria a ordem dos pares conforme os
                 * tamanhos das tabelas, e o resultado deixaria de ser comparavel com o dos
                 * outros metodos por um motivo que nao tem nada a ver com a juncao.
                 */
                if (montaS) resultado.escrever(sonda.carga(j), montada.carga(i));
                else resultado.escrever(montada.carga(i), sonda.carga(j));

                contador.escreve();
            }
        }

        return resultado;
    }
}
