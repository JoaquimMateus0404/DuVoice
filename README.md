# DuVoice - App de Gravação de Áudio

## ⚠️ IMPORTANTE - Configuração do Java

Este projeto requer **Java 17 ou Java 21** (LTS). Se você estiver com erro de build com Java 25, siga estes passos:

### No Android Studio:
1. Abra **File > Settings** (ou **Android Studio > Preferences** no Mac)
2. Navegue para **Build, Execution, Deployment > Build Tools > Gradle**
3. Em **Gradle JDK**, selecione **"Embedded JDK"** (geralmente JDK 17 ou 21)
4. Clique em **Apply** e **OK**
5. Execute **File > Sync Project with Gradle Files**

### Ou instale Java 17/21:
- [Download Java 17 (Amazon Corretto)](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)
- [Download Java 21 (Oracle)](https://www.oracle.com/java/technologies/downloads/#java21)

Depois de instalar, configure o `JAVA_HOME` para apontar para a instalação.

---

## 🎙️ Funcionalidades

### Funções Básicas
- ▶️ **Gravar / ⏸️ Pausar / ⏹️ Parar** áudio
- 🔊 **Reproduzir** gravações
- 📁 **Lista** de áudios gravados
- ✏️ **Renomear** gravações
- 🗑️ **Apagar** gravações
- ⏱️ **Mostrar duração** da gravação
- 📊 **Indicador visual** do som (ondas)

### Funções Avançadas
- 🎚️ **Qualidade do áudio** (baixa/média/alta)
- 📂 **Categorias** (Geral, Aulas, Reuniões, Ideias, Música)
- ⭐ **Favoritar** gravações
- 🔍 **Pesquisa e filtros**
- 📱 **Ordenação** (por data, nome, duração)
- 🎵 **Formatos** (WAV, AAC)
- 🎧 **Mono ou Estéreo**

---

## 🏗️ Arquitetura

- **MVVM** (Model-View-ViewModel)
- **Room** para persistência local
- **DataStore** para configurações
- **Coroutines & Flow** para operações assíncronas
- **Navigation Component** para navegação
- **Material Design 3** para UI
- **ViewBinding** para acesso às views

---

## 📦 Estrutura do Projeto

```
com.cleansoft.duvoice/
├── data/
│   ├── local/          # Room Database (DAO, Entity)
│   ├── model/          # Modelos de dados
│   └── repository/     # Repositories
├── service/            # Foreground Service para gravação
├── ui/
│   ├── components/     # Custom Views (WaveformView)
│   ├── home/           # Lista de gravações
│   ├── player/         # Reprodutor de áudio
│   ├── record/         # Gravação de áudio
│   └── settings/       # Configurações
└── util/audio/         # AudioRecorder, AudioPlayer, AudioEncoder
```

---

## 🚀 Como Executar

1. Abra o projeto no **Android Studio**
2. Configure o **Gradle JDK** conforme instruções acima
3. Sincronize o projeto (**File > Sync Project with Gradle Files**)
4. Execute no emulador ou dispositivo físico

---

## 📝 Permissões Necessárias

O app requer as seguintes permissões:
- `RECORD_AUDIO` - Para gravar áudio
- `FOREGROUND_SERVICE` - Para continuar gravando em background
- `FOREGROUND_SERVICE_MICROPHONE` - Tipo de foreground service (Android 14+)
- `POST_NOTIFICATIONS` - Para mostrar notificação de gravação (Android 13+)

---

## 🔧 Requisitos

- **Android Studio**: Hedgehog (2023.1.1) ou superior
- **Gradle**: 8.13+
- **Kotlin**: 2.0.21
- **compileSdk**: 36
- **minSdk**: 28 (Android 9.0)
- **Java**: 17 ou 21 (LTS)

---

## 📄 Licença

Este é um projeto pessoal desenvolvido para fins de aprendizado e uso pessoal.

