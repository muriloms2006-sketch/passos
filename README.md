# Meus Passos

[![Android CI](https://github.com/muriloms2006-sketch/passos/actions/workflows/android.yml/badge.svg)](https://github.com/muriloms2006-sketch/passos/actions/workflows/android.yml)

Aplicativo Android de **contagem de passos** desenvolvido como trabalho final da
Unidade Curricular **Desenvolvimento para Dispositivos Móveis**.

O app conta os passos do usuário usando o sensor do celular, mostra o progresso
em relação a uma meta diária em um gráfico desenhado pelo próprio grupo, obtém a
localização do dispositivo e salva registros (passos + localização + data/hora)
em um banco de dados local, exibindo o histórico na tela.

## Funcionalidades

- Contagem de passos em tempo real a partir do sensor de passos do aparelho
- Barra de progresso customizada mostrando o percentual da meta diária (5000 passos)
- Obtenção da localização atual (latitude e longitude) via GPS ou rede
- Salvamento de registros com passos, localização e data/hora
- Histórico dos registros salvos, do mais recente para o mais antigo

## Requisitos do trabalho

| Requisito | Como foi atendido | Onde está no código |
|---|---|---|
| **View customizada** | `GraficoPassos` estende `View` e desenha, via `Canvas` e `Paint`, uma barra de progresso arredondada com o percentual da meta | [`GraficoPassos.java`](app/src/main/java/com/app/meuspassos/GraficoPassos.java) |
| **Banco de dados** | SQLite com `SQLiteOpenHelper`: tabela `registros` com inserção e listagem dos registros | [`BancoDados.java`](app/src/main/java/com/app/meuspassos/BancoDados.java) |
| **Geolocalização** | `LocationManager` com provedor GPS (ou rede, como alternativa), tratando as permissões em tempo de execução | [`MainActivity.java`](app/src/main/java/com/app/meuspassos/MainActivity.java) (`obterLocalizacao`) |
| **Sensores** | Sensor `TYPE_STEP_COUNTER` (contador de passos) via `SensorManager` / `SensorEventListener` | [`MainActivity.java`](app/src/main/java/com/app/meuspassos/MainActivity.java) (`iniciarSensor`, `onSensorChanged`) |
| **Compilação automática** | GitHub Actions compila o projeto, roda os testes e gera o APK a cada push/PR | [`.github/workflows/android.yml`](.github/workflows/android.yml) |

## Estrutura do projeto

```
app/src/main/
├── AndroidManifest.xml            # Permissões e declaração da Activity
├── java/com/app/meuspassos/
│   ├── MainActivity.java          # Tela principal: sensor, localização, botões e histórico
│   ├── GraficoPassos.java         # View customizada (barra de progresso da meta)
│   └── BancoDados.java            # Acesso ao SQLite (tabela "registros")
└── res/layout/
    └── activity_main.xml          # Layout da tela principal
```

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
2. Caminhe com o celular: o contador e a barra de progresso são atualizados automaticamente.
3. Toque em **Obter localização** e permita o acesso à localização.
4. Toque em **Salvar registro** para gravar os passos e a localização atuais no histórico.

> **Observação:** o sensor de passos (`TYPE_STEP_COUNTER`) não existe na maioria
> dos emuladores. Para testar a contagem de passos, use um celular físico.

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
| Push ou Pull Request para `master` | Compila o app, roda os testes unitários e publica o APK como artifact |
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
- SQLite (`SQLiteOpenHelper`)
- `SensorManager` e `LocationManager` do Android
- GitHub Actions

## Integrantes

- Murilo
- Kauã
- _(adicionar demais integrantes)_
