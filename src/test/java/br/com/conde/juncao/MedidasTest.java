package br.com.conde.juncao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Os numeros do README, afirmados.
 *
 * <p>Um README e texto: ele nao quebra quando deixa de ser verdade. Cada afirmacao das
 * tabelas esta repetida aqui para quebrar.
 */
class MedidasTest {

    /* As tabelas sao calculadas uma vez so: cada uma roda o laco aninhado varias vezes. */
    private static final List<Medidas.LinhaTamanho> TAMANHO = Medidas.oTamanho();
    private static final List<Medidas.LinhaRepeticao> REPETICAO = Medidas.aRepeticao();
    private static final List<Medidas.LinhaEspalhamento> ESPALHAMENTO = Medidas.oEspalhamento();
    private static final List<Medidas.LinhaOrdenada> ORDENADA = Medidas.aOrdenada();
    private static final List<Medidas.LinhaResultado> RESULTADO = Medidas.oResultado();

    // ───────────────────── 1. a correcao ─────────────────────

    @Test
    @DisplayName("nenhuma combinacao diverge do laco aninhado")
    void aCorrecaoNaoDiverge() {
        List<Medidas.LinhaCorrecao> linhas = Medidas.aCorrecao();

        assertThat(linhas).hasSize(30);
        assertThat(linhas).allSatisfy(l ->
                assertThat(l.divergencias()).as(l.metodo() + " em " + l.entrada()).isZero());

        assertThat(pares(linhas, "um para um")).isEqualTo(600);
        assertThat(pares(linhas, "sorteadas")).isEqualTo(599);
        assertThat(pares(linhas, "repetidas")).isEqualTo(60_042);
        assertThat(pares(linhas, "com estrutura")).isEqualTo(138);
        assertThat(pares(linhas, "por lei")).isEqualTo(27_245);
    }

    private static int pares(List<Medidas.LinhaCorrecao> linhas, String entrada) {
        List<Integer> todos = linhas.stream()
                .filter(l -> l.entrada().equals(entrada))
                .map(Medidas.LinhaCorrecao::paresCertos)
                .distinct()
                .toList();

        assertThat(todos).as("os seis metodos concordam no numero de pares").hasSize(1);

        return todos.get(0);
    }

    // ───────────────────── 2. o custo por tamanho ─────────────────────

    @Test
    @DisplayName("o laco aninhado e o produto dos tamanhos e o espalhamento e a soma")
    void oCrescimentoDeCadaUm() {
        assertThat(tamanho(8_000, "laco aninhado").comparacoes()).isEqualTo(64_000_000L);
        assertThat(tamanho(8_000, "espalhamento bom").comparacoes()).isEqualTo(15_759L);
        assertThat(tamanho(8_000, "ordena e mescla").comparacoes()).isEqualTo(206_707L);

        for (int n : new int[] {500, 1_000, 2_000, 4_000, 8_000})
            assertThat(tamanho(n, "laco aninhado").comparacoes())
                    .as("o laco e exatamente n ao quadrado, em todo tamanho")
                    .isEqualTo((long) n * n);

        assertThat(tamanho(8_000, "laco aninhado").comparacoes()
                / (double) tamanho(4_000, "laco aninhado").comparacoes())
                .as("dobrar n quadruplica o laco")
                .isCloseTo(4.0, within(0.01));

        assertThat(tamanho(8_000, "espalhamento bom").comparacoes()
                / (double) tamanho(4_000, "espalhamento bom").comparacoes())
                .as("e so dobra o espalhamento")
                .isCloseTo(2.0, within(0.02));
    }

    /**
     * A COLUNA QUE JUSTIFICA O CONTADOR TER DUAS.
     *
     * <p>Os blocos fazem exatamente as mesmas comparacoes do laco aninhado e leem sessenta e
     * tres vezes menos. Um metodo que nao mexe numa coluna pode mexer na outra por um fator
     * de sessenta.
     */
    @Test
    @DisplayName("os blocos leem 63 vezes menos pelas mesmas comparacoes")
    void osBlocosSoMudamALeitura() {
        for (int n : new int[] {500, 1_000, 2_000, 4_000, 8_000})
            assertThat(tamanho(n, "em blocos").comparacoes())
                    .as("tamanho " + n)
                    .isEqualTo(tamanho(n, "laco aninhado").comparacoes());

        assertThat(tamanho(8_000, "laco aninhado").linhasLidas()).isEqualTo(64_008_000L);
        assertThat(tamanho(8_000, "em blocos").linhasLidas()).isEqualTo(1_008_000L);

        assertThat(tamanho(8_000, "laco aninhado").linhasLidas()
                / (double) tamanho(8_000, "em blocos").linhasLidas())
                .isCloseTo(63.5, within(0.5));
    }

    @Test
    @DisplayName("a memoria separa os tres: 1 linha, um lado inteiro, os dois lados")
    void aMemoriaSeparaOsTres() {
        assertThat(tamanho(8_000, "laco aninhado").maiorGuardado()).isEqualTo(1);
        assertThat(tamanho(8_000, "em blocos").maiorGuardado()).isEqualTo(64);
        assertThat(tamanho(8_000, "espalhamento bom").maiorGuardado()).isEqualTo(8_000);
        assertThat(tamanho(8_000, "ordena e mescla").maiorGuardado()).isEqualTo(16_000);
    }

    @Test
    @DisplayName("os quatro metodos escrevem o mesmo numero de pares, em todo tamanho")
    void todosEscrevemOMesmo() {
        for (int n : new int[] {500, 1_000, 2_000, 4_000, 8_000}) {
            List<Integer> pares = TAMANHO.stream()
                    .filter(l -> l.linhas() == n)
                    .map(Medidas.LinhaTamanho::pares)
                    .distinct()
                    .toList();

            assertThat(pares).as("tamanho " + n).hasSize(1);
        }
    }

    private static Medidas.LinhaTamanho tamanho(int linhas, String metodo) {
        return TAMANHO.stream()
                .filter(l -> l.linhas() == linhas && l.metodo().equals(metodo))
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 3. a repeticao ─────────────────────

    /**
     * O CASO RUIM DO ESPALHAMENTO QUE NAO TEM CONSERTO.
     *
     * <p>As comparacoes por sondagem seguem exatamente as linhas por chave: com 200 linhas
     * por chave sao 200,1 comparacoes por sondagem. Nenhuma funcao de espalhamento muda isso,
     * porque as 200 linhas TEM que ficar no mesmo balde.
     */
    @Test
    @DisplayName("as comparacoes por sondagem seguem as linhas por chave, uma a uma")
    void aSondagemSegueARepeticao() {
        assertThat(REPETICAO).allSatisfy(l ->
                assertThat(l.porSondagem())
                        .as(l.distintas() + " chaves distintas")
                        .isCloseTo(l.linhasPorChave(), within(l.linhasPorChave() * 0.15 + 1.5)));

        assertThat(repeticao(4_000).porSondagem()).isCloseTo(2.0, within(0.05));
        assertThat(repeticao(20).porSondagem()).isCloseTo(200.1, within(0.1));
        assertThat(repeticao(10).porSondagem()).isCloseTo(400.3, within(0.1));
    }

    @Test
    @DisplayName("a vantagem sobre o laco aninhado cai de 2025 para 10")
    void aVantagemDesmorona() {
        assertThat(repeticao(4_000).vezesOLaco()).isCloseTo(2025.8, within(0.1));
        assertThat(repeticao(400).vezesOLaco()).isCloseTo(360.7, within(0.1));
        assertThat(repeticao(40).vezesOLaco()).isCloseTo(38.0, within(0.1));
        assertThat(repeticao(10).vezesOLaco()).isCloseTo(10.0, within(0.1));

        for (int i = 1; i < REPETICAO.size(); i++)
            assertThat(REPETICAO.get(i).vezesOLaco())
                    .as("menos chaves distintas, menos vantagem")
                    .isLessThan(REPETICAO.get(i - 1).vezesOLaco());

        assertThat(repeticao(10).comparacoes()).isEqualTo(1_601_354L);
    }

    private static Medidas.LinhaRepeticao repeticao(int distintas) {
        return REPETICAO.stream()
                .filter(l -> l.distintas() == distintas)
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 4. o espalhamento ─────────────────────

    @Test
    @DisplayName("com chaves estruturadas, a funcao crua custa 829 vezes a boa")
    void oEspalhamentoCruDesmorona() {
        assertThat(espalhamento("com estrutura", "cru").comparacoes()).isEqualTo(3_999_687L);
        assertThat(espalhamento("com estrutura", "cru").porSondagem())
                .as("quase mil comparacoes por sondagem: tudo num balde so")
                .isCloseTo(999.9, within(0.1));

        assertThat(espalhamento("com estrutura", "cru").vezesABoa()).isCloseTo(828.8, within(0.1));
        assertThat(espalhamento("com estrutura", "bom").porSondagem()).isLessThan(1.5);
    }

    /**
     * A INVERSAO QUE EU NAO TINHA PREVISTO.
     *
     * <p>Eu escrevi a funcao crua como a versao ruim. Com chaves sorteadas num universo
     * pequeno ela e DUAS VEZES MELHOR que a boa: inteiros proximos entram em baldes
     * consecutivos, um por balde, sem colisao nenhuma.
     */
    @Test
    @DisplayName("com chave bem comportada a funcao crua ganha da boa, por duas vezes")
    void aFuncaoCruaGanhaNoDiaBom() {
        assertThat(espalhamento("sorteadas", "cru").porSondagem()).isCloseTo(1.0, within(0.05));
        assertThat(espalhamento("sorteadas", "bom").porSondagem()).isCloseTo(2.0, within(0.05));

        assertThat(espalhamento("sorteadas", "cru").vezesABoa())
                .as("meia vez a boa, ou seja, o dobro de rapida")
                .isCloseTo(0.5, within(0.02));

        assertThat(espalhamento("sorteadas", "cru").comparacoes())
                .isLessThan(espalhamento("sorteadas", "bom").comparacoes());
    }

    private static Medidas.LinhaEspalhamento espalhamento(String entrada, String funcao) {
        return ESPALHAMENTO.stream()
                .filter(l -> l.entrada().equals(entrada) && l.funcao().equals(funcao))
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 5. a entrada ja ordenada ─────────────────────

    /**
     * A SEGUNDA COISA QUE EU TINHA ESCRITO ERRADA.
     *
     * <p>Eu escrevi que, com entrada ja ordenada, o ordena-e-mescla ganha de todo mundo. Em
     * comparacoes ele PERDE: 480.014 contra 353.324 do espalhamento.
     *
     * <p>O que ele ganha e memoria, e ganha de um jeito que nao e uma fracao: zero contra
     * duzentas mil.
     */
    @Test
    @DisplayName("com entrada ordenada a mescla perde em comparacoes e ganha a memoria inteira")
    void aMesclaOrdenadaPerdeEGanha() {
        assertThat(ordenada("espalhamento bom").comparacoes()).isEqualTo(353_324L);
        assertThat(ordenada("mescla, ja ordenada").comparacoes()).isEqualTo(480_014L);
        assertThat(ordenada("ordena e mescla").comparacoes()).isEqualTo(4_191_886L);

        assertThat(ordenada("mescla, ja ordenada").vezesOMelhor())
                .as("1,4 vezes o espalhamento, e nao menos que ele")
                .isCloseTo(1.4, within(0.05));

        assertThat(ordenada("mescla, ja ordenada").maiorGuardado())
                .as("e nao guarda NADA")
                .isZero();

        assertThat(ordenada("espalhamento bom").maiorGuardado())
                .isEqualTo(Medidas.LINHAS_GRANDES);

        assertThat(ordenada("ordena e mescla").maiorGuardado())
                .isEqualTo(2L * Medidas.LINHAS_GRANDES);
    }

    @Test
    @DisplayName("ordenar o que ja estava ordenado custa nove vezes a mescla sozinha")
    void ordenarOJaOrdenadoNaoEDeGraca() {
        assertThat(ordenada("ordena e mescla").comparacoes()
                / (double) ordenada("mescla, ja ordenada").comparacoes())
                .isCloseTo(8.7, within(0.1));

        assertThat(ordenada("ordena e mescla").vezesOMelhor()).isCloseTo(11.9, within(0.1));
    }

    private static Medidas.LinhaOrdenada ordenada(String metodo) {
        return ORDENADA.stream()
                .filter(l -> l.metodo().equals(metodo))
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 6. quando o resultado e grande ─────────────────────

    /**
     * A TERCEIRA COISA QUE EU TINHA ESCRITO ERRADA, e a mais interessante.
     *
     * <p>Eu escrevi que, com resultado grande, a diferenca entre os metodos sumiria. Ela
     * inverteu: o ordena-e-mescla faz 0,12 comparacoes por par escrito e o espalhamento faz
     * 1,00.
     */
    @Test
    @DisplayName("com resultado grande, o ordena-e-mescla faz menos de uma comparacao por par")
    void comResultadoGrandeAOrdemInverte() {
        assertThat(resultado(20, "ordena e mescla").porPar())
                .as("menos de uma comparacao por par escrito")
                .isCloseTo(0.12, within(0.01));

        assertThat(resultado(20, "espalhamento bom").porPar()).isCloseTo(1.00, within(0.01));
        assertThat(resultado(20, "laco aninhado").porPar()).isCloseTo(19.99, within(0.01));

        assertThat(resultado(20, "ordena e mescla").comparacoes())
                .as("oito vezes menos comparacoes que o espalhamento")
                .isLessThan(resultado(20, "espalhamento bom").comparacoes() / 8);

        assertThat(resultado(20, "pares").pares()).isEqualTo(800_316);
    }

    @Test
    @DisplayName("com chaves unicas a ordem e a do livro, e ela vira conforme o resultado cresce")
    void aOrdemViraNoMeioDaTabela() {
        assertThat(resultado(4_000, "espalhamento bom").comparacoes())
                .as("com chave unica o espalhamento ganha do ordena-e-mescla por doze vezes")
                .isLessThan(resultado(4_000, "ordena e mescla").comparacoes() / 10);

        assertThat(resultado(200, "espalhamento bom").comparacoes())
                .as("com 200 chaves distintas eles ja empatam")
                .isCloseTo(resultado(200, "ordena e mescla").comparacoes(),
                        org.assertj.core.data.Offset.offset(10_000L));

        assertThat(resultado(40, "ordena e mescla").comparacoes())
                .as("e com 40 o ordena-e-mescla ja ganha")
                .isLessThan(resultado(40, "espalhamento bom").comparacoes());
    }

    @Test
    @DisplayName("o laco aninhado faz o mesmo numero de comparacoes em toda a tabela")
    void oLacoNaoSenteNada() {
        for (int distintas : new int[] {4_000, 200, 40, 20})
            assertThat(resultado(distintas, "laco aninhado").comparacoes())
                    .as(distintas + " chaves distintas")
                    .isEqualTo(16_000_000L);
    }

    private static Medidas.LinhaResultado resultado(int distintas, String metodo) {
        return RESULTADO.stream()
                .filter(l -> l.distintas() == distintas
                        && (metodo.equals("pares") || l.metodo().equals(metodo)))
                .findFirst()
                .orElseThrow();
    }
}
