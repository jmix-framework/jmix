window.applyTheme = () => {
    const storedTheme = localStorage.getItem("jmix.app.theme");
    const theme = storedTheme
        ? storedTheme
        : window.matchMedia("(prefers-color-scheme: dark)").matches
            ? "dark"
            : "light";
    document.documentElement.setAttribute("theme", theme);
};

window.matchMedia("(prefers-color-scheme: dark)")
    .addEventListener('change', function () {
        window.applyTheme()
    });

window.applyTheme();