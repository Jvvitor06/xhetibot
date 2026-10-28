package br.com.chatia.service;

import br.com.chatia.model.Conversa;
import br.com.chatia.repository.ConversaRepository;
import br.com.chatia.repository.MensagemRepository;

import java.time.Instant;
import java.util.List;

public class ConversaService {

    private static final int TITULO_MAXIMO = 60;
    public static final String TITULO_PADRAO = "Nova conversa";
    private static final String NAO_ENCONTRADA = "Conversa não encontrada.";

    private final ConversaRepository conversaRepository = new ConversaRepository();
    private final MensagemRepository mensagemRepository = new MensagemRepository();

    public Conversa criar(String usuarioId, String titulo) {
        exigirUsuario(usuarioId);

        String tituloFinal = (titulo == null || titulo.isBlank())
                ? TITULO_PADRAO
                : titulo.trim();
        validarTitulo(tituloFinal);

        Instant agora = Instant.now();
        Conversa conversa = new Conversa();
        conversa.setUsuarioId(usuarioId);
        conversa.setTitulo(tituloFinal);
        conversa.setCriadaEm(agora);
        conversa.setAtualizadaEm(agora);

        conversaRepository.salvar(conversa);
        return conversa;
    }

    public List<Conversa> listar(String usuarioId) {
        exigirUsuario(usuarioId);
        return conversaRepository.listarPorUsuario(usuarioId);
    }

    public Conversa buscar(String usuarioId, String conversaId) {
        exigirUsuario(usuarioId);
        Conversa conversa = conversaRepository.buscarPorIdEUsuario(conversaId, usuarioId);
        if (conversa == null) {
            throw new IllegalArgumentException(NAO_ENCONTRADA);
        }
        return conversa;
    }

    public void renomear(String usuarioId, String conversaId, String novoTitulo) {
        exigirUsuario(usuarioId);

        if (novoTitulo == null || novoTitulo.isBlank()) {
            throw new IllegalArgumentException("O título não pode ficar vazio.");
        }
        String tituloFinal = novoTitulo.trim();
        validarTitulo(tituloFinal);

        if (!conversaRepository.renomear(conversaId, usuarioId, tituloFinal)) {
            throw new IllegalArgumentException(NAO_ENCONTRADA);
        }
    }

    public void excluir(String usuarioId, String conversaId) {
        exigirUsuario(usuarioId);

        // Só exclui se a conversa existir E for do usuário.
        if (!conversaRepository.excluir(conversaId, usuarioId)) {
            throw new IllegalArgumentException(NAO_ENCONTRADA);
        }
        mensagemRepository.excluirPorConversa(conversaId, usuarioId);
    }

    private void exigirUsuario(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new IllegalArgumentException("Usuário não identificado.");
        }
    }

    private void validarTitulo(String titulo) {
        if (titulo.length() > TITULO_MAXIMO) {
            throw new IllegalArgumentException(
                    "O título pode ter no máximo " + TITULO_MAXIMO + " caracteres."
            );
        }
    }
}