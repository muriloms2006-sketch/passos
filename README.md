# Meus Passos

[![Android CI](https://github.com/muriloms2006-sketch/passos/actions/workflows/android.yml/badge.svg)](https://github.com/muriloms2006-sketch/passos/actions/workflows/android.yml)

Aplicativo Android de **contagem de passos** desenvolvido como trabalho final da
Unidade Curricular **Desenvolvimento para Dispositivos Móveis**.

O app conta os passos do dia usando os sensores do celular, mostra o progresso
em relação a uma meta diária num anel desenhado pelo próprio grupo, obtém a
localização do dispositivo e salva registros (passos + localização + data/hora)
num banco de dados local, que podem ser consultados, abertos no mapa e apagados
numa tela de histórico.

## Funcionalidades

- Contagem dos passos **do dia**: a contagem continua depois de fechar o app,
  recomeça sozinha quando vira o dia e pode ser zerada manualmente
- Sensor de passos do aparelho, com detecção pelo **acelerômetro** quando o
  aparelho não tem esse sensor (como no emulador)
- Anel de progresso animado com o percentual da meta diária (5000 passos), que
  muda de cor ao bater a meta
- Distância (km) e calorias (kcal) estimadas
- Obtenção da localização atual (latitude e longitude) via GPS ou rede
- Salvamento de registros com passos, localização e data/hora
- Tela de histórico: resumo, abrir o local de um registro no mapa, selecionar
  e apagar registros ou apagar tudo

## Requisitos do trabalho

| Requisito | Como foi atendido | Onde está no código |
|---|---|---|
| **View customizada** | `GraficoPassos` estende `View` e desenha com `Canvas` (`drawArc`) um anel de progresso animado (`ValueAnimator`), com atributos próprios no XML (`app:meta`, `app:corProgresso`, `app:espessura`...) | [`GraficoPassos.java`](app/src/main/java/com/app/meuspassos/GraficoPassos.java), [`attrs.xml`](app/src/main/res/values/attrs.xml) |
| **Banco de dados** | SQLite com `SQLiteOpenHelper`: tabela `registros` com inserção, listagem, contagem e exclusão (em lote, com transação) | [`BancoDados.java`](app/src/main/java/com/app/meuspassos/BancoDados.java) |
| **Geolocalização** | `LocationManager` com provedor GPS (ou rede, como alternativa), tratando as permissões em tempo de execução; cada registro pode ser aberto no mapa | [`MainActivity.java`](app/src/main/java/com/app/meuspassos/MainActivity.java) (`obterLocalizacao`), [`HistoricoActivity.java`](app/src/main/java/com/app/meuspassos/HistoricoActivity.java) (`abrirNoMapa`) |
| **Sensores** | `TYPE_STEP_COUNTER` via `SensorManager` / `SensorEventListener`; sem ele, `TYPE_ACCELEROMETER` com detecção de passos própria | [`MainActivity.java`](app/src/main/java/com/app/meuspassos/MainActivity.java) (`iniciarSensor`, `onSensorChanged`), [`DetectorPassos.java`](app/src/main/java/com/app/meuspassos/DetectorPassos.java), [`ContadorDiario.java`](app/src/main/java/com/app/meuspassos/ContadorDiario.java) |
| **Compilação automática** | GitHub Actions compila o projeto, roda os testes e o lint e gera o APK a cada push/PR | [`.github/workflows/android.yml`](.github/workflows/android.yml) |

## Estrutura do projeto

```
app/src/main/
├── AndroidManifest.xml            # Permissões e declaração das telas
├── java/com/app/meuspassos/
│   ├── MainActivity.java          # Tela inicial: sensores, localização e salvar registro
│   ├── HistoricoActivity.java     # Tela de histórico: mapa, seleção e exclusão
│   ├── RegistroAdapter.java       # Adapter da lista do histórico
│   ├── GraficoPassos.java         # View customizada (anel de progresso da meta)
│   ├── ContadorDiario.java        # Converte o total do sensor nos passos do dia
│   ├── DetectorPassos.java        # Detecta passos pelo acelerômetro
│   ├── Estimativas.java           # Distância, calorias e progresso
│   ├── BancoDados.java            # Acesso ao SQLite (tabela "registros")
│   ├── Registro.java              # Modelo de um registro salvo
│   └── Telas.java                 # Ajuste das telas às barras do sistema
└── res/
    ├── layout/                    # activity_main, activity_historico, item_registro
    ├── values/attrs.xml           # Atributos XML da view customizada
    └── values/                    # Textos, cores, estilos e meta diária

app/src/test/                      # Testes unitários (contador, detector, estimativas)
```

### Como os passos do dia são contados

O `TYPE_STEP_COUNTER` devolve o total de passos **desde que o celular ligou**. O
`ContadorDiario` guarda uma *base* (salva em `SharedPreferences`) e calcula
`passos do dia = total - base`. A base é trocada quando vira o dia, quando o
usuário zera o contador e quando o celular reinicia (o total volta a zero), sem
perder o que já tinha sido contado.

Sem esse sensor, o `DetectorPassos` lê o acelerômetro: cada pisada faz a
aceleração passar de um limiar e depois voltar (histerese), com um intervalo
mínimo entre passos. Nesse modo os passos só são contados com o app aberto.

### Banco de dados

Arquivo `passos.db`, tabela `registros`:

| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | INTEGER (PK, autoincremento) | Identificador do registro |
| `passos` | INTEGER | Quantidade de passos no momento do registro |
| `latitude` | REAL | Latitude obtida |
| `longitude` | REAL | Longitude obtida |
| `data` | TEXT | Data e hora (`dd/MM/yyyy HH:mm`) |

### Permissões

| Permissão | Uso |
|---|---|
| `ACTIVITY_RECOGNITION` | Necessária (Android 10+) para ler o sensor de passos |
| `ACCESS_FINE_LOCATION` | Localização precisa (GPS) |
| `ACCESS_COARSE_LOCATION` | Localização aproximada (rede) |

## Como usar

1. Abra o app e permita o acesso à **atividade física** para que os passos sejam contados.
2. Caminhe com o celular: o anel, a distância e as calorias são atualizados automaticamente.
3. Toque em **Obter localização** e permita o acesso à localização.
4. Toque em **Salvar registro** para gravar os passos e a localização atuais.
5. Toque em **Ver histórico** para ver os registros:
   - toque num registro para abrir o local no mapa;
   - segure um registro para entrar no modo de seleção e apagar os escolhidos;
   - use **Apagar tudo** para limpar o histórico.

> **Observação:** a maioria dos emuladores não tem o sensor de passos
> (`TYPE_STEP_COUNTER`), então o app usa o acelerômetro. Para simular passos no
> emulador, mexa o aparelho em *Extended controls → Virtual sensors*. Para a
> contagem mais precisa, use um celular físico.

## Download do APK

- **Versões publicadas:** baixe o `meuspassos.apk` na página de
  [Releases](https://github.com/muriloms2006-sketch/passos/releases).
- **Último build:** abra a aba [Actions](https://github.com/muriloms2006-sketch/passos/actions),
  escolha a execução mais recente do workflow *Android CI* e baixe o artifact
  `meuspassos-apk` (é necessário estar logado no GitHub).

Para instalar, copie o APK para o celular e permita a instalação de apps de
fontes desconhecidas.

## Integração contínua (GitHub Actions)

O workflow [`android.yml`](.github/workflows/android.yml) roda automaticamente:

| Evento | O que acontece |
|---|---|
| Push ou Pull Request para `master` | Compila o app, roda os testes unitários e o lint (que barra erros de layout e código) e publica o APK como artifact |
| Push de uma tag `v*` (ex.: `v1.0.0`) | Faz o mesmo e cria uma **Release** com o APK anexado |
| Manual (*Run workflow*) | Pode ser disparado pela aba Actions a qualquer momento |

Para publicar uma nova versão:

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Como compilar localmente

Pré-requisitos: Android Studio (ou Android SDK com a plataforma 37) e JDK 25.

```bash
git clone https://github.com/muriloms2006-sketch/passos.git
cd passos
./gradlew assembleDebug        # no Windows: gradlew.bat assembleDebug
```

O APK é gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Tecnologias

- Java 11 (código-fonte) e Android SDK (minSdk 24, targetSdk 37)
- Gradle 9.6 com Android Gradle Plugin 9.4
- SQLite (`SQLiteOpenHelper`) e `SharedPreferences`
- `SensorManager` e `LocationManager` do Android
- JUnit 4 para os testes unitários
- GitHub Actions

## Integrantes

- Murilo
- Kauã
- Yuri
- Gabrielle
