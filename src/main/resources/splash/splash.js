const tl = gsap.timeline({
    onComplete: () => {
        // Aguarda 100ms após o fechamento completo para acionar o carregamento da próxima tela no Java
        setTimeout(() => {
            if (window.javaBridge) {
                window.javaBridge.onLoadFinished();
            }
        }, 100);
    }
});

// ----------------------------------------------------
// ESTADOS INICIAIS
// ----------------------------------------------------
gsap.set([".circle-dark", ".circle-mid", ".circle-light"], { scale: 0 });
gsap.set(".brand-name", { scale: 0.3, opacity: 0 });

// ----------------------------------------------------
// 1. ANIMAÇÃO DE ENTRADA (Zoom In das Ondas e do Texto)
// ----------------------------------------------------
tl.to(".circle-dark", {
    scale: 22,
    duration: 1.0,
    ease: "power2.inOut"
})
    .to(".circle-mid", {
        scale: 18,
        duration: 0.9,
        ease: "power2.inOut"
    }, "-=0.65")
    .to(".circle-light", {
        scale: 14,
        duration: 0.8,
        ease: "power2.inOut"
    }, "-=0.55")
    .to(".brand-name", {
        scale: 1,
        opacity: 1,
        duration: 0.7,
        ease: "power3.out"
    }, "-=0.4")

    // ----------------------------------------------------
    // 2. PAUSA BREVE
    // ----------------------------------------------------
    .to({}, { duration: 0.6 })

    // ----------------------------------------------------
    // 3. ANIMAÇÃO DE SAÍDA (REVERSE): Zoom Out dos Círculos + Texto
    // ----------------------------------------------------

    // Texto encolhe em Zoom Out e desvanece
    .to(".brand-name", {
        scale: 0.1,
        opacity: 0,
        duration: 0.5,
        ease: "power2.in"
    })

    // O círculo interno (Claro) encolhe primeiro junto com o texto
    .to(".circle-light", {
        scale: 0,
        duration: 0.6,
        ease: "power2.in"
    }, "<") // Dispara exatamente junto com o Zoom Out do texto

    // O círculo do meio (Médio) encolhe logo em seguida
    .to(".circle-mid", {
        scale: 0,
        duration: 0.6,
        ease: "power2.in"
    }, "-=0.45")

    // O círculo externo (Escuro) fecha por último, revelando o fundo para a transição
    .to(".circle-dark", {
        scale: 0,
        duration: 0.7,
        ease: "power2.in"
    }, "-=0.45");