package br.com.chatia.rag;

import br.com.chatia.model.Documento;

import java.util.List;
import java.util.Set;

/** O que a busca encontrou: as palavras usadas e os melhores documentos. */
public record ResultadoBusca(Set<String> palavras, List<Documento> documentos) {

    public static ResultadoBusca vazio() {
        return new ResultadoBusca(Set.of(), List.of());
    }

    public boolean encontrou() {
        return !documentos.isEmpty();
    }
}