package br.com.conde.juncao;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * O JUIZ: o laco aninhado.
 *
 * <p>Ele e o juiz certo e nao so o conveniente. Nao ha nele tabela a montar, ordem a manter,
 * balde a escolher nem faixa de iguais a percorrer: ele faz todas as perguntas possiveis e
 * escreve as que deram certo. Um juiz que fosse um segundo metodo esperto poderia errar
 * junto, e justamente nas entradas que quebram os espertos.
 *
 * <h2>O que ele compara</h2>
 *
 * <p>O MULTICONJUNTO de pares, e nao a lista. Dois metodos corretos produzem os mesmos pares
 * em ordens diferentes: o espalhamento sai na ordem dos baldes, o ordena-e-mescla sai na
 * ordem das chaves. Comparar a ordem reprovaria os dois por um motivo que nao tem nada a ver
 * com juncao.
 */
public final class Juiz {

    private Juiz() {
    }

    public record Veredito(int paresCertos, int paresDele, int divergencias, String falha) {

        public boolean passou() {
            return divergencias == 0;
        }
    }

    public static Veredito julgar(Juncao juncao, Tabela r, Tabela s) {
        long[] certo = new LacoAninhado().juntar(r, s, new Contador()).ordenado();
        long[] dele = juncao.juntar(r, s, new Contador()).ordenado();

        if (Arrays.equals(certo, dele)) return new Veredito(certo.length, dele.length, 0, null);

        String falha = dele.length != certo.length
                ? "escreveu " + dele.length + " pares, sao " + certo.length
                : "mesmo numero de pares, pares diferentes";

        return new Veredito(certo.length, dele.length, 1, falha);
    }

    /**
     * O mesmo, com as duas tabelas ordenadas antes.
     *
     * <p>O metodo que confia na ordem precisa de uma entrada ordenada, e precisa ser julgado
     * com ela. Julga-lo com entrada fora de ordem mediria um erro que ele nao comete: ele
     * avisa que confia.
     */
    public static Veredito julgarOrdenado(Juncao juncao, Tabela r, Tabela s) {
        return julgar(juncao, Tabelas.ordenada(r), Tabelas.ordenada(s));
    }

    /** Os metodos que entram nas tabelas, cada um com o nome que a tabela imprime. */
    public static Map<String, Supplier<Juncao>> osMetodos() {
        Map<String, Supplier<Juncao>> metodos = new LinkedHashMap<>();

        metodos.put("laco aninhado", LacoAninhado::new);
        metodos.put("em blocos", () -> new LacoAninhado(64));
        metodos.put("espalhamento bom", () -> new PorEspalhamento(Espalhamento.BOA));
        metodos.put("espalhamento cru", () -> new PorEspalhamento(Espalhamento.CRUA));
        metodos.put("ordena e mescla", OrdenaEMescla::new);

        return metodos;
    }
}
