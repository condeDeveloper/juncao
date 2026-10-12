package br.com.conde.juncao;

/**
 * O LACO ANINHADO: para cada linha de R, percorre S inteira.
 *
 * <p>Ele e o juiz deste repositorio e tambem um dos metodos medidos, e as duas coisas pelo
 * mesmo motivo: nao ha o que errar nele. Nao ha tabela a montar, ordem a manter, balde a
 * escolher. Ele pergunta todas as perguntas possiveis e escreve as que deram certo.
 *
 * <p>O custo e |R| vezes |S| comparacoes, sempre, aconteca o que acontecer com os dados. E
 * essa teimosia e uma virtude escondida: ele e o unico metodo daqui que nao tem caso ruim,
 * porque ja esta sempre no pior.
 *
 * <h2>Em blocos</h2>
 *
 * <p>A versao em blocos le um pedaco de R para a memoria e percorre S uma vez por pedaco.
 * Ela faz EXATAMENTE as mesmas comparacoes, nem uma a menos. O que ela economiza e LEITURA,
 * e por isso as duas colunas existem separadas: um metodo que nao muda uma coluna pode mudar
 * a outra por um fator de mil.
 */
public final class LacoAninhado implements Juncao {

    private final int tamanhoDoBloco;

    /** Sem blocos: o laco aninhado puro, que le S inteira uma vez por linha de R. */
    public LacoAninhado() {
        this(1);
    }

    public LacoAninhado(int tamanhoDoBloco) {
        if (tamanhoDoBloco < 1) throw new IllegalArgumentException("bloco de pelo menos uma linha");

        this.tamanhoDoBloco = tamanhoDoBloco;
    }

    @Override
    public String nome() {
        return tamanhoDoBloco == 1 ? "laco aninhado" : "em blocos de " + tamanhoDoBloco;
    }

    @Override
    public Resultado juntar(Tabela r, Tabela s, Contador contador) {
        Resultado resultado = new Resultado();

        for (int inicio = 0; inicio < r.tamanho(); inicio += tamanhoDoBloco) {
            int fim = Math.min(inicio + tamanhoDoBloco, r.tamanho());

            contador.le(fim - inicio);
            contador.guarda(fim - inicio);

            for (int j = 0; j < s.tamanho(); j++) {
                contador.le();

                for (int i = inicio; i < fim; i++) {
                    contador.compara();

                    if (r.chave(i) == s.chave(j)) resultado.escrever(r.carga(i), s.carga(j));
                }
            }

            contador.solta(fim - inicio);
        }

        for (int i = 0; i < resultado.quantos(); i++) contador.escreve();

        return resultado;
    }
}
