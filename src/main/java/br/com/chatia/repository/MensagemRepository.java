package br.com.chatia.repository;

import br.com.chatia.config.MongoConfig;
import br.com.chatia.model.AutorMensagem;
import br.com.chatia.model.Mensagem;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MensagemRepository {

    private MongoCollection<Document> collection() {
        return MongoConfig.getDatabase().getCollection("mensagens");
    }

    public void salvar(Mensagem mensagem) {
        Document doc = new Document()
                .append("usuarioId", mensagem.getUsuarioId())
                .append("conversaId", mensagem.getConversaId())
                .append("autor", mensagem.getAutor().name())
                .append("conteudo", mensagem.getConteudo())
                .append("criadaEm", Date.from(mensagem.getCriadaEm()));

        collection().insertOne(doc);
        mensagem.setId(doc.getObjectId("_id").toHexString());
    }

    public List<Mensagem> listarPorConversa(String conversaId, String usuarioId) {
        List<Mensagem> lista = new ArrayList<>();

        for (Document doc : collection()
                .find(filtro(conversaId, usuarioId))
                .sort(Sorts.ascending("criadaEm", "_id"))) {
            lista.add(paraMensagem(doc));
        }
        return lista;
    }

    public long excluirPorConversa(String conversaId, String usuarioId) {
        return collection()
                .deleteMany(filtro(conversaId, usuarioId))
                .getDeletedCount();
    }

    private Bson filtro(String conversaId, String usuarioId) {
        return Filters.and(
                Filters.eq("conversaId", conversaId),
                Filters.eq("usuarioId", usuarioId)
        );
    }

    private Mensagem paraMensagem(Document doc) {
        Mensagem m = new Mensagem();
        m.setId(doc.getObjectId("_id").toHexString());
        m.setUsuarioId(doc.getString("usuarioId"));
        m.setConversaId(doc.getString("conversaId"));
        m.setAutor(AutorMensagem.valueOf(doc.getString("autor")));
        m.setConteudo(doc.getString("conteudo"));
        m.setCriadaEm(doc.getDate("criadaEm").toInstant());
        return m;
    }
}