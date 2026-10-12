package br.com.conde.juncao;

/**
 * O CONTADOR: comparacoes de chave, linhas lidas e linhas guardadas. Nunca segundos.
 *
 * <p>A COMPARACAO DE CHAVE e a medida certa e nao so a conveniente. Toda juncao, de qualquer
 * jeito que seja escrita, decide se duas linhas casam comparando as chaves delas. O que
 * separa um metodo do outro e so quantas dessas comparacoes ele consegue nao fazer.
 *
 * <p>E ela e contada em TODA fase, inclusive na ordenacao e na montagem da tabela de
 * espalhamento. Contar so a fase de casamento seria o jeito mais facil de fazer o
 * espalhamento parecer gratuito: ele cobra na montagem.
 *
 * <p>A LINHA GUARDADA e a segunda medida, e ela existe porque a primeira nao conta a
 * historia toda. O laco aninhado nao guarda nada, o espalhamento guarda um lado inteiro e o
 * ordena-e-mescla guarda os dois para ordenar. Numa juncao de verdade e a memoria que decide
 * se o metodo cabe na maquina.
 *
 * <p>Segundos nao serviriam: a integracao continua roda em tres sistemas, e o laco aninhado
 * percorre vetores contiguos que o processador adora enquanto o espalhamento pula pela
 * memoria. Uma medida em segundos misturaria "quantas comparacoes preciso fazer" com "quao
 * rapido e fazer uma", e so a primeira e sobre o algoritmo.
 */
public final class Contador {

    private long comparacoes;
    private long linhasLidas;
    private long linhasEscritas;
    private long linhasGuardadas;
    private long maiorGuardado;
    private long sondagens;

    public void compara() {
        comparacoes++;
    }

    public void compara(long quantas) {
        comparacoes += quantas;
    }

    public void le() {
        linhasLidas++;
    }

    public void le(long quantas) {
        linhasLidas += quantas;
    }

    public void escreve() {
        linhasEscritas++;
    }

    /** Uma linha entrou na memoria de trabalho do metodo. */
    public void guarda(long quantas) {
        linhasGuardadas += quantas;
        maiorGuardado = Math.max(maiorGuardado, linhasGuardadas);
    }

    /** A memoria de trabalho foi liberada, por exemplo no fim de um bloco. */
    public void solta(long quantas) {
        linhasGuardadas -= quantas;
    }

    /** Uma busca na tabela de espalhamento, que pode varrer um balde inteiro. */
    public void sonda() {
        sondagens++;
    }

    public long comparacoes() {
        return comparacoes;
    }

    public long linhasLidas() {
        return linhasLidas;
    }

    public long linhasEscritas() {
        return linhasEscritas;
    }

    public long maiorGuardado() {
        return maiorGuardado;
    }

    public long sondagens() {
        return sondagens;
    }

    public double comparacoesPorLinhaEscrita() {
        return linhasEscritas == 0 ? 0 : (double) comparacoes / linhasEscritas;
    }

    public double comparacoesPorSondagem() {
        return sondagens == 0 ? 0 : (double) comparacoes / sondagens;
    }

    public void zerar() {
        comparacoes = 0;
        linhasLidas = 0;
        linhasEscritas = 0;
        linhasGuardadas = 0;
        maiorGuardado = 0;
        sondagens = 0;
    }
}
