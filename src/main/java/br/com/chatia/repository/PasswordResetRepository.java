package br.com.chatia.repository;

import br.com.chatia.config.MongoConfig;
import br.com.chatia.model.TokenRecuperacao;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.time.Instant;
import java.util.Date;

/** Coleção "password_reset_tokens". */
public class PasswordResetRepository {

    private MongoCollection<Document> collection() {
        return MongoConfig.getDatabase().getCollection("password_reset_tokens");
    }

    /** Cada usuário tem no máximo UM código ativo: o novo substitui o antigo. */
    public void substituir(String usuarioId, String tokenHash, Instant expiraEm) {
        excluirDoUsuario(usuarioId);

        collection().insertOne(new Document()
                .append("usuarioId", usuarioId)
                .append("tokenHash", tokenHash)
                .append("expiraEm", Date.from(expiraEm))
                .append("tentativas", 0)
                .append("criadoEm", new Date()));
    }

    public TokenRecuperacao buscarPorUsuario(String usuarioId) {
        Document doc = collection().find(Filters.eq("usuarioId", usuarioId)).first();
        if (doc == null) {
            return null;
        }

        TokenRecuperacao token = new TokenRecuperacao();
        token.setUsuarioId(doc.getString("usuarioId"));
        token.setTokenHash(doc.getString("tokenHash"));
        token.setExpiraEm(doc.getDate("expiraEm").toInstant());
        token.setTentativas(doc.getInteger("tentativas", 0));
        return token;
    }

    public void incrementarTentativas(String usuarioId) {
        collection().updateOne(
                Filters.eq("usuarioId", usuarioId),
                Updates.inc("tentativas", 1)
        );
    }

    public void excluirDoUsuario(String usuarioId) {
        collection().deleteMany(Filters.eq("usuarioId", usuarioId));
    }
}