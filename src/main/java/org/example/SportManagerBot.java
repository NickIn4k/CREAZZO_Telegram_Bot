package org.example;

import Models.Ergast.MRData;
import Services.ErgastApi;
import Services.PexelsApi;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

public class SportManagerBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    // Stato conversazione => alcuni comandi hanno bisogno di un secondo input
    private final Map<Long, BotState> userStates = new HashMap<>();

    // Sport supportati
    private static final String[] sportAccepted = {
            "f1", "soccer",
            "basketball", "wec", "motorsport"
    };

    // enum dei possibilit stati
    private enum BotState {
        none,
        waiting_photo,
        waiting_video,
        waiting_f1
    }

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

        BotState state = userStates.getOrDefault(chatId, BotState.none);

        // Bot è in attesa del media (foto/video)
        if (state == BotState.waiting_photo) {
            userStates.put(chatId, BotState.none);
            sendPhoto(messageText, chatId);
            return;
        }

        if (state == BotState.waiting_video) {
            userStates.put(chatId, BotState.none);
            sendVideo(messageText, chatId);
            return;
        }

        if (state == BotState.waiting_f1) {
            userStates.put(chatId, BotState.none);
            // invia l’array degli argomenti come se fosse /f1 <args>
            String[] f1Args = messageText.split(" ");
            handleF1Command(f1Args, chatId);
            return;
        }

        String[] args = messageText.split(" ");

        String mediaMsg = """
                    📸 <b>Che sport vuoi?</b>
                    
                    Scrivi uno tra:
                        • F1 🏎️
                        • Motorsport 🚗
                        • WEC 🏎
                        • Calcio ⚽
                        • Basketball 🏀
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
                    
                    🏢  <b>teams</b> – Lista dei team attuali
                
                    ℹ️  Maggiori info con il comando <b>/help</b>
                    """;
                    send(msg, chatId, true);
                } else {
                    // Elimina il primo elemento da args => non tiene più conto di "/f1"
                    args = Arrays.copyOfRange(args, 1, args.length);
                    handleF1Command(args, chatId);
                }
                break;
            default:
                send("❓ Comando non riconosciuto. Usa /help", chatId, false);
        }
    }

    // Metodi divisi per comando
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

    private void helpMessage(long chatId) {
        String msg = """
            📖  <b>Comandi disponibili</b>
            
            <b>/start</b> – Avvia il bot
            <b>/help</b> – Mostra questo messaggio
            
            📸  <b>Foto e Video</b>
            
            <b>/photo &lt;sport&gt;</b> – Ricevi un’immagine sportiva
            <b>/video &lt;sport&gt;</b> – Ricevi un video sportivo
            
            🏎️  <b>Formula 1</b>
            
            <b>/f1 next</b> – Prossima gara
            <b>/f1 last</b> – Ultima gara
            <b>/f1 last results</b> – Classifica ultima gara
            <b>/f1 drivers</b> – WDC aggiornata
            <b>/f1 constructors</b> – WCC aggiornata
            <b>/f1 calendar &lt;anno&gt;</b> – Calendario stagione
            <b>/f1 driver &lt;nome&gt;</b> – Info pilota
            <b>/f1 teams</b> – Lista dei team attuali
            
            🏋️  <b>Personal Trainer</b>
            
            ⚠️  Sport supportati: F1, Motorsport, WEC, Calcio, Basketball
            """;
        send(msg, chatId, true);
    }

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

            default:
                send("❌ Comando F1 non riconosciuto", chatId, false);
        }
    }

    private void f1Next(ErgastApi ergastApi, long chatId) {
        MRData nextRaceData = ergastApi.getNextRace();
        if (nextRaceData != null && nextRaceData.RaceTable != null && !nextRaceData.RaceTable.Races.isEmpty())
            send(nextRaceData.RaceTable.Races.getFirst().toString(), chatId, true);
        else
            send("😕 Nessuna prossima gara trovata", chatId, false);
    }

    private void f1Last(ErgastApi ergastApi, long chatId) {
        MRData lastRaceData = ergastApi.getLastRace();
        if (lastRaceData != null && lastRaceData.RaceTable != null && !lastRaceData.RaceTable.Races.isEmpty())
            send(lastRaceData.RaceTable.Races.getFirst().toString(), chatId, true);
        else
            send("😕 Nessuna ultima gara trovata", chatId, false);
    }

    private void f1LastResults(ErgastApi ergastApi, long chatId){
        MRData lastResults = ergastApi.getLastRaceResults();
        if (lastResults != null && lastResults.RaceTable != null && !lastResults.RaceTable.Races.isEmpty()) {
            var race = lastResults.RaceTable.Races.getFirst();

            String output = String.format("🏁 Risultati Ultima Gara - %s, Round %s\n\n", race.raceName, lastResults.RaceTable.round);

            for (var r : race.Results)
                output += r.toString() + "\n";

            send(output, chatId, true);
        }
        else
            send("😕 Nessun risultato ultima gara", chatId, false);
    }

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
        MRData driver = ergastApi.getDriver(id);
        send(driver.DriverTable.Drivers.getFirst().toString(), chatId, true);
    }

    private void f1Teams(ErgastApi ergastApi, long chatId) {
        MRData teams = ergastApi.getConstructors();
        String msg = "<b>Lista di tutti i Team F1</b> \n\n";

        if(teams != null && teams.ConstructorTable != null) {
            var constructors = teams.ConstructorTable.Constructors;

            for(var c :  constructors)
                msg +=  c.toString() + "\n";
        }

        send(msg, chatId, true);
    }

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
            F1, Motorsport, WEC, Calcio, Basketball
            """, chatId, false);
            return true;
        }
        return false;
    }
}