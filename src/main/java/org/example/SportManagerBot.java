package org.example;

import Services.PexelsApi;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;

public class SportManagerBot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;

    public SportManagerBot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            String[] args = message_text.split(" ");

            switch (args[0]) {
                case "/start":
                    startMessage(chat_id);
                    break;
                case "/help":
                    helpMessage(chat_id);
                    break;
                case "/photo":
                    if(args.length == 2)
                        sendPhoto(args[1], chat_id);
                    else{
                        String msg = "Il comando è vuoto 😤";
                        send(msg, chat_id, false);
                        return;
                    }
                    break;
            }
        }
    }

    private void startMessage(long chat_id){
        String msg = """
        👋 Benvenuto in <b>SportManagerBot</b>!

        Con questo bot puoi:
        🏎️ consultare <b>informazioni sportive</b>
        📸 ricevere <b>immagini a tema sport</b>
        🏋️ organizzare <b>i tuoi allenamenti</b> in palestra
        🎮 divertirti con <b>contenuti extra</b>

        Usa il comando <b>/help</b> per scoprire tutte le funzionalità disponibili e iniziare subito!
        """;

        send(msg, chat_id, true);
    }

    private void helpMessage(long chat_id){
        String msg = """
        📖 <b>Comandi disponibili</b> 📖

        <b>/start</b> – Avvia il bot
        <b>/help</b> – Mostra l’elenco dei comandi disponibili

        📸 <b>Immagini</b> 📸
        <b>/photo &lt;sport&gt;</b> – Ricevi un’immagine sullo sport scelto
        Esempio: /photo calcio, /photo f1, /photo basketball

        🏋️ <b>Allenamenti</b> 🏋️
        <b>/training</b> – Gestisci o visualizza i tuoi allenamenti in palestra

        🎮 <b>Extra</b> 🎮
        <b>/meme</b> – Meme sportivo casuale
        <b>/game</b> – Mini-gioco a tema sportivo

        ⚠️ Sport supportati: F1, Motorsport, Calcio, Basket, Tennis (solo per foto)

        Scrivi un comando per iniziare!
        """;

        send(msg, chat_id, true);
    }

    private void sendPhoto(String query, long chat_id){
        String[] sportsAccepted = {"f1", "formula 1", "calcio", "soccer", "basketball", "WEC", "motorsport", "Tennis"};

        if(!Arrays.asList(sportsAccepted).contains(query)){
            String msg = "Hey! Non hai inserito uno sport accettato 😕";
            send(msg, chat_id, false);
            return;
        }

        PexelsApi pexelsApi = new PexelsApi();
        String url = pexelsApi.getPhotoUrl(query);

        if(url == null || url.isEmpty()){
            String msg = "Nessuna immagine trovata 😒";
            send(msg, chat_id, false);
            return;
        }

        // Manda l'immagine
        try {
            telegramClient.execute(buildPhotoMessage(url, query, chat_id));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private SendMessage buildMessage(String msg, long chat_id, boolean HTML){
        SendMessage.SendMessageBuilder builder = SendMessage
                .builder()
                .chatId(chat_id)
                .text(msg);

        if(HTML)
            builder.parseMode("HTML");

        return builder.build();
    }


    private SendPhoto buildPhotoMessage(String url, String query, long chat_id){
        return SendPhoto
                .builder()
                .chatId(chat_id)
                .photo(new InputFile(url))
                .caption("Ecco un'immagine su: " + query)
                .build();
    }

    private void send(String msg, long chat_id, boolean HTML){
        try {
            telegramClient.execute(buildMessage(msg, chat_id, HTML));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}


