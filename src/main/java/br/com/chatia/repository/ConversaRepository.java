package br.com.chatia.repository;

import br.com.chatia.config.MongoConfig;
import br.com.chatia.model.Conversa;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConversaRepository {

    private MongoCollection<Document> collection() {
        return MongoConfig.getDatabase().getCollection("conversas");
    }

    public void salvar(Conversa conversa) {
        Document doc = new Document()
                .append("usuarioId", conversa.getUsuarioId())
                .append("titulo", conversa.getTitulo())
                .append("criadaEm", Date.from(conversa.getCriadaEm()))
                .append("atualizadaEm", Date.from(conversa.getAtualizadaEm()));

        collection().insertOne(doc);
        conversa.setId(doc.getObjectId("_id").toHexString());
    }

    public List<Conversa> listarPorUsuario(String usuarioId) {
        List<Conversa> lista = new ArrayList<>();

        for (Document doc : collection()
                .find(Filters.eq("usuarioId", usuarioId))
                .sort(Sorts.descending("atualizadaEm"))) {
            lista.add(paraConversa(doc));
        }
        return lista;
    }

    public Conversa buscarPorIdEUsuario(String conversaId, String usuarioId) {
        Bson filtro = filtroDono(conversaId, usuarioId);
        if (filtro == null) {
            return null;
        }
        Document doc = collection().find(filtro).first();
        return doc == null ? null : paraConversa(doc);
    }

    public boolean renomear(String conversaId, String usuarioId, String novoTitulo) {
        Bson filtro = filtroDono(conversaId, usuarioId);
        if (filtro == null) {
            return false;
        }
        return collection().updateOne(filtro, Updates.combine(
                Updates.set("titulo", novoTitulo),
                Updates.set("atualizadaEm", new Date())
        )).getMatchedCount() > 0;
    }

    public void registrarAtividade(String conversaId, String usuarioId) {
        Bson filtro = filtroDono(conversaId, usuarioId);
        if (filtro != null) {
            collection().updateOne(filtro, Updates.set("atualizadaEm", new Date()));
        }
    }

    public boolean excluir(String conversaId, String usuarioId) {
        Bson filtro = filtroDono(conversaId, usuarioId);
        if (filtro == null) {
            return false;
        }
        return collection().deleteOne(filtro).getDeletedCount() > 0;
    }

    /** O filtro SEMPRE exige id da conversa E dono. Aqui mora o isolamento. */
    private Bson filtroDono(String conversaId, String usuarioId) {
        if (conversaId == null || usuarioId == null
                || !ObjectId.isValid(conversaId)) {
            return null;
        }
        return Filters.and(
                Filters.eq("_id", new ObjectId(conversaId)),
                Filters.eq("usuarioId", usuarioId)
        );
    }

    private Conversa paraConversa(Document doc) {
        Conversa c = new Conversa();
        c.setId(doc.getObjectId("_id").toHexString());
        c.setUsuarioId(doc.getString("usuarioId"));
        c.setTitulo(doc.getString("titulo"));
        c.setCriadaEm(doc.getDate("criadaEm").toInstant());
        c.setAtualizadaEm(doc.getDate("atualizadaEm").toInstant());
        return c;
    }
}