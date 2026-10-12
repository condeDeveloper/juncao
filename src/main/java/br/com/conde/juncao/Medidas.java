package br.com.conde.juncao;

import java.util.ArrayList;
import java.util.List;

/**
 * As MEDIDAS, em comparacoes de chave. Nunca segundos.
 *
 * <p>A COMPARACAO DE CHAVE e contada em TODA fase, inclusive na ordenacao e na montagem da
 * tabela de espalhamento. Contar so a fase de casamento seria o jeito mais facil de fazer o
 * espalhamento parecer gratuito, porque ele cobra na montagem, e de fazer o ordena-e-mescla
 * parecer linear, porque ele cobra na ordenacao.
 *
 * <p>Toda tabela daqui tem um teste do lado que afirma os mesmos numeros.
 */
public final class Medidas {

    private Medidas() {
    }

    /**
     * Quantas linhas por tabela nas medidas que incluem o laco aninhado.
     *
     * <p>Quatro mil e nao cem mil porque o laco aninhado e quadratico: com cem mil linhas de
     * cada lado ele faria dez BILHOES de comparacoes, e ele aparece em quase toda tabela
     * porque e a regua. O que as tabelas afirmam sao razoes entre metodos, e elas nao mudam
     * com o tamanho.
     */
    public static final int LINHAS = 4_000;

    /** O tamanho das medidas que nao tem o laco aninhado dentro. */
    public static final int LINHAS_GRANDES = 200_000;

    public static final long SEMENTE = 20_251_011L;

    // ───────────────────── 1. a correcao ─────────────────────

    public record LinhaCorrecao(String metodo, String entrada, int paresCertos,
                                int divergencias) {
    }

    /**
     * TODO METODO DEVOLVE O MESMO MULTICONJUNTO DE PARES, em toda entrada.
     *
     * <p>Isso e o que faz a comparacao de custo valer alguma coisa. Se um deles respondesse
     * um pouco diferente, a coluna de comparacoes dele estaria medindo um trabalho que os
     * outros nao fazem.
     */
    public static List<LinhaCorrecao> aCorrecao() {
        List<LinhaCorrecao> linhas = new ArrayList<>();
        int n = 600;

        for (var entrada : Tabelas.todas(n, SEMENTE).entrySet()) {
            Tabela r = entrada.getValue()[0];
            Tabela s = entrada.getValue()[1];

            for (var metodo : Juiz.osMetodos().entrySet()) {
                Juiz.Veredito veredito = Juiz.julgar(metodo.getValue().get(), r, s);

                linhas.add(new LinhaCorrecao(metodo.getKey(), entrada.getKey(),
                        veredito.paresCertos(), veredito.divergencias()));
            }

            Juiz.Veredito daOrdenada =
                    Juiz.julgarOrdenado(new OrdenaEMescla(true), r, s);

            linhas.add(new LinhaCorrecao("mescla, ja ordenada", entrada.getKey(),
                    daOrdenada.paresCertos(), daOrdenada.divergencias()));
        }

        return linhas;
    }

    // ───────────────────── 2. o custo por tamanho ─────────────────────

    public record LinhaTamanho(int linhas, String metodo, long comparacoes, int pares,
                               long maiorGuardado, long linhasLidas) {
    }

    /**
     * O CUSTO POR TAMANHO, com chaves sorteadas.
     *
     * <p>E a tabela que todo livro tem, e ela esta aqui para que as outras possam contradizer
     * alguma coisa. O laco aninhado cresce com o quadrado, o espalhamento cresce com a soma
     * dos dois lados e o ordena-e-mescla cresce com n log n.
     */
    public static List<LinhaTamanho> oTamanho() {
        List<LinhaTamanho> linhas = new ArrayList<>();

        for (int n : new int[] {500, 1_000, 2_000, 4_000, 8_000}) {
            Tabela r = Tabelas.sorteadas("R", n, n, SEMENTE);
            Tabela s = Tabelas.sorteadas("S", n, n, SEMENTE + 1);

            for (var metodo : Juiz.osMetodos().entrySet()) {
                if (metodo.getKey().equals("espalhamento cru")) continue;

                Contador contador = new Contador();
                Resultado resultado = metodo.getValue().get().juntar(r, s, contador);

                linhas.add(new LinhaTamanho(n, metodo.getKey(), contador.comparacoes(),
                        resultado.quantos(), contador.maiorGuardado(), contador.linhasLidas()));
            }
        }

        return linhas;
    }

    // ───────────────────── 3. a repeticao ─────────────────────

    public record LinhaRepeticao(int distintas, double linhasPorChave, long comparacoes,
                                 int pares, double porSondagem, double vezesOLaco) {
    }

    /**
     * CHAVES REPETIDAS, que e o caso ruim do espalhamento que NAO tem conserto.
     *
     * <p>O balde de uma chave que aparece mil vezes tem mil linhas, e cada sondagem dessa
     * chave percorre as mil. Nenhuma funcao de espalhamento resolve: as mil linhas TEM que
     * ficar juntas, porque todas casam.
     *
     * <p>A coluna da direita e quantas vezes o laco aninhado o espalhamento custa. Enquanto
     * ela for pequena, o metodo esta ganhando.
     */
    public static List<LinhaRepeticao> aRepeticao() {
        List<LinhaRepeticao> linhas = new ArrayList<>();
        int n = LINHAS;

        for (int distintas : new int[] {n, n / 10, n / 100, n / 200, n / 400}) {
            Tabela r = Tabelas.repetidas("R", n, distintas, SEMENTE);
            Tabela s = Tabelas.repetidas("S", n, distintas, SEMENTE + 1);

            Contador doLaco = new Contador();

            new LacoAninhado().juntar(r, s, doLaco);

            Contador contador = new Contador();
            Resultado resultado = new PorEspalhamento(Espalhamento.BOA).juntar(r, s, contador);

            linhas.add(new LinhaRepeticao(distintas, (double) n / distintas,
                    contador.comparacoes(), resultado.quantos(),
                    contador.comparacoesPorSondagem(),
                    (double) doLaco.comparacoes() / contador.comparacoes()));
        }

        return linhas;
    }

    // ───────────────────── 4. o espalhamento ─────────────────────

    public record LinhaEspalhamento(String entrada, String funcao, long comparacoes,
                                    double porSondagem, double vezesABoa) {
    }

    /**
     * O ESPALHAMENTO CRU CONTRA O BOM, que e o caso ruim que TEM conserto.
     *
     * <p>Com chaves multiplas de 1.024, o indice do balde e o resto por uma potencia de
     * dois, ou seja, os bits baixos, e os bits baixos sao todos zero. Tudo cai num balde so.
     *
     * <p>E as chaves sao TODAS DIFERENTES. Nao e repeticao: e a funcao.
     */
    public static List<LinhaEspalhamento> oEspalhamento() {
        List<LinhaEspalhamento> linhas = new ArrayList<>();
        int n = LINHAS;

        var entradas = new java.util.LinkedHashMap<String, Tabela[]>();

        entradas.put("sorteadas", new Tabela[] {
                Tabelas.sorteadas("R", n, n, SEMENTE), Tabelas.sorteadas("S", n, n, SEMENTE + 1)});

        entradas.put("com estrutura", new Tabela[] {
                Tabelas.comEstrutura("R", n, 1_024, SEMENTE),
                Tabelas.comEstrutura("S", n, 1_024, SEMENTE + 1)});

        for (var entrada : entradas.entrySet()) {
            Tabela r = entrada.getValue()[0];
            Tabela s = entrada.getValue()[1];
            long daBoa = 0;

            for (Espalhamento funcao : new Espalhamento[] {Espalhamento.BOA, Espalhamento.CRUA}) {
                Contador contador = new Contador();

                new PorEspalhamento(funcao).juntar(r, s, contador);

                if (funcao == Espalhamento.BOA) daBoa = contador.comparacoes();

                linhas.add(new LinhaEspalhamento(entrada.getKey(),
                        funcao == Espalhamento.BOA ? "bom" : "cru",
                        contador.comparacoes(), contador.comparacoesPorSondagem(),
                        (double) contador.comparacoes() / daBoa));
            }
        }

        return linhas;
    }

    // ───────────────────── 5. a entrada ja ordenada ─────────────────────

    public record LinhaOrdenada(String metodo, long comparacoes, long maiorGuardado,
                                double vezesOMelhor) {
    }

    /**
     * QUANDO A ENTRADA JA VEM ORDENADA, com {@link #LINHAS_GRANDES} linhas de cada lado.
     *
     * <p>Sem o laco aninhado na tabela, que com duzentas mil linhas de cada lado faria
     * quarenta bilhoes de comparacoes.
     *
     * <p>Entrada ordenada nao e sorte: e o que um indice entrega, e e a situacao normal de
     * uma juncao por chave primaria num banco de dados.
     */
    public static List<LinhaOrdenada> aOrdenada() {
        List<LinhaOrdenada> linhas = new ArrayList<>();
        int n = LINHAS_GRANDES;

        Tabela r = Tabelas.ordenada(Tabelas.sorteadas("R", n, n, SEMENTE));
        Tabela s = Tabelas.ordenada(Tabelas.sorteadas("S", n, n, SEMENTE + 1));

        var metodos = new java.util.LinkedHashMap<String, Juncao>();

        metodos.put("espalhamento bom", new PorEspalhamento(Espalhamento.BOA));
        metodos.put("ordena e mescla", new OrdenaEMescla());
        metodos.put("mescla, ja ordenada", new OrdenaEMescla(true));

        long melhor = Long.MAX_VALUE;
        List<long[]> brutos = new ArrayList<>();

        for (var metodo : metodos.entrySet()) {
            Contador contador = new Contador();

            metodo.getValue().juntar(r, s, contador);

            melhor = Math.min(melhor, contador.comparacoes());
            brutos.add(new long[] {contador.comparacoes(), contador.maiorGuardado()});
        }

        int i = 0;

        for (var metodo : metodos.entrySet()) {
            long[] bruto = brutos.get(i++);

            linhas.add(new LinhaOrdenada(metodo.getKey(), bruto[0], bruto[1],
                    (double) bruto[0] / melhor));
        }

        return linhas;
    }

    // ───────────────────── 6. quando o resultado e grande ─────────────────────

    public record LinhaResultado(int distintas, int pares, String metodo, long comparacoes,
                                 double porPar) {
    }

    /**
     * QUANDO O RESULTADO E GRANDE, a escolha do metodo some.
     *
     * <p>Eu escrevi aqui que a diferenca entre os metodos sumiria, porque escrever um par e
     * trabalho que nenhum deles evita. Nao sumiu: ela INVERTEU.
     *
     * <p>Com 20 chaves distintas e 800.316 pares de saida, o ordena-e-mescla faz 0,12
     * comparacoes POR PAR ESCRITO. Menos de uma. Ele compara so as bordas da faixa de
     * iguais e depois escreve o produto inteiro sem comparar mais nada, e ai o custo dele
     * para de crescer: 92.770 comparacoes contra 800.316 do espalhamento.
     *
     * <p>O espalhamento nao tem como fazer isso. Ele precisa sondar uma vez por linha de um
     * dos lados, e cada sondagem percorre o balde daquela chave.
     *
     * <p>Ou seja: "o espalhamento e linear e o ordena-e-mescla e n log n" e uma frase sobre
     * o custo de ACHAR os pares. Quando ha muitos pares, quem manda e o custo de ENTREGAR,
     * e ai a ordem dos dois se inverte.
     */
    public static List<LinhaResultado> oResultado() {
        List<LinhaResultado> linhas = new ArrayList<>();
        int n = LINHAS;

        for (int distintas : new int[] {n, n / 20, n / 100, n / 200}) {
            Tabela r = Tabelas.repetidas("R", n, distintas, SEMENTE);
            Tabela s = Tabelas.repetidas("S", n, distintas, SEMENTE + 1);

            for (var metodo : Juiz.osMetodos().entrySet()) {
                if (metodo.getKey().equals("espalhamento cru")) continue;
                if (metodo.getKey().equals("em blocos")) continue;

                Contador contador = new Contador();
                Resultado resultado = metodo.getValue().get().juntar(r, s, contador);

                linhas.add(new LinhaResultado(distintas, resultado.quantos(), metodo.getKey(),
                        contador.comparacoes(),
                        (double) contador.comparacoes() / Math.max(1, resultado.quantos())));
            }
        }

        return linhas;
    }
}
