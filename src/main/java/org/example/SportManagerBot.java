package org.example;
import DbModels.*;
import Models.ApiFootball.fixtures.FixturesResponse;
import Models.ApiFootball.standings.League;
import Models.ApiFootball.standings.StandingsResponse;
import Models.BallDontLie.*;
import Models.Ergast.Constructor;
import Models.Ergast.MRData;
import Models.TheSportsDb.EventsResponse;
import Models.Wiki.WikipediaSummaryResponse;
import Services.*;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import java.time.LocalDate;
import java.util.*;

public class SportManagerBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    // Stato conversazione => alcuni comandi hanno bisogno di un secondo input
    private final Map<Long, BotState> userStates = new HashMap<>();

    // Sport supportati
    private static final String[] sportAccepted = {
            "f1", "soccer", "nba",
            "basketball", "wec", "motorsport"
    };

    // enum dei possibilit stati
    private enum BotState {
        none,
        waiting_photo,
        waiting_video,
        waiting_f1,
        waiting_wec,
        waiting_basket,
        waiting_soccer,
        waiting_training,
        waiting_training_day,
        waiting_exercise,
        waiting_workout
    }

    // Le leghe del calcio hanno degli id specifici
    private static final Map<String, Integer> leaguesMap = Map.of(
        "seriea", 135,
        "champions", 2,
        "worldcup", 1
    );

    // Costruttore
    public SportManagerBot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
    }

    // Main del programma
    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;

        String messageText = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getUserName();
        String firstName = update.getMessage().getFrom().getFirstName();

        // Inserisci nel db il nuovo utente
        User user = DBManager.getInstance().getUserByTelegramId(chatId);
        if (user == null) {
            boolean created = DBManager.getInstance().addUser(chatId, username, firstName);
            if (!created) {
                send("❌ Errore: impossibile registrarti nel database.", chatId, false);
                return;
            }
        }

        BotState state = userStates.getOrDefault(chatId, BotState.none);

        // Gestione degli stati d'attesa del bot
        String[] args = messageText.split(" ");
        switch(state) {
            case waiting_photo:
                userStates.put(chatId, BotState.none);
                sendPhoto(messageText, chatId);
                return; // Return e non break perchè non deve accettare altro
            case waiting_video:
                userStates.put(chatId, BotState.none);
                sendVideo(messageText, chatId);
                return;
            case waiting_f1:
                userStates.put(chatId, BotState.none);
                handleF1Command(args, chatId);
                return;
            case waiting_wec:
                userStates.put(chatId, BotState.none);
                handleWecCommand(args, chatId);
                return;
            case waiting_basket:
                userStates.put(chatId, BotState.none);
                handleBasketCommand(args, chatId);
                return;
            case waiting_soccer:
                userStates.put(chatId, BotState.none);
                handleSoccerCommand(args, chatId);
                return;
            case waiting_training:
                userStates.put(chatId, BotState.none);
                handleTrainingCommand(args, chatId);
                return;
            case waiting_training_day:
                userStates.put(chatId, BotState.none);
                handleTrainingDayCommand(args, chatId);
                return;
            case waiting_exercise:
                userStates.put(chatId, BotState.none);
                handleExerciseCommand(args, chatId);
                return;
            case waiting_workout:
                userStates.put(chatId, BotState.none);
                handleWorkoutCommand(args, chatId);
                return;
        }

        String mediaMsg = """
                    📸 <b>Che sport vuoi?</b>
                    
                    Scrivi uno tra:
                        • F1 🏎️
                        • Motorsport 🚗
                        • WEC 🏎
                        • Calcio ⚽
                        • Basketball 🏀
                        • NBA ⛹️
                    """;

        switch (args[0]) {
            case "/start":
                startMessage(chatId);
                break;
            case "/help":
                helpMessage(chatId);
                break;
            case "/photo":
                if (args.length == 1) {
                    userStates.put(chatId, BotState.waiting_photo);
                    send(mediaMsg, chatId, true);
                } else
                    sendPhoto(args[1], chatId);
                break;
            case "/video":
                if (args.length == 1) {
                    userStates.put(chatId, BotState.waiting_video);
                    send(mediaMsg, chatId, true);
                }
                else
                    sendVideo(args[1], chatId);
                break;
            case "/f1":
                if (args.length == 1) {
                    userStates.put(chatId, BotState.waiting_f1);
                    String msg = """
                    🏎️  <b>Comandi F1</b>
                
                    Scegli uno dei comandi:
                    
                    🏁  <b>next</b> – Prossima gara
                    ⏮️  <b>last</b> – Ultima gara
                    📊  <b>last results</b> – Classifica ultima gara
                    👤  <b>drivers</b> – WDC aggiornata
                    🏎️  <b>constructors</b> – WCC aggiornata
                    📅  <b>calendar &lt;anno&gt;</b> – Calendario stagione
                    👤  <b>driver &lt;nome&gt;</b> – Info su un pilota
                    🏢  <b>teams</b> – Lista dei team
                    🏢  <b>team &lt;nome&gt;</b> – Info su un team
                    
                    ℹ️  Maggiori info con il comando <b>/help</b>
                    """;
                    send(msg, chatId, true);
                } else {
                    // Elimina il primo elemento da args => non tiene più conto di "/f1"
                    args = Arrays.copyOfRange(args, 1, args.length);
                    handleF1Command(args, chatId);
                }
                break;
            case "/wec":
                if (args.length == 1) {
                    userStates.put(chatId, BotState.waiting_wec);
                    String msg = """
                    🏎️  <b>Comandi WEC</b>
                
                    Scegli uno dei comandi:
                    
                    🏁  <b>next</b> – Prossima gara
                    ⏮️  <b>last</b> – Ultima gara
                    📊  <b>seasons &lt;anno&gt;</b> – Stagione dell'anno scelto
                    🏢  <b>car &lt;team&gt; &lt;modello&gt;</b> – Info sul modello scelto
                    
                    ℹ️  Maggiori info con il comando <b>/help</b>
                    """;
                    send(msg, chatId, true);
                } else {
                    args = Arrays.copyOfRange(args, 1, args.length); // rimuovo "/wec"
                    handleWecCommand(args, chatId);
                }
                break;
            case "/basket":
                if (args.length == 1) {
                    // Nessun argomento specificato, attendi input
                    userStates.put(chatId, BotState.waiting_basket);
                    String msg = """
                    🏀  <b>Comandi Basket</b>
                
                    Scegli uno dei comandi:
                
                    👤  <b>players</b> – Lista giocatori (prima pagina)
                    🔍  <b>player &lt;nome&gt;</b> – Cerca un giocatore
                    🏀  <b>teams</b> – Lista squadre NBA
                    📅  <b>games season &lt;anno&gt;</b> – Partite per stagione
                    📅  <b>games team &lt;id&gt; &lt;anno&gt;</b> - Partite per team
                    🏢  <b>team &lt;nome&gt;</b> – Info su un team
                
                    ℹ️  Maggiori info con il comando <b>/help</b>
                    """;
                    send(msg, chatId, true);
                } else {
                    // Rimuove "/basket" e gestisce i comandi separatamente
                    String[] basketArgs = Arrays.copyOfRange(args, 1, args.length);
                    handleBasketCommand(basketArgs, chatId);
                }
                break;
            case "/soccer":
                if (args.length == 1) {
                    userStates.put(chatId, BotState.waiting_soccer);
                    String msg = """
                    ⚽  <b>Comandi Soccer</b>
            
                    Scegli una lega con /soccer {lega} &lt;comando&gt;:
            
                    • SerieA
                    • Champions
                    • WorldCup
            
                    Dopo, scegli uno dei comandi:

                    👥 <b>teams &lt;anno&gt;</b> – Lista squadre
                    👤 <b>players &lt;teamId&gt;</b> – Giocatori di un team
                    🔍 <b>player &lt;playerId&gt; &lt;anno&gt;</b> – Info giocatore
                    🔍 <b>player &lt;nome&gt;</b> – Info giocatore
                    📅 <b>season &lt;anno&gt;</b> – Partite stagione
                    📊 <b>standings &lt;anno&gt;</b> – Classifica aggiornata
                    🏢 <b>team &lt;nome&gt;</b> – Info su un team
                    
                    ⚠️ <b>Attenzione!</b> anni accettati tra il <b>2021</b> e il <b>2023</b>.
            
                    ℹ️ Maggiori info con il comando <b>/help</b>
                    """;
                    send(msg, chatId, true);
                } else {
                    String[] soccerArgs = Arrays.copyOfRange(args, 1, args.length);
                    handleSoccerCommand(soccerArgs, chatId);
                }
                break;
            case "/training":
                if (args.length == 1) {
                    userStates.put(chatId, BotState.waiting_training);
                    String msg = """
                    🏋️ <b>Gestione Allenamenti</b>
                    
                    Comandi disponibili:
                    
                    ➕ <b>new &lt;nome&gt;</b> – Crea una scheda
                    📋 <b>list</b> – Elenco schede
                    ⭐ <b>select &lt;id&gt;</b> – Attiva una scheda
                    🗑️ <b>remove &lt;id&gt;</b> – Rimuovi una scheda
                    📋 <b>list &lt;id&gt; days</b> – Elenco allenamenti
                    📋 <b>list &lt;id&gt; exercises </b> – Elenco esercizi
                    """;
                    send(msg, chatId, true);
                } else {
                    String[] trainingArgs = Arrays.copyOfRange(args, 1, args.length);
                    handleTrainingCommand(trainingArgs, chatId);
                }
                break;
            case "/trainingDay":
                if (args.length == 1) {
                    userStates.put(chatId, BotState.waiting_training_day);
                    String msg = """
                    🗓️ <b>Gestione Giorni di Allenamento</b>
                    
                    Comandi disponibili:
                    
                    ➕ <b>add &lt;idScheda&gt; &lt;giorno&gt; &lt;focus&gt;</b> - Aggiungi allenamento
                    ❌ <b>remove &lt;idGiorno&gt;</b> - Elimina allenamento
                    📋 <b>list &lt;idScheda&gt;</b> - Elenco allenamenti
                    """;
                    send(msg, chatId, true);
                } else {
                    String[] trainingDayArgs = Arrays.copyOfRange(args, 1, args.length);
                    handleTrainingDayCommand(trainingDayArgs, chatId);
                }
                break;
            case "/exercise":
                if (args.length == 1) {
                    userStates.put(chatId, BotState.waiting_exercise);
                    String msg = """
                    🏋️ <b>Gestione Esercizi</b>
                    
                    Comandi disponibili:
                    
                    ➕ <b>add &lt;id giorno&gt; &lt;nome&gt; # &lt;sets&gt; &lt;reps&gt; &lt;peso&gt; # [note]</b> - Aggiungi esercizio
                    ❌ <b>remove &lt;id esercizio&gt;</b> - Elimina esercizio
                    📋 <b>list &lt;id scheda&gt;</b> - Elenco esercizi
                    """;
                    send(msg, chatId, true);
                } else {
                    String[] exerciseArgs = Arrays.copyOfRange(args, 1, args.length);
                    handleExerciseCommand(exerciseArgs, chatId);
                }
                break;
            case "/workout":
                if (args.length == 1) {
                    userStates.put(chatId, BotState.waiting_workout);
                    String msg = """
                    🏃‍♂️ <b>Gestione Allenamenti Svolti</b>
                    
                    Comandi disponibili:
                    
                    ▶️ <b>start &lt;id giorno&gt;</b> – Inizia una sessione di allenamento
                    ✅ <b>complete &lt;id sessione&gt;</b> – Completa una sessione
                    📊 <b>list &lt;id giorno&gt;</b> – Elenco sessioni registrate
                    """;
                    send(msg, chatId, true);
                } else {
                    String[] workoutArgs = Arrays.copyOfRange(args, 1, args.length);
                    handleWorkoutCommand(workoutArgs, chatId);
                }
                break;
            default:
                send("❓ Comando non riconosciuto. Usa /help", chatId, false);
        }
    }

    // Metodi divisi per comando

    // Start
    private void startMessage(long chatId) {
        String msg = """
        👋 Benvenuto in <b>SportManagerBot</b>!
        
        🏎️ Info sportive
        📸 Immagini a tema sport
        🏋️ Allenamenti
        🎮 Contenuti extra
        
        Usa <b>/help</b> per iniziare!
        """;
        send(msg, chatId, true);
    }

    // Help
    private void helpMessage(long chatId) {
        String msg = """
        📖 <b>Comandi disponibili</b>
        
        <b>/start</b> – Avvia il bot
        <b>/help</b> – Mostra questo messaggio
        
        📸 <b>Foto e Video</b>
        <b>/photo &lt;sport&gt;</b> – Ricevi un’immagine sportiva
        <b>/video &lt;sport&gt;</b> – Ricevi un video sportivo
        
        🏎️ <b>Formula 1</b>
        /f1 next – Prossima gara
        /f1 last – Ultima gara
        /f1 last results – Classifica ultima gara
        /f1 drivers – WDC aggiornata
        /f1 constructors – WCC aggiornata
        /f1 calendar &lt;anno&gt; – Calendario stagione
        /f1 driver &lt;nome&gt; – Info pilota
        /f1 teams – Lista di alcuni team storici
        /f1 team &lt;nome&gt – Info team
        
        🏎 <b>WEC</b>
        /wec next – Prossima gara
        /wec last – Ultima gara
        /wec seasons &lt;anno&gt; – Stagione dell'anno scelto
        /wec car &lt;team&gt; &lt;modello&gt; – Info sul modello scelto
        
        🏀 <b>Basket NBA</b>
        /basket players – Lista giocatori (prima pagina)
        /basket player &lt;nome&gt; – Cerca un giocatore
        /basket teams – Lista squadre NBA
        /basket games season &lt;anno&gt; – Partite per stagione
        /basket games team &lt;id&gt; &lt;anno&gt; - Partite per team
        /basket team &lt;nome&gt – Info team
    
        ⚽ <b>Calcio</b>
        /soccer &lt;lega&gt; – Mostra i comandi disponibili per la lega
        /soccer &lt;lega&gt; teams &lt;anno&gt; – Lista squadre
        /soccer &lt;lega&gt; players &lt;idTeam&gt; &lt;anno&gt; – Giocatori del team
        /soccer &lt;lega&gt; player &lt;idGiocatore&gt; &lt;anno&gt; – Info giocatore
        /soccer &lt;lega&gt; player &lt;nome&gt; – Info giocatore
        /soccer &lt;lega&gt; season &lt;anno&gt; – Partite della stagione
        /soccer &lt;lega&gt; standings – Classifica aggiornata
        /soccer &lt;lega&gt; team &lt;nome&gt – Info team
        
        🏋️ <b>Personal Trainer</b>
        /training new &lt;nome&gt; – Crea nuova scheda
        /training list – Elenco schede
        /training select &lt;id&gt; – Attiva una scheda
        /training remove &lt;id&gt; – Rimuovi una scheda
        /training list &lt;id&gt; days – Elenco giorni di allenamento della scheda
        /training list &lt;id&gt; exercises – Elenco esercizi della scheda
        
        /trainingDay add &lt;id scheda&gt; &lt;nome giorno&gt; – Aggiungi un giorno alla scheda
        /trainingDay remove &lt;id giorno&gt; – Rimuovi un giorno dalla scheda
        /trainingDay list &lt;id scheda&gt; – Elenco giorni di allenamento della scheda
        
        /exercise add &lt;id giorno&gt; &lt;nome esercizio&gt; # &lt;sets&gt; &lt;reps&gt; &lt;peso&gt; # [note] – Aggiungi un esercizio
        /exercise remove &lt;id esercizio&gt; – Rimuovi un esercizio
        /exercise list &lt;id giorno&gt; – Elenco esercizi del giorno
        
        /workout start &lt;id giorno&gt; – Inizia sessione di allenamento
        /workout complete &lt;id sessione&gt; – Completa sessione di allenamento
        /workout list &lt;id giorno&gt; – Elenco sessioni di allenamento registrate
        
        ⚠️ Sport supportati: F1, Motorsport, WEC, Calcio, Basketball
        """;

        send(msg, chatId, true);
    }

    //#region Pexels API
    // Foto
    private void sendPhoto(String query, long chatId) {
        String betterQuery = stringNormalization(query);

        if(checkMediaErrorMessage(betterQuery, chatId))
            return;

        PexelsApi pexelsApi = new PexelsApi();
        String url = pexelsApi.getPhotoUrl(betterQuery);

        if (url == null || url.isEmpty()) {
            send("😕 Nessuna immagine trovata", chatId, false);
            return;
        }

        try {
            telegramClient.execute(
                    SendPhoto.builder()
                            .chatId(chatId)
                            .photo(new InputFile(url))
                            .caption("Immagine di: " + query.toUpperCase())
                            .build()
            );
        } catch (TelegramApiException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Video
    private void sendVideo(String query, long chatId) {
        String betterQuery = stringNormalization(query);

        if(checkMediaErrorMessage(betterQuery, chatId))
            return;

        PexelsApi pexelsApi = new PexelsApi();
        String url = pexelsApi.getVideoUrl(betterQuery);

        if (url == null || url.isEmpty()) {
            send("😕 Nessun video trovato", chatId, false);
            return;
        }

        try {
            telegramClient.execute(
                    SendVideo.builder()
                            .chatId(chatId)
                            .video(new InputFile(url))
                            .caption("Video di: " + query.toUpperCase())
                            .build()
            );
        } catch (TelegramApiException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    //endregion

    //#region Ergast API (F1)
    // Metodo generico
    private void handleF1Command(String[] args, long chatId) {
        if (args.length == 0) {
            send("❌ Devi specificare un comando F1", chatId, false);
            return;
        }

        ErgastApi ergastApi = new ErgastApi();

        switch (args[0].toLowerCase()) {
            case "next":
                f1Next(ergastApi, chatId);
                break;
            case "last":
                if(args.length >= 2 && args[1].equalsIgnoreCase("results")){
                    f1LastResults(ergastApi, chatId);
                    break;
                }
                f1Last(ergastApi, chatId);
                break;
            case "drivers":
                MRData drivers = ergastApi.getDriverStandings();
                send(drivers.StandingsTable.toString(), chatId, true);
                break;
            case "constructors":
                MRData constructors = ergastApi.getConstructorStandings();
                send(constructors.StandingsTable.toString(), chatId, true);
                break;
            case "calendar":
                if(args.length >= 2)
                    f1Calendar(ergastApi,chatId,args[1]);
                else
                    send("❌ Devi specificare un anno", chatId, false);
                break;
            case "driver":
                if (args.length >= 2)
                    f1Driver(ergastApi,chatId,args[1]);
                else
                    send("❌ Devi specificare un pilota", chatId, false);
                break;
            case "teams":
                f1Teams(ergastApi, chatId);
                break;
            case "team":
                String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                f1SpecificTeam(ergastApi, chatId, name);
                break;
            default:
                send("❌ Comando F1 non riconosciuto", chatId, false);
        }
    }

    // Prossima gara
    private void f1Next(ErgastApi ergastApi, long chatId) {
        MRData nextRaceData = ergastApi.getNextRace();
        if (nextRaceData != null && nextRaceData.RaceTable != null && !nextRaceData.RaceTable.Races.isEmpty())
            send(nextRaceData.RaceTable.Races.getFirst().toString(), chatId, true);
        else
            send("😕 Nessuna prossima gara trovata", chatId, false);
    }

    // Ultima gara
    private void f1Last(ErgastApi ergastApi, long chatId) {
        MRData lastRaceData = ergastApi.getLastRace();
        if (lastRaceData != null && lastRaceData.RaceTable != null && !lastRaceData.RaceTable.Races.isEmpty())
            send(lastRaceData.RaceTable.Races.getFirst().toString(), chatId, true);
        else
            send("😕 Nessuna ultima gara trovata", chatId, false);
    }

    // Risultati ultima gara
    private void f1LastResults(ErgastApi ergastApi, long chatId){
        MRData lastResults = ergastApi.getLastRaceResults();
        if (lastResults != null && lastResults.RaceTable != null && !lastResults.RaceTable.Races.isEmpty()) {
            var race = lastResults.RaceTable.Races.getFirst();

            String output = String.format("🏁 Risultati Ultima Gara - %s, Round %s\n\n", race.raceName, lastResults.RaceTable.round);

            for (var r : race.Results)
                output = output.concat(r.toString() + "\n");

            send(output, chatId, true);
        }
        else
            send("😕 Nessun risultato ultima gara", chatId, false);
    }

    // Calendario per anno
    private void f1Calendar(ErgastApi ergastApi, long chatId, String sYear) {
        int year = java.time.Year.now().getValue();

        try {
            year = Integer.parseInt(sYear);
        }
        catch (NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
        }

        MRData calendar = ergastApi.getCalendar(year);
        send(calendar.RaceTable.toString(), chatId, true);
    }

    private void f1Driver(ErgastApi ergastApi, long chatId, String id) {
        WikiSportService wikiService = new WikiSportService();
        MRData data = ergastApi.getDriver(id);

        if (data == null || data.DriverTable == null || data.DriverTable.Drivers == null || data.DriverTable.Drivers.isEmpty()) {
            send("😕 Pilota non trovato", chatId, false);
            return;
        }

        var driver = data.DriverTable.Drivers.getFirst();
        String wikiUrl = driver.url != null && !driver.url.isEmpty() ? driver.url : "https://it.wikipedia.org/wiki/f1";

        WikipediaSummaryResponse resp = wikiService.getFromUrl(wikiUrl);

        String imgUrl = null;
        String text = driver.toString();

        if (resp != null) {
            if (resp.extract != null && !resp.extract.isEmpty())
                text = driver.familyName + " " + driver.givenName + "\n\n" + resp.extract;

            if (resp.thumbnail != null && resp.thumbnail.source != null)
                imgUrl = resp.thumbnail.source;
        }

        sendContentPicture(null, imgUrl, chatId);

        // In-line button
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(buildLinkButton("🏢 Wikipedia: " + driver.familyName, wikiUrl))
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Errore invio messaggio pilota: " + e.getMessage());
        }
    }

    // Dati sul team + inline button + immagine
    private void f1Teams(ErgastApi ergastApi, long chatId) {
        MRData teams = ergastApi.getConstructors();
        WikiSportService wikiService = new WikiSportService();

        if (teams == null || teams.ConstructorTable == null || teams.ConstructorTable.Constructors == null || teams.ConstructorTable.Constructors.isEmpty()) {
            send("😕 Nessun team trovato", chatId, false);
            return;
        }

        // Set per tracciare i prefissi già stampati
        Set<String> printed = new HashSet<>();

        for (var constructor : teams.ConstructorTable.Constructors) {
            String prefix = constructor.name.split("-")[0].trim();

            if (!printed.contains(prefix)){
                sendSingleF1Team(chatId, wikiService, constructor);
                printed.add(prefix);
            }
        }
    }

    private void f1SpecificTeam(ErgastApi ergastApi, long chatId, String name){
        MRData team = ergastApi.getSpecificTeam(name);
        WikiSportService wikiService = new WikiSportService();

        if (team == null || team.ConstructorTable == null || team.ConstructorTable.Constructors == null || team.ConstructorTable.Constructors.isEmpty()) {
            send("😕 Nessun team trovato", chatId, false);
            return;
        }

        var constructor = team.ConstructorTable.Constructors.getFirst();

        sendSingleF1Team(chatId, wikiService, constructor);
    }

    private void sendSingleF1Team(long chatId, WikiSportService wikiService, Constructor constructor) {
        if(constructor.name == null || constructor.nationality == null)
            return;

        // Testo base
        String text = constructor.toString();
        String imgUrl = null;

        WikipediaSummaryResponse resp = null;

        // Provo con url
        if (constructor.url != null && !constructor.url.isEmpty())
            resp = wikiService.getFromUrl(constructor.url);

        // Fallback
        if (resp == null)
            resp = wikiService.getFromText(constructor.name);

        // Se la risposta è valida e ha anche l'url dell'immagine
        if (resp != null) {
            if (resp.extract != null && !resp.extract.isEmpty())
                text = constructor.name + "\n\n" + resp.extract;

            if (resp.thumbnail != null && resp.thumbnail.source != null)
                imgUrl = resp.thumbnail.source;
        }

        sendContentPicture(null, imgUrl, chatId);

        // In-line button
        String wikiLink = constructor.url != null ? constructor.url : "https://it.wikipedia.org";

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(buildLinkButton("🔗 Wikipedia: " + constructor.name, wikiLink))
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Errore invio team: " + e.getMessage());
        }
    }

    //#endregion

    //#region TheSportsDb API (WEC)
    // Metodo generico
    private void handleWecCommand(String[] args, long chatId) {
        if(args.length == 0) {
            send("❌ Devi specificare un comando WEC", chatId, false);
            return;
        }

        TheSportsDbApi wecApi = new TheSportsDbApi();

        switch(args[0].toLowerCase()) {
            case "next":
                wecNext(wecApi, chatId);
                break;
            case "last":
                wecLast(wecApi, chatId);
                break;
            case "seasons":
                if(args.length >= 2)
                    wecSeason(wecApi, chatId, args[1]);
                else
                    send("❌ Devi specificare una stagione", chatId, false);
                break;
            case "car":
                if(args.length >= 2){
                    String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    wecSpecificTeam(chatId, name);
                }
                else
                    send("❌ Devi specificare Team e modello!", chatId, false);
                break;
            default:
                send("❌ Comando WEC non riconosciuto", chatId, false);
                break;
        }
    }

    // Prossima gara
    private void wecNext(TheSportsDbApi wecApi, long chatId){
        EventsResponse resp = wecApi.getNextEvents();

        if(resp == null || resp.events == null || resp.events.isEmpty()) {
            send("😕 Nessun evento trovato", chatId, false);
            return;
        }

        send(resp.toString(), chatId, true);
    }

    // Ultima gara
    private void wecLast(TheSportsDbApi wecApi, long chatId){
        EventsResponse resp = wecApi.getLastEvents();

        if(resp == null || resp.events == null || resp.events.isEmpty()) {
            send("😕 Nessun evento trovato", chatId, false);
            return;
        }

        send(resp.toString(), chatId, true);
    }

    // Stagione per anno
    private void wecSeason(TheSportsDbApi wecApi, long chatId, String season) {
        EventsResponse resp = wecApi.getSeasonEvents(season);
        if(resp == null || resp.events == null || resp.events.isEmpty()) {
            send("😕 Nessun evento trovato per la stagione " + season, chatId, false);
            return;
        }
        send(resp.toString(), chatId, true);
    }

    private void wecSpecificTeam(long chatId, String name) {
        if (name == null || name.isEmpty()) {
            send("😕 Nome team non valido", chatId, false);
            return;
        }

        WikiSportService wikiService = new WikiSportService();

        // Titolo: "{team}_{modello}"
        String wikiTitle = name.trim().replace(" ", "_");

        wikiResponse(wikiService, name, wikiTitle, chatId);
    }

    //#endregion

    //#region BallDontLie API (Basket - NBA)
    private void handleBasketCommand(String[] args, long chatId) {
        if (args.length == 0) {
            send("❌ Devi specificare un comando basket", chatId, false);
            return;
        }

        BallDontLieApi basketApi = new BallDontLieApi();

        switch (args[0].toLowerCase()) {
            case "players":
                basketPlayers(basketApi, chatId);
                break;
            case "player":
                if (args.length >= 2) {
                    String playerName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    basketPlayerSearch(basketApi, chatId, playerName);
                } else
                    send("❌ Devi specificare un nome", chatId, false);
                break;
            case "teams":
                basketTeams(basketApi, chatId);
                break;
            case "games":
                if (args.length >= 2) {
                    try {
                        if (args[1].equalsIgnoreCase("season") && args.length >= 3) {
                            int season = Integer.parseInt(args[2]);
                            basketGamesSeason(basketApi, chatId, season);
                        }
                        else if (args[1].equalsIgnoreCase("team") && args.length >= 4) {
                            int teamId = Integer.parseInt(args[2]);
                            int season = Integer.parseInt(args[3]);
                            basketGamesByTeam(basketApi, chatId, teamId, season);
                        } else {
                            send("❌ Comando partite non valido. Usa: games season <anno> o games team <id> <anno>", chatId, false);
                        }
                    } catch (NumberFormatException e) {
                        send("❌ Parametro numerico non valido", chatId, false);
                    }
                } else
                    send("❌ Devi specificare ulteriori parametri per le partite", chatId, false);
                break;
            case "team":
                if(args.length >= 2){
                    String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    basketSpecificTeam(chatId, name);
                }
                else
                    send("❌ Devi specificare Team e modello!", chatId, false);
                break;
            default:
                send("❌ Comando basket non riconosciuto", chatId, false);
        }
    }

    private void basketPlayers(BallDontLieApi api, long chatId) {
        PlayersResponse resp = api.getPlayers(1);
        if (resp == null || resp.data == null || resp.data.isEmpty()) {
            send("😕 Nessun giocatore trovato", chatId, false);
            return;
        }

        send("👤 Lista giocatori (prima pagina):\n\n",chatId, false);
        for (Player p : resp.data) {
            send(p.toString(), chatId, false);
        }
    }

    private void basketPlayerSearch(BallDontLieApi api, long chatId, String name) {
        if (name == null || name.isBlank()) {
            send("😕 Nome giocatore non valido", chatId, false);
            return;
        }

        PlayersResponse resp = api.searchPlayers(name);
        if (resp == null || resp.data == null || resp.data.isEmpty()) {
            send("😕 Nessun giocatore trovato con il nome: " + name, chatId, false);
            return;
        }

        // Più risultati => messaggio testuale
        if (resp.data.size() > 1) {
            String msg = "🔍 Risultati ricerca giocatore:\n\n";
            for (Player p : resp.data)
                msg = msg.concat(p.toString()).concat("\n");

            send(msg, chatId, false);
            return;
        }

        // Un solo risultato => maggiori dettagli e foto
        Player player = resp.data.getFirst();
        WikiSportService wikiService = new WikiSportService();
        String wikiTitle = wikiService.toWikiCamelCase(player.first_name + " " + player.last_name);

        wikiResponse(wikiService, player.toString(), wikiTitle, chatId);
    }
    private void basketTeams(BallDontLieApi api, long chatId) {
        TeamsResponse resp = api.getTeams();
        WikiSportService wikiService = new WikiSportService();

        if (resp == null || resp.data == null || resp.data.isEmpty()) {
            send("😕 Nessun team trovato", chatId, false);
            return;
        }

        send("🏀 Squadre NBA:\n", chatId, false);

        for (var team : resp.data) {
            if (team.full_name != null && !team.full_name.isEmpty()){
                String teamName = team.full_name;
                String wikiTitle = wikiService.toWikiCamelCase(teamName);

                wikiResponse(wikiService, teamName, wikiTitle, chatId);
            }
        }
    }

    private void basketGamesSeason(BallDontLieApi api, long chatId, int season) {
        GamesResponse resp = api.getGamesBySeason(season);
        if (resp == null || resp.data == null || resp.data.isEmpty()) {
            send("😕 Nessuna partita trovata per la stagione " + season, chatId, false);
            return;
        }

        String msg = "📅 Partite stagione " + season + ":\n\n";
        for (var g : resp.data)
            msg = msg.concat(g.toString()).concat("\n");

        send(msg, chatId, false);
    }

    private void basketGamesByTeam(BallDontLieApi api, long chatId, int teamId, int season) {
        GamesResponse resp = api.getGamesByTeam(teamId, season);
        Team team = resp.data.getFirst().home_team;

        if (resp.data == null || resp.data.isEmpty() || team == null ) {
            send("😕 Nessuna partita trovata per il team " + teamId + " nella stagione " + season, chatId, false);
            return;
        }

        String msg = "📅 Partite " + team.full_name + " (id: " + teamId + ") stagione " + season + ":\n\n";
        for (var g : resp.data)
            msg = msg.concat(g.toString()).concat("\n");

        send(msg, chatId, false);
    }

    private void basketSpecificTeam(long chatId, String name) {
        if (name == null || name.isEmpty()) {
            send("😕 Nome team non valido", chatId, false);
            return;
        }

        WikiSportService wikiService = new WikiSportService();
        // Titolo: "{team}_{modello}"
        String wikiTitle = wikiService.toWikiCamelCase(name);

        wikiResponse(wikiService, name, wikiTitle, chatId);
    }
    //#endregion

    //#region ApiFootball API (SerieA - Champions league - Mondiali)
    private void handleSoccerCommand(String[] args, long chatId) {
        if (args.length == 0) {
            send("❌ Devi specificare una lega", chatId, false);
            return;
        }

        FootballApi api = new FootballApi();
        String leagueName = args[0].toLowerCase();
        Integer leagueId = getLeagueId(leagueName); // mappa nome lega → ID lega

        if (leagueId == null) {
            send("❌ Lega non valida: " + args[0], chatId, false);
            return;
        }

        if (args.length == 1) {
           // Solo lega selezionata, mostra guida comandi
           String msg = """
           ⚽ <b>Comandi disponibili per %s:</b>
           
           👥 <b>teams &lt;anno&gt;</b> – Lista squadre
           👤 <b>players &lt;teamId&gt;</b> – Giocatori di un team
           🔍 <b>player &lt;playerId&gt; &lt;anno&gt;</b> – Info giocatore
           🔍 <b>player &lt;nome&gt;</b> – Info giocatore
           📅 <b>season &lt;anno&gt;</b> – Partite stagione
           📊 <b>standings &lt;anno&gt;</b> – Classifica aggiornata
           🏢 <b>team &lt;nome&gt;</b> – Info su un team
           """.formatted(
               args[0] != null ? args[0] : "N/A"
           );
           send(msg, chatId, true);
           return;
        }

        // Rimuove il primo argomento (lega) e gestisce i comandi
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
        String cmd = commandArgs[0].toLowerCase();

        switch (cmd) {
            case "teams":
                if (commandArgs.length >= 2) {
                    try {
                        int year = Integer.parseInt(commandArgs[1]);
                        sendTeams(api, leagueId, year, chatId);
                    } catch (NumberFormatException e) {
                        send("❌ Anno non valido", chatId, false);
                    }
                } else
                    send("❌ Devi specificare l'anno per la lista squadre", chatId, false);
                break;
            case "players":
                if (commandArgs.length >= 2) {
                    try {
                        int teamId = Integer.parseInt(commandArgs[1]);
                        int year = Integer.parseInt(commandArgs[2]);
                        sendPlayers(api, teamId, year, chatId);
                    } catch (NumberFormatException e) {
                        send("❌ Team ID non valido", chatId, false);
                    }
                } else
                    send("❌ Devi specificare il teamId", chatId, false);
                break;
            case "player":
                if (commandArgs.length >= 2) {
                    try {
                        int playerId = Integer.parseInt(commandArgs[1]);
                        if (commandArgs.length >= 3) {
                            int season = Integer.parseInt(commandArgs[2]);
                            sendPlayer(api, playerId, season, chatId);
                            break;
                        }

                        // Altrimenti i parametri sono il nome!
                        String playerName = String.join(" ", Arrays.copyOfRange(commandArgs, 1, commandArgs.length));
                        soccerSpecificPlayer(chatId, playerName.trim());
                    } catch (NumberFormatException e) {
                        // Non un numero => tutto l'input è il nome!
                        String playerName = String.join(" ", Arrays.copyOfRange(commandArgs, 1, commandArgs.length));
                        soccerSpecificPlayer(chatId, playerName.trim());
                    }
                } else
                    send("❌ Devi specificare almeno il playerId o il nome del giocatore", chatId, false);
                break;
            case "team":
                String teamName = String.join(" ", Arrays.copyOfRange(commandArgs, 1, commandArgs.length));
                soccerSpecificTeam(chatId, teamName.trim());
                break;
            case "next":
            case "last":
            case "season":
            case "standings":
                handleSeasonCommands(api, cmd, commandArgs, leagueId, chatId);
                break;
            default:
                send("❌ Comando non riconosciuto per la lega " + leagueName, chatId, false);
        }
    }

    private void sendTeams(FootballApi api, int leagueId, int year, long chatId){
        Models.ApiFootball.teams.TeamsResponse resp = api.getTeams(leagueId, year);

        if(resp == null || resp.response.isEmpty()) {
            send("😕 Nessuna squadra trovata", chatId, false);
            return;
        }

        for(var t : resp.response)
            sendContentPicture(t.team.toString(), t.team.logo, chatId);
    }

    private void sendPlayers(FootballApi api, int teamId, int year, long chatId) {
        Models.ApiFootball.players.PlayersResponse resp = api.getPlayersByTeamId(teamId, year);

        if(resp == null || resp.response.isEmpty()) {
            send("😕 Nessun giocatore trovato", chatId, false);
            return;
        }

        for(var p : resp.response)
            sendContentPicture(p.player.toString(), p.player.photo, chatId );
    }

    private void sendPlayer(FootballApi api, int playerId, int season, long chatId) {
        Models.ApiFootball.players.PlayersResponse resp = api.getPlayerById(playerId, season);

        if(resp == null || resp.response == null || resp.response.isEmpty()) {
            send("😕 Giocatore non trovato", chatId, false);
            return;
        }

        Models.ApiFootball.players.Player pl = resp.response.getFirst().player;
        sendContentPicture(pl.toString(), pl.photo, chatId);
    }

    private void soccerSpecificPlayer(long chatId, String playerName) {
        if (playerName == null || playerName.isBlank()) {
            send("❌ Nome giocatore non valido", chatId, false);
            return;
        }

        WikiSportService wikiService = new WikiSportService();
        String wikiTitle = wikiService.toWikiCamelCase(playerName);

        wikiResponse(wikiService, playerName, wikiTitle, chatId);
    }

    private void soccerSpecificTeam(long chatId, String teamName) {
        if (teamName == null || teamName.isBlank()) {
            send("❌ Nome Team non valido", chatId, false);
            return;
        }

        WikiSportService wikiService = new WikiSportService();
        String wikiTitle = wikiService.toWikiCamelCase(teamName);

        wikiResponse(wikiService, teamName, wikiTitle, chatId);
    }

    private void handleSeasonCommands(FootballApi api, String cmd, String[] args, int leagueId, long chatId) {
        int season = 2023; // default (per il piano gratuito)

        // Lettura anno se presente
        if (args.length >= 2) {
            try {
                season = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                send("❌ Anno non valido", chatId, false);
                return;
            }
        }

        // Controllo range stagione
        if (season < 2021 || season > 2023) {
            send("❌ Anno non valido\nAnni accettati solo tra 2021 e 2023", chatId, false);
            return;
        }

        switch (cmd) {
            case "season":
                FixturesResponse fixtures = api.getFixturesByLeague(leagueId, season);

                if (fixtures == null || fixtures.response == null || fixtures.response.isEmpty()) {
                    send("😕 Nessuna partita trovata per la stagione " + season, chatId, false);
                    return;
                }

                int nFixtures = 4;
                String message = "";

                for (int i = 0; i < fixtures.response.size(); i++) {
                    message = message.concat(fixtures.response.get(i).toString()).concat("\n\n");

                    // Invia ogni 4 partite o alla fine
                    if ((i + 1) % nFixtures == 0 || i == fixtures.response.size() - 1) {
                        send(message, chatId, true);
                        message = "";
                    }
                }
                break;
            case "standings":
                StandingsResponse standings = api.getStandings(leagueId, season);

                if (standings == null || standings.response == null || standings.response.isEmpty()) {
                    send("😕 Classifica non disponibile", chatId, true);
                    return;
                }

                League lg = standings.response.getFirst().league;
                send(lg.toString(), chatId, true);
            break;
            default:
                send("❌ Comando stagione non riconosciuto", chatId, false);
                break;
        }
    }

    //#endregion

    //#region Training DB
    private void handleTrainingCommand(String[] args, long chatId) {
        if (args.length == 0) {
            send("❌ Devi specificare un comando training", chatId, false);
            return;
        }

        DBManager db = DBManager.getInstance();
        switch (args[0].toLowerCase()) {
            case "new":
                if (args.length < 2) {
                    send("❌ Manca il nome della scheda!", chatId, false);
                    return;
                }
                String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                createTrainingPlan(db, chatId, name);
                break;
            case "list":
                if (args.length == 1)
                    listTrainingPlans(db, chatId);
                else {
                    int planId = safeParseInt(args[1], chatId, "⚠️ ID scheda non valido.");

                    if (args.length == 3 && args[2].equalsIgnoreCase("days"))
                        listTrainingDays(db, chatId, planId);
                    else if (args.length == 3 && args[2].equalsIgnoreCase("exercises"))
                        listExercises(db, chatId, planId);
                    else
                        send("❌ Comando list non valido", chatId, false);
                }
                break;
            case "select":
                if (args.length < 2) {
                    send("❌ Manca l'id della scheda", chatId, false);
                    return;
                }
                selectTrainingPlan(db, chatId, args[1]);
                break;
            case "remove":
                if (args.length < 2) {
                    send("❌ Manca l'id della scheda", chatId, false);
                    return;
                }
                deleteTrainingPlan(db, chatId, args[1]);
                break;
            default:
                send("❌ Comando training non valido", chatId, false);
        }
    }

    private void createTrainingPlan(DBManager db, long chatId, String name) {
        User user = db.getUserByTelegramId(chatId);
        boolean created = db.createTrainingPlan(user.id, name, false);
        if (created)
            send("✅ Scheda \"" + name + "\" creata correttamente!", chatId, false);
        else
            send("❌ Errore durante la creazione della scheda", chatId, false);
    }

    private void listTrainingPlans(DBManager db, long chatId) {
        User user = db.getUserByTelegramId(chatId);
        List<TrainingPlan> plans = db.getTrainingPlans(user.id);

        if (plans.isEmpty()) {
            send("❌ Non hai ancora creato schede di allenamento", chatId, false);
            return;
        }

        String msg = "📋 <b>Le tue schede</b>\n\n";

        for (TrainingPlan plan : plans)
            msg = msg.concat(plan.toString() + "\n");

        send(msg, chatId, true);
    }

    private void selectTrainingPlan(DBManager db, long chatId, String planIdStr) {
        User user = db.getUserByTelegramId(chatId);

        int planId = safeParseInt(planIdStr, chatId, "⚠️ ID scheda non valido.");

        List<TrainingPlan> plans = db.getTrainingPlans(user.id);
        boolean found = false;
        for (TrainingPlan plan : plans) {
            if (plan.id == planId) {
                found = true;
                break;
            }
        }

        if (!found) {
            send("❌ Scheda non trovata.", chatId, false);
            return;
        }

        db.setAllTrainingPlansInactive(user.id);

        if (db.setTrainingPlanActive(planId))
            send("✅ Scheda selezionata con successo!", chatId, false);
        else
            send("❌ Errore nell'attivare la scheda.", chatId, false);
    }

    private void deleteTrainingPlan(DBManager db, long chatId, String planIdStr) {
        User user = db.getUserByTelegramId(chatId);

        int planId = safeParseInt(planIdStr, chatId, "⚠️ ID scheda non valido.");

        boolean removed = db.removeTrainingPlan(user.id, planId);
        if (removed)
            send("🗑️ Scheda rimossa correttamente!", chatId, false);
        else
            send("❌ Errore durante la rimozione della scheda", chatId, false);
    }

    private void listTrainingDays(DBManager db, long chatId, int planId) {
        TrainingPlan plan = db.getTrainingPlanById(planId);
        if (plan == null) {
            send("❌ Scheda non trovata.", chatId, false);
            return;
        }

        List<TrainingDay> days = db.getTrainingDays(planId);
        if (days.isEmpty()) {
            send("⚠️ Nessun giorno di allenamento definito per questa scheda.", chatId, false);
            return;
        }

        String msg = "📅 <b>Giorni della scheda \"" + plan.name + "\"</b>\n\n";
        for (TrainingDay day : days)
            msg = msg.concat("🆔 " + day.id + " - " + dayOfWeekToString(day.dayOfWeek) + " – Focus: " + day.focus + "\n");

        send(msg, chatId, true);
    }

    private void listExercises(DBManager db, long chatId, int planId) {
        TrainingPlan plan = db.getFullTrainingPlan(planId);
        if (plan == null) {
            send("❌ Scheda non trovata.", chatId, false);
            return;
        }

        if(plan.getTrainingDays().isEmpty())
            send("⚠️ Nessun allenamento o esercizio definito.", chatId, false);

        String msg = "📋 <b>Esercizi della scheda</b>\n\n";
        for (TrainingDay day : plan.getTrainingDays())
            msg = msg.concat(day.toString() + "\n");

        send(msg, chatId, true);
    }
    //#endregion

    //#region Training day
    private void handleTrainingDayCommand(String[] args, long chatId) {
        if (args.length == 0) {
            send("❌ Devi specificare un comando trainingDay", chatId, false);
            return;
        }

        DBManager db = DBManager.getInstance();
        switch (args[0].toLowerCase()) {
            case "add":
                if (args.length < 4) {
                    send("⚠️ Usa: /trainingDay add <idScheda> <giorno> <focus>", chatId, false);
                    return;
                }
                int planId;
                int dayOfWeek;
                try {
                    planId = Integer.parseInt(args[1]);
                    dayOfWeek = Integer.parseInt(args[2]); // 1-7
                    if(dayOfWeek < 1 || dayOfWeek > 7){
                        send("❌ Giorno non valido: accetto solo da 1 a 7", chatId, false);
                        return;
                    }
                } catch (NumberFormatException e) {
                    send("❌ Formato dati non valido", chatId, false);
                    return;
                }
                String focus = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                addTrainingDay(db, chatId, planId, dayOfWeek, focus);
                break;
            case "remove":
                if (args.length < 2) {
                    send("⚠️ Usa: /trainingDay remove <idGiorno>", chatId, false);
                    return;
                }
                removeTrainingDay(db, chatId, args[1]);
                break;
            case "list":
                if (args.length < 2) {
                    send("⚠️ Usa: /trainingDay list <idScheda>", chatId, false);
                    return;
                }
                int id = safeParseInt(args[1], chatId, "⚠️ ID allenamento non valido.");
                listTrainingDays(db, chatId, id);
                break;
            default:
                send("❌ Comando trainingDay non valido", chatId, false);
        }
    }

    private void addTrainingDay(DBManager db, long chatId, int planId, int dayOfWeek, String focus) {
        boolean created = db.addTrainingDay(planId, dayOfWeek, focus);
        if (created)
            send("✅ Giorno di allenamento aggiunto con successo!", chatId, false);
        else
            send("❌ Errore durante l'aggiunta del giorno", chatId, false);
    }

    private void removeTrainingDay(DBManager db, long chatId, String dayIdStr) {
        int dayId = safeParseInt(dayIdStr, chatId, "⚠️ ID allenamento non valido.");

        boolean removed = db.removeTrainingDay(dayId);
        if (removed)
            send("🗑️ Giorno di allenamento rimosso", chatId, false);
        else
            send("❌ Errore nella rimozione del giorno", chatId, false);
    }
    //endregion

    //#region Exercise
    private void handleExerciseCommand(String[] args, long chatId) {
        DBManager db = DBManager.getInstance();

        switch (args[0].toLowerCase()) {
            case "add":
                if (args.length < 6) {
                    send("❌ Uso corretto:\n/exercise add <id giorno> <nome> <sets> <reps> <peso> [note]", chatId, false);
                    return;
                }
                addExercise(db, chatId, args);
                break;
            case "remove":
                if (args.length < 2) {
                    send("❌ Manca l'id dell'esercizio", chatId, false);
                    return;
                }
                removeExercise(db, chatId, args[1]);
                break;
            case "list":
                if (args.length < 2) {
                    send("❌ Manca l'id del giorno", chatId, false);
                    return;
                }
                int planId = safeParseInt(args[1], chatId, "⚠️ ID giorno non valido.");
                listDayExercises(db, chatId, planId);
                break;
            default:
                send("❌ Comando exercise non valido", chatId, false);
        }
    }

    private void addExercise(DBManager db, long chatId, String[] args) {
        String combined = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String[] parts = combined.split("#");

        if (parts.length < 2) {
            send("⚠️ Formato comando non valido. Usa:\n/exercise add <id giorno> <nome esercizio> # <sets> <reps> <peso> # [note opzionali]", chatId, false);
            return;
        }

        // Prima parte
        String[] firstPart = parts[0].trim().split(" ", 2); // separa solo il primo spazio
        if (firstPart.length < 2) {
            send("⚠️ Inserisci ID giorno e nome esercizio.", chatId, false);
            return;
        }
        int dayId = safeParseInt(firstPart[0], chatId, "❌ ID non valido!");
        String name = firstPart[1].trim();

        // Seconda parte: sets, reps, peso
        String[] secondPart = parts[1].trim().split(" ");
        if (secondPart.length < 3) {
            send("⚠️ Inserisci sets, reps e peso.", chatId, false);
            return;
        }
        int sets = safeParseInt(secondPart[0], chatId, "❌ Numero set non valido!");
        int reps = safeParseInt(secondPart[1], chatId, "❌ Numero reps non valido!");
        double weight = 0.0f;
        if(sets == -1 || reps == -1)
            return;
        try{
            weight = Double.parseDouble(secondPart[2]);
        } catch (NumberFormatException e) {
            send("❌ Peso non valido!", chatId, false);
        }

        // Terza parte: note (opzionale)
        String notes = parts.length >= 3 ? parts[2].trim() : null;

        boolean created = db.addUserExercise(dayId, name, sets, reps, weight, notes);
        if (created)
            send("✅ Esercizio aggiunto correttamente!", chatId, false);
        else
            send("❌ Errore durante l'aggiunta dell'esercizio", chatId, false);
    }

    private void removeExercise(DBManager db, long chatId, String exerciseIdStr) {
        int exerciseId = safeParseInt(exerciseIdStr, chatId, "⚠️ ID esercizio non valido.");

        if (db.removeUserExercise(exerciseId))
            send("🗑️ Esercizio rimosso.", chatId, false);
        else
            send("❌ Errore durante la rimozione dell'esercizio.", chatId, false);
    }

    private void listDayExercises(DBManager db, long chatId, int dayId) {
        List<UserExercise> exercises = db.getUserExercises(dayId);

        if (exercises.isEmpty()) {
            send("❌ Non ci sono esercizi registrati per questo giorno.", chatId, false);
            return;
        }

        String msg = "📋 <b>Esercizi del giorno</b>\n\n";
        for (UserExercise ex : exercises)
            msg = msg.concat(ex.toString()).concat("\n");

        send(msg, chatId, true);
    }
    //#endregion

    //#region workout
    private void handleWorkoutCommand(String[] args, long chatId) {
        DBManager db = DBManager.getInstance();

        if (args.length < 1) {
            send("❌ Comando workout non valido.", chatId, false);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                if (args.length < 2) {
                    send("❌ Manca l'id del giorno.", chatId, false);
                    return;
                }
                startWorkoutSession(db, chatId, args[1]);
                break;
            case "complete":
                if (args.length < 2) {
                    send("❌ Manca l'id della sessione.", chatId, false);
                    return;
                }
                completeWorkoutSession(db, chatId, args[1]);
                break;
            case "list":
                if (args.length < 2) {
                    send("❌ Manca l'id del giorno.", chatId, false);
                    return;
                }
                listWorkoutSession(db, chatId, args[1]);
                break;
            default:
                send("❌ Comando workout non valido.", chatId, false);
                break;
        }
    }

    private void startWorkoutSession(DBManager db, long chatId, String dayIdStr){
        int dayId = safeParseInt(dayIdStr, chatId, "⚠️ ID giorno non valido.");
        if(dayId == -1)
            return;

        //check della data
        int today = LocalDate.now().getDayOfWeek().getValue();
        int dayOfWeek = db.getTrainingDayOfWeek(dayId);

        if(dayOfWeek == -1){
            send("⚠️ ID giorno non valido.", chatId, false);
            return;
        }
        else if(today < dayOfWeek){
            send("""
                    ⛔ <b>Questo allenamento è previsto per un giorno futuro!</b> ⛔
                    Puoi iniziare solo allenamenti di oggi o precedenti.
                    """, chatId, true);
            return;
        }

        if (db.addWorkoutSession(dayId))
            send("🏋️ Sessione di allenamento avviata!", chatId, false);
        else
            send("❌ Errore avvio sessione.", chatId, false);
    }

    private void completeWorkoutSession(DBManager db, long chatId, String sessionIdStr) {
        int sessionId = safeParseInt(sessionIdStr, chatId, "⚠️ ID sessione non valido.");
        if(sessionId == -1)
            return;

        if (db.completeWorkoutSession(sessionId))
            send("🏋️ Sessione completata!", chatId, false);
        else
            send("❌ Sessione non trovata.", chatId, false);
    }

    private void listWorkoutSession(DBManager db, long chatId, String dayIdStr) {
        int id = safeParseInt(dayIdStr, chatId, "⚠️ ID giorno non valido.");
        if(id == -1)
            return;

        var sessions = db.getWorkoutSessions(id);

        if (sessions.isEmpty()) {
            send("🧐 Nessuna sessione registrata.", chatId, false);
            return;
        }

        String msg = "📊 <b>Sessioni di allenamento</b>\n\n";
        for (WorkoutSession session : sessions)
            msg = msg.concat(session.toString()).concat("\n\n");

        send(msg, chatId, true);
    }
    //#endregion

    //#region Helper
    // Metodi extra per evitare ripetizione codice
    private void send(String msg, long chatId, boolean html) {
        SendMessage.SendMessageBuilder builder = SendMessage.builder()
                .chatId(chatId)
                .text(msg);

        if (html)
            builder.parseMode("HTML");

        try {
            telegramClient.execute(builder.build());
        } catch (TelegramApiException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private String stringNormalization(String in){
        String betterQuery = in.toLowerCase();

        if (betterQuery.equals("calcio"))
            return "soccer";

        if (betterQuery.equals("basket"))
            return "basketball";

        return betterQuery;
    }

    private boolean checkMediaErrorMessage(String in,  long chatId) {
        if (!Arrays.asList(sportAccepted).contains(in)) {
            send("""
            😤 Sport non valido!
            
            Sport disponibili:
            F1, Motorsport, WEC, Calcio, Basketball (NBA)
            F1, Motorsport, WEC, Calcio, Basketball (NBA)
            """, chatId, false);
            return true;
        }
        return false;
    }

    private InlineKeyboardMarkup buildLinkButton(String text, String url) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text(text)
                                        .url(url)
                                        .build()
                        )
                )
                .build();
    }

    private void sendContentPicture(String caption, String url, long chatId) {
        // Check di validità => metodo custom
        if (!isValidTelegramImage(url)) {
            if (caption != null && !caption.isEmpty())
                send(caption, chatId, true);
            return;
        }

        if (!url.isEmpty()) {
            try {
                SendPhoto.SendPhotoBuilder builder = SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(url));

                if (caption != null && !caption.isEmpty())
                    builder.caption(caption);

                telegramClient.execute(builder.build());
            } catch (TelegramApiException e) {
                System.err.println("Errore invio foto: " + e.getMessage());
            }
        }
    }

    private Integer getLeagueId(String name) {
        return leaguesMap.get(name.toLowerCase());
    }

    private void wikiResponse(WikiSportService wikiService, String name, String wikiTitle, long chatId){
        WikipediaSummaryResponse resp = wikiService.getFromText(wikiTitle);

        // Fallback
        String text = name;
        String imgUrl = null;
        String wikiLink = "https://it.wikipedia.org/wiki/" + wikiTitle;

        if (resp != null) {
            if (resp.extract != null && !resp.extract.isEmpty())
                text = "<b>" + name + "</b>\n" + resp.extract;

            if (resp.thumbnail != null && resp.thumbnail.source != null)
                imgUrl = resp.thumbnail.source;

            // Aggiorna il link per sicurezza
            if (resp.content_urls != null && resp.content_urls.desktop != null && resp.content_urls.desktop.page != null)
                wikiLink = resp.content_urls.desktop.page;
        }else{
            send("❌ Assicurati di aver usato il nome corretto!", chatId, false);
            return;
        }

        sendContentPicture(null, imgUrl, chatId);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(buildLinkButton("🔗 Wikipedia", wikiLink))
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Errore invio messaggio WEC team: " + e.getMessage());
        }
    }

    private boolean isValidTelegramImage(String url) {
        if (url == null)
            return false;

        String lower = url.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    }

    // Utilizzo di enhanced switch (consiglio dell'editor)
    public static String dayOfWeekToString(int day) {
        return switch (day) {
            case 1 -> "Lunedì";
            case 2 -> "Martedì";
            case 3 -> "Mercoledì";
            case 4 -> "Giovedì";
            case 5 -> "Venerdì";
            case 6 -> "Sabato";
            case 7 -> "Domenica";
            default -> "Giorno non valido";
        };
    }

    private int safeParseInt(String value, long chatId, String errorMessage) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            send(errorMessage, chatId, false);
            return -1;
        }
    }
    //#endregion
}