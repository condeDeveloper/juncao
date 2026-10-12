package br.com.conde.juncao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JuncaoTest {

    // ───────────────────── o juiz contra todos os metodos ─────────────────────

    @Test
    @DisplayName("todo metodo bate com o laco aninhado, em toda entrada e todo tamanho")
    void todosBatemComOJuiz() {
        List<String> falhas = new ArrayList<>();
        int conferidas = 0;

        for (int n : new int[] {1, 2, 7, 60, 400})
            for (var entrada : Tabelas.todas(n, 31).entrySet()) {
                Tabela r = entrada.getValue()[0];
                Tabela s = entrada.getValue()[1];

                for (var metodo : Juiz.osMetodos().entrySet()) {
                    Juiz.Veredito veredito = Juiz.julgar(metodo.getValue().get(), r, s);

                    conferidas++;

                    if (!veredito.passou())
                        falhas.add(metodo.getKey() + " em " + entrada.getKey() + " com " + n
                                + " linhas: " + veredito.falha());
                }

                Juiz.Veredito daOrdenada = Juiz.julgarOrdenado(new OrdenaEMescla(true), r, s);

                conferidas++;

                if (!daOrdenada.passou())
                    falhas.add("mescla ja ordenada em " + entrada.getKey() + ": "
                            + daOrdenada.falha());
            }

        assertThat(conferidas).isGreaterThan(100);
        assertThat(falhas).isEmpty();
    }

    /**
     * TABELAS DE TAMANHOS BEM DIFERENTES SAO UM CASO SEPARADO.
     *
     * <p>O espalhamento monta o lado MENOR, e e a unica decisao dele que depende de qual
     * tabela e qual. Uma troca de lado esquecida na hora de escrever o par daria pares
     * invertidos, que e um erro que so aparece quando os dois lados tem tamanhos diferentes:
     * com tamanhos iguais a escolha e arbitraria e o erro se esconde.
     */
    @Test
    @DisplayName("juntar uma tabela grande com uma pequena nao inverte o par")
    void tamanhosBemDiferentes() {
        for (int[] tamanhos : new int[][] {{1, 500}, {500, 1}, {10, 2_000}, {2_000, 10}}) {
            Tabela r = Tabelas.repetidas("R", tamanhos[0], 30, 5);
            Tabela s = Tabelas.repetidas("S", tamanhos[1], 30, 6);

            for (var metodo : Juiz.osMetodos().entrySet()) {
                Juiz.Veredito veredito = Juiz.julgar(metodo.getValue().get(), r, s);

                assertThat(veredito.divergencias())
                        .as(metodo.getKey() + " com " + tamanhos[0] + " por " + tamanhos[1]
                                + ": " + veredito.falha())
                        .isZero();
            }
        }
    }

    /**
     * A FAIXA DE IGUAIS DOS DOIS LADOS: o erro classico do ordena-e-mescla.
     *
     * <p>Com a chave 7 aparecendo tres vezes em R e duas em S, o resultado tem SEIS pares.
     * Um laco que avanca os dois indices juntos acharia dois, e passaria em qualquer teste
     * com chaves unicas.
     */
    @Test
    @DisplayName("tres de um lado e duas do outro dao seis pares, e nao dois")
    void aFaixaDeIguaisDaOProduto() {
        Tabela r = Tabela.de("R", new int[] {1, 7, 7, 7, 9});
        Tabela s = Tabela.de("S", new int[] {7, 7, 9});

        for (var metodo : Juiz.osMetodos().entrySet()) {
            Resultado resultado = metodo.getValue().get().juntar(r, s, new Contador());

            assertThat(resultado.quantos()).as(metodo.getKey()).isEqualTo(7);
        }

        Resultado mescla = new OrdenaEMescla(true).juntar(r, s, new Contador());

        assertThat(mescla.quantos()).isEqualTo(7);
    }

    @Test
    @DisplayName("tabela vazia de qualquer lado da resultado vazio")
    void tabelaVazia() {
        Tabela vazia = Tabela.de("vazia", new int[0]);
        Tabela cheia = Tabelas.unicas("cheia", 100, 3);

        for (var metodo : Juiz.osMetodos().entrySet()) {
            assertThat(metodo.getValue().get().juntar(vazia, cheia, new Contador()).quantos())
                    .as(metodo.getKey()).isZero();

            assertThat(metodo.getValue().get().juntar(cheia, vazia, new Contador()).quantos())
                    .as(metodo.getKey()).isZero();

            assertThat(metodo.getValue().get().juntar(vazia, vazia, new Contador()).quantos())
                    .as(metodo.getKey()).isZero();
        }
    }

    @Test
    @DisplayName("sem chave em comum, nenhum par sai")
    void semChaveEmComum() {
        Tabela r = Tabela.de("R", new int[] {1, 2, 3});
        Tabela s = Tabela.de("S", new int[] {4, 5, 6});

        for (var metodo : Juiz.osMetodos().entrySet())
            assertThat(metodo.getValue().get().juntar(r, s, new Contador()).quantos())
                    .as(metodo.getKey()).isZero();
    }

    @Test
    @DisplayName("chaves negativas e extremas funcionam em todos os metodos")
    void chavesExtremas() {
        int[] extremas = {0, -1, 1, Integer.MIN_VALUE, Integer.MAX_VALUE, -1_000, 1_000};

        Tabela r = Tabela.de("R", extremas);
        Tabela s = Tabela.de("S", extremas);

        for (var metodo : Juiz.osMetodos().entrySet()) {
            Juiz.Veredito veredito = Juiz.julgar(metodo.getValue().get(), r, s);

            assertThat(veredito.divergencias()).as(metodo.getKey()).isZero();
            assertThat(veredito.paresCertos()).isEqualTo(extremas.length);
        }
    }

    // ───────────────────── o que cada metodo promete sobre si ─────────────────────

    @Test
    @DisplayName("o laco aninhado faz exatamente o produto dos tamanhos, sempre")
    void oLacoNaoTemCasoBom() {
        for (var entrada : Tabelas.todas(300, 8).values()) {
            Contador contador = new Contador();

            new LacoAninhado().juntar(entrada[0], entrada[1], contador);

            assertThat(contador.comparacoes())
                    .as("ele ja esta sempre no pior caso, entao nao tem caso ruim")
                    .isEqualTo(300L * 300);
        }
    }

    /**
     * Os blocos economizam LEITURA e nao economizam COMPARACAO, e e por isso que o contador
     * tem as duas colunas.
     */
    @Test
    @DisplayName("os blocos fazem as mesmas comparacoes e leem sessenta vezes menos")
    void osBlocosEconomizamLeituraENaoComparacao() {
        Tabela r = Tabelas.sorteadas("R", 2_000, 2_000, 1);
        Tabela s = Tabelas.sorteadas("S", 2_000, 2_000, 2);

        Contador semBloco = new Contador();
        Contador comBloco = new Contador();

        new LacoAninhado().juntar(r, s, semBloco);
        new LacoAninhado(64).juntar(r, s, comBloco);

        assertThat(comBloco.comparacoes()).isEqualTo(semBloco.comparacoes());

        assertThat(semBloco.linhasLidas() / (double) comBloco.linhasLidas())
                .as("sessenta vezes menos leitura, pelas mesmas comparacoes")
                .isGreaterThan(50.0);

        assertThat(comBloco.maiorGuardado()).isEqualTo(64);
        assertThat(semBloco.maiorGuardado()).isEqualTo(1);
    }

    @Test
    @DisplayName("a mescla que confia na ordem nao guarda nada")
    void aMesclaOrdenadaNaoGuardaNada() {
        Tabela r = Tabelas.ordenada(Tabelas.sorteadas("R", 5_000, 5_000, 1));
        Tabela s = Tabelas.ordenada(Tabelas.sorteadas("S", 5_000, 5_000, 2));

        Contador confiando = new Contador();
        Contador ordenando = new Contador();

        new OrdenaEMescla(true).juntar(r, s, confiando);
        new OrdenaEMescla().juntar(r, s, ordenando);

        assertThat(confiando.maiorGuardado()).isZero();
        assertThat(ordenando.maiorGuardado()).isEqualTo(10_000);

        assertThat(ordenando.comparacoes())
                .as("ordenar o que ja estava ordenado nao e de graca")
                .isGreaterThan(confiando.comparacoes() * 5);
    }

    /**
     * O ESPALHAMENTO RESPONDE CERTO COM QUALQUER FUNCAO, inclusive com uma que mande tudo
     * para o mesmo balde. Essa e a razao de o caso ruim dele ser invisivel num teste de
     * correcao, e a razao de ele precisar de uma medida e nao de uma asserao.
     */
    @Test
    @DisplayName("o espalhamento cru responde certo e custa oitocentas vezes mais")
    void oEspalhamentoCruRespondeCerto() {
        Tabela r = Tabelas.comEstrutura("R", 2_000, 1_024, 1);
        Tabela s = Tabelas.comEstrutura("S", 2_000, 1_024, 2);

        assertThat(Juiz.julgar(new PorEspalhamento(Espalhamento.CRUA), r, s).divergencias())
                .as("certo, so que lento")
                .isZero();

        Contador cru = new Contador();
        Contador bom = new Contador();

        new PorEspalhamento(Espalhamento.CRUA).juntar(r, s, cru);
        new PorEspalhamento(Espalhamento.BOA).juntar(r, s, bom);

        assertThat(cru.comparacoes() / (double) bom.comparacoes()).isGreaterThan(300.0);
    }

    /**
     * E A FUNCAO CRUA NAO E SIMPLESMENTE PIOR.
     *
     * <p>Com chaves sorteadas num universo pequeno, ela ganha da boa: inteiros proximos
     * entram em baldes consecutivos, um por balde, enquanto a funcao boa espalha de verdade
     * e aceita as colisoes do acaso.
     */
    @Test
    @DisplayName("com chave bem comportada, a funcao crua e duas vezes melhor que a boa")
    void aFuncaoCruaGanhaNoDiaBom() {
        Tabela r = Tabelas.sorteadas("R", 4_000, 4_000, Medidas.SEMENTE);
        Tabela s = Tabelas.sorteadas("S", 4_000, 4_000, Medidas.SEMENTE + 1);

        Contador cru = new Contador();
        Contador bom = new Contador();

        new PorEspalhamento(Espalhamento.CRUA).juntar(r, s, cru);
        new PorEspalhamento(Espalhamento.BOA).juntar(r, s, bom);

        assertThat(cru.comparacoesPorSondagem()).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.05));
        assertThat(bom.comparacoesPorSondagem()).isCloseTo(2.0, org.assertj.core.data.Offset.offset(0.05));

        assertThat(cru.comparacoes()).isLessThan(bom.comparacoes());
    }

    // ───────────────────── as pecas ─────────────────────

    @Test
    @DisplayName("a ordenacao por mescla deixa as chaves em ordem e e estavel")
    void aOrdenacaoEEstavel() {
        Tabela tabela = Tabela.de("t", new int[] {5, 1, 5, 1, 9, 1});
        int[] ordem = OrdenaEMescla.ordenarPorChave(tabela, new Contador());

        assertThat(ordem).containsExactly(1, 3, 5, 0, 2, 4);

        Tabela grande = Tabelas.repetidas("g", 5_000, 50, 4);
        int[] daGrande = OrdenaEMescla.ordenarPorChave(grande, new Contador());

        for (int i = 1; i < daGrande.length; i++) {
            assertThat(grande.chave(daGrande[i])).isGreaterThanOrEqualTo(grande.chave(daGrande[i - 1]));

            if (grande.chave(daGrande[i]) == grande.chave(daGrande[i - 1]))
                assertThat(daGrande[i]).as("estavel: empate mantem a ordem original")
                        .isGreaterThan(daGrande[i - 1]);
        }
    }

    @Test
    @DisplayName("o resultado guarda o par num long so e sai em ordem")
    void oResultadoCabeNumLong() {
        Resultado resultado = new Resultado();

        resultado.escrever(5, 9);
        resultado.escrever(1, 2);
        resultado.escrever(5, 3);

        assertThat(resultado.quantos()).isEqualTo(3);
        assertThat(resultado.deR(0)).isEqualTo(5);
        assertThat(resultado.deS(0)).isEqualTo(9);

        long[] ordenado = resultado.ordenado();

        assertThat((int) (ordenado[0] >>> 32)).isEqualTo(1);
        assertThat((int) (ordenado[2] >>> 32)).isEqualTo(5);
        assertThat((int) ordenado[1]).isEqualTo(3);
    }

    @Test
    @DisplayName("tabela com chaves e cargas de tamanhos diferentes e recusada")
    void tabelaMalFormada() {
        assertThatThrownBy(() -> new Tabela("t", new int[3], new int[2]))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new LacoAninhado(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ───────────────────── o sorteio da casa ─────────────────────

    /**
     * O TESTE DO DESLOCAMENTO DE 32.
     *
     * <p>Sem o {@code >>> 32}, o gerador entregaria os bits baixos. Num repositorio sobre
     * juncao por espalhamento isso seria especialmente grave: o balde e o resto por uma
     * potencia de dois, exatamente os bits baixos. A tabela do espalhamento estaria medindo
     * o gerador e nao a funcao.
     */
    @Test
    @DisplayName("o deslocamento de 32 e o que faz o sorteio sortear")
    void oDeslocamentoDeTrintaEDois() {
        Tabelas.Sorteio sorteio = new Tabelas.Sorteio(1);
        int[] quantos = new int[8];

        for (int i = 0; i < 80_000; i++) quantos[sorteio.ate(8)]++;

        for (int resto = 0; resto < 8; resto++)
            assertThat(quantos[resto]).as("resto " + resto).isBetween(9_600, 10_400);

        long estado = 1;
        int alternancias = 0;
        int anterior = -1;

        for (int i = 0; i < 1_000; i++) {
            estado = estado * 6364136223846793005L + 1442695040888963407L;

            int baixo = (int) (estado & 1);

            if (anterior >= 0 && baixo != anterior) alternancias++;

            anterior = baixo;
        }

        assertThat(alternancias)
                .as("o ultimo bit do estado cru alterna em TODOS os passos")
                .isEqualTo(999);
    }

    @Test
    @DisplayName("as entradas sao o que dizem ser")
    void asEntradasSaoOQueDizem() {
        assertThat(distintas(Tabelas.unicas("u", 1_000, 1))).isEqualTo(1_000);
        assertThat(distintas(Tabelas.repetidas("r", 1_000, 20, 1))).isEqualTo(20);

        Tabela comEstrutura = Tabelas.comEstrutura("e", 1_000, 1_024, 1);

        for (int i = 0; i < comEstrutura.tamanho(); i++)
            assertThat(comEstrutura.chave(i) % 1_024).isZero();

        assertThat(distintas(comEstrutura))
                .as("com estrutura nao quer dizer repetida: elas sao quase todas diferentes")
                .isGreaterThan(800);

        Tabela porLei = Tabelas.emLei("l", 20_000, 1_000, 1);
        int[] contagem = new int[1_000];

        for (int i = 0; i < porLei.tamanho(); i++) contagem[porLei.chave(i)]++;

        assertThat(contagem[0])
                .as("a chave mais comum aparece mais que vinte vezes a media")
                .isGreaterThan(20 * 20_000 / 1_000);
    }

    private static int distintas(Tabela tabela) {
        return (int) java.util.Arrays.stream(tabela.chaves()).distinct().count();
    }
}
