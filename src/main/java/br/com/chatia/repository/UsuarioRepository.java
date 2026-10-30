package br.com.chatia.repository;

import br.com.chatia.config.MongoConfig;
import br.com.chatia.model.Usuario;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import com.mongodb.client.model.Updates;
import org.bson.types.ObjectId;

public class UsuarioRepository {

    // A coleção só é obtida quando for usada, não na construção.
    // Assim a aplicação abre mesmo se o banco ainda não estiver configurado.
    private MongoCollection<Document> collection() {
        return MongoConfig
                .getDatabase()
                .getCollection("usuarios");
    }

    public void salvar(Usuario usuario) {

        Document document = new Document()
                .append("nome", usuario.getNome())
                .append("email", usuario.getEmail())
                .append("senhaHash", usuario.getSenhaHash());

        collection().insertOne(document);

        usuario.setId(
                document.getObjectId("_id").toHexString()
        );
    }

    public Usuario buscarPorEmail(String email) {

        Document document = collection().find(
                Filters.eq("email", email)
        ).first();

        if (document == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setId(document.getObjectId("_id").toHexString());
        usuario.setNome(document.getString("nome"));
        usuario.setEmail(document.getString("email"));
        usuario.setSenhaHash(document.getString("senhaHash"));

        return usuario;
    }
    public boolean atualizarSenha(String usuarioId, String novoHash) {
        if (usuarioId == null || !ObjectId.isValid(usuarioId)) {
            return false;
        }
        return collection().updateOne(
                Filters.eq("_id", new ObjectId(usuarioId)),
                Updates.set("senhaHash", novoHash)
        ).getMatchedCount() > 0;
    }
}
