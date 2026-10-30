package br.com.chatia.service;

import br.com.chatia.model.AutorMensagem;
import br.com.chatia.model.Mensagem;
import br.com.chatia.repository.ConversaRepository;
import br.com.chatia.repository.MensagemRepository;

import java.time.Instant;
import java.util.List;

public class MensagemService {

    private static final int CONTEUDO_MAXIMO = 8000;
    private static final String NAO_ENCONTRADA = "Conversa não encontrada.";

    private final ConversaRepository conversaRepository = new ConversaRepository();
    private final MensagemRepository mensagemRepository = new MensagemRepository();

    /** Valida o texto digitado pelo usuário (vazio ou grande demais). */
    public void validarConteudo(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            throw new IllegalArgumentException("A mensagem não pode estar vazia.");
        }
        if (conteudo.length() > CONTEUDO_MAXIMO) {
            throw new IllegalArgumentException(
                    "A mensagem pode ter no máximo " + CONTEUDO_MAXIMO + " caracteres."
            );
        }
    }

    public Mensagem salvar(String usuarioId, String conversaId,
                           AutorMensagem autor, String conteudo) {

        if (autor == null) {
            throw new IllegalArgumentException("Autor da mensagem não informado.");
        }

        if (autor == AutorMensagem.USUARIO) {
            validarConteudo(conteudo);
        } else if (conteudo == null || conteudo.isBlank()) {
            // O limite de tamanho vale só para o usuário: respostas da IA podem ser longas.
            throw new IllegalArgumentException("A resposta da IA veio vazia.");
        }

        exigirDono(usuarioId, conversaId);

        Mensagem mensagem = new Mensagem();
        mensagem.setUsuarioId(usuarioId);
        mensagem.setConversaId(conversaId);
        mensagem.setAutor(autor);
        mensagem.setConteudo(conteudo.trim());
        mensagem.setCriadaEm(Instant.now());

        mensagemRepository.salvar(mensagem);
        conversaRepository.registrarAtividade(conversaId, usuarioId);
        return mensagem;
    }

    public List<Mensagem> listar(String usuarioId, String conversaId) {
        exigirDono(usuarioId, conversaId);
        return mensagemRepository.listarPorConversa(conversaId, usuarioId);
    }

    /** A conversa precisa existir E pertencer ao usuário. */
    private void exigirDono(String usuarioId, String conversaId) {
        if (usuarioId == null || usuarioId.isBlank()
                || conversaRepository.buscarPorIdEUsuario(conversaId, usuarioId) == null) {
            throw new IllegalArgumentException(NAO_ENCONTRADA);
        }
    }
}