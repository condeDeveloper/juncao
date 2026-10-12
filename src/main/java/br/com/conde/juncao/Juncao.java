package br.com.conde.juncao;

/**
 * JUNTAR DUAS TABELAS pela igualdade de chave.
 *
 * <p>Todos os metodos daqui respondem exatamente a mesma pergunta e devolvem exatamente o
 * mesmo multiconjunto de pares. Nenhum deles aproxima, nenhum deles descarta nada. O que
 * muda e so quantas comparacoes de chave cada um precisa fazer e quanta memoria precisa
 * para isso.
 *
 * <p>Isso e o que faz a comparacao valer: quando duas versoes respondem a mesma coisa, a
 * diferenca de custo entre elas e pura.
 */
public interface Juncao {

    Resultado juntar(Tabela r, Tabela s, Contador contador);

    String nome();
}
