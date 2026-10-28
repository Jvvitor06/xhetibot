/*
 * Animação de abertura com GSAP.
 * Ao terminar, avisa o Java (window.javaApp.finalizar()) para abrir o login.
 */

// Duração TOTAL da animação, em segundos (definida pelo desenvolvedor).
const DURACAO_TOTAL = 5;

// Para funcionar sem internet, coloque o arquivo gsap.min.js nesta pasta.
// Se ele não existir, tenta o CDN. Se nada carregar, segue direto para o login.
const GSAP_LOCAL = "gsap.min.js";
const GSAP_CDN = "https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.5/gsap.min.js";

let finalizado = false;

function finalizar() {
    if (finalizado) return;
    finalizado = true;

    // Avisa o Java: o SplashView escuta os alerts do WebView e abre o login.
    alert("CHATIA_FINALIZAR");
}

function iniciar() {
    const titulo = document.querySelector("#titulo");

}
function iniciar() {
    const titulo = document.querySelector("#titulo");

    // Divide o título em letras para animar uma por uma
    titulo.innerHTML = Array.from(titulo.textContent)
        .map(c => '<span class="letra">' + (c === " " ? "&nbsp;" : c) + "</span>")
        .join("");

    gsap.set(".intro", { visibility: "visible" });

    const tl = gsap.timeline({ onComplete: () => finalizar() });

    tl
        // 1. brilho de fundo
        .fromTo("#glow",
            { opacity: 0, scale: 0.5 },
            { opacity: 1, scale: 1, duration: 1.2, ease: "power2.out" }, 0)

        // 2. logo (balão) entra girando
        .fromTo("#bubble",
            { scale: 0, rotation: -25, opacity: 0 },
            { scale: 1, rotation: 0, opacity: 1, duration: 0.9, ease: "back.out(1.8)" }, 0.1)

        // 3. pontinhos aparecem em sequência e "digitam"
        .from(".dot", { scale: 0, duration: 0.35, stagger: 0.15, ease: "back.out(3)" }, "-=0.3")
        .to(".dot", { y: -8, duration: 0.25, stagger: 0.1, yoyo: true, repeat: 1, ease: "power1.inOut" })

        // 4. nome da aplicação, letra por letra
        .from(".letra", { opacity: 0, y: 30, duration: 0.5, stagger: 0.06, ease: "power3.out" }, "-=0.2")

        // 5. elementos secundários
        .from("#subtitle", { opacity: 0, y: 12, duration: 0.6, ease: "power2.out" }, "-=0.2")
        .fromTo("#barra",
            { scaleX: 0 },
            { scaleX: 1, duration: 1.1, ease: "power1.inOut" }, "-=0.3")

        // 6. animação de saída
        .to("#intro", { y: -30, opacity: 0, duration: 0.6, ease: "power2.in" }, "+=0.25")
        .to("#glow", { opacity: 0, duration: 0.4 }, "<");

    // Ajusta a linha do tempo inteira para a duração definida acima
    tl.duration(DURACAO_TOTAL);
}

function carregarScript(src, aoCarregar, aoFalhar) {
    const s = document.createElement("script");
    s.src = src;
    s.onload = aoCarregar;
    s.onerror = aoFalhar;
    document.head.appendChild(s);
}

carregarScript(GSAP_LOCAL, iniciar, () => {
    carregarScript(GSAP_CDN, iniciar, () => {
        console.error("GSAP não pôde ser carregado. Seguindo sem animação.");
        setTimeout(() => finalizar(), 800);
    });
});

// Trava de segurança: nunca deixa o usuário preso no splash.
setTimeout(() => finalizar(), (DURACAO_TOTAL + 10) * 1000);