# 🏆 SportManagerBot

**SportManagerBot** è un bot Telegram sviluppato in **Java** da **Nicola Creazzo**, pensato per unire **informazione sportiva**, **intrattenimento** e **gestione degli allenamenti personali** in un’unica applicazione accessibile direttamente da Telegram.

Il bot interagisce con diverse **API REST pubbliche** per fornire dati aggiornati su vari sport come **Formula 1, WEC, Basket NBA e Calcio**, oltre a contenuti multimediali e informazioni enciclopediche.

---

## 📌 Idea del progetto

SportManagerBot nasce con l’obiettivo di permettere all’utente di:

- 📊 Ottenere **informazioni sportive aggiornate** tramite API REST
- 🖼️ Ricevere **immagini e video sportivi**
- 📚 Consultare **schede informative di team, piloti, atleti** tramite Wikipedia
- 🏋️ Gestire i propri **allenamenti in palestra** tramite database
- 🎉 Usufruire di funzionalità extra come:
    - meme sportivi
    - mini-giochi
    - statistiche personalizzate

Il tutto senza uscire da Telegram.

---

## 🧩 Tecnologie utilizzate

- **Telegram Bot API**
- **HTTP Client Java**
- **Gson** (JSON parsing)
- **API REST via HTTPS**

---

## 🌐 API utilizzate

Il bot utilizza esclusivamente **API REST pubbliche**:

| API                       | Utilizzo                                 | Documentazione                                                 |
|---------------------------|------------------------------------------|----------------------------------------------------------------|
| Ergast API (jolpi/ergast) | Formula 1 (gare, piloti, costruttori)    | https://github.com/jolpica/jolpica-f1/blob/main/docs/README.md |
| TheSportsDB               | Motorsport, WEC, info team               | https://www.thesportsdb.com/api.php                            |
| Ball Don't Lie            | Basket NBA (giocatori, squadre, partite) | https://www.balldontlie.io                                     |
| API-Football              | Calcio (Serie A, Champions, Mondiali)    | https://www.api-football.com/documentation-v3                  |
| Pexels API                | Foto e video sportivi                    | https://www.pexels.com/api/                                    |
| Wikipedia REST API        | Descrizioni, immagini, loghi             | https://www.mediawiki.org/wiki/REST_API                        |

---

## ⚙️ Setup del progetto

### Clonazione repository
```bash
git clone https://github.com/tuo-username/SportManagerBot.git
cd SportManagerBot
```

### Configurazione file ``config.properties``
Copia il file di esempio:
```bash
cp example.properties config.properties
```
Apri il file e inserisci le tue chiavi API!
```properties
BOT_TOKEN = YOUR:BOT:TOKEN
PEXELS_API_KEY = YOUR:API:KEY
THESPORTSDB_API_KEY = YOUR:API:KEY
BALLDONTLIE_API_KEY = YOUR:API:KEY
APIFOOTBALL_API_KEY = YOUR:API:KEY
```

### Dipendenze e requisiti

- **Java JDK 21**
- **Apache Maven**
- Connessione HTTPS abilitata

Le principali librerie utilizzate dal progetto sono:

- **Telegram Bots API**
    - `telegrambots-longpolling`
    - `telegrambots-client`

- **Gson**
    - `com.google.code.gson:gson`

- **Apache Commons Configuration**
    - `commons-configuration2`

- **Apache Commons BeanUtils**
    - `commons-beanutils`

Estratto di ``POM.xml``:
```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<dependencies>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.13.1</version>
    </dependency>

    <dependency>
        <groupId>org.telegram</groupId>
        <artifactId>telegrambots-longpolling</artifactId>
        <version>9.2.0</version>
    </dependency>

    <dependency>
        <groupId>org.telegram</groupId>
        <artifactId>telegrambots-client</artifactId>
        <version>9.2.0</version>
    </dependency>

    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-configuration2</artifactId>
        <version>2.12.0</version>
    </dependency>

    <dependency>
        <groupId>commons-beanutils</groupId>
        <artifactId>commons-beanutils</artifactId>
        <version>1.9.4</version>
    </dependency>
</dependencies>
```

---

## 🤖 Guida all’utilizzo

### Comandi base
```
/start – Avvia il bot
/help – Mostra il messaggio di aiuto
```

### Foto e Video
```
/photo <sport> – Ricevi un’immagine sportiva
/video <sport> – Ricevi un video sportivo
```

### Formula 1
```
/f1 next – Prossima gara
/f1 last – Ultima gara
/f1 last results – Classifica ultima gara
/f1 drivers – WDC aggiornata
/f1 constructors – WCC aggiornata
/f1 calendar <anno> – Calendario stagione
/f1 driver <nome> – Info pilota
/f1 teams – Lista di alcuni team storici
/f1 team <nome> – Info team
```

### FIA WEC
```
/wec next – Prossima gara
/wec last – Ultima gara
/wec seasons <anno> – Stagione dell'anno scelto
/wec car <team> <modello> – Info sul modello scelto
```

### Basket (NBA)
```
/basket players – Lista giocatori (prima pagina)
/basket player <nome> – Cerca un giocatore
/basket teams – Lista squadre NBA
/basket games season <anno> – Partite per stagione
/basket games team <id> <anno> – Partite per team
/basket team <nome> – Info team
```

### Calcio (Champions League, Serie A, World Cup)
```
/soccer <lega> – Mostra i comandi disponibili
/soccer <lega> teams <anno> – Lista squadre
/soccer <lega> players <idTeam> <anno> – Giocatori del team
/soccer <lega> player <idGiocatore> <anno> – Info giocatore
/soccer <lega> player <nome> – Info giocatore
/soccer <lega> season <anno> – Partite della stagione
/soccer <lega> standings – Classifica aggiornata
/soccer <lega> team <nome> – Info team
```

---

# 🗣️ Esempi di conversazione
<img src="assets/start.jpeg" alt="Esempio di utilizzo del bot" width="49%"/>
<img src="assets/photo_video.jpeg" alt="Esempio di utilizzo del bot" width="49%"/>
<img src="assets/f1_status.jpeg" alt="Esempio di utilizzo del bot" width="49%"/>
<img src="assets/basket_wiki.jpeg" alt="Esempio di utilizzo del bot" width="49%"/>
<img src="assets/soccer_wiki.jpeg" alt="Esempio di utilizzo del bot" width="49%"/>
<img src="assets/soccer_example.jpeg" alt="Esempio di utilizzo del bot" width="49%"/>