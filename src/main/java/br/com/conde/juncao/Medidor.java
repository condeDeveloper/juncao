package br.com.conde.juncao;

import java.util.Locale;

/**
 * O MEDIDOR: imprime as tabelas que o README mostra e que os testes conferem.
 *
 * <p>Toda tabela daqui tem um teste do lado que afirma os mesmos numeros. Texto nao quebra
 * quando deixa de ser verdade; teste quebra.
 */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";

        if (comando.equals("correcao") || comando.equals("tudo")) correcao();
        if (comando.equals("tamanho") || comando.equals("tudo")) tamanho();
        if (comando.equals("repeticao") || comando.equals("tudo")) repeticao();
        if (comando.equals("espalhamento") || comando.equals("tudo")) espalhamento();
        if (comando.equals("ordenada") || comando.equals("tudo")) ordenada();
        if (comando.equals("resultado") || comando.equals("tudo")) resultado();
    }

    private static void correcao() {
        System.out.println();
        System.out.println("TODO METODO DEVOLVE O MESMO MULTICONJUNTO DE PARES.");
        System.out.println();

        var linhas = Medidas.aCorrecao();
        long erradas = linhas.stream().filter(l -> l.divergencias() > 0).count();

        System.out.printf("  %-22s %-16s %14s %14s%n",
                "metodo", "entrada", "pares", "divergencias");

        for (var l : linhas)
            System.out.printf("  %-22s %-16s %14d %14d%n",
                    l.metodo(), l.entrada(), l.paresCertos(), l.divergencias());

        System.out.println();
        System.out.printf("  %d combinacoes, %d com divergencia.%n", linhas.size(), erradas);
        System.out.println();
    }

    private static void tamanho() {
        System.out.println();
        System.out.println("O CUSTO POR TAMANHO, com chaves sorteadas.");
        System.out.println();
        System.out.printf("  %8s %-22s %16s %12s %16s %16s%n",
                "linhas", "metodo", "comparacoes", "pares", "maior guardado", "linhas lidas");

        for (var l : Medidas.oTamanho())
            System.out.printf(Locale.ROOT, "  %8d %-22s %16d %12d %16d %16d%n",
                    l.linhas(), l.metodo(), l.comparacoes(), l.pares(), l.maiorGuardado(),
                    l.linhasLidas());

        System.out.println();
    }

    private static void repeticao() {
        System.out.println();
        System.out.println("CHAVES REPETIDAS no espalhamento bom, com " + Medidas.LINHAS
                + " linhas de cada lado.");
        System.out.println();
        System.out.printf("  %12s %18s %16s %12s %16s %20s%n",
                "chaves distintas", "linhas por chave", "comparacoes", "pares",
                "por sondagem", "vezes mais barato");

        for (var l : Medidas.aRepeticao())
            System.out.printf(Locale.ROOT, "  %12d %18.1f %16d %12d %16.1f %20.1f%n",
                    l.distintas(), l.linhasPorChave(), l.comparacoes(), l.pares(),
                    l.porSondagem(), l.vezesOLaco());

        System.out.println();
    }

    private static void espalhamento() {
        System.out.println();
        System.out.println("O ESPALHAMENTO CRU CONTRA O BOM, com " + Medidas.LINHAS
                + " linhas de cada lado.");
        System.out.println();
        System.out.printf("  %-16s %-10s %16s %16s %18s%n",
                "entrada", "funcao", "comparacoes", "por sondagem", "vezes o bom");

        for (var l : Medidas.oEspalhamento())
            System.out.printf(Locale.ROOT, "  %-16s %-10s %16d %16.1f %18.1f%n",
                    l.entrada(), l.funcao(), l.comparacoes(), l.porSondagem(), l.vezesABoa());

        System.out.println();
    }

    private static void ordenada() {
        System.out.println();
        System.out.println("QUANDO A ENTRADA JA VEM ORDENADA, com " + Medidas.LINHAS_GRANDES
                + " linhas de cada lado.");
        System.out.println();
        System.out.printf("  %-22s %18s %18s %16s%n",
                "metodo", "comparacoes", "maior guardado", "vezes o melhor");

        for (var l : Medidas.aOrdenada())
            System.out.printf(Locale.ROOT, "  %-22s %18d %18d %16.1f%n",
                    l.metodo(), l.comparacoes(), l.maiorGuardado(), l.vezesOMelhor());

        System.out.println();
    }

    private static void resultado() {
        System.out.println();
        System.out.println("QUANDO O RESULTADO E GRANDE, A ESCOLHA SOME.");
        System.out.println();
        System.out.printf("  %12s %14s %-22s %16s %18s%n",
                "chaves distintas", "pares", "metodo", "comparacoes", "por par escrito");

        for (var l : Medidas.oResultado())
            System.out.printf(Locale.ROOT, "  %12d %14d %-22s %16d %18.2f%n",
                    l.distintas(), l.pares(), l.metodo(), l.comparacoes(), l.porPar());

        System.out.println();
    }
}
