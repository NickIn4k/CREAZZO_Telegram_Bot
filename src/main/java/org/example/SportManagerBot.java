package org.example;

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
        waiting_video
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
        📖 <b>Comandi disponibili</b>
        
        <b>/start</b> – Avvia il bot
        <b>/help</b> – Mostra questo messaggio
        
        📸 <b>/photo &lt;sport&gt;</b>
        Ricevi un’immagine sportiva
        
        📸 <b>/video &lt;sport&gt;</b>
        Ricevi un video sportivo
        
        🏋️ <b>/training</b>
        Gestisci allenamenti
        
        ⚠️ Sport supportati:
        F1, Motorsport, WEC, Calcio, Basketball
        """;
        send(msg, chatId, true);
    }

    private void sendPhoto(String query, long chatId) {
        String betterQuery = stringNormalization(query);

        if(!checkMediaErrorMessage(betterQuery, chatId))
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

        if(!checkMediaErrorMessage(betterQuery, chatId))
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
            return false;
        }
        return true;
    }
}