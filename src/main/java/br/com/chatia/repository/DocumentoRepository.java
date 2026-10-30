package br.com.chatia.repository;

import br.com.chatia.config.MongoConfig;
import br.com.chatia.model.Documento;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/** Coleção "documents": a base de conhecimento usada pelo RAG. */
public class DocumentoRepository {

    private MongoCollection<Document> collection() {
        return MongoConfig.getDatabase().getCollection("documents");
    }

    public void salvar(Documento documento) {
        // Nomes dos campos seguem o enunciado (seção 22)
        Document doc = new Document()
                .append("title", documento.getTitulo())
                .append("content", documento.getConteudo())
                .append("keywords", documento.getPalavrasChave())
                .append("createdAt", Date.from(documento.getCriadoEm()))
                .append("updatedAt", Date.from(documento.getAtualizadoEm()));

        collection().insertOne(doc);
        documento.setId(doc.getObjectId("_id").toHexString());
    }

    public boolean existeComTitulo(String titulo) {
        return collection().countDocuments(Filters.eq("title", titulo)) > 0;
    }

    /** Documentos que tenham PELO MENOS UMA das palavras no campo keywords. */
    public List<Documento> buscarPorPalavrasChave(Collection<String> palavras) {
        List<Documento> lista = new ArrayList<>();
        if (palavras.isEmpty()) {
            return lista;
        }
        for (Document doc : collection().find(Filters.in("keywords", palavras))) {
            lista.add(paraDocumento(doc));
        }
        return lista;
    }

    private Documento paraDocumento(Document doc) {
        Documento d = new Documento();
        d.setId(doc.getObjectId("_id").toHexString());
        d.setTitulo(doc.getString("title"));
        d.setConteudo(doc.getString("content"));
        d.setPalavrasChave(doc.getList("keywords", String.class));
        d.setCriadoEm(doc.getDate("createdAt").toInstant());
        d.setAtualizadoEm(doc.getDate("updatedAt").toInstant());
        return d;
    }
}