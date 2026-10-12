package br.com.conde.juncao;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AS TABELAS DE ENTRADA. Elas nao sao decoracao: a entrada decide qual metodo ganha.
 *
 * <p>Os tres metodos respondem a mesma coisa sempre, entao a unica pergunta interessante e
 * em que entrada cada um quebra. Cada gerador daqui existe para quebrar um deles.
 */
public final class Tabelas {

    private Tabelas() {
    }

    /**
     * O SORTEIO DA CASA: um gerador congruente linear escrito aqui, com constantes fixas.
     *
     * <p>Nao e {@link java.util.Random} de proposito. Uma medida que depende de uma
     * biblioteca nao e reproduzivel: a implementacao pode mudar entre versoes da maquina
     * virtual e as tabelas do README deixariam de bater sem que nada aqui tivesse mudado.
     */
    public static final class Sorteio {

        private long estado;

        public Sorteio(long semente) {
            this.estado = semente;
        }

        /**
         * O proximo inteiro.
         *
         * <p>O DESLOCAMENTO DE 32 CARREGA O METODO. Os bits BAIXOS de um gerador congruente
         * linear de modulo potencia de dois sao pessimos: o ultimo bit alterna zero e um
         * para sempre, o penultimo tem periodo quatro.
         *
         * <p>Num repositorio sobre juncao por espalhamento isso seria especialmente grave,
         * porque o balde e justamente o resto por uma potencia de dois: as chaves sorteadas
         * cairiam num padrao de baldes e a tabela do espalhamento mediria o gerador em vez
         * de medir a funcao de espalhamento. Ha um teste so para isto.
         */
        public int proximo() {
            estado = estado * 6364136223846793005L + 1442695040888963407L;
            return (int) (estado >>> 32);
        }

        public int ate(int limite) {
            return Math.floorMod(proximo(), limite);
        }

        public double fracao() {
            return (proximo() & 0x7FFFFFFF) / (double) 0x80000000L;
        }
    }

    /** Chaves distintas, embaralhadas. Uma juncao de um para um. */
    public static Tabela unicas(String nome, int quantas, long semente) {
        Sorteio sorteio = new Sorteio(semente);
        int[] chaves = new int[quantas];

        for (int i = 0; i < quantas; i++) chaves[i] = i;

        for (int i = quantas - 1; i > 0; i--) {
            int j = sorteio.ate(i + 1);
            int guarda = chaves[i];

            chaves[i] = chaves[j];
            chaves[j] = guarda;
        }

        return Tabela.de(nome, chaves);
    }

    /** Chaves sorteadas por igual num universo. Repete pouco, se o universo e grande. */
    public static Tabela sorteadas(String nome, int quantas, int universo, long semente) {
        Sorteio sorteio = new Sorteio(semente);
        int[] chaves = new int[quantas];

        for (int i = 0; i < quantas; i++) chaves[i] = sorteio.ate(universo);

        return Tabela.de(nome, chaves);
    }

    /**
     * Poucas chaves distintas, cada uma repetida muitas vezes.
     *
     * <p>E o caso ruim do espalhamento que NAO tem conserto: as linhas de uma chave tem que
     * ficar no mesmo balde porque todas casam entre si.
     */
    public static Tabela repetidas(String nome, int quantas, int distintas, long semente) {
        Sorteio sorteio = new Sorteio(semente);
        int[] chaves = new int[quantas];

        for (int i = 0; i < quantas; i++) chaves[i] = sorteio.ate(distintas);

        return Tabela.de(nome, chaves);
    }

    /**
     * Chaves com estrutura nos bits baixos: todas multiplas de um passo.
     *
     * <p>E o caso ruim do espalhamento que TEM conserto. Elas sao todas diferentes, entao o
     * problema nao e repeticao: o indice do balde e o resto por uma potencia de dois, e com
     * um passo potencia de dois todos os restos sao iguais.
     *
     * <p>E chave assim nao e exotica: identificador alinhado, numero com sufixo fixo, codigo
     * que carrega um campo nos bits de baixo. Quase toda chave que alguem gerou tem alguma
     * estrutura.
     */
    public static Tabela comEstrutura(String nome, int quantas, int passo, long semente) {
        Sorteio sorteio = new Sorteio(semente);
        int[] chaves = new int[quantas];

        for (int i = 0; i < quantas; i++) chaves[i] = (sorteio.ate(quantas * 4)) * passo;

        return Tabela.de(nome, chaves);
    }

    /**
     * Chaves por lei de potencia: a primeira aparece o dobro da segunda, o triplo da
     * terceira, e assim por diante.
     *
     * <p>E como chave de verdade se distribui. Numa tabela de pedidos por cliente, o maior
     * cliente tem mais pedidos que os mil menores somados.
     */
    public static Tabela emLei(String nome, int quantas, int distintas, long semente) {
        Sorteio sorteio = new Sorteio(semente);
        double[] acumulado = new double[distintas];
        double soma = 0;

        for (int i = 0; i < distintas; i++) {
            soma += 1.0 / (i + 1);
            acumulado[i] = soma;
        }

        int[] chaves = new int[quantas];

        for (int i = 0; i < quantas; i++) {
            double sorteado = sorteio.fracao() * soma;
            int posicao = Arrays.binarySearch(acumulado, sorteado);

            chaves[i] = posicao >= 0 ? posicao : -(posicao + 1);

            if (chaves[i] >= distintas) chaves[i] = distintas - 1;
        }

        return Tabela.de(nome, chaves);
    }

    /** A mesma tabela, com as linhas em ordem de chave. */
    public static Tabela ordenada(Tabela tabela) {
        return tabela.reordenada(OrdenaEMescla.ordenarPorChave(tabela, new Contador()));
    }

    /** Os pares de tabelas que as medidas percorrem, cada um com o nome que a tabela imprime. */
    public static Map<String, Tabela[]> todas(int tamanho, long semente) {
        Map<String, Tabela[]> pares = new LinkedHashMap<>();

        pares.put("um para um", new Tabela[] {
                unicas("R", tamanho, semente), unicas("S", tamanho, semente + 1)});

        pares.put("sorteadas", new Tabela[] {
                sorteadas("R", tamanho, tamanho, semente + 2),
                sorteadas("S", tamanho, tamanho, semente + 3)});

        pares.put("repetidas", new Tabela[] {
                repetidas("R", tamanho, Math.max(1, tamanho / 100), semente + 4),
                repetidas("S", tamanho, Math.max(1, tamanho / 100), semente + 5)});

        pares.put("com estrutura", new Tabela[] {
                comEstrutura("R", tamanho, 1_024, semente + 6),
                comEstrutura("S", tamanho, 1_024, semente + 7)});

        pares.put("por lei", new Tabela[] {
                emLei("R", tamanho, Math.max(2, tamanho / 10), semente + 8),
                emLei("S", tamanho, Math.max(2, tamanho / 10), semente + 9)});

        return pares;
    }
}
