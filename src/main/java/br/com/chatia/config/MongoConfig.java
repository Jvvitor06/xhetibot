package br.com.chatia.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConfig {

    private static final String CHAVE_URI = "MONGODB_URI";
    private static final String NOME_BANCO = "chat_ia";

    private static MongoClient client;
    private static MongoDatabase database;

    private MongoConfig() {
    }

    public static synchronized MongoDatabase getDatabase() {
        if (database == null) {
            String uri = AppConfig.get(CHAVE_URI);

            if (uri == null) {
                throw new IllegalStateException(
                        CHAVE_URI + " não encontrada. Defina a variável de "
                                + "ambiente ou crie o arquivo "
                                + AppConfig.getCaminhoArquivo()
                );
            }

            client = MongoClients.create(uri);
            database = client.getDatabase(NOME_BANCO);
        }
        return database;
    }

    public static synchronized void fechar() {
        if (client != null) {
            client.close();
            client = null;
            database = null;
        }
    }
}
