# juncao

Junção de tabelas do zero em Java 21: laço aninhado, laço em blocos, junção por espalhamento
e ordena-e-mescla.

O juiz é o **laço aninhado**, e ele é o juiz certo e não só o conveniente: não há nele tabela
a montar, ordem a manter, balde a escolher nem faixa de iguais a percorrer. Ele faz todas as
perguntas possíveis e escreve as que deram certo.

E o que ele confere é o **multiconjunto** de pares, não a lista. Dois métodos corretos
produzem os mesmos pares em ordens diferentes: o espalhamento sai na ordem dos baldes, o
ordena-e-mescla sai na ordem das chaves.

A unidade é a **comparação de chave**, contada em **toda** fase, inclusive na ordenação e na
montagem da tabela de espalhamento. Contar só a fase de casamento seria o jeito mais fácil de
fazer o espalhamento parecer grátis, porque ele cobra na montagem, e de fazer o
ordena-e-mescla parecer linear, porque ele cobra na ordenação.

## O custo por tamanho, que é a tabela do livro

```
$ mvn exec:java -Dexec.args="tamanho"

    linhas metodo                      comparacoes        pares   maior guardado     linhas lidas
      8000 laco aninhado                  64000000         8011                1         64008000
      8000 em blocos                      64000000         8011               64          1008000
      8000 espalhamento bom                  15759         8011             8000            16000
      8000 ordena e mescla                  206707         8011            16000            18108
```

Os quatro escrevem **os mesmos 8.011 pares**, e é isso que torna a comparação de custo pura.

E repare nas duas últimas colunas, que é por que o contador tem quatro. O laço em blocos faz
**exatamente** as mesmas 64 milhões de comparações do laço simples e lê **63 vezes menos**. A
memória separa os quatro em uma linha, 64 linhas, um lado inteiro e os dois lados.

## Como rodar

```
mvn test
mvn exec:java -Dexec.args="tudo"
```

Os comandos do medidor são `correcao`, `tamanho`, `repeticao`, `espalhamento`, `ordenada`,
`resultado` e `tudo`.

## O que as medidas me corrigiram

**O espalhamento "cru", que eu escrevi como a versão ruim, é duas vezes melhor que o bom.**

```
$ mvn exec:java -Dexec.args="espalhamento"

  entrada          funcao          comparacoes     por sondagem        vezes o bom
  sorteadas        bom                    7898              2.0                1.0
  sorteadas        cru                    3983              1.0                0.5
  com estrutura    bom                    4826              1.2                1.0
  com estrutura    cru                 3999687            999.9              828.8
```

Com chaves sorteadas num universo pequeno, usar a chave crua como índice de balde dá **1,0**
comparação por sondagem, contra 2,0 da função boa. Inteiros próximos entram em baldes
consecutivos, um por balde, sem colisão nenhuma, enquanto uma função que espalha de verdade
aceita as colisões que o acaso traz.

Com chaves múltiplas de 1.024, a mesma função crua faz **999,9** comparações por sondagem e
custa **829 vezes** a boa. As chaves são **todas diferentes**: não é repetição, é a função. O
índice do balde é o resto por uma potência de dois, e os bits baixos dessas chaves são todos
zero.

Então a escolha não é entre ruim e boa: é entre uma que depende da chave e uma que não
depende. No dia bom a crua ganha por 2; no dia ruim ela perde por 829. Nenhuma média entre
esses dois números significa coisa alguma.

**Com a entrada já ordenada, o ordena-e-mescla não ganha de todo mundo. Ele perde.**

```
$ mvn exec:java -Dexec.args="ordenada"

  metodo                        comparacoes     maior guardado   vezes o melhor
  espalhamento bom                   353324             200000              1.0
  ordena e mescla                   4191886             400000             11.9
  mescla, ja ordenada                480014                  0              1.4
```

Eu tinha escrito que, com a entrada chegando pronta, a mescla ganha de todo mundo. Em
comparações ela perde: 480.014 contra 353.324, **1,4 vezes mais**.

O que ela ganha é a memória, e ganha de um jeito que não é uma fração: **zero** contra as
duzentas mil linhas do lado montado. Numa junção que não cabe na memória, 1,4 vezes mais
comparações é o preço de conseguir rodar.

E ordenar o que já estava ordenado não é de graça: 4.191.886 contra 480.014, **8,7 vezes**.

**Quando o resultado é grande, a diferença entre os métodos não some. Ela inverte.**

```
$ mvn exec:java -Dexec.args="resultado"

  chaves distintas          pares metodo                      comparacoes    por par escrito
          4000           3983 laco aninhado                  16000000            4017.07
          4000           3983 espalhamento bom                   7898               1.98
          4000           3983 ordena e mescla                   95448              23.96
           200          80248 espalhamento bom                  86510               1.08
           200          80248 ordena e mescla                   93962               1.17
            20         800316 laco aninhado                  16000000              19.99
            20         800316 espalhamento bom                 800316               1.00
            20         800316 ordena e mescla                   92770               0.12
```

Eu escrevi que, com muitos pares de saída, a escolha do método sumiria, porque escrever um
par é trabalho que nenhum deles evita.

Com 20 chaves distintas e 800.316 pares, o ordena-e-mescla faz **0,12 comparações por par
escrito**. Menos de uma. Ele compara só as **bordas** da faixa de iguais e depois escreve o
produto inteiro sem comparar mais nada, e aí o custo dele para de crescer.

O espalhamento não tem como fazer isso: ele precisa sondar uma vez por linha, e cada sondagem
percorre o balde daquela chave. Ele faz 800.316 comparações, exatamente uma por par.

A ordem vira no meio da tabela. Com chave única o espalhamento ganha por doze vezes; com 200
chaves distintas eles empatam; com 40 o ordena-e-mescla já ganha.

Ou seja: *"o espalhamento é linear e o ordena-e-mescla é n log n"* é uma frase sobre o custo
de **achar** os pares. Quando há muitos pares, quem manda é o custo de **entregar**.

**E o caso ruim do espalhamento que não tem conserto.**

```
$ mvn exec:java -Dexec.args="repeticao"

  chaves distintas   linhas por chave      comparacoes        pares     por sondagem    vezes mais barato
          4000                1.0             7898         3983              2.0               2025.8
           400               10.0            44359        40322             11.1                360.7
            40              100.0           420873       400391            105.2                 38.0
            20              200.0           800316       800316            200.1                 20.0
            10              400.0          1601354      1601354            400.3                 10.0
```

As comparações por sondagem seguem as linhas por chave **uma a uma**: 200 linhas por chave,
200,1 comparações por sondagem. Nenhuma função de espalhamento muda isso, porque as 200
linhas **têm** que ficar no mesmo balde: todas casam.

A vantagem sobre o laço aninhado cai de 2.025 vezes para 10.

## O que não tem aqui

Só junção por igualdade de chave, e só junção interna. Sem junção externa, sem desigualdade,
sem mais de duas tabelas. Nenhuma medida daqui depende disso: as perguntas são sobre quantas
comparações cada método consegue **não** fazer, e essa pergunta já tem resposta com duas
tabelas e um igual.

## Licença

MIT.
