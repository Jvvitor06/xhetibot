package br.com.chatia.exception;

public class IAException extends RuntimeException {


        public IAException(String mensagemAmigavel) {
            super(mensagemAmigavel);
        }
        public IAException(String mensagemAmigavel, Throwable causa){
            super(mensagemAmigavel, causa);
        }
    }

