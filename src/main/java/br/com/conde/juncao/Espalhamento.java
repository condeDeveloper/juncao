package br.com.conde.juncao;

/**
 * COMO ESPALHAR UMA CHAVE em baldes.
 *
 * <p>As duas versoes daqui existem porque a diferenca entre elas e invisivel ate o momento
 * em que destroi o metodo, e e um dos poucos defeitos de desempenho que um teste de
 * correcao jamais pega: a juncao por espalhamento responde CERTO com qualquer funcao, ate
 * com uma que mande tudo para o mesmo balde. Ela so fica quadratica.
 */
public enum Espalhamento {

    /**
     * A chave mesma, sem mexer.
     *
     * <p>E o que parece inofensivo e nao e. O indice do balde e o resto por uma potencia de
     * dois, ou seja, sao os BITS BAIXOS da chave. Qualquer chave com estrutura nos bits
     * baixos vira um amontoado: identificadores que pulam de 64 em 64, de 1000 em 1000, ou
     * que trazem um sufixo fixo. E chave de tabela de verdade quase sempre tem estrutura,
     * porque ela foi gerada por alguma coisa.
     *
     * <h2>E ela nao e simplesmente pior</h2>
     *
     * <p>Com chaves sorteadas num universo pequeno, ela e DUAS VEZES MELHOR que a boa: 1,0
     * comparacao por sondagem contra 2,0. Inteiros proximos entram em baldes consecutivos,
     * um por balde, sem colisao nenhuma, enquanto a funcao boa espalha de verdade e aceita
     * as colisoes que o acaso traz.
     *
     * <p>Entao a escolha nao e entre ruim e boa: e entre uma que depende da chave e uma que
     * nao depende. No dia bom a crua ganha por 2; no dia ruim ela perde por 829. Essa e a
     * forma dessa decisao, e nenhuma media entre os dois numeros significa coisa alguma.
     */
    CRUA {
        @Override
        public int de(int chave) {
            return chave;
        }
    },

    /**
     * O finalizador do SplitMix64, cortado para 32 bits.
     *
     * <p>Ele mistura os bits altos nos baixos com deslocamentos e multiplicacoes. Depois
     * dele, dois valores que diferem num bit so caem em baldes sem relacao nenhuma.
     */
    BOA {
        @Override
        public int de(int chave) {
            long x = chave * 0x9E3779B97F4A7C15L;

            x ^= x >>> 30;
            x *= 0xBF58476D1CE4E5B9L;
            x ^= x >>> 27;
            x *= 0x94D049BB133111EBL;
            x ^= x >>> 31;

            return (int) x;
        }
    };

    public abstract int de(int chave);

    public int balde(int chave, int quantosBaldes) {
        return de(chave) & (quantosBaldes - 1);
    }
}
