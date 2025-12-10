# 🤖 SportManagerBot

SportManagerBot è un bot Telegram sviluppato in **Java** da **Nicola Creazzo**.  
Il bot nasce con l’obiettivo di unire **informazione sportiva**, **organizzazione degli allenamenti** e **intrattenimento** in un’unica applicazione utilizzabile direttamente da Telegram.

---

## 📌 Idea del progetto

Il bot permette all’utente di:
- ottenere informazioni aggiornate su alcuni sport (F1, Motorsport, Basket e Calcio) tramite API REST via HTTPS
- gestire i propri allenamenti in palestra grazie a un database relazionale interno
- utilizzare funzionalità extra come il “meme sportivo del giorno” e semplici mini-giochi

---

## 📰 API utilizzate

Il progetto utilizza esclusivamente API che restituiscono **dati testuali in formato JSON**.

- **F1 – Ergast API**  
  https://ergast.com/mrd/

- **Motorsport (multi-serie) – MotorsportsInfo API**  
  https://motorsportsinfo.app/

- **Basket – balldontlie API**  
  https://www.balldontlie.io/

- **Calcio – Football-Data.org API**  
  https://www.football-data.org/

Tutte le chiamate alle API avvengono tramite protocollo **HTTPS**.

---

## 🏋️ Database e gestione allenamenti

SportManagerBot utilizza un **database relazionale locale** (SQLite o MySQL) per salvare:

- allenamenti programmati
- esercizi
- serie e ripetizioni
- date degli allenamenti

Ogni utente Telegram può gestire il proprio piano di allenamento direttamente attraverso i comandi del bot.

---

## ⚙️Configurazione del file `config.properties`

Per motivi di sicurezza, il file `config.properties` **non è incluso nel repository**.

### Passaggi per configurarlo:

1. Duplica il file di esempio:

    - Windows CMD:
      ```cmd
      copy reources\example.properties reources\config.properties
      ```
2. Inserisci i parametri sensibili forniti dagli sviluppatori (es. BOT_TOKEN).

---

## Autore
Nicola Creazzo

---

## TO DO
- API immagini Unplash API / Pexels API
- Struttura DB per allenamento
- Struttura DB per meme del giorno
