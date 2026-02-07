<p align="center">
  <img src="app/src/main/res/drawable/ic_launcher_foreground.xml" width="120" alt="DuVoice Logo"/>
</p>

<h1 align="center">🎙️ DuVoice</h1>

<p align="center">
  <strong>Aplicação profissional de gravação de voz para Android</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-28%2B-green?logo=android" alt="Min SDK"/>
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-purple?logo=kotlin" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Material%20Design-3-blue?logo=material-design" alt="Material 3"/>
  <img src="https://img.shields.io/badge/License-Proprietary-red" alt="License"/>
</p>

<p align="center">
  <a href="#-funcionalidades">Funcionalidades</a> •
  <a href="#-screenshots">Screenshots</a> •
  <a href="#-instalação">Instalação</a> •
  <a href="#-arquitetura">Arquitetura</a> •
  <a href="#-tecnologias">Tecnologias</a>
</p>

---

## 📱 Sobre o Projeto

**DuVoice** é uma aplicação Android moderna e intuitiva para gravação de voz, desenvolvida com as melhores práticas de desenvolvimento Android. Ideal para estudantes, profissionais e qualquer pessoa que precise capturar áudio com qualidade.

### ✨ Destaques

- 🎨 **Interface moderna** com Material Design 3
- 🌙 **Modo escuro** automático
- 📊 **Visualização de ondas** em tempo real
- 🔒 **Gravação em background** com notificação
- 📱 **Widget** de gravação rápida na home screen
- ⚡ **Performance otimizada** e baixo consumo de bateria

---

## 🎯 Funcionalidades

### Gravação de Áudio
| Funcionalidade | Descrição |
|----------------|-----------|
| ▶️ Gravar | Iniciar gravação com um toque |
| ⏸️ Pausar/Retomar | Pausar e continuar a gravação |
| ⏹️ Parar | Finalizar e guardar a gravação |
| 🎚️ Qualidade | Baixa, Média ou Alta qualidade |
| 🎵 Formato | WAV (sem compressão) ou AAC |
| 🎧 Canais | Mono ou Estéreo |
| 📊 Visualização | Ondas de áudio em tempo real |

### Gestão de Gravações
| Funcionalidade | Descrição |
|----------------|-----------|
| 📁 Lista | Ver todas as gravações |
| 🔊 Reproduzir | Player com controles completos |
| ✏️ Renomear | Editar nome da gravação |
| 🗑️ Apagar | Eliminar gravações |
| ⭐ Favoritos | Marcar gravações importantes |
| 📂 Categorias | Organizar por tipo (Aulas, Reuniões, Ideias, etc.) |
| 🔍 Pesquisa | Encontrar gravações rapidamente |
| 📤 Partilhar | Enviar gravações para outras apps |

### Funcionalidades Avançadas
| Funcionalidade | Descrição |
|----------------|-----------|
| 🔔 Notificação | Controles de gravação na notificação |
| 📱 Widget | Gravação rápida a partir da home screen |
| 💡 Modo Ideia | Capturar ideias rapidamente |
| 📈 Estatísticas | Ver tempo total gravado |
| ⚙️ Configurações | Personalizar comportamento da app |

---

## 📸 Screenshots

<p align="center">
  <i>Screenshots em breve...</i>
</p>

<!-- 
<p align="center">
  <img src="screenshots/home.png" width="200"/>
  <img src="screenshots/record.png" width="200"/>
  <img src="screenshots/player.png" width="200"/>
  <img src="screenshots/settings.png" width="200"/>
</p>
-->

---

## 📥 Instalação

### Requisitos
- Android **9.0** (API 28) ou superior
- ~50 MB de espaço livre
- Permissão de microfone

### Play Store
<a href="#">
  <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" width="200"/>
</a>

*Em breve disponível na Google Play Store*

### Build Manual

```bash
# Clonar o repositório
git clone https://github.com/cleansoft/duvoice.git

# Entrar na pasta
cd duvoice

# Compilar APK de debug
./gradlew assembleDebug

# Ou APK de release (requer keystore)
./gradlew assembleRelease
```

---

## 🏗️ Arquitetura

O projeto segue a arquitetura **MVVM** (Model-View-ViewModel) recomendada pela Google:

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │  Fragments  │  │  ViewModels │  │   Adapters  │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                   Repositories                        │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ Room (SQLite)│  │  DataStore  │  │ File System │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

### Estrutura de Pastas

```
com.cleansoft.duvoice/
│
├── 📁 data/
│   ├── local/              # Room Database
│   │   ├── dao/            # Data Access Objects
│   │   ├── entity/         # Entidades da BD
│   │   └── AppDatabase.kt
│   ├── model/              # Modelos de domínio
│   └── repository/         # Repositórios
│
├── 📁 service/
│   └── AudioRecordService.kt   # Foreground Service
│
├── 📁 ui/
│   ├── components/         # Custom Views
│   │   └── WaveformView.kt
│   ├── home/               # Ecrã principal
│   ├── player/             # Reprodutor
│   ├── record/             # Gravação
│   ├── settings/           # Definições
│   └── stats/              # Estatísticas
│
├── 📁 util/
│   └── audio/              # Utilitários de áudio
│       ├── AudioRecorder.kt
│       ├── AudioPlayer.kt
│       └── AudioEncoder.kt
│
├── 📁 widget/
│   └── QuickRecordWidget.kt    # Widget home screen
│
└── MainActivity.kt
```

---

## 🛠️ Tecnologias

### Core
| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| Kotlin | 2.0.21 | Linguagem principal |
| Android SDK | 36 | Target SDK |
| Gradle | 8.13+ | Build system |

### Jetpack & AndroidX
| Biblioteca | Utilização |
|------------|------------|
| Room | Base de dados local SQLite |
| DataStore | Preferências do utilizador |
| Navigation | Navegação entre ecrãs |
| ViewModel | Gestão de estado da UI |
| LiveData & Flow | Dados reativos |
| ViewBinding | Acesso type-safe às views |
| SplashScreen | Ecrã de arranque |

### UI
| Biblioteca | Utilização |
|------------|------------|
| Material Design 3 | Componentes de UI modernos |
| ConstraintLayout | Layouts flexíveis |
| RecyclerView | Listas performantes |
| SwipeRefreshLayout | Pull to refresh |

### Processamento de Áudio
| API | Utilização |
|-----|------------|
| MediaRecorder | Gravação de áudio |
| MediaPlayer | Reprodução de áudio |
| AudioRecord | Captura de dados brutos (waveform) |

---

## 🔐 Permissões

| Permissão | Razão |
|-----------|-------|
| `RECORD_AUDIO` | Capturar áudio do microfone |
| `FOREGROUND_SERVICE` | Manter gravação ativa em background |
| `FOREGROUND_SERVICE_MICROPHONE` | Tipo de serviço (Android 14+) |
| `POST_NOTIFICATIONS` | Mostrar notificação de gravação |
| `VIBRATE` | Feedback tátil |

---

## ⚠️ Configuração do Ambiente de Desenvolvimento

### Requisitos
- **Android Studio**: Hedgehog (2023.1.1) ou superior
- **JDK**: 17 ou 21 (LTS)
- **Gradle**: 8.13+

### Configurar Java no Android Studio

1. **File > Settings** (ou **Preferences** no Mac)
2. **Build, Execution, Deployment > Build Tools > Gradle**
3. Em **Gradle JDK**, selecione **JDK 17** ou **21**
4. Clique em **Apply** e **OK**
5. **File > Sync Project with Gradle Files**

### Build de Release

```bash
# Gerar APK assinado
./gradlew assembleRelease

# Gerar AAB para Play Store
./gradlew bundleRelease
```

---

## 📄 Licença

Copyright © 2024-2026 CleanSoft. Todos os direitos reservados.

Este software é proprietário. Não é permitida a cópia, modificação ou distribuição sem autorização expressa.

---

## 👨‍💻 Autor

**Duarte Gauss**

- 📧 Email: joaquimmateus0404@gmail.com
- 🌐 Website: [joaquimmateus.com](https://joaquim-mateus.vercel.app/)

---

